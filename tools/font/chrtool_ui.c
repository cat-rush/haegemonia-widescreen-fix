#include "chrtool.h"
#include <commdlg.h>
#include <windowsx.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>

#define ID_OPEN       101
#define ID_SAVECHR    102
#define ID_SAVETGA    103
#define ID_EXPORT     104
#define ID_IMGCOMBO   105
#define ID_ZOOMCOMBO  106
#define ID_NAMES      107
#define ID_NAMESOK    108
#define ID_CELLW      109
#define ID_CELLH      110
#define ID_FIRST      111
#define ID_COUNT      112
#define ID_HDRAPPLY   113
#define ID_GX         114
#define ID_GU0        115
#define ID_GV0        116
#define ID_GU1        117
#define ID_GV1        118
#define ID_GLYPHAPPLY 119
#define ID_FONTCOMBO  120
#define ID_SIZE       121
#define ID_BOLD       122
#define ID_ITAL       123
#define ID_PADH       124
#define ID_PADV       125
#define ID_CELLH2     126
#define ID_SCALECOMBO 127
#define ID_BUILD      128
#define ID_SAMPLE     129
#define ID_PREVCOMBO  130
#define ID_SELINFO    131

#define PANEL_W 400
#define PREVIEW_H 200
#define STATUS_H 22

static ChrDoc g_doc;
static HWND g_main, g_atlas, g_panel, g_prev, g_status;
static HWND g_ctl[256];
static HFONT g_uiFont;
static int g_zoomIdx = 0;
static const float g_ztab[9] = { 0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 6.0f, 8.0f, 12.0f, 16.0f };
static int g_drag = 0;
static int g_dragX0, g_dragY0;
static uint16_t g_origU0, g_origV0, g_origU1, g_origV1;
static int g_prevScale = 2;

static void SetStatus(const char *fmt, ...);
static void fill_img_combo(void);
static void update_glyph_fields(void);
static void update_scroll(void);

typedef struct {
    int idx;
    GlyphRec before, after;
    int nudge;
} UndoEnt;

#define UNDO_MAX 256
static UndoEnt g_undo[UNDO_MAX];
static int g_undo_n = 0;

static void undo_push(int idx, const GlyphRec *before, const GlyphRec *after, int nudge)
{
    if (before->X == after->X && before->u0 == after->u0 && before->v0 == after->v0 &&
        before->u1 == after->u1 && before->v1 == after->v1) return;
    if (g_undo_n < UNDO_MAX) {
        g_undo_n++;
    } else {
        memmove(&g_undo[0], &g_undo[1], (UNDO_MAX - 1) * sizeof(UndoEnt));
    }
    g_undo[g_undo_n - 1].idx = idx;
    g_undo[g_undo_n - 1].before = *before;
    g_undo[g_undo_n - 1].after = *after;
    g_undo[g_undo_n - 1].nudge = nudge;
}

static void undo_pop(void)
{
    UndoEnt e;
    if (g_undo_n <= 0) { SetStatus("nothing to undo"); return; }
    e = g_undo[g_undo_n - 1];
    g_undo_n--;
    if (e.idx >= 0 && e.idx < (int)g_doc.recordCount) {
        g_doc.glyphs[e.idx] = e.before;
        g_doc.sel = e.idx;
        g_doc.curImg = e.before.X;
        fill_img_combo();
        update_glyph_fields();
        InvalidateRect(g_atlas, NULL, TRUE);
        InvalidateRect(g_prev, NULL, TRUE);
        SetStatus("undone glyph %d", e.idx);
    }
}

static void SetStatus(const char *fmt, ...)
{
    char buf[512];
    va_list va;
    va_start(va, fmt);
    vsnprintf(buf, sizeof(buf), fmt, va);
    va_end(va);
    SetWindowText(g_status, buf);
}

static int get_int(int id)
{
    char b[64];
    GetWindowText(g_ctl[id], b, sizeof(b));
    return atoi(b);
}

static void set_int(int id, int v)
{
    char b[64];
    snprintf(b, sizeof(b), "%d", v);
    SetWindowText(g_ctl[id], b);
}

static HWND MakeButton(HWND parent, const char *t, int id, int x, int y, int w, int h)
{
    HWND hw = CreateWindow("BUTTON", t, WS_CHILD | WS_VISIBLE | WS_TABSTOP,
                           x, y, w, h, parent, (HMENU)(INT_PTR)id, NULL, NULL);
    SendMessage(hw, WM_SETFONT, (WPARAM)g_uiFont, TRUE);
    return hw;
}

static HWND MakeEdit(HWND parent, int id, int x, int y, int w, int h)
{
    HWND hw = CreateWindow("EDIT", "", WS_CHILD | WS_VISIBLE | WS_BORDER | WS_TABSTOP | ES_AUTOHSCROLL,
                           x, y, w, h, parent, (HMENU)(INT_PTR)id, NULL, NULL);
    SendMessage(hw, WM_SETFONT, (WPARAM)g_uiFont, TRUE);
    return hw;
}

static HWND MakeLabel(HWND parent, const char *t, int x, int y, int w, int h)
{
    HWND hw = CreateWindow("STATIC", t, WS_CHILD | WS_VISIBLE,
                           x, y, w, h, parent, NULL, NULL, NULL);
    SendMessage(hw, WM_SETFONT, (WPARAM)g_uiFont, TRUE);
    return hw;
}

static HWND MakeCombo(HWND parent, int id, int x, int y, int w, int h, int dropdown)
{
    HWND hw = CreateWindow("COMBOBOX", "", WS_CHILD | WS_VISIBLE | WS_TABSTOP |
                           (dropdown ? CBS_DROPDOWNLIST : CBS_DROPDOWN) | WS_VSCROLL,
                           x, y, w, h, parent, (HMENU)(INT_PTR)id, NULL, NULL);
    SendMessage(hw, WM_SETFONT, (WPARAM)g_uiFont, TRUE);
    return hw;
}

static HWND MakeCheck(HWND parent, const char *t, int id, int x, int y, int w, int h)
{
    HWND hw = CreateWindow("BUTTON", t, WS_CHILD | WS_VISIBLE | WS_TABSTOP | BS_AUTOCHECKBOX,
                           x, y, w, h, parent, (HMENU)(INT_PTR)id, NULL, NULL);
    SendMessage(hw, WM_SETFONT, (WPARAM)g_uiFont, TRUE);
    return hw;
}

static void fill_img_combo(void)
{
    int i;
    HWND c = g_ctl[ID_IMGCOMBO];
    SendMessage(c, CB_RESETCONTENT, 0, 0);
    for (i = 0; i < (int)g_doc.nameCount; i++) {
        char b[96];
        snprintf(b, sizeof(b), "%d: %s", i, g_doc.names[i]);
        SendMessage(c, CB_ADDSTRING, 0, (LPARAM)b);
    }
    if (g_doc.nameCount) SendMessage(c, CB_SETCURSEL, g_doc.curImg, 0);
}

static void update_header_fields(void)
{
    char b[512];
    int i, n = 0;
    set_int(ID_CELLW, (int)g_doc.cellW);
    set_int(ID_CELLH, (int)g_doc.cellH);
    set_int(ID_FIRST, (int)g_doc.firstChar);
    set_int(ID_COUNT, (int)g_doc.recordCount);
    b[0] = 0;
    for (i = 0; i < (int)g_doc.nameCount; i++) {
        n += snprintf(b + n, sizeof(b) - n, "%s%s", i ? "," : "", g_doc.names[i]);
        if (n >= (int)sizeof(b)) break;
    }
    SetWindowText(g_ctl[ID_NAMES], b);
    fill_img_combo();
}

static void update_glyph_fields(void)
{
    int s = g_doc.sel;
    if (s >= 0 && s < (int)g_doc.recordCount) {
        GlyphRec *g = &g_doc.glyphs[s];
        char b[64];
        set_int(ID_GX, g->X);
        set_int(ID_GU0, g->u0);
        set_int(ID_GV0, g->v0);
        set_int(ID_GU1, g->u1);
        set_int(ID_GV1, g->v1);
        snprintf(b, sizeof(b), "glyph %d  char '%c' (0x%02X)", s, (char)(g_doc.firstChar + s),
                 (unsigned)(g_doc.firstChar + s));
        SetWindowText(g_ctl[ID_SELINFO], b);
    } else {
        SetWindowText(g_ctl[ID_SELINFO], "no glyph selected");
    }
}

static void refresh_all(void)
{
    update_header_fields();
    update_glyph_fields();
    update_scroll();
    InvalidateRect(g_atlas, NULL, TRUE);
    InvalidateRect(g_prev, NULL, TRUE);
}

static int CALLBACK EnumFamProc(const LOGFONTA *lf, const TEXTMETRICA *tm, DWORD type, LPARAM lParam)
{
    HWND cb = (HWND)lParam;
    char last[128];
    (void)tm; (void)type;
    if (!lf) return 1;
    if (SendMessage(cb, CB_GETCOUNT, 0, 0) > 0) {
        SendMessage(cb, CB_GETLBTEXT, SendMessage(cb, CB_GETCOUNT, 0, 0) - 1, (LPARAM)last);
        if (strcmp(last, lf->lfFaceName) == 0) return 1;
    }
    SendMessage(cb, CB_ADDSTRING, 0, (LPARAM)lf->lfFaceName);
    return 1;
}

static void fill_font_combo(void)
{
    HWND cb = g_ctl[ID_FONTCOMBO];
    LOGFONTA lf;
    HDC dc = GetDC(NULL);
    memset(&lf, 0, sizeof(lf));
    lf.lfCharSet = DEFAULT_CHARSET;
    strcpy(lf.lfFaceName, "");
    EnumFontFamiliesExA(dc, &lf, (FONTENUMPROCA)EnumFamProc, (LPARAM)cb, 0);
    ReleaseDC(NULL, dc);
    if (SendMessage(cb, CB_GETCOUNT, 0, 0) > 0) {
        SendMessage(cb, CB_SELECTSTRING, (WPARAM)-1, (LPARAM)"Arial");
        if (SendMessage(cb, CB_GETCURSEL, 0, 0) < 0) SendMessage(cb, CB_SETCURSEL, 0, 0);
    }
}

static void do_open(void)
{
    OPENFILENAMEA ofn;
    char path[MAX_PATH] = "";
    char err[256] = "";
    memset(&ofn, 0, sizeof(ofn));
    ofn.lStructSize = sizeof(ofn);
    ofn.hwndOwner = g_main;
    ofn.lpstrFilter = "CHR font metrics (*.chr)\0*.chr\0All files (*.*)\0*.*\0";
    ofn.lpstrFile = path;
    ofn.nMaxFile = sizeof(path);
    ofn.Flags = OFN_FILEMUSTEXIST | OFN_PATHMUSTEXIST;
    if (!GetOpenFileNameA(&ofn)) return;
    doc_free(&g_doc);
    doc_init(&g_doc);
    if (!chr_load(&g_doc, path, err, sizeof(err))) {
        MessageBox(g_main, err, "Load failed", MB_ICONERROR);
        SetStatus("load failed: %s", err);
        return;
    }
    refresh_all();
    SetStatus("%s : %d glyphs, %d page(s), S=%.2f%s", g_doc.srcPath, (int)g_doc.recordCount,
              (int)g_doc.nameCount, g_doc.S, err[0] ? " (partial)" : "");
    if (err[0]) MessageBox(g_main, err, "Load warning", MB_ICONWARNING);
    SetFocus(g_atlas);
}

static void track_saved_path(const char *path)
{
    const char *p, *q, *s;
    size_t dn;
    strncpy(g_doc.srcPath, path, MAX_PATH - 1);
    g_doc.srcPath[MAX_PATH - 1] = 0;
    p = strrchr(path, '\\');
    q = strrchr(path, '/');
    s = p > q ? p : q;
    if (!s) { strcpy(g_doc.srcDir, "."); return; }
    dn = (size_t)(s - path);
    if (dn == 0) { strcpy(g_doc.srcDir, "\\"); return; }
    if (dn >= MAX_PATH) dn = MAX_PATH - 1;
    memcpy(g_doc.srcDir, path, dn);
    g_doc.srcDir[dn] = 0;
}

static void do_save_chr(void)
{
    OPENFILENAMEA ofn;
    char path[MAX_PATH];
    char err[256];
    snprintf(path, sizeof(path), "%s", g_doc.srcPath[0] ? g_doc.srcPath : "font.chr");
    memset(&ofn, 0, sizeof(ofn));
    ofn.lStructSize = sizeof(ofn);
    ofn.hwndOwner = g_main;
    ofn.lpstrFilter = "CHR font metrics (*.chr)\0*.chr\0All files (*.*)\0*.*\0";
    ofn.lpstrFile = path;
    ofn.nMaxFile = sizeof(path);
    ofn.lpstrDefExt = "chr";
    ofn.Flags = OFN_OVERWRITEPROMPT | OFN_PATHMUSTEXIST;
    if (!GetSaveFileNameA(&ofn)) return;
    if (!chr_save(&g_doc, path, err, sizeof(err))) {
        MessageBox(g_main, err, "Save failed", MB_ICONERROR);
        return;
    }
    track_saved_path(path);
    SetStatus("saved %s", path);
}

static void do_save_tga(void)
{
    OPENFILENAMEA ofn;
    char path[MAX_PATH];
    char err[256];
    if (g_doc.curImg >= (int)g_doc.nameCount) { SetStatus("no atlas page"); return; }
    if (!g_doc.srcDir[0]) strcpy(g_doc.srcDir, ".");
    snprintf(path, sizeof(path), "%s\\%s", g_doc.srcDir, g_doc.names[g_doc.curImg]);
    memset(&ofn, 0, sizeof(ofn));
    ofn.lStructSize = sizeof(ofn);
    ofn.hwndOwner = g_main;
    ofn.lpstrFilter = "TGA image (*.tga)\0*.tga\0All files (*.*)\0*.*\0";
    ofn.lpstrFile = path;
    ofn.nMaxFile = sizeof(path);
    ofn.lpstrDefExt = "tga";
    ofn.Flags = OFN_OVERWRITEPROMPT | OFN_PATHMUSTEXIST;
    if (!GetSaveFileNameA(&ofn)) return;
    if (!chr_save_tga(&g_doc, g_doc.curImg, path, err, sizeof(err))) {
        MessageBox(g_main, err, "Save failed", MB_ICONERROR);
        return;
    }
    SetStatus("saved %s", path);
}

static void backup_if_needed(const char *path)
{
    char bak[MAX_PATH];
    if (GetFileAttributesA(path) == INVALID_FILE_ATTRIBUTES) return;
    snprintf(bak, sizeof(bak), "%s.bakMOD", path);
    if (GetFileAttributesA(bak) == INVALID_FILE_ATTRIBUTES) CopyFileA(path, bak, TRUE);
}

static void do_export(void)
{
    char msg[768];
    int i, n = 0;
    if (!g_doc.srcDir[0] || !g_doc.srcPath[0]) { SetStatus("open or save a .chr first (export goes to its folder)"); return; }
    if (!g_doc.nameCount) { SetStatus("nothing to export"); return; }
    n += snprintf(msg + n, sizeof(msg) - n, "Export to the folder of the loaded font?\n\n%s\n\nFiles:\n  %s\n",
                  g_doc.srcDir, g_doc.srcPath);
    for (i = 0; i < (int)g_doc.nameCount && g_doc.imgLoaded[i]; i++) {
        n += snprintf(msg + n, sizeof(msg) - n, "  %s\\%s\n", g_doc.srcDir, g_doc.names[i]);
    }
    n += snprintf(msg + n, sizeof(msg) - n, "\nExisting files are backed up as .bakMOD.");
    if (MessageBox(g_main, msg, "Export to game folder", MB_YESNO | MB_ICONWARNING) != IDYES) return;
    backup_if_needed(g_doc.srcPath);
    for (i = 0; i < (int)g_doc.nameCount && g_doc.imgLoaded[i]; i++) {
        char p[MAX_PATH];
        snprintf(p, sizeof(p), "%s\\%s", g_doc.srcDir, g_doc.names[i]);
        backup_if_needed(p);
    }
    {
        char err[256];
        if (!chr_save(&g_doc, g_doc.srcPath, err, sizeof(err))) {
            MessageBox(g_main, err, "Export failed", MB_ICONERROR);
            return;
        }
        for (i = 0; i < (int)g_doc.nameCount && g_doc.imgLoaded[i]; i++) {
            char p[MAX_PATH];
            snprintf(p, sizeof(p), "%s\\%s", g_doc.srcDir, g_doc.names[i]);
            if (!chr_save_tga(&g_doc, i, p, err, sizeof(err))) {
                MessageBox(g_main, err, "Export failed", MB_ICONERROR);
                return;
            }
        }
    }
    SetStatus("exported to %s", g_doc.srcDir);
}

static void do_apply_header(void)
{
    uint32_t cw = (uint32_t)max(1, get_int(ID_CELLW));
    uint32_t ch = (uint32_t)max(1, get_int(ID_CELLH));
    uint32_t first = (uint32_t)max(0, get_int(ID_FIRST));
    uint32_t count = (uint32_t)max(0, get_int(ID_COUNT));
    if (count != g_doc.recordCount) {
        GlyphRec *ng = realloc(g_doc.glyphs, (count ? count : 1) * sizeof(GlyphRec));
        if (!ng && count) { SetStatus("oom"); return; }
        g_doc.glyphs = ng;
        if (count > g_doc.recordCount) memset(g_doc.glyphs + g_doc.recordCount, 0, (count - g_doc.recordCount) * sizeof(GlyphRec));
        g_doc.recordCount = count;
    }
    g_doc.cellW = cw;
    g_doc.cellH = ch;
    g_doc.firstChar = first;
    if (g_doc.sel >= (int)count) g_doc.sel = count ? (int)count - 1 : -1;
    refresh_all();
    SetStatus("header applied: cell %ux%u, chars %u..%u (%u glyphs)", cw, ch, first, first + count, count);
}

static void do_apply_glyph(void)
{
    GlyphRec *g;
    int s = g_doc.sel;
    if (s < 0 || s >= (int)g_doc.recordCount) { SetStatus("select a glyph first"); return; }
    g = &g_doc.glyphs[s];
    g->X = (uint16_t)max(0, get_int(ID_GX));
    g->u0 = (uint16_t)max(0, get_int(ID_GU0));
    g->v0 = (uint16_t)max(0, get_int(ID_GV0));
    g->u1 = (uint16_t)max(0, get_int(ID_GU1));
    g->v1 = (uint16_t)max(0, get_int(ID_GV1));
    if ((int)g->X < (int)g_doc.nameCount && g_doc.imgLoaded[g->X]) {
        if (g->u1 > g_doc.imgW[g->X]) g->u1 = (uint16_t)g_doc.imgW[g->X];
        if (g->v1 > g_doc.imgH[g->X]) g->v1 = (uint16_t)g_doc.imgH[g->X];
        if (g->u0 > g->u1) g->u0 = g->u1;
        if (g->v0 > g->v1) g->v0 = g->v1;
    }
    g_doc.curImg = g->X;
    fill_img_combo();
    update_glyph_fields();
    InvalidateRect(g_atlas, NULL, TRUE);
    InvalidateRect(g_prev, NULL, TRUE);
}

static void do_apply_names(void)
{
    char b[512];
    char *p, *ctx = NULL;
    int n = 0;
    GetWindowText(g_ctl[ID_NAMES], b, sizeof(b));
    p = strtok_s(b, ",;", &ctx);
    while (p && n < MAX_IMG) {
        while (*p == ' ') p++;
        if (*p) {
            strncpy(g_doc.names[n], p, 63);
            g_doc.names[n][63] = 0;
            n++;
        }
        p = strtok_s(NULL, ",;", &ctx);
    }
    g_doc.nameCount = n;
    if (g_doc.curImg >= n) g_doc.curImg = n ? n - 1 : 0;
    fill_img_combo();
    SetStatus("names: %d", n);
}

static void do_build(void)
{
    GenParams gp;
    char err[256];
    char fam[128];
    int scIdx;
    GetWindowText(g_ctl[ID_FONTCOMBO], fam, sizeof(fam));
    if (!fam[0]) { SetStatus("pick a font family first"); return; }
    memset(&gp, 0, sizeof(gp));
    strncpy(gp.family, fam, sizeof(gp.family) - 1);
    gp.designPx = get_int(ID_SIZE);
    if (gp.designPx <= 0) gp.designPx = 10;
    gp.bold = SendMessage(g_ctl[ID_BOLD], BM_GETCHECK, 0, 0) == BST_CHECKED;
    gp.italic = SendMessage(g_ctl[ID_ITAL], BM_GETCHECK, 0, 0) == BST_CHECKED;
    gp.hpad = get_int(ID_PADH);
    gp.vpad = get_int(ID_PADV);
    if (gp.hpad < 0) gp.hpad = 0;
    if (gp.vpad < 0) gp.vpad = 0;
    gp.cellH = get_int(ID_CELLH2);
    scIdx = (int)SendMessage(g_ctl[ID_SCALECOMBO], CB_GETCURSEL, 0, 0);
    gp.S = (scIdx == 0) ? 1.0f : (scIdx == 1) ? 2.0f : 4.0f;
    gp.firstChar = max(0, get_int(ID_FIRST));
    gp.count = get_int(ID_COUNT);
    if (gp.count <= 0) gp.count = 222;
    if (!gen_build(&g_doc, &gp, err, sizeof(err))) {
        MessageBox(g_main, err, "Build failed", MB_ICONERROR);
        return;
    }
    refresh_all();
    SetStatus("built %s: %d glyphs, %d page(s) %dx%d, cell %ux%u, S=%.0f", fam, (int)g_doc.recordCount,
              (int)g_doc.nameCount, (int)g_doc.imgW[0], (int)g_doc.imgH[0],
              (int)g_doc.cellW, (int)g_doc.cellH, g_doc.S);
}

static float atlas_zoom(HWND hw, float *ox, float *oy)
{
    RECT rc;
    int iw, ih;
    float z;
    int sx = 0, sy = 0;
    SCROLLINFO si;
    GetClientRect(hw, &rc);
    si.cbSize = sizeof(si);
    si.fMask = SIF_POS;
    if (GetScrollInfo(hw, SB_HORZ, &si)) sx = si.nPos;
    if (GetScrollInfo(hw, SB_VERT, &si)) sy = si.nPos;
    z = g_ztab[g_zoomIdx];
    if (g_doc.curImg < (int)g_doc.nameCount && g_doc.imgLoaded[g_doc.curImg]) {
        float dw, dh;
        iw = (int)g_doc.imgW[g_doc.curImg];
        ih = (int)g_doc.imgH[g_doc.curImg];
        if (z <= 0.0f) {
            z = min((float)(rc.right - 10) / iw, (float)(rc.bottom - 10) / ih);
            if (z <= 0.0f) z = 0.1f;
        }
        dw = iw * z;
        dh = ih * z;
        *ox = (dw < rc.right) ? (rc.right - dw) * 0.5f : (float)(-sx);
        *oy = (dh < rc.bottom) ? (rc.bottom - dh) * 0.5f : (float)(-sy);
    } else {
        z = 1.0f;
        *ox = *oy = 8.0f;
    }
    return z;
}

static void get_scroll(int *sx, int *sy)
{
    SCROLLINFO si;
    si.cbSize = sizeof(si);
    si.fMask = SIF_POS;
    GetScrollInfo(g_atlas, SB_HORZ, &si);
    *sx = si.nPos;
    GetScrollInfo(g_atlas, SB_VERT, &si);
    *sy = si.nPos;
}

static void set_bar(int bar, int content, int page, int pos)
{
    SCROLLINFO si;
    memset(&si, 0, sizeof(si));
    si.cbSize = sizeof(si);
    si.fMask = SIF_ALL | SIF_DISABLENOSCROLL;
    si.nMin = 0;
    si.nMax = max(content, 1);
    si.nPage = page;
    si.nPos = max(0, min(pos, max(0, content - page)));
    SetScrollInfo(g_atlas, bar, &si, FALSE);
}

static void update_scroll(void)
{
    RECT rc;
    float z, ox, oy;
    int iw = 0, ih = 0, sx, sy;
    if (!g_atlas) return;
    GetClientRect(g_atlas, &rc);
    z = atlas_zoom(g_atlas, &ox, &oy);
    if (g_doc.curImg < (int)g_doc.nameCount && g_doc.imgLoaded[g_doc.curImg]) {
        iw = (int)(g_doc.imgW[g_doc.curImg] * z + 0.5f);
        ih = (int)(g_doc.imgH[g_doc.curImg] * z + 0.5f);
    }
    get_scroll(&sx, &sy);
    set_bar(SB_HORZ, iw, rc.right, sx);
    set_bar(SB_VERT, ih, rc.bottom, sy);
}

static void zoom_at(int wheel, int mx, int my)
{
    int old = g_zoomIdx, nz = old;
    RECT rc;
    float z0, z1, ox, oy;
    int iw, ih;
    if (wheel > 0) { if (nz < 8) nz++; }
    else { if (nz > 0) nz--; }
    if (nz == old) return;
    z0 = atlas_zoom(g_atlas, &ox, &oy);
    {
        float imgX = (mx - ox) / z0;
        float imgY = (my - oy) / z0;
        g_zoomIdx = nz;
        z1 = atlas_zoom(g_atlas, &ox, &oy);
        GetClientRect(g_atlas, &rc);
        if (g_doc.curImg < (int)g_doc.nameCount && g_doc.imgLoaded[g_doc.curImg]) {
            iw = (int)(g_doc.imgW[g_doc.curImg] * z1 + 0.5f);
            ih = (int)(g_doc.imgH[g_doc.curImg] * z1 + 0.5f);
            set_bar(SB_HORZ, iw, rc.right, (int)(imgX * z1 - mx + 0.5f));
            set_bar(SB_VERT, ih, rc.bottom, (int)(imgY * z1 - my + 0.5f));
            SetStatus(nz == 0 ? "zoom fit: content %dx%d, view %dx%d" :
                      "zoom %gx: content %dx%d, view %dx%d%s%s",
                      g_ztab[nz], iw, ih, rc.right, rc.bottom,
                      iw > rc.right ? " [H scroll]" : "",
                      ih > rc.bottom ? " [V scroll]" : "");
        }
    }
    update_scroll();
    SendMessage(g_ctl[ID_ZOOMCOMBO], CB_SETCURSEL, nz, 0);
    InvalidateRect(g_atlas, NULL, TRUE);
}

static void paint_atlas(HWND hw)
{
    PAINTSTRUCT ps;
    RECT rc;
    HDC dc;
    float z, ox, oy;
    int i;
    HBRUSH br;
    update_scroll();
    dc = BeginPaint(hw, &ps);
    br = CreateSolidBrush(RGB(10, 10, 10));
    FillRect(dc, &ps.rcPaint, br);
    DeleteObject(br);
    z = atlas_zoom(hw, &ox, &oy);
    if (g_doc.curImg < (int)g_doc.nameCount && g_doc.imgLoaded[g_doc.curImg] && g_doc.pix[g_doc.curImg]) {
        int iw = (int)g_doc.imgW[g_doc.curImg];
        int ih = (int)g_doc.imgH[g_doc.curImg];
        int dw = (int)(iw * z + 0.5f), dh = (int)(ih * z + 0.5f);
        BITMAPINFO bi;
        void *pv = NULL;
        HBITMAP dib;
        HDC mem;
        HGDIOBJ os;
        BLENDFUNCTION bf;
        memset(&bi, 0, sizeof(bi));
        bi.bmiHeader.biSize = sizeof(BITMAPINFOHEADER);
        bi.bmiHeader.biWidth = iw;
        bi.bmiHeader.biHeight = -ih;
        bi.bmiHeader.biPlanes = 1;
        bi.bmiHeader.biBitCount = 32;
        bi.bmiHeader.biCompression = BI_RGB;
        dib = CreateDIBSection(dc, &bi, DIB_RGB_COLORS, &pv, NULL, 0);
        mem = CreateCompatibleDC(dc);
        os = SelectObject(mem, dib);
        memcpy(pv, g_doc.prem[g_doc.curImg], (size_t)iw * ih * 4);
        GdiFlush();
        bf.BlendOp = AC_SRC_OVER;
        bf.BlendFlags = 0;
        bf.SourceConstantAlpha = 255;
        bf.AlphaFormat = AC_SRC_ALPHA;
        AlphaBlend(dc, (int)ox, (int)oy, dw, dh, mem, 0, 0, iw, ih, bf);
        SelectObject(mem, os);
        DeleteDC(mem);
        DeleteObject(dib);
        for (i = 0; i < (int)g_doc.recordCount; i++) {
            GlyphRec *g = &g_doc.glyphs[i];
            int x0, y0, x1, y1;
            HPEN pen;
            HGDIOBJ op;
            if (g->X != g_doc.curImg) continue;
            x0 = (int)(ox + g->u0 * z);
            y0 = (int)(oy + g->v0 * z);
            x1 = (int)(ox + g->u1 * z);
            y1 = (int)(oy + g->v1 * z);
            pen = CreatePen(PS_SOLID, 1, (i == g_doc.sel) ? RGB(255, 80, 80) : RGB(60, 200, 60));
            op = SelectObject(dc, pen);
            SelectObject(dc, GetStockObject(NULL_BRUSH));
            Rectangle(dc, x0, y0, x1, y1);
            SelectObject(dc, op);
            DeleteObject(pen);
            if (i == g_doc.sel) {
                HBRUSH cb2 = CreateSolidBrush(RGB(255, 255, 0));
                RECT r2;
                char lab[8];
                r2.left = x0 - 3; r2.top = y0 - 3; r2.right = x0 + 3; r2.bottom = y0 + 3;
                FillRect(dc, &r2, cb2);
                r2.left = x1 - 3; r2.right = x1 + 3;
                FillRect(dc, &r2, cb2);
                r2.top = y1 - 3; r2.bottom = y1 + 3;
                FillRect(dc, &r2, cb2);
                r2.left = x0 - 3; r2.right = x0 + 3;
                FillRect(dc, &r2, cb2);
                DeleteObject(cb2);
                snprintf(lab, sizeof(lab), "%c", (char)(g_doc.firstChar + i));
                SetBkMode(dc, TRANSPARENT);
                SetTextColor(dc, RGB(255, 255, 100));
                TextOut(dc, x0 + 2, y0 - 16, lab, (int)strlen(lab));
            }
        }
    } else {
        RECT rc2 = { 0, 0, 400, 40 };
        SetBkMode(dc, TRANSPARENT);
        SetTextColor(dc, RGB(160, 160, 160));
        DrawText(dc, "no atlas loaded - use Open (top right panel)", -1, &rc2, DT_LEFT | DT_TOP);
    }
    EndPaint(hw, &ps);
}

static int hit_glyph(int mx, int my, int tol, int *corner)
{
    float z, ox, oy;
    int i;
    z = atlas_zoom(g_atlas, &ox, &oy);
    *corner = 0;
    for (i = (int)g_doc.recordCount - 1; i >= 0; i--) {
        GlyphRec *g = &g_doc.glyphs[i];
        float x0, y0, x1, y1;
        if (g->X != g_doc.curImg) continue;
        x0 = ox + g->u0 * z;
        y0 = oy + g->v0 * z;
        x1 = ox + g->u1 * z;
        y1 = oy + g->v1 * z;
        if (i == g_doc.sel) {
            if (mx >= x1 - tol && mx <= x1 + tol && my >= y1 - tol && my <= y1 + tol) { *corner = 1; return i; }
            if (mx >= x0 - tol && mx <= x0 + tol && my >= y1 - tol && my <= y1 + tol) { *corner = 2; return i; }
            if (mx >= x1 - tol && mx <= x1 + tol && my >= y0 - tol && my <= y0 + tol) { *corner = 3; return i; }
            if (mx >= x0 - tol && mx <= x0 + tol && my >= y0 - tol && my <= y0 + tol) { *corner = 4; return i; }
            if (mx >= x1 - tol && mx <= x1 + tol && my >= y0 - tol && my <= y1 + tol) { *corner = 5; return i; }
            if (mx >= x0 - tol && mx <= x0 + tol && my >= y0 - tol && my <= y1 + tol) { *corner = 6; return i; }
            if (my >= y1 - tol && my <= y1 + tol && mx >= x0 - tol && mx <= x1 + tol) { *corner = 7; return i; }
            if (my >= y0 - tol && my <= y0 + tol && mx >= x0 - tol && mx <= x1 + tol) { *corner = 8; return i; }
        }
        if (mx >= x0 - tol && mx <= x1 + tol && my >= y0 - tol && my <= y1 + tol) return i;
    }
    return -1;
}

static void atlas_mouse(LPARAM lParam, int down, int up)
{
    int mx = GET_X_LPARAM(lParam), my = GET_Y_LPARAM(lParam);
    if (up) {
        if (g_drag) {
            GlyphRec *g = &g_doc.glyphs[g_doc.sel];
            GlyphRec before;
            before.X = g->X;
            before.u0 = g_origU0;
            before.v0 = g_origV0;
            before.u1 = g_origU1;
            before.v1 = g_origV1;
            undo_push(g_doc.sel, &before, g, 0);
            g_drag = 0;
            update_glyph_fields();
            InvalidateRect(g_atlas, NULL, TRUE);
            InvalidateRect(g_prev, NULL, TRUE);
            SetStatus("glyph rect edited (Ctrl+Z to undo)");
        }
        return;
    }
    if (down) {
        int corner = 0;
        int hit = hit_glyph(mx, my, 6, &corner);
        g_drag = 0;
        if (hit >= 0) {
            g_doc.sel = hit;
            g_doc.curImg = g_doc.glyphs[hit].X;
            fill_img_combo();
            update_glyph_fields();
            g_origU0 = g_doc.glyphs[hit].u0;
            g_origV0 = g_doc.glyphs[hit].v0;
            g_origU1 = g_doc.glyphs[hit].u1;
            g_origV1 = g_doc.glyphs[hit].v1;
            g_dragX0 = mx;
            g_dragY0 = my;
            g_drag = corner ? corner : 1;
            SetCapture(g_atlas);
            InvalidateRect(g_atlas, NULL, TRUE);
        }
    } else {
        if (g_drag) {
            float z, ox, oy;
            GlyphRec *g = &g_doc.glyphs[g_doc.sel];
            int dx = (int)((mx - g_dragX0) / atlas_zoom(g_atlas, &ox, &oy));
            int dy = (int)((my - g_dragY0) / atlas_zoom(g_atlas, &ox, &oy));
            int minw = 1;
            (void)z;
            if (g_drag == 1) {
                g->u0 = (uint16_t)max(0, g_origU0 + dx);
                g->v0 = (uint16_t)max(0, g_origV0 + dy);
                g->u1 = (uint16_t)max(g->u0 + minw, g_origU1 + dx);
                g->v1 = (uint16_t)max(g->v0 + minw, g_origV1 + dy);
            } else if (g_drag == 4) {
                g->u0 = (uint16_t)max(0, g_origU0 + dx);
                g->v0 = (uint16_t)max(0, g_origV0 + dy);
            } else if (g_drag == 3) {
                g->u1 = (uint16_t)max(g->u0 + minw, g_origU1 + dx);
                g->v0 = (uint16_t)max(0, g_origV0 + dy);
            } else if (g_drag == 2) {
                g->u0 = (uint16_t)max(0, g_origU0 + dx);
                g->v1 = (uint16_t)max(g->v0 + minw, g_origV1 + dy);
            } else if (g_drag == 5) {
                g->u1 = (uint16_t)max(g->u0 + minw, g_origU1 + dx);
            } else if (g_drag == 6) {
                g->u0 = (uint16_t)max(0, min((int)g->u1 - minw, g_origU0 + dx));
            } else if (g_drag == 7) {
                g->v1 = (uint16_t)max(g->v0 + minw, g_origV1 + dy);
            } else if (g_drag == 8) {
                g->v0 = (uint16_t)max(0, min((int)g->v1 - minw, g_origV0 + dy));
            } else {
                g->u1 = (uint16_t)max(g->u0 + minw, g_origU1 + dx);
                g->v1 = (uint16_t)max(g->v0 + minw, g_origV1 + dy);
            }
            update_glyph_fields();
            InvalidateRect(g_atlas, NULL, TRUE);
            InvalidateRect(g_prev, NULL, TRUE);
        }
    }
}

static void atlas_key(WPARAM w)
{
    GlyphRec *g;
    GlyphRec before;
    int s = g_doc.sel;
    int n = (GetKeyState(VK_SHIFT) & 0x8000) ? 5 : 1;
    if (s < 0 || s >= (int)g_doc.recordCount) return;
    g = &g_doc.glyphs[s];
    before = *g;
    switch (w) {
    case VK_LEFT:  if (g->u0 >= n) { g->u0 -= n; g->u1 -= n; } break;
    case VK_RIGHT: g->u0 += n; g->u1 += n; break;
    case VK_UP:    if (g->v0 >= n) { g->v0 -= n; g->v1 -= n; } break;
    case VK_DOWN:  g->v0 += n; g->v1 += n; break;
    case VK_PRIOR: if (s > 0) g_doc.sel = s - 1; break;
    case VK_NEXT:  if (s < (int)g_doc.recordCount - 1) g_doc.sel = s + 1; break;
    }
    if (w == VK_LEFT || w == VK_RIGHT || w == VK_UP || w == VK_DOWN) {
        if (g_undo_n > 0 && g_undo[g_undo_n - 1].idx == s && g_undo[g_undo_n - 1].nudge) {
            g_undo[g_undo_n - 1].after = *g;
        } else {
            undo_push(s, &before, g, 1);
        }
    }
    update_glyph_fields();
    InvalidateRect(g_atlas, NULL, TRUE);
    InvalidateRect(g_prev, NULL, TRUE);
}

static LRESULT CALLBACK AtlasProc(HWND hw, UINT msg, WPARAM w, LPARAM l)
{
    switch (msg) {
    case WM_PAINT:
        paint_atlas(hw);
        return 0;
    case WM_LBUTTONDOWN:
        SetFocus(hw);
        atlas_mouse(l, 1, 0);
        return 0;
    case WM_LBUTTONUP:
        atlas_mouse(l, 0, 1);
        ReleaseCapture();
        return 0;
    case WM_MOUSEMOVE:
        atlas_mouse(l, 0, 0);
        return 0;
    case WM_KEYDOWN:
        if ((GetKeyState(VK_CONTROL) & 0x8000) && w == 'Z') {
            undo_pop();
            return 0;
        }
        atlas_key(w);
        return 0;
    case WM_MOUSEWHEEL: {
        POINT pt;
        RECT rc;
        int d = GET_WHEEL_DELTA_WPARAM(w);
        pt.x = GET_X_LPARAM(l);
        pt.y = GET_Y_LPARAM(l);
        ScreenToClient(hw, &pt);
        GetClientRect(hw, &rc);
        if (pt.x >= 0 && pt.x < rc.right && pt.y >= 0 && pt.y < rc.bottom)
            zoom_at(d, pt.x, pt.y);
        return 0;
    }
    case WM_VSCROLL:
    case WM_HSCROLL: {
        SCROLLINFO si;
        int pos, page, maxp;
        si.cbSize = sizeof(si);
        si.fMask = SIF_ALL;
        GetScrollInfo(hw, (msg == WM_VSCROLL) ? SB_VERT : SB_HORZ, &si);
        pos = si.nPos;
        page = si.nPage;
        switch (LOWORD(w)) {
        case SB_LINEUP: pos -= 20; break;
        case SB_LINEDOWN: pos += 20; break;
        case SB_PAGEUP: pos -= page; break;
        case SB_PAGEDOWN: pos += page; break;
        case SB_THUMBTRACK: pos = si.nTrackPos; break;
        case SB_TOP: pos = 0; break;
        case SB_BOTTOM: pos = si.nMax; break;
        }
        maxp = si.nMax - (int)si.nPage;
        if (pos < 0) pos = 0;
        if (pos > maxp) pos = maxp;
        if (pos != si.nPos) {
            si.fMask = SIF_POS;
            si.nPos = pos;
            SetScrollInfo(hw, (msg == WM_VSCROLL) ? SB_VERT : SB_HORZ, &si, FALSE);
            InvalidateRect(hw, NULL, TRUE);
        }
        return 0;
    }
    case WM_SETCURSOR:
        if (LOWORD(l) == HTCLIENT) {
            POINT pt;
            int corner = 0;
            int hit;
            LPCSTR cur = IDC_CROSS;
            GetCursorPos(&pt);
            ScreenToClient(hw, &pt);
            hit = hit_glyph(pt.x, pt.y, 6, &corner);
            if (hit >= 0) {
                if (corner == 5 || corner == 6) cur = IDC_SIZEWE;
                else if (corner == 7 || corner == 8) cur = IDC_SIZENS;
                else if (corner == 1 || corner == 4) cur = IDC_SIZENWSE;
                else if (corner == 2 || corner == 3) cur = IDC_SIZENESW;
                else cur = IDC_SIZEALL;
            }
            SetCursor(LoadCursor(NULL, cur));
            return TRUE;
        }
        break;
    }
    return DefWindowProc(hw, msg, w, l);
}

static void paint_preview(HWND hw)
{
    PAINTSTRUCT ps;
    RECT rc;
    HDC dc = BeginPaint(hw, &ps);
    char sample[512];
    int i, n;
    float z = (float)g_prevScale;
    int x = 8, y = 8;
    int lineH = (int)(g_doc.cellH * z);
    HBRUSH br = CreateSolidBrush(RGB(10, 10, 10));
    FillRect(dc, &ps.rcPaint, br);
    DeleteObject(br);
    if (lineH < 4) lineH = 4;
    GetClientRect(hw, &rc);
    if (!g_doc.recordCount) {
        RECT r2 = { 8, 8, rc.right, 40 };
        SetBkMode(dc, TRANSPARENT);
        SetTextColor(dc, RGB(160, 160, 160));
        DrawText(dc, "preview: load or build a font first", -1, &r2, DT_LEFT | DT_TOP);
        EndPaint(hw, &ps);
        return;
    }
    GetWindowText(g_ctl[ID_SAMPLE], sample, sizeof(sample));
    if (!sample[0]) strcpy(sample, "The quick brown fox 0123456789");
    n = (int)strlen(sample);
    for (i = 0; i < n; i++) {
        int c = (unsigned char)sample[i];
        int idx, page, sw, sh, dw, dh;
        GlyphRec *g;
        if (c == '\r') continue;
        if (c == '\n') { x = 8; y += lineH; continue; }
        idx = c - (int)g_doc.firstChar;
        if (idx < 0 || idx >= (int)g_doc.recordCount) { x += (int)(g_doc.cellW * z); continue; }
        g = &g_doc.glyphs[idx];
        page = g->X;
        sw = g->u1 - g->u0;
        sh = g->v1 - g->v0;
        dw = (int)((sw / g_doc.S + 1.0f) * z + 0.5f);
        dh = (int)(sh * z / g_doc.S + 0.5f);
        if (x + dw > rc.right - 8) { x = 8; y += lineH; }
        if (page < (int)g_doc.nameCount && g_doc.imgLoaded[page] && g_doc.prem[page] && sw > 0 && sh > 0) {
            int iw = (int)g_doc.imgW[page], ih = (int)g_doc.imgH[page];
            BITMAPINFO bi;
            void *pv = NULL;
            HBITMAP dib;
            HDC mem;
            HGDIOBJ os;
            BLENDFUNCTION bf;
            memset(&bi, 0, sizeof(bi));
            bi.bmiHeader.biSize = sizeof(BITMAPINFOHEADER);
            bi.bmiHeader.biWidth = iw;
            bi.bmiHeader.biHeight = -ih;
            bi.bmiHeader.biPlanes = 1;
            bi.bmiHeader.biBitCount = 32;
            bi.bmiHeader.biCompression = BI_RGB;
            dib = CreateDIBSection(dc, &bi, DIB_RGB_COLORS, &pv, NULL, 0);
            mem = CreateCompatibleDC(dc);
            os = SelectObject(mem, dib);
            memcpy(pv, g_doc.prem[page], (size_t)iw * ih * 4);
            GdiFlush();
            bf.BlendOp = AC_SRC_OVER;
            bf.BlendFlags = 0;
            bf.SourceConstantAlpha = 255;
            bf.AlphaFormat = AC_SRC_ALPHA;
            AlphaBlend(dc, x, y, dw, dh, mem, g->u0, g->v0, sw, sh, bf);
            SelectObject(mem, os);
            DeleteDC(mem);
            DeleteObject(dib);
        }
        x += dw;
    }
    EndPaint(hw, &ps);
}

static LRESULT CALLBACK PrevProc(HWND hw, UINT msg, WPARAM w, LPARAM l)
{
    switch (msg) {
    case WM_PAINT:
        paint_preview(hw);
        return 0;
    }
    return DefWindowProc(hw, msg, w, l);
}

static void layout_children(void)
{
    RECT rc;
    GetClientRect(g_main, &rc);
    MoveWindow(g_atlas, 0, 0, max(50, rc.right - PANEL_W), max(50, rc.bottom - PREVIEW_H - STATUS_H), TRUE);
    MoveWindow(g_panel, max(50, rc.right - PANEL_W), 0, PANEL_W, max(50, rc.bottom - PREVIEW_H - STATUS_H), TRUE);
    MoveWindow(g_prev, 0, max(0, rc.bottom - PREVIEW_H - STATUS_H), rc.right, PREVIEW_H, TRUE);
    MoveWindow(g_status, 0, max(0, rc.bottom - STATUS_H), rc.right, STATUS_H, TRUE);
}

static LRESULT CALLBACK PanelProc(HWND hw, UINT msg, WPARAM w, LPARAM l)
{
    if (msg == WM_COMMAND) return SendMessage(g_main, WM_COMMAND, w, l);
    return DefWindowProc(hw, msg, w, l);
}

static LRESULT CALLBACK MainProc(HWND hw, UINT msg, WPARAM w, LPARAM l)
{
    switch (msg) {
    case WM_CREATE: {
        g_main = hw;
        g_atlas = CreateWindow("ChrToolAtlas", "", WS_CHILD | WS_VISIBLE | WS_BORDER | WS_VSCROLL | WS_HSCROLL,
                               0, 0, 100, 100, hw, NULL, NULL, NULL);
        g_prev = CreateWindow("ChrToolPreview", "", WS_CHILD | WS_VISIBLE | WS_BORDER,
                              0, 0, 100, 100, hw, NULL, NULL, NULL);
        g_panel = CreateWindow("ChrToolPanel", "", WS_CHILD | WS_VISIBLE,
                               0, 0, PANEL_W, 100, hw, NULL, NULL, NULL);
        g_status = CreateWindow("STATIC", "", WS_CHILD | WS_VISIBLE,
                                0, 0, 100, STATUS_H, hw, NULL, NULL, NULL);
        SendMessage(g_status, WM_SETFONT, (WPARAM)g_uiFont, TRUE);

        g_ctl[ID_OPEN] = MakeButton(g_panel, "Open", ID_OPEN, 8, 8, 88, 24);
        g_ctl[ID_SAVECHR] = MakeButton(g_panel, "Save chr", ID_SAVECHR, 99, 8, 96, 24);
        g_ctl[ID_SAVETGA] = MakeButton(g_panel, "SaveTGA", ID_SAVETGA, 198, 8, 96, 24);
        g_ctl[ID_EXPORT] = MakeButton(g_panel, "Export", ID_EXPORT, 297, 8, 96, 24);

        MakeLabel(g_panel, "Page:", 8, 40, 34, 20);
        g_ctl[ID_IMGCOMBO] = MakeCombo(g_panel, ID_IMGCOMBO, 44, 38, 140, 300, 1);
        MakeLabel(g_panel, "Zoom:", 190, 40, 36, 20);
        g_ctl[ID_ZOOMCOMBO] = MakeCombo(g_panel, ID_ZOOMCOMBO, 228, 38, 110, 200, 1);
        {
            static const char *zl[9] = { "fit", "1x", "2x", "3x", "4x", "6x", "8x", "12x", "16x" };
            int zi;
            for (zi = 0; zi < 9; zi++) SendMessage(g_ctl[ID_ZOOMCOMBO], CB_ADDSTRING, 0, (LPARAM)zl[zi]);
        }
        SendMessage(g_ctl[ID_ZOOMCOMBO], CB_SETCURSEL, 0, 0);

        MakeLabel(g_panel, "Names:", 8, 68, 42, 20);
        g_ctl[ID_NAMES] = MakeEdit(g_panel, ID_NAMES, 50, 66, 270, 22);
        g_ctl[ID_NAMESOK] = MakeButton(g_panel, "Set", ID_NAMESOK, 324, 66, 62, 22);

        MakeLabel(g_panel, "cell W", 8, 96, 38, 20);
        g_ctl[ID_CELLW] = MakeEdit(g_panel, ID_CELLW, 48, 94, 52, 22);
        MakeLabel(g_panel, "cell H", 104, 96, 38, 20);
        g_ctl[ID_CELLH] = MakeEdit(g_panel, ID_CELLH, 144, 94, 52, 22);
        MakeLabel(g_panel, "first char", 200, 96, 52, 20);
        g_ctl[ID_FIRST] = MakeEdit(g_panel, ID_FIRST, 254, 94, 46, 22);
        MakeLabel(g_panel, "glyph count", 8, 122, 66, 20);
        g_ctl[ID_COUNT] = MakeEdit(g_panel, ID_COUNT, 76, 120, 46, 22);
        g_ctl[ID_HDRAPPLY] = MakeButton(g_panel, "Apply header", ID_HDRAPPLY, 126, 120, 80, 22);
        g_ctl[ID_SELINFO] = MakeLabel(g_panel, "-", 210, 122, 184, 20);

        MakeLabel(g_panel, "page", 8, 152, 30, 20);
        g_ctl[ID_GX] = MakeEdit(g_panel, ID_GX, 40, 150, 52, 22);
        MakeLabel(g_panel, "U left", 96, 152, 34, 20);
        g_ctl[ID_GU0] = MakeEdit(g_panel, ID_GU0, 132, 150, 52, 22);
        MakeLabel(g_panel, "V top", 188, 152, 34, 20);
        g_ctl[ID_GV0] = MakeEdit(g_panel, ID_GV0, 224, 150, 52, 22);
        MakeLabel(g_panel, "U right", 8, 180, 40, 20);
        g_ctl[ID_GU1] = MakeEdit(g_panel, ID_GU1, 50, 178, 52, 22);
        MakeLabel(g_panel, "V bottom", 106, 180, 52, 20);
        g_ctl[ID_GV1] = MakeEdit(g_panel, ID_GV1, 160, 178, 52, 22);
        g_ctl[ID_GLYPHAPPLY] = MakeButton(g_panel, "Apply glyph", ID_GLYPHAPPLY, 216, 178, 70, 22);
        MakeLabel(g_panel, "wheel zoom / drag edges-corners / arrows nudge / Ctrl+Z", 8, 206, 390, 18);

        g_ctl[ID_FONTCOMBO] = MakeCombo(g_panel, ID_FONTCOMBO, 8, 232, 384, 400, 1);
        MakeLabel(g_panel, "size px", 8, 266, 42, 20);
        g_ctl[ID_SIZE] = MakeEdit(g_panel, ID_SIZE, 52, 264, 60, 22);
        SetWindowText(g_ctl[ID_SIZE], "10");
        g_ctl[ID_BOLD] = MakeCheck(g_panel, "bold", ID_BOLD, 118, 266, 56, 20);
        g_ctl[ID_ITAL] = MakeCheck(g_panel, "ital", ID_ITAL, 176, 266, 56, 20);
        MakeLabel(g_panel, "atlas scale", 236, 266, 60, 20);
        g_ctl[ID_SCALECOMBO] = MakeCombo(g_panel, ID_SCALECOMBO, 298, 264, 80, 200, 1);
        SendMessage(g_ctl[ID_SCALECOMBO], CB_ADDSTRING, 0, (LPARAM)"1x");
        SendMessage(g_ctl[ID_SCALECOMBO], CB_ADDSTRING, 0, (LPARAM)"2x");
        SendMessage(g_ctl[ID_SCALECOMBO], CB_ADDSTRING, 0, (LPARAM)"4x");
        SendMessage(g_ctl[ID_SCALECOMBO], CB_SETCURSEL, 1, 0);

        MakeLabel(g_panel, "pad H", 8, 294, 34, 20);
        g_ctl[ID_PADH] = MakeEdit(g_panel, ID_PADH, 44, 292, 56, 22);
        SetWindowText(g_ctl[ID_PADH], "0");
        MakeLabel(g_panel, "pad V", 104, 294, 34, 20);
        g_ctl[ID_PADV] = MakeEdit(g_panel, ID_PADV, 140, 292, 56, 22);
        SetWindowText(g_ctl[ID_PADV], "2");
        MakeLabel(g_panel, "line H (0=auto)", 200, 294, 90, 20);
        g_ctl[ID_CELLH2] = MakeEdit(g_panel, ID_CELLH2, 292, 292, 56, 22);
        SetWindowText(g_ctl[ID_CELLH2], "12");
        g_ctl[ID_BUILD] = MakeButton(g_panel, "BUILD FONT", ID_BUILD, 8, 320, 384, 26);

        MakeLabel(g_panel, "Preview text (multi-line, word wrap):", 8, 358, 250, 20);
        g_ctl[ID_SAMPLE] = CreateWindow("EDIT", "", WS_CHILD | WS_VISIBLE | WS_BORDER | WS_TABSTOP |
                                        ES_MULTILINE | ES_AUTOVSCROLL | ES_WANTRETURN | WS_VSCROLL,
                                        8, 380, 384, 64, g_panel, (HMENU)(INT_PTR)ID_SAMPLE, NULL, NULL);
        SendMessage(g_ctl[ID_SAMPLE], WM_SETFONT, (WPARAM)g_uiFont, TRUE);
        SetWindowText(g_ctl[ID_SAMPLE], "The quick brown fox jumps over the lazy dog\r\n0123456789 ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        MakeLabel(g_panel, "Preview scale:", 8, 452, 90, 20);
        g_ctl[ID_PREVCOMBO] = MakeCombo(g_panel, ID_PREVCOMBO, 100, 450, 80, 200, 1);
        SendMessage(g_ctl[ID_PREVCOMBO], CB_ADDSTRING, 0, (LPARAM)"1x");
        SendMessage(g_ctl[ID_PREVCOMBO], CB_ADDSTRING, 0, (LPARAM)"2x");
        SendMessage(g_ctl[ID_PREVCOMBO], CB_ADDSTRING, 0, (LPARAM)"4x");
        SendMessage(g_ctl[ID_PREVCOMBO], CB_SETCURSEL, 1, 0);

        fill_font_combo();
        SetStatus("open a .chr or pick a system font and press BUILD FONT");
        return 0;
    }
    case WM_SIZE:
        layout_children();
        update_scroll();
        InvalidateRect(g_atlas, NULL, TRUE);
        return 0;
    case WM_CTLCOLORLISTBOX:
        if (g_uiFont) SelectObject((HDC)w, g_uiFont);
        return (LRESULT)GetStockObject(WHITE_BRUSH);
    case WM_COMMAND:
        switch (LOWORD(w)) {
        case ID_OPEN: do_open(); return 0;
        case ID_SAVECHR: do_save_chr(); return 0;
        case ID_SAVETGA: do_save_tga(); return 0;
        case ID_EXPORT: do_export(); return 0;
        case ID_NAMESOK: do_apply_names(); return 0;
        case ID_HDRAPPLY: do_apply_header(); return 0;
        case ID_GLYPHAPPLY: do_apply_glyph(); return 0;
        case ID_BUILD: do_build(); return 0;
        case ID_IMGCOMBO:
            if (HIWORD(w) == CBN_SELCHANGE) {
                g_doc.curImg = (int)SendMessage(g_ctl[ID_IMGCOMBO], CB_GETCURSEL, 0, 0);
                if (g_doc.curImg < 0) g_doc.curImg = 0;
                update_scroll();
                InvalidateRect(g_atlas, NULL, TRUE);
            }
            return 0;
        case ID_ZOOMCOMBO:
            if (HIWORD(w) == CBN_SELCHANGE) {
                g_zoomIdx = (int)SendMessage(g_ctl[ID_ZOOMCOMBO], CB_GETCURSEL, 0, 0);
                if (g_zoomIdx < 0) g_zoomIdx = 0;
                update_scroll();
                InvalidateRect(g_atlas, NULL, TRUE);
            }
            return 0;
        case ID_PREVCOMBO:
            if (HIWORD(w) == CBN_SELCHANGE) {
                static const int psTab[3] = { 1, 2, 4 };
                int ci = (int)SendMessage(g_ctl[ID_PREVCOMBO], CB_GETCURSEL, 0, 0);
                if (ci >= 0 && ci < 3) g_prevScale = psTab[ci];
                InvalidateRect(g_prev, NULL, TRUE);
            }
            return 0;
        }
        return 0;
    case WM_DESTROY:
        PostQuitMessage(0);
        return 0;
    }
    return DefWindowProc(hw, msg, w, l);
}

int WINAPI WinMain(HINSTANCE inst, HINSTANCE prev, LPSTR cmdline, int show)
{
    WNDCLASSA wc;
    MSG msg;
    HWND hw;
    (void)prev; (void)cmdline;
    doc_init(&g_doc);
    g_uiFont = CreateFontA(-12, 0, 0, 0, FW_NORMAL, 0, 0, 0, DEFAULT_CHARSET,
                           OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, CLEARTYPE_QUALITY,
                           DEFAULT_PITCH | FF_DONTCARE, "Calibri");
    if (!g_uiFont) g_uiFont = (HFONT)GetStockObject(DEFAULT_GUI_FONT);

    memset(&wc, 0, sizeof(wc));
    wc.style = CS_HREDRAW | CS_VREDRAW;
    wc.lpfnWndProc = MainProc;
    wc.hInstance = inst;
    wc.hCursor = LoadCursor(NULL, IDC_ARROW);
    wc.hbrBackground = (HBRUSH)(COLOR_BTNFACE + 1);
    wc.lpszClassName = "ChrToolMain";
    RegisterClassA(&wc);

    memset(&wc, 0, sizeof(wc));
    wc.style = CS_HREDRAW | CS_VREDRAW;
    wc.lpfnWndProc = AtlasProc;
    wc.hInstance = inst;
    wc.hCursor = LoadCursor(NULL, IDC_CROSS);
    wc.lpszClassName = "ChrToolAtlas";
    RegisterClassA(&wc);

    memset(&wc, 0, sizeof(wc));
    wc.style = CS_HREDRAW | CS_VREDRAW;
    wc.lpfnWndProc = PrevProc;
    wc.hInstance = inst;
    wc.hCursor = LoadCursor(NULL, IDC_ARROW);
    wc.lpszClassName = "ChrToolPreview";
    RegisterClassA(&wc);

    memset(&wc, 0, sizeof(wc));
    wc.style = CS_HREDRAW | CS_VREDRAW;
    wc.lpfnWndProc = PanelProc;
    wc.hInstance = inst;
    wc.hCursor = LoadCursor(NULL, IDC_ARROW);
    wc.hbrBackground = (HBRUSH)(COLOR_BTNFACE + 1);
    wc.lpszClassName = "ChrToolPanel";
    RegisterClassA(&wc);

    hw = CreateWindowA("ChrToolMain", "CHR Font Tool - Haegemonia font atlas builder",
                       WS_OVERLAPPEDWINDOW, CW_USEDEFAULT, CW_USEDEFAULT, 1500, 900,
                       NULL, NULL, inst, NULL);
    if (!hw) return 1;
    ShowWindow(hw, show);
    UpdateWindow(hw);
    while (GetMessage(&msg, NULL, 0, 0) > 0) {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }
    doc_free(&g_doc);
    if (g_uiFont && g_uiFont != (HFONT)GetStockObject(DEFAULT_GUI_FONT)) DeleteObject(g_uiFont);
    return (int)msg.wParam;
}
