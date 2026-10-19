#include "chrtool.h"
#include <stdio.h>
#include <string.h>

int main(void)
{
    ChrDoc d;
    GenParams p;
    char err[256] = "";
    doc_init(&d);
    memset(&p, 0, sizeof(p));
    strcpy(p.family, "Arial");
    p.designPx = 24;
    p.cellH = 0;
    p.hpad = 0;
    p.vpad = 1;
    p.S = 2;
    p.firstChar = 33;
    p.count = 222;
    printf("building 2x...\n");
    if (!gen_build(&d, &p, err, sizeof(err))) {
        printf("FAIL 2x: %s\n", err);
        return 1;
    }
    printf("2x ok: atlas %ux%u cell %ux%u\n", d.imgW[0], d.imgH[0], d.cellW, d.cellH);
    chr_save_tga(&d, 0, "gtest_2x.tga", err, sizeof(err));
    chr_save(&d, "gtest_2x.chr", err, sizeof(err));
    doc_free(&d);

    doc_init(&d);
    p.S = 4;
    printf("building 4x...\n");
    if (!gen_build(&d, &p, err, sizeof(err))) {
        printf("FAIL 4x: %s\n", err);
        return 1;
    }
    printf("4x ok: atlas %ux%u cell %ux%u\n", d.imgW[0], d.imgH[0], d.cellW, d.cellH);
    chr_save_tga(&d, 0, "gtest_4x.tga", err, sizeof(err));
    chr_save(&d, "gtest_4x.chr", err, sizeof(err));
    doc_free(&d);

    doc_init(&d);
    p.S = 1;
    printf("building 1x...\n");
    if (!gen_build(&d, &p, err, sizeof(err))) {
        printf("FAIL 1x: %s\n", err);
        return 1;
    }
    printf("1x ok: atlas %ux%u cell %ux%u\n", d.imgW[0], d.imgH[0], d.cellW, d.cellH);
    doc_free(&d);
    printf("all ok\n");
    return 0;
}
