#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
int main(void) {
    FILE *f = fopen("C:\\Users\\Nova\\haegemonia-mod\\Game.exe", "rb");
    fseek(f, 0, SEEK_END); long sz = ftell(f); fseek(f, 0, SEEK_SET);
    unsigned char *b = malloc(sz);
    fread(b, 1, sz, f); fclose(f);
    for (long p = 0x1000; p < sz - 20; p++) {
        if (b[p] != 0x8B && b[p] != 0x3B) continue;
        int m = b[p+1];
        if ((m & 0xC0) != 0x80) continue;
        if (b[p+2] != 0xB8 || b[p+3] != 0x0A || b[p+4] != 0x00 || b[p+5] != 0x00) continue;
        int r2 = (m >> 3) & 7;
        for (long q = p + 6; q < p + 26 && q < sz - 6; q++) {
            if (b[q] != 0x8B && b[q] != 0x89 && b[q] != 0xC7) continue;
            int mm = b[q+1];
            if (((mm & 0xC0) != 0x80) && ((mm & 0xC0) != 0x40)) continue;
            if ((mm & 0x07) != 0x04) continue;
            int sib = b[q+2];
            int idx = (sib >> 3) & 7;
            int base = sib & 7;
            if (base != 5) continue;
            if (idx != r2) continue;
            int disp;
            if ((mm & 0xC0) == 0x40) disp = (int)(int8_t)b[q+3];
            else memcpy(&disp, b + q + 3, 4);
            printf("count-indexed %s at 0x%X disp=0x%X (count read at 0x%X)\n",
                b[q] == 0x8B ? "load" : "store",
                (unsigned)(q + 0x400000), disp, (unsigned)(p + 0x400000));
            break;
        }
    }
    free(b);
    return 0;
}

