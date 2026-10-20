# Haegemonia: Legions of Iron — Widescreen Fix (source)

!!! ALL IS MADE USING DEEPSEEK BY A PERSON WHO HAS NO CLUE IN PROGRAMMING !!!

Source repository of the widescreen mod for the 2002 classic *Haegemonia:
Legions of Iron* (Steam v1.10): true 16:9 at 1080p/1440p/4K without
stretching, 2x-resolution fonts, every UI element and clickable area
re-anchored for widescreen, crisp mouse camera rotation and an optional
8-player AI extension.

Everything here was produced by reverse-engineering the game binary — no
original game code or assets are redistributed, only this project's own
hook, tools and documentation.

## How it works

The mod is a **proxy DLL**: it ships as `binkw32.dll` and forwards the
game's Bink video calls to the renamed original (`bink_orig.dll`) while
hooking the game from `DllMain`. All fixes live in one file, `hghook.c`,
organized as:

- drawing choke-point hooks (text queue, glyph/icons, plain quads)
- clickable-area picker remapping
- file-read interception (3D corner panels, the back arrow)
- D3D8 device hooking (resolution list, scene-view shifts)
- font metric patching (per-atlas scale detection)
- gameplay QoL patches (AI players 0-7, mouse rotation)

## Building

Requires a 32-bit MinGW GCC (tested with [w64devkit](https://github.com/skeeto/w64devkit)):

```
gcc -m32 -shared -O2 hghook.c proxy.def -o binkw32.dll
```

or run `build.bat` (which also copies the result into the game folder).

## Repository layout

```
hghook.c            the entire mod - every fix, heavily commented
proxy.def           Bink export forwarding table
stubs.inc           assembler helpers used by the hook
resolve.inc         runtime address resolution helpers
Start.inf.mod       the modded Start.inf (8 AI-player rows, QoL feature)
hghook.ini          configuration example
docs/               project documentation
  HANDOFF.md          the project diary: every fix, all addresses, traps
  AI_PLAYERS.md       the AI 0-7 extension, full story + patch inventory
  SCRIPTING_REFERENCE.md   the bried guide how to understand .scp game scripts
tools/font/         CHR font tool (rebuilds/replaces .chr fonts at
                    1x/2x/4x from any system font) + font scaler
tools/inf/          InfTool - the .inf packer/unpacker (sources)
tools/scp/          ScpEditor - the small SCP mission-script editor
tools/scanners/     small C scanners used during reverse-engineering
re/scripts/         the Ghidra headless scripts (Java) + runners used to
                    decompile/disassemble every function documented in
                    HANDOFF.md - the research toolchain
assets/fonts/       the current .chr fonts + their atlas pages
screenshots/        the game running with the mod
```

## Rebuilding the fonts

`tools/font/run_chrtool.bat` builds `chrtool.exe` (needs `stb_image.h`,
w64devkit). Open a `.chr`, edit glyph rects or build a new font from any
system font at 1x/2x/4x.

## Reverse-engineering toolchain

`re/scripts/` contains ~500 Ghidra headless scripts (each `X.java` +
`run_X.bat` pair dumps/disassembles one function or region). Project
setup: Ghidra 12.x, `analyzeHeadless.bat` against a copy of `Game.exe`,
`-noanalysis`, `-postScript <name>`. `docs/HANDOFF.md` documents every
address, the mental model of the rendering pipeline, and the traps that
were hit along the way.

## Installation (end users)

1. Rename the game's original `binkw32.dll` to `bink_orig.dll`.
2. Copy `binkw32.dll` (built above) + `hghook.ini` next to `Game.exe`.
3. Copy `Start.inf.mod` over `Inf\Prism\Start.inf` (8-player setup).
4. Copy `assets/fonts/*` into `GFX\Font\`.
5. Pick 1920x1080 / 2560x1440 / 3840x2160 in the game's options.

Uninstall: delete `binkw32.dll` + `hghook.ini`, rename `bink_orig.dll`
back, restore the original `Start.inf` and fonts.

## Configuration

See the comments inside `hghook.ini` — sections `[MapLabels]`, `[QoL]`,
`[RotationSmooth]` and `[Suffix]`.

## License

[MIT](LICENSE)
