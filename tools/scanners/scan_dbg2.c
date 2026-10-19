#include <stdio.h>
#include <stdlib.h>
int main(void) {
    FILE *f = fopen("C:\\Users\\Nova\\haegemonia-mod\\Game.exe", "rb");
    fseek(f, 0, SEEK_END); long sz = ftell(f); fseek(f, 0, SEEK_SET);
    unsigned char *b = malloc(sz);
    fread(b, 1, sz, f); fclose(f);
    printf("sz=%ld\n", sz);
    for (long p = 0x3cc9b2; p < 0x3cc9c0; p++) printf("%02X ", b[p]);
    printf("\n");
    return 0;
}
