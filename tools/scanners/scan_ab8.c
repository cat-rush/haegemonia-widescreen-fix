#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
int main(void) {
    FILE *f = fopen("C:\\Users\\Nova\\haegemonia-mod\\Game.exe", "rb");
    fseek(f, 0, SEEK_END); long sz = ftell(f); fseek(f, 0, SEEK_SET);
    unsigned char *b = malloc(sz);
    fread(b, 1, sz, f); fclose(f);
    for (long p = 0x1000; p < sz - 8; p++) {
        if ((b[p] != 0x89 && b[p] != 0xC7 && b[p] != 0x8B && b[p] != 0x83 && b[p] != 0x3B && b[p] != 0xFF && b[p] != 0x81)) continue;
        int m = b[p+1];
        if ((m & 0xC0) != 0x80) continue;
        if (b[p+2] != 0xB8 || b[p+3] != 0x0A || b[p+4] != 0x00 || b[p+5] != 0x00) continue;
        int op = b[p];
        printf("%s at 0x%X modrm=%02X\n", op == 0x89 ? "STORE" : op == 0xC7 ? "STOREi" : op == 0x8B ? "load" : op == 0x83 ? "cmpi" : op == 0x3B ? "cmp" : op == 0xFF ? "inc/dec" : "arith",
            (unsigned)(p + 0x400000), m);
    }
    free(b);
    return 0;
}
