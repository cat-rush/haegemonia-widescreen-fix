#ifndef CHRTOOL_H
#define CHRTOOL_H

#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <stdint.h>

#define MAX_IMG 8

typedef struct {
    uint16_t X, u0, v0, u1, v1;
} GlyphRec;

typedef struct {
    uint32_t size;
    uint32_t nameCount;
    char names[MAX_IMG][64];
    uint32_t cellW, cellH, firstChar, recordCount;
    GlyphRec *glyphs;
    uint32_t imgW[MAX_IMG], imgH[MAX_IMG];
    uint8_t *pix[MAX_IMG];
    uint8_t *prem[MAX_IMG];
    int imgLoaded[MAX_IMG];
    char srcPath[MAX_PATH];
    char srcDir[MAX_PATH];
    int sel;
    int curImg;
    float S;
} ChrDoc;

void doc_init(ChrDoc *d);
void doc_free(ChrDoc *d);
int chr_load(ChrDoc *d, const char *path, char *err, int errsz);
int chr_save(const ChrDoc *d, const char *path, char *err, int errsz);
int chr_save_tga(const ChrDoc *d, int img, const char *path, char *err, int errsz);
int img_load_tga(const char *path, uint8_t **pix, int *w, int *h, char *err, int errsz);
int img_load_generic(const char *path, uint8_t **pix, int *w, int *h, char *err, int errsz);
void img_make_premult(uint8_t *pix, int w, int h, uint8_t **prem);

typedef struct {
    char family[128];
    int bold, italic;
    int designPx;
    int cellH;
    int hpad, vpad;
    float S;
    int firstChar, count;
} GenParams;

int gen_build(ChrDoc *d, const GenParams *p, char *err, int errsz);

#endif
