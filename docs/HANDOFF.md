# Haegemonia: Legions of Iron Widescreen mod

This document describes every fix, the reverse-engineered data behind it,
all known addresses, and the build loop.

Goal: 16:9 (1080p/1440p/4K), no stretching, plus 2x-resolution font support.

## Current state (all user-verified unless noted)

3D + all text + clickable areas + UI icons correct; fonts at 2x texture
resolution with original size. Verified: text areas, tooltips, cursor, map
icons, slider, list row heights, viewport clips (save-list crop), research
screen container, loading screens (bar + "LOADING" labels, pre-menu +
savegame popup), quit dialog (fade strips + yes/no popup), settings popups,
squad panel, sound-slider thumbs + click zones, corner panels (BR screenselect
quick-nav, BL starmap ship buttons, TL ALAP base buttons вЂ” anchored to the
16:9 corners via file-read interception; the TL panel's planet/jumphole name
TEXTS are gameplay-HUD texts, not starmap-related), the BR panel's back arrow
(fix 25 вЂ” its own mesh with the panel's original authored OBST, patched
+72 authored units in-buffer), the PAUSED/x SPEED top
labels (centered), the technology tree screen (edges + node frames + icons +
labels centered, node hover/click hit-test shifted to match), the progress
bars (research + planet bars centered, TR panel production/stat lines
right-anchored), the floating messages (left-aligned), the starmap
star-system labels (placed across the full 16:9 width, overlap-avoiding,
on-screen), the MP setup screen (player-table frames + 1px left border +
icons + text), the option dropdown popups (box + corner frames + rows
centered at the object level, unconfined across the 16:9 width, all
screens), the text-entry caret (chat box + player name field), the map
name-plate borders (untouched вЂ” world space), the portrait message window
(3D frame + speaker + text + count right-anchored, its buttons pick
correctly - fix 26 aligned the Accept/Decline clickable areas with the
rest), and the rotating ship display (bottom-left, left-anchored via
its scene container).

Resolution options = 3 widescreen modes (1920x1080, 2560x1440, 3840x2160;
4K lists and works, though some GPU-specific 4K cases are flaky). Registry
persistence, no 800x600 reset. Custom fonts: chrtool.exe builds/replaces
fonts at 1x/2x/4x from any system font; the hook is generalized to a
per-atlas scale. Crisp mouse-driven 3D-camera rotation (fix 23,
user-verified) and the main-menu version-text suffix (fix 24).

Also merged: the AI players 0-7 extension (skirmish/scenario setup) as the
[QoL] section, gated by hghook.ini.

Known limitations / pending:
- The floating-message fly "jerk" (0.5-design/frame y steps + ~1s
  re-anchors) is the game's own motion grid вЂ” intentionally NOT fixed.
- The planet production popup's FRAME is not drawn via the 471f6/4728e
  paths (renderer unknown); its rows get the general shift.
- QoL cosmetic fixes (yellow-selection follows the click; the editing row
  live-updates) are implemented but still pending user verification.

## User rules (CRITICAL)

- Warn and ask before anything resource-heavy or suspicious runs on the PC.
- User installs tools manually. User runs the game and reports visually.
- User reports visual results; ask precisely.

## Environment

- Game: C:\Program Files (x86)\Steam\steamapps\common\Haegemonia Legions of Iron
  (Steam, v1.10, D3D8 via dgVoodoo, 32-bit).
- Working folder: C:\Users\Nova\haegemonia-mod\ (Game.exe copy, sources, logs)
- Ghidra 12.1.4 at C:\ghidra_12.1.4_PUBLIC (JDK: Temurin 25 at
  C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot; run_*.bat set it).
- w64devkit GCC at C:\w64devkit\bin\gcc.exe (-m32).
- Ghidra headless quirks:
  * analyzeHeadless.bat breaks on paths with parentheses -> Game.exe copied
    to the working folder; project ghidra-proj\Haegemonia.
  * -postScript takes the FILE NAME; scripts print to files in the working dir.
  * Project addresses are VAs (image base 0x400000). RUNTIME RVAs = VA-0x400000.
  * Decompiler can return null on huge fns (147KB FUN_0069e24e!) - guard.
  * Run pattern: X.java + run_x.bat, run, read the report txt.

## Build, deploy, revert

- binkw32.dll = our proxy (forwards 85 Bink exports to bink_orig.dll).
  Source: hghook.c.
  Build: `gcc -m32 -shared -O2 hghook.c proxy.def -o binkw32.dll`
- BUILD NOTE: newer w64devkit GCC dead-store-eliminates statics used only
  from inline asm -> add `__attribute__((used))`. C helpers called from asm
  stubs need `used` + `noinline`.
- REVERT: delete binkw32.dll, rename bink_orig.dll -> binkw32.dll.
  Fonts revert: copy *.chr.bak1x over *.chr + restore GFX\Font\backup\ images.
- Current restore point: binkw32_v68_stable.dll + hghook.c
  (v68 = the current head; the message-scene misclassification fix.
  v67 added the message Accept/Decline pick override; v66 the
  back-arrow file patch; v65 the crisp-rotation fix 23 + the menu
  version-text suffix). Older binkw32_v*_stable.dll backups (incl.
  v67) exist in the working folder and in the game folder.
  chrtool_stable.exe = the font tool.
- The hook's crash filter (g_crash_filter) logs every unhandled exception
  (code/addr/regs/stack/ebp chain) to proj_log.txt.
- hghook.ini config (game folder): the mod reads four sections:
  * `[MapLabels] OverlappingLabels=0/1` (0 = labels avoid each other,
    1 = labels may overlap; the screen-bounds test always runs).
  * `[QoL] QoLFeaturesEnabled=1` (default 1 = AI 0-7 behavior enabled;
    0 = vanilla AI behavior, pair with the ORIGINAL Start.inf).
  * `[RotationSmooth] RotationSmoothEnabled=1` (default 1 = crisp mouse-driven
    3D camera rotation; 0 = vanilla), `RotationBoost=4.0` (animator approach
    speed multiplier; 1.0 = vanilla feel, higher = crisper),
    `RotationSens=1.0` (mouse sensitivity for the rotation; 1.0 = vanilla,
    lower = more mouse movement per degree; user's values 3.2/0.025).
  * `[Suffix] SuffixEnabled=1` (the extra text next to "Version 1.09"
    on the main menu), `SuffixText=widescreen-fix v1.0 by .Nx`,
    `SuffixYellowText=v1.0` (the part painted in the highlight color),
    `SuffixYellowColor=FFFFFFE0` (ARGB hex).

## Tweak constants (in hghook.c)

| Constant | Value | Meaning |
|---|---|---|
| g_shift | (W - 800*H/600)/2 | centering shift, screen px (240 @1080p) |
| g_pick_shift | W/(2*scale)-400 | same shift in design units (133.33 @16:9) |
| g_world_scale | 800/designW | full-16:9 world scale (0.75 @16:9) |
| g_tech_mouse_shift | = g_pick_shift | tech-tree hit-test mouse shift (design) |
| g_slide_shift_int | 133 | sound-slider click-zone shift (design) |
| LINE_FACTOR | 0.85 | line spacing multiplier |
| CURSOR_SCALE | 0.5 | cursor icon scale (origin-anchored) |
| ICON_SCALE_CENTER | 0.66 | map icons + slider scale (centered pivot) |
| MSG_SCENE_SHIFT | -75.7 | message-window 3D view shift (view units) |
| MSG_TEXT_EXTRA | 10.0 | message-window text extra px @1080p (scaled H/1080) |
| BR_PANEL_SHIFT | 218.05 | BR panel draw-time m[12] compensation |
| BACKARROW_SHIFT | 72.0 | back.bmod OBST patch, authored units (fix 25) |
| g_thresh | 800*scale*1.004 + 6 | 4K-safe text-batch threshold |
| g_rowh_factor | ROW_FACTOR*1.8/scaleY | list row pitch factor (4K-safe) |

designW = 600*W/H (1066.67 @1080p); scale = H/600 (1.8 @1080p).
screen px = design * W/designW. The v58 message fix has NO ini keys (hardcoded).

## The rendering pipeline (mental model)

- EVERYTHING 2D is authored in an 800x600 DESIGN space. The mod overrides
  designW (0xc0a790) to 600*W/H so authored-800 layouts map 1:1 onto
  800*H/600 screen px; then "centering" = adding g_shift to left-authored
  4:3 content.
- All 2D drawing funnels through choke points, each with our tracer + a
  per-draw owner (frame above the draw system):
  * 4728e + caller 0x38670..0x398D0 = text queue (glyph quads)
  * 4728e + caller 0x4ECD6..0x4F22A / 0x4F2D4 = icons/sprites (ecd6/efac)
  * 471f6 = plain quads (containers, strips, lines - n=2 = line!)
  Quads arriving in the hooks are in SCREEN px already (design * W/designW).
  EXCEPTIONS exist: some screens draw via 4728e callers OUTSIDE those
  ranges (the tech tree's icons/text = caller 0x3864F) - a diag must log
  ALL callers, never assume the three known ones.
- The game hit-tests with UN-shifted authored coords: any visual shift
  needs a matching interaction-layer fix (subtract the shift from the
  design mouse), see the picker/tech-tree/popup fixes below.

## Fix inventory

### 1. Core scaling + input
- designW override: 0xc0a790 (800.0f) -> 600*W/H (applied early + re-asserted),
  + .rdata layoutW 0xb316d4 -> same, layoutCenter 0xb3a848 -> half.
- SetTransform 4:3 safety correction (device vtable slot 37): type-3 matrix
  aspect in [1.32,1.35] + viewport aspect >1.34 -> m[0] *= mat_aspect/va.
- ClipCursor: full-height clips expanded to full screen width (kills the
  dead zone + cursor teleport). SetCursorPos clamped to screen.

### 2. Text centering (4728e hook, queue path)
Batches whose caller is 0x38670..0x398D0 with minx>=0 AND
maxx <= 800*H/600+5 shift by g_shift, UNLESS:
- tooltip popup (owner = FUN_00553274, 0x553274..0x553c20; the tooltip
  builder's box FUN_005400cf is never shifted), or
- owner (frame above the text funnel FUN_004ef060) inside FUN_00780fa1
  (0x780fa1..0x787958) = MAP LABELS (never shift), or
- owner in FUN_0077efc8 (0x77EFC8..0x780C82) with text-object origin
  x > 100 = the squad panel (game already positions it centered), or
- owner 0x52A188/0x52A1FD (popup rows via FUN_0052a0e0) вЂ” popup rows NEVER
  get the general shift (they draw at the object's shifted +0x88; the
  object shift = +133.33 design = the general shift amount, so other widget
  texts that DO shift land on the shifted box).

Per-subject rules that override/extend the general rule:
- Floating messages: origin exactly 10.0 (narrowed exclusion ox 5..20) ->
  moved back LEFT (not centered). Any other low-origin text from that
  renderer (the Morale at ox~0) stays centered.
- Message window (see fix 20): while a message is up, 136CDC-owner batches
  with design box x 520..820, y 250..570 get an extra +g_shift (total
  W-800*scale) + MSG_TEXT_EXTRA.
- PAUSED/x SPEED (see fix 11): squad_all owner + batch center in [320..480]
  design + miny < 200px -> dx = W/2 - batch_center.
- TR top bar (see fix 10): owners 0x77AE14..0x77D5A2 -> shift by
  W - 800*scale (right-anchor).
- Tech tree (see fix 12): owner range 0x7d527c..0x7df406 -> +g_shift.
- BL ship-list names (owner 0x77F162) and in-production icon (0x77F3CC):
  NO shift вЂ” the game projects them from the 3D scene, they follow the
  ship-scene view shift.

### 3. Clickable areas (strip picker)
FUN_00551ca5 = the widget strip picker (thiscall: strip, x, y;
row = y/b2e418; spans at *(*(strip+0x38)+4), entries {min u16, max u16,
id}; row structs at [strip+0x30] + row*0x18). BOTH call sites wrapped:
0x54F035 (pickA) + 0x53572B (pickB). patch_call_site keeps the E8 (only the
rel32 changes) -> wrappers are entered WITH the return address pushed
([esp]=ret, [esp+4]=arg1, [esp+8]=arg2).

The pick x is mapped per the ROW'S SPAN SIGNATURE (walked from the strip's
own data - no screen/owner guesses), pick_span_kind:
- kind 0 = menu strips (e.g. spans 49..777): x -= g_pick_shift (menus draw
  centered) - unchanged.
- kind 1 = single full-width span 0..800: x *= g_world_scale (0.75 @16:9) =
  the starmap HUD rows incl. TL/BL corner panels + the world map (full 16:9
  clickable).
- kind 2 = multi-span rows: TR top bar (y<92.3, rawx>=788.9 ->
  x = rawx-266.67 clamped; user-calibrated zone 1420..1920 px x 0..166 px);
  BR screenselect (y>=563.33 && rawx>=955.56 -> x = rawx-218.05 = the draw
  shift, clamped to 678.5..799.5; user-calibrated zone 1720..1920 px x
  1014..1080 px); everything else in kind 2 falls through to the world
  scale (map objects under the BR's old place stay selectable).
- kind 3 = portrait-message-window buttons (single span, fmin >= 500,
  fmax >= 780): rawx >= 816.67 -> x = rawx - 266.67 (the +240px visual
  move); below that the pick is rejected (the old centered place x
  ~1230..1660 px is dead).
- v67 STRIP-IDENTITY OVERRIDE: every pick whose `this` == the message
  screen's strip ([[msg+8]+0x38], fix 26) maps like kind 3 REGARDLESS
  of the row's span signature (the Accept/Decline row is multi-span
  and fell into kind 0).

Key facts: the corner-panel widgets are STRIPS whose spans are full-width
(0..800) вЂ” a plain pick shift pushes their zones into the centered box; the
ships are a DIFFERENT widget family (mesh picks via FUN_004a04fb/4a89f3/
4a0119) and were never the corner buttons; the game distinguishes the
TR/BR rows by their multi-span layout. The old whole-line expansion
(row-span clamp + 0..799.5 box clamp) is REMOVED.

### 4. Icons (4728e icon path)
Icon shift whitelist (ecd6 path):
- FUN_0077ae14 (empire-info top bar): W - 800*scale (right-anchor, see fix 10)
- FUN_0074d20c (planet icons): +g_shift
- FUN_005311b4 (scrollbar slider): +g_shift
- MP screen owners 0x7ce33c..0x7cef09 + 0x7c3968..0x7c4d7a: +g_shift
- World/map icons (FUN_00780fa1) and the cursor (FUN_0043518f): never shifted.

Icon size scale (4728e hook, exact call-site checks вЂ” no frame-walk ranges):
- cursor: rva 0x4EF9E + [g_saved_ebp+4] == 0x43520F (FUN_0043518f's ecd6
  call 0x43520A) -> origin-anchored x0.5 (tip stays at mouse).
- scrollbar slider: rva 0x4EF9E + ret 0x53137D (FUN_005311b4's ecd6 call
  0x531378) + rva 0x4F2D4 + [[g_saved_ebp]+4] == 0x53140F (44f648->efac
  path) -> per-call centered x0.66 + centering shift.
- MAP icons: rva 0x4EF9E + rets 0x780B24/0x782DF9 (FUN_00780fa1's ecd6
  calls 0x780B1F/0x782DF4) -> whole-icon centered x0.66: pivot = ecd6
  origin ([g_saved_ebp+8/+0xC] * scale) + half the atlas grid. THE ATLAS =
  *(g_saved_ebp - 0x54) (ecd6's prologue saves its ECX to [ebp-0x54]
  "89 4D AC"); fields [+0x14]=cellsPerRow, [+0x1c]=cellW, [+0x20]=cellH,
  [+0x30]=cellCount. Never shifted.
- g_saved_ebp IS the caller's ebp (the tracer E9-jumps in before 4728e's
  prologue runs); origin args = [g_saved_ebp+8/+0xC].
- DO NOT patch 0x44ECD6's entry or the 0x780B1F/0x782DF4 call sites: both
  regions are runtime-rewritten by the game (entry patches never fire;
  site hijacks receive a small ID as ECX and AV in ecd6).

### 5. Fonts (2x resolution support)
- FONT METRICS PATCH: per font object halve +0x18/+0x1c (image W/H ints)
  and +0x20/+0x24 (advance/lineH px floats) once; +0x1c/+0x24 also x0.85
  (tighter line spacing). This makes the game's OWN text measurements
  (map-label plates, tooltip boxes, backgrounds) 1x.
- Per-atlas scale S (in the hook): font_scale_for(imw) = imw/256
  ({256:1,512:2,1024:4,2048:8}, default 2); the FONT PATCH divides
  +0x18/+0x1c by S and +0x20/+0x24 by 1/S. Current game fonts are all 2x
  (512px).
- Line-height fix (font_line_h_for): per font +0x1c = (cellH-1)*imh/vspanA
  ('A' glyph v-rect from the glyph table at patch time) so lineH == quad
  height for ANY font.
- See "Reverse-engineered reference" for the .chr/atlas/font-object formats,
  and "CHR Font Tool" for chrtool.

### 6. Viewport clips, row heights, research screen
- VIEWPORT CLIP CENTERING: FUN_0044864e (renderer SetRect, vtable+0xa0,
  6-dword rect {x,y,w,h,zmin,zmax}) call sites
  (0x43CFF9/0x4474A2/0x517996/0x5208EE/0x7DDF79) wrapped: any
  non-fullscreen rect gets x += 240 (fixes the save-list text crop).
- ROW HEIGHT: FUN_004f21c2 returns [font+0x30] (cellH*scaleY used as design
  units); its 18 caller sites are retargeted to a ret-address trampoline
  scaling the return by ROW_FACTOR*1.8/scaleY (rows wrap text; cancels the
  scale at 4K).
- RESEARCH SCREEN: 471f6 quads with caller 0x541EF1 (widget renderer) and
  owner in FUN_0075bfa3 (0x75BFA3..0x75D517) get +g_shift (container +
  overlays).

### 7. Loading screen, quit dialog, yes/no popups, sliders
- LOADING: pre-menu bar quads (471f6 owners 0x317F05/0x317F7B) +
  "LOADING"/"SAVE"/"PROCESS" label images (ecd6 rets 0x317AA7/0x317FFD/
  0x318430/0x3184C4/0x31856B) shifted by +g_shift. The bg art + savegame
  popup bar are already centered by the layoutW override (never touch
  0x3176D7/0x317845/0x317EB2/0x3181A7/0x318228).
- QUIT DIALOG: FUN_007a5c71's two 800x75 fade strips (471f6 owners
  0x3A5F85/0x3A5FE7) extended to full screen width (right verts >1400 -> W).
- YES/NO POPUPS: ecd6 draws with ret 0x50F58D (+ 0x50F5E7; FUN_0050f465
  widget icons) and minx>=0 && maxx<=1445 shift +240 (the popup box +
  buttons; the sound-slider grooves share this path and shift correctly).
- SOUND SLIDERS: thumbs = ecd6 ret 0x50DCF2 -> +g_shift; click zone =
  inline patch at 0x50E3CE in FUN_0050e183 (event 5, mouse int at
  [ebp+0x14]) subtracting g_slide_shift_int (133 design) so
  index = ((mouse-133)-xmin-w/2)/step matches the visuals.

### 8. Resolution options + 4K
- IDirect3D8 vtable slots 6/7 replaced: GetAdapterModeCount -> 3,
  EnumAdapterModes -> {1920x1080, 2560x1440, 3840x2160}, X8R8G8B8/60Hz.
- The game's mode-list builder FUN_00447ad3 patched: caps 1600x1200 ->
  3840x2160 (0x447B66/0x447B73) + the 4:3-only aspect filter NOP'd
  (0x447BCF, 9 bytes).
- Startup registry validation: value outside the 3 -> rewritten to
  1920x1080 (kills the 800x600 reset). Registry stays the persistence
  (REG_SZ "ResolutionX"/"ResolutionY" under
  HKLM\Software\Digital Reality\Haegemonia).
- Direct3DCreate8 IAT patch runs FIRST in the worker (the game calls it
  before W/H are set).
- 4K FIXES: g_thresh = 800*scale*1.004 + 6 (proportional slack instead of
  +5px вЂ” elements stopped shifting at 4K). g_rowh_factor =
  ROW_FACTOR*1.8/scaleY (the list row pitch = [font+0x30] = cellH*scaleY px
  used as design units; a constant 0.66 doubled the design pitch at 4K).
  Both updated in design_width_override on mode change.

### 9. Corner panels (BR/BL/TL)
The three corner panels are 3D BMOD meshes; their 3D draw path is not
directly capturable (dgVoodoo handles the device draws, VB data unreadable),
so they are anchored via FILE-READ INTERCEPTION: CreateFileA/ReadFile IAT
hooks remember the panel BMOD handles and patch the authored floats in the
read buffer (the files on disk stay original). These are ONLY the 3D corner
BUTTON meshes вЂ” the TL panel's planet/jumphole name TEXTS are gameplay HUD
texts and are NOT part of this work. All user-verified at 1080p,
resolution-independent.

- BR = screenselect.BMOD: TRPS x 186.33 -> 331.67 (in-buffer). The panel
  sets its OWN type-0x100 screen-model transform each frame: m[12] = the
  OBST design x (= the file-patch value), m[13] = 0, m[14] = the OBST z
  (-104..-154), scale r[0] ~1.25. The game CULLS the panel when m[12]
  crosses ~333.33 (the 4:3 design box = 800.0 design; ~332.8 TRPS) вЂ” a
  binary cull (invisible, not clipped), so the file patch alone can never
  reach the 16:9 edge (+20px from 331.67 already culled it). FIX: the file
  patch stays at 331.67 (the cull never fires) and the matrix passed to
  the device gets m[12] += BR_PANEL_SHIFT (218.05, user-calibrated) on a
  COPY вЂ” the game's matrix memory is untouched. The FX stripe was already
  correct (anchored at W-7).
- BR FX stripe (2D): drawn in design space via FUN_00711c0a (line draw
  calls 471f6 at 0x711D46) from the panel renderer (runtime callers rva2
  0x2C4A2C/0x2C4A50), authored at design x 603-872 un-shifted. FIX: 471f6
  hook shifts only those draws by (W-7-793*s).
- BL = starmap_alap_nagy1..3.BMOD: OBST pos x float 0.002476811 minus
  91.05 (2.91 px/u -> cancels the 240px centering; left edge lands at 7px).
- TL = starmap_alap.BMOD / starmap_alap_select.BMOD: pos x 7.274771 minus
  84.78; pos z -0.007273 minus 2.06 = the vertical driver (y = 1.56 +
  3.125*z, target 8px). Pos.y must NOT be patched (it leaks into screen x
  via depth/perspective, ~-3.1 px/u).
- Calibration fits: BL x = 235 - 2.96*D; ALAP x = 235 - 2.96*D.
- BL panel "Max" capacity labels (queue owners 0x77FB2D/0x77FB87/0x77FBE1,
  inside the squad range so they skip the general shift) get +10 physical
  px right (10*(Hf/1080) game px).
- The BL squad-panel capacity triangles use the same line-draw path
  (owner 0x37F9A4) вЂ” left untouched (correct).

### 10. TR panel (empire top bar)
The empire top bar (FUN_0077ae14, owners 0x77AE14..0x77D5A2) anchors its
RIGHT edge (authored 800 design = the 4:3 bar) to the screen's right edge:
icons + text shifted by W - 800*scale in the 4728e hook (replaces the old
centered +g_shift for that range; the planet 0x74D20C + slider 0x5311B4
ranges keep the +g_shift). User-verified "perfect".

### 11. PAUSED / x SPEED + mission time
The top-center labels are drawn by the squad HUD FUN_0077efc8 via
"#fjob##w800##acenter#..." (game centers them in the 4:3 box = design
center 400; the squad exclusion left them there). Rule: squad_all owner +
batch center in [320..480] design + miny < 200px -> shift dx = W/2 -
batch_center. The bottom MISSION TIME / GAMETIME + "Talking to" status
line (owner 0x710B44, drained by FUN_0071092a's funnel at 0x710B3F; job
0xc327e8, drawn left-anchored at design (95,570)) gets the general +g_shift
and was LEFT AS IS (user preferred it; an anchor-next-to-corner-panel
variant was tried and reverted).

### 12. Technology tree
The TechTreeScreen (code 0x7d527c..0x7df406, ctor FUN_007d9e89, opened from
the research screen via FUN_007d51a0) draws its whole content in the 4:3
box un-shifted: tree edges = 471f6 n=2 line segments (owner 0x7D572C,
caller 0x142062), node frames/glow quads = 471f6 (owners 0x7D8xxx, callers
0x5E23A3/0x5E1C1A), node icons + name labels = 4728e caller 0x3864F (just
below the queue range вЂ” NOT the queue/ecd6 paths!). Fix: owner range
0x7d527c..0x7df406 -> +g_shift in BOTH the 471f6 and 4728e hooks.
Hit-test: FUN_007d7cb5 (node hover check) E9-hooked; it passes the design
mouse (c68030/c68034) to FUN_009e15bc (stdcall, RET 4!) as {x,y} with
ECX = **(node+0x10); the stub subtracts g_tech_mouse_shift (= g_pick_shift
= 133.33 design) from x.

### 13. Progress bars
The research + planet production bars are drawn as a HORIZONTAL LINE
(471f6 n=2, 351px @1080p) by FUN_00712755 (0x712755..0x712984, called from
the research fn FUN_0075bfa3 (sites 0x75cc76/0x75cd06/0x75cd96) + the
planet fn FUN_0074d20c (sites 0x750a58/0x750afc) + the map fn (0x785449))
-> +g_shift, EXCEPT the map caller (owner 0x780FA1..0x787958 - never
shift). The TR panel's production/stat lines = FUN_00712c5e
(0x712c5e..0x712da6, called ONLY from the TR fn FUN_0077ae14) which
DELEGATES to FUN_00711c0a -> the 471f6 caller is 0x711D46 and the frame
above it (owner) is FUN_00712c5e itself (NOT the TR fn!) -> rshift
(W - 800*scale, same as the TR icons/text).
LESSON: line draws chain helpers - the CALLER = the deepest helper's 471f6
call site, the OWNER = the intermediate fn one frame above it; neither may
equal the screen fn.

### 14. Floating messages + morale/cloud bars
- FLOATING MESSAGES: drawn by FUN_00536a0f (owner 0x536a0f..0x5374f3,
  origin x=10.0). The renderer is centered by DEFAULT; ONLY the flying
  messages (ox exactly 10.0) are moved back left via the narrowed
  exclusion ox 5..20 вЂ” any other low-origin text from that renderer (the
  Morale at ox~0) stays centered.
- THE FLY JERK (known, intentionally NOT fixed): the game steps each
  message y by 0.5 DESIGN units per FRAME (0.9px at 16:9) and re-anchors
  it DOWN ~1.8px every ~1s (re-anchor ticks coincide with the opacity
  update ticks). Every smoothing/suppression attempt fought or regressed,
  so no fly-jerk code ships. LESSON: when a motion "jerks" only after the
  16:9 patch, the game updates the position in DESIGN units on a coarse
  grid вЂ” the scale makes the steps visible.
- MORALE + CLOUD: the planet-widget vertical bars (3-quad IM draws via
  0xEBDBB, y 400..1200 вЂ” the morale bar + building slots) get +g_shift
  ONLY when the call chain goes through the planet screen fn FUN_0074d20c
  (0x74D20C..0x750E80); the world asteroid-cloud sprite shares the
  signature but renders from the game view вЂ” the chain check keeps it
  untouched.

### 15. Starmap labels
The star-system labels were confined to the 4:3 box because the label
placement grid is hardcoded to 800 design: FUN_0077ee70 (check, x-5/y-5/
w+10/h+10 margins, corners + step-4 cell sampling), FUN_0077ed6a
(unbounded mark), FUN_0077edde (bounded mark) all index obj+0x1140 as a
800x600 byte grid (stride 600, cell = 1 design unit; NEVER cleared вЂ”
placement is sticky, cells are re-marked each frame and labels re-place
every 300 frames via the counter at mapobj+0x1124). The label positions
live in the label objects at +0x94/+0x98 (design coords); the spiral
candidate = star + (cos,sin)*(60 + 10*rounds), angle step 0.1
(b39900=60, b2e570=10, b30ab0=0.1, b3ad78=6.283), label plate = 14
design tall (b3ab78).
Fix: E9-hook the 3 functions (thiscall+stdcall, ret 0x10) and scale x by
800/designW = 4H/3W in all grid accesses (integer math, identity at 4:3;
a uniform x-scale preserves the overlap relation; the 480000-byte buffer
keeps working). The stored/drawn positions stay in real design coords.
The check's bounds (x+w+5 < designW, y in [0,600)) enforce "labels stay
on screen". [MapLabels] OverlappingLabels=1 skips the occupancy sampling
(labels may overlap) but the bounds test always runs.

### 16. MP setup screen
- Player-row frames + separators: qlog_quad owner 0x7ce33c..0x7cef09.
- 1px LEFT border: qlog_quad caller 0x71A9E0..0x71AAA9 (FUN_0071a9e0,
  called via FUN_00711e94 sites 0x712732/0x712745) + chain guard: shift
  only if the MP fn (0x7CE33C..0x7CEF09) is in the frames above AND the
  map fn (0x780FA1..0x787958) is NOT.
- Icons: do_icon owners 0x7ce33c..0x7cef09 + 0x7c3968..0x7c4d7a
  (map-settings icons at design 535/600/675).
- The row TEXT already shifts via the general queue rule.

### 17. Option dropdown popups
The dropdown popup class is centered at the OBJECT level вЂ” the clamp call
site 0x525F18 in the renderer FUN_00525e9e is wrapped (E8 -> E9 stub) and
the object's x gets +g_pick_shift once per open, classified by mouse-x
> 100 (the popup opens at the authored combo position while the click is
at the centered position). All 3 elements (box, corner frames, rows) read
the object's +0x88/+0x8c -> they move together; no per-element draw rules.
Popup rows (owner 0x52A188/0x52A1FD) skip the general text shift; other
widget texts keep it. Re-open detected by the x change. The helper then
runs its OWN clamp: x+w vs designW (horizontal bounds REMOVED per the
user) + y+h vs b31a50 (the game's own). b31a54 stays 790!
- b31a54 must NEVER be patched past ~1000: an unknown runtime reader
  breaks the general text layout ("majority of text moved left").
- The clamp is a fastcall (ECX = obj, ret 0). ENTRY-HOOK TRAP: an E9 on
  FUN_00525d66's entry crashed at the resume address (the runtime bytes at
  0x525D6B differ from the static image) вЂ” wrap the CALL SITE instead and
  never jump into mid-function code in that region.
- FUN_0054e90f is a GENERIC widget update вЂ” it sits in the call chain of
  ALL widget-rendered texts (planet screen texts, MP texts) вЂ” NOT a popup
  discriminator.
- The popup rows mutate the shared text object c65d10 (origin/width) вЂ”
  texts drawn after a popup can inherit the shifted origin ("stale state"
  class of bugs).

### 18. Text-entry caret
The "|"-glyph caret is drawn via the non-queue renderer FUN_00437bb4 from
FUN_005074c9 (0x5074C9..0x507CF7 вЂ” the EditField draw, chat box + player
name field; class ctor FUN_005060ff, vtable 0xB3179C). It bypassed the
general shift -> left-aligned 4:3 box. Fix: non-queue path + owner in that
range + single-glyph signature (vcount 4..16, stride 0x1c) -> +g_shift.

### 19. Map name-plate borders (guard)
The MP 1px-left-border rule uses FUN_0071a9e0, which ALSO draws the map
name-plate left borders (world space). The rule therefore requires the MP
fn (0x7CE33C..0x7CEF09) in the frames above the helper and excludes the
map fn (0x780FA1..0x787958) вЂ” map name-plate borders stay untouched.

### 20. Portrait message window
The portrait message popup (cMessageScreen, singleton global 0xc6818c;
layout Inf\Prism\Messages.inf вЂ” Text 548,310,231,239, TextNum 657,262,22,18,
Accept 543,271, Decline 695,270, Ok 546,305,243,260, buttons 543/695,
Scene with Render_Screen_Model=1) is authored on the RIGHT side of the
800 design box (x 543..789). Two parts:
- 3D (frame message.bmod + speaker head + mesa): rendered by the
  Scene-widget path FUN_0052f0ff -> FUN_0047523a (scene renderer, thiscall
  ECX = the scene object). The view matrix = [[scene+0x40]+0x60], the
  screen-model slot = [scene+0x7c] (0 for the message scene), the proj =
  [[scene+0x40]+0x164] (perspective, fovY 45 deg, focal 1302.8px @1080p).
  Entry-hook FUN_0047523a (0x47523a, OUTSIDE the runtime-rewritten region,
  prologue 55 8B EC 81 EC E8 00 00 00 56 57, resume 0x475245): shift
  view[12] by MSG_SCENE_SHIFT (-75.7 view units = the +240px half-centering
  at ~3.17 px/unit; the window plate sits at view z ~ -11 behind the
  camera at (0,0,400)). ONLY the message scene may move: the empire-info
  3D + planet background share the SAME default camera (0,0,400) +
  sm==0, so per-scene TIMING classification: a scene seen while NO
  message is up (obj+0xe8 == 0) is never shifted (st=1); a scene first
  seen while a message is up = the message (st=2), shifted once + sticky
  (the view is constant per scene; re-applied only if the game rewrote
  it). v68 REFINEMENT (fix 27): only the FIRST default-camera scene
  ever seen with a message up gets st=2 - screens opened during a
  message register later and stay st=1. Trap: the C helper must be called with pushal/popal - ECX carries
  the scene object.
- TEXT (queue path, owner FUN_00536a0f 0x536a0f..0x5374f3): the message
  body + the "1/2" count are the 136CDC-owner batches whose design box =
  x 520..820, y 250..570 while a message is up -> extra +g_shift (total
  W-800*scale) + MSG_TEXT_EXTRA (10px @1080p, scaled by H/1080). The
  flying messages (msg_fly, ox 5..20) and the planet-screen texts
  (x<520) stay.
- BUTTONS (Ok/Accept/Decline): they pick through the strip picker as a
  single-span row 546..789 -> pick_span_kind 3 (see fix 3).
LESSONS: the device-vtable heap copy at dev+0x300 does NOT match this
dgVoodoo setup (entries not in the mapped-d3d8 range) - the game-level
scene hook is the reliable 3D route; entry-hooking a fn whose runtime
bytes differ crashes the game (FUN_0047523a is safe, FUN_00525d66 was
not); never classify scene widgets by camera alone - the default (0,0,400)
camera is shared by other screens.

### 21. Rotating ship display
The bottom-left rotating ship models (ALWAYS in-game: gameplay + starmap;
closed on planet/research screens) are the real world models
(gfx/inventions/*.BMOD, Starmap.inf [Squad1..3]) rendered as SCREEN MODELS
(device SetTransform slot 0x100) by the starmap HUD scene (view = the
remap matrix R{(-1,0,0),(0,0,1),(0,1,0)} + T(0,0,400); the models sit at
y~1195, x -780..-200, z -608..623 = the centered 4:3 box). THE FIX
("move the container, not the models"):
- scene_fix: the ship scene's view[12] = +293.4 units (240px at the
  ships' depth y_m+400 = 1595; px/unit = 1.3589*W/2/depth).
- my_SetTransform type 0x100, band x -1300..-100 / y 1100..1450 / |z|<800
  / |scale|<3, buckets = pos/32: models whose matrix CHANGES across frames
  (change count >= 3) = the ships -> their scene = g_ship_scene (sticky);
  STATIC models (seen >= 5, unchanged) = the corner buttons -> m2[12] +=
  g_shift*(m[13]+400)/(1.3589*W/2) on a matrix COPY (exact cancellation,
  any depth). NEVER mutate the game's matrix memory (caused meshes
  artifacting) - pass the modified copy to the device.
- The BL ship-list name (owner 0x77F162) + in-production icon (0x77F3CC)
  get NO shifts - the game projects them from the 3D scene, they follow
  the view shift (keeping an old -g_shift double-shifted them).
- Calibration: TL alap + BL nagy buttons ~25px further left via the file
  patches (alap 76.34->84.78, nagy 82.5->91.05); the BR panel
  (screenselect.BMOD, rendered at IDENTITY - no type-0x100 transform -
  so a view shift moves it ~957px left at depth 400) is compensated via
  the file patch 186.33->331.67 (+240px; the 4:3 box crop was the hard
  right limit) plus the draw-time BR_PANEL_SHIFT (fix 9).
LESSONS: scene-view shifts move EVERYTHING in the scene - compensate the
already-anchored parts with opposite per-model shifts (works because both
scale linearly in depth); classify by ROTATION (the ships spin, the
buttons don't); the BR has NO world transform (identity mesh) so a
per-model compensation can never reach it - use the file patch; the
mission view renders the same ship feature through the same persistent
scene.

### 22. QoL: AI players 0-7 (gated)
Expands the hardcoded AI-player count in the scenario/skirmish setup from
0-3 to 0-7 (the skirmish maps "Friends" and "Siege" define 8 empires).
Merged into the stable hook, diagnostics removed, only the behavior
patches + minimal install log lines remain. Requires the modded
Inf\Prism\Start.inf (8 [AiPlayersPopup] rows; = Start.inf.mod in the
working folder). Gate: hghook.ini [QoL] QoLFeaturesEnabled=1 (default 1;
0 = vanilla AI behavior, pair with the ORIGINAL Start.inf).
Verified: pick 0-7 works (no reset), AI row clicking works, team/race/
empire edits apply to the clicked row and survive the rebuild, an AI can
take the former player slot (empire 1), the player controls their empire
and all 7 AIs play. Implemented but pending user verification (cosmetic):
the yellow selection follows the click (nothing yellow until a row is
clicked), and the editing row above the table live-updates.
The shipped patch set (see the QoL addresses below): fixrow, addE bound 8,
aiStack clamps, hdr-live editing row, yellow-selection + auto-select
fixes, edit propagation, claim0/1/cap, start-force, row-based AIMARK at
launch, and the d1ef2 level-change flag. Full RE story + patch inventory:
AI_PLAYERS.md. Research archive: hghook_ai.c + binkw32_ai_diag.dll.

### 23. Crisp mouse rotation (3D world)
The in-game mouse-camera handler FUN_0078fb7e (0x78fb7e..0x790131) reads
the mouse driver (FUN_004f42fb +0x2c/+0x30) every frame, computes
whole-pixel deltas vs the stored position, and recomputes the camera
targets: pitch target = current(+0xa0) - dy, rotate target =
current(+0x9c) - dx, each handed to a target SETTER with the speed
constant 50.0: FUN_0065e00c (pitch, stores +0xd4/+0xd8/+0xdc) at call
site 0x790052, FUN_0065de73 (rotate, +0xc8/+0xcc/+0xd0) at 0x7900BA,
FUN_0065ddb4 (distance/zoom, +0x78FFF3). The per-frame ANIMATOR
FUN_0065b0e2 eases the current angles toward the targets (linear:
angle += (target-angle)*speed/b2ec18; inertial mode (+0x3c flag):
spring-damper FUN_0040c2a0 with velocity state; pitch clamped +-89
deg). THAT EASING = the vanilla "too smooth" (floaty, gliding)
rotation feel. FUN_0065ff59 = the camera variable REGISTRY mapping
script names (s_Pitch=+0xa0, s_Rotate=+0x9c, s_Distance=+0x98,
s_RotateTarget=+0xc8, s_RotateTargetSpeed=+0xd0,
s_MaxRotateSpeed=+0x80, s_LinearCameraSpeedMode=+0x3c, ...) to object
offsets - rotation feel is script-tunable.
FIX (user-verified): two parts, both gated by hghook.ini [RotationSmooth]:
- RotationBoost (crispness): wrap ONLY the handler's two setter call sites
  (E8 kept, rel32 -> stub; other setter callers = cutscenes/UI,
  untouched) and multiply the setter's SPEED argument (50.0) by
  RotationBoost - the animator approaches that much faster. Target
  accumulation, clamps and flags stay vanilla.
- RotationSens (sensitivity): applied at the handler ENTRY (0x78FB7E,
  prologue 55 8B EC 81 EC 8C 00 00 00): the stub scales the mouse
  driver position toward the battle's stored anchor (+0x90/+0x94)
  with ROUNDING (truncation adds a ~0.5px/frame directional bias =
  constant pitch drift toward one side), so the handler's own dx/dy
  computation sees RotationSens-scaled deltas. Skips zoom mode
  (battle+0x7647c != 0) and the pre-first-drag state (the camera's
  pitch target speed +0xdc is zeroed at camera init - until a rotation
  setter has run the game is not in mouse-rotation mode).
  User's final values: RotationBoost=3.2 / RotationSens=0.025.
KEY LESSONS: (1) the game's drag-rotate is SELF-REFERENTIAL - its
per-frame delta re-reads the camera's stored target, so scaling the
setter's target against any camera value decays the rotation
geometrically by (1-s) per frame -> "locks rotation". The sensitivity
can only be applied to the raw deltas at the source. (2) The handler's
INTERIOR must never be patched (mid-function stubs crashed the game);
the entry region is runtime-verified clean. (3) A startup crash during
this session turned out to be the user's edited
GFX\Interface\Ikonok\Multiplayer1\multiplayer_ikon.tga, not the hook.

### 24. Version-text suffix (main menu banner)
Draws the configurable extra text ("widescreen-fix v1.0 by .Nx", the
"v1.0" part in highlight color) right after the game's "Version 1.09"
line on the main menu. The version fn + text funnel are part of the
game's protected code (decrypt/re-encrypt around each call - patching
or calling it from outside crashes), so the suffix is drawn as RAW
GLYPH QUADS through the game's own 4728e draw, reusing the version
batch's saved 4728e args (same texture + state, only the verts pointer
changes): qlog_text captures the version batch by its authored position
(design ~(646, 582)), the font is found once by matching the 'V'
glyph's UVs against the text object's font vector (c65d10+8..+0xc),
glyph UVs come from the font's 0x14-float table, and the vertex layout
= {x,y,z,rhw,DIFFUSE,u,v} in strip order TL,TR,BR,BL with the diffuse
set per glyph. The nested batch skips every 4728e shift rule via the
g_suffix_draw flag. Config: [Suffix] SuffixEnabled/SuffixText/
SuffixYellowText/SuffixYellowColor. (Implementation in hghook.c.)

### 25. BR panel back arrow (back.bmod)
The back button visual on the planet/empire/research screens is NOT the
screens' own 3D frames (those are centered fine) and NOT a 471f6/4728e
draw (a position-filtered capture of both choke points found nothing at
the BackButton's authored box 678..795 x 569..591 except the BR panel's
own stripe lines). It is gfx/interface/screenselect/back.bmod - a
separate 1796-byte mesh loaded by the screenselect panel ctor
(FUN_0071aab0, string c287e8 "gfx/interface/screenselect/back.bmod")
with the panel's ORIGINAL authored OBST x float (186.3332 = 4A 55 3A
43, two copies - the file is doubled, two LODs) and the same OBST z
(-154.294). The panel's file patch (186.33 -> 331.67) never touched
back.bmod, so the arrow rendered ~240px left of the panel while the
click area (the kind-2 BR strip-pick zone 1720..1920 px) was already
fine - the user's "visual only" report.
The arrow renders in the SCREEN's own scene (default camera 0,0,400,
sm=0 - g_last_scene != g_ship_scene), NOT the ship scene: its
type-0x100 transform is m12=186.33/m13=0/m14=-154.29 with scale s0
~0.078 (1.25/16, the screen-scene convention), set by RUNTIME-GENERATED
code (the caller 0x562475C sits in a zero-in-the-static-image region -
no static decomp possible).
FAILED ROUTES (all verified):
- patching back.bmod's OBST to 331.67 (the panel's value) -> the arrow
  became INVISIBLE (the game's own cull fires there - the panel only
  survives at 331.67 by 1.66 units and the arrow's extent pushes it
  over),
- shifting the arrow's device matrix copy (m2[12] += 145.34 in
  my_SetTransform) -> NO visual movement at all (the screen scene
  positions its models from the LOADED OBST, not the device matrix;
  the per-model SetTransform is cull bookkeeping),
- VS-constant logging found no position constants (the position is not
  in the shader constants).
FIX: in-buffer OBST patch in my_ReadFile for back.bmod (path match:
"screenselect" folder + "back.bmod" filename): 186.3332 -> +72.0
authored units (user-calibrated 2026-10-19; the full authored alignment
145.34 overshot - the calibration landed at exactly half). The game's
cull never sees a value near its threshold, the draw follows the OBST.
LESSONS: a missing draw in BOTH choke-point logs can be a 3D mesh
rendered by a screen scene; screen-scene models can ignore the device
world matrix (CPU-baked positions) - test a small in-file OBST patch
to find what drives the draw; the "same widget on three screens" can
be one shared mesh loaded by a shared panel ctor.

### 26. Message-window Accept/Decline clickable areas
The portrait message's Accept/Decline clickable areas were at the
UNSHIFTED authored positions while the visuals are right-anchored
(clicking the Accept visual triggered Decline; Accept's own zone sat
further left). The message screen's own strip = [[msg+8]+0x38] (the
picker's ECX for the message hover/click checks: FUN_0072194b +
FUN_0053975a both go through FUN_0054f002 -> FUN_00551ca5 with
ECX = [widget+0x38]; for the message the widget = [msg+8]). Its rows:
the Ok row = single span 546..789 (pick_span_kind 3, right-anchored -
worked since v64) but the Accept/Decline row = MULTI-span (543..637 +
695..717) -> kind 0 (x -= g_pick_shift, the centered mapping) ->
un-shifted clicks. FIX (v67): the strip IDENTITY overrides the kind -
every pick whose `this` == msg_strip_get() maps right-anchored
(rawx >= 816.67 -> x = rawx - 266.67, else 2000 = dead; the old left
place is rejected, same as the kind-3 Ok rule). User-verified.
FAILED ROUTES (all verified):
- entry-hooking the handlers FUN_00723378 (accept) / FUN_007234a1
  (decline) / FUN_00722dd8 (ok) -> CRASH at the resume address
  (0x722DE3, AV): the whole 0x72xxxx message region is a runtime-
  PROTECTED region (alloc base 0x722000, 4MB, prot 0x20) - the game
  decrypts/re-encrypts the code around calls, so entry patches there
  are lethal (same family as the fix-24 version-fn trap).
- shifting the widget AREA objects ([widget+0x48], vtable 0xB31CC0,
  +4=w +8=h +0xC=x +0x10=y; Accept = 543/271 94x24, Decline =
  695/270 94x24, Ok = 546/305 243x260): the x += designW-800 patch
  applied but changed NOTHING - the area objects are NOT the hit-test
  source (the strip pick is).
- the widgets have NO strip at their own +0x38 (it is 0) - the strip
  lives at [[msg+8]+0x38].
The ACTION-handler log confirmed the design mouse tracks the OS
cursor exactly at click time (dm=(881.7,437.2) for a click at screen
(1587,787)).

### 27. Screen opened during a message (scene misclassification)
A screen (empire/planet/research) opened WHILE a message was up got
its 3D frame extra-shifted +240px right: fix 20's timing classification
(st=2) shifted ANY default-camera scene first seen with a message
active, and the screen's scene (default camera + sm==0, exactly like
the message's) was misclassified as the message scene. The owner
chains are IDENTICAL (0x54EB68 -> 0x53C110 -> 0x68EEC7: the widget
update + the generic screen dispatch - the message and the screens
share the chain), and the scene's model list does not contain
[msg+0xd8] (the entries are model NODES, not the bmod pointer), so
neither discriminates. FIX (v68): the FIRST default-camera scene ever
registered with a message active = the message window's own scene (it
renders the instant a message opens - before any screen can be
opened); any later registration while a message is active = a screen
-> st=1 (never shifted). The message scene object is the singleton's
persistent scene (one object for all messages - no re-registration
risk). User-verified (empire screen during a message stays put, the
message itself still shifts).

## Reverse-engineered reference

### .chr font format
- "INFO" + size(u32) + nameCount(u32) + per name (len+bytes) + 4x u32
  (cellW, cellH, firstChar, recordCount) + recordCount x 10 bytes
  (5 x u16: X, u0, v0, u1, v1; u/v = per-glyph pixel rects in the atlas,
  X = ATLAS PAGE INDEX - records are tight-packed, not gridded).
- Loader FUN_00435da3 (record loop at 0x436C4D): u/v divided by the image
  dims -> per-glyph table [font+0x4c] stride 0x14 floats {X,u0,v0,u1,v1}.
- Metrics: advance = (u1-u0)*[font+0x18], lineH = (v1-v0)*[font+0x1c],
  quad width = (u1-u0)*[font+0x20], quad height = cellH*scaleY (why a 2x
  .chr never doubled heights). FUN_004390fd recomputes +0x20/+0x24/+0x30
  from the ints on mode change -> patching the ints stays consistent.
- Advance formula: advance = (u1-u0)*[font+0x18] + b2e418, lineH =
  (v1-v0)*[font+0x1c] + b2e418; b2e418 = 1.0f (1px letter spacing).
  lineH - quadH = +1 always -> author v-span = (cellH-1)*S.

### Font object (100 bytes, FUN_00439580)
+0x18 imageW, +0x1c imageH (ints), +0x20 advance px, +0x24 lineH px,
+0x30 quad height, +0x34 firstChar, +0x38 cellH, +0x3c recordCount,
+0x40 cellW, +0x4c glyph table. Fonts enumerated via the vector at
c65d10+8..+0xc; entry = 0x14-byte slot {image, wrapper}; FONT = [wrapper+4].

### Atlas .tga
Type 2, 32bpp, desc=8 (bottom-up), straight BGRA (ink RGB 255, A=mask),
26-byte v2 footer optional. Atlas scale S = font_scale_for(imw) = imw/256.
chr_scale.ps1 scales u0,v0,u1,v1 x2 (X untouched); 1x backups *.chr.bak1x;
original images in GFX\Font\backup\. Re-run after restoring 1x files:
`powershell -File chr_scale.ps1 -Files (the .chr paths)`

### CHR Font Tool (chrtool.exe)
- Build: run_chrtool.bat (w64devkit, gcc -mwindows; needs stb_image.h +
  chrtool*.c/h in the working folder; copies chrtool.exe to the game
  folder). chrrt.c = .chr round-trip test.
- Open .chr -> viewer + per-glyph rect editor (click/drag corners/arrows)
  + header editor + live preview; BUILDER renders any system font into an
  atlas + .chr (1x/2x/4x). Export writes to the loaded font's folder with
  .bakMOD backups. Authored invariants: header cellW/cellH = design units,
  rects = atlas px, v-span = cellH*S, X = 0 (single atlas).
- Builder details: size/design px, bold/italic, padH/padV, cellH, scale
  1x/2x/4x, char range; grayscale-AA glyphs (ANTIALIASED_QUALITY into
  32bpp DIB, lum->alpha, straight BGRA), per-char advance via
  GetCharABCWidths, tight-packed grid with 1px gutters, single atlas
  256*S growing x2 to 4096 max.

### Popup class (0xe8-byte object, ctor FUN_0052388f 0x52388f..0x525bf4, vtable PTR_FUN_00b31a14)
+0x7c w, +0x88 x, +0x8c y, +0x90 h, +0xe1 DrawBorder, +0xe2 DrawBackground,
+0xe3 AutoWidth, +0xe4 "width computed" flag, +0x80 open flag,
+0xa4..0xa8 item list, +0xcc..0xd8 corner textures, +0x33..+0x36
szel*.tga strings. Corner textures = gfx/prism/popup/szel{bal,jobb,
alsojobb,alsobal}.tga.
- FUN_00525bf5 (0x525bf5..0x525cd8): AutoWidth width = max item width + 1
  (content-driven, NO 4:3 cap).
- FUN_00525cd9 = popup height (sum of row heights); FUN_00529e3a = item
  width measure; FUN_00529d60 = row height.
- FUN_00525d66 (0x525d66..0x525e9d) = the position clamp: AutoWidth
  x+w <= b31a54(790) / y+h <= b31a50(590); non-AutoWidth uses
  b316d4/b316d0. THE 4:3 CONFINEMENT = b31a54.
- FUN_00525e9e (0x525e9e..0x526ce7) = the renderer: box quad via
  FUN_0043ff26, 4 corner frames via FUN_0044ecd6, rows via FUN_0052a0e0
  (funnel rets 0x52A188/0x52A1FD). All 3 elements read +0x88/+0x8c.
  Called from the popup update FUN_0054e90f (0x54e90f..0x54f001) at
  0x525f18 (the wrapped clamp call).
- FUN_00526ce8 = the open fn (+0x80=1, +0x88=x, +0x8c=y); 7 call sites
  (combo/listbox/etc. widget events).
- cPrismPopupMenu = FUN_0054a8d0 (0x54a8d0..0x54dfdc).
- pchain facts: menu dropdown rows = chain [0x52A1FD, 0x5268E9, 0x54EB68,
  0x53C110]; 0x536CDC rows = chain [0x536CDC, 0x54EB68, 0x53C110,
  0x68EEC7]; popup box quads = owner 0x525FFB via caller 0x4400FB.

### cMessageScreen (singleton *(0xc6818c), 0x110 bytes, vtable 0xB3AB9C, ctor FUN_0071d214 0x71d214..0x71eb20)
+0xe8 = the message currently shown (0 = no popup), +0xd8 = the
message.bmod model, +0xec = mesa1, +0xdc/+0xe0/+0xe4 = Ok/Accept/Decline
widgets. Scene-widget draw = FUN_0052f0ff (vtable 0xB31ADC slot 13) ->
FUN_0047523a (call sites 0x52F118/0x52F15C).
- The widgets' AREA objects ([widget+0x48], vtable 0xB31CC0): +4=w,
  +8=h, +0xC=x, +0x10=y (Accept 543/271 94x24, Decline 695/270 94x24,
  Ok 546/305 243x260). NOT the hit-test source (shifting them changed
  nothing).
- The message's pick strip = [[msg+8]+0x38] (the FUN_0054f002 picker
  ECX for the message checks); its rows = the button spans (Ok single
  546..789, Accept/Decline multi-span 543..637 + 695..717).
- The message handlers FUN_00723378 (accept) / FUN_007234a1 (decline) /
  FUN_00722dd8 (ok) + the whole 0x72xxxx region sit in a runtime-
  PROTECTED 4MB alloc (base 0x722000, prot 0x20) - the game
  decrypts/re-encrypts it around calls; entry patches there CRASH
  (0x722DE3 AV).

### Strip picker internals
FUN_00551ca5(this,x,y): row = ftol(y / *(float*)0xb2e418); row struct at
[this+0x30] + row*0x18 (+0 = start list idx u16, +8 = list ptr); list
entries u32 {lo: spanIdx, hi: nextIdx}; span table = *(*(this+0x38)+4),
entries 8 bytes {min u16, max u16, id u32}. pickA (0x54F035) clamps x to
the row span range.

### Starmap label placement grid
FUN_0077ee70 check (0x77ee70..0x77efc7), FUN_0077ed6a unbounded mark
(0x77ed6a..0x77eddd), FUN_0077edde bounded mark (0x77edde..0x77ee6f);
call sites 0x78438e/0x78457b/0x7843e6/0x7845b5/0x782f34 in FUN_00780fa1.
Grid = mapobj+0x1140, 800x600 bytes, stride 600, never cleared; re-place
counter mapobj+0x1124 (every 300 frames); label positions at +0x94/+0x98.

### Scene renderer (FUN_0047523a, 0x47523a..0x475a81)
thiscall, ECX = scene object; view matrix = [[scene+0x40]+0x60] (UI
scenes = camera (0,0,400)); proj = [[scene+0x40]+0x164] (perspective,
fovY 45 deg; proj[0][0] = -1.3589 @16:9, proj[1][1] = 2.4142); screen
model slot = [scene+0x7c]. Entry prologue 55 8B EC 81 EC E8 00 00 00 56
57, resume 0x475245 (safe to entry-hook; OUTSIDE the runtime-rewritten
region).

### D3D8 draw path / device
- Device = DAT_00c59c4c; the d3d8 device vtable is a HEAP COPY at
  dev+0x300 whose entries point into a MANUALLY-MAPPED copy of d3d8.dll
  at 0x10000000 (invisible to module enumeration - find via VirtualQuery
  on exec pages). Game's device-call convention: `this` pushed ON THE
  STACK + ECX = the vtable.
- In-game only these fire: 71 = DrawPrimitive (map renderer via
  FUN_00447337, caller 0x73969), 72 = draw via the 471f6 tail (call
  0x447221, ret 0x47227), 73 = draw via the 4728e tail (call 0x4472CA,
  ret 0x472D0). No UP draws. Slot 70 (0x118, FUN_004473b7) never fires.
  Other mapped slots: 37 SetTransform, 40 SetViewport, 42 SetRenderTarget,
  61 SetTexture, 63 SetRenderState, 76 SetVertexShader, 79 SetVSConst,
  83 SetStreamSource-ish(stream,vb,40), 85 SetIndices(ib,0).
- Device-vtable slot traps are UNRELIABLE under this dgVoodoo setup -
  use the game-level hooks (4728e/471f6/47523a) instead. Calling device
  methods from inside a device trap crashes the game (user32 dispatch AV
  ~1-3s later) - the trap-time call path is unusable.
- The corner panels + rotating ships render via dgVoodoo DrawPrimitive
  with VB data unreadable - hence the file-read interception and
  SetTransform-copy approaches.

### Corner-panel BMOD values (file-read interception patches)
- BR screenselect.BMOD: TRPS x 186.33 -> 331.67; draw-time m[12] += 218.05
  on a matrix copy; game cull when m[12] crosses ~333.33 design.
- BR back.BMOD (back arrow, fix 25): OBST x 186.3332 -> +72.0 (same
  float pattern; the panel value 331.67 gets it culled - the arrow's
  cull has no headroom).
- BL starmap_alap_nagy1..3.BMOD: OBST pos x 0.002476811 - 91.05.
- TL starmap_alap.BMOD / starmap_alap_select.BMOD: pos x 7.274771 - 84.78;
  pos z -0.007273 - 2.06 (vertical; y = 1.56 + 3.125*z).
- cBMODLoader vtable base 0xB2F664: slot 14 = MESH parser FUN_0048cf81,
  slot 22 = MBOX FUN_0048be3d, slot 25 = BLST FUN_0048c5ca, slot 28 =
  OBST thunk FUN_0048cc8a -> slot 6 read-u32; the read stream has its OWN
  vtable. Do NOT E9-hook 0x48xxxx entries (game HANGS).

### Ship scene (screen-model transform)
SetTransform type 0x100 band: x -1300..-100 / y 1100..1450 / |z|<800 /
|scale|<3; buckets = pos/32; rotating models (matrix change count >= 3) =
the ships; static models (seen >= 5) = the corner buttons (compensated on
a matrix copy: m2[12] += g_shift*(m[13]+400)/(1.3589*W/2)). Scene view =
R{(-1,0,0),(0,0,1),(0,1,0)} + T(0,0,400); ships at y~1195, x -780..-200,
z -608..623; depth = y_m+400 = 1595; view[12] += 293.4.

### Camera (mouse rotation, fix 23)
- FUN_0078fb7e (0x78fb7e..0x790131) = in-game mouse-camera handler
  (callers: FUN_00692f7d @0x692F8B, FUN_006dd5a4 @0x6DD5B2, FUN_0076bc4a
  @0x76D09C). Camera object = [battle+0x54]; battle anchors (stored
  mouse pos) at battle+0x90/+0x94; rotation-vs-zoom mode battle+0x7647c.
- Setter call sites inside it: 0x790052 -> FUN_0065e00c (pitch),
  0x7900BA -> FUN_0065de73 (rotate), 0x78FFF3 -> FUN_0065ddb4 (zoom);
  all pass speed 0x42480000 = 50.0. Fix 23 wraps 0x790052 + 0x7900BA
  (E8 kept, rel32 -> boost stub) and the handler ENTRY 0x78FB7E
  (prologue 55 8B EC 81 EC 8C 00 00 00, resume 0x78FB87) for RotationSens.
  The handler's INTERIOR is NOT safe to patch (runtime bytes differ
  from the static image around 0x78fdaa+).
- Setters (thiscall, ret 8, args = target float + speed float):
  pitch +0xd4 target/+0xd8 flag/+0xdc speed, +0x70 delta/+0x74 dir(+-0.1)/
  +0x78 counter; rotate +0xc8/+0xcc/+0xd0, +0x7c/+0x80/+0x84; zoom
  +0xbc/+0xc0/+0xc4, +0x64/+0x68(+-1)/+0x6c.
- Current angles: +0x98 distance, +0x9c rotate, +0xa0 pitch. Animator
  FUN_0065b0e2 (0x65b0e2..0x65be3a) runs per frame (called from
  FUN_0065be76); linear mode = angle += (target-angle)*speed/b2ec18,
  inertial (+0x3c flag) = spring FUN_0040c2a0; pitch clamp +-89
  (25..89 in squad mode). Position update = FUN_0065be76 (0x65be76..0x65ce15,
  eases +0xf4..+0xfc toward +0xe0..+0xe8 targets, calls FUN_0065d1db to
  rebuild the view matrices via [obj+0x88]).
- FUN_0065ff59 = script-variable registry (name -> camera object offset):
  s_Pitch=+0xa0, s_Rotate=+0x9c, s_Distance=+0x98, s_RotateTarget=+0xc8,
  s_RotateTargetFlag=+0xcc, s_RotateTargetSpeed=+0xd0,
  s_MaxRotateSpeed=+0x80, s_CurRotateSpeed=+0x84, s_LinearCameraSpeedMode=
  +0x3c, s_ContinuedRotateMode=+0xb9, s_FollowModeRotationLock=+0xba.
- FUN_0065cea0 = camera init/position-to-angles (zeroes all the target
  state incl. +0xdc). Mouse driver: FUN_004f42fb (get, singleton
  DAT_00c65d54, +0x2c/+0x30 = x/y ints), FUN_004f4295 (set + design
  mouse c68030/c68034 + SetCursorPos), FUN_004f3ea5/4f3eb7 (capture
  flags). The handler recenters the driver to ftol(battle+0x90/0x94)
  after each apply (= the game's mouse-look snap).

### Scenario row model (QoL/AI)
- Row = 0x1fc bytes, vtable 0xB31E64 -> 0xB3249C: +0x08 name[0x80],
  +0x88 ready/flag byte, +0x8c slot idx, +0x90 TEAM, +0x94 race
  (0/1/2, -1=SELECTABLE), +0x98 AI id (-1 = human/none), +0x9c active,
  +0x120 cfg[0x18], +0x1b4..+0x1d0 cfg ints, +0x1cc AI COUNT (loop
  bound!), +0x1d4/+0x1e0/+0x1ec three aiStack triples (idx/team/race),
  +0x1f8 byte.
- Vtable 0xB3249C (510 slots): slot 2 = 0x570DB0 plain assign/copy (used
  by FUN_00827ff2 + FUN_0055561b), slot 3 = 0x56FDB0 (returns 7 = row
  type id), slot 5 = 0x570220 (deser), slot 6 = 0x5707D0 (ser), slot 8 =
  0x81F880 (slot reconciler: assigns an id=-1 row the first free slot;
  the ONLY 0x0081F880 dword in the exe is at 0xB324BC; called from the
  config command processor FUN_005605f0, ROWVAL caller 0x560900).
- Row list DAT_00c68604 (vtable 0xB3A4DC): [lst+8] = vector base,
  [lst+0x10] = vector count; plus STATIC rows of the mplayer.inf config
  DAT_00c66e98 ([cfg+8] list; static count = FUN_0056cd8d =
  [cfg+8]+0x20 - 1; MAXPLAYERNUM=8, JOINNUMBER=8; reader FUN_0056c3af;
  get/create FUN_0068fc30).
- FUN_00827fb0 (list count) = vector + static; FUN_00827fd5 (valid) =
  FUN_0068fc30 + FUN_0056cc03; FUN_00828ff4(pos) = pos < staticCount ?
  -1 : pos - staticCount (click pos -> vector index); FUN_00828b49 addE
  (remove by name digit, swap-remove); FUN_00828312 addR1 (append);
  FUN_00828663 addR2 (replace-by-name or append); FUN_007cb3f4 =
  per-event handler (8-loop 0x7cb6b5, addR2 loop 0x7cc3c2, addE loop
  0x7cc742, consume 0x7cc820).
- Scenario screen (Start.inf via Prism UI): widgets found by name via
  FUN_0054f4ee; FUN_007ace1b (screen builder) creates ids 0..11 per
  table; screen+0x50 = AiPlayersPopup; screen+0xab8 = selected AI count
  (written by FUN_007d30fc, store at 0x7d3129; -1 = reset); screen+0xabc
  = click selection; screen+0xab0 = name->obj map cfg (lookup
  FUN_0072df01). FUN_007d1ef2 = rebuild (has ret $4!). FUN_007bda04 =
  popup on-change for ALL scenario popups (case 7 = AiPlayersPopup ->
  +0x1cc; cases 9/10/11 = team/empire/race). FUN_007bebf4 = ready
  handler; FUN_007bf616 = name-field Enter handler.
- Map cfg value obj: [0]=players, [1]=teams, [0x38]=slot array; slot obj
  +8 = TEAM (0-based), +0xc = race (0=HUMAN,1=MARS,2=KARIAK;
  -1=SELECTABLE). FUN_007d2f84(screen,row) applies slot team/race;
  FUN_007d2e1a = vis check.
- The launch (FUN_007c5dc6 @ 0x7c5dc6..0x7c8539, called from 0x56b9b0):
  row loop (caller 0x7C69E6) copies each row into the template
  (FUN_00827ff2) and writes acStack[row.idx]=1 + config "Empire:
  row.idx"; "PlayerNum: %d" = FUN_00827fb0(); triple loop (0x7C6A57,
  bound = template+0x1cc); per-empire loop (state/controller writes);
  AI-marking loop (0x7C8347, bound = the AI count) вЂ” REPLACED by the
  row-based AIMARK stub (mark the empire iff a VECTOR row with id >= 0
  has idx == slot). Empire helpers: FUN_00667637(empire) = "state 2/3 =
  real"; FUN_006676ef(empire,v) = set state (+0x34 flag when v==3);
  FUN_00939090(ctrl,v)/FUN_00938fc8(ctrl,v) = controller type/aimode.
  The battle loader FUN_0066154e pairs rows with empires by name:
  "Empire"+(row.idx+1) vs "empireN", CASE-INSENSITIVE strcmp
  (FUN_00ac0b30 - string objects, not raw strcmp), then applies the
  row's race via "HUMAN"/"DARZOK"/"KARIAK" keys.

## Key addresses

All absolute VAs; runtime RVA = VA - 0x400000.

### Globals & data
| Address | What |
|---|---|
| 0xc0a790 | designW float (800.0) - overridden to 600*W/H, early + re-asserted |
| 0xc0a794 | 600.0f |
| 0xc5127c / 0xc51280 | W / H (ints) |
| 0xc51278 | frame counter-ish |
| 0xc512d0 | line-draw scratch matrix (cleared by FUN_00711c0a) |
| 0xc68030 / 0xc68034 | design mouse x/y |
| 0xc68064 | screen-state object (+8 = screen id; NOT a discriminator - 1 on every screen) |
| 0xc65d10 | current text object (+0x20 color-ish; +0x24/+0x28 design origin; +8..+0xc font vector) |
| 0xc6818c | cMessageScreen singleton |
| 0xc68274 | HUD state object (DAT_00c68274); squad lists +0x76440 (stride 0x10) / +0x7644c (stride 0x18); getter FUN_0076bb6e |
| 0xc59c4c | device (DAT_00c59c4c); dev+0x300 = heap vtable copy |
| 0xc0a84c / 0xc0a854 | "center" strings (job attribute parser) |
| 0xc286e0 / 0xc28730 | menu PAUSED/SPEED job strings |
| 0xc2d974 / 0xc2d9c4 | squad PAUSED job strings |
| 0xc327e8 | mission-time job string (submitted by FUN_007e5deb) |

### .rdata layout constants
| Address | Value | What |
|---|---|---|
| 0xb316d4 | 800.0 | layoutW (overridden with designW) |
| 0xb316d0 | 600.0 | layout height |
| 0xb3a848 | 400.0 | layoutCenter (overridden to half) |
| 0xb2e418 | 1.0 | letter spacing / picker row-height divisor |
| 0xb2e448 | 0.5 | strip-renderer half-width factor (x = layoutCenter - 0.5*w) |
| 0xb31a54 | 790 | popup x clamp (NEVER patch past ~1000 - breaks general text layout) |
| 0xb31a50 | 590 | popup y clamp |
| 0xb39900 | 60.0 | starmap spiral radius |
| 0xb2e570 | 10.0 | spiral step per wrap |
| 0xb30ab0 | 0.1 | spiral angle step |
| 0xb3ad78 | 6.283 | 2*PI wrap |
| 0xb3ab78 | 14.0 | label plate height (design) |
| 0xb3ad70 / 0xb3ad74 / 0xb3ad58 / 0xb3ad30 | - | squad-panel layout consts |
| 0xb2e480=3, 0xb314b4=5, 0xb367bc=15, 0xb3ab7c=254, 0xb3ab80=293, 0xb2e568=100 | - | runtime FPU consts |

### Text pipeline
| Address | What |
|---|---|
| FUN_00438670 | text queue (draw call 0x39037); queue caller range 0x38670..0x398D0 |
| FUN_004f014e | text layout |
| FUN_004f0a57 | text system |
| FUN_004ef060 | text funnel (68 call sites) |
| FUN_0044ecd6 | glyph/icon builder (4728e calls 0x4EF99, ret 0x4EF9E; saves its ECX (atlas) to [ebp-0x54] "89 4D AC") |
| FUN_0044efac | icon builder (call 0x4F2CF, ret 0x4F2D4) |
| FUN_0044f648 | icon wrapper |
| FUN_0044728e | indexed quad draw (stride 0x1c) |
| FUN_004471f6 | quad draw (stride 0x14) |
| FUN_00437bb4 | non-queue text renderer (caret path) |
| FUN_0043518f | cursor renderer (ecd6 call 0x43520A) |
| FUN_0044864e | renderer SetRect (vtable+0xa0, 6-dword rect); sites 0x43CFF9/0x4474A2/0x517996/0x5208EE/0x7DDF79 |
| FUN_004f21c2 | row height ([font+0x30]); 18 call sites retargeted |
| FUN_00435da3 | font loader (record loop 0x436C4D) |
| FUN_00439580 | font factory (100-byte font object) |
| FUN_004390fd | metric recompute on mode change (+0x20/+0x24/+0x30 from the ints) |
| FUN_0043ff26 | box quad (popup) |
| FUN_00447ad3 | mode-list builder (caps 0x447B66/0x447B73; aspect filter NOP 0x447BCF, 9 bytes) |
| 0x4bbf63 | aspect getter: rect at obj+0x134 ([+4]x1 [+8]y1 [+0xc]x2 [+0x10]y2 [+0x24]flag); flag==1 -> true screen aspect (native widescreen path exists, unused) |
| FUN_0047523a | scene renderer (0x47523a..0x475a81; prologue 55 8B EC 81 EC E8 00 00 00 56 57, resume 0x475245) |
| FUN_005400cf | widget renderer |
| FUN_00551ca5 | strip picker; call sites 0x54F035 (pickA), 0x53572B (pickB) |
| FUN_00536a0f | planet widget + message text (0x536a0f..0x5374f3) |
| FUN_0053975a | mouse update (tooltips) |
| FUN_00553274 | tooltip builder (0x553274..0x553c20) |
| FUN_005311b4 | scrollbar slider (0x5311b4..0x5317d5) |
| FUN_0052b36c | slider events |
| FUN_0050db50 | slider render (0x50DB50..0x50E182; class vtable 0xB317EC) |
| FUN_0050e183 | slider events (vtable+0x1c; event 5 = click/track at 0x50E3CE, mouse int [ebp+0x14]) |
| FUN_0050f465 | widget-icon renderer (ecd6 ret 0x50F58D) |
| FUN_00518bb1 | track handler (0x518BB1) |
| FUN_00690c9b | menu text layer |
| FUN_0069e24e | 147KB strategic dispatcher (menu+map+planet+credits) |
| FUN_006c3b4d | strip renderer (menu dispatcher only) |
| FUN_004f3512 | job attribute parser |
| FUN_004f31b0 | anchor object ctor |
| FUN_004a04fb / 4a89f3 / 4a0119 | ships' hover mesh picks (NOT the corner buttons) |

### Screen owners & helpers
| Address | What |
|---|---|
| FUN_00780fa1 | map labels + world/map icons (0x780fa1..0x787959); icon ecd6 calls 0x780B1F/0x782DF4 - NEVER shifted |
| FUN_0077ee70 | label placement check (0x77ee70..0x77efc7) |
| FUN_0077ed6a | unbounded mark (0x77ed6a..0x77eddd) |
| FUN_0077edde | bounded mark (0x77edde..0x77ee6f); call sites 0x78438e/0x78457b/0x7843e6/0x7845b5/0x782f34 |
| mapobj+0x1140 | 800x600 byte occupancy grid (stride 600) |
| mapobj+0x1124 | label re-place counter (every 300 frames) |
| label obj +0x94/+0x98 | label design position |
| FUN_0077efc8 | squad HUD (0x77efc8..0x780c82); squad buttons = 7-slot row at design x 134.6+67.9i, y 541.6; class buttons ecd6 call 0x77F3FB ret 0x77F400, origin (10-b2e480, 474+55*i-b3ad70) authored at LEFT edge |
| FUN_0077ae14 | empire-info top bar (0x77ae14..0x77d5a3) |
| FUN_0077d5a3 | top bar text |
| FUN_0074d20c | planet icons (0x74d20c..0x750e80) |
| FUN_00719f0a | in-game HUD text |
| FUN_00712755 | progress-bar line (0x712755..0x712984) |
| FUN_00712c5e | TR production/stat lines (0x712c5e..0x712da6) |
| FUN_00711c0a | design-line helper (calls 471f6 at 0x711D46) |
| FUN_00711e94 | line wrapper (calls FUN_0071a9e0 at 0x712732/0x712745) |
| FUN_0071a9e0 | design-line helper (0x71A9E0..0x71AAA9) - MP 1px border + map name-plates |
| FUN_0071092a | mission-time funnel (call 0x710B3F, owner 0x710B44) |
| FUN_0075bfa3 | research screen (0x75BFA3..0x75D517) |
| FUN_00714237 | research screen fn (opens the tech tree) |
| TechTreeScreen | code 0x7d527c..0x7df406; ctor FUN_007d9e89; factory FUN_007d51a0; nodes FUN_007d8fb4; edges FUN_007db62b; hover FUN_007d7cb5 -> FUN_009e15bc (stdcall RET 4, ECX = **(node+0x10)) |
| FUN_00717243 | loading init (0x717243..0x717BA5) |
| FUN_00717BA5 | loading frame (0x717BA5..0x7188D2) |
| FUN_007a5c71 | quit dialog (0x7A5C71..0x7A5FF7) fade strips |
| FUN_007ce33c + FUN_007cec54 | MP player rows (0x7ce33c..0x7cef09) |
| FUN_007c3968 | MP map-settings icons / AI table renderer (0x7c3968..0x7c4d7a) |
| FUN_0076bb6e | HUD getter |

### Popups / EditField / message window
| Address | What |
|---|---|
| FUN_0052388f | popup ctor (0x52388f..0x525bf4, vtable PTR_FUN_00b31a14, 0xe8 bytes) |
| FUN_00525bf5 | AutoWidth calc (0x525bf5..0x525cd8) |
| FUN_00525cd9 | popup height |
| FUN_00525d66 | position clamp (0x525d66..0x525e9d) |
| FUN_00525e9e | popup renderer (0x525e9e..0x526ce7) |
| FUN_00526ce8 | popup open (7 call sites) |
| FUN_0052a0e0 | popup row text (0x52A0E0..0x52A276; funnel rets 0x52A188/0x52A1FD) |
| 0x525F18 | the wrapped clamp CALL SITE (E8 -> E9 stub) |
| FUN_0054e90f | generic widget update (0x54e90f..0x54f001) |
| FUN_0054a8d0 | cPrismPopupMenu (0x54a8d0..0x54dfdc) |
| FUN_005060ff | EditField ctor (0xcc bytes, vtable 0xB3179C) |
| FUN_005074c9 | EditField draw (0x5074C9..0x507CF7) |
| FUN_0071d214 | cMessageScreen ctor (0x71d214..0x71eb20); vtable 0xB3AB9C |
| FUN_0052f0ff | Scene-widget draw (vtable 0xB31ADC slot 13); call sites 0x52F118/0x52F15C |

### Device (d3d8) slots
| Slot | What |
|---|---|
| 37 | SetTransform (4:3 safety correction here) |
| 40 | SetViewport (in-game only fullscreen + research clip {927,143,673,719}) |
| 42 | SetRenderTarget (2 surface ptrs; rotating targets = map texture double-buffer) |
| 61 | SetTexture |
| 63 | SetRenderState |
| 71 | DrawPrimitive (map renderer via FUN_00447337, caller 0x73969) |
| 72 | draw via 471f6 tail (call 0x447221, ret 0x47227) |
| 73 | draw via 4728e tail (call 0x4472CA, ret 0x472D0) |
| 70 (0x118, FUN_004473b7) | never fires |
| 76 / 79 / 83 / 85 | SetVertexShader / SetVSConst / SetStreamSource-ish(stream,vb,40) / SetIndices(ib,0) |

### QoL (AI) patch sites + data
| Address | What |
|---|---|
| 0x7cc609 | fixrow (row builder: idx/team/race from map cfg; preserve-by-position pidx>=0; +0x98=i; f1cc=0) |
| 0x7cc75e | addE loop bound = 8 |
| 0x7be37d | claim-loop cap (c >= 3 skips aiStack writes) |
| 0x7be194 | CLAIM0 (acStack[0]=0 reserve) |
| 0x7be415 | count recompute |
| 0x7c3c99 | hdr_live_stub (copies selected row's idx/team/race into template locals -0x1a0/-0x19c/-0x198; jump 0x7c3cf9) |
| 0x7beb90 | edit propagation (cases 9/10/11 write into clicked row; case 10 allows slot 0) |
| 0x7c8347 | AIMARK (row-based AI marking at launch) |
| 0x7c49ba | auto-select fix (jne -> jmp) |
| 0x7c4a3f | yellow-selection (yellow = click selection screen+0xabc == row.pos) |
| 0x7d3129 | d30fc store (screen+0xab8 = selected AI count) |
| DAT_00c68604 | scenario row list (vtable 0xB3A4DC) |
| DAT_00c66e98 | mplayer.inf config (MAXPLAYERNUM=8, JOINNUMBER=8) |
| FUN_007c5dc6 | launch (0x7c5dc6..0x7c8539, called from 0x56b9b0) |
| FUN_007d30fc | set selected count (thiscall, ECX=screen, arg=count/-1) |
| FUN_007d1ef2 | rebuild (ret $4!) |
| FUN_007bda04 | popup on-change (case 7/9/10/11) |
| FUN_007bebf4 / FUN_007bf616 | ready / name-Enter handlers |
| FUN_00667637 / FUN_006676ef | empire state check / set |
| FUN_00939090 / FUN_00938fc8 | controller type / aimode |
| FUN_0066154e | battle loader (pairs rows with empires by name) |
| FUN_00ac0b30 | case-insensitive string-object compare |
| FUN_00570db0 | row assign/copy |
| FUN_005605f0 | config command processor (ROWVAL caller 0x560900) |
| FUN_0055561b | valid refresh (template <- config entry 0) |
| FUN_0054f4ee | widget finder |
| FUN_007ace1b | scenario screen builder |
| FUN_0072df01 | map cfg lookup (screen+0xab0) |
| FUN_007d2f84 / FUN_007d2e1a | apply slot team/race / vis check |

## Owner-frame maps

### TEXT (frame above text funnel FUN_004ef060)
- 0x3D4870 (FUN_007d42f0): bottom text (version) - SHIFT
- 0x153ACA (FUN_00553cxx): main menu items - SHIFT
- 0x120D6E/0x120E57 (FUN_00520xxx): settings list - SHIFT
- 0x384EB0 (FUN_00780fa1): MAP LABELS - NEVER SHIFT
- 0x136CDC (FUN_00536a0f): planet screen text - SHIFT (+ message-window box rule)
- 0x37FE49 (FUN_0077efc8) / 0x37D56D (FUN_0077d5a3) / 0x31A0B4 (FUN_00719f0a): in-game HUD + top-bar empire TEXT - SHIFT
- 0x52A188/0x52A1FD: popup rows - NEVER general shift (draw at shifted +0x88)
- 0x77FB2D/0x77FB87/0x77FBE1: squad Max labels - +10 physical px right
- 0x77F162: BL ship-list names - NO shift (follow 3D scene)

### ICONS (4728e caller 0x4ECD6..0x4F22A)
- FUN_0077ae14: empire panel icons - right-shift W-800*scale
- FUN_0074d20c: planet icons - SHIFT (+g_shift)
- FUN_00780fa1 (world/map), FUN_0043518f (cursor): never shift (slider excepted)
- FUN_005311b4: slider - SHIFT + centered 0.66
- 0x7ce33c..0x7cef09 + 0x7c3968..0x7c4d7a: MP icons - SHIFT
- 0x77F3CC: BL in-production icon - NO shift (follow 3D scene)
- NOT a discriminator: screen id (c68064 obj +8 = 1 on every screen)

### QUADS (471f6) / special draw sites
- 0x317F05/0x317F7B: loading bar quads - +g_shift
- 0x317AA7/0x317FFD/0x318430/0x3184C4/0x31856B: loading labels (ecd6) - +g_shift; NEVER touch 0x3176D7/0x317845/0x317EB2/0x3181A7/0x318228
- 0x3A5F85/0x3A5FE7: quit fade strips - full screen width
- 0x50F58D (+0x50F5E7): yes/no popup widget icons - +240 when left-box
- 0x50DCF2: sound-slider thumb - +g_shift
- 0x2C4A2C/0x2C4A50 (caller 0x711D46): BR FX stripe - shift (W-7-793*s)
- caller 0x541EF1 + owner 0x75BFA3..0x75D517: research screen - +g_shift
- owner 0x7d527c..0x7df406: tech tree (edges + frames) - +g_shift
- owner 0x7ce33c..0x7cef09: MP row frames - +g_shift
- caller 0x71A9E0..0x71AAA9: MP 1px left border - +g_shift (chain-guarded)
- owner FUN_00712755: progress lines - +g_shift EXCEPT map owner
- caller 0x711D46 + owner FUN_00712c5e: TR stat lines - rshift W-800*scale
- 0xEBDBB: morale/planet bars (IM draws) - +g_shift via planet-screen chain check

## Hunting methodology (the "4:3 box" playbook)

1. FIND THE OWNER: locate the screen/function via string xrefs (RTTI names
   "XxxScreen@@", ".inf" filenames, label strings), decompile it to get its
   code RANGE. That range = the "owner range" for its draws.
2. DIAGNOSTIC BUILD: add a throttled logger (owner ^ caller ^ bounds dedupe
   key, ~10 lines/s max, 500-line budget) to qlog_quad and qlog_text
   filtering the owner range; log owner, caller, x/y bounds, vertex count.
   Ask the user to open the screen for ~10s. READ THE LOG:
   * n=2 = line segments (tree edges, FX stripes)
   * n=4 = one textured quad (frames, containers)
   * n=24..80 = text glyphs (queue) or icon cells (icon path)
   * positions: x in [0..1440]@1080p = un-shifted 4:3 -> needs +g_shift;
     x centered already = some other rule handles it (check before
     double-shifting).
3. WRITE THE RULE: owner-range -> add g_shift to x in the right hook. Keep
   ranges TIGHT (the version text fn sits below the tech range; the
   map-label range must never shift).
4. CHECK THE INTERACTION LAYER (the step everyone forgets): the game
   hit-tests with the UN-shifted authored coords. Find the screen's hit
   function = the one reading the design mouse (0xc68030/c68034) inside the
   owner range (xrefs from the range). Hook it and subtract the shift from
   the mouse x (design units: g_pick_shift = 133.33 design = the same
   centering). Verify hover+click with the user.
5. USER VERIFIES: visuals + hover + click, then STABLE: back up
   binkw32_vNN_stable.dll + hghook_vNNclean.c, update this file.

### The 4:3-box classification (what to do with ANY found element)
- Centered-in-4:3-box text (center ~400 design): +g_shift (or center
  exactly: dx = W/2 - batch_center) UNLESS owner is an exclusion.
- Left-authored design-space text/icons: +g_shift (general queue rule
  already does this for text; icons need an owner whitelist).
- Corner panel content (BR/BL/TL/TR): anchor to the 16:9 corner, NOT
  centered - file-read interception for the 3D meshes, owner-specific
  shifts for their text/icons.
- World/map space (FUN_00780fa1 map labels, nebula quads): NEVER shift.
- Tooltips (FUN_00553274): never shift (their box doesn't shift).
- The squad HUD (FUN_0077efc8, owner 0x77EFC8..0x780C82): excluded from
  the general shift on purpose; its own labels are fixed individually
  (Max labels stay +10px right, ship names follow the 3D, PAUSED centers).

## Hook-writing traps (all hit, do not repeat)

- NEVER jump into a hooked function's epilogue (MOV ESP,EBP...) without
  running its prologue first: EBP is the CALLER's frame -> silently wrong
  returns / stack corruption. Write a self-contained stub ending in `ret`
  (and clean any stack you pushed).
- CHECK THE CALLEE'S `ret n`: FUN_009e15bc is stdcall (RET 4) - hand-
  balancing the stack as if cdecl over-pops by 4 -> crash a few frames
  later with a float-looking pointer in a register. FUN_007d1ef2 also has
  ret $4.
- 32-bit thiscall/fastcall args: first arg = ECX. Double-deref patterns
  are common: FUN_009e15bc's object = **(node+0x10) (a pointer stored in
  a slot) - dereferencing once gives garbage fields.
- pusha slot order (verified in the compiled stub with objdump):
  [esp+0]=EDI, [esp+4]=ESI, [esp+8]=EBP, [esp+12]=ESP, [esp+16]=EBX,
  [esp+20]=EDX, [esp+24]=ECX, [esp+28]=EAX; retaddr at [esp+32], arg1 at
  [esp+36], arg2 [+40], arg3 [+44]. After N pushes the fixed offsets shift
  by 4*N. Reading "saved ecx" from [esp+4] gives ESI and stashing to
  [esp+0] overwrites EDI -> garbage results the game acts on.
- The runtime-rewritten region 0x43xxxx-0x4472xx must NOT be entry-patched
  (the game rewrites that code; E9-patching FUN_004471f6's entry crashes
  right after the intro movies). Use per-call-site wrappers or the
  4728e/471f6 hooks. Inline patches there must be re-asserted from a
  per-frame hook. Do NOT patch 0x44ECD6's entry or the 0x780B1F/0x782DF4
  call sites (site hijacks receive a small ID as ECX and AV).
- Never E9-hook 0x48xxxx entries (BMOD loader region - game HANGS).
- Entry hooks on functions whose runtime bytes differ from the static
  image crash at the resume address (FUN_00525d66/FUN_00525e9e).
  FUN_0047523a is safe. Never jump into mid-function code in such regions.
- The 0x72xxxx region (base 0x722000, 4MB) is a runtime-PROTECTED
  alloc (prot 0x20) - the game decrypts/re-encrypts the code around
  each call. Entry-patching the message handlers (0x723378/0x7234a1/
  0x722dd8) crashed at 0x722DE3. Use data patches or call-site wraps
  OUTSIDE the region for anything message-related.
- patch_call_site keeps the E8 (only the rel32 changes) -> wrappers are
  entered WITH the return address pushed ([esp]=ret, [esp+4]=arg1,
  [esp+8]=arg2). Tail-jump wrappers and wrong prologue-replay sizes crash.
- Patch sites must be at the instruction START; verify bytes against the
  "== ... bytes mismatch ==" log lines. Keep 5 bytes clear of 0x7befbe
  (FUN_007befbe's entry); never mid-function-hook 0x570220 (the game
  jumps into 0x57022d internally).
- Calling device methods from inside a device trap crashes the game
  (user32 dispatch AV ~1-3s later); trap nesting clobbers g_slot_tag.
- Reading game pages can AV (lazy-decrypt PAGE_NOACCESS pages) - all
  game-memory reads need VirtualQuery checks.
- ALWAYS pass a modified matrix COPY to the device - never mutate the
  game's matrix memory (caused mesh artifacting).
- statics used only from asm need `__attribute__((used))` (GCC dead-store
  elimination). C helpers called from asm stubs need used+noinline; use
  pushal/popal when the helper's args live in ECX (e.g. the scene object).
- b31a54 must never be patched past ~1000 (an unknown runtime reader
  breaks the whole general text layout).
- When adding an owner rule, keep the if/else-if chain intact - inserting
  a rule as a separate `if` re-attaches the general-shift `else if` to it
  (observed: "whole game text stretch"/double-shift).
- Verify an intercept site actually executes (a liveness log) BEFORE
  building on it.
- Diagnose with ONE variable changed per build; ask the user precisely
  ("where exactly, fraction of screen width/height").
- LINE DRAWS chain through helpers (FUN_00711c0a/0x711D46, FUN_00712755,
  FUN_00712c5e): the 471f6 CALLER = the deepest helper's call site, the
  OWNER = the intermediate fn one frame above it; neither may equal the
  screen fn. When an element refuses to appear in filtered logs, log
  EVERYTHING once (owner + caller + bounds, no filter) and classify
  afterwards - the "same widget" on three screens can use three different
  helpers.
- Some elements only draw while animating/active (progress fills, loading
  bars) - make sure the user has the element VISIBLE when capturing
  diagnostics.

## Files in the working folder

- hghook.c = the current source (v68, the message-scene classification
  fix + Accept/Decline pick override + back-arrow file patch + menu
  suffix banner);
  binkw32_v68_stable.dll = the current restore point. Older
  hghook_vNNclean.c + binkw32_v*_stable.dll backups are kept in the
  folder (incl. v67). hghook_v65refl.c = the reflections side-project
  fork (NOT part of the stable release).
- chrtool.h/.c(chr)/.c(gen)/.c(ui) + run_chrtool.bat = CHR Font Tool;
  chrtool_stable.exe = the stable tool build; stb_image.h = png/jpg/bmp
  loader; chrrt.c = .chr round-trip test; gentest.c/gditest.c = GDI
  measurement harnesses; chr_scale.ps1 = .chr x2 scaler.
- Start.inf.mod = the mod Start.inf (8 [AiPlayersPopup] rows) for the game
  folder.
- binkw32.dll + backups; proxy.def, stubs.inc, resolve.inc.
- Game.exe + ghidra-proj\; proj_log.txt (the hook truncates it at each game
  start; newest "hook DLL" line = newest session), *report.txt,
  *Dump.java + run_*.bat (Ghidra script pattern).
- OBSOLETE - ignore: hghook_v40bdiag..v45diag.c + binkw32_v40bdiag..
  v45diag.dll (an abandoned parallel session, buggy stubs); hghook_v42wip.c
  + binkw32_v42wip.dll (crashes - don't run); hghook_experimental.c (old
  k-transform WIP); hghook_v3.c (experiment line, reference only);
  hghook_ai.c + binkw32_ai_diag.dll + binkw32_baseline.dll + hghook_ai_v*.c
  + binkw32_ai_milestone_*.dll (the AI research archive, see AI_PLAYERS.md);
  hghook_v61diag.c + binkw32_v61diag.dll (BR-crop diagnostics archive).

## Build/test loop

1. Edit hghook.c -> gcc build -> copy binkw32.dll to the game folder
   (user asked: do it automatically) -> user runs game -> read proj_log.txt.
2. Static work: Java GhidraScript + run_*.bat -> report txt in the working
   folder.
3. Update this file after every finding. Restore point = hghook.c + backup
   DLL (CURRENT: binkw32_v68_stable.dll + hghook_v68clean.c).

## Related documents

- AI_PLAYERS.md - the AI 0-7 expansion: full reverse-engineering story +
  patch inventory (the merged [QoL] behavior is summarized above).
- MAP_SUBPROJECT.md - the custom multiplayer map project ("Nova" map,
  level.inf format, generator + validator, parser reverse-engineering).
- SCRIPTING_REFERENCE.md - the SCP script system reference (ScpDec,
  command catalog, XML anatomy).
