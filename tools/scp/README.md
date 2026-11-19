# ScpEditor

Native Win32 GUI editor for Haegemonia SCP mission scripts (tree view). Built with
w64devkit (GCC), no external dependencies.

## Build

    build.bat        -> scpeditor.exe

## Editing

- Middle: script tree. Shows groups (Commands), commands, parameters, conditions,
  Select/Switch, Include/CallScript/RunScript, Return and On* event triggers.
- Right panel: inspector for the selected node (name, lifetime, rem, comment,
  parameter type/value, condition expression).
- Toolbar: New, Open, Save (packed .scp - the game-safe format), Undo, Redo,
  Del, Up, Dn, Dup, Rem.
- Right-click the tree for an Insert menu (groups, conditions, selects, events...).
- Edit > Insert > Command... opens a picker of all 240+ known engine commands.

Keyboard: Ctrl+N/O/S file ops, Ctrl+Z/Y undo/redo, Del delete, Ctrl+D duplicate,
Ctrl+R toggle Rem, Alt+Up/Down move, F2 focus field.

## Format support

- Opens packed .scp (game format: u32 size + 78 DA + raw DEFLATE) and text .scp/.xml.
- **Save (Ctrl+S) writes PACKED .scp** - the game's mission loader is not
  reliable with text .scp files (a text file in the include chain breaks the
  whole mission script: timers/dialogues/tasks all stop working), so the
  packed format is the default. The game's own shipped scripts are all packed.
- The packer writes the full zlib stream INCLUDING the ADLER32 trailer -
  without it the game's zlib inflate rejects the file ("ZLib Error: ...")
  and the script fails to load. (Earlier builds omitted the trailer; that
  broke missions in BOTH text and packed form.)
- Attribute values are sanitized on save (NULs/control chars stripped) - an
  embedded NUL ("All\0") from the combo-box text read used to corrupt the
  XML and kill the game's script parser (the e5m5 mission bug).
- Included DEFLATE decoder reads all shipped campaign scripts.

## Notes

- Text nodes and attribute values round-trip without data loss (whitespace-only
  text is normalized).
- Undo keeps the last 100 snapshots.
- The game reads the editor state attributes (ScriptEditorExpanded etc.) but they
  are only cosmetic - new nodes get sensible defaults.
