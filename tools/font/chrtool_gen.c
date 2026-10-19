#include "chrtool.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static void sanitize_name(const char *in, char *out, int outsz)
{
    int i = 0, j = 0;
    for (i = 0; in[i] && j < outsz - 1; i++) {
        char c = in[i];
        if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_') {
            out[j++] = c;
        } else if (c == ' ') {
            if (j == 0 || out[j - 1] != '_') out[j++] = '_';
        }
    }
    out[j] = 0;
    if (j == 0) strcpy(out, "font");
}

static int render_scan(HDC hdc, HFONT fnt, int ch, uint8_t **pixOut, int *bwOut, int *bhOut,
                       int *il, int *ir, int *it, int *ib, char *err, int errsz)
{
    RECT r = { 0, 0, 10000, 10000 };
    RECT rr;
    int bw, bh, x, y;
    BITMAPINFO bi;
    void *pv = NULL;
    HBITMAP dib;
    HDC th;
    HGDIOBJ odib, otf;
    int ok = 1;
    uint8_t *copy;

    DrawTextA(hdc, (LPCSTR)&ch, 1, &r, DT_CALCRECT | DT_LEFT | DT_TOP | DT_NOPREFIX | DT_SINGLELINE);
    if (r.right <= r.left || r.bottom <= r.top) { r.left = 0; r.top = 0; r.right = 8; r.bottom = 8; }
    bw = r.right - r.left + 8;
    bh = r.bottom - r.top + 8;
    memset(&bi, 0, sizeof(bi));
    bi.bmiHeader.biSize = sizeof(BITMAPINFOHEADER);
    bi.bmiHeader.biWidth = bw;
    bi.bmiHeader.biHeight = -bh;
    bi.bmiHeader.biPlanes = 1;
    bi.bmiHeader.biBitCount = 32;
    bi.bmiHeader.biCompression = BI_RGB;
    dib = CreateDIBSection(hdc, &bi, DIB_RGB_COLORS, &pv, NULL, 0);
    if (!dib) { snprintf(err, errsz, "CreateDIBSection failed"); return 0; }
    memset(pv, 0, (size_t)bw * bh * 4);
    th = CreateCompatibleDC(hdc);
    odib = SelectObject(th, dib);
    otf = SelectObject(th, fnt);
    SetTextColor(th, RGB(255, 255, 255));
    SetBkMode(th, TRANSPARENT);
    rr.left = 2;
    rr.top = 2;
    rr.right = 2 + (r.right - r.left);
    rr.bottom = 2 + (r.bottom - r.top);
    DrawTextA(th, (LPCSTR)&ch, 1, &rr, DT_LEFT | DT_TOP | DT_NOPREFIX | DT_SINGLELINE);
    GdiFlush();
    {
        const uint8_t *p32 = (const uint8_t *)pv;
        *il = bw;
        *ir = -1;
        *it = bh;
        *ib = -1;
        for (y = 0; y < bh; y++) {
            for (x = 0; x < bw; x++) {
                if (p32[(y * bw + x) * 4 + 2] > 0) {
                    if (x < *il) *il = x;
                    if (x > *ir) *ir = x;
                    if (y < *it) *it = y;
                    if (y > *ib) *ib = y;
                }
            }
        }
        if (*ir < 0) { *il = 2; *ir = 2; *it = 2; *ib = 2; ok = 0; }
    }
    copy = malloc((size_t)bw * bh * 4);
    if (copy) memcpy(copy, pv, (size_t)bw * bh * 4);
    SelectObject(th, otf);
    SelectObject(th, odib);
    DeleteDC(th);
    DeleteObject(dib);
    if (!copy) { snprintf(err, errsz, "oom"); return 0; }
    *pixOut = copy;
    *bwOut = bw;
    *bhOut = bh;
    return ok;
}

int gen_build(ChrDoc *d, const GenParams *p, char *err, int errsz)
{
    int S = (int)(p->S > 0 ? p->S : 2.0f);
    int P;
    HDC hdc;
    HFONT fnt;
    HGDIOBJ ofnt;
    TEXTMETRICA tm;
    int capTop, refIt, minH, cellH_d, cellH, cellW_a, cellW_d, maxAdv = 1;
    int *adv = NULL, *abcA = NULL;
    int i, count, first, pages;
    int atlas;
    int cols, capPerPage;
    int imgW, imgH;
    ABC abc;
    int ok = 0;

    doc_free(d);
    d->S = p->S;
    if (S < 1 || S > 16) { snprintf(err, errsz, "scale must be 1..16"); return 0; }
    P = p->designPx * S;
    if (P < 4 || P > 4096) { snprintf(err, errsz, "render size %d out of range", P); return 0; }
    count = p->count;
    first = p->firstChar;
    if (count < 1 || count > 8192) { snprintf(err, errsz, "glyph count %d out of range", count); return 0; }
    if (first < 0 || first + count > 65536) { snprintf(err, errsz, "char range out of range"); return 0; }

    hdc = CreateCompatibleDC(NULL);
    if (!hdc) { snprintf(err, errsz, "CreateCompatibleDC failed"); return 0; }
    fnt = CreateFontA(-P, 0, 0, 0, p->bold ? FW_BOLD : FW_NORMAL, p->italic, 0, 0,
                      DEFAULT_CHARSET, OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS,
                      ANTIALIASED_QUALITY, DEFAULT_PITCH | FF_DONTCARE, p->family);
    if (!fnt) { DeleteDC(hdc); snprintf(err, errsz, "CreateFont failed for '%s'", p->family); return 0; }
    ofnt = SelectObject(hdc, fnt);
    if (!GetTextMetricsA(hdc, &tm)) { SelectObject(hdc, ofnt); DeleteObject(fnt); DeleteDC(hdc); snprintf(err, errsz, "GetTextMetrics failed"); return 0; }

    {
        int refCh = (first <= 0x41 && 0x41 < first + count) ? 0x41 : first;
        int ril, rir, rit, rib, rbw, rbh;
        uint8_t *rbuf = NULL;
        if (!render_scan(hdc, fnt, refCh, &rbuf, &rbw, &rbh, &ril, &rir, &rit, &rib, err, errsz)) {
            SelectObject(hdc, ofnt); DeleteObject(fnt); DeleteDC(hdc);
            return 0;
        }
        free(rbuf);
        refIt = rit;
        capTop = 2 + (int)tm.tmAscent - rit;
        if (capTop < 2) capTop = tm.tmAscent;
    }
    minH = p->vpad + (capTop + (int)tm.tmDescent + 2 + S - 1) / S;
    if (p->cellH > 0 && p->cellH < minH) {
        snprintf(err, errsz, "line H %d is too small for size px %d: need at least %d", p->cellH, p->designPx, minH);
        goto done;
    }
    cellH_d = p->cellH > 0 ? p->cellH : minH;
    if (cellH_d < 1) cellH_d = 1;
    cellH = cellH_d * S;

    adv = calloc(count, sizeof(int));
    abcA = calloc(count, sizeof(int));
    d->glyphs = calloc(count, sizeof(GlyphRec));
    if (!adv || !abcA || !d->glyphs) { snprintf(err, errsz, "oom"); goto done; }
    for (i = 0; i < count; i++) {
        int ch = first + i;
        int a;
        if (GetCharABCWidthsA(hdc, ch, ch, &abc)) {
            a = abc.abcA + (int)abc.abcB + abc.abcC;
            abcA[i] = abc.abcA;
        } else {
            int w32 = 0;
            if (!GetCharWidth32A(hdc, ch, ch, &w32)) w32 = tm.tmAveCharWidth;
            a = (int)w32;
            abcA[i] = 0;
        }
        a += 2 * p->hpad * S;
        if (a < 1) a = 1;
        adv[i] = a;
        if (a > maxAdv) maxAdv = a;
    }
    cellW_a = maxAdv + 4;
    cellW_d = (maxAdv + 3 + S - 1) / S + 2 * p->hpad;
    if (cellW_d < 1) cellW_d = 1;

    atlas = 256 * S;
    imgW = atlas;
    imgH = atlas;
    cols = (atlas - 2) / cellW_a;
    if (cols < 1) cols = 1;
    {
        int rowsPerPage = (atlas - 2) / cellH;
        if (rowsPerPage < 1) rowsPerPage = 1;
        capPerPage = cols * rowsPerPage;
        pages = (count + capPerPage - 1) / capPerPage;
        if (pages > MAX_IMG) {
            snprintf(err, errsz, "font does not fit %d atlas pages (need %d)", MAX_IMG, pages);
            goto done;
        }
    }
    for (i = 0; i < pages; i++) {
        d->pix[i] = calloc((size_t)imgW * imgH * 4, 1);
        if (!d->pix[i]) { snprintf(err, errsz, "oom"); goto done; }
        d->imgW[i] = (uint32_t)imgW;
        d->imgH[i] = (uint32_t)imgH;
        d->imgLoaded[i] = 1;
    }

    for (i = 0; i < count; i++) {
        int ch = first + i;
        int pg = i / capPerPage;
        int j = i % capPerPage;
        int col = j % cols;
        int row = j / cols;
        int cellLeft = 1 + col * cellW_a;
        int cellTop = 1 + row * cellH;
        uint8_t *pgImg = d->pix[pg];
        int il2, ir2, it2, ib2, bw, bh;
        uint8_t *buf = NULL;
        int tx, ty, sx = 0, sy = 0, sw, sh, x, y;

        render_scan(hdc, fnt, ch, &buf, &bw, &bh, &il2, &ir2, &it2, &ib2, err, errsz);
        tx = cellLeft + p->hpad * S + abcA[i] - il2;
        ty = cellTop + p->vpad * S - refIt;
        sw = bw;
        sh = bh;
        if (tx < cellLeft - 4) { sx = (cellLeft - 4) - tx; tx = cellLeft - 4; }
        if (ty < cellTop) { sy = cellTop - ty; ty = cellTop; }
        if (tx + sw > cellLeft + cellW_a - 2) sw = (cellLeft + cellW_a - 2) - tx;
        if (ty + sh > cellTop + cellH - S) sh = (cellTop + cellH - S) - ty;
        if (sx + sw > bw) sw = bw - sx;
        if (sy + sh > bh) sh = bh - sy;
        if (sw > 0 && sh > 0) {
            for (y = 0; y < sh; y++) {
                const uint8_t *src = buf + ((size_t)(sy + y) * bw + sx) * 4;
                uint8_t *dst = pgImg + ((size_t)(ty + y) * imgW + tx) * 4;
                for (x = 0; x < sw; x++) {
                    uint8_t v = src[x * 4 + 2];
                    dst[x * 4] = 255;
                    dst[x * 4 + 1] = 255;
                    dst[x * 4 + 2] = 255;
                    dst[x * 4 + 3] = v;
                }
            }
        }
        free(buf);
        {
            int u1v = tx + ir2 + 1;
            int advu1 = cellLeft + adv[i];
            if (u1v < advu1) u1v = advu1;
            if (u1v > cellLeft + cellW_a - 2) u1v = cellLeft + cellW_a - 2;
            d->glyphs[i].X = (uint16_t)pg;
            d->glyphs[i].u0 = (uint16_t)cellLeft;
            d->glyphs[i].v0 = (uint16_t)cellTop;
            d->glyphs[i].u1 = (uint16_t)u1v;
            d->glyphs[i].v1 = (uint16_t)(cellTop + cellH - S);
        }
    }

    {
        char base[64];
        sanitize_name(p->family, base, 40);
        for (i = 0; i < pages; i++) {
            snprintf(d->names[i], sizeof(d->names[i]), "%s_%dpx%s%s_%03d.tga",
                     base, p->designPx, p->bold ? "b" : "", p->italic ? "i" : "", i);
        }
    }
    d->nameCount = (uint32_t)pages;
    d->cellW = (uint32_t)cellW_d;
    d->cellH = (uint32_t)cellH_d;
    d->firstChar = (uint32_t)first;
    d->recordCount = (uint32_t)count;
    for (i = 0; i < pages; i++)
        img_make_premult(d->pix[i], imgW, imgH, &d->prem[i]);
    d->curImg = 0;
    d->sel = -1;
    ok = 1;

done:
    free(adv);
    free(abcA);
    SelectObject(hdc, ofnt);
    DeleteObject(fnt);
    DeleteDC(hdc);
    return ok;
}
