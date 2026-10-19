#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>

int main(void) {
    FILE *f = fopen("C:\\Users\\Nova\\haegemonia-mod\\Game.exe", "rb");
    if (!f) { printf("no file\n"); return 1; }
    fseek(f, 0, SEEK_END);
    long sz = ftell(f);
    fseek(f, 0, SEEK_SET);
    unsigned char *b = malloc(sz);
    fread(b, 1, sz, f);
    fclose(f);

    /* find all VAs of single-char strings '0'..'7' */
    unsigned char *set = calloc(sz, 1); /* set[off] = 1 if "X\0" at off with X in 0..7 */
    for (long i = 0; i < sz - 1; i++) {
        if (b[i] >= '0' && b[i] <= '7' && b[i+1] == 0) set[i] = 1;
    }
    /* scan for arrays of 4 pointers, all pointing to single-digit strings */
    long n = 0;
    for (long p = 0; p <= sz - 16; p += 4) {
        uint32_t v0, v1, v2, v3;
        memcpy(&v0, b+p, 4); memcpy(&v1, b+p+4, 4);
        memcpy(&v2, b+p+8, 4); memcpy(&v3, b+p+12, 4);
        long o0 = (long)v0 - 0x400000, o1 = (long)v1 - 0x400000,
             o2 = (long)v2 - 0x400000, o3 = (long)v3 - 0x400000;
        if (o0 >= 0 && o0 < sz && set[o0] && o1 >= 0 && o1 < sz && set[o1] &&
            o2 >= 0 && o2 < sz && set[o2] && o3 >= 0 && o3 < sz && set[o3]) {
            printf("VA 0x%08lX: '%c' '%c' '%c' '%c'  (strs 0x%08X 0x%08X 0x%08X 0x%08X)\n",
                (unsigned long)p + 0x400000, b[o0], b[o1], b[o2], b[o3],
                v0, v1, v2, v3);
            if (++n > 60) break;
        }
    }
    printf("total=%ld\n", n);
    free(set); free(b);
    return 0;
}
