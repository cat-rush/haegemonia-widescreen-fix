#include <windows.h>
#include <stdio.h>

static int scan(const unsigned char *pv, int bw, int bh, int *l, int *r, int *t, int *b)
{
    int x, y;
    *l = 100000; *r = -1; *t = 100000; *b = -1;
    for (y = 0; y < bh; y++) {
        for (x = 0; x < bw; x++) {
            if (pv[(y * bw + x) * 4 + 2] > 8) {
                if (x < *l) *l = x;
                if (x > *r) *r = x;
                if (y < *t) *t = y;
                if (y > *b) *b = y;
            }
        }
    }
    return *r >= 0;
}

int main(void)
{
    const char *chars = "Wj.MyX!Aga,py";
    int P = 48;
    HDC hdc = CreateCompatibleDC(NULL);
    HFONT f = CreateFontA(-P, 0, 0, 0, FW_NORMAL, 0, 0, 0, DEFAULT_CHARSET,
                          OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS,
                          ANTIALIASED_QUALITY, DEFAULT_PITCH | FF_DONTCARE, "Arial");
    SelectObject(hdc, f);
    {
        TEXTMETRICA tm;
        GetTextMetricsA(hdc, &tm);
        printf("TM: height=%ld ascent=%ld descent=%ld intLeading=%ld\n",
               tm.tmHeight, tm.tmAscent, tm.tmDescent, tm.tmInternalLeading);
    }
    printf("ch abcA abcB abcC | calcRect l,t,r,b (w) | ink l,t,r,b | IL+asc guess\n");
    for (int i = 0; chars[i]; i++) {
        char ch = chars[i];
        ABC abc;
        GetCharABCWidthsA(hdc, ch, ch, &abc);
        RECT r = {0, 0, 10000, 10000};
        DrawTextA(hdc, &ch, 1, &r, DT_CALCRECT | DT_LEFT | DT_TOP | DT_NOPREFIX | DT_SINGLELINE);
        int bw = r.right - r.left + 4;
        int bh = r.bottom - r.top + 4;
        BITMAPINFO bi;
        void *pv = NULL;
        memset(&bi, 0, sizeof(bi));
        bi.bmiHeader.biSize = sizeof(BITMAPINFOHEADER);
        bi.bmiHeader.biWidth = bw;
        bi.bmiHeader.biHeight = -bh;
        bi.bmiHeader.biPlanes = 1;
        bi.bmiHeader.biBitCount = 32;
        bi.bmiHeader.biCompression = BI_RGB;
        HBITMAP dib = CreateDIBSection(hdc, &bi, DIB_RGB_COLORS, &pv, NULL, 0);
        memset(pv, 0, (size_t)bw * bh * 4);
        HDC th = CreateCompatibleDC(hdc);
        HGDIOBJ od = SelectObject(th, dib);
        HGDIOBJ of = SelectObject(th, f);
        SetTextColor(th, RGB(255, 255, 255));
        SetBkMode(th, TRANSPARENT);
        RECT rr = {2, 2, 2 + bw - 4, 2 + bh - 4};
        DrawTextA(th, &ch, 1, &rr, DT_LEFT | DT_TOP | DT_NOPREFIX | DT_SINGLELINE);
        GdiFlush();
        int l, rr2, t, b;
        scan((const unsigned char *)pv, bw, bh, &l, &rr2, &t, &b);
        printf("'%c' %4d %4u %4d | %d,%d,%d,%d (w=%d) | ink %d..%d, %d..%d\n",
               ch, abc.abcA, abc.abcB, abc.abcC, r.left, r.top, r.right, r.bottom,
               r.right - r.left, l, rr2, t, b);
        SelectObject(th, of);
        SelectObject(th, od);
        DeleteDC(th);
        DeleteObject(dib);
    }
    DeleteObject(f);
    DeleteDC(hdc);
    return 0;
}
