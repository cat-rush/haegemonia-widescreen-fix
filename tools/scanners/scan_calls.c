#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
int main(void) {
    FILE *f = fopen("C:\\Users\\Nova\\haegemonia-mod\\Game.exe", "rb");
    if (!f) { printf("nofile\n"); return 1; }
    fseek(f, 0, SEEK_END); long sz = ftell(f); fseek(f, 0, SEEK_SET);
    unsigned char *b = malloc(sz);
    fread(b, 1, sz, f); fclose(f);
    fprintf(stderr, "scan start sz=%ld\n", sz);
    uint32_t targets[] = {0x7d2d10, 0x7d2e1a, 0x7d2f84};
    const char *names[] = {"hideby", "helper", "setrow"};
    for (long p = 0x1000; p < sz - 5; p++) {
        if (b[p] != 0xE8 && b[p] != 0xE9) continue;
        int32_t rel; memcpy(&rel, b + p + 1, 4);
        uint32_t tgt = (uint32_t)(p + 5) + 0x400000 + (uint32_t)rel;
        for (int i = 0; i < 3; i++) {
            if (tgt == targets[i]) {
                printf("%c %s at 0x%X\n", b[p] == 0xE8 ? 'c' : 'j', names[i], (unsigned)(p + 0x400000));
                fflush(stdout);
            }
        }
    }
    fprintf(stderr, "scan done\n");
    return 0;
}
