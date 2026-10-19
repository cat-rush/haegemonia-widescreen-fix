#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
int main(void) {
    FILE *f = fopen("C:\\Users\\Nova\\haegemonia-mod\\Game.exe", "rb");
    fseek(f, 0, SEEK_END); long sz = ftell(f); fseek(f, 0, SEEK_SET);
    unsigned char *b = malloc(sz);
    fread(b, 1, sz, f); fclose(f);
    printf("size=%ld byte@0x3D2690=%02X\n", sz, b[0x3D2690]);
    for (long p = 0x3D2680; p < 0x3D26A0; p++) {
        if (b[p] == 0xE8) {
            int32_t rel; memcpy(&rel, b+p+1, 4);
            uint32_t tgt = (uint32_t)(p+5) + 0x400000 + (uint32_t)rel;
            printf("call@0x%lX -> 0x%X\n", (unsigned long)p, tgt);
        }
    }
    return 0;
}
