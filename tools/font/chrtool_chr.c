#include "chrtool.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define STB_IMAGE_IMPLEMENTATION
#define STBI_ONLY_PNG
#define STBI_ONLY_JPEG
#define STBI_ONLY_BMP
#define STBI_NO_LINEAR
#define STBI_NO_HDR
#include "stb_image.h"

static uint32_t rd32(const uint8_t *b)
{
    return (uint32_t)b[0] | ((uint32_t)b[1] << 8) | ((uint32_t)b[2] << 16) | ((uint32_t)b[3] << 24);
}

static uint16_t rd16(const uint8_t *b)
{
    return (uint16_t)(b[0] | (b[1] << 8));
}

static void wr32(uint8_t *b, uint32_t v)
{
    b[0] = (uint8_t)v; b[1] = (uint8_t)(v >> 8); b[2] = (uint8_t)(v >> 16); b[3] = (uint8_t)(v >> 24);
}

static void wr16(uint8_t *b, uint16_t v)
{
    b[0] = (uint8_t)v; b[1] = (uint8_t)(v >> 8);
}

void doc_init(ChrDoc *d)
{
    memset(d, 0, sizeof(*d));
    d->S = 2.0f;
    d->sel = -1;
}

void doc_free(ChrDoc *d)
{
    int i;
    free(d->glyphs);
    d->glyphs = NULL;
    for (i = 0; i < MAX_IMG; i++) {
        free(d->pix[i]); d->pix[i] = NULL;
        free(d->prem[i]); d->prem[i] = NULL;
    }
    d->nameCount = 0;
    d->recordCount = 0;
    memset(d->imgLoaded, 0, sizeof(d->imgLoaded));
    memset(d->imgW, 0, sizeof(d->imgW));
    memset(d->imgH, 0, sizeof(d->imgH));
    d->sel = -1;
}

int img_load_tga(const char *path, uint8_t **out, int *w, int *h, char *err, int errsz)
{
    FILE *f = fopen(path, "rb");
    uint8_t hdr[18];
    int type, bpp, topdown, idlen, row, i;
    uint8_t *buf;
    if (!f) { snprintf(err, errsz, "cannot open %s", path); return 0; }
    if (fread(hdr, 1, 18, f) != 18) { fclose(f); snprintf(err, errsz, "short tga header: %s", path); return 0; }
    type = hdr[2];
    if (type != 2 && type != 10) { fclose(f); snprintf(err, errsz, "unsupported tga type %d: %s", type, path); return 0; }
    bpp = hdr[16];
    if (bpp != 32 && bpp != 24) { fclose(f); snprintf(err, errsz, "unsupported tga bpp %d: %s", bpp, path); return 0; }
    *w = rd16(hdr + 12);
    *h = rd16(hdr + 14);
    if (*w < 1 || *h < 1 || *w > 8192 || *h > 8192) { fclose(f); snprintf(err, errsz, "bad tga dims %dx%d: %s", *w, *h, path); return 0; }
    topdown = (hdr[17] & 0x20) != 0;
    idlen = hdr[0];
    fseek(f, idlen, SEEK_CUR);
    buf = malloc((size_t)(*w) * (*h) * 4);
    if (!buf) { fclose(f); snprintf(err, errsz, "oom: %s", path); return 0; }
    if (type == 2) {
        int bpc = bpp / 8;
        uint8_t *rowbuf = malloc((size_t)(*w) * bpc);
        if (!rowbuf) { free(buf); fclose(f); snprintf(err, errsz, "oom: %s", path); return 0; }
        for (row = 0; row < *h; row++) {
            int dstrow = topdown ? row : (*h - 1 - row);
            uint8_t *d = buf + (size_t)dstrow * (*w) * 4;
            if (fread(rowbuf, 1, (size_t)(*w) * bpc, f) != (size_t)(*w) * bpc) {
                free(rowbuf); free(buf); fclose(f);
                snprintf(err, errsz, "short tga data: %s", path);
                return 0;
            }
            if (bpp == 32) {
                memcpy(d, rowbuf, (size_t)(*w) * 4);
            } else {
                int x;
                for (x = 0; x < *w; x++) {
                    d[x * 4] = rowbuf[x * 3];
                    d[x * 4 + 1] = rowbuf[x * 3 + 1];
                    d[x * 4 + 2] = rowbuf[x * 3 + 2];
                    d[x * 4 + 3] = 255;
                }
            }
        }
        free(rowbuf);
    } else {
        int x = 0, y = 0, bpc = bpp / 8;
        while (y < *h) {
            int c, packet, n;
            uint8_t px[4] = {0, 0, 0, 255};
            if (fread(&c, 1, 1, f) != 1) break;
            packet = c & 0x80;
            n = (c & 0x7F) + 1;
            if (packet) {
                if (fread(px, 1, bpc, f) != (size_t)bpc) break;
                for (i = 0; i < n && y < *h; i++) {
                    int dstrow = topdown ? y : (*h - 1 - y);
                    uint8_t *d = buf + (size_t)dstrow * (*w) * 4 + (size_t)x * 4;
                    d[0] = px[0]; d[1] = px[1]; d[2] = px[2]; d[3] = (bpp == 32) ? px[3] : 255;
                    if (++x >= *w) { x = 0; y++; }
                }
            } else {
                for (i = 0; i < n && y < *h; i++) {
                    uint8_t px2[4] = {0, 0, 0, 255};
                    int dstrow;
                    uint8_t *d;
                    if (fread(px2, 1, bpc, f) != (size_t)bpc) break;
                    dstrow = topdown ? y : (*h - 1 - y);
                    d = buf + (size_t)dstrow * (*w) * 4 + (size_t)x * 4;
                    d[0] = px2[0]; d[1] = px2[1]; d[2] = px2[2]; d[3] = (bpp == 32) ? px2[3] : 255;
                    if (++x >= *w) { x = 0; y++; }
                }
            }
        }
    }
    fclose(f);
    *out = buf;
    return 1;
}

int img_load_generic(const char *path, uint8_t **out, int *w, int *h, char *err, int errsz)
{
    int x, y;
    const unsigned char *s = stbi_load(path, w, h, NULL, 4);
    uint8_t *buf;
    if (!s) { snprintf(err, errsz, "cannot load image: %s", path); return 0; }
    buf = malloc((size_t)(*w) * (*h) * 4);
    if (!buf) { stbi_image_free((void *)s); snprintf(err, errsz, "oom: %s", path); return 0; }
    for (y = 0; y < *h; y++) {
        for (x = 0; x < *w; x++) {
            const unsigned char *p = s + ((size_t)y * *w + x) * 4;
            uint8_t *d = buf + ((size_t)y * *w + x) * 4;
            d[0] = p[2]; d[1] = p[1]; d[2] = p[0]; d[3] = p[3];
        }
    }
    stbi_image_free((void *)s);
    *out = buf;
    return 1;
}

void img_make_premult(uint8_t *pix, int w, int h, uint8_t **prem)
{
    size_t n = (size_t)w * h, i;
    if (!*prem) *prem = malloc(n * 4);
    if (!*prem) return;
    for (i = 0; i < n; i++) {
        int a = pix[i * 4 + 3];
        (*prem)[i * 4]     = (uint8_t)((pix[i * 4] * a + 127) / 255);
        (*prem)[i * 4 + 1] = (uint8_t)((pix[i * 4 + 1] * a + 127) / 255);
        (*prem)[i * 4 + 2] = (uint8_t)((pix[i * 4 + 2] * a + 127) / 255);
        (*prem)[i * 4 + 3] = (uint8_t)a;
    }
}

static void split_dir(const char *path, char *dir, int dirsz)
{
    const char *p = strrchr(path, '\\');
    const char *q = strrchr(path, '/');
    const char *s = p > q ? p : q;
    if (!s) { strncpy(dir, ".", dirsz); return; }
    size_t n = (size_t)(s - path);
    if (n == 0) { strncpy(dir, "\\", dirsz); return; }
    if (n >= (size_t)dirsz) n = dirsz - 1;
    memcpy(dir, path, n);
    dir[n] = 0;
}

int chr_load(ChrDoc *d, const char *path, char *err, int errsz)
{
    FILE *f;
    long len;
    uint8_t *b;
    uint32_t off = 12, i;
    f = fopen(path, "rb");
    if (!f) { snprintf(err, errsz, "cannot open %s", path); return 0; }
    fseek(f, 0, SEEK_END);
    len = ftell(f);
    fseek(f, 0, SEEK_SET);
    if (len < 16 || len > 64 * 1024 * 1024) { fclose(f); snprintf(err, errsz, "bad file size: %s", path); return 0; }
    b = malloc((size_t)len);
    if (!b) { fclose(f); snprintf(err, errsz, "oom: %s", path); return 0; }
    if (fread(b, 1, (size_t)len, f) != (size_t)len) { free(b); fclose(f); snprintf(err, errsz, "read fail: %s", path); return 0; }
    fclose(f);
    if (memcmp(b, "INFO", 4) != 0) { free(b); snprintf(err, errsz, "not a .chr (bad magic): %s", path); return 0; }
    d->size = rd32(b + 4);
    d->nameCount = rd32(b + 8);
    if (d->nameCount > MAX_IMG) { free(b); snprintf(err, errsz, "too many images (%u): %s", d->nameCount, path); return 0; }
    for (i = 0; i < d->nameCount; i++) {
        uint32_t nl = rd32(b + off);
        if (off + 4 + nl > (uint32_t)len || nl >= 64) { free(b); snprintf(err, errsz, "bad name entry: %s", path); return 0; }
        memcpy(d->names[i], b + off + 4, nl);
        d->names[i][nl] = 0;
        off += 4 + nl;
    }
    if (off + 16 > (uint32_t)len) { free(b); snprintf(err, errsz, "truncated header: %s", path); return 0; }
    d->cellW = rd32(b + off);
    d->cellH = rd32(b + off + 4);
    d->firstChar = rd32(b + off + 8);
    d->recordCount = rd32(b + off + 12);
    off += 16;
    if (off + d->recordCount * 10 > (uint32_t)len) { free(b); snprintf(err, errsz, "truncated records: %s", path); return 0; }
    d->glyphs = calloc(d->recordCount ? d->recordCount : 1, sizeof(GlyphRec));
    if (!d->glyphs) { free(b); snprintf(err, errsz, "oom: %s", path); return 0; }
    for (i = 0; i < d->recordCount; i++) {
        d->glyphs[i].X = rd16(b + off);
        d->glyphs[i].u0 = rd16(b + off + 2);
        d->glyphs[i].v0 = rd16(b + off + 4);
        d->glyphs[i].u1 = rd16(b + off + 6);
        d->glyphs[i].v1 = rd16(b + off + 8);
        off += 10;
    }
    free(b);
    strncpy(d->srcPath, path, MAX_PATH - 1);
    d->srcPath[MAX_PATH - 1] = 0;
    split_dir(path, d->srcDir, MAX_PATH);
    for (i = 0; i < d->nameCount; i++) {
        char p[MAX_PATH];
        char e2[256];
        snprintf(p, sizeof(p), "%s\\%s", d->srcDir, d->names[i]);
        if (img_load_tga(p, &d->pix[i], (int *)&d->imgW[i], (int *)&d->imgH[i], e2, sizeof(e2))) {
            d->imgLoaded[i] = 1;
        } else {
            static const char *exts[] = { "png", "jpg", "jpeg", "bmp" };
            int k;
            for (k = 0; k < 4; k++) {
                char q[MAX_PATH];
                char base[MAX_PATH];
                char *dot = strrchr(d->names[i], '.');
                strncpy(base, d->names[i], sizeof(base) - 1);
                base[sizeof(base) - 1] = 0;
                if (dot && (size_t)(dot - d->names[i]) < sizeof(base)) base[dot - d->names[i]] = 0;
                snprintf(q, sizeof(q), "%s\\%s.%s", d->srcDir, base, exts[k]);
                if (img_load_generic(q, &d->pix[i], (int *)&d->imgW[i], (int *)&d->imgH[i], e2, sizeof(e2))) {
                    d->imgLoaded[i] = 1;
                    break;
                }
            }
            if (!d->imgLoaded[i]) snprintf(err, errsz, "image missing: %s (%s)", p, e2);
        }
        if (d->imgLoaded[i]) img_make_premult(d->pix[i], (int)d->imgW[i], (int)d->imgH[i], &d->prem[i]);
    }
    d->curImg = 0;
    d->sel = d->recordCount ? 0 : -1;
    if (d->imgLoaded[0] && d->imgW[0] >= 64) {
        float s = (float)d->imgW[0] / 256.0f;
        d->S = (s < 1.0f) ? 1.0f : s;
    }
    return 1;
}

int chr_save(const ChrDoc *d, const char *path, char *err, int errsz)
{
    uint32_t total = 12, off = 12, i;
    uint8_t *b;
    FILE *f;
    for (i = 0; i < d->nameCount; i++) total += 4 + (uint32_t)strlen(d->names[i]);
    total += 16 + d->recordCount * 10;
    b = malloc(total);
    if (!b) { snprintf(err, errsz, "oom"); return 0; }
    memcpy(b, "INFO", 4);
    wr32(b + 4, total);
    wr32(b + 8, d->nameCount);
    for (i = 0; i < d->nameCount; i++) {
        uint32_t nl = (uint32_t)strlen(d->names[i]);
        wr32(b + off, nl);
        memcpy(b + off + 4, d->names[i], nl);
        off += 4 + nl;
    }
    wr32(b + off, d->cellW);
    wr32(b + off + 4, d->cellH);
    wr32(b + off + 8, d->firstChar);
    wr32(b + off + 12, d->recordCount);
    off += 16;
    for (i = 0; i < d->recordCount; i++) {
        wr16(b + off, d->glyphs[i].X);
        wr16(b + off + 2, d->glyphs[i].u0);
        wr16(b + off + 4, d->glyphs[i].v0);
        wr16(b + off + 6, d->glyphs[i].u1);
        wr16(b + off + 8, d->glyphs[i].v1);
        off += 10;
    }
    f = fopen(path, "wb");
    if (!f) { free(b); snprintf(err, errsz, "cannot write %s", path); return 0; }
    if (fwrite(b, 1, total, f) != total) { fclose(f); free(b); snprintf(err, errsz, "write fail: %s", path); return 0; }
    fclose(f);
    free(b);
    return 1;
}

int chr_save_tga(const ChrDoc *d, int img, const char *path, char *err, int errsz)
{
    uint8_t hdr[18];
    static const uint8_t footer[26] = {
        'T','R','U','E','V','I','S','I','O','N','-','X','F','I','L','E','.','\0',
        0,0,0,0, 0,0,0,0
    };
    FILE *f;
    int row, w, h;
    if (img < 0 || img >= (int)d->nameCount || !d->imgLoaded[img] || !d->pix[img]) {
        snprintf(err, errsz, "no atlas data for page %d", img);
        return 0;
    }
    w = (int)d->imgW[img];
    h = (int)d->imgH[img];
    memset(hdr, 0, sizeof(hdr));
    hdr[2] = 2;
    hdr[12] = (uint8_t)(w & 0xFF);
    hdr[13] = (uint8_t)(w >> 8);
    hdr[14] = (uint8_t)(h & 0xFF);
    hdr[15] = (uint8_t)(h >> 8);
    hdr[16] = 32;
    hdr[17] = 8;
    f = fopen(path, "wb");
    if (!f) { snprintf(err, errsz, "cannot write %s", path); return 0; }
    if (fwrite(hdr, 1, 18, f) != 18) { fclose(f); snprintf(err, errsz, "write fail: %s", path); return 0; }
    for (row = h - 1; row >= 0; row--) {
        if (fwrite(d->pix[img] + (size_t)row * w * 4, 1, (size_t)w * 4, f) != (size_t)w * 4) {
            fclose(f);
            snprintf(err, errsz, "write fail: %s", path);
            return 0;
        }
    }
    fwrite(footer, 1, sizeof(footer), f);
    fclose(f);
    return 1;
}
