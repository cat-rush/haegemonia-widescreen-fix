// hghook.c - Haegemonia widescreen fix, v68 stable (2026-10-19)
// v68: screens opened WHILE a message is up no longer get the message
// view shift. The fix-20 scene classification shifted ANY default-
// camera scene first seen with a message active - a screen
// (empire/planet/research) opened during a message was misclassified
// as the message scene and its 3D frame got +240px. Now only the
// FIRST default-camera scene ever seen with a message up (the message
// window's own scene - it renders the instant a message opens, before
// any screen can) is shifted; later registrations while a message is
// active are screens and stay st=1 (never shifted). User-verified.
// v67: the message window's Accept/Decline CLICKABLE AREAS. The message
// screen's own strip ([[msg+8]+0x38]) holds the button rows: the Ok row
// is a single span (kind 3, right-anchored, worked) but the
// Accept/Decline row is MULTI-span -> fell into kind 0 (centered
// mapping) -> clicks landed at the un-shifted authored positions while
// the visuals are right-anchored (user: clicking the Accept visual
// triggered Decline). Fix: the strip IDENTITY overrides the kind - all
// rows of the message strip map right-anchored (rawx >= 816.67 ->
// x = rawx-266.67, else dead). User-verified.
// v66: the BR panel's BACK ARROW (gfx/interface/screenselect/back.bmod)
// is its own mesh with the panel's ORIGINAL authored OBST x (186.3332).
// The panel's file patch (186.33 -> 331.67) never touched it, so the
// arrow rendered ~240px left of the panel on the planet/empire/research
// screens (user-reported; the click area was already fine). The same
// +145.34 patch got the arrow culled by the game (invisible), and a
// device-copy matrix shift did not move its draw at all (the screen
// scene positions its models from the loaded OBST, not the device
// matrix), so the fix is an in-buffer OBST patch +72.0 authored units
// (user-calibrated 2026-10-19). User-verified on all three screens.
// v65: 3D-world mouse ROTATION made crisp (user-verified). The vanilla
// mouse-camera handler (FUN_0078fb7e) accumulates the drag distance into
// the camera pitch/rotate TARGETS each frame and the camera animator
// (FUN_0065b0e2) eases the angles toward the targets - that easing is
// the vanilla "too smooth" (floaty/gliding) feel. Two parts:
// - RotationBoost: the handler's two setter call sites (0x790052 ->
//   FUN_0065e00c pitch, 0x7900BA -> FUN_0065de73 rotate) are wrapped
//   (E8 kept, rel32 -> stub) and the speed argument (50.0) is
//   multiplied by RotationBoost - the animator approaches that much
//   faster. Target accumulation, clamping and flags stay vanilla.
// - RotationSens: applied at the handler ENTRY (0x78FB7E, prologue
//   55 8B EC 81 EC 8C 00 00 00): the mouse driver position is scaled
//   toward the battle's stored anchor (+0x90/+0x94) with rounding, so
//   the handler's own dx/dy computation sees RotationSens-scaled
//   deltas. The game's drag-rotate is self-referential (its per-frame
//   delta re-reads the camera's stored target), so scaling the target
//   itself decays the rotation geometrically - the deltas must be
//   scaled at the source. Zoom mode (battle+0x7647c != 0) is skipped.
// Gated by hghook.ini [RotationSmooth] RotationSmoothEnabled=1 /
// RotationBoost=4.0 / RotationSens=1.0 (0 = nothing patched).
// v64: the portrait message window's BUTTONS (span 546..789, the
// Messages.inf layout) now pick right-anchored: rawx >= 816.67 maps
// x = rawx - 266.67 (the v58 +240px visual move), the old centered
// place (x ~1230..1660 px) is dead. User-verified.
// v63: the CLICKABLE AREAS re-done screen by screen. The strip picker
// (FUN_00551ca5, both call sites) now maps the pick x per the row's
// span signature: menu strips keep the centering shift, full-width
// starmap rows get the full-16:9 scale (800/designW), the TR top bar
// maps right-anchored into its user-calibrated zone (1420..1920 x
// 0..166 px), the BR screenselect into its zone (1720..1920 x
// 1014..1080 px, via the v62 draw shift). The v24.81 whole-line
// expansion is removed. All user-verified 2026-10-17.
// v62: the BR panel (screenselect) is anchored to the true 16:9 right
// edge at DRAW time. Reverse-engineered: the panel sets its OWN
// type-0x100 screen-model transform each frame (m[12] = the OBST
// design x = the file-patch value, m[13]=0, m[14]=the OBST z
// -104..-154); the game CULLS the panel when m[12] crosses ~333.33
// (the 4:3 design box - that was the v60 "4:3 box crop"). Fix: the
// file patch stays at 331.67 (the cull never fires) and the matrix
// passed to the device gets m[12] += BR_PANEL_SHIFT (218.05, user-
// verified) on a COPY - the game's matrix memory is untouched. The
// FX stripe was already correct. Also: the BL panel "Max" capacity
// labels (owners 0x77FB2D/0x77FB87/0x77FBE1) get +10 physical px
// right (user-verified). v61 diagnostics (SetViewport/SetRect/
// DrawPrimitive traps) proved no D3D clip exists in-game - the
// "crop" was this game-side cull.
// v60: the rotating ship display (bottom-left, always in-game) is
// left-anchored via its scene container: the ship-panel scene's view x
// shifts (rotating models = the ships), the scene's STATIC models (the
// corner buttons) get the opposite compensation on a matrix copy, and
// the v30 BL ship-list name/icon shifts are removed (they follow the
// projection). TL alap + BL nagy buttons re-calibrated ~25px left.
// All fixes from v59 unchanged.
// v59: the AI-players-0-7 side project (AI_PLAYERS.md, milestone v3) is
// merged in as the [QoL] section (gated by hghook.ini [QoL]
// QoLFeaturesEnabled=1). All AI research diagnostics removed - only the
// user-verified behavior patches + minimal install log lines remain.
// Previous (v58): portrait message window right-anchored. v57: MP screen +
// option popups + EditField caret + map-label border guard.
// v41 stable (starmap labels un-confined + [MapLabels] OverlappingLabels)
// plus the popup/MP session (v43-v57, all user-verified):
// - MP setup screen: player-row frames + separators (owner 0x7ce33c..
//   0x7cef09), the 1px left border (caller 0x71A9E0..0x71AAA9 + the
//   MP fn in the chain, map fn excluded), empire/team icons + map-
//   settings icons (do_icon).
// - Option dropdown popups centered at the OBJECT level: the clamp
//   call site 0x525F18 in the renderer FUN_00525e9e is wrapped; the
//   object's x gets +g_pick_shift once per open, classified by
//   mouse - x > 100 (the popup opens at the authored combo position
//   while the click is at the centered position). All 3 elements
//   (background quad, corner frames, row texts) draw from the
//   object's +0x88/+0x8c so they move together - no per-element draw
//   rules. The popup rows (owner 0x52A188/0x52A1FD) never get the
//   general text shift; other widget texts (planet rows, MP value
//   texts) DO - the general +g_shift lands them on the shifted box.
//   Horizontal clamp removed per the user (implemented in the helper
//   with designW; b31a54 stays 790 - patching it past ~1000 breaks
//   an unknown runtime reader of the general text layout). The y
//   clamp replicates the game's own (b31a50).
// - EditField text-entry caret (chat box + player name field):
//   drawn as a "|" glyph via the non-queue text renderer
//   FUN_00437bb4 from FUN_005074c9 - shifted by owner + the single-
//   glyph batch signature.
// - MP 1px left border chain check: FUN_00711e94 also draws the map
//   name-plate borders (world space) - the rule requires the MP fn
//   (0x7CE33C..0x7CEF09) in the chain and excludes the map fn
//   (0x780FA1..0x787958).
// All diagnostics removed for the stable build.






#include <windows.h>
#include <stdio.h>
#include <string.h>
#include <math.h>

/* v62: BR panel draw-time compensation, design units (see my_SetTransform).
   Final user-verified value: 218.05 (the box-edge -> 16:9-edge move). */
#define BR_PANEL_SHIFT 218.05f

typedef void* (WINAPI *tDirect3DCreate8)(UINT);
typedef HRESULT (WINAPI *tCreateDevice)(void* d3d8, UINT adapter, int devType,
    HWND focus, DWORD behavior, void* params, void** outdev);
typedef HRESULT (WINAPI *tSetTransform)(void* dev, int type, const float* m);
typedef HRESULT (WINAPI *tGetViewport)(void* dev, DWORD* vp);
typedef HRESULT (WINAPI *tSetVSConst)(void* dev, DWORD startReg, const void* data, DWORD count);

static tDirect3DCreate8 g_real_D3DC8 = 0;
static tCreateDevice g_real_CreateDevice = 0;
static tSetTransform g_real_SetTransform = 0;
static tGetViewport g_real_GetViewport = 0;
static tSetVSConst g_real_SetVSConst = 0;
static CRITICAL_SECTION g_log_cs;
static int g_log_ok = 0;

/* ---- v60 ship-container fix: state ---- */
typedef struct { DWORD k1, k2, k3, k4, k5, k6; } diagkey_t;

static DWORD* g_pScreenW = 0;
static DWORD* g_pScreenH = 0;
static BYTE* g_exe = 0;
__attribute__((used)) static void* g_d3d8dev = 0;
__attribute__((used)) static DWORD g_last_scene = 0;
__attribute__((used)) static float g_last_svx = 0.0f, g_last_svy = 0.0f, g_last_svz = 0.0f;
__attribute__((used)) static DWORD g_ship_scene = 0;
__attribute__((used)) static DWORD g_staticcomp_logged = 0;
__attribute__((used)) static DWORD g_shipview_logged = 0;
__attribute__((used)) static diagkey_t g_shipkeys[64];
__attribute__((used)) static int g_shiprot[64];
__attribute__((used)) static int g_shipseen[64];
__attribute__((used)) static DWORD g_shiplastm[64];
__attribute__((used)) static int g_shipkeys_n = 0;
static float g_pick_shift;
static float g_shift;
static int msg_window_active(void);
static DWORD msg_strip_get(void);

#include "stubs.inc"

void* WINAPI my_Direct3DCreate8(UINT sdkver);
static HRESULT (WINAPI *g_real_GetAdapterModeCount)(void*, UINT) = 0;
static HRESULT (WINAPI *g_real_EnumAdapterModes)(void*, UINT, UINT, void*) = 0;
static HRESULT WINAPI my_GetAdapterModeCount(void* d3d8, UINT adapter);
static HRESULT WINAPI my_EnumAdapterModes(void* d3d8, UINT adapter, UINT mode, void* out);

static void log_line(const char* s) {
    if (!g_log_ok) return;
    EnterCriticalSection(&g_log_cs);
    FILE* f = fopen("C:\\Users\\Nova\\haegemonia-mod\\proj_log.txt", "a");
    if (f) { fputs(s, f); fclose(f); }
    LeaveCriticalSection(&g_log_cs);
}

static HRESULT WINAPI my_SetVSConst(void* dev, DWORD startReg, const void* data, DWORD count) {
    return g_real_SetVSConst(dev, startReg, data, count);
}

static HRESULT WINAPI my_SetTransform(void* dev, int type, const float* m) {
    HRESULT hr;
    float m2[16];
    int corrected = 0;
    /* v60: ship-container fix (the verified scheme):
       - the ship-panel SCENE's view shifts left (moves the rotating
         ships + TL display + everything in the scene),
       - STATIC models in the tight band (the v29-anchored corner
         buttons) get the OPPOSITE compensation on a matrix COPY,
       - rotating models (bucket change-count >= 3) are the ships:
         never compensated, and their scene = g_ship_scene.
       The game's matrix memory is never mutated (m2 copy only). */
    /* v62: BR panel screen-model compensation.
       The screenselect panel sets its OWN type-0x100 transform each
       frame: m[12] = the OBST design x (= the file-patch value, 331.67),
       m[13] = 0, m[14] = the OBST z (-104..-154). The game CULLS the
       panel when m[12] exceeds ~333.33 (the 4:3 design box) - the file
       patch alone can never reach the 16:9 edge (331.67 is already at
       the cull limit). Fix: keep the file value at 331.67 (the game's
       cull never fires) and pass a shifted COPY to the device (the
       game's matrix memory is untouched - same trick as the corner
       buttons). BR_PANEL_SHIFT = 64.6 design units = the remaining
       box-edge -> 16:9-edge distance (calibrated 2026-10-17). */
    if (type == 0x100 && g_shift > 1.0f && m && !IsBadReadPtr(m, 64) &&
        g_ship_scene && g_last_scene == g_ship_scene &&
        m[13] > -0.01f && m[13] < 0.01f &&
        m[14] > -250.0f && m[14] < -90.0f &&
        m[12] > -300.0f && m[12] < 400.0f) {
        memcpy(m2, m, sizeof(m2));
        m2[12] += BR_PANEL_SHIFT;
        m = m2;
    }
    else if (type == 0x100 && g_shift > 1.0f && m && !IsBadReadPtr(m, 64) &&
        m[12] > -1300.0f && m[12] < -100.0f &&
        m[13] > 1100.0f && m[13] < 1450.0f &&
        m[14] > -800.0f && m[14] < 800.0f &&
        m[0] < 3.0f && m[0] > -3.0f && m[5] < 3.0f && m[5] > -3.0f) {
        diagkey_t k;
        k.k1 = (DWORD)(m[12] / 32.0f);
        k.k2 = (DWORD)(m[13] / 32.0f);
        k.k3 = (DWORD)(m[14] / 32.0f);
        k.k4 = 0; k.k5 = 0; k.k6 = 0;
        int slot = -1, i;
        for (i = 0; i < g_shipkeys_n; i++) {
            if (g_shipkeys[i].k1 == k.k1 && g_shipkeys[i].k2 == k.k2 && g_shipkeys[i].k3 == k.k3) {
                slot = i;
                break;
            }
        }
        DWORD mbits = (*(DWORD*)&m[0]) ^ (*(DWORD*)&m[5]);
        if (slot < 0 && g_shipkeys_n < 64) {
            slot = g_shipkeys_n++;
            g_shipkeys[slot] = k;
            g_shiprot[slot] = 0;
            g_shipseen[slot] = 0;
            g_shiplastm[slot] = mbits;
        }
        if (slot >= 0) {
            if (g_shiplastm[slot] != mbits) {
                g_shiplastm[slot] = mbits;
                if (g_shiprot[slot] < 10) g_shiprot[slot]++;
            }
            if (g_shipseen[slot] < 200) g_shipseen[slot]++;
            if (g_shiprot[slot] >= 3) {
                /* rotating model = a ship: the scene rendering them = the
                   container. Sticky detection. */
                if (!g_ship_scene && g_last_scene) {
                    g_ship_scene = g_last_scene;
                    log_line("== ship-container scene detected ==\n");
                }
            }
            else if (g_shipseen[slot] >= 5) {
                /* static model (corner buttons): compensate the scene
                   view shift back - pass a shifted copy to the device */
                float Wf = (float)*(DWORD*)(g_exe + 0xc5127c - 0x400000);
                float units = g_shift * (m[13] + 400.0f) / (1.3589f * Wf * 0.5f);
                memcpy(m2, m, sizeof(m2));
                m2[12] += units;
                m = m2;
                if (!g_staticcomp_logged) {
                    g_staticcomp_logged = 1;
                    log_line("== ship scene static compensation active ==\n");
                }
            }
        }
    }
    if (type == 3 && m) {
        float ax = m[0] < 0 ? -m[0] : m[0];
        float ay = m[5] < 0 ? -m[5] : m[5];
        float mat_aspect = ay / (ax + 0.000001f);
        if (mat_aspect > 1.32f && mat_aspect < 1.35f && g_real_GetViewport && dev) {
            DWORD vp[4] = { 0, 0, 0, 0 };
            g_real_GetViewport(dev, vp);
            if (vp[2] > 0 && vp[3] > 0) {
                float va = (float)vp[2] / (float)vp[3];
                if (va > 1.34f) {
                    memcpy(m2, m, sizeof(m2));
                    m2[0] *= mat_aspect / va;
                    corrected = 1;
                }
            }
        }
    }
    hr = g_real_SetTransform(dev, type, corrected ? m2 : m);
    return hr;
}

static HRESULT WINAPI my_CreateDevice(void* d3d8, UINT adapter, int devType,
        HWND focus, DWORD behavior, void* params, void** outdev) {
    HRESULT hr = g_real_CreateDevice(d3d8, adapter, devType, focus, behavior, params, outdev);
    if (SUCCEEDED(hr) && outdev && *outdev) {
        g_d3d8dev = *outdev;
        void** vtbl = *(void***)*outdev;
        DWORD oldp;
        if (!g_real_SetTransform) {
            g_real_SetTransform = (tSetTransform)vtbl[37];
            g_real_GetViewport = (tGetViewport)vtbl[41];
            g_real_SetVSConst = (tSetVSConst)vtbl[79];
            VirtualProtect(&vtbl[37], 4, PAGE_READWRITE, &oldp);
            vtbl[37] = my_SetTransform;
            VirtualProtect(&vtbl[37], 4, oldp, &oldp);
            VirtualProtect(&vtbl[79], 4, PAGE_READWRITE, &oldp);
            vtbl[79] = my_SetVSConst;
            VirtualProtect(&vtbl[79], 4, oldp, &oldp);
            log_line("== device created, SetTransform + SetVSConst hooked ==\n");
        }
    }
    return hr;
}
void* WINAPI my_Direct3DCreate8(UINT sdkver) {
    void* d3d8 = g_real_D3DC8(sdkver);
    if (d3d8) {
        void** vtbl = *(void***)d3d8;
        DWORD oldp;
        if (!g_real_CreateDevice) {
            g_real_CreateDevice = (tCreateDevice)vtbl[15];
            VirtualProtect(&vtbl[15], 4, PAGE_READWRITE, &oldp);
            vtbl[15] = my_CreateDevice;
            VirtualProtect(&vtbl[15], 4, oldp, &oldp);
        }
        if (!g_real_GetAdapterModeCount) {
            g_real_GetAdapterModeCount = (HRESULT (WINAPI*)(void*, UINT))vtbl[6];
            g_real_EnumAdapterModes = (HRESULT (WINAPI*)(void*, UINT, UINT, void*))vtbl[7];
            VirtualProtect(&vtbl[6], 4, PAGE_READWRITE, &oldp);
            vtbl[6] = my_GetAdapterModeCount;
            VirtualProtect(&vtbl[6], 4, oldp, &oldp);
            VirtualProtect(&vtbl[7], 4, PAGE_READWRITE, &oldp);
            vtbl[7] = my_EnumAdapterModes;
            VirtualProtect(&vtbl[7], 4, oldp, &oldp);
            log_line("== IDirect3D8 mode enumeration hooked (3 widescreen modes) ==\n");
        }
    }
    return d3d8;
}

/* ---- ClipCursor: expand design-space clips to the full screen ---- */
static BOOL (WINAPI *g_real_ClipCursor)(const RECT*) = 0;

static BOOL WINAPI my_ClipCursor(const RECT* r) {
    if (r && g_real_ClipCursor) {
        RECT r2 = *r;
        int w = r2.right - r2.left;
        int h = r2.bottom - r2.top;
        if (w > 0 && h > 0 && g_pScreenW && g_pScreenH && *g_pScreenH > 0) {
            /* any clip that spans the full screen height is a design-space
               confine (e.g. the centered 4:3 box [240..1680]); force full width */
            if (h >= (int)*g_pScreenH - 4) {
                if (r2.left != 0) r2.left = 0;
                if (r2.right != (int)*g_pScreenW) r2.right = (int)*g_pScreenW;
            }
        }
        return g_real_ClipCursor(&r2);
    }
    return g_real_ClipCursor ? g_real_ClipCursor(r) : 0;
}

static void patch_clipcursor(void* exe) {
    HMODULE u32 = GetModuleHandleA("user32.dll");
    if (!u32) return;
    g_real_ClipCursor = (BOOL (WINAPI*)(const RECT*))GetProcAddress(u32, "ClipCursor");
    if (!g_real_ClipCursor) return;
    IMAGE_DOS_HEADER* dos = (IMAGE_DOS_HEADER*)exe;
    IMAGE_NT_HEADERS* nt = (IMAGE_NT_HEADERS*)((BYTE*)exe + dos->e_lfanew);
    DWORD impRVA = nt->OptionalHeader.DataDirectory[IMAGE_DIRECTORY_ENTRY_IMPORT].VirtualAddress;
    IMAGE_IMPORT_DESCRIPTOR* imp = (IMAGE_IMPORT_DESCRIPTOR*)((BYTE*)exe + impRVA);
    int patched = 0;
    for (; imp->Name; imp++) {
        const char* dll = (const char*)((BYTE*)exe + imp->Name);
        if (_stricmp(dll, "user32.dll") != 0) continue;
        IMAGE_THUNK_DATA* thunk = (IMAGE_THUNK_DATA*)((BYTE*)exe + imp->FirstThunk);
        for (; thunk->u1.Function; thunk++) {
            if ((DWORD)thunk->u1.Function == (DWORD)g_real_ClipCursor) {
                DWORD oldp;
                VirtualProtect(thunk, 4, PAGE_READWRITE, &oldp);
                thunk->u1.Function = (ULONG_PTR)&my_ClipCursor;
                VirtualProtect(thunk, 4, oldp, &oldp);
                patched = 1;
            }
        }
    }
    log_line(patched ? "== ClipCursor IAT patched ==\n" : "== ClipCursor not found in IAT ==\n");
}

/* ---- SetCursorPos: clamp to the screen ---- */
static BOOL (WINAPI *g_real_SetCursorPos)(int, int) = 0;

static BOOL WINAPI my_SetCursorPos(int x, int y) {
    if (g_pScreenW && g_pScreenH && *g_pScreenW > 0 && *g_pScreenH > 0) {
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > (int)*g_pScreenW - 1) x = (int)*g_pScreenW - 1;
        if (y > (int)*g_pScreenH - 1) y = (int)*g_pScreenH - 1;
    }
    return g_real_SetCursorPos ? g_real_SetCursorPos(x, y) : 0;
}

static void patch_setcursorpos(void* exe) {
    HMODULE u32 = GetModuleHandleA("user32.dll");
    if (!u32) return;
    g_real_SetCursorPos = (BOOL (WINAPI*)(int, int))GetProcAddress(u32, "SetCursorPos");
    if (!g_real_SetCursorPos) return;
    IMAGE_DOS_HEADER* dos = (IMAGE_DOS_HEADER*)exe;
    IMAGE_NT_HEADERS* nt = (IMAGE_NT_HEADERS*)((BYTE*)exe + dos->e_lfanew);
    DWORD impRVA = nt->OptionalHeader.DataDirectory[IMAGE_DIRECTORY_ENTRY_IMPORT].VirtualAddress;
    IMAGE_IMPORT_DESCRIPTOR* imp = (IMAGE_IMPORT_DESCRIPTOR*)((BYTE*)exe + impRVA);
    int patched = 0;
    for (; imp->Name; imp++) {
        const char* dll = (const char*)((BYTE*)exe + imp->Name);
        if (_stricmp(dll, "user32.dll") != 0) continue;
        IMAGE_THUNK_DATA* thunk = (IMAGE_THUNK_DATA*)((BYTE*)exe + imp->FirstThunk);
        for (; thunk->u1.Function; thunk++) {
            if ((DWORD)thunk->u1.Function == (DWORD)g_real_SetCursorPos) {
                DWORD oldp;
                VirtualProtect(thunk, 4, PAGE_READWRITE, &oldp);
                thunk->u1.Function = (ULONG_PTR)&my_SetCursorPos;
                VirtualProtect(thunk, 4, oldp, &oldp);
                patched = 1;
            }
        }
    }
    log_line(patched ? "== SetCursorPos IAT patched ==\n" : "== SetCursorPos not found in IAT ==\n");
}

/* ---- v27/v29: corner-panel corner-anchor via file-read interception ----
   Intercept the BMOD bytes as the game reads them:
   BR (screenselect.BMOD): patch the 186.33 floats (4A 55 3A 43) to
   259.0 (00 80 81 43) = +72.67 TRPS units (calibrated 2026-10-12).
   BL (starmap_alap*.BMOD): patch the authored OBST pos-x floats -
   alap/select variants: 7.274771 (EC CA E8 40),
   nagy variants: 0.002476811 (00 52 22 3B) -
   subtracting bl_delta = (designW-800)/4 (=66.67 at 16:9), which
   removes the (designW-800)/2 centering shift (1 unit = 2 design px,
   same factor as the BR calibration). Resolution-independent.
   Pure data interception - no file edits, no game-code hooks. */
static HANDLE (WINAPI *g_real_CreateFileA)(LPCSTR, DWORD, DWORD, LPSECURITY_ATTRIBUTES, DWORD, DWORD, HANDLE) = 0;
static BOOL (WINAPI *g_real_ReadFile)(HANDLE, LPVOID, DWORD, LPDWORD, LPOVERLAPPED) = 0;
__attribute__((used)) static HANDLE g_brpanel_handle = 0;
__attribute__((used)) static HANDLE g_backpanel_handle = 0;
__attribute__((used)) static HANDLE g_blpanel_handles[8];
__attribute__((used)) static int g_blpanel_n = 0;
__attribute__((used)) static int g_backfile_logged = 0;

static int is_screenselect_path(LPCSTR p) {
    if (!p) return 0;
    const char* s = p;
    while (*s) s++;
    while (s > p && s[-1] != '\\' && s[-1] != '/') s--;
    return (_stricmp(s, "screenselect.BMOD") == 0);
}

/* v66: back.bmod = the BR panel's back arrow mesh (same authored OBST
   x float 186.3332 as the panel). Match the screenselect folder. */
static int is_screenselect_back_path(LPCSTR p) {
    if (!p) return 0;
    const char* s = p;
    while (*s) s++;
    if (s - p < 9) return 0;
    if (_strnicmp(s - 9, "back.bmod", 9) != 0) return 0;
    const char* q = p;
    while (*q) {
        if (_strnicmp(q, "screenselect", 12) == 0) return 1;
        q++;
    }
    return 0;
}

static int is_starmap_alap_path(LPCSTR p) {
    if (!p) return 0;
    const char* s = p;
    while (*s) s++;
    while (s > p && s[-1] != '\\' && s[-1] != '/') s--;
    return (_strnicmp(s, "starmap_alap", 12) == 0);
}

static int bl_handle(HANDLE h) {
    int i;
    for (i = 0; i < g_blpanel_n; i++)
        if (g_blpanel_handles[i] == h) return 1;
    return 0;
}

static float bl_delta(void) {
    /* v29 calibration @1080p: the NAGY meshes (BL ship buttons, pos 0.0025)
       scale at ~2.91 px/u. Removing the full (designW-800)/2 = 133.33
       design container shift = 240px -> 82.5u (user-verified: left edge
       7px).
       v60: +8.55 (82.5 -> 91.05) = ~25px more left, matching the TL
       alap adjustment (user-calibrated). */
    return 91.05f;
}

static float bl_delta_alap(void) {
    /* v29 calibration @1080p (ALAP mesh = TL base button, pos x 7.274771).
       Fit (no pos.y/z patches): x = 235 - 2.96*D  (66.1->40, 77.2->5,
       79.6->0). D=76.7 -> 6px, target 8 -> 76.0.
       NOTE: pos.y leaks into screen x via depth/perspective (~-3.1 px/u) -
       do not patch pos.y.
       v60: +8.44 (76.34 -> 84.78) = ~25px more left (user-calibrated:
       40px was 15px too far). */
    return 84.78f;
}

static float bl_z_delta(void) {
    /* v29: vertical driver for the TL panel = pos z (position is applied
       rotated: x->x, y->depth, z->vertical). y = 1.56 + 3.125*z (2.7->10,
       13.9->45). Target 8px -> z = 2.06. */
    return 2.06f;
}

static HANDLE WINAPI my_CreateFileA(LPCSTR name, DWORD acc, DWORD share,
        LPSECURITY_ATTRIBUTES sa, DWORD disp, DWORD flags, HANDLE tmpl) {
    HANDLE h = g_real_CreateFileA(name, acc, share, sa, disp, flags, tmpl);
    if (h != INVALID_HANDLE_VALUE && is_screenselect_path(name)) {
        g_brpanel_handle = h;
    }
    if (h != INVALID_HANDLE_VALUE && is_screenselect_back_path(name)) {
        g_backpanel_handle = h;
    }
    if (h != INVALID_HANDLE_VALUE && is_starmap_alap_path(name)) {
        if (g_blpanel_n < 8) g_blpanel_handles[g_blpanel_n++] = h;
    }
    return h;
}

static BOOL WINAPI my_ReadFile(HANDLE h, LPVOID buf, DWORD n, LPDWORD out, LPOVERLAPPED ov) {
    BOOL r = g_real_ReadFile(h, buf, n, out, ov);
    if (!r || !buf || !out || *out == 0) return r;
    DWORD got = *out;
    if (got > 0x40000) got = 0x40000;
    if (h == g_brpanel_handle) {
        BYTE* b = (BYTE*)buf;
        for (DWORD i = 0; i + 4 <= got; i++) {
            if (b[i] == 0x4A && b[i + 1] == 0x55 && b[i + 2] == 0x3A && b[i + 3] == 0x43) {
                /* v27: 186.33 -> 259.0 = the 16:9 right-corner anchor
                   (3.6066 px/TRPS). v60: ship-scene view-shift
                   compensation: 259.0 -> 331.67.
                   v62: 331.67 = the cull limit (the game hides the
                   panel when its OBST x crosses ~333.33 = the 4:3
                   design box). The 16:9-edge move happens at DRAW
                   time via the type-0x100 compensation in
                   my_SetTransform (BR_PANEL_SHIFT) - the file value
                   must stay at 331.67 so the cull never fires. */
                float nv = 331.67f;
                memcpy(&b[i], &nv, 4);
            }
        }
    }
    else if (h == g_backpanel_handle) {
        /* v66: the BR panel's back arrow is its OWN mesh
           (gfx/interface/screenselect/back.bmod) with the panel's
           ORIGINAL authored OBST x float (186.3332). The panel's file
           patch (186.33 -> 331.67, see above) never touched back.bmod,
           so the arrow rendered ~240px left of the panel on the
           planet/empire/research screens (user-reported). The same
           +145.34 got the arrow culled by the game (invisible), and a
           device-copy matrix shift did NOT move its draw at all (the
           screen scene positions its models from the loaded OBST), so
           the fix is an in-buffer OBST patch with the user-calibrated
           value +72.0 authored units (2026-10-19). */
        BYTE* b = (BYTE*)buf;
        for (DWORD i = 0; i + 4 <= got; i++) {
            if (b[i] == 0x4A && b[i + 1] == 0x55 && b[i + 2] == 0x3A && b[i + 3] == 0x43) {
                float nv = 186.3332f + 72.0f;
                memcpy(&b[i], &nv, 4);
                if (!g_backfile_logged) {
                    g_backfile_logged = 1;
                    log_line("== back.bmod OBST patched (+72.0) ==\n");
                }
            }
        }
    }
    else if (bl_handle(h)) {
        BYTE* b = (BYTE*)buf;
        int hasA = 0;
        for (DWORD i = 0; i + 4 <= got && !hasA; i++) {
            if (b[i] == 0xEC && b[i + 1] == 0xCA && b[i + 2] == 0xE8 && b[i + 3] == 0x40) hasA = 1;
        }
        for (DWORD i = 0; i + 4 <= got; i++) {
            int isA = (b[i] == 0xEC && b[i + 1] == 0xCA && b[i + 2] == 0xE8 && b[i + 3] == 0x40);
            int isB = (b[i] == 0x00 && b[i + 1] == 0x52 && b[i + 2] == 0x22 && b[i + 3] == 0x3B);
            int isZ = (hasA && b[i] == 0x10 && b[i + 1] == 0x36 && b[i + 2] == 0xEE && b[i + 3] == 0xBC);
            if (isA || isB || isZ) {
                float oldv;
                float nv;
                memcpy(&oldv, &b[i], 4);
                if (isA) nv = oldv - bl_delta_alap();
                else if (isB) nv = oldv - bl_delta();
                else nv = oldv - bl_z_delta();
                memcpy(&b[i], &nv, 4);
            }
        }
    }
    return r;
}

static void patch_fileio(void* exe) {
    static int tried = 0;
    if (tried) return;
    tried = 1;
    HMODULE k32 = GetModuleHandleA("kernel32.dll");
    if (!k32) return;
    g_real_CreateFileA = (HANDLE (WINAPI*)(LPCSTR, DWORD, DWORD, LPSECURITY_ATTRIBUTES, DWORD, DWORD, HANDLE))
        GetProcAddress(k32, "CreateFileA");
    g_real_ReadFile = (BOOL (WINAPI*)(HANDLE, LPVOID, DWORD, LPDWORD, LPOVERLAPPED))
        GetProcAddress(k32, "ReadFile");
    if (!g_real_CreateFileA || !g_real_ReadFile) {
        log_line("== fileio: kernel32 hooks unavailable ==\n");
        return;
    }
    IMAGE_DOS_HEADER* dos = (IMAGE_DOS_HEADER*)exe;
    IMAGE_NT_HEADERS* nt = (IMAGE_NT_HEADERS*)((BYTE*)exe + dos->e_lfanew);
    DWORD impRVA = nt->OptionalHeader.DataDirectory[IMAGE_DIRECTORY_ENTRY_IMPORT].VirtualAddress;
    IMAGE_IMPORT_DESCRIPTOR* imp = (IMAGE_IMPORT_DESCRIPTOR*)((BYTE*)exe + impRVA);
    int p1 = 0, p2 = 0;
    for (; imp->Name; imp++) {
        const char* dll = (const char*)((BYTE*)exe + imp->Name);
        if (_stricmp(dll, "kernel32.dll") != 0) continue;
        IMAGE_THUNK_DATA* thunk = (IMAGE_THUNK_DATA*)((BYTE*)exe + imp->FirstThunk);
        for (; thunk->u1.Function; thunk++) {
            DWORD oldp;
            if ((DWORD)thunk->u1.Function == (DWORD)g_real_CreateFileA) {
                VirtualProtect(thunk, 4, PAGE_READWRITE, &oldp);
                thunk->u1.Function = (ULONG_PTR)&my_CreateFileA;
                VirtualProtect(thunk, 4, oldp, &oldp);
                p1 = 1;
            }
            else if ((DWORD)thunk->u1.Function == (DWORD)g_real_ReadFile) {
                VirtualProtect(thunk, 4, PAGE_READWRITE, &oldp);
                thunk->u1.Function = (ULONG_PTR)&my_ReadFile;
                VirtualProtect(thunk, 4, oldp, &oldp);
                p2 = 1;
            }
        }
    }
    char buf[100];
    _snprintf(buf, sizeof(buf), "== fileio IAT patched: CreateFileA=%d ReadFile=%d ==\n", p1, p2);
    log_line(buf);
}

/* ---- widescreen resolution list (v24.57/24.60) ----
   The graphics options screen enumerates IDirect3D8 (the game imports
   only Direct3DCreate8) via GetAdapterModeCount / EnumAdapterModes
   (vtable slots 6/7). We replace them with exactly the 3 widescreen
   modes. The game's list builder FUN_00447ad3 also hardcodes a mode
   cap (800..1600 x 600..1200, formats 22/31, aspect >= 4:3) - the
   caps must be raised (v24.60) or the modes get filtered out and the
   game crashes on the empty list. The startup validation below keeps
   the registry value inside the list (no 800x600 reset). ---- */

/* raise FUN_00447ad3's hardcoded mode caps: width <= 1600 (0x640) ->
   3840, height <= 1200 (0x4B0) -> 2160 */
static void patch_mode_caps(void* exe) {
    BYTE* p1 = (BYTE*)exe + (0x447B66 - 0x400000);
    BYTE* p2 = (BYTE*)exe + (0x447B73 - 0x400000);
    DWORD oldp;
    if (*(DWORD*)p1 == 0x640) {
        VirtualProtect(p1, 4, PAGE_EXECUTE_READWRITE, &oldp);
        *(DWORD*)p1 = 0xF00;
        VirtualProtect(p1, 4, oldp, &oldp);
        log_line("== mode cap width 1600 -> 3840 patched ==\n");
    }
    if (*(DWORD*)p2 == 0x4B0) {
        VirtualProtect(p2, 4, PAGE_EXECUTE_READWRITE, &oldp);
        *(DWORD*)p2 = 0x870;
        VirtualProtect(p2, 4, oldp, &oldp);
        log_line("== mode cap height 1200 -> 2160 patched ==\n");
    }
    /* the 4:3-only aspect filter: "test $0x41,%ah; jp reject" at
       0x447BCF rejects every ratio strictly above 4:3 -> NOP it so
       widescreen modes are accepted (the <4:3 reject stays). */
    BYTE* p3 = (BYTE*)exe + (0x447BCF - 0x400000);
    if (p3[0] == 0xF6 && p3[1] == 0xC4 && p3[2] == 0x41 &&
        p3[3] == 0x0F && p3[4] == 0x8A) {
        VirtualProtect(p3, 9, PAGE_EXECUTE_READWRITE, &oldp);
        for (int i = 0; i < 9; i++) p3[i] = 0x90;
        VirtualProtect(p3, 9, oldp, &oldp);
        log_line("== mode aspect filter (4:3-only) disabled ==\n");
    }
}

/* startup: if the registry resolution is not one of the 3 modes,
   rewrite it to 1920x1080 (keeps the game from resetting to 800x600) */
typedef struct { DWORD w, h, refresh, format; } WsMode;
static const WsMode g_ws_modes[3] = {
    { 1920, 1080, 60, 22 },   /* D3DFMT_X8R8G8B8 */
    { 2560, 1440, 60, 22 },
    { 3840, 2160, 60, 22 }
};
#define WS_MODES 3

static HRESULT WINAPI my_GetAdapterModeCount(void* d3d8, UINT adapter) {
    return WS_MODES;
}

static HRESULT WINAPI my_EnumAdapterModes(void* d3d8, UINT adapter, UINT mode, void* out) {
    if (mode >= WS_MODES) return 0x8876086C;   /* D3DERR_INVALIDCALL */
    DWORD* m = (DWORD*)out;
    m[0] = g_ws_modes[mode].w;
    m[1] = g_ws_modes[mode].h;
    m[2] = g_ws_modes[mode].refresh;
    m[3] = g_ws_modes[mode].format;
    return 0;
}

/* startup: if the registry resolution is not one of the 3 modes,
   rewrite it to 1920x1080 (keeps the game from resetting to 800x600) */
static void validate_reg_resolution(void) {
    HKEY hk = 0;
    char buf[64];
    DWORD cb, type;
    long r = RegOpenKeyExA(HKEY_LOCAL_MACHINE,
        "Software\\Digital Reality\\Haegemonia", 0, KEY_READ | KEY_WRITE, &hk);
    if (r != 0) return;
    int w = 0, h = 0;
    cb = sizeof(buf);
    if (RegQueryValueExA(hk, "ResolutionX", 0, &type, (LPBYTE)buf, &cb) == 0 && type == 1)
        w = atoi(buf);
    cb = sizeof(buf);
    if (RegQueryValueExA(hk, "ResolutionY", 0, &type, (LPBYTE)buf, &cb) == 0 && type == 1)
        h = atoi(buf);
    int ok = 0;
    for (int i = 0; i < WS_MODES; i++) {
        if ((DWORD)w == g_ws_modes[i].w && (DWORD)h == g_ws_modes[i].h) { ok = 1; break; }
    }
    if (!ok) {
        RegSetValueExA(hk, "ResolutionX", 0, REG_SZ, (const BYTE*)"1920", 5);
        RegSetValueExA(hk, "ResolutionY", 0, REG_SZ, (const BYTE*)"1080", 5);
        {
            char l[160];
            _snprintf(l, sizeof(l),
                "== resolution validation: %dx%d not in list -> set 1920x1080 ==\n", w, h);
            log_line(l);
        }
    }
    else {
        char l[160];
        _snprintf(l, sizeof(l),
            "== resolution validation: %dx%d ok ==\n", w, h);
        log_line(l);
    }
    RegCloseKey(hk);
}

/* ---- hghook.ini config (next to Game.exe, re-read at each start) ---- */
static char g_cfg_path[MAX_PATH];
static float g_cfg_line_factor = 0.85f;
static float g_cfg_letter_spacing = 1.0f;
static float g_cfg_word_spacing = 8.0f;
static float g_cfg_shift_factor = 1.004f;
static float g_cfg_shift_slack = 6.0f;
static float g_cfg_row_factor = 0.66f;
static float g_cfg_row4k = 1.8f;
static float g_cfg_cursor_scale = 0.5f;
static float g_cfg_icon_scale = 0.66f;
static int g_cfg_overlap_labels = 0;   /* 1 = allow starmap labels to overlap */
static int g_qol_enabled = 0;          /* [QoL] QoLFeaturesEnabled - AI players 0-7 extension */
static int g_cfg_suffix_enabled = 1;   /* [Suffix] SuffixEnabled - version-text suffix */
static int g_rotation_enabled = 1;     /* [RotationSmooth] RotationSmoothEnabled */
__attribute__((used)) static float g_rotation_boost = 4.0f;  /* [RotationSmooth] RotationBoost */
__attribute__((used)) static float g_rotation_sens = 1.0f;   /* [RotationSmooth] RotationSens */
static char g_cfg_suffix_text[64];
static char g_cfg_suffix_yellow_text[32];
static DWORD g_cfg_suffix_yellow = 0xFFFFFFE0;

static float cfg_float(const char* sec, const char* key, float def) {
    char buf[64];
    GetPrivateProfileStringA(sec, key, "", buf, sizeof(buf), g_cfg_path);
    if (!buf[0]) return def;
    return (float)atof(buf);
}

static void load_config(void) {
    char exep[MAX_PATH];
    GetModuleFileNameA(NULL, exep, sizeof(exep));
    {
        char *p = strrchr(exep, '\\');
        if (p) *p = 0;
        _snprintf(g_cfg_path, sizeof(g_cfg_path), "%s\\hghook.ini", exep);
    }
    if (GetFileAttributesA(g_cfg_path) == INVALID_FILE_ATTRIBUTES) {
        FILE* f = fopen(g_cfg_path, "w");
        if (f) {
            fprintf(f,
                "; hghook.ini - Haegemonia widescreen mod tweaks\n"
                "; Next to Game.exe. Re-read at every game start.\n"
                "; Delete a line (or the file) to use the built-in default.\n"
                "\n"
                "[Text]\n"
                "; line height factor (0.85 = original ink vs v-rect padding)\n"
                "LineFactor=0.85\n"
                "; extra advance per glyph, design px (b2e418)\n"
                "LetterSpacing=1.0\n"
                "; advance of the space character, design px (b30f40)\n"
                "WordSpacing=8.0\n"
                "\n"
                "[UI]\n"
                "; centering threshold = 800*scale*ShiftFactor + ShiftSlack\n"
                "ShiftFactor=1.004\n"
                "ShiftSlack=6\n"
                "; list row pitch = RowFactor*cellH at 1080p, *Row4K/scale above\n"
                "RowFactor=0.66\n"
                "Row4K=1.8\n"
                "\n"
                "[Cursor]\n"
                "CursorScale=0.5\n"
                "\n"
                "[Icons]\n"
                "; map icons + slider scale about their center\n"
                "IconScale=0.66\n"
                "\n"
                "[MapLabels]\n"
                "; 0 = starmap labels avoid each other (default)\n"
                "; 1 = allow starmap labels to overlap (they still stay on screen)\n"
                "OverlappingLabels=0\n"
                "\n"
                "[QoL]\n"
                "; 1 = allow up to 7 AI players on the skirmish/setup screen\n"
                "; (pairs with the modded Inf\\Prism\\Start.inf). 0 = vanilla AI\n"
                "; behavior - pair with the ORIGINAL Start.inf for a fully\n"
                "; vanilla setup.\n"
                "QoLFeaturesEnabled=1\n"
                "\n"
                "[Suffix]\n"
                "; the extra text drawn next to \"Version 1.09\" on the main\n"
                "; menu (max 31 chars). 0 = disabled.\n"
                "SuffixEnabled=1\n"
                "SuffixText=widescreen-fix v1.0 by .Nx\n"
                "; the part of SuffixText painted in the highlight color\n"
                "SuffixYellowText=v1.0\n"
                "; highlight color as ARGB hex (FFFFFFE0 = light yellow)\n"
                "SuffixYellowColor=FFFFFFE0\n"
                "\n"
                "[RotationSmooth]\n"
                "; 1 = crisp mouse-driven 3D camera rotation (the vanilla\n"
                "; animator eases the angles = floaty/gliding feel)\n"
                "RotationSmoothEnabled=1\n"
                "; animator approach speed multiplier. 1.0 = vanilla feel,\n"
                "; higher = crisp. Try 3..8 if still too smooth.\n"
                "RotationBoost=4.0\n"
                "; mouse sensitivity for the rotation. 1.0 = vanilla,\n"
                "; lower = more mouse movement per degree (e.g. 0.4).\n"
                "RotationSens=1.0\n");
            fclose(f);
        }
    }
    g_cfg_line_factor = cfg_float("Text", "LineFactor", 0.85f);
    g_cfg_letter_spacing = cfg_float("Text", "LetterSpacing", 1.0f);
    g_cfg_word_spacing = cfg_float("Text", "WordSpacing", 8.0f);
    g_cfg_shift_factor = cfg_float("UI", "ShiftFactor", 1.004f);
    g_cfg_shift_slack = cfg_float("UI", "ShiftSlack", 6.0f);
    g_cfg_row_factor = cfg_float("UI", "RowFactor", 0.66f);
    g_cfg_row4k = cfg_float("UI", "Row4K", 1.8f);
    g_cfg_cursor_scale = cfg_float("Cursor", "CursorScale", 0.5f);
    g_cfg_icon_scale = cfg_float("Icons", "IconScale", 0.66f);
    g_cfg_overlap_labels = (int)GetPrivateProfileIntA("MapLabels", "OverlappingLabels", 0, g_cfg_path);
    g_qol_enabled = (int)GetPrivateProfileIntA("QoL", "QoLFeaturesEnabled", 1, g_cfg_path);
    g_cfg_suffix_enabled = (int)GetPrivateProfileIntA("Suffix", "SuffixEnabled", 1, g_cfg_path);
    GetPrivateProfileStringA("Suffix", "SuffixText", "widescreen-fix v1.0 by .Nx",
                             g_cfg_suffix_text, sizeof(g_cfg_suffix_text), g_cfg_path);
    GetPrivateProfileStringA("Suffix", "SuffixYellowText", "v1.0",
                             g_cfg_suffix_yellow_text, sizeof(g_cfg_suffix_yellow_text), g_cfg_path);
    {
        char cb[32];
        GetPrivateProfileStringA("Suffix", "SuffixYellowColor", "FFFFFFE0", cb, sizeof(cb), g_cfg_path);
        g_cfg_suffix_yellow = (DWORD)strtoul(cb, NULL, 16);
        if (!g_cfg_suffix_yellow) g_cfg_suffix_yellow = 0xFFFFFFE0;
    }
    g_rotation_enabled = (int)GetPrivateProfileIntA("RotationSmooth", "RotationSmoothEnabled", 1, g_cfg_path);
    g_rotation_boost = cfg_float("RotationSmooth", "RotationBoost", 4.0f);
    if (g_rotation_boost < 0.1f) g_rotation_boost = 0.1f;
    if (g_rotation_boost > 50.0f) g_rotation_boost = 50.0f;
    g_rotation_sens = cfg_float("RotationSmooth", "RotationSens", 1.0f);
    if (g_rotation_sens < 0.05f) g_rotation_sens = 0.05f;
    if (g_rotation_sens > 4.0f) g_rotation_sens = 4.0f;
    {
        char buf[256];
        _snprintf(buf, sizeof(buf),
            "== CONFIG %s: Line=%.2f Letter=%.2f Word=%.2f Shift=%.3f+%.0f Row=%.2f/%.2f Cursor=%.2f Icon=%.2f Overlap=%d QoL=%d Suffix=%d Rotation=%d/x%.1f/s%.2f ==\n",
            g_cfg_path, g_cfg_line_factor, g_cfg_letter_spacing, g_cfg_word_spacing,
            g_cfg_shift_factor, g_cfg_shift_slack, g_cfg_row_factor, g_cfg_row4k,
            g_cfg_cursor_scale, g_cfg_icon_scale, g_cfg_overlap_labels, g_qol_enabled,
            g_cfg_suffix_enabled, g_rotation_enabled, g_rotation_boost, g_rotation_sens);
        log_line(buf);
    }
}

/* ---- global design-width override (v1 core fix) ---- */
static void patch_rdata_float(void* exe, DWORD rva, float val, const char* name) {    float* p = (float*)((BYTE*)exe + rva);
    if (*p == val) return;
    char buf[128];
    _snprintf(buf, sizeof(buf), "== %s override: %.4f -> %.4f ==\n", name, *p, val);
    DWORD oldp;
    VirtualProtect(p, 4, PAGE_READWRITE, &oldp);
    *p = val;
    VirtualProtect(p, 4, oldp, &oldp);
    log_line(buf);
}

static float g_shift = 0.0f;
static float g_thresh = 1e9f;
__attribute__((used)) static float g_pick_shift = 0.0f;   /* design units: (W/(2*scale))-400 = 133.33 @1080p */
__attribute__((used)) static float g_world_scale = 1.0f; /* starmap world rows: 800/designW = 0.75 @16:9 */
__attribute__((used)) static int g_slide_shift_int = 0;   /* = (int)g_pick_shift for the slider click zone */
__attribute__((used)) static float g_tech_mouse_shift = 0.0f; /* tech-tree hit-test mouse shift */
static float g_rowh_factor;   /* defined at the rowh section; updated per scale */
#ifndef ROW_FACTOR
#define ROW_FACTOR 0.66f
#endif

static void design_width_override(void* exe) {
    float* pDesW = (float*)((BYTE*)exe + 0xc0a790 - 0x400000);
    DWORD* pW = (DWORD*)((BYTE*)exe + 0xc5127c - 0x400000);
    DWORD* pH = (DWORD*)((BYTE*)exe + 0xc51280 - 0x400000);
    patch_rdata_float(exe, 0xb2e418 - 0x400000, g_cfg_letter_spacing, "letterSpacing");
    patch_rdata_float(exe, 0xb30f40 - 0x400000, g_cfg_word_spacing, "wordSpacing");
    if (*pW <= 0 || *pH <= 0) return;
    /* v49: b31a54 (790) is LEFT AT ITS ORIGINAL VALUE. Patching it past
       ~1000 (v43/v45/v46 tried 1056/1066) broke the general text layout
       - some runtime code reads it that Ghidra's static xrefs don't
       show. The popup horizontal clamp is now implemented in the
       popup_clamp_shift helper with designW instead (popup object
       centering stays perfect, see below). */
    float newv = 800.0f;
    if ((double)*pW / (double)*pH > 1.34) {
        newv = 600.0f * (float)*pW / (float)*pH;
    }
    float oldv = *pDesW;
    if (oldv != newv) {
        DWORD oldp;
        VirtualProtect(pDesW, 4, PAGE_READWRITE, &oldp);
        *pDesW = newv;
        VirtualProtect(pDesW, 4, oldp, &oldp);
        char buf[128];
        _snprintf(buf, sizeof(buf),
            "== designW override: %.4f -> %.4f (W=%d H=%d) ==\n", oldv, newv, *pW, *pH);
        log_line(buf);
        patch_rdata_float(exe, 0xb316d4 - 0x400000, newv, "layoutW");
        patch_rdata_float(exe, 0xb3a848 - 0x400000, newv * 0.5f, "layoutCenter");
    }
    {
        float scale = (float)*pH / 600.0f;
        if ((double)*pW / (double)*pH > 1.34) {
            g_shift = ((float)*pW - 800.0f * scale) * 0.5f;
            /* proportional slack: the game's authored 800-design layouts
               overshoot by up to ~3 design units; a fixed +5px margin was
               enough at 1080p but at 4K (scale 3.6) the same overshoot
               exceeds it -> some elements stopped shifting ("reverted to
               left-aligned"). */
            g_thresh = 800.0f * scale * g_cfg_shift_factor + g_cfg_shift_slack;
            g_pick_shift = (float)*pW / (2.0f * scale) - 400.0f;
            g_slide_shift_int = (int)(g_pick_shift + 0.5f);
            g_tech_mouse_shift = g_pick_shift;
            g_world_scale = 800.0f / newv;
            /* row pitch: keep the design result constant (= the 1080p
               calibration ROW_FACTOR*cellH*1.8) */
            g_rowh_factor = g_cfg_row_factor * g_cfg_row4k / scale;
        }
        else {
            g_shift = 0.0f;
            g_thresh = 1e9f;
            g_pick_shift = 0.0f;
            g_slide_shift_int = 0;
            g_tech_mouse_shift = 0.0f;
            g_world_scale = 1.0f;
            g_rowh_factor = g_cfg_row_factor;
        }
        char buf[128];
        _snprintf(buf, sizeof(buf), "== TEXT center: shift=%.2fpx thresh=%.2fpx ==\n",
                  g_shift, g_thresh);
        log_line(buf);
    }
}

/* ================= draw tracers + text centering (queue path only) ================= */
DWORD g_471f6_resume = 0;
DWORD g_4728e_resume = 0;
DWORD g_saved_ebp = 0;
__attribute__((used)) DWORD g_saved_ebp2 = 0;

/* icon size scales: cursor stays origin-anchored (tip at the mouse) at
   0.5; map icons + slider scale about their CENTER at 0.66 so they stay
   centered in their bounds. */
#define CURSOR_SCALE 0.5f
#define ICON_SCALE_CENTER 0.66f
/* line spacing factor: the original .chr v-rects carry ~15% vertical
   padding (ink 24-25px in a 28px v-span at fontB) -> 0.85 converts the
   v-span metric to the ink height the containers were sized around.
   v24.81 value; do not remove (it broke map-label/planet/production
   container heights when set to 1.0). */
#define LINE_FACTOR 0.85f
/* list row height factor: FUN_004f21c2 returns [font+0x30] = cellH*scaleY
   (a PIXEL value the game uses as design units). Scaling the return by a
   constant broke at 4K (the value doubles with the resolution). The
   factor now cancels the scale: ROW_FACTOR*1.8/scaleY -> the design row
   pitch stays ROW_FACTOR*cellH*1.8 (= the 1080p calibration) at every
   resolution. */
#ifndef ROW_FACTOR
#define ROW_FACTOR 0.66f
#endif

/* owner = the frame ABOVE the text system (queue/layout/funnel).
   Map labels funnel via FUN_006941a7 (site ~0x69463e). */
static DWORD walk_owner_ret(void) {
    DWORD f = g_saved_ebp;
    int i;
    for (i = 0; i < 12; i++) {
        if (f < 0x10000 || f > 0x7FFE0000) break;
        if (IsBadReadPtr((void*)f, 8)) break;
        DWORD ret = *(DWORD*)(f + 4);
        DWORD next = *(DWORD*)f;
        if (ret < 0x400000 || ret > 0x400000 + 0xA00000) break;
        if ((ret >= 0x438670 && ret <= 0x4398D0) ||   /* queue */
            (ret >= 0x44EC00 && ret <= 0x44F800) ||   /* glyph/icon builders */
            (ret >= 0x4EF000 && ret <= 0x4F3000)) {   /* text layout + funnel */
            if (next <= f) break;
            f = next;
            continue;
        }
        return ret;
    }
    return 0;
}

/* the full caller chain above the text system (owner1..4) - used to
   tell the planet screen apart from the game view for the planet-bar
   IM shift (the world cloud sprite shares the draw signature). */
static void walk_owner_chain(DWORD* out, int maxn) {
    DWORD f = g_saved_ebp;
    int i, n = 0;
    for (i = 0; i < 16; i++) {
        if (f < 0x10000 || f > 0x7FFE0000) break;
        if (IsBadReadPtr((void*)f, 8)) break;
        DWORD ret = *(DWORD*)(f + 4);
        DWORD next = *(DWORD*)f;
        if (ret < 0x400000 || ret > 0x400000 + 0xA00000) break;
        if ((ret >= 0x438670 && ret <= 0x4398D0) ||
            (ret >= 0x44EC00 && ret <= 0x44F800) ||
            (ret >= 0x4EF000 && ret <= 0x4F3000)) {
            if (next <= f) break;
            f = next;
            continue;
        }
        if (n < maxn) out[n++] = ret;
        else return;
        if (next <= f) break;
        f = next;
    }
    while (n < maxn) out[n++] = 0;
}

/* ---- v46: popup OBJECT-level centering ----
   FUN_00525e9e (the popup renderer) calls FUN_00525d66 (the clamp,
   ECX = popup object) at 0x525F18 - the single choke point of the
   option-dropdown popup class. The popup is drawn as 3 elements
   (background quad, corner frames, row texts) that all read the
   object's x/y (+0x88/+0x8c) -> shifting the OBJECT's x ONCE at open
   makes all 3 elements move together - no per-element draw rules.
   Classifier: the popup opens at the click point's AUTHORED position
   (the combo widget's left edge in the 800-space) while the click
   (design mouse) is at the CENTERED position = authored + 133.33
   design -> mouse - x > 100 means "needs centering" (x += g_pick_shift
   -> the popup lands at the mouse). Popups opened AT the mouse
   (mouse - x ~ 0) or at game-centered positions stay.
   Re-open = the open fn resets +0x88 to the fresh authored x ->
   detected by the x change and re-classified.
   v46 lesson: the v45 ENTRY hook on the clamp crashed at the resume
   point (eip = the game's first instruction, valid frame - the
   runtime bytes there differ from the static image). The call site
   (0x525F18) is wrapped instead and the REAL clamp runs as a NORMAL
   call from the helper (identical to the game's own call). Horizontal
   bounds are removed per the user: b31a54 -> designW keeps the
   SHIFTED popup on screen; the y clamp stays. */
__attribute__((used)) static DWORD g_popcall_resume = 0;
static DWORD g_popobjs[64];
static float g_popx[64];
static int g_popobjs_n = 0;

__attribute__((used, noinline)) static void popup_clamp_shift(DWORD obj) {
    if (g_exe && obj >= 0x10000 && !IsBadReadPtr((void*)obj, 0xf0)) {
        float* px = (float*)(obj + 0x88);
        float x = *px;
        float mx = *(float*)(g_exe + 0xc68030 - 0x400000);
        int i;
        for (i = 0; i < g_popobjs_n; i++) {
            if (g_popobjs[i] == obj) {
                float lastx = g_popx[i];
                if (x - lastx > 1.0f || lastx - x > 1.0f) {
                    /* fresh open position (the open fn reset +0x88) */
                    if (g_pick_shift > 0.0f && mx - x > 100.0f) {
                        x += g_pick_shift;
                        *px = x;
                    }
                    g_popx[i] = x;
                }
                goto run_clamp;
            }
        }
        /* first time this popup object is seen */
        if (g_pick_shift > 0.0f && mx - x > 100.0f) {
            x += g_pick_shift;
            *px = x;
        }
        if (g_popobjs_n < 64) {
            g_popobjs[g_popobjs_n] = obj;
            g_popx[g_popobjs_n] = x;
            g_popobjs_n++;
        }
    }
run_clamp:
    /* v49: own clamp implementation. The game's real clamp
       (FUN_00525d66) reads b31a54, which must stay at 790 (an unknown
       runtime reader breaks the general text layout when it's patched
       past ~1000). Horizontal bounds are removed per the user: the
       x-clamp here only keeps the SHIFTED popup on screen (limit =
       designW). The vertical clamp replicates the game's own
       (b31a50 = 600-10). */
    if (g_exe && obj >= 0x10000 && !IsBadReadPtr((void*)obj, 0xf0)) {
        float* px = (float*)(obj + 0x88);
        float* py = (float*)(obj + 0x8c);
        float w = *(float*)(obj + 0x7c);
        float x = *px;
        float y = *py;
        float dw = *(float*)(g_exe + 0xc0a790 - 0x400000);
        if (dw > 0.0f && dw < x + w) {
            *px = x - ((x + w) - dw);
        }
        float limy = *(float*)(g_exe + 0xb31a50 - 0x400000);
        DWORD h = 0;
        {
            DWORD fn = 0x00525CD9;
            __asm__ volatile (
                "movl %[o], %%ecx\n"
                "movl %[f], %%eax\n"
                "call *%%eax\n"
                "movl %%eax, %[h]\n"
                : [h] "=m" (h)
                : [o] "r" (obj), [f] "r" (fn)
                : "eax", "ecx", "edx", "memory", "cc"
            );
        }
        float hf = (float)h;
        if (limy < y + hf) {
            *py = y - ((y + hf) - limy);
        }
    }
}

__attribute__((naked)) static void popup_clamp_call_stub(void) {
    __asm__ volatile (
        "pushal\n"
        "movl %ecx, %eax\n"
        "pushl %eax\n"
        "call _popup_clamp_shift\n"
        "addl $4, %esp\n"
        "popal\n"
        "jmp *_g_popcall_resume\n"
    );
}

static void patch_popup_clamp(void* exe) {
    BYTE* p = (BYTE*)exe + (0x525f18 - 0x400000);
    if (p[0] == 0xE9) return;
    if (p[0] != 0xE8 || p[1] != 0x49 || p[2] != 0xFE || p[3] != 0xFF || p[4] != 0xFF) {
        log_line("== popup clamp call bytes mismatch ==\n");
        return;
    }
    g_popcall_resume = 0x525f18 + 5;
    DWORD rel = (DWORD)((BYTE*)&popup_clamp_call_stub - (p + 5));
    DWORD oldp;
    VirtualProtect(p, 5, PAGE_EXECUTE_READWRITE, &oldp);
    p[0] = 0xE9;
    *(DWORD*)(p + 1) = rel;
    VirtualProtect(p, 5, oldp, &oldp);
    log_line("== popup clamp call wrapped (0x525F18) ==\n");
}

/* ---- version-text suffix ("Version 1.09" + our line) ----
   The version fn + text funnel are part of the game's protected code
   (decrypt/re-encrypt around each call - patching or calling it from
   outside crashes). The suffix is therefore drawn as RAW GLYPH QUADS
   through the game's own 4728e draw - the only game function involved
   is the one already running the version batch:
   - qlog_text captures the version batch by its authored position
     (design ~(646, 582)): right edge + top/bottom y (post-shift),
   - the batch's own 4728e args (saved by the tracer) are reused for
     our draw (same texture + state, only the verts pointer changes),
   - the font = found once by matching the 'V' glyph's UVs against the
     text object's font vector; glyph UVs come from its table (stride
     0x14 floats {X,u0,v0,u1,v1}); the px-per-uv factor, the record
     crop and the space width are measured from the version batch
     every frame,
   - the vertex layout = {x, y, z, rhw, DIFFUSE, u, v} with the strip
     order TL, TR, BR, BL; the diffuse is set per glyph ("v1.0" =
     light yellow, the rest white).
   Our nested batch is skipped by every 4728e shift rule via the
   g_suffix_draw flag. The version text itself is never touched. */
#define VER_SUFFIX_WHITE 0xFFFFFFFF
#define VER_SUFFIX_MAXCHARS 32

static volatile DWORD g_suffix_draw = 0;
static volatile float g_ver_maxx = 0.0f;
static volatile int g_ver_seen = 0;

static volatile DWORD g_4728e_arg0 = 0, g_4728e_arg1 = 0, g_4728e_arg2 = 0,
    g_4728e_arg3 = 0, g_4728e_arg4 = 0, g_4728e_arg5 = 0,
    g_4728e_arg6 = 0, g_4728e_arg7 = 0;

static float g_vt[7];          /* first vertex of the version batch (template) */
static DWORD g_sfont = 0;      /* matched font object */
static DWORD g_sfont_recs = 0; /* its glyph table */
static DWORD g_sfont_first = 0, g_sfont_count = 0;
static float g_sfactor = 1.0f; /* screen px per uv unit */
static float g_su_crop = 1.0f; /* drawn/record u-extent ratio */
static float g_sv_crop = 1.0f; /* drawn/record v-extent ratio */
static float g_space_w = 0.0f; /* the space glyph's drawn width */

__attribute__((used, noinline)) static void ver_suffix_glyphs(float* v, DWORD vcount,
                                                              int fl, float mny, float mxy) {
    if (!g_exe || vcount < 8 || fl < 7) return;
    if (!g_cfg_suffix_enabled) return;
    int k;
    for (k = 0; k < 7; k++) g_vt[k] = v[k];
    /* font lookup (once): match the 'V' glyph's u0,v0 against the font
       vector of the text object (c65d10 + 8..+0xc; POINTER entries to
       0x14-byte slots {image, wrapper}; font = [wrapper+4] - the same
       walk as font_metrics_scan) */
    if (!g_sfont) {
        DWORD obj = *(DWORD*)(g_exe + 0xc65d10 - 0x400000);
        if (obj > 0x10000 && !IsBadReadPtr((void*)obj, 0x40)) {
            DWORD begin = *(DWORD*)(obj + 8);
            DWORD end = *(DWORD*)(obj + 0xc);
            DWORD e;
            for (e = begin; e + 4 <= end && e - begin < 0x1000; e += 4) {
                DWORD slot = 0, wrapper = 0, font = 0;
                if (IsBadReadPtr((void*)e, 4)) break;
                slot = *(DWORD*)e;
                if (!slot || slot < 0x10000 || IsBadReadPtr((void*)slot, 0x14)) continue;
                wrapper = *(DWORD*)(slot + 4);
                if (!wrapper || wrapper < 0x10000 || IsBadReadPtr((void*)wrapper, 0x10)) continue;
                font = *(DWORD*)(wrapper + 4);
                if (!font || font < 0x10000 || IsBadReadPtr((void*)font, 0x60)) continue;
                DWORD recs = *(DWORD*)(font + 0x4c);
                DWORD first = *(DWORD*)(font + 0x34);
                DWORD cnt = *(DWORD*)(font + 0x3c);
                if (!recs || recs < 0x10000 || cnt > 1024) continue;
                int vi = (int)'V' - (int)first;
                if (vi < 0 || vi >= (int)cnt) continue;
                if (IsBadReadPtr((void*)(recs + vi * 0x14), 0x14)) continue;
                float* r = (float*)(recs + vi * 0x14);
                if (fabsf(r[1] - g_vt[5]) < 0.002f && fabsf(r[2] - g_vt[6]) < 0.002f) {
                    g_sfont = font;
                    g_sfont_recs = recs;
                    g_sfont_first = first;
                    g_sfont_count = cnt;
                    break;
                }
            }
        }
    }
    if (!g_sfont) return;
    /* measure the 'V' glyph EVERY frame: the quad width = the full
       record u-span x the advance scale; the drawn UV rect = the
       record cropped by the game's metric patch (the crop changes
       when the hook patches the font, so measure per frame). The 8th
       glyph of "Version 1.09" = the SPACE - its width has no record. */
    {
        int vi = (int)'V' - (int)g_sfont_first;
        float* r = (float*)(g_sfont_recs + vi * 0x14);
        float wV = 0.0f;
        float mn = 1e30f, mx = -1e30f;
        DWORD i2;
        for (i2 = 0; i2 < 4; i2++) {
            float x = v[i2 * fl];
            if (x < mn) mn = x;
            if (x > mx) mx = x;
        }
        wV = mx - mn;
        float du = r[3] - r[1];
        g_sfactor = (du > 0.0001f) ? (wV / du) : 1.0f;
        float batch_u1 = v[1 * fl + 5];
        float batch_v1 = v[2 * fl + 6];
        g_su_crop = (du > 0.0001f) ? (batch_u1 - g_vt[5]) / du : 1.0f;
        float dv = r[4] - r[2];
        g_sv_crop = (dv > 0.0001f) ? (batch_v1 - g_vt[6]) / dv : 1.0f;
        if (vcount >= 32) {
            float smn = 1e30f, smx = -1e30f;
            for (i2 = 28; i2 < 32 && i2 < (int)vcount; i2++) {
                float x = v[i2 * fl];
                if (x < smn) smn = x;
                if (x > smx) smx = x;
            }
            g_space_w = (smx - smn);
        }
    }
    /* build the suffix quads + one nested 4728e call (the version
       draw's args, only the verts pointer + the count change) */
    static float sverts[VER_SUFFIX_MAXCHARS * 4 * 7];
    float h = mxy - mny;
    float Hf = (float)*(DWORD*)(g_exe + 0xc51280 - 0x400000);
    float pad = 8.0f * (Hf / 1080.0f);
    float curx = g_ver_maxx + pad;
    float y0 = mny;
    /* the highlight range = the [Suffix] SuffixYellowText substring */
    const char* ystart = g_cfg_suffix_yellow_text[0]
                             ? strstr(g_cfg_suffix_text, g_cfg_suffix_yellow_text) : NULL;
    int ypos = ystart ? (int)(ystart - g_cfg_suffix_text) : -1;
    int ylen = (int)strlen(g_cfg_suffix_yellow_text);
    int n = 0;
    const char* c;
    for (c = g_cfg_suffix_text; *c && n < VER_SUFFIX_MAXCHARS; c++) {
        int ci = (int)(unsigned char)*c - (int)g_sfont_first;
        float u0 = 0.0f, v0 = 0.0f, du = 0.0f, dv = 0.0f;
        float w = 0.0f;
        if (ci >= 0 && ci < (int)g_sfont_count) {
            float* r = (float*)(g_sfont_recs + ci * 0x14);
            u0 = r[1];
            v0 = r[2];
            du = r[3] - r[1];
            dv = r[4] - r[2];
            /* the quad WIDTH uses the FULL record u-span (the advance);
               the UV rect uses the cropped span (the metric patch) */
            w = du * g_sfactor;
        }
        else {
            /* the space (no record): the measured space width, the UVs
               stay 0 (transparent atlas corner) */
            w = g_space_w;
        }
        int idx = (int)(c - g_cfg_suffix_text);
        DWORD colc = (idx >= ypos && idx < ypos + ylen)
                         ? g_cfg_suffix_yellow : VER_SUFFIX_WHITE;
        float colf;
        memcpy(&colf, &colc, 4);
        float u1 = u0 + du * g_su_crop;
        float v1 = v0 + dv * g_sv_crop;
        float x1 = curx + w;
        float y1 = y0 + h;
        float* o = sverts + n * 28;
        int i2;
        for (i2 = 0; i2 < 4; i2++) {
            /* the game's strip order = TL, TR, BR, BL (zigzag) */
            float X, Y, U, V;
            if (i2 == 0) { X = curx; Y = y0;  U = u0; V = v0; }
            else if (i2 == 1) { X = x1; Y = y0;  U = u1; V = v0; }
            else if (i2 == 2) { X = x1; Y = y1;  U = u1; V = v1; }
            else { X = curx; Y = y1;  U = u0; V = v1; }
            float* d = o + i2 * 7;
            /* vertex layout: {x, y, z, rhw, DIFFUSE, u, v} */
            d[0] = X;
            d[1] = Y;
            d[2] = g_vt[2];
            d[3] = g_vt[3];
            d[4] = colf;
            d[5] = U;
            d[6] = V;
        }
        n++;
        curx = x1;
    }
    if (n < 1) return;
    DWORD nv = (DWORD)(n * 4);
    DWORD a3 = nv;
    DWORD a4 = nv / 2;
    DWORD fn = (DWORD)g_exe + (0x44728e - 0x400000);
    DWORD sverts_ptr = (DWORD)sverts;
    DWORD se = g_saved_ebp, se2 = g_saved_ebp2;
    g_suffix_draw = 1;
    __asm__ volatile (
        "pushl %[a8]\n"
        "pushl %[verts]\n"
        "pushl %[a6]\n"
        "pushl %[a5]\n"
        "pushl %[a4]\n"
        "pushl %[a3]\n"
        "pushl %[a2]\n"
        "pushl %[a1]\n"
        "call *%[f]\n"
        "addl $32, %%esp\n"
        :
        : [f] "r" (fn), [a1] "m" (g_4728e_arg0), [a2] "m" (g_4728e_arg1),
          [a3] "m" (a3), [a4] "m" (a4), [a5] "m" (g_4728e_arg4),
          [a6] "m" (g_4728e_arg5), [verts] "m" (sverts_ptr), [a8] "m" (g_4728e_arg7)
        : "eax", "ecx", "edx", "memory", "cc");
    g_suffix_draw = 0;
    g_saved_ebp = se;
    g_saved_ebp2 = se2;
}

/* ---- v58: message window 16:9 right-anchoring ----
   The portrait message window (cMessageScreen, singleton c6818c) draws
   its 3D frame + speaker head as a Scene-widget render via
   FUN_0047523a (thiscall, ECX = the scene object): the view matrix
   lives at [[scene+0x40]+0x60] (camera (0,0,400) for the UI scenes)
   and the screen-model slot = [scene+0x7c] (0 = no screen model).
   Only the message window's scene must move: other UI scenes share the
   same default camera (empire-info 3D, planet background) but they
   also render while NO message is up, while the message scene exists
   only while one shows. Per-scene classification:
     st=1 = seen with no message up -> never shift
     st=2 = first seen during a message -> message scene, shift once
            (sticky: the view stays shifted through the close fade;
             the game never rewrites it)
   The view is constant per scene -> writing the absolute value is
   idempotent. Calibration: the UI-depth plane maps ~3.17 px per view
   unit at 1080p -> -75.7 view units = the +240px half-centering. */
#define MSG_SCENE_SHIFT (-75.7f)
/* message text + count ("1/2") extra right offset, screen px at 1080p */
#define MSG_TEXT_EXTRA 10.0f

__attribute__((used, noinline)) static DWORD msg_window_obj(void) {
    if (!g_exe) return 0;
    DWORD o = *(DWORD*)(g_exe + 0xc6818c - 0x400000);
    if (!o || o < 0x10000 || IsBadReadPtr((void*)(o + 0x110), 4)) return 0;
    return o;
}

__attribute__((used, noinline)) static int msg_window_active(void) {
    DWORD o = msg_window_obj();
    if (!o) return 0;
    return *(DWORD*)(o + 0xe8) != 0;
}

__attribute__((used)) static DWORD g_scene_resume = 0;
__attribute__((used)) static DWORD g_msgscenes[16];
__attribute__((used)) static float g_msgscenes_x[16];
__attribute__((used)) static int g_msgst[16];
__attribute__((used)) static int g_msgscenes_n = 0;
__attribute__((used)) static DWORD g_first_active_scene = 0;

__attribute__((used, noinline)) static void scene_fix(DWORD sceneW) {
    if (!g_exe) return;
    DWORD o = msg_window_obj();
    int active = 0;
    if (o) active = *(DWORD*)(o + 0xe8) != 0;
    if (sceneW <= 0x10000 || IsBadReadPtr((void*)(sceneW + 0x40), 8)) return;
    DWORD ctx = *(DWORD*)(sceneW + 0x40);
    DWORD sm = *(DWORD*)(sceneW + 0x7c);
    /* v60: track the current scene (for the ship-container detection
       in my_SetTransform) */
    g_last_scene = sceneW;
    g_last_svx = g_last_svy = g_last_svz = 0.0f;
    if (ctx > 0x10000 && !IsBadReadPtr((void*)(ctx + 0x60), 64)) {
        float* vv = (float*)(ctx + 0x60);
        g_last_svx = vv[12]; g_last_svy = vv[13]; g_last_svz = vv[14];
    }
    /* v60: ship-panel scene = the container. Shift its view x left so the
       rotating ships + TL display leave the centered 4:3 box. px/unit =
       1.3589*(W/2)/depth, depth = y_model+400 = 1595 for the ships ->
       +293.5 units = 240px @1080p. The STATIC models in the scene are
       compensated back in my_SetTransform; the v30 text/icon shifts are
       removed (they follow the projection). */
    if (g_shift > 1.0f && g_ship_scene && sceneW == g_ship_scene &&
        ctx > 0x10000 && !IsBadReadPtr((void*)(ctx + 0x60), 64)) {
        float* sv = (float*)(ctx + 0x60);
        if (sv[13] == 0.0f && sv[14] == 400.0f) {
            float Wf = (float)*(DWORD*)(g_exe + 0xc5127c - 0x400000);
            float units = g_shift * 1595.0f / (1.3589f * Wf * 0.5f);
            if (sv[12] != units) {
                sv[12] = units;
                if (!g_shipview_logged) {
                    g_shipview_logged = 1;
                    char buf[128];
                    _snprintf(buf, sizeof(buf), "== ship scene view shift: +%.2f units ==\n", (double)units);
                    log_line(buf);
                }
            }
        }
    }
    if (sm != 0) return;
    if (ctx <= 0x10000 || IsBadReadPtr((void*)(ctx + 0x60), 64)) return;
    float* view = (float*)(ctx + 0x60);
    if (view[13] != 0.0f || view[14] != 400.0f) return;
    int i, found = -1;
    for (i = 0; i < g_msgscenes_n; i++) {
        if (g_msgscenes[i] == sceneW) { found = i; break; }
    }
    if (found < 0 && g_msgscenes_n < 16) {
        found = g_msgscenes_n++;
        g_msgscenes[found] = sceneW;
        g_msgst[found] = 0;
        g_msgscenes_x[found] = 0.0f;
        /* v68: the FIRST default-camera scene ever seen with a message
           up = the message window's scene. Any OTHER scene registering
           while a message is up = a SCREEN (empire/planet/research)
           opened during the message - it must never get the message
           view shift (its 3D frame is already centered). */
        if (active) {
            if (!g_first_active_scene) {
                g_first_active_scene = sceneW;
            }
            else if (sceneW != g_first_active_scene) {
                g_msgst[found] = 1;
            }
        }
    }
    if (found < 0) return;
    if (!active) {
        if (g_msgst[found] == 0) g_msgst[found] = 1;
    }
    else if (g_msgst[found] == 0) {
        g_msgst[found] = 2;
        g_msgscenes_x[found] = view[12] + MSG_SCENE_SHIFT;
    }
    if (g_msgst[found] == 2 && view[12] != g_msgscenes_x[found]) {
        view[12] = g_msgscenes_x[found];
    }
}

__attribute__((naked)) static void scene_render_stub(void) {
    __asm__ volatile (
        "pushal\n"
        "movl 24(%esp), %eax\n"   /* saved ECX = param_1 (the scene object) */
        "pushl %eax\n"
        "call _scene_fix\n"
        "addl $4, %esp\n"
        "popal\n"
        "pushl %ebp\n"
        "movl %esp, %ebp\n"
        "subl $0xE8, %esp\n"
        "pushl %esi\n"
        "pushl %edi\n"
        "jmp *_g_scene_resume\n"
    );
}

__attribute__((used, noinline)) static void patch_scene_hook(void* exe) {
    BYTE* p = (BYTE*)exe + (0x47523a - 0x400000);
    DWORD rel = (DWORD)((BYTE*)&scene_render_stub - (p + 5));
    if (p[0] == 0xE9 && *(DWORD*)(p + 1) == rel) return;
    if (p[0] != 0x55 || p[1] != 0x8B || p[2] != 0xEC ||
        p[3] != 0x81 || p[4] != 0xEC) {
        log_line("== message scene hook entry mismatch ==\n");
        return;
    }
    g_scene_resume = 0x47523a + 11;
    DWORD oldp;
    VirtualProtect(p, 5, PAGE_EXECUTE_READWRITE, &oldp);
    p[0] = 0xE9;
    *(DWORD*)(p + 1) = rel;
    VirtualProtect(p, 5, oldp, &oldp);
    log_line("== message-window scene hook installed (0x47523A) ==\n");
}

/* 471f6 = quad draw: centering shifts for the loading bar, quit strips,
   research containers and the BR FX stripe */

__attribute__((used)) void qlog_quad(DWORD caller, DWORD verts, DWORD vcount, DWORD stride) {
    if (!verts || verts < 0x10000) return;
    if (IsBadReadPtr((void*)verts, 8)) return;
    /* owner = the ret into the quad renderer's caller (screen-level fn) */
    if (g_saved_ebp2 > 0x10000 && !IsBadReadPtr((void*)(g_saved_ebp2 + 4), 4)) {
        DWORD owner = *(DWORD*)(g_saved_ebp2 + 4);
        DWORD rva2 = owner - 0x400000;        /* v24.43: center the pre-menu loading bar (Start variant):
           FUN_00717BA5 fill (0x717F00) + remain (0x717F76) quads are
           authored at 254..547 in the 800-space -> shift by g_shift */
        if (g_shift > 1.0f && (rva2 == 0x317F05 || rva2 == 0x317F7B) &&
            vcount <= 8 && !IsBadReadPtr((void*)verts, stride * vcount)) {
            float* qv = (float*)verts;
            int qf = (int)(stride / 4);
            if (qf < 3) qf = 5;
            float mnx = 1e30f, mxx = -1e30f;
            DWORD i2;
            for (i2 = 0; i2 < vcount; i2++) {
                float x = qv[i2 * qf];
                if (x < mnx) mnx = x;
                if (x > mxx) mxx = x;
            }
            if (mnx >= 0.0f && mxx <= g_thresh) {
                for (i2 = 0; i2 < vcount; i2++) {
                    qv[i2 * qf] += g_shift;
                }
            }
        }
        /* v24.44: quit-dialog fade strips (FUN_007a5c71 sites 0x7A5F80/
           0x7A5FE2): two 800x75 black strips at (0,0) and (0,525) that
           fade the screen behind the yes/no popup. Authored 800 design
           wide -> cover only 0..1440 at 16:9. Extend the right verts to
           the full screen width. */
        if (g_shift > 1.0f && (rva2 == 0x3A5F85 || rva2 == 0x3A5FE7) &&
            vcount <= 8 && !IsBadReadPtr((void*)verts, stride * vcount)) {
            float* qv = (float*)verts;
            int qf = (int)(stride / 4);
            if (qf < 3) qf = 5;
            float Wf = (float)*(DWORD*)(g_exe + 0xc5127c - 0x400000);
            DWORD i2;
            for (i2 = 0; i2 < vcount; i2++) {
                float x = qv[i2 * qf];
                if (x > 1400.0f && x < Wf - 1.0f) {
                    qv[i2 * qf] = Wf;
                }
            }
        }
        /* center the left-authored UI containers drawn via the widget
           renderer (call site 0x541EF1):
           - research screen FUN_0075bfa3 (0x75BFA3..0x75D517) */
        if (g_shift > 1.0f && caller == 0x541EF1) {
            static const DWORD ranges[2] = { 0x75BFA3, 0x75D517 };
            int hit = 0, r;
            for (r = 0; r < 2; r += 2) {
                if (owner >= ranges[r] && owner <= ranges[r + 1]) { hit = 1; break; }
            }
            if (hit && vcount <= 32 && !IsBadReadPtr((void*)verts, stride * vcount)) {
                float* qv = (float*)verts;
                int qf = (int)(stride / 4);
                if (qf < 3) qf = 5;
                float mnx = 1e30f, mxx = -1e30f;
                DWORD i2;
                for (i2 = 0; i2 < vcount; i2++) {
                    float x = qv[i2 * qf];
                    if (x < mnx) mnx = x;
                    if (x > mxx) mxx = x;
                }
                if (mnx >= 0.0f && mxx <= g_thresh) {
                    for (i2 = 0; i2 < vcount; i2++) {
                        qv[i2 * qf] += g_shift;
                    }
                }
            }
        }
        /* v27: BR panel FX strip. The screenselect panel's decorative
           zigzag stripe draws as design-space lines (FUN_00711c0a ->
           471f6 call site 0x711D46) authored at design x ~603-872
           (bottom-right of the 800 box) but drawn UN-shifted. Anchor it
           to the 16:9 corner: the panel's authored right edge (793) must
           land at W-7 (the original 4:3 margin).
           ONLY the BR renderer's call sites (rva2 0x2C4A2C/0x2C4A50 in
           the runtime-rewritten 0x42C4xx block) - other line users
           (e.g. the BL squad-panel capacity triangles at 0x37F9A4)
           must stay untouched. */
        if (g_shift > 1.0f && caller == 0x711D46 && g_exe &&
            (rva2 == 0x2C4A2C || rva2 == 0x2C4A50) &&
            vcount >= 2 && vcount <= 64 && stride >= 8 && stride <= 0x40 &&
            !IsBadReadPtr((void*)verts, stride * vcount)) {
            float Wf = (float)*(DWORD*)(g_exe + 0xc5127c - 0x400000);
            float Hf = (float)*(DWORD*)(g_exe + 0xc51280 - 0x400000);
            float sf = Hf / 600.0f;
            float fxshift = Wf - 7.0f - 793.0f * sf;
            float* qv = (float*)verts;
            int qf = (int)(stride / 4);
            if (qf < 3) qf = 5;
            DWORD i2;
            for (i2 = 0; i2 < vcount; i2++) {
                qv[i2 * qf] += fxshift;
            }
        }
        /* v34: technology tree screen (0x7d527c..0x7df406, the
           TechTreeScreen methods). Every draw owned by it - the tree
           EDGES (n=2 line segments via 471f6), the node frames/glow
           quads - is authored in the 4:3 box and left-aligned at
           16:9 -> shift by g_shift. The version text (FUN_007d42f0,
           0x7d42f0..0x7d4994) is below the range and already shifts
           via the queue rule. */
        if (g_shift > 1.0f && owner >= 0x7d527c && owner <= 0x7df406 &&
            vcount > 0 && vcount <= 4096 && stride >= 8 && stride <= 0x40 &&
            !IsBadReadPtr((void*)verts, stride * vcount)) {
            float* qv = (float*)verts;
            int qf = (int)(stride / 4);
            if (qf < 3) qf = 5;
            DWORD i2;
            for (i2 = 0; i2 < vcount; i2++) {
                qv[i2 * qf] += g_shift;
            }
        }
        /* v37: TR panel lines. FUN_00712c5e (TR production/stat bars,
           called only from the TR fn FUN_0077ae14) delegates to
           FUN_00711c0a -> the 471f6 call site is 0x711D46 and the
           frame above it is FUN_00712c5e itself (owner = ret into it).
           The panel anchors its RIGHT edge -> rshift. */
        if (g_shift > 1.0f && caller == 0x711D46 && g_exe &&
            owner >= 0x712c5e && owner <= 0x712da6 &&
            vcount >= 2 && vcount <= 64 && stride >= 8 && stride <= 0x40 &&
            !IsBadReadPtr((void*)verts, stride * vcount)) {
            float Wf = (float)*(DWORD*)(g_exe + 0xc5127c - 0x400000);
            float sf = (float)*(DWORD*)(g_exe + 0xc51280 - 0x400000) / 600.0f;
            float rshift = Wf - 800.0f * sf;
            float* qv = (float*)verts;
            int qf = (int)(stride / 4);
            if (qf < 3) qf = 5;
            DWORD i2;
            for (i2 = 0; i2 < vcount; i2++) {
                qv[i2 * qf] += rshift;
            }
        }
        /* v36: progress bars. The research + planet production bars are
           drawn as a horizontal LINE (n=2) by FUN_00712755
           (0x712755..0x712984), called from FUN_0075bfa3 (research
           screen) and FUN_0074d20c (planet production). They are
           authored in the 4:3 box -> +g_shift. The map-label caller
           (FUN_00780fa1 site 0x785449) must NEVER shift. */
        if (g_shift > 1.0f && caller >= 0x712755 && caller <= 0x712984 &&
            !(owner >= 0x780FA1 && owner <= 0x787958) &&
            vcount > 0 && vcount <= 64 && stride >= 8 && stride <= 0x40 &&
            !IsBadReadPtr((void*)verts, stride * vcount)) {
            float* qv = (float*)verts;
            int qf = (int)(stride / 4);
            if (qf < 3) qf = 5;
            DWORD i2;
            for (i2 = 0; i2 < vcount; i2++) {
                qv[i2 * qf] += g_shift;
            }
        }
        /* v42: MP setup screen (FUN_007ce33c + FUN_007cec54,
           0x7ce33c..0x7cef09): the participating-player row frames +
           separators (n=2 line segments via FUN_00711c0a). Authored in
           the 800-space -> +g_shift (the row text already shifts via
           the queue rule). */
        if (g_shift > 1.0f && owner >= 0x7ce33c && owner <= 0x7cef09 &&
            vcount > 0 && vcount <= 256 && stride >= 8 && stride <= 0x40 &&
            !IsBadReadPtr((void*)verts, stride * vcount)) {
            float* qv = (float*)verts;
            int qf = (int)(stride / 4);
            if (qf < 3) qf = 5;
            float mnx = 1e30f, mxx = -1e30f;
            DWORD i2;
            for (i2 = 0; i2 < vcount; i2++) {
                float x = qv[i2 * qf];
                if (x < mnx) mnx = x;
                if (x > mxx) mxx = x;
            }
            if (mnx >= 0.0f && mxx <= g_thresh) {
                for (i2 = 0; i2 < vcount; i2++) {
                    qv[i2 * qf] += g_shift;
                }
            }
        }
        /* v42: the LEFT border of the same player rows. The top/right/
           bottom edges draw via FUN_00711c0a (owner 0x7CECE1 above) but
           the left edge goes through FUN_0071a9e0 (design-line helper,
           471f6 call sites 0x71AA5D, called via FUN_00711e94 sites
           0x712732/0x712745). Same +g_shift.
           v57: FUN_00711e94 (the bordered-box drawer) also draws the
           MAP name-plate borders (world space - NEVER shift). Walk the
           frames above the helper: shift only when the MP screen fn
           (0x7CE33C..0x7CEF09) is in the chain and the map fn
           (0x780FA1..0x787958) is NOT. */
        if (g_shift > 1.0f && caller >= 0x71A9E0 && caller <= 0x71AAA9 &&
            vcount > 0 && vcount <= 16 && stride >= 8 && stride <= 0x40 &&
            !IsBadReadPtr((void*)verts, stride * vcount)) {
            DWORD f = g_saved_ebp2;
            int mp = 0, map = 0, k;
            for (k = 0; k < 4 && f > 0x10000 && f < 0x7FFE0000; k++) {
                if (IsBadReadPtr((void*)f, 8)) break;
                DWORD ret = *(DWORD*)(f + 4);
                DWORD next = *(DWORD*)f;
                if (ret >= 0x7CE33C && ret <= 0x7CEF09) mp = 1;
                if (ret >= 0x780FA1 && ret <= 0x787958) map = 1;
                if (next <= f) break;
                f = next;
            }
            if (mp && !map) {
                float* qv = (float*)verts;
                int qf = (int)(stride / 4);
                if (qf < 3) qf = 5;
                float mnx = 1e30f, mxx = -1e30f;
                DWORD i2;
                for (i2 = 0; i2 < vcount; i2++) {
                    float x = qv[i2 * qf];
                    if (x < mnx) mnx = x;
                    if (x > mxx) mxx = x;
                }
                if (mnx >= 0.0f && mxx <= g_thresh) {
                    for (i2 = 0; i2 < vcount; i2++) {
                        qv[i2 * qf] += g_shift;
                    }
                }
            }
        }
    }
}

/* ---- 2x font metrics patch (v24) ----
   Font object (100 bytes, FUN_00439580): +0x18 = image W (int),
   +0x1c = image H (int), +0x20 = advance px (float, = +0x18*scaleX),
   +0x24 = lineH px (float, = +0x1c*scaleY), +0x38 = cellH (int).
   The 2x .chr doubles the u/v deltas -> doubled advance/lineH/quad width.
   Halve the ints + derived floats so the game's own measurements are 1x.
   Fonts live in the vector at c65d10+8..+0xc. Each entry is a 0x14-byte
   slot {image, wrapper}; the FONT is at [wrapper+4] (see FUN_004f014e:
   iVar3 = *(*(c65d10+8)+idx*4)+4, then [iVar3+4]+0x18). c65d10 is only
   valid during a text draw, so scan from the 4728e hook. */
static DWORD g_fontpatched[64];
static int g_fontpatched_n = 0;
static int g_fontscan_state = 0;
static DWORD g_last_fontscan = 0;
static int g_fontscan_fails = 0;

/* atlas scale S = physical atlas px / 256 design px. The tool authors
   fonts at 256*S (S=1,2,4); keep 2 as the fallback for unknown sizes. */
static int font_scale_for(int imw, int imh) {
    (void)imh;
    if (imw == 256) return 1;
    if (imw == 512) return 2;
    if (imw == 1024) return 4;
    if (imw == 2048) return 8;
    if (imw >= 64 && (imw % 256) == 0) return imw / 256;
    return 2;
}

/* lineH = (vspanA)*[font+0x1c] + 1 (the game adds +1); the quad height
   is [font+0x30] = header cellH*scaleY. Set +0x1c so lineH == cellH ==
   quad height for every font. vspanA = the 'A' glyph's v-rect (atlas px). */
static int font_line_h_for(DWORD font, int imh, int cellh, int cnt, int S) {
    int spanPx = 0;
    if (cellh > 1) {
        DWORD tbl = *(DWORD*)(font + 0x4c);
        int firstc = *(int*)(font + 0x34);
        int idx = 0x41 - firstc;
        if (tbl && idx >= 0 && idx < cnt && !IsBadReadPtr((void*)(tbl + idx * 0x14), 0x14)) {
            float v0 = *(float*)(tbl + idx * 0x14 + 8);
            float v1 = *(float*)(tbl + idx * 0x14 + 0x10);
            spanPx = (int)((v1 - v0) * imh + 0.5f);
        }
    }
    if (spanPx > 0) {
        int lh = (int)((float)(cellh - 1) * imh / spanPx + 0.5f);
        if (lh < 4) lh = 4;
        if (lh > imh) lh = imh;
        return lh;
    }
    return (int)((float)(imh / S) * g_cfg_line_factor);
}

static void font_metrics_scan(void) {
    if (!g_exe) return;
    {
        DWORD now = GetTickCount();
        if (g_fontscan_state == 1 && now - g_last_fontscan < 30000) return;
        DWORD textobj = *(DWORD*)(g_exe + 0xc65d10 - 0x400000);
        if (!textobj || textobj < 0x10000 || IsBadReadPtr((void*)textobj, 0x10)) {
            if (g_fontscan_state == 0) g_fontscan_fails++;
            return;
        }
        DWORD begin = *(DWORD*)(textobj + 8);
        DWORD end = *(DWORD*)(textobj + 0xc);
        if (!begin || end <= begin || (end - begin) > 0x4000) {
            if (g_fontscan_state == 0) g_fontscan_fails++;
            return;
        }
        int n = 0, patched_now = 0;
        for (DWORD p = begin; p < end && n < 256; p += 4, n++) {
            DWORD slot = 0, wrap = 0, font = 0;
            int imw = 0, imh = 0, cellh = 0, cnt = 0;
            int rej = 0;
            if (IsBadReadPtr((void*)p, 4)) break;
            slot = *(DWORD*)p;
            if (!slot || slot < 0x10000 || IsBadReadPtr((void*)slot, 0x14)) { rej = 1; }
            if (!rej) {
                wrap = *(DWORD*)(slot + 4);
                if (!wrap || wrap < 0x10000 || IsBadReadPtr((void*)wrap, 0x10)) { rej = 2; }
            }
            if (!rej) {
                font = *(DWORD*)(wrap + 4);
                if (!font || font < 0x10000 || IsBadReadPtr((void*)font, 0x60)) { rej = 3; }
            }
            if (!rej) {
                imw = *(int*)(font + 0x18);
                imh = *(int*)(font + 0x1c);
                cellh = *(int*)(font + 0x38);
                cnt = *(int*)(font + 0x3c);
                if (imw < 32 || imw > 4096 || imh < 32 || imh > 4096) rej = 4;
                else if (cellh < 4 || cellh > 256) rej = 5;
                else if (cnt < 32 || cnt > 4096) rej = 6;
                else {
                    DWORD tbl = *(DWORD*)(font + 0x4c);
                    if (!tbl || tbl < 0x10000 || IsBadReadPtr((void*)tbl, 4)) rej = 7;
                }
            }
            if (rej) {
                continue;
            }
            {
                int i, known = 0;
                for (i = 0; i < g_fontpatched_n; i++) {
                    if (g_fontpatched[i] == font) { known = 1; break; }
                }
                if (known) continue;
                if (g_fontpatched_n < 64) g_fontpatched[g_fontpatched_n++] = font;
                {
                    int S = font_scale_for(imw, imh);
                    int lh = font_line_h_for(font, imh, cellh, cnt, S);
                    *(int*)(font + 0x18) = imw / S;
                    *(int*)(font + 0x1c) = lh;
                    *(float*)(font + 0x20) *= 1.0f / S;
                    *(float*)(font + 0x24) *= (1.0f / S) * g_cfg_line_factor;
                }
                patched_now = 1;
            }
        }
        if (n > 0) {
            g_last_fontscan = now;
            if (patched_now) {
                g_fontscan_state = 1;
                g_fontscan_fails = 0;
            }
            else if (g_fontpatched_n > 0 && g_fontscan_state == 0) {
                g_fontscan_state = 1;
            }
        }
    }
}

/* scale icon quads by `f` about the builder's origin args:
   cebp = the builder's (ecd6/efac) ebp; [cebp+8]/[cebp+0xC] = origin
   (design coords). Used for the cursor (origin-anchored). */
static void scale_icon_quads(DWORD cebp, float* v, DWORD vcount, int fl, float f) {
    float dw = *(float*)(g_exe + 0xc0a790 - 0x400000);
    float sx = (float)*(DWORD*)(g_exe + 0xc5127c - 0x400000) / dw;
    float sy = (float)*(DWORD*)(g_exe + 0xc51280 - 0x400000) / 600.0f;
    float ox = *(float*)(cebp + 8) * sx;
    float oy = *(float*)(cebp + 12) * sy;
    DWORD i3;
    for (i3 = 0; i3 < vcount; i3++) {
        v[i3 * fl] = ox + (v[i3 * fl] - ox) * f;
        v[i3 * fl + 1] = oy + (v[i3 * fl + 1] - oy) * f;
    }
}

/* scale icon quads by `f` about an explicit pivot */
static void scale_icon_at(float* v, DWORD vcount, int fl, float cx, float cy, float f) {
    DWORD i3;
    for (i3 = 0; i3 < vcount; i3++) {
        v[i3 * fl] = cx + (v[i3 * fl] - cx) * f;
        v[i3 * fl + 1] = cy + (v[i3 * fl + 1] - cy) * f;
    }
}

/* scale icon quads by `f` about the CENTER of this call's quads - keeps
   single-cell icons centered in their original bounds. */
static void scale_icon_center(float* v, DWORD vcount, int fl, float f) {
    float mnx = 1e30f, mxx = -1e30f, mny = 1e30f, mxy = -1e30f;
    DWORD i3;
    for (i3 = 0; i3 < vcount; i3++) {
        float x = v[i3 * fl];
        float y = v[i3 * fl + 1];
        if (x < mnx) mnx = x;
        if (x > mxx) mxx = x;
        if (y < mny) mny = y;
        if (y > mxy) mxy = y;
    }
    scale_icon_at(v, vcount, fl, (mnx + mxx) * 0.5f, (mny + mxy) * 0.5f, f);
}

/* ---- whole-icon size for multi-cell map icons: ecd6 saves its ECX
   (the atlas) to [ebp-0x54] in its prologue; the 4728e hook runs inside
   ecd6's loop with g_saved_ebp = ecd6's ebp, so the atlas is reachable
   directly from the hook. NO call-site wrappers / entry patches: the
   game rewrites its code at runtime and hijacking sites caused
   AV-swallowed calls (startup ECX was a small ID, not the atlas). ---- */
__attribute__((used)) static float g_icon_w = 0.0f;   /* whole icon size, screen px */
__attribute__((used)) static float g_icon_h = 0.0f;

/* returns 1 and sets g_icon_w/h if the atlas (ecd6's ECX) is valid */
static int atlas_dims_from_ebp(DWORD ebp) {
    DWORD atlas = *(DWORD*)(ebp - 0x54);
    if (!atlas || atlas < 0x10000 || IsBadReadPtr((void*)atlas, 0x40)) return 0;
    int cpr = *(int*)(atlas + 0x14);
    int cw = *(int*)(atlas + 0x1c);
    int ch = *(int*)(atlas + 0x20);
    int cnt = *(int*)(atlas + 0x30);
    if (cpr < 1 || cpr > 64 || cw < 1 || cw > 512 || ch < 1 || ch > 512 ||
        cnt < 1 || cnt > 4096) return 0;
    float dw = *(float*)(g_exe + 0xc0a790 - 0x400000);
    float sx = (float)*(DWORD*)(g_exe + 0xc5127c - 0x400000) / dw;
    float sy = (float)*(DWORD*)(g_exe + 0xc51280 - 0x400000) / 600.0f;
    int rows = (cnt + cpr - 1) / cpr;
    g_icon_w = (float)(cpr * cw) * sx;
    g_icon_h = (float)(rows * ch) * sy;
    return 1;
}

/* ---- row height fix: FUN_004f21c2 (font-height accessor) returns
   [font+0x30] = cellH*scaleY, which the list renderers use as the row
   pitch -> rows wrap the text with excess height. Scale the return by
   ROW_FACTOR. Convention-agnostic trampoline: the caller sites (E8)
   are retargeted to a wrapper that swaps the return address for a
   scale stub and JMPs into the real runtime function; the function's
   own RET (whatever its convention) lands in the stub, which scales
   ST(0) and jumps to the saved caller return address. ---- */
__attribute__((used)) static void* g_rowh_real = (void*)0x004f21c2;
__attribute__((used)) static float g_rowh_factor = ROW_FACTOR;__attribute__((used)) static DWORD g_rowh_caller_ret = 0;
__attribute__((used)) static DWORD g_rowh_caller_edx = 0;

__attribute__((used, naked)) static void rowh_scale_stub(void) {
    __asm__ volatile (
        "fmuls _g_rowh_factor\n"
        "movl _g_rowh_caller_edx, %edx\n"
        "jmp *_g_rowh_caller_ret\n"
    );
}

__attribute__((naked)) static void wrap_rowh_call(void) {
    __asm__ volatile (
        "popl %eax\n"
        "movl %eax, _g_rowh_caller_ret\n"
        "movl %edx, _g_rowh_caller_edx\n"
        "pushl $_rowh_scale_stub\n"
        "jmp *_g_rowh_real\n"
    );
}

static void patch_rowh_sites(void* exe) {
    static const DWORD sites[18] = {
        0x500e8d, 0x500ecb, 0x506ee4, 0x507ef3, 0x507f28, 0x517fde,
        0x51fad0, 0x520c0f, 0x521581, 0x522006, 0x529de4, 0x52d9d7,
        0x52da15, 0x718819, 0x73eeb3, 0x75e452, 0x75c69d, 0x7cee6b
    };
    for (int i = 0; i < 18; i++) {
        BYTE* p = (BYTE*)exe + (sites[i] - 0x400000);
        DWORD rel = (DWORD)((BYTE*)&wrap_rowh_call - (p + 5));
        if (p[0] == 0xE8 && *(DWORD*)(p + 1) == rel) continue;
        if (p[0] != 0xE8) continue;
        DWORD oldp;
        VirtualProtect(p, 5, PAGE_EXECUTE_READWRITE, &oldp);
        *(DWORD*)(p + 1) = rel;
        VirtualProtect(p, 5, oldp, &oldp);
    }
}

/* ---- viewport/clip centering (save-list crop fix): FUN_0044864e
   (renderer SetRect, vtable+0xa0) receives a 6-dword viewport
   {x,y,w,h,zmin,zmax} (ints + floats). UI widgets set authored
   left-box rects (x=0, w=800*scale) as the clip -> centered text gets
   cropped. Shift x by the centering shift when the rect is a left box
   (x==0, 100 < w < screen W). Wraps the 5 call sites (tail-jump). ---- */
__attribute__((used)) void setrect_probe(DWORD site, DWORD r) {
    (void)site;
    int x = *(int*)(r + 0);
    int w = *(int*)(r + 8);
    int W = (int)*(DWORD*)(g_exe + 0xc5127c - 0x400000);
    if (g_shift > 1.0f && w > 100 && w < W - 10) {
        *(int*)(r + 0) = x + (int)g_shift;
    }
}

__attribute__((used)) static void* g_setrect_real = (void*)0x0044864e;

__attribute__((used, naked)) static void wrap_setrect(void) {
    __asm__ volatile (
        "pushal\n"
        "movl 36(%esp), %ecx\n"
        "movl 32(%esp), %edx\n"
        "pushl %ecx\n"
        "pushl %edx\n"
        "call _setrect_probe\n"
        "addl $8, %esp\n"
        "popal\n"
        "jmp *_g_setrect_real\n"
    );
}

__attribute__((used, noinline)) static unsigned int techhit_c(DWORD node) {
    if (!g_exe) return 0;
    if (*(int*)(node + 0x18) == 0) return 0;
    DWORD slot = *(DWORD*)(node + 0x10);
    DWORD obj = 0;
    if (slot && slot > 0x10000 && !IsBadReadPtr((void*)slot, 4)) {
        obj = *(DWORD*)slot;
    }
    if (!obj || obj < 0x10000 || IsBadReadPtr((void*)obj, 0x110)) return 0;
    float x = *(float*)(g_exe + 0xc68030 - 0x400000);
    float y = *(float*)(g_exe + 0xc68034 - 0x400000);
    float xy[2];
    xy[0] = x - g_tech_mouse_shift;
    xy[1] = y;
    unsigned int r;
    __asm__ volatile (
        "movl %[o], %%ecx\n"
        "leal %[p], %%eax\n"
        "pushl %%eax\n"
        "movl $0x009e15bc, %%eax\n"
        "call *%%eax\n"
        : "=a"(r)
        : [o] "m"(obj), [p] "m"(xy[0])
        : "ecx", "edx", "memory"
    );
    return r;
}

__attribute__((naked)) static void techhit_stub(void) {
    __asm__ volatile (
        "pushl %ecx\n"
        "call _techhit_c\n"
        "addl $4, %esp\n"
        "ret\n"
    );
}

static void patch_techhit(void* exe) {
    BYTE* p = (BYTE*)exe + (0x7d7cb5 - 0x400000);
    if (p[0] == 0xE9) return;
    if (p[0] != 0x55 || p[1] != 0x8B || p[2] != 0xEC) {
        return;
    }
    DWORD rel = (DWORD)((BYTE*)&techhit_stub - (p + 5));
    DWORD oldp;
    VirtualProtect(p, 5, PAGE_EXECUTE_READWRITE, &oldp);
    p[0] = 0xE9;
    *(DWORD*)(p + 1) = rel;
    VirtualProtect(p, 5, oldp, &oldp);
    log_line("== tech-tree node hit-test hooked (0x7D7CB5) ==\n");
}

static void patch_setrect_sites(void* exe) {
    static const DWORD sites[5] = { 0x43cff9, 0x4474a2, 0x517996, 0x5208ee, 0x7ddf79 };
    for (int i = 0; i < 5; i++) {
        BYTE* p = (BYTE*)exe + (sites[i] - 0x400000);
        DWORD rel = (DWORD)((BYTE*)&wrap_setrect - (p + 5));
        if (p[0] == 0xE8 && *(DWORD*)(p + 1) == rel) continue;
        if (p[0] != 0xE8) continue;
        DWORD oldp;
        VirtualProtect(p, 5, PAGE_EXECUTE_READWRITE, &oldp);
        *(DWORD*)(p + 1) = rel;
        VirtualProtect(p, 5, oldp, &oldp);
    }
}

/* ---- starmap label placement grid: 16:9 extension (v40) ----
   FUN_0077ee70 (placement check) and FUN_0077ed6a/FUN_0077edde (cell
   marks) keep a 800x600 occupancy grid (obj+0x1140, stride 600, cell
   = 1 design unit). At 16:9 the design width is 600*W/H (1066.67 @
   1080p), so the hardcoded x-bound 800 confines every label to the
   4:3 box (screen px 1440): stars past that edge get their labels
   placed inside the box and the label plates/footnotes stop at 1440.
   Fix: scale the x coordinate of all three grid accesses by
   800/designW = 800/(600*W/H) = 4H/3W (exact integer math, no FPU
   disturbance). A uniform x-scale preserves overlap relations and the
   y axis is untouched; the existing 480000-byte buffer keeps working
   (the grid is a scratch occupancy map, not an absolute layout).
   The functions are thiscall+stdcall (caller never cleans the stack,
   ret 0x10). ---- */

static int grid_sx(int v) {
    if (!g_exe) return v;
    int W = *(int*)(g_exe + 0xc5127c - 0x400000);
    int H = *(int*)(g_exe + 0xc51280 - 0x400000);
    if (W <= 0 || H <= 0) return v;
    return (int)(((long long)v * 4 * H) / (3LL * W));
}

/* FUN_0077ee70 clone with scaled x. Args: design x,y (label origin),
   w (measured text width), h (=14). The game checks x-5,y-5,w+10,h+10
   against the grid bounds and the occupied cells; low byte of the
   result is the pass/fail flag (caller does AND 0xFF / TEST).
   OverlappingLabels=1 skips the occupancy test (labels may overlap)
   but the screen-bounds test always runs, so labels stay on screen
   in both modes. */
__attribute__((used, noinline)) static unsigned int grid_check_c(DWORD obj, int x, int y, int w, int h) {
    x -= 5;
    y -= 5;
    w += 10;
    h += 10;
    int gx = grid_sx(x);
    int gx1 = grid_sx(x + w);
    if (gx1 >= 800 || gx < 0 || y < 0 || y + h >= 600) return 0;
    if (g_cfg_overlap_labels) return 1;
    if (obj < 0x10000 || IsBadReadPtr((void*)(obj + 0x1140), 480000)) return 0;
    BYTE* base = (BYTE*)(obj + 0x1140);
    if (base[gx1 * 600 + y] == 1 || base[gx * 600 + (y + h)] == 1 ||
        base[gx1 * 600 + (y + h)] == 1) {
        return 0;
    }
    int cx, cy;
    for (cx = gx; cx < gx1; cx += 4) {
        for (cy = y; cy < y + h; cy += 4) {
            if (base[cx * 600 + cy] == 1) return 0;
        }
    }
    return 1;
}

/* FUN_0077ed6a (unbounded mark) + FUN_0077edde (bounded mark) share
   this: mark all cells of the scaled rect, clamped to the buffer. */
__attribute__((used, noinline)) static void grid_mark_c(DWORD obj, int x, int y, int w, int h, int bounded) {
    int gx = grid_sx(x);
    int gx1 = grid_sx(x + w);
    int y0 = y;
    int y1 = y + h;
    if (gx < 0) gx = 0;
    if (gx1 > 800) gx1 = 800;
    if (y0 < 0) y0 = 0;
    if (y1 > 600) y1 = 600;
    if (gx1 <= gx || y1 <= y0) return;
    if (obj < 0x10000 || IsBadReadPtr((void*)(obj + 0x1140), 480000)) return;
    BYTE* base = (BYTE*)(obj + 0x1140);
    int cx, cy;
    for (cx = gx; cx < gx1; cx++) {
        for (cy = y0; cy < y1; cy++) {
            base[cx * 600 + cy] = 1;
        }
    }
}

__attribute__((naked)) static void grid_check_stub(void) {
    __asm__ volatile (
        "pushal\n"                 /* E; [E+0]=edi [E+4]=esi [E+8]=ebp [E+12]=esp [E+16]=ebx [E+20]=edx [E+24]=ecx(obj) [E+28]=eax; ret@E+32 x@E+36 y@E+40 w@E+44 h@E+48 */
        "movl 48(%esp), %eax\n"    /* h */
        "pushl %eax\n"
        "movl 48(%esp), %eax\n"    /* w */
        "pushl %eax\n"
        "movl 48(%esp), %eax\n"    /* y */
        "pushl %eax\n"
        "movl 48(%esp), %eax\n"    /* x */
        "pushl %eax\n"
        "movl 40(%esp), %eax\n"    /* obj (saved ecx) */
        "pushl %eax\n"
        "call _grid_check_c\n"
        "addl $20, %esp\n"
        "movl %eax, 28(%esp)\n"    /* stash the result in the saved-eax slot */
        "popal\n"
        "ret $16\n"
    );
}

__attribute__((naked)) static void grid_mark_stub(void) {
    __asm__ volatile (
        "pushal\n"
        "pushl $0\n"               /* bounded = 0 (FUN_0077ed6a) */
        "movl 52(%esp), %eax\n"    /* h */
        "pushl %eax\n"
        "movl 52(%esp), %eax\n"    /* w */
        "pushl %eax\n"
        "movl 52(%esp), %eax\n"    /* y */
        "pushl %eax\n"
        "movl 52(%esp), %eax\n"    /* x */
        "pushl %eax\n"
        "movl 44(%esp), %eax\n"    /* obj (saved ecx) */
        "pushl %eax\n"
        "call _grid_mark_c\n"
        "addl $24, %esp\n"
        "movl %eax, 28(%esp)\n"    /* stash in the saved-eax slot */
        "popal\n"
        "ret $16\n"
    );
}

__attribute__((naked)) static void grid_markb_stub(void) {
    __asm__ volatile (
        "pushal\n"
        "pushl $1\n"               /* bounded = 1 (FUN_0077edde) */
        "movl 52(%esp), %eax\n"    /* h */
        "pushl %eax\n"
        "movl 52(%esp), %eax\n"    /* w */
        "pushl %eax\n"
        "movl 52(%esp), %eax\n"    /* y */
        "pushl %eax\n"
        "movl 52(%esp), %eax\n"    /* x */
        "pushl %eax\n"
        "movl 44(%esp), %eax\n"    /* obj (saved ecx) */
        "pushl %eax\n"
        "call _grid_mark_c\n"
        "addl $24, %esp\n"
        "movl %eax, 28(%esp)\n"    /* stash in the saved-eax slot */
        "popal\n"
        "ret $16\n"
    );
}

static void patch_grid_fns(void* exe) {
    static const DWORD sites[3] = { 0x77ed6a, 0x77edde, 0x77ee70 };
    static const void* stubs[3] = { &grid_mark_stub, &grid_markb_stub, &grid_check_stub };
    static int done = 0;
    for (int i = 0; i < 3; i++) {
        BYTE* p = (BYTE*)exe + (sites[i] - 0x400000);
        DWORD rel = (DWORD)((BYTE*)stubs[i] - (p + 5));
        if (p[0] == 0xE9 && *(DWORD*)(p + 1) == rel) continue;
        if (p[0] != 0x55 || p[1] != 0x8B || p[2] != 0xEC) {
            if (!done) {
                char buf[128];
                _snprintf(buf, sizeof(buf),
                    "== grid fn 0x%X entry mismatch (0x%02X 0x%02X 0x%02X) ==\n",
                    sites[i], p[0], p[1], p[2]);
                log_line(buf);
            }
            continue;
        }
        DWORD oldp;
        VirtualProtect(p, 5, PAGE_EXECUTE_READWRITE, &oldp);
        p[0] = 0xE9;
        *(DWORD*)(p + 1) = rel;
        VirtualProtect(p, 5, oldp, &oldp);
    }
    if (!done) {
        done = 1;
        log_line("== starmap label grid hooked (16:9 placement, x scaled by 4H/3W) ==\n");
    }
}

/* 4728e = indexed quads (queue text + font-atlas icons).
   Shift ONLY the text queue path (0x38670..0x398D0). Tooltips
   (FUN_00553274), map labels (FUN_00780fa1), the BL squad panel
   (FUN_0077efc8) and the settings popup rows (0x52A188/0x52A1FD with
   origin x > 500) never shift. */
__attribute__((used)) void qlog_text(DWORD caller, DWORD verts, DWORD vcount, DWORD stride) {
    if (!verts || verts < 0x10000) return;
    if (vcount < 1) return;
    if (vcount > 4096) vcount = 4096;
    if (IsBadReadPtr((void*)verts, stride * vcount)) return;
    float* v = (float*)verts;
    int fl = (int)(stride / 4);
    if (fl < 3) fl = 7;
    DWORD rva = caller - 0x400000;
    int is_queue = (rva >= 0x38670 && rva <= 0x398D0);
    if (is_queue) {
        /* our own version-suffix text batch - no shift rules may touch
           it (its x is computed from the version text's final on-screen
           right edge). */
        if (g_suffix_draw) return;
        /* keep the rowh + setrect call-site wrappers alive (the game
           rewrites code at runtime); re-assert on every text draw */
        if (g_exe) {
            patch_rowh_sites(g_exe);
            patch_setrect_sites(g_exe);
        }
        DWORD i;
        float minx = 1e30f, maxx = -1e30f;
        for (i = 0; i < vcount; i++) {
            float x = v[i * fl];
            if (x < minx) minx = x;
            if (x > maxx) maxx = x;
        }
        DWORD owner = walk_owner_ret();
        /* tooltip popup text: drawn by FUN_00553274 (0x553274..0x553c20,
           the tooltip builder). The BOX never gets the centering shift,
           so the TEXT must not either. */
        int is_tooltip = (owner >= 0x553274 && owner <= 0x553c20);
        /* v42: some dropdown popup lists are built through the tooltip
           system: their row TEXT arrives with a different direct owner
           (e.g. 0x536CDC = FUN_00536a0f) but the tooltip builder
           FUN_00553274 sits in the owner CHAIN. The game positions
           those rows at the click point in the full design space - a
           +g_shift would displace them right of their box ("randomly
           misaligned"). Detect the tooltip fn in the chain and treat
           like tooltips (never shift). */
        int chain_tip = 0;
        if (!is_tooltip) {
            DWORD chain[4];
            walk_owner_chain(chain, 4);
            for (int k = 0; k < 4; k++) {
                if (chain[k] >= 0x553274 && chain[k] <= 0x553c20) {
                    chain_tip = 1;
                    break;
                }
            }
        }
        /* map labels: owner inside FUN_00780fa1 (0x780fa1..0x787958, the
           map/screen label renderer). Planet-screen text (FUN_00536a0f)
           shifts normally. */
        int is_map = (owner >= 0x780FA1 && owner <= 0x787958);
        /* BL squad-panel text: FUN_0077efc8 (0x77EFC8..0x780C82) positions
           the whole bottom-left squad panel in the centered design space
           -> a +g_shift would double-shift it. The whole renderer range
           skips the shift (user-verified: "max" texts sit at the left
           edge of the 16:9 screen). */
        float ox = 0.0f;
        DWORD obj = 0;
        if (g_exe) {
            obj = *(DWORD*)(g_exe + 0xc65d10 - 0x400000);
            if (obj && !IsBadReadPtr((void*)obj, 0x40)) {
                ox = *(float*)(obj + 0x24);
            }
        }
        int squad_all = (owner >= 0x77EFC8 && owner <= 0x780C82);
        int hud_centered = ((owner >= 0x77EFC8 && owner <= 0x780FA1) && ox > 100.0f) || squad_all;
        /* v62: the BL panel "Max" capacity labels (owners 0x77FB2D/
           0x77FB87/0x77FBE1). The squad range skips the general shift
           so they stay at the authored left edge; the user wants them
           +10 physical px right (10*(Hf/1080) game px). */
        int is_max_label = (owner == 0x77FB2D || owner == 0x77FB87 ||
                            owner == 0x77FBE1);
        /* option-popup rows: FUN_0052a0e0 draws its rows via two
           funnel calls (rets 0x52A188/0x52A1FD) and is called ONLY by
           the popup renderer FUN_00525e9e. The popup OBJECT's x is
           now centered by the clamp hook (v45) -> the rows draw at the
           centered position already; the general +g_shift must NEVER
           apply to them. */
        int popup_rows = (owner == 0x52A188 || owner == 0x52A1FD);
        /* v55: NO special rule for the 0x536CDC planet-widget texts.
           The v50-v54 popup-bounds test was WRONG: the MP screen's own
           value texts (0x536CDC owner, scattered y) fell inside the
           open popup's rectangle and were skipped -> "misplaced label
           next to the popup". The planet production popup's rows also
           draw via 0x536CDC, but they land on the shifted box
           CORRECTLY with the general shift: the box gets +133.33 from
           the object shift and the rows get +133.33 from the general
           shift - identical amounts. The option popups' rows all draw
           via FUN_0052a0e0 (popup_rows above) and skip. */
        /* v38: floating messages draw via the planet text fn
           FUN_00536a0f (owner 0x536CDC) with origin x = 10.0 while
           flying -> keep them at the authored left edge. Per the
           user's plan the renderer is centered by DEFAULT (everything
           gets the general shift) and ONLY the messages are moved
           back left: the message origin is EXACTLY 10.0, so the
           exclusion is narrowed to ox 5..20 - any other text drawn
           by this renderer at a small origin (e.g. the Morale at
           ox~0) stays centered. */
        int msg_fly = ((owner >= 0x536a0f && owner <= 0x5374f3) &&
                       ox >= 5.0f && ox <= 20.0f);
        /* v31 TR panel: the top bar's text follows the bar to the
           max-right corner (same shift as the icons). */
        int tr_bar_text = (owner >= 0x77AE14 && owner <= 0x77D5A2);
        /* v30 REMOVED in v60: the BL ship-list text shift (owner 0x77F162)
           is superseded - the ship-panel scene view shift moves the ships
           and the names follow the projection; the old -g_shift would
           double-shift them. */
        /* v32: PAUSED / x SPEED - the top-center labels drawn by the
           squad HUD FUN_0077efc8 via "#fjob##w800##acenter#..." (the
           game centers them in the 4:3 box = design center 400). The
           squad exclusion below leaves them there -> center on 16:9. */
        if (g_shift > 1.0f && squad_all && minx >= 0.0f && maxx <= g_thresh) {
            float Wf = (float)*(DWORD*)(g_exe + 0xc5127c - 0x400000);
            float dwf = *(float*)(g_exe + 0xc0a790 - 0x400000);
            if (Wf > 0.0f && dwf > 0.0f) {
                float d = Wf / dwf;
                float desctr = (minx + maxx) * 0.5f / d;
                if (desctr >= 320.0f && desctr <= 480.0f) {
                    float mny = 1e30f;
                    DWORD i2;
                    for (i2 = 0; i2 < vcount; i2++) {
                        float y = v[i2 * fl + 1];
                        if (y < mny) mny = y;
                    }
                    if (mny < 200.0f) {
                        float dx = Wf * 0.5f - (minx + maxx) * 0.5f;
                        for (i2 = 0; i2 < vcount; i2++) {
                            v[i2 * fl] += dx;
                        }
                    }
                }
            }
        }
        if (g_shift > 1.0f && tr_bar_text && minx >= 0.0f && maxx <= g_thresh) {
            float Wf = (float)*(DWORD*)(g_exe + 0xc5127c - 0x400000);
            float sf = (float)*(DWORD*)(g_exe + 0xc51280 - 0x400000) / 600.0f;
            float rshift = Wf - 800.0f * sf;
            for (i = 0; i < vcount; i++) {
                v[i * fl] += rshift;
            }
        }
        else if (g_shift > 1.0f && is_max_label) {
            float Hf = (float)*(DWORD*)(g_exe + 0xc51280 - 0x400000);
            float mx = 10.0f * (Hf / 1080.0f);
            for (i = 0; i < vcount; i++) {
                v[i * fl] += mx;
            }
        }
        else if (g_shift > 1.0f && !is_tooltip && !chain_tip && !is_map &&
            !hud_centered && !popup_rows && !msg_fly &&
            minx >= 0.0f && maxx <= g_thresh) {
            for (i = 0; i < vcount; i++) {
                v[i * fl] += g_shift;
            }
        }
        /* v58: the portrait message window's text. While a message is
           up, the FUN_00536a0f texts inside the message window's
           authored box (design x ~520..820, y 250..570 - the body
           lines AND the "1/2" count text at y 262) are the message
           lines - anchor them to the 16:9 right edge with an EXTRA
           +g_shift (total = W - 800*scale, the same rshift as the TR
           panel) plus a small calibration offset. The flying messages
           (msg_fly) and other renderer texts (planet panel at x<520)
           stay. */
        if (g_shift > 1.0f && !msg_fly &&
            owner >= 0x536a0f && owner <= 0x5374f3) {
            if (msg_window_active()) {
                float Wf = (float)*(DWORD*)(g_exe + 0xc5127c - 0x400000);
                float Hf = (float)*(DWORD*)(g_exe + 0xc51280 - 0x400000);
                float dw = *(float*)(g_exe + 0xc0a790 - 0x400000);
                if (Wf > 0.0f && dw > 0.0f) {
                    float sy = Hf / 600.0f;
                    float dx1 = maxx / (Wf / dw);
                    float mny = 1e30f;
                    DWORD i2;
                    for (i2 = 0; i2 < vcount; i2++) {
                        float y = v[i2 * fl + 1];
                        if (y < mny) mny = y;
                    }
                    float dy0 = mny / sy;
                    if (dy0 >= 250.0f && dy0 <= 570.0f &&
                        dx1 >= 520.0f && dx1 <= 820.0f) {
                        float extra = MSG_TEXT_EXTRA * (Hf / 1080.0f);
                        for (i2 = 0; i2 < vcount; i2++) {
                            v[i2 * fl] += g_shift + extra;
                        }
                    }
                }
            }
        }
        /* capture the version text ("Version 1.09", authored design
           (646, 582)) by its POSITION, not its owner (the runtime
           funnel's frames break the owner-chain walk). The batch has
           already been shifted (+g_shift) here, so subtract it to get
           the authored x. The version text itself is never patched -
           we only measure it + draw the suffix next to it. */
        if (g_exe) {
            float Wf = (float)*(DWORD*)(g_exe + 0xc5127c - 0x400000);
            float Hf = (float)*(DWORD*)(g_exe + 0xc51280 - 0x400000);
            float dw = *(float*)(g_exe + 0xc0a790 - 0x400000);
            if (Wf > 0.0f && dw > 0.0f && Hf > 0.0f) {
                float sx = Wf / dw;
                float sy = Hf / 600.0f;
                float mnx = 1e30f, mxx = -1e30f, mny = 1e30f, mxy = -1e30f;
                DWORD i3;
                for (i3 = 0; i3 < vcount; i3++) {
                    float x = v[i3 * fl];
                    float y = v[i3 * fl + 1];
                    if (x < mnx) mnx = x;
                    if (x > mxx) mxx = x;
                    if (y < mny) mny = y;
                    if (y > mxy) mxy = y;
                }
                float dx0 = (mnx - g_shift) / sx;
                float dy0 = mny / sy;
                if (owner == 0x3D4870 ||
                    (dx0 >= 630.0f && dx0 <= 665.0f &&
                     dy0 >= 565.0f && dy0 <= 595.0f &&
                     (mxy - mny) < 60.0f * (Hf / 1080.0f) &&
                     vcount >= 24 && vcount <= 160)) {
                    g_ver_maxx = mxx;
                    g_ver_seen = 1;
                    ver_suffix_glyphs(v, vcount, fl, mny, mxy);
                }
            }
        }
        /* 2x font: metrics are fixed at the source (font_metrics_scan) so the
           quads arriving here are already 1x. v23's per-draw x/y x0.5
           compensation is REMOVED. */
        font_metrics_scan();
    }
    else {
        /* v42/v50: shift the planet-widget vertical bars (3-quad IM
           draws via 0xEBDBB, y 400..1200 - the morale bar + building
           slots) ONLY when the call chain goes through the planet
           screen fn FUN_0074d20c (0x74D20C..0x750E80). The world
           asteroid-cloud sprite shares the draw signature but is
           rendered from the game view - the chain check keeps it
           untouched (user-verified absent in v38). */
        if (rva == 0xEBDBB && vcount == 12) {
            float mnx = 1e30f, mxx = -1e30f, mny = 1e30f, mxy = -1e30f;
            DWORD i;
            for (i = 0; i < vcount; i++) {
                float x = v[i * fl];
                float y = v[i * fl + 1];
                if (x < mnx) mnx = x;
                if (x > mxx) mxx = x;
                if (y < mny) mny = y;
                if (y > mxy) mxy = y;
            }
            if (g_shift > 1.0f && mnx >= 0.0f && mxx <= g_thresh &&
                mny > 400.0f && mxy < 1200.0f) {
                DWORD chain[4];
                walk_owner_chain(chain, 4);
                int k, planet = 0;
                for (k = 0; k < 4; k++) {
                    if (chain[k] >= 0x74D20C && chain[k] <= 0x750E80) {
                        planet = 1;
                        break;
                    }
                }
                if (planet) {
                    for (i = 0; i < vcount; i++) {
                        v[i * fl] += g_shift;
                    }
                }
            }
        }
        /* v34: technology tree node icons + name labels draw via 4728e
           from call site 0x3864F (just outside the queue range) with
           the TechTreeScreen methods as owners - shift by g_shift. */
        {
            DWORD ttown = walk_owner_ret();
            if (g_shift > 1.0f && ttown >= 0x7d527c && ttown <= 0x7df406) {
                DWORD i2;
                for (i2 = 0; i2 < vcount; i2++) {
                    v[i2 * fl] += g_shift;
                }
            }
        }
        /* v56: the EditField text-entry CARET. The caret is drawn as a
           "|" glyph via FUN_00437bb4 (a non-queue text renderer - its
           4728e caller is outside the queue range) from the EditField
           draw fn FUN_005074c9 (0x5074C9..0x507CF7, the MP chat box +
           player name field). The general shift never sees it -> the
           caret stays in the left-aligned 4:3 box while the field's
           text (queue path) is centered. Shift it by owner + the
           single-glyph batch signature. */
        if (g_shift > 1.0f && vcount >= 4 && vcount <= 16 && stride == 0x1c) {
            DWORD cowner = walk_owner_ret();
            if (cowner >= 0x5074C9 && cowner <= 0x507CF7) {
                float mnx = 1e30f, mxx = -1e30f;
                DWORD i2;
                for (i2 = 0; i2 < vcount; i2++) {
                    float x = v[i2 * fl];
                    if (x < mnx) mnx = x;
                    if (x > mxx) mxx = x;
                }
                if (mnx >= 0.0f && mxx <= g_thresh) {
                    for (i2 = 0; i2 < vcount; i2++) {
                        v[i2 * fl] += g_shift;
                    }
                }
            }
        }
        /* icon/sprite path (FUN_0044efac/ecd6 = 0x4ECD6..0x4F22A):
           shift icons drawn by the UI panels, never world/map icons.
           Whitelist: FUN_0077ae14 (0x77AE14..0x77D5A2, empire-info top
           bar), FUN_0074d20c (planet-screen icons, 0x74D20C..0x750E80),
           FUN_005311b4 (scrollbar slider, 0x5311B4..0x5317D5).
           World/map icons (FUN_00780fa1) and the cursor stay; the map
           icons get SCALED (see above) but never shifted. */
        if (rva >= 0x4ECD6 && rva <= 0x4F22A) {
            DWORD iowner = walk_owner_ret();
            /* v24.43: center the "LOADING"/"SAVE"/"PROCESS" label images
               (load.tga/process.tga/save.tga, 296x24) drawn at (252, y) in
               the 800-space by FUN_00717243 (init, c==0: call 0x717AA2) and
               FUN_00717BA5 (frame: 0x717FF8 Start variant, 0x71842B c==2,
               0x7184BF c==1, 0x718566 c==0 popup) -> shift by g_shift */
            if (g_shift > 1.0f && g_saved_ebp > 0x10000 &&
                !IsBadReadPtr((void*)(g_saved_ebp + 0x10), 8)) {
                DWORD lret = *(DWORD*)(g_saved_ebp + 4) - 0x400000;
                if (lret == 0x317AA7 || lret == 0x317FFD ||
                    lret == 0x318430 || lret == 0x3184C4 || lret == 0x31856B) {
                    DWORD i;
                    float mnx = 1e30f, mxx = -1e30f;
                    for (i = 0; i < vcount; i++) {
                        float x = v[i * fl];
                        if (x < mnx) mnx = x;
                        if (x > mxx) mxx = x;
                    }
                    if (mnx >= 0.0f && mxx <= g_thresh) {
                        for (i = 0; i < vcount; i++) {
                            v[i * fl] += g_shift;
                        }
                    }
                }
            }
            /* v24.54: sound-settings slider THUMBS = ecd6 draws from the
               thumb renderer FUN_0050dcxx (ret 0x50DCF2, ~24 design wide).
               The grooves (0x10F58D) already get the popup shift; the
               thumbs lag 240px behind -> shift them too. */
            if (g_shift > 1.0f && g_saved_ebp > 0x10000 &&
                !IsBadReadPtr((void*)(g_saved_ebp + 4), 4) &&
                *(DWORD*)(g_saved_ebp + 4) == 0x50DCF2) {
                DWORD i;
                for (i = 0; i < vcount; i++) {
                    v[i * fl] += g_shift;
                }
            }
            /* v24.45: yes/no popup widgets (box + buttons) draw via the
               generic widget-icon path: FUN_0050f465 -> ecd6 (ret
               0x50F58D). FUN_0050f465 only fires for popup-style
               widgets (observed: the quit dialog box + Yes/No buttons;
               regular menu/HUD icons use other renderers), and they are
               authored in the 800-space -> shift left-box draws. */
            if (g_shift > 1.0f && rva == 0x4EF9E && g_saved_ebp > 0x10000 &&
                !IsBadReadPtr((void*)(g_saved_ebp + 4), 4) &&
                *(DWORD*)(g_saved_ebp + 4) == 0x50F58D) {
                DWORD i;
                float mnx = 1e30f, mxx = -1e30f;
                for (i = 0; i < vcount; i++) {
                    float x = v[i * fl];
                    if (x < mnx) mnx = x;
                    if (x > mxx) mxx = x;
                }
                if (mnx >= 0.0f && mxx <= g_thresh) {
                    for (i = 0; i < vcount; i++) {
                        v[i * fl] += g_shift;
                    }
                }
            }
            /* v30 REMOVED in v60: the BL "in production" icon shift
               (ecd6 ret 0x77F3CC) is superseded - it follows the ship
               scene projection like the names; the old -g_shift would
               double-shift it. */
            /* icon size scale: cursor sprite (ecd6 call site 0x43520A,
               ret 0x43520F), the scrollbar slider (FUN_005311b4, ecd6
               call site 0x531378, ret 0x53137D) and the MAP icons
               (FUN_00780fa1, ecd6 call sites 0x780B1F/0x782DF4, rets
               0x780B24/0x782DF9). g_saved_ebp = the caller's (ecd6's)
               ebp (the tracer E9-jumps in before 4728e's prologue runs),
               so [g_saved_ebp+8/+0xC] = ecd6's first two stack args =
               the icon origin (design coords). Exact call-site checks
               only - no frame walking (a broad owner range caught
               unrelated draws, e.g. the scrollbar slider). */
            if (rva == 0x4EF9E && g_saved_ebp > 0x10000 &&
                !IsBadReadPtr((void*)(g_saved_ebp + 0x10), 8)) {
                DWORD eret = *(DWORD*)(g_saved_ebp + 4);
                if (eret == 0x43520F || eret == 0x53137D ||
                    eret == 0x780B24 || eret == 0x782DF9) {
                    if (eret == 0x43520F) {
                        /* cursor: origin-anchored (tip stays at mouse) */
                        scale_icon_quads(g_saved_ebp, v, vcount, fl, g_cfg_cursor_scale);
                    }
                    else if (eret == 0x780B24 || eret == 0x782DF9) {
                        /* map icons: whole-icon centered scale. The atlas
                           (ecd6's ECX) = [g_saved_ebp-0x54]; multi-cell
                           icons scale about origin + half the atlas grid
                           so they shrink as one piece. */
                        if (atlas_dims_from_ebp(g_saved_ebp)) {
                            float dw = *(float*)(g_exe + 0xc0a790 - 0x400000);
                            float sx = (float)*(DWORD*)(g_exe + 0xc5127c - 0x400000) / dw;
                            float sy = (float)*(DWORD*)(g_exe + 0xc51280 - 0x400000) / 600.0f;
                            float cx = *(float*)(g_saved_ebp + 8) * sx + g_icon_w * 0.5f;
                            float cy = *(float*)(g_saved_ebp + 12) * sy + g_icon_h * 0.5f;
                            scale_icon_at(v, vcount, fl, cx, cy, g_cfg_icon_scale);
                        }
                        else {
                            scale_icon_center(v, vcount, fl, g_cfg_icon_scale);
                        }
                    }
                    else {
                        /* slider: per-call center (verified fine) */
                        scale_icon_center(v, vcount, fl, g_cfg_icon_scale);
                    }
                }
            }
            /* v31 TR panel: the empire top bar (FUN_0077ae14,
               owners 0x77AE14..0x77D5A2) anchors its right edge
               (authored 800 design = the 4:3 bar) to the screen's
               right edge - the whole bar keeps its width and lands
               at the max-right 16:9 corner. */
            if (g_shift > 1.0f && iowner >= 0x77AE14 && iowner <= 0x77D5A2) {
                float Wf = (float)*(DWORD*)(g_exe + 0xc5127c - 0x400000);
                float sf = (float)*(DWORD*)(g_exe + 0xc51280 - 0x400000) / 600.0f;
                float rshift = Wf - 800.0f * sf;
                DWORD i;
                for (i = 0; i < vcount; i++) {
                    v[i * fl] += rshift;
                }
            }
            int do_icon = ((iowner >= 0x74D20C && iowner <= 0x750E80) ||
                           (iowner >= 0x5311B4 && iowner <= 0x5317D5) ||
                           /* v42: MP setup screen (FUN_007ce33c +
                              FUN_007cec54, 0x7ce33c..0x7cef09): empire/team
                              icons of the player rows, authored in the
                              800-space */
                           (iowner >= 0x7ce33c && iowner <= 0x7cef09) ||
                           /* v42: MP map-settings row (FUN_007c3968,
                              0x7c3968..0x7c4d7a, called from FUN_007b957a):
                              the 3 setting icons at design x 535/600/675 */
                           (iowner >= 0x7c3968 && iowner <= 0x7c4d7a));
            if (do_icon && g_shift > 1.0f) {
                DWORD i;
                float minx = 1e30f, maxx = -1e30f;
                for (i = 0; i < vcount; i++) {
                    float x = v[i * fl];
                    if (x < minx) minx = x;
                    if (x > maxx) maxx = x;
                }
                if (minx >= 0.0f && maxx <= g_thresh) {
                    for (i = 0; i < vcount; i++) {
                        v[i * fl] += g_shift;
                    }
                }
            }
        }
        else if (rva == 0x4F2D4 && g_saved_ebp > 0x10000) {
            /* efac path (4728e call 0x44F2CF): the scrollbar slider's
               alt path (44f648 direct, ret 0x53140F).
               g_saved_ebp = efac's ebp; [g_saved_ebp] = 44f648's ebp. */
            DWORD cb = *(DWORD*)g_saved_ebp;
            if (cb > 0x10000 && !IsBadReadPtr((void*)(g_saved_ebp + 0x10), 8) &&
                !IsBadReadPtr((void*)(cb + 4), 4) && *(DWORD*)(cb + 4) == 0x53140F) {
                scale_icon_center(v, vcount, fl, g_cfg_icon_scale);
                if (g_shift > 1.0f) {
                    DWORD i2;
                    float minx = 1e30f, maxx = -1e30f;
                    for (i2 = 0; i2 < vcount; i2++) {
                        float x = v[i2 * fl];
                        if (x < minx) minx = x;
                        if (x > maxx) maxx = x;
                    }
                    if (minx >= 0.0f && maxx <= g_thresh) {
                        for (i2 = 0; i2 < vcount; i2++) {
                            v[i2 * fl] += g_shift;
                        }
                    }
                }
            }
            /* the FUN_0044f648 variant of the popup widget draw (ret into
               FUN_0050f465 = 0x50F5E7): same centering as the ecd6 path */
            else if (g_shift > 1.0f && cb > 0x10000 &&
                     !IsBadReadPtr((void*)(g_saved_ebp + 0x10), 8) &&
                     !IsBadReadPtr((void*)(cb + 4), 4) && *(DWORD*)(cb + 4) == 0x50F5E7) {
                DWORD i2;
                float minx = 1e30f, maxx = -1e30f;
                for (i2 = 0; i2 < vcount; i2++) {
                    float x = v[i2 * fl];
                    if (x < minx) minx = x;
                    if (x > maxx) maxx = x;
                }
                if (minx >= 0.0f && maxx <= g_thresh) {
                    for (i2 = 0; i2 < vcount; i2++) {
                        v[i2 * fl] += g_shift;
                    }
                }
            }
        }
    }
}

__attribute__((naked)) static void quad_trace_471f6(void) {
    __asm__ volatile (
        "movl %ebp, _g_saved_ebp2\n"
        "pushal\n"
        "movl 32(%esp), %ecx\n"
        "movl 44(%esp), %eax\n"
        "movl 40(%esp), %edx\n"
        "movl 48(%esp), %ebx\n"
        "shll $1, %edx\n"
        "pushl %ebx\n"
        "pushl %edx\n"
        "pushl %eax\n"
        "pushl %ecx\n"
        "call _qlog_quad\n"
        "addl $16, %esp\n"
        "popal\n"
        "pushl %ebp\n"
        "movl %esp, %ebp\n"
        "subl $0xC, %esp\n"
        "jmp *_g_471f6_resume\n"
    );
}

__attribute__((naked)) static void quad_trace_4728e(void) {
    __asm__ volatile (
        "movl %ebp, _g_saved_ebp\n"
        "pushal\n"
        "movl 36(%esp), %eax\n"
        "movl %eax, _g_4728e_arg0\n"
        "movl 40(%esp), %eax\n"
        "movl %eax, _g_4728e_arg1\n"
        "movl 44(%esp), %eax\n"
        "movl %eax, _g_4728e_arg2\n"
        "movl 48(%esp), %eax\n"
        "movl %eax, _g_4728e_arg3\n"
        "movl 52(%esp), %eax\n"
        "movl %eax, _g_4728e_arg4\n"
        "movl 56(%esp), %eax\n"
        "movl %eax, _g_4728e_arg5\n"
        "movl 60(%esp), %eax\n"
        "movl %eax, _g_4728e_arg6\n"
        "movl 64(%esp), %eax\n"
        "movl %eax, _g_4728e_arg7\n"
        "movl 32(%esp), %ecx\n"
        "movl 60(%esp), %eax\n"
        "movl 64(%esp), %ebx\n"
        "movl 48(%esp), %edx\n"
        "shll $1, %edx\n"
        "pushl %ebx\n"
        "pushl %edx\n"
        "pushl %eax\n"
        "pushl %ecx\n"
        "call _qlog_text\n"
        "addl $16, %esp\n"
        "popal\n"
        "pushl %ebp\n"
        "movl %esp, %ebp\n"
        "subl $0xC, %esp\n"
        "jmp *_g_4728e_resume\n"
    );
}

static void patch_quad_traces(void* exe) {
    BYTE* p1 = (BYTE*)exe + (0x4471f6 - 0x400000);
    BYTE* p2 = (BYTE*)exe + (0x44728e - 0x400000);
    if (p1[0] != 0xE9) {
        g_471f6_resume = 0x4471f6 + 6;
        DWORD oldp;
        VirtualProtect(p1, 5, PAGE_EXECUTE_READWRITE, &oldp);
        p1[0] = 0xE9;
        *(DWORD*)(p1 + 1) = (DWORD)((BYTE*)&quad_trace_471f6 - (p1 + 5));
        VirtualProtect(p1, 5, oldp, &oldp);
    }
    if (p2[0] != 0xE9) {
        g_4728e_resume = 0x44728e + 6;
        DWORD oldp;
        VirtualProtect(p2, 5, PAGE_EXECUTE_READWRITE, &oldp);
        p2[0] = 0xE9;
        *(DWORD*)(p2 + 1) = (DWORD)((BYTE*)&quad_trace_4728e - (p2 + 5));
        VirtualProtect(p2, 5, oldp, &oldp);
    }
    static int once = 0;
    if (!once) {
        once = 1;
        log_line("== quad draw tracers installed (0x4471F6 + 0x44728E) ==\n");
    }
}

/* ---- sound slider click-zone shift (v24.55) ----
   FUN_0050e183 (slider widget event handler), event 5 (click/track):
   index = (mouse_x - xmin - w/2) / ((xmax-xmin-w)/(count-1)) with the
   mouse x as an INT at [ebp+0x14]. The visuals are shifted +240px by
   our draw hooks; the zone must follow -> subtract g_slide_shift_int
   (=133 design) from the mouse x. Patch replaces:
      50E3CE: fildl [ebp+0x14]   (DB 45 14)
      50E3D1: fsubs [ebp-0x18]   (D8 65 E8)
   with a jump to a stub that emulates both. */
__attribute__((used)) static DWORD g_slide_resume = 0;

__attribute__((naked)) static void slide_click_stub(void) {
    __asm__ volatile (
        "pushal\n"
        "movl 20(%ebp), %eax\n"
        "subl _g_slide_shift_int, %eax\n"
        "pushl %eax\n"
        "fildl (%esp)\n"
        "addl $4, %esp\n"
        "fsubs -24(%ebp)\n"
        "popal\n"
        "jmp *_g_slide_resume\n"
    );
}

static void patch_slide_click(void* exe) {
    BYTE* p = (BYTE*)exe + (0x50E3CE - 0x400000);
    if (p[0] == 0xE9) return;
    if (p[0] != 0xDB || p[1] != 0x45 || p[2] != 0x14 ||
        p[3] != 0xD8 || p[4] != 0x65 || p[5] != 0xE8) {
        return;
    }
    g_slide_resume = 0x50E3D4;
    DWORD rel = (DWORD)((BYTE*)&slide_click_stub - (p + 5));
    DWORD oldp;
    VirtualProtect(p, 6, PAGE_EXECUTE_READWRITE, &oldp);
    p[0] = 0xE9;
    *(DWORD*)(p + 1) = rel;
    p[5] = 0x90;
    VirtualProtect(p, 6, oldp, &oldp);
    log_line("== slider click-zone patch installed (0x50E3CE) ==\n");
}



__attribute__((used)) static void* g_orig_pick = (void*)0x00551ca5;
__attribute__((used)) static void* g_orig_box = (void*)0x006cc5d5;

/* v67: the message window's Accept/Decline clickable areas. The
   message screen's own strip = [[msg+8]+0x38] (the picker's ECX for
   the message's hover/click checks). Its Ok row is a single span
   (546..789 -> pick_span_kind 3, right-anchored, works), but the
   Accept/Decline row is MULTI-SPAN (543..637 + 695..717) -> kind 0
   (x -= g_pick_shift, the centered mapping) -> the clicks landed at
   the un-shifted authored positions while the visuals are right-
   anchored (user: clicking the Accept visual triggered Decline).
   The strip identity overrides the kind: every row of the message
   strip gets the right-anchored mapping (rawx >= 816.67 ->
   x = rawx - 266.67, else dead - the old left place is rejected,
   same as the kind-3 Ok rule).
   LESSONS: the message screen's handlers + hit-test code live in the
   game's runtime-protected region (base 0x722000, 4MB alloc - the
   game decrypts/re-encrypts it around calls): entry-patching
   0x723378/0x7234a1/0x722dd8 crashed at the resume (0x722DE3, AV).
   The widget AREA objects ([widget+0x48]: +4=w +8=h +0xC=x +0x10=y,
   vtable 0xB31CC0, e.g. Accept 543/271 94x24) are NOT the hit-test
   source - shifting their x changed nothing. The strip pick is the
   hit-test after all. */
__attribute__((used)) static int g_msg_strip_logged = 0;

__attribute__((used, noinline)) static DWORD msg_strip_get(void) {
    if (!g_exe) return 0;
    DWORD msg = *(DWORD*)(g_exe + 0xc6818c - 0x400000);
    if (!msg || msg < 0x10000 || IsBadReadPtr((void*)(msg + 0x10), 4)) return 0;
    DWORD cfg = *(DWORD*)(msg + 8);
    if (!cfg || cfg < 0x10000 || IsBadReadPtr((void*)(cfg + 0x3c), 4)) return 0;
    DWORD s = *(DWORD*)(cfg + 0x38);
    if (!s || s < 0x10000 || IsBadReadPtr((void*)(s + 0x3c), 4)) return 0;
    return s;
}

static void patch_action_hooks(void* exe) {
    /* REMOVED (v66 msg work): the accept/decline/ok handlers
       (0x723378/0x7234a1/0x722dd8) live in the game's runtime-protected
       code region (base 0x722000, 4MB) - entry-patching them crashed
       the game at the resume address (0x722DE3, AV) because the game
       decrypts/re-encrypts that region around each call. The hit-test
       must be found elsewhere (data patches / call sites outside the
       region). */
    (void)exe;
}

/* ===== v63 clickable-area rules (FUN_00551ca5 strip picker, both
   pick sites 0x54F035/0x53572B wrapped) =====
   The v24.81 whole-line expansion is REMOVED (the row-span clamp +
   the 0..799.5 box clamp made the whole y line clickable). The pick
   x is now chosen by the strip row's SPAN SIGNATURE (pick_span_kind):
   - kind 0 (menu strips, e.g. 49..777): x = rawx - g_pick_shift
     (the v9 centering shift - menus draw centered).
   - kind 1 (single full-width span 0..800 = the starmap HUD rows
     incl. the TL/BL corner panels): x = rawx * g_world_scale
     (800/designW = 0.75 @16:9; the v41 full-16:9 scale; the TL/BL
     rows are full-width so their left clicks hit either way).
   - kind 2 (multi-span rows >= 3 spans ending at ~800):
       y < 92.3 (TR top bar): rawx >= 788.9 -> x = rawx - 266.67
         clamped <= 799.5 (the right-anchor mapping, user-calibrated
         zone x 1420..1920 y 0..166); below it the world scale.
       y >= 563.33 && rawx >= 955.56 (BR screenselect): the
         user-calibrated zone x 1720..1920 y 1014..1080 mapped into
         the panel's span via x = rawx - 218.05 (the v62 draw shift)
         clamped to 678.5..799.5; the old place now falls through to
         the world scale (the map objects there stay selectable).
       else: x = rawx * g_world_scale (world objects, full 16:9).
   User-verified 2026-10-17: TL + BL + TR + BR corner panels, the
   main menu, and the starmap world selection all correct. */


/* full-width strips (span 0..800, the starmap HUD rows) are authored
   edge-to-edge: the -shift would kill their left 240px (where the
   corner panels visually live). Such rows get NO shift; the centered
   menu strips (e.g. 49..777) keep the normal -shift.
   BR rows (screenselect) carry a 3-span signature (0-X,Y-678,Z-800):
   the panel was moved +218.05 right at draw time (v62) - the pick
   accepts only the visual region (rawx >= 800), the old place
   (rawx 603..800) is rejected. */
__attribute__((used, noinline)) static int pick_span_kind(DWORD this_, float y) {
    if (!this_ || !g_exe || IsBadReadPtr((void*)this_, 0x40)) return 0;
    float div = *(float*)(g_exe + 0xb2e418 - 0x400000);
    if (div < 0.01f || div > 100000.0f) return 0;
    int count = *(int*)(this_ + 0x34);
    int row = (int)(y / div);
    if (row < 1 || row >= count) return 0;
    DWORD rowBase = *(DWORD*)(this_ + 0x30);
    if (!rowBase || IsBadReadPtr((void*)(rowBase + (DWORD)row * 0x18), 0x18)) return 0;
    DWORD rowptr = rowBase + (DWORD)row * 0x18;
    DWORD listBase = *(DWORD*)(rowptr + 8);
    unsigned short idx = *(unsigned short*)(rowptr + 0);
    DWORD mid = *(DWORD*)(this_ + 0x38);
    DWORD spanT = 0;
    if (mid && !IsBadReadPtr((void*)(mid + 4), 4)) spanT = *(DWORD*)(mid + 4);
    int steps = 0;
    int nspan = 0;
    int fmin = -1, fmax = -1;
    while (idx != 0xffff && steps < 64) {
        if (!listBase || IsBadReadPtr((void*)(listBase + (DWORD)idx * 4), 4)) return 0;
        unsigned int entry = *(unsigned int*)(listBase + (DWORD)idx * 4);
        unsigned short spanIdx = (unsigned short)(entry & 0xffff);
        unsigned short next = (unsigned short)(entry >> 16);
        if (spanIdx == 0xffff || !spanT || IsBadReadPtr((void*)(spanT + (DWORD)spanIdx * 8), 8)) return 0;
        if (fmin < 0) fmin = *(unsigned short*)(spanT + (DWORD)spanIdx * 8);
        fmax = *(unsigned short*)(spanT + (DWORD)spanIdx * 8 + 2);
        nspan++;
        idx = next;
        steps++;
    }
    if (nspan >= 3 && fmax >= 790) return 2;   /* BR row */
    if (nspan >= 1 && fmin == 0 && fmax >= 790) return 1;  /* full-width row (TL/BL) */
    if (nspan == 1 && fmin >= 500 && fmax >= 780) return 3;  /* message-window row (right-authored) */
    return 0;
}

__attribute__((used)) static float pick_expand_a(DWORD this_, DWORD cebp, float y, float rawx) {
    (void)cebp;
    int kind = pick_span_kind(this_, y);
    float x;
    /* v67: the message screen's strip ([[msg+8]+0x38]) is right-anchored
       as a whole - its Accept/Decline row is multi-span (kind 0 would
       map it centered) while its Ok row is the kind-3 single span.
       The strip identity wins for every row. */
    if (this_ && this_ == msg_strip_get()) {
        if (!g_msg_strip_logged) {
            g_msg_strip_logged = 1;
            char pm[96];
            _snprintf(pm, sizeof(pm),
                "== message strip 0x%X pick override active ==\n", this_);
            log_line(pm);
        }
        x = (rawx >= 816.67f) ? rawx - 266.67f : 2000.0f;
        return x;
    }
    if (kind == 2) {
        if (y < 92.3f) { /* TR top bar rows: desired zone 788.9..1066.7 maps right-anchored */
            if (rawx >= 788.9f) {
                x = rawx - 266.67f;
                if (x > 799.5f) x = 799.5f;
            } else {
                x = rawx * g_world_scale;  /* left of the TR: world objects stay alive */
            }
        } else if (y >= 563.33f && rawx >= 955.56f) { /* BR: desired zone x 1720..1920 y 1014..1080 */
            x = rawx - 218.05f;
            if (x > 799.5f) x = 799.5f;
            if (x < 678.5f) x = 678.5f;
        } else {
            x = rawx * g_world_scale;  /* world objects: full 16:9 scale */
        }
    } else if (kind == 1) {
        x = rawx * g_world_scale;
    } else if (kind == 3) {
        /* portrait message window buttons (spans 546..789): the v58
           visuals are right-anchored (+240px) -> accept only the
           visual region and map right-anchored */
        x = (rawx >= 816.67f) ? rawx - 266.67f : 2000.0f;
    } else {
        x = rawx - g_pick_shift;
    }
    return x;
}

__attribute__((used)) static float pick_expand_b(DWORD this_, DWORD cebp, float y, float rawx) {
    (void)cebp;
    int kind = pick_span_kind(this_, y);
    float x;
    /* v67: same message-strip override as pick_expand_a */
    if (this_ && this_ == msg_strip_get()) {
        x = (rawx >= 816.67f) ? rawx - 266.67f : 2000.0f;
        return x;
    }
    if (kind == 2) {
        if (y < 92.3f) {
            if (rawx >= 788.9f) {
                x = rawx - 266.67f;
                if (x > 799.5f) x = 799.5f;
            } else {
                x = rawx * g_world_scale;
            }
        } else if (y >= 563.33f && rawx >= 955.56f) {
            x = rawx - 218.05f;
            if (x > 799.5f) x = 799.5f;
            if (x < 678.5f) x = 678.5f;
        } else {
            x = rawx * g_world_scale;
        }
    } else if (kind == 1) {
        x = rawx * g_world_scale;
    } else if (kind == 3) {
        x = (rawx >= 816.67f) ? rawx - 266.67f : 2000.0f;
    } else {
        x = rawx - g_pick_shift;
    }
    return x;
}



__attribute__((naked)) static void wrap_pick_a(void) {
    __asm__ volatile (
        "pushl %ecx\n"                 /* [S-4]=this */
        "pushl %ebp\n"                 /* [S-8]=caller ebp (owner = [ebp+4]) */
        "flds 12(%esp)\n"              /* st0 = x = [S+4] */
        "subl $8, %esp\n"              /* esp=S-16 */
        "fstps 0(%esp)\n"              /* [S-16] = rawx */
        "flds 24(%esp)\n"              /* st0 = y = [S+8] */
        "fstps 4(%esp)\n"              /* [S-12] = y */
        "pushl 0(%esp)\n"              /* push rawx ; S-20 */
        "pushl 8(%esp)\n"              /* push y ; S-24 */
        "pushl 16(%esp)\n"             /* push ebp ; S-28 */
        "pushl 24(%esp)\n"             /* push this ; S-32 */
        "call _pick_expand_a\n"        /* (this, ebp, y, rawx) -> st0 */
        "addl $16, %esp\n"             /* esp=S-16 */
        "fstps 20(%esp)\n"             /* [S+4] (orig x slot) = x' */
        "movl 12(%esp), %ecx\n"        /* ecx = this = [S-4] */
        "pushl 12(%esp)\n"             /* push this ; S-20 */
        "pushl 28(%esp)\n"             /* push y = [S+8] ; S-24 */
        "pushl 28(%esp)\n"             /* push x' = [S+4] ; S-28 */
        "call *_g_orig_pick\n"         /* FUN_00551ca5(ecx, x', y), ret 8 -> esp=S-20 */
        "addl $12, %esp\n"             /* esp=S-8 */
        "popl %ebp\n"                  /* esp=S-4 */
        "popl %ecx\n"                  /* esp=S */
        "ret $8\n"
    );
}

__attribute__((naked)) static void wrap_pick_b(void) {
    __asm__ volatile (
        "pushl %ecx\n"                 /* [S-4]=this */
        "pushl %ebp\n"                 /* [S-8]=caller ebp */
        "flds 12(%esp)\n"              /* st0 = x = [S+4] */
        "subl $8, %esp\n"              /* esp=S-16 */
        "fstps 0(%esp)\n"              /* [S-16] = rawx */
        "flds 24(%esp)\n"              /* st0 = y = [S+8] */
        "fstps 4(%esp)\n"              /* [S-12] = y */
        "pushl 0(%esp)\n"              /* push rawx ; S-20 */
        "pushl 8(%esp)\n"              /* push y ; S-24 */
        "pushl 16(%esp)\n"             /* push ebp ; S-28 */
        "pushl 24(%esp)\n"             /* push this ; S-32 */
        "call _pick_expand_b\n"        /* (this, ebp, y, rawx) -> st0 */
        "addl $16, %esp\n"             /* esp=S-16 */
        "fstps 20(%esp)\n"             /* [S+4] (orig x slot) = x' */
        "movl 12(%esp), %ecx\n"        /* ecx = this = [S-4] */
        "pushl 12(%esp)\n"             /* push this ; S-20 */
        "pushl 28(%esp)\n"             /* push y = [S+8] ; S-24 */
        "pushl 28(%esp)\n"             /* push x' = [S+4] ; S-28 */
        "call *_g_orig_pick\n"         /* FUN_00551ca5(ecx, x', y), ret 8 -> esp=S-20 */
        "addl $12, %esp\n"             /* esp=S-8 */
        "popl %ebp\n"                  /* esp=S-4 */
        "popl %ecx\n"                  /* esp=S */
        "ret $8\n"
    );
}

__attribute__((naked)) static void wrap_box(void) {
    __asm__ volatile (
        "pushl %ecx\n"
        "call *_g_orig_box\n"
        "addl $4, %esp\n"
        "ret\n"
    );
}

/* ============ v65: crisp 3D-world mouse rotation ============
   The in-game mouse-camera handler FUN_0078fb7e accumulates the drag
   distance into the camera pitch/rotate TARGETS every frame and the
   camera animator FUN_0065b0e2 eases the current angles toward the
   targets - the easing is the vanilla "too smooth" (floaty/gliding)
   feel. We wrap ONLY the handler's two setter call sites (other setter
   callers = cutscenes/UI, untouched) and multiply the setter's SPEED
   argument by RotationBoost (the animator approaches RotationBoost times faster).
   Target accumulation, clamping and flags stay vanilla.
   Sensitivity (RotationSens) is applied separately at the handler entry -
   see patch_rotation_sens_entry below. */
__attribute__((used)) static void* g_rotation_pitch_real = (void*)0x0065e00c;
__attribute__((used)) static void* g_rotation_rotate_real = (void*)0x0065de73;

/* Entered via E8 (ret pushed): [esp]=ret, [esp+4]=target, [esp+8]=speed,
   ECX = camera object. Multiply the speed in place, then run the real
   setter so its `ret 8` returns straight to the original caller. */
__attribute__((naked)) static void wrap_rotation_pitch(void) {
    __asm__ volatile (
        "pushl %ecx\n"                 /* [S-4]=cam */
        "flds 12(%esp)\n"              /* st0 = speed [S+8] */
        "fmuls _g_rotation_boost\n"          /* st0 = speed * boost */
        "fstps 12(%esp)\n"             /* [S+8] = boosted speed */
        "popl %ecx\n"                  /* esp=S, ecx=cam */
        "jmp *_g_rotation_pitch_real\n"     /* real setter, ret 8 -> caller */
    );
}

__attribute__((naked)) static void wrap_rotation_rotate(void) {
    __asm__ volatile (
        "pushl %ecx\n"                 /* [S-4]=cam */
        "flds 12(%esp)\n"              /* st0 = speed [S+8] */
        "fmuls _g_rotation_boost\n"          /* st0 = speed * boost */
        "fstps 12(%esp)\n"             /* [S+8] = boosted speed */
        "popl %ecx\n"                  /* esp=S, ecx=cam */
        "jmp *_g_rotation_rotate_real\n"    /* real setter, ret 8 -> caller */
    );
}

/* ---- sensitivity: scale the driver reads at the handler ENTRY ----
   The entry (0x78FB7E, prologue 55 8B EC 81 EC 8C 00 00 00) is
   runtime-verified clean (the handler's interior is NOT safe to patch).
   At entry ECX = the battle object: scale the mouse driver's position
   toward the battle's stored anchor (+0x90/+0x94) with rounding, so
   the handler's own dx/dy computation (driver - anchor) sees
   RotationSens-scaled deltas without a directional bias. Zoom mode
   (battle+0x7647c != 0) and pre-first-drag states are skipped (the
   rotation setters zero the pitch target speed +0xdc at camera init -
   until one has run, the game is not in mouse-rotation mode). */
__attribute__((used)) static void* g_rotation_entry_res = (void*)0x0078fb87;

__attribute__((naked)) static void stub_rotation_entry(void) {
    __asm__ volatile (
        "pushal\n"
        "pushl %ecx\n"
        "call _rotation_sens_entry\n"
        "addl $4, %esp\n"
        "popal\n"
        "pushl %ebp\n"                 /* recreate the replaced prologue */
        "movl %esp, %ebp\n"
        "subl $0x8C, %esp\n"
        "jmp *_g_rotation_entry_res\n"
    );
}

static __attribute__((used, noinline)) void rotation_sens_entry(DWORD battle) {
    if (!g_rotation_enabled) return;
    if (g_rotation_sens > 0.999f && g_rotation_sens < 1.001f) return;
    if (IsBadReadPtr((void*)(battle + 0x7647c), 1)) return;
    if (*(BYTE*)(battle + 0x7647c) != 0) return;   /* zoom mode: vanilla deltas */
    if (IsBadReadPtr((void*)(battle + 0x54), 4)) return;
    DWORD cam = *(DWORD*)(battle + 0x54);
    if (!cam || IsBadReadPtr((void*)(cam + 0xdc), 4)) return;
    float ts = *(float*)(cam + 0xdc);              /* pitch target speed */
    if (ts < 10.0f || ts > 10000.0f) return;       /* the rotation setters have
                                                      not run yet (menu/startup/
                                                      camera init zeroes it) */
    if (IsBadReadPtr((void*)(battle + 0x90), 8)) return;
    float sx = *(float*)(battle + 0x90);
    float sy = *(float*)(battle + 0x94);
    if (sx < 0.0f || sx > 4000.0f || sy < 0.0f || sy > 4000.0f) return;
    DWORD drv = (DWORD)(*(int(*)())0x004f42fb)();
    if (!drv || IsBadReadPtr((void*)(drv + 0x2c), 8)) return;
    int ix = *(int*)(drv + 0x2c);
    int iy = *(int*)(drv + 0x30);
    if (ix < -100 || ix > 5000 || iy < -100 || iy > 5000) return;
    float dx = (float)ix - sx;
    float dy = (float)iy - sy;
    /* round (not truncate): the anchor is int-valued in-game (the game's
       own settle syncs it from int driver reads), so this yields exactly
       round(s*delta) with no constant directional bias */
    *(int*)(drv + 0x2c) = (int)(sx + dx * g_rotation_sens + 0.5f);
    *(int*)(drv + 0x30) = (int)(sy + dy * g_rotation_sens + 0.5f);
}

static void patch_rotation_sens_entry(void* exe) {
    static int done = 0;
    if (done) return;
    if (!g_rotation_enabled) return;
    if (g_rotation_sens > 0.999f && g_rotation_sens < 1.001f) { done = 1; return; } /* no-op */
    BYTE* p = (BYTE*)exe + (0x78fb7e - 0x400000);
    static const BYTE want[9] = {0x55, 0x8B, 0xEC, 0x81, 0xEC, 0x8C, 0x00, 0x00, 0x00};
    if (p[0] != 0xE9 && memcmp(p, want, 9) == 0) {
        DWORD rel = (DWORD)((BYTE*)&stub_rotation_entry - (p + 5));
        DWORD oldp;
        VirtualProtect(p, 5, PAGE_EXECUTE_READWRITE, &oldp);
        p[0] = 0xE9;
        *(DWORD*)(p + 1) = rel;
        VirtualProtect(p, 5, oldp, &oldp);
        log_line("== rotation-sens: handler entry 0x78FB7E wrapped (sens scaling) ==\n");
    } else if (p[0] != 0xE9) {
        log_line("== rotation-sens: 0x78FB7E bytes mismatch, skipped ==\n");
    }
    done = 1;
}

static void patch_rotation_smooth(void* exe) {
    static int done = 0;
    if (done) return;
    if (!g_rotation_enabled) return;         /* 0 = vanilla behavior, sites untouched */
    static const BYTE want_pitch[5] = {0xE8, 0xB5, 0xDF, 0xEC, 0xFF}; /* 0x790052 -> 65E00C */
    static const BYTE want_rot[5]   = {0xE8, 0xB4, 0xDD, 0xEC, 0xFF}; /* 0x7900BA -> 65DE73 */
    int ok = 1;
    {
        BYTE* p = (BYTE*)exe + (0x790052 - 0x400000);
        if (memcmp(p, want_pitch, 5) != 0) {
            log_line("== rotation-boost: 0x790052 bytes mismatch, skipped ==\n");
            ok = 0;
        } else {
            DWORD rel = (DWORD)((BYTE*)&wrap_rotation_pitch - (p + 5));
            DWORD oldp;
            VirtualProtect(p, 5, PAGE_EXECUTE_READWRITE, &oldp);
            *(DWORD*)(p + 1) = rel;
            VirtualProtect(p, 5, oldp, &oldp);
            log_line("== rotation-boost: pitch site 0x790052 wrapped ==\n");
        }
    }
    {
        BYTE* p = (BYTE*)exe + (0x7900BA - 0x400000);
        if (memcmp(p, want_rot, 5) != 0) {
            log_line("== rotation-boost: 0x7900BA bytes mismatch, skipped ==\n");
            ok = 0;
        } else {
            DWORD rel = (DWORD)((BYTE*)&wrap_rotation_rotate - (p + 5));
            DWORD oldp;
            VirtualProtect(p, 5, PAGE_EXECUTE_READWRITE, &oldp);
            *(DWORD*)(p + 1) = rel;
            VirtualProtect(p, 5, oldp, &oldp);
            log_line("== rotation-boost: rotate site 0x7900BA wrapped ==\n");
        }
    }
    if (ok) done = 1;
}
static void patch_call_site(void* exe, DWORD rva, void* wrapper, const char* name) {
    BYTE* p = (BYTE*)exe + rva;
    if (p[0] != 0xE8) {
        char buf[128];
        _snprintf(buf, sizeof(buf), "== %s site mismatch (byte 0x%02X) ==\n", name, p[0]);
        log_line(buf);
        return;
    }
    DWORD rel = (DWORD)((BYTE*)wrapper - (p + 5));
    DWORD oldp;
    VirtualProtect(p, 5, PAGE_EXECUTE_READWRITE, &oldp);
    *(DWORD*)(p + 1) = rel;
    VirtualProtect(p, 5, oldp, &oldp);
    {
        char buf[128];
        _snprintf(buf, sizeof(buf), "== call site %s patched ==\n", name);
        log_line(buf);
    }
}

static void patch_pick_sites(void* exe) {
    static int done = 0;
    if (done) return;
    done = 1;
    patch_call_site(exe, 0x54F035 - 0x400000, &wrap_pick_a, "pickA 0x54F035");
    patch_call_site(exe, 0x53572B - 0x400000, &wrap_pick_b, "pickB 0x53572B");
    patch_call_site(exe, 0x6CDA1F - 0x400000, &wrap_box, "boxhit 0x6CDA1F");
}

/* ================= QoL: AI players 0-7 (skirmish/setup extension) =================
   v59 merge: enables picking 0-7 AI players in the scenario/skirmish setup
   (the vanilla hardcodes 0-3). Gated by hghook.ini [QoL] QoLFeaturesEnabled
   (1 = on, default; 0 = vanilla behavior - pair with the original Start.inf).
   Pairs with the modded Inf\Prism\Start.inf ([AiPlayersPopup] rows 0-7).
   Behavior-only: the research/diagnostics live in hghook_ai.c and the
   AI_PLAYERS.md diary (milestone v3, user-verified: 7 AIs play, an AI can
   take the former player slot, the editing row live-updates, the yellow
   highlight follows the click).
   Patch inventory:
   - force-render 0x7c4711: always run the table render branch
   - addrow-bound 0x7cc75e: after every rebuild remove rows count+1..8
   - clamp sites 1-10: aiStack[count] reads overflow the 3-triple arrays
     when count > 3 -> clamp the index to 0 (the arrays are only ever
     zero-initialized, so index 0 is semantically identical)
   - hdr-live 0x7c3c99: the editing row above the table copies the SELECTED
     vector row's idx/team/race live (sites 0x7c3cbb/0x7c3cdd are skipped)
   - yellow-sel 0x7c4a3f + auto-select 0x7c49ba: the yellow text follows
     the CLICK selection (the vanilla's id-based flag made "AI ID 1" yellow
     permanently because the mod's rows carry id = i)
   - fixrow 0x7cc609: built AI rows get idx/team/race from the map cfg
     slots (preserving popup edits by position), +0x98 id = i, +0x1cc = 0
   - edit-prop 0x7beb90: team/empire/race popup edits land in the clicked
     row + the rebuild command row stays active
   - claim0/claim1/claimcap 0x7be194/0x7be415/0x7be37d: slot-0 reserve,
     count recompute, triple-write cap for counts > 3
   - start-force 0x7bf88d: NOP the valid() gate in the start handler
   - aimark 0x7c8347: row-based AI marking at launch (covers 0-7, never
     marks the human; replaces the vanilla 3-triple loop)
   - d1ef2 flag: level changes invalidate the fixrow preserve
================================================================================= */

static int qol_rd(DWORD p, DWORD* v) {    if (!p || p < 0x10000 || p > 0x7FFE0000) return 0;
    if (IsBadReadPtr((void*)p, 4)) return 0;
    *v = *(DWORD*)p;
    return 1;
}

/* ---- the map cfg object for the current level (screen+0xab0 map -> obj) ---- */
static DWORD qol_find_cfg_obj(DWORD screen) {
    DWORD cfg = 0;
    if (!qol_rd(screen + 0xab0, &cfg) || !cfg || cfg < 0x10000 || cfg > 0x7FFE0000) return 0;
    DWORD arr = 0, cnt = 0;
    qol_rd(cfg, &arr); qol_rd(cfg + 8, &cnt);
    if (!arr || cnt > 16 || arr < 0x10000 || arr > 0x7FFE0000) return 0;
    DWORD src = 0;
    qol_rd(screen + 0xab4, &src);
    for (DWORD i = 0; i < cnt; i++) {
        DWORD v = 0;
        if (!qol_rd(arr + i * 4, &v) || !v) continue;
        DWORD np = 0;
        qol_rd(v + 8, &np);
        if (np && np == src) return v;
    }
    for (DWORD i = 0; i < cnt; i++) {
        DWORD v = 0;
        if (!qol_rd(arr + i * 4, &v) || !v) continue;
        DWORD np = 0;
        qol_rd(v + 8, &np);
        if (np && src && np >= 0x10000 && src >= 0x10000 &&
            !IsBadReadPtr((void*)np, 2) && !IsBadReadPtr((void*)src, 2) &&
            _stricmp((const char*)np, (const char*)src) == 0) return v;
    }
    return 0;
}

/* ---- level-change flag: FUN_007d1ef2 (the full table rebuild) marks a new
   level so fixrow drops the preserve and rebuilds from the map cfg ---- */
__attribute__((used)) static int g_qol_level_changed = 0;
__attribute__((naked)) static void qol_d1ef2(void) {
    __asm__ volatile (
        "movl $1, _g_qol_level_changed\n"
        "pushl %ebp\n"            /* 55 8B EC 6A FF */
        "movl %esp, %ebp\n"
        "pushl $-1\n"
        "jmp *_g_qol_res_d1ef2\n"
    );
}
__attribute__((used)) static DWORD g_qol_res_d1ef2 = 0x7d1ef7;

static void patch_qol_d1ef2(void* exe) {
    BYTE* p = (BYTE*)exe + (0x7d1ef2 - 0x400000);
    static const BYTE want[5] = {0x55, 0x8B, 0xEC, 0x6A, 0xFF};
    if (p[0] == 0xE9) return;
    if (memcmp(p, want, 5) != 0) {
        log_line("== qol d1ef2 bytes mismatch ==\n");
        return;
    }
    DWORD rel = (DWORD)((BYTE*)&qol_d1ef2 - (p + 5));
    DWORD oldp;
    VirtualProtect(p, 5, PAGE_EXECUTE_READWRITE, &oldp);
    p[0] = 0xE9;
    *(DWORD*)(p + 1) = rel;
    VirtualProtect(p, 5, oldp, &oldp);
    log_line("== qol d1ef2 flag patched (0x7d1ef2) ==\n");
}

/* ---- mid-function patch helper (5-byte jmp over the verified bytes) ---- */
static void patch_mid(void* exe, DWORD va, void* stub, const BYTE* want, int len, const char* name) {
    BYTE* p = (BYTE*)exe + (va - 0x400000);
    if (p[0] == 0xE9) return;
    if (memcmp(p, want, len) != 0) {
        char buf[160];
        _snprintf(buf, sizeof(buf), "== midpatch %s bytes mismatch ==\n", name);
        log_line(buf);
        return;
    }
    DWORD rel = (DWORD)((BYTE*)stub - (p + 5));
    DWORD oldp;
    VirtualProtect(p, 5, PAGE_EXECUTE_READWRITE, &oldp);
    p[0] = 0xE9;
    *(DWORD*)(p + 1) = rel;
    VirtualProtect(p, 5, oldp, &oldp);
    char b2[128];
    _snprintf(b2, sizeof(b2), "== midpatch %s installed (0x%X) ==\n", name, va);
    log_line(b2);
}

static void patch_clamp_site(void* exe, DWORD va, void* stub, const BYTE* want, int len, const char* name) {
    BYTE* p = (BYTE*)exe + (va - 0x400000);
    if (p[0] == 0xE9) return;
    if (memcmp(p, want, len) != 0) {
        char buf[128];
        _snprintf(buf, sizeof(buf), "== clamp %s bytes mismatch ==\n", name);
        log_line(buf);
        return;
    }
    DWORD rel = (DWORD)((BYTE*)stub - (p + 5));
    DWORD oldp;
    VirtualProtect(p, len, PAGE_EXECUTE_READWRITE, &oldp);
    p[0] = 0xE9;
    *(DWORD*)(p + 1) = rel;
    for (int i = 5; i < len; i++) p[i] = 0x90;
    VirtualProtect(p, len, oldp, &oldp);
    char buf[128];
    _snprintf(buf, sizeof(buf), "== clamp %s patched (0x%X) ==\n", name, va);
    log_line(buf);
}

/* ---- force FUN_007c3968's render branch: 0x7c4711 jne 0x7c4872 -> jmp ---- */
static void patch_force_render(void* exe) {
    BYTE* p = (BYTE*)exe + (0x7c4711 - 0x400000);
    static const BYTE want[6] = {0x0F, 0x85, 0x5B, 0x01, 0x00, 0x00};
    if (p[0] == 0xE9) return;
    if (memcmp(p, want, 6) != 0) {
        log_line("== force-render bytes mismatch ==\n");
        return;
    }
    DWORD oldp;
    VirtualProtect(p, 6, PAGE_EXECUTE_READWRITE, &oldp);
    p[0] = 0xE9;
    p[1] = 0x5C;
    p[2] = 0x01;
    p[3] = 0x00;
    p[4] = 0x00;
    p[5] = 0x90;
    VirtualProtect(p, 6, oldp, &oldp);
    log_line("== force-render patched (0x7c4711) ==\n");
}

/* ---- addE loop bound in FUN_007cb3f4 (0x7cc75e): the vanilla bound of 3
   left stale rows "AI ID count+1..8" behind; bound = 8 removes them all ---- */
__attribute__((used)) static DWORD g_bound_body = 0x7cc76b;
__attribute__((used)) static DWORD g_bound_exit = 0x7cc820;
__attribute__((naked)) static void bound_stub(void) {
    __asm__ volatile (
        "cmpl $8, -0x428(%ebp)\n"    /* i < 8: remove rows i+1 = count+1..8 */
        "jl 1f\n"
        "jmp *_g_bound_exit\n"
        "1:\n"
        "jmp *_g_bound_body\n"
    );
}

static void patch_addrow_bound(void* exe) {
    BYTE* p = (BYTE*)exe + (0x7cc75e - 0x400000);
    static const BYTE want[7] = {0x83, 0xBD, 0xD8, 0xFB, 0xFF, 0xFF, 0x03};
    if (p[0] == 0xE9) return;
    if (memcmp(p, want, 7) != 0) {
        log_line("== addrow-bound bytes mismatch ==\n");
        return;
    }
    DWORD oldp;
    VirtualProtect(p, 7, PAGE_EXECUTE_READWRITE, &oldp);
    p[0] = 0xE9;
    *(DWORD*)(p + 1) = (DWORD)((BYTE*)&bound_stub - (p + 5));
    p[5] = 0x90;
    p[6] = 0x90;
    VirtualProtect(p, 7, oldp, &oldp);
    /* NOP the now-redundant jge at 0x7cc765 */
    BYTE* q = (BYTE*)exe + (0x7cc765 - 0x400000);
    VirtualProtect(q, 6, PAGE_EXECUTE_READWRITE, &oldp);
    for (int i = 0; i < 6; i++) q[i] = 0x90;
    VirtualProtect(q, 6, oldp, &oldp);
    log_line("== addrow-bound patched (0x7cc75e) ==\n");
}

/* ===== aiStack clamp stubs (index clamp to 0 when count > 2) ===== */
__attribute__((used)) static DWORD g_res_clamp1 = 0x7cc9bc;
__attribute__((naked)) static void clamp_stub1(void) {
    __asm__ volatile (
        "movl 0xab8(%ecx), %edx\n"
        "cmpl $2, %edx\n"
        "jle 1f\n"
        "xorl %edx, %edx\n"
        "1:\n"
        "movl -0x38(%ebp,%edx,4), %eax\n"
        "jmp *_g_res_clamp1\n"
    );
}
/* Site 2 (0x7c3c99, the FUN_007c3968 header aiStack-idx read) -> LIVE
   header: copy the SELECTED vector row's idx/team/race into the header
   template locals (the vanilla read the aiStack triples here; the mod's
   rows live in the VECTOR, and ab8 = the selected row's vector index set
   by the click handler via d30fc). This makes the editing row above the
   table live-update. Only the three fields the vanilla wrote are touched
   (never the name/id - the renderer has NO commit path back to the list). */
__attribute__((used)) static DWORD g_res_hdrlive = 0x7c3cf9;
__attribute__((naked)) static void hdr_live_stub(void) {
    __asm__ volatile (
        "pushal\n"
        "movl -0x3e4(%ebp), %eax\n"   /* screen */
        "testl %eax, %eax\n"
        "jz 2f\n"
        "movl 0xab8(%eax), %edx\n"    /* selected AI index */
        "testl %edx, %edx\n"
        "jl 2f\n"
        "movl 0xc68604, %ecx\n"       /* list */
        "testl %ecx, %ecx\n"
        "jz 2f\n"
        "movl 0x8(%ecx), %ebx\n"      /* vector base */
        "testl %ebx, %ebx\n"
        "jz 2f\n"
        "movl 0x10(%ecx), %esi\n"     /* vector count */
        "cmpl %esi, %edx\n"
        "jge 2f\n"
        "imull $0x1fc, %edx, %edx\n"
        "addl %ebx, %edx\n"           /* row = base + ab8*0x1fc */
        "movl 0x8c(%edx), %eax\n"
        "movl %eax, -0x1a0(%ebp)\n"   /* template.idx = row.idx */
        "movl 0x90(%edx), %eax\n"
        "movl %eax, -0x19c(%ebp)\n"   /* template.team = row.team */
        "movl 0x94(%edx), %eax\n"
        "movl %eax, -0x198(%ebp)\n"   /* template.race = row.race */
        "2:\n"
        "popal\n"
        "jmp *_g_res_hdrlive\n"
    );
}
__attribute__((used)) static DWORD g_res_clamp3 = 0x7c3cd7;
__attribute__((naked)) static void clamp_stub3(void) {
    __asm__ volatile (
        "jmp *_g_res_clamp3\n"
    );
}
__attribute__((used)) static DWORD g_res_clamp4 = 0x7c3cf9;
__attribute__((naked)) static void clamp_stub4(void) {
    __asm__ volatile (
        "jmp *_g_res_clamp4\n"
    );
}

/* ---- yellow highlight (0x7c4a3f): the table's yellow text = FUN_007ce33c
   param_5 = (row.id == header-template id). The header template is
   refreshed from the config's first entry each frame (valid() -> template
   .assign(configEntry0)) and the mod's rows carry id = i -> "AI ID 1" was
   permanently yellow. Replace the flag with the CLICK selection
   (screen+0xabc == row.pos), the same condition as the selection arrows. ---- */
__attribute__((used)) static DWORD g_res_yellow = 0x7c4a4f;
__attribute__((naked)) static void yellow_stub(void) {
    __asm__ volatile (
        "movl -0x3e4(%ebp), %eax\n"   /* screen */
        "testl %eax, %eax\n"
        "jz 1f\n"
        "movl 0xabc(%eax), %eax\n"    /* abc = clicked row pos */
        "movl -0x18(%ebp), %ecx\n"    /* row pair */
        "testl %ecx, %ecx\n"
        "jz 1f\n"
        "cmpl 0x4(%ecx), %eax\n"      /* abc vs pair[1] */
        "sete %cl\n"
        "pushl %ecx\n"
        "jmp *_g_res_yellow\n"
        "1:\n"
        "xorl %ecx, %ecx\n"
        "pushl %ecx\n"
        "jmp *_g_res_yellow\n"
    );
}

static void patch_yellow_sel(void* exe) {
    BYTE* p = (BYTE*)exe + (0x7c4a3f - 0x400000);
    static const BYTE want[5] = {0x8B, 0x85, 0xB8, 0xFD, 0xFF};
    if (p[0] == 0xE9) return;
    if (memcmp(p, want, 5) != 0) {
        log_line("== yellow sel bytes mismatch ==\n");
        return;
    }
    DWORD rel = (DWORD)((BYTE*)&yellow_stub - (p + 5));
    DWORD oldp;
    VirtualProtect(p, 5, PAGE_EXECUTE_READWRITE, &oldp);
    p[0] = 0xE9;
    *(DWORD*)(p + 1) = rel;
    VirtualProtect(p, 5, oldp, &oldp);
    log_line("== yellow sel patched (0x7c4a3f) ==\n");
}

/* ---- auto-select fix (0x7c49ba): when no row is selected (abc < 0) the
   renderer auto-selects the first row whose id == the header template id.
   The vanilla's rows all had id=-1 so this never fired; the mod's rows have
   id=i -> AI1 got auto-selected (arrows). jne -> jmp. ---- */
static void patch_yellow_fix(void* exe) {
    BYTE* p = (BYTE*)exe + (0x7c49ba - 0x400000);
    static const BYTE want[2] = {0x75, 0x12};
    if (p[0] == 0xEB) return;
    if (memcmp(p, want, 2) != 0) {
        log_line("== yellow fix bytes mismatch ==\n");
        return;
    }
    DWORD oldp;
    VirtualProtect(p, 2, PAGE_EXECUTE_READWRITE, &oldp);
    p[0] = 0xEB;
    VirtualProtect(p, 2, oldp, &oldp);
    log_line("== yellow fix patched (0x7c49ba) ==\n");
}

__attribute__((used)) static DWORD g_res_clamp5 = 0x7be8ee;
__attribute__((naked)) static void clamp_stub5(void) {
    __asm__ volatile (
        "movl 0xab8(%edx), %eax\n"
        "cmpl $2, %eax\n"
        "jle 1f\n"
        "xorl %eax, %eax\n"
        "1:\n"
        "movl -0x28(%ebp,%eax,4), %ecx\n"
        "jmp *_g_res_clamp5\n"
    );
}
__attribute__((used)) static DWORD g_res_clamp6 = 0x7be910;
__attribute__((naked)) static void clamp_stub6(void) {
    __asm__ volatile (
        "movl 0xab8(%eax), %ecx\n"
        "cmpl $2, %ecx\n"
        "jle 1f\n"
        "xorl %ecx, %ecx\n"
        "1:\n"
        "movl -0x34(%ebp,%ecx,4), %edx\n"
        "jmp *_g_res_clamp6\n"
    );
}
__attribute__((used)) static DWORD g_res_clamp7 = 0x7be946;
__attribute__((naked)) static void clamp_stub7(void) {
    __asm__ volatile (
        "movl 0xab8(%edx), %eax\n"
        "cmpl $2, %eax\n"
        "jle 1f\n"
        "xorl %eax, %eax\n"
        "1:\n"
        "movl %eax, -0xad4(%ebp)\n"
        "jmp *_g_res_clamp7\n"
    );
}
__attribute__((used)) static DWORD g_res_clamp8 = 0x7be971;
__attribute__((naked)) static void clamp_stub8(void) {
    __asm__ volatile (
        "movl 0xab8(%ecx), %edx\n"
        "cmpl $2, %edx\n"
        "jle 1f\n"
        "xorl %edx, %edx\n"
        "1:\n"
        "movl %edx, -0xadc(%ebp)\n"
        "jmp *_g_res_clamp8\n"
    );
}
__attribute__((used)) static DWORD g_res_clamp9 = 0x7be99f;
__attribute__((naked)) static void clamp_stub9(void) {
    __asm__ volatile (
        "movl 0xab8(%eax), %ecx\n"
        "cmpl $2, %ecx\n"
        "jle 1f\n"
        "xorl %ecx, %ecx\n"
        "1:\n"
        "movl %ecx, -0xae4(%ebp)\n"
        "jmp *_g_res_clamp9\n"
    );
}
__attribute__((used)) static DWORD g_res_clamp10 = 0x7be9fd;
__attribute__((naked)) static void clamp_stub10(void) {
    __asm__ volatile (
        "movl 0xab8(%ecx), %edx\n"
        "cmpl $2, %edx\n"
        "jle 1f\n"
        "xorl %edx, %edx\n"
        "1:\n"
        "movl %edx, -0xae8(%ebp)\n"
        "jmp *_g_res_clamp10\n"
    );
}

static void patch_ai_clamps(void* exe) {
    static const BYTE w1[10] = {0x8B,0x91,0xB8,0x0A,0x00,0x00,0x8B,0x44,0x95,0xC8};
    static const BYTE w2[10] = {0x8B,0x82,0xB8,0x0A,0x00,0x00,0x8B,0x4C,0x85,0xA8};
    static const BYTE w3[10] = {0x8B,0x88,0xB8,0x0A,0x00,0x00,0x8B,0x54,0x8D,0xC0};
    static const BYTE w4[10] = {0x8B,0x91,0xB8,0x0A,0x00,0x00,0x8B,0x44,0x95,0xB4};
    static const BYTE w5[10] = {0x8B,0x82,0xB8,0x0A,0x00,0x00,0x8B,0x4C,0x85,0xD8};
    static const BYTE w6[10] = {0x8B,0x88,0xB8,0x0A,0x00,0x00,0x8B,0x54,0x8D,0xCC};
    static const BYTE w7[12] = {0x8B,0x82,0xB8,0x0A,0x00,0x00,0x89,0x85,0x2C,0xF5,0xFF,0xFF};
    static const BYTE w8[12] = {0x8B,0x91,0xB8,0x0A,0x00,0x00,0x89,0x95,0x24,0xF5,0xFF,0xFF};
    static const BYTE w9[12] = {0x8B,0x88,0xB8,0x0A,0x00,0x00,0x89,0x8D,0x1C,0xF5,0xFF,0xFF};
    static const BYTE w10[12] = {0x8B,0x91,0xB8,0x0A,0x00,0x00,0x89,0x95,0x18,0xF5,0xFF,0xFF};
    patch_clamp_site(exe, 0x7cc9b2, &clamp_stub1, w1, 10, "site1 7cc9b2");
    patch_clamp_site(exe, 0x7c3c99, &hdr_live_stub, w2, 10, "hdrlive 7c3c99");
    patch_clamp_site(exe, 0x7c3cbb, &clamp_stub3, w3, 10, "site3 7c3cbb");
    patch_clamp_site(exe, 0x7c3cdd, &clamp_stub4, w4, 10, "site4 7c3cdd");
    patch_clamp_site(exe, 0x7be8e4, &clamp_stub5, w5, 10, "site5 7be8e4");
    patch_clamp_site(exe, 0x7be906, &clamp_stub6, w6, 10, "site6 7be906");
    patch_clamp_site(exe, 0x7be93a, &clamp_stub7, w7, 12, "site7 7be93a");
    patch_clamp_site(exe, 0x7be965, &clamp_stub8, w8, 12, "site8 7be965");
    patch_clamp_site(exe, 0x7be993, &clamp_stub9, w9, 12, "site9 7be993");
    patch_clamp_site(exe, 0x7be9f1, &clamp_stub10, w10, 12, "site10 7be9f1");
    patch_yellow_fix(exe);
    patch_yellow_sel(exe);
}

/* ---- fixrow (0x7cc609): the addR2 row builder writes idx/team/race from
   the map cfg slots (fallback 0 for -1/SELECTABLE) and PRESERVES the
   existing row's fields when a row with the same position exists (the
   popup edits live in the row). +0x98 (AI id) = i; +0x1cc = 0 (the count
   lives in the command row; a nonzero f1cc drove the launch's triple-loop
   over-read). g_qol_level_changed invalidates the preserve on level
   switches. ---- */
__attribute__((used)) static void fix_row_fields(DWORD ebp_) {
    DWORD i = 0, screen = 0;
    qol_rd(ebp_ - 0x428, &i);
    qol_rd(ebp_ - 0x9f8, &screen);
    DWORD idx = i + 1;
    DWORD team = 0xFFFFFFFF, race = 0xFFFFFFFF;
    int preserved = 0;
    int changed = g_qol_level_changed;
    if (i == 0) g_qol_level_changed = 0;
    if (!changed) {
        DWORD lst = 0;
        qol_rd(0xc68604, &lst);
        if (lst && lst > 0x10000 && lst < 0x7FFE0000) {
            DWORD base = 0, cnt = 0;
            qol_rd(lst + 8, &base); qol_rd(lst + 0x10, &cnt);
            if (base && cnt <= 16 && i < cnt) {
                DWORD row = base + i * 0x1fc;
                DWORD name0 = 0, pidx = 0, pteam = 0, prace = 0;
                qol_rd(row + 8, &name0);
                qol_rd(row + 0x8c, &pidx); qol_rd(row + 0x90, &pteam); qol_rd(row + 0x94, &prace);
                if (name0 != 0 && (int)pidx >= 0 && (int)pidx <= 8) {
                    idx = pidx; team = pteam; race = prace;
                    preserved = 1;
                }
            }
        }
    }
    if (!preserved) {
        DWORD obj = qol_find_cfg_obj(screen);
        if (obj) {
            DWORD slots = 0, players = 0;
            qol_rd(obj + 0x38, &slots); qol_rd(obj, &players);
            if (slots && players > idx && players <= 16) {
                DWORD slot = 0;
                if (qol_rd(slots + idx * 4, &slot) && slot && slot > 0x10000) {
                    qol_rd(slot + 8, &team); qol_rd(slot + 0xc, &race);
                }
            }
        }
    }
    if ((int)team == -1) team = 0;
    if ((int)race == -1) race = 0;
    if (!IsBadWritePtr((void*)(ebp_ - 0x7a0), 4)) *(DWORD*)(ebp_ - 0x7a0) = idx;
    if (!IsBadWritePtr((void*)(ebp_ - 0x79c), 4)) *(DWORD*)(ebp_ - 0x79c) = team;
    if (!IsBadWritePtr((void*)(ebp_ - 0x798), 4)) *(DWORD*)(ebp_ - 0x798) = race;
    if (!IsBadWritePtr((void*)(ebp_ - 0x794), 4)) *(DWORD*)(ebp_ - 0x794) = i;
    if (!IsBadWritePtr((void*)(ebp_ - 0x660), 4)) *(DWORD*)(ebp_ - 0x660) = 0;
}

__attribute__((naked)) static void fixrow_7cc609(void) {
    __asm__ volatile (
        "pushal\n"
        "movl 8(%esp), %eax\n"    /* saved ebp */
        "pushl %eax\n"
        "call _fix_row_fields\n"
        "addl $4, %esp\n"
        "popal\n"
        "jmp *_g_res_fixrow\n"
    );
}
__attribute__((used)) static DWORD g_res_fixrow = 0x7cc65d;

static void patch_fixrow_site(void* exe) {
    BYTE* p = (BYTE*)exe + (0x7cc609 - 0x400000);
    static const BYTE want[5] = {0x8B, 0x85, 0xD8, 0xFB, 0xFF};
    if (p[0] == 0xE9) return;
    if (memcmp(p, want, 5) != 0) {
        log_line("== fixrow site bytes mismatch ==\n");
        return;
    }
    DWORD rel = (DWORD)((BYTE*)&fixrow_7cc609 - (p + 5));
    DWORD oldp;
    VirtualProtect(p, 5, PAGE_EXECUTE_READWRITE, &oldp);
    p[0] = 0xE9;
    *(DWORD*)(p + 1) = rel;
    VirtualProtect(p, 5, oldp, &oldp);
    log_line("== fixrow site patched (0x7cc609) ==\n");
}

/* ---- EDIT propagation (0x7beb90): popup edits (widget idx 9=team,
   10=empire/slot, 11=race) land in the clicked AI row's own fields, and
   the command row must carry active=1 + the current count so the rebuild
   runs. ---- */
__attribute__((used)) static void apply_row_edit(DWORD ebp_) {
    DWORD widx = 0;
    qol_rd(ebp_ + 8, &widx);
    if (widx != 9 && widx != 10 && widx != 11) return;
    DWORD screen = 0;
    qol_rd(ebp_ - 0xb04, &screen);
    if (!screen) return;
    DWORD ab8 = 0;
    qol_rd(screen + 0xab8, &ab8);
    DWORD sel = 0;
    qol_rd(ebp_ - 0x10, &sel);
    DWORD lst = 0;
    qol_rd(0xc68604, &lst);
    if (!lst || lst < 0x10000 || lst > 0x7FFE0000) return;
    DWORD base = 0, cnt = 0;
    qol_rd(lst + 8, &base); qol_rd(lst + 0x10, &cnt);
    if (!base || cnt > 16) return;
    if (!IsBadWritePtr((void*)(ebp_ - 0x178), 4)) *(DWORD*)(ebp_ - 0x178) = 1;
    if (!IsBadWritePtr((void*)(ebp_ - 0x48), 4)) *(DWORD*)(ebp_ - 0x48) = cnt;
    if (ab8 >= cnt) return;
    DWORD row = base + ab8 * 0x1fc;
    if (IsBadWritePtr((void*)row, 0x100)) return;
    if (widx == 9) {
        *(int*)(row + 0x90) = (int)sel;      /* team */
    } else if (widx == 11) {
        *(int*)(row + 0x94) = (int)sel;      /* race */
    } else {
        DWORD obj = qol_find_cfg_obj(screen);
        DWORD players = 0;
        if (obj) qol_rd(obj, &players);
        if (players > 0 && sel >= players) return;
        *(int*)(row + 0x8c) = (int)sel;      /* empire slot (0 = former player slot) */
        if (obj) {
            DWORD slots = 0;
            qol_rd(obj + 0x38, &slots);
            DWORD slot = 0;
            if (slots && sel < 16 && qol_rd(slots + sel * 4, &slot) && slot > 0x10000) {
                DWORD team = 0, race = 0;
                qol_rd(slot + 8, &team); qol_rd(slot + 0xc, &race);
                if ((int)race < 0) race = 0;
                *(int*)(row + 0x90) = (int)team;
                *(int*)(row + 0x94) = (int)race;
            }
        }
    }
}

__attribute__((naked)) static void wk_7beb90(void) {
    __asm__ volatile (
        "pushal\n"
        "movl 8(%esp), %eax\n"    /* saved ebp */
        "pushl %eax\n"
        "call _apply_row_edit\n"
        "addl $4, %esp\n"
        "popal\n"
        "leal -0x214(%ebp), %eax\n" /* overwritten LEA + PUSH */
        "pushl %eax\n"
        "jmp *_g_res_7beb90\n"
    );
}
__attribute__((used)) static DWORD g_res_7beb90 = 0x7beb97;

/* ---- claim-loop start of FUN_007bda04 case 7 (0x7be194): reserve slot 0
   for the human player (the original did this via the human row in the
   list; without it the first AI claims slot 0 and takes over the player's
   empire at game start) ---- */
__attribute__((naked)) static void wk_7be194(void) {
    __asm__ volatile (
        "movb $0, -0x228(%ebp)\n"   /* acStack[0] = 0 (slot 0 reserved) */
        "movl $0, -0x22c(%ebp)\n"   /* overwritten: MOV [EBP-0x22c],0 */
        "jmp *_g_res_7be194\n"
    );
}
__attribute__((used)) static DWORD g_res_7be194 = 0x7be19e;

/* ---- claim-loop end of FUN_007bda04 case 7 (0x7be415): the claim loop
   overflows the 3-triple aiStack for count>3 and the c=6 race write
   clobbers the sel local [EBP-0x10]; recompute the count from the popup
   sel (screen+idx -> widget+0x9c) instead. Also force the row's active
   byte +0x9c to 1 (it is otherwise stack garbage). ---- */
__attribute__((naked)) static void wk_7be415(void) {
    __asm__ volatile (
        "movb $1, -0x178(%ebp)\n"  /* row active +0x9c = 1 */
        "movl 0x8(%ebp), %ecx\n"   /* idx */
        "movl -0xb04(%ebp), %eax\n"/* screen */
        "testl %eax, %eax\n"
        "jz 1f\n"
        "movl 0x34(%eax,%ecx,4), %ecx\n" /* widget */
        "testl %ecx, %ecx\n"
        "jz 1f\n"
        "movl 0x9c(%ecx), %ecx\n"  /* sel (unclobbered) */
        "subl -0x22c(%ebp), %ecx\n"/* - unfilled */
        "movl %ecx, -0x48(%ebp)\n" /* count */
        "jmp 2f\n"
        "1:\n"                     /* fallback: old path */
        "movl -0x10(%ebp), %ecx\n"
        "subl -0x22c(%ebp), %ecx\n"
        "movl %ecx, -0x48(%ebp)\n"
        "2:\n"
        "jmp *_g_res_7be415\n"
    );
}
__attribute__((used)) static DWORD g_res_7be415 = 0x7be421;

/* ---- claim-loop body in FUN_007bda04 case 7 (0x7be37d): for c >= 3 the
   triple-array writes overflow the 3-triple aiStack and clobber locals
   (the c=6 race write hits the sel local). Skip the template3/triple
   writes for c >= 3; the acStack marking still runs (drives unfilled). ---- */
__attribute__((naked)) static void wk_7be37d(void) {
    __asm__ volatile (
        "cmpl $3, -0x440(%ebp)\n"    /* c >= 3? */
        "jge 1f\n"
        "movl -0x444(%ebp), %ecx\n"  /* redo: MOV ECX,[EBP-0x444] */
        "movl %ecx, -0x5bc(%ebp)\n"  /* MOV [EBP-0x5bc],ECX */
        "jmp *_g_res_7be37d\n"       /* 0x7be383 */
        "1:\n"
        "jmp *_g_skip_7be37d\n"      /* 0x7be3e9 */
    );
}
__attribute__((used)) static DWORD g_res_7be37d = 0x7be383;
__attribute__((used)) static DWORD g_skip_7be37d = 0x7be3e9;

/* ---- force the human-row append in FUN_007bf616: NOP the JZ gate ---- */
static void patch_start_force(void* exe) {
    BYTE* p = (BYTE*)exe + (0x7bf88d - 0x400000);
    static const BYTE want[6] = {0x0F, 0x84, 0x4C, 0x01, 0x00, 0x00};
    if (p[0] == 0x90 && p[1] == 0x90) return;
    if (memcmp(p, want, 6) != 0) {
        log_line("== start-force bytes mismatch ==\n");
        return;
    }
    DWORD oldp;
    VirtualProtect(p, 6, PAGE_EXECUTE_READWRITE, &oldp);
    for (int i = 0; i < 6; i++) p[i] = 0x90;
    VirtualProtect(p, 6, oldp, &oldp);
    log_line("== start-force patched (0x7bf88d) ==\n");
}

static void patch_ai_edits(void* exe) {
    static const BYTE wA[6] = {0x8D, 0x85, 0xEC, 0xFD, 0xFF, 0xFF};
    patch_mid(exe, 0x7beb90, &wk_7beb90, wA, 6, "edit-prop 7beb90");
    static const BYTE wB[6] = {0xC7, 0x85, 0xD4, 0xFD, 0xFF, 0xFF};
    patch_mid(exe, 0x7be194, &wk_7be194, wB, 6, "claim0 7be194");
    static const BYTE wC[6] = {0x8B, 0x4D, 0xF0, 0x2B, 0x8D, 0xD4};
    patch_mid(exe, 0x7be415, &wk_7be415, wC, 6, "claim1 7be415");
    static const BYTE wF[6] = {0x8B, 0x8D, 0xBC, 0xFB, 0xFF, 0xFF};
    patch_mid(exe, 0x7be37d, &wk_7be37d, wF, 6, "claimcap 7be37d");
    patch_start_force(exe);
}

/* ---- launch AI-mark replacement (0x7c8347): the vanilla loop AI-marks
   empires by matching its 3-triple aiStack (cap 3). The mod needs up to 7:
   mark the empire iff a VECTOR row with id >= 0 has idx == slot. Never
   marks the human (the static row's id = -1). ---- */
typedef void (__thiscall *p6676ef_t)(DWORD, DWORD);
typedef void (__thiscall *p939090_t)(DWORD, DWORD);
typedef void (__thiscall *p938fc8_t)(DWORD, DWORD);
typedef void (__cdecl *p42a8acv_t)(DWORD, const char*, ...);

__attribute__((used)) static void mark_empire_if_ai(DWORD empire, DWORD idx, DWORD aimode) {
    DWORD lst = 0;
    if (!qol_rd(0xc68604, &lst) || !lst || lst < 0x10000) return;
    DWORD base = 0, cnt = 0;
    qol_rd(lst + 8, &base); qol_rd(lst + 0x10, &cnt);
    if (!base || base < 0x10000 || cnt > 16) return;
    for (DWORD i = 0; i < cnt; i++) {
        DWORD row = base + i * 0x1fc;
        int rid = 0xEEEEEEEE, rIdx = 0xEEEEEEEE;
        if (!qol_rd(row + 0x98, (DWORD*)&rid) || !qol_rd(row + 0x8c, (DWORD*)&rIdx)) continue;
        if (rid >= 0 && rIdx == (int)idx) {
            p6676ef_t f1 = (p6676ef_t)0x6676ef; f1(empire, 3);
            p42a8acv_t f2 = (p42a8acv_t)0x42a8ac;
            f2(0xC4FEC8, "AI Diff Level %d", 0);
            DWORD ctrl = 0;
            qol_rd(empire + 0x30, &ctrl);
            if (ctrl && ctrl > 0x10000) {
                p939090_t f3 = (p939090_t)0x939090; f3(ctrl, 2);
                p938fc8_t f4 = (p938fc8_t)0x938fc8; f4(ctrl, aimode);
            }
            return;
        }
    }
}

__attribute__((naked)) static void launch_aimark(void) {
    __asm__ volatile (
        "pushal\n"
        "movl -0x948(%ebp), %eax\n"      /* empire index (slot) */
        "movl -0x234(%ebp,%eax,4), %edx\n" /* AI mode[slot] */
        "movl -0x448(%ebp), %ecx\n"      /* empire obj */
        "pushl %edx\n"
        "pushl %eax\n"
        "pushl %ecx\n"
        "call _mark_empire_if_ai\n"
        "addl $12, %esp\n"
        "popal\n"
        "jmp *_g_aimark_ret\n"
    );
}
__attribute__((used)) static DWORD g_aimark_ret = 0x7c83d1;

static void patch_launch_ai3(void* exe) {
    BYTE* p = (BYTE*)exe + (0x7c8347 - 0x400000);
    static const BYTE want[5] = {0x3B, 0x55, 0xB8, 0x0F, 0x8D};
    if (p[0] == 0xE9) return;
    if (memcmp(p, want, 5) != 0) {
        log_line("== aimark bytes mismatch ==\n");
        return;
    }
    DWORD rel = (DWORD)((BYTE*)&launch_aimark - (p + 5));
    DWORD oldp;
    VirtualProtect(p, 5, PAGE_EXECUTE_READWRITE, &oldp);
    p[0] = 0xE9;
    *(DWORD*)(p + 1) = rel;
    VirtualProtect(p, 5, oldp, &oldp);
    log_line("== aimark patched (0x7c8347) ==\n");
}

/* ---- the whole AI extension, gated by [QoL] QoLFeaturesEnabled ---- */
static void patch_ai_qol(void* exe) {
    if (!g_qol_enabled) return;
    patch_force_render(exe);
    patch_addrow_bound(exe);
    patch_ai_clamps(exe);
    patch_fixrow_site(exe);
    patch_ai_edits(exe);
    patch_launch_ai3(exe);
    patch_qol_d1ef2(exe);
}

static LONG WINAPI g_crash_filter(PEXCEPTION_POINTERS ep) {
    if (g_log_ok) {
        DWORD code = ep->ExceptionRecord->ExceptionCode;
        DWORD addr = (DWORD)ep->ExceptionRecord->ExceptionAddress;
        DWORD eip = ep->ContextRecord->Eip;
        DWORD esp = ep->ContextRecord->Esp;
        char buf[1024];
        int off = _snprintf(buf, sizeof(buf),
            "== CRASH: code=0x%08X addr=0x%08X (RVA 0x%X) eip=0x%08X esp=0x%08X\n",
            code, addr, addr - 0x400000, eip, esp);
        {
            MEMORY_BASIC_INFORMATION mbi;
            if (VirtualQuery((void*)addr, &mbi, sizeof(mbi)) == sizeof(mbi)) {
                off += _snprintf(buf + off, sizeof(buf) - off,
                    "   region: base=0x%X alloc=0x%X prot=0x%X\n",
                    (DWORD)mbi.BaseAddress, (DWORD)mbi.AllocationBase, mbi.Protect);
            }
            HMODULE me = GetModuleHandleA("binkw32.dll");
            off += _snprintf(buf + off, sizeof(buf) - off,
                "   hook base=0x%X\n", (DWORD)me);
        }
        off += _snprintf(buf + off, sizeof(buf) - off,
            "   eax=0x%08X ecx=0x%08X edx=0x%08X ebx=0x%08X ebp=0x%08X esi=0x%08X edi=0x%08X\n"
            "   stack:",
            ep->ContextRecord->Eax, ep->ContextRecord->Ecx,
            ep->ContextRecord->Edx, ep->ContextRecord->Ebx,
            ep->ContextRecord->Ebp, ep->ContextRecord->Esi,
            ep->ContextRecord->Edi);
        for (int i = 0; i < 48 && off < (int)sizeof(buf) - 16; i++) {
            DWORD v = 0xFFFFFFFF;
            if (!IsBadReadPtr((void*)(esp + i * 4), 4)) {
                v = *(DWORD*)(esp + i * 4);
            }
            else break;
            off += _snprintf(buf + off, sizeof(buf) - off, " 0x%08X", v);
        }
        off += _snprintf(buf + off, sizeof(buf) - off, "\n");
        log_line(buf);
        /* walk the ebp chain for the call stack */
        {
            char cb2[512];
            int o2 = _snprintf(cb2, sizeof(cb2), "   chain:");
            DWORD f = ep->ContextRecord->Ebp;
            for (int i = 0; i < 14; i++) {
                if (f < 0x10000 || f > 0x7FFE0000) break;
                if (IsBadReadPtr((void*)f, 8)) break;
                DWORD ret = *(DWORD*)(f + 4);
                if (ret < 0x400000 || ret > 0x400000 + 0xA00000) break;
                o2 += _snprintf(cb2 + o2, sizeof(cb2) - o2, " 0x%X", ret - 0x400000);
                DWORD next = *(DWORD*)f;
                if (next <= f) break;
                f = next;
            }
            o2 += _snprintf(cb2 + o2, sizeof(cb2) - o2, "\n");
            log_line(cb2);
        }
    }
    return EXCEPTION_CONTINUE_SEARCH;
}

static DWORD WINAPI worker(LPVOID arg) {
    (void)arg;
    g_log_ok = 1;
    /* per-session log: flush the previous session's proj_log.txt (the
       findings are distilled into MODLOG/HANDOFF; a fresh log per run
       keeps it small and easy to read) */
    {
        FILE* f = fopen("C:\\Users\\Nova\\haegemonia-mod\\proj_log.txt", "w");
        if (f) fclose(f);
    }
    log_line("== hook DLL loaded (release-1.0) ==\n");
    HMODULE exe = GetModuleHandleA(NULL);
    g_exe = (BYTE*)exe;
    load_config();
    g_pScreenW = (DWORD*)((BYTE*)exe + 0xc5127c - 0x400000);
    g_pScreenH = (DWORD*)((BYTE*)exe + 0xc51280 - 0x400000);
    validate_reg_resolution();
    /* EARLY d3d8 IAT patch: the game calls Direct3DCreate8 very early
       (before W/H are set), so the hook must land first - the old order
       waited for W/H and missed it. */
    for (int tries = 0; tries < 100 && !g_real_D3DC8; tries++) {
        HMODULE d3d8 = LoadLibraryA("d3d8.dll");
        if (d3d8) {
            g_real_D3DC8 = (tDirect3DCreate8)GetProcAddress(d3d8, "Direct3DCreate8");
        }
        if (!g_real_D3DC8) Sleep(20);
    }
    if (!g_real_D3DC8) {
        log_line("== FAILED to resolve Direct3DCreate8 ==\n");
    }
    else {
        IMAGE_DOS_HEADER* dos = (IMAGE_DOS_HEADER*)exe;
        IMAGE_NT_HEADERS* nt = (IMAGE_NT_HEADERS*)((BYTE*)exe + dos->e_lfanew);
        DWORD impRVA = nt->OptionalHeader.DataDirectory[IMAGE_DIRECTORY_ENTRY_IMPORT].VirtualAddress;
        IMAGE_IMPORT_DESCRIPTOR* imp = (IMAGE_IMPORT_DESCRIPTOR*)((BYTE*)exe + impRVA);
        int patched = 0;
        for (; imp->Name; imp++) {
            const char* dll = (const char*)((BYTE*)exe + imp->Name);
            if (_stricmp(dll, "d3d8.dll") != 0) continue;
            IMAGE_THUNK_DATA* thunk = (IMAGE_THUNK_DATA*)((BYTE*)exe + imp->FirstThunk);
            for (; thunk->u1.Function; thunk++) {
                if ((DWORD)thunk->u1.Function == (DWORD)g_real_D3DC8) {
                    DWORD oldp;
                    VirtualProtect(thunk, 4, PAGE_READWRITE, &oldp);
                    thunk->u1.Function = (ULONG_PTR)&my_Direct3DCreate8;
                    VirtualProtect(thunk, 4, oldp, &oldp);
                    patched = 1;
                }
            }
        }
        log_line(patched ? "== Direct3DCreate8 IAT patched (early) ==\n" : "== IAT patch: entry not found ==\n");
    }
    for (int tries = 0; tries < 600; tries++) {
        DWORD* pW = (DWORD*)((BYTE*)exe + 0xc5127c - 0x400000);
        DWORD* pH = (DWORD*)((BYTE*)exe + 0xc51280 - 0x400000);
        if (*pW > 0 && *pH > 0) {
            design_width_override(exe);
            break;
        }
        Sleep(50);
    }
    patch_clipcursor(exe);
    patch_setcursorpos(exe);
    patch_quad_traces(exe);
    patch_pick_sites(exe);
    patch_rotation_smooth(exe);
    patch_rotation_sens_entry(exe);
    patch_rowh_sites(exe);
    patch_setrect_sites(exe);
    patch_slide_click(exe);
    patch_techhit(exe);
    patch_mode_caps(exe);
    patch_fileio(exe);
    patch_grid_fns(exe);
    patch_popup_clamp(exe);
    patch_action_hooks(exe);
    patch_ai_qol(exe);
    for (int i = 0; i < 40; i++) {
        Sleep(3000);
        design_width_override(exe);
        patch_quad_traces(exe);
        patch_rowh_sites(exe);
        patch_setrect_sites(exe);
            patch_slide_click(exe);
        patch_techhit(exe);
        patch_mode_caps(exe);
        patch_fileio(exe);
        patch_grid_fns(exe);
        patch_popup_clamp(exe);
        patch_action_hooks(exe);
        patch_scene_hook(exe);
        patch_rotation_smooth(exe);
        patch_rotation_sens_entry(exe);
        patch_ai_qol(exe);
    }
    return 0;
}

BOOL WINAPI DllMain(HINSTANCE hinst, DWORD reason, LPVOID reserved) {
    (void)hinst; (void)reserved;
    if (reason == DLL_PROCESS_ATTACH) {
        InitializeCriticalSection(&g_log_cs);
        HMODULE orig = LoadLibraryA("bink_orig.dll");
        if (orig) {
#include "resolve.inc"
        }
        SetUnhandledExceptionFilter(g_crash_filter);
        CreateThread(NULL, 0, worker, NULL, 0, NULL);
    }
    if (reason == DLL_PROCESS_DETACH) {
        DeleteCriticalSection(&g_log_cs);
    }
    return TRUE;
}















