#include "chrtool.h"
#include <stdio.h>
#include <string.h>

int main(int argc, char **argv)
{
    ChrDoc d;
    char err[256] = "";
    char out[512];
    if (argc < 2) { printf("usage: chrrt <file.chr>\n"); return 1; }
    doc_init(&d);
    if (!chr_load(&d, argv[1], err, sizeof(err))) {
        printf("LOAD FAIL: %s\n", err);
        return 1;
    }
    printf("loaded: names=%u cell=%ux%u first=%u count=%u S=%.2f\n",
           d.nameCount, d.cellW, d.cellH, d.firstChar, d.recordCount, d.S);
    snprintf(out, sizeof(out), "%s.rt", argv[1]);
    if (!chr_save(&d, out, err, sizeof(err))) {
        printf("SAVE FAIL: %s\n", err);
        return 1;
    }
    {
        FILE *fa = fopen(argv[1], "rb");
        FILE *fb = fopen(out, "rb");
        int same = 1;
        long la, lb;
        fseek(fa, 0, SEEK_END); la = ftell(fa); fseek(fa, 0, SEEK_SET);
        fseek(fb, 0, SEEK_END); lb = ftell(fb); fseek(fb, 0, SEEK_SET);
        if (la != lb) same = 0;
        else {
            unsigned char a, b;
            long i;
            for (i = 0; i < la; i++) {
                if (fread(&a, 1, 1, fa) != 1 || fread(&b, 1, 1, fb) != 1 || a != b) { same = 0; break; }
            }
        }
        fclose(fa);
        fclose(fb);
        printf("roundtrip %s: %s (len %ld)\n", out, same ? "IDENTICAL" : "DIFFERS", la);
        remove(out);
        return same ? 0 : 1;
    }
}
