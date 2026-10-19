#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
int main(void) {
    FILE *f = fopen("C:\\Users\\Nova\\haegemonia-mod\\Game.exe", "rb");
    fseek(f, 0, SEEK_END); long sz = ftell(f); fseek(f, 0, SEEK_SET);
    unsigned char *b = malloc(sz);
    fread(b, 1, sz, f); fclose(f);
    uint32_t targets[] = {0x7d3100, 0x7d30fc, 0x7d3706, 0x7d1ef2};
    const char *names[] = {"store3100", "d30fc", "d3706", "d1ef2"};
    for (long p = 0x1000; p < sz - 5; p++) {
        if (b[p] != 0xE8 && b[p] != 0xE9) continue;
        int32_t rel; memcpy(&rel, b + p + 1, 4);
        uint32_t tgt = (uint32_t)(p + 5) + 0x400000 + (uint32_t)rel;
        for (int i = 0; i < 4; i++) {
            if (tgt == targets[i]) printf("%c %s at 0x%X\n", b[p] == 0xE8 ? 'c' : 'j', names[i], (unsigned)(p + 0x400000));
        }
    }
    return 0;
}
