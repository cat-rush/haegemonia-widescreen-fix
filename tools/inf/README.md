# InfTool — Haegemonia .inf unpacker/packer

Command-line tool for the game's `.inf` config format (auto-detects the
packed binary format, magic `0xFFFFA5AD`, and the text format; converts
both ways).

Build (w64devkit):

```
gcc -O2 inftool.c ttcore.c -o inftool.exe
```

`ttcore.c/h` is the shared .inf loader/saver core (also used by the SCP
editor in `../scp/`).
