# AI Players 0-7 expansion — MERGED into the stable hook (v59, 2026-10-16)

Goal: expand the hardcoded AI-player count in the scenario/skirmish setup
from 0-3 to 0-7 (the skirmish maps "Friends" and "Siege" define 8 empires,
so 7 AI is possible data-wise).

## STATUS: MERGED (2026-10-16) — hghook.c v59 stable
- The whole behavior patch set (session-3 final + the two cosmetic fixes)
  is now part of the stable hook, in the "[QoL]" section of hghook.c.
- Gate: hghook.ini `[QoL] QoLFeaturesEnabled=1` (default 1 = enabled;
  0 = vanilla AI behavior — pair with the ORIGINAL Start.inf). The default
  ini template documents it.
- All research diagnostics (every logging hook, list/cfg dumps, the
  AI-specific crash dumps) were REMOVED for the merge; only the behavior
  patches + minimal "== ... patched ==" install lines remain. The diag
  source stays archived as hghook_ai.c (binkw32_ai_diag.dll).
- Restore points: binkw32_v59_stable.dll + hghook_v59clean.c (v58 + the
  AI extension). The mod's Start.inf.mod = the 8-row [AiPlayersPopup].
- The two cosmetic fixes are still pending user verification in-game
  (yellow follows the click; the editing row live-updates).

## SESSION 3 (2026-10-16, evening) — START FIXED, 0-7 AI PLAYABLE
- PROBLEM 1 SOLVED: the player controls their empire; all 7 AIs play.
- PROBLEM 2 SOLVED: an AI can take the former player slot (empire 1).
- The launch mechanics are now FULLY mapped (see the new sections below).
- COSMETIC LEFTOVERS SOLVED (same evening, second pass):
  - YELLOW FIXED. The previous "yellow fix" (0x7c49ba jne→jmp) only
    disabled the auto-SELECT (arrows), NOT the yellow. The REAL yellow =
    FUN_007ce33c param_5 = (row.id == header-template id). The header
    template is overwritten each frame by the config's FIRST entry (valid()
    = FUN_00827fd5 = FUN_0068fc30 + FUN_0056cc03 -> FUN_0055561b ->
    template.vt+8(configEntry0) = FUN_00570db0, caller 0x555670 verified in
    the ROWASSIGN log). The mod's rows carry id = i (fixrow), and the
    config's first entry carries id = 0 -> "AI ID 1" (id 0) was always
    yellow. NEW FIX (0x7c4a3f): the flag is now the CLICK selection
    (screen+0xabc == row.pos) - nothing is yellow until a row is clicked,
    then the clicked row gets yellow text + the arrows.
  - EDITING-ROW LIVE UPDATE FIXED (0x7c3c99, hdr_live_stub): the editing
    row above the table is drawn from the header template whose
    idx/team/race were read from the 3-triple aiStack (0x7c3c99/bb/dd) -
    skipped since milestone v2 -> permanently 0/0/0 (stale). The stub now
    copies the SELECTED vector row's idx/team/race (row = base +
    ab8*0x1fc; ab8 = the selected AI index set by the click handler via
    d30fc) into the template locals -0x1a0/-0x19c/-0x198, exactly the
    three fields the vanilla's triple reads wrote. The header name "AI ID
    %d" (ab8+1) was already live.
  - CORRECTION of the session-3 conclusion: the renderer (FUN_007c3968)
    has NO commit path back to the list - the header block only DRAWS
    (FUN_007d4e10 = template.team getter; FUN_007d4e30 = base(); the
    built display row is transient + inserted into a LOCAL team-sorted
    array). The earlier "wrong races + spectator mode" symptoms were the
    then-unfixed AIMARK/all-AI bug + the triple garbage, not a commit.
  - Both fixes are cosmetic-only (the yellow flag and the header template
    fields feed nothing but the drawing). Deployed in binkw32_ai_diag.dll;
    NOT yet user-verified.

### The launch, fully mapped (FUN_007c5dc6 @ 0x7c5dc6..0x7c8539, called
from 0x56b9b0, "LAUNCH" log line)
1. Row loop (caller 0x7C69E6): for each row (FUN_00827fb0 = static +
   vector) copies it into the template via FUN_00827ff2 and writes
   acStack[row.idx]=1 + config "Empire: row.idx". NOTE (corrected): the
   copy inside FUN_00827ff2 goes through the template's row vt+8 =
   FUN_00570db0 (plain assign); FUN_0081f880 = vt+0x20 (slot 8) is the
   SLOT RECONCILER (assigns an id=-1 row the first free slot), called
   from the config command processor FUN_005605f0 (ROWVAL caller
   0x560900) — the launch's "Empire: N" config writes trigger it.
2. "PlayerNum: %d" = FUN_00827fb0() = the TOTAL row count (the number of
   players; vanilla 3 AI = 4, mod 7 AI = 8).
3. Triple loop (0x7C6A57, bound = the template's +0x1cc = the AI count):
   acStack[aiStack[i]] = 1 + "Empire: 5" writes. OVER-READS for count>3
   (reads team/race values) — harmless for the claims.
4. Per-empire loop (the 8 map empires): valid → "%s %d" (empire name +
   num) + "AI mode: %d list: %s" (random) config writes; if
   acStack[empireIndex]==0 → FUN_006676ef(empire, 0) (dead/neutral);
   else Money/RP-limit/Mineral-Abundance/Technical-Level writes and, if
   [empire+0x5c]!=0, FUN_006676ef(empire, 3) (state 3 = real).
5. AI-marking loop (0x7C8347, bound = the AI count): per empire, if
   aiStack[i] == empireIndex → FUN_006676ef(empire,3) + "AI Diff Level"
   write + FUN_00939090(empire+0x30, 2) (controller type 2 = AI) +
   FUN_00938fc8(empire+0x30, aimode). THIS WAS THE BUG: with count>3 the
   loop reads the team/race values past the 3 idx triples and matches the
   HUMAN's empire (slot 0 matches team0=0/race0=0) → the player's empire
   became AI ("all AI"). The vanilla's count=3 matched only {1,2,3}.
   FIXED by REPLACING the whole loop with a row-based stub (AIMARK):
   mark the empire iff a VECTOR row with id>=0 has idx==slot. This also
   marks AIs 4-7 (the triple design only covered 3).
- Empire helpers: FUN_00667637(empire) = "state 2/3 = real";
  FUN_006676ef(empire,v) = set state (+0x34 flag when v==3);
  FUN_00939090(ctrl,v)/FUN_00938fc8(ctrl,v) = controller type/aimode.
- The battle loader FUN_0066154e (per empire) then reads the empires'
  config keys ("IsRealEmpire" etc. from Empires/*.inf), and pairs rows
  with empires by name: "Empire"+(row.idx+1) vs the empire's name
  "empireN" — CASE-INSENSITIVE strcmp (FUN_00ac0b30). Verified correct
  via SCMP logs. Applies the row's race via "HUMAN"/"DARZOK"/"KARIAK"
  keys.

### Fixes in session 3 (hghook_ai.c)
1. **AIMARK (0x7c8347)**: row-based AI marking in the launch (above).
2. **fixrow f1cc zero**: the mod's AI rows carried +0x1cc = the AI count
   (the claim loop wrote it into the command row and the rebuild
   propagated it); the vanilla's rows carry 0. The launch's triple loops
   use the LAST row's f1cc as the bound, so the mod's rows drove the
   over-read. fixrow now zeroes the built row's f1cc (the rebuild's loop
   bound is read before the loop, so the setup still works; the count
   stays in the command row).
3. **Problem-2 preserve**: fixrow preserve now allows pidx >= 0 so an
   empire edit to slot 0 survives the rebuild (was pidx >= 1).
4. **Yellow auto-select (0x7c49ba, 75 12 → EB 12)**: the renderer
   auto-selects the row with row.id == header-template id when
   screen+0xabc < 0 — the vanilla's rows had id=-1 (never fired), the
   mod's id=i made AI1 permanently selected (ARROWS). jne→jmp. NOTE: this
   does NOT fix the yellow TEXT — see the new 0x7c4a3f patch below.
5. **Panel/header live-fill (0x7c3c99, hdr_live_stub)**: the editing row
   above the table is drawn from the header template's idx/team/race, the
   three fields the vanilla reads from the 3-triple aiStack at
   0x7c3c99/0x7c3cbb/0x7c3cdd. The first attempt to fill them live was
   reverted on the suspicion that "the header block commits the template
   back by name" — that suspicion was WRONG (see the hook-lessons
   correction: the renderer has NO commit path; the observed "wrong races
   + spectator mode" were the then-unfixed AIMARK/all-AI bug + the triple
   garbage). The stub now copies the SELECTED vector row's idx/team/race
   (row = base + ab8*0x1fc) into the template locals -0x1a0/-0x19c/-0x198
   and jumps to 0x7c3cf9 (past the two remaining sites). Only those three
   fields are touched — the name/id are never modified, so the earlier
   cross-contamination cannot happen.
6. **BASELINE build**: `-DBASELINE` = logging-only DLL (no behavior
   patches) + the original Start.inf — used to capture the vanilla
   launch for comparison (PlayerNum=4, triples {1,2,3}, count=3, no
   over-read; the vanilla game "can control" confirmed).
7. **Yellow text (0x7c4a3f, second pass)**: the table's yellow text =
   FUN_007ce33c param_5 = (row.id == [-0x248]) where [-0x248] = the
   header template's id, which valid() refreshes from the config's FIRST
   entry every frame (FUN_0055561b -> template.vt+8(configEntry0) =
   FUN_00570db0 assign; ROWASSIGN caller 0x555670). The mod's config
   entry carries id 0 and the mod's rows carry id = i -> AI1 (id 0) was
   permanently yellow. The flag computation (0x7c4a3f..0x7c4a4e,
   `mov -0x248,eax; cmp -0x3b0,eax; sete cl; push ecx`) is replaced by
   a stub pushing (screen+0xabc == row.pos) — the CLICK selection, the
   same condition as the selection arrows. Cosmetic only.
8. **Editing-row live update (0x7c3c99, hdr_live_stub, second pass)**:
   the header template's idx/team/race were read from the 3-triple
   aiStack at 0x7c3c99/0x7c3cbb/0x7c3cdd (clamp stubs 2-4 = plain skips
   since session 3 -> stale 0/0/0). The stub now copies the SELECTED
   VECTOR row's fields: row = list.base + ab8*0x1fc, template.idx =
   row+0x8c, template.team = row+0x90, template.race = row+0x94 (locals
   -0x1a0/-0x19c/-0x198), then jumps past the two remaining sites to
   0x7c3cf9. ab8 = the selected AI index (the click handler stores it via
   d30fc) and apply_row_edit writes edits to the same row -> the header
   now live-updates. Only the three fields the vanilla's triple reads
   wrote are touched; name/id are never modified.

### Hook lessons (session 3)
- Prologue replays MUST resume exactly after the replayed bytes: the
  strcmp hook (0xac0b30, prologue 55 8B EC 57 56 53 = 6 bytes → resume
  0xac0b36) crashed the game instantly when resumed mid-instruction
  (0xac0b38). Same class of bug as the earlier 42a8ac resume (0x42a8b3).
- Patch sites must be at the instruction START: the ai3/AIMARK site is
  0x7c8347 (cmp) not 0x7c8346; the yellow fix is 0x7c49ba not 0x7c49bb.
  Always verify with the "== ... bytes mismatch ==" log lines.
- CORRECTED (second pass): the header template (0x7c3968's frame) is NOT
  committed back to the list — the renderer has no commit path (FUN_007d4e10
  = template.team getter, FUN_007d4e30 = base(); the display row is
  transient and inserted into a local team-sorted array). The valid()
  lookup only COPIES the config's first entry INTO the template
  (template.assign via row vt+8 = FUN_00570db0). Writing the template's
  idx/team/race from the selected row is therefore safe (hdr_live_stub).
- FUN_00ac0b30 = case-insensitive string-compare (objects), not raw
  strcmp — args are string objects.

## SESSION 2 verification (2026-10-16, after milestone v2) — old conclusions checked
- FUN_0081f880 is the ROW vtable 0xB3249C slot 8 (vt+0x20), NOT a
  registry callback (verified by exe pointer scan). See the OPEN section.
- The READY handler appends an id=-1 row itself — "no human row in the
  list" was WRONG. The launch moment is still unobserved.
- DAT_00c68604 = STATIC (mplayer.inf config) + VECTOR rows; the vector
  was empty during the whole last session while the table showed rows.
- Problem 2 (AI cannot take the former player slot 0) = two self-inflicted
  blockers in hghook_ai.c (fixrow preserve pidx>=1 + CLAIM0 slot-0
  reserve). See the Problem 2 section.
- The last-session log is from an unsaved experiment DLL (humanrow +
  ready-ep hooks) — current sources/DLLs do NOT contain that code.

## MILESTONE v2 (2026-10-16) — count pick + table + parameter edits WORK
- Saved as `binkw32_ai_milestone_v2.dll` + `hghook_ai_v2.c` (also deployed
  to the game folder as binkw32.dll).
- Pick 0-7 in the AiPlayersPopup: correct rows, correct count, NO reset.
- Clicking AI rows in the table: selects the row, parameter row above the
  table shows name/team/race, NO crash.
- Team/race/empire edits on a selected AI row: apply to exactly that row
  and survive the rebuild. Empire edit = any slot 0..players-1 (the former
  player slot allowed).
- WAS BROKEN in v2: game START made EVERY empire an AI (the player
  controlled nothing). RESOLVED in session 3 — see the RESOLVED section.

## Fixes in milestone v2 (hghook_ai.c)
1. **fixrow patch (0x7cc609)**: the addR2 row builder writes idx/team/race
   from the map cfg slots (fallback 0 for -1/SELECTABLE) — AND now
   PRESERVES the existing row's fields when a row with the same position
   exists (the popup edits live in the row). Also sets **+0x98 (AI id) = i**
   (0..6). `g_level_changed` (set by the d1ef2 flow hook) invalidates the
   preserve on level switches.
2. **addE loop bound (0x7cc75e)**: bound = constant 8 (remove rows
   count+1..8 after every pick) — was the root cause of the stuck table.
3. **claim-loop cap (0x7be37d)**: the case-7 slot claims for c >= 3 skip
   the triple-array writes (the aiStack holds only 3 triples; the writes
   overflowed the stack locals — the c=6 race write clobbered the sel
   local, which was THE pick-7 reset bug).
4. **claim-loop slot-0 reserve (CLAIM0 hook 0x7be194)**: acStack[0] = 0 —
   the human's slot never gets claimed by an AI (the original relied on
   the human row in the list for this).
5. **count recompute (0x7be415)**: the claim-loop end re-reads the popup
   sel (screen+idx -> widget+0x9c) instead of the clobbered local, forces
   the row's active +0x9c = 1.
6. **header aiStack-read skip (clamp stubs 2-4 = plain jumps to
   0x7c3cb5/0x7c3cd7/0x7c3cf9)**: FUN_007c3968's header no longer copies
   the aiStack triples into the row-template's idx/race/id fields — the
   template already holds the clicked row's own fields (copied by
   FUN_00827fd5). Reading the triples picked up the claim-loop garbage
   (race=3 -> job array[3]=NULL -> crash in FUN_0052a0cf).
7. **edit propagation (addR1 site 0x7beb90)**: FUN_007bda04 cases 9/10/11
   (team/empire/race popups) now write the edit into the clicked list row
   (position = screen+0xab8 = the game's AI index), set the command row's
   active=1 + count=current-cnt so the rebuild runs. Case 10 rejects only
   sel >= players (slot 0 IS allowed).

## RESOLVED: game start makes all empires AI (session 3)
- ROOT CAUSE (verified): the launch (FUN_007c5dc6) AI-marks empires by
  matching its 3-triple aiStack against empire indices with the loop
  bound = the AI count. With count>3 the loop reads the TEAM/RACE values
  past the 3 idx triples and matches the HUMAN's empire (slot 0 matches
  team0=0/race0=0) → the player's empire got the AI treatment
  (FUN_006676ef(empire,3) + controller type 2). The vanilla's count=3
  matched only {1,2,3}, so the human was never touched.
- FIX: the launch's AI-marking loop (0x7c8347) is replaced with a
  row-based stub (AIMARK): mark the empire iff a VECTOR row with
  id >= 0 has idx == slot. Covers all 7 AIs and never marks the human
  (the static row's id = -1).
- Supporting fix: the mod's AI rows carried +0x1cc = the AI count (the
  vanilla's carry 0) — fixrow now zeroes it, so the launch's triple
  loops (bound = the last row's f1cc) no longer over-read.
- FUN_0081f880 = row vtable 0xB3249C slot 8 (vt+0x8? NO: vt+0x20 — the
  dword 0x0081F880 appears exactly ONCE in Game.exe, at 0xB324BC). It is
  the row COPY/reconcile method (called by FUN_00827ff2's copy:
  template->vt+8(source)): builds the team-per-slot map and assigns an
  id=-1 row the first free slot; the launch's row loop therefore
  reassigns the human's row to a free slot when an AI occupies slot 0.
- Other verified pieces (kept for reference): the READY button
  (FUN_007bebf4) appends a row {ready=toggled, idx=0, team=0, race=0,
  id=-1, active=0, e8=1, count=0, name=""} via addR1 (log: LISTOP addR1
  ret=0x7BEF9F) — "no human row in the list" was wrong, but the ready
  row is dropped by rebuilds before launch and is NOT the human marker.
  FUN_007bf616 (name-field Enter) appends the human row {name, ready=0,
  idx=0, team=0, race=0, id=-1, active=0, e8=1, count=0} behind the
  valid() gate 0x7bf88d (NOPped); its tail FUN_00538461/FUN_00539483 is
  a global-object swap, not the launch.
- ROW MODEL (verified): DAT_00c68604 = list obj (vtable 0xB3A4DC).
  FUN_00827fb0 (count) = [lst+0x10] (VECTOR count) + FUN_0056cd8d()
  (STATIC count). Static rows = the mplayer.inf config object
  DAT_00c66e98 (created by FUN_0056c3af from "Game.inf/mplayer.inf",
  MAXPLAYERNUM=8, JOINNUMBER=8; get/create = FUN_0068fc30).
  FUN_0056cd8d = [cfg+8] ? ([cfg+8]+0x20 - 1) : -1. FUN_00828ff4(pos) =
  pos < staticCount ? -1 : pos - staticCount (click pos -> VECTOR index;
  -1 = a static row). Static count observed = 0 or 1 across sessions.
  The VECTOR is empty during level-select rendering; the setup's AI rows
  live in the VECTOR once picked.
- Do NOT patch within 5 bytes of 0x7befbe (FUN_007befbe's entry — a 5-byte
  E9 at 0x7befba overlaps it).

## Problem 2: cannot assign an AI to the former player empire (slot 0)
- RESOLVED (session 3): the fixrow preserve guard was `pidx >= 1` so an
  empire edit to slot 0 reverted on the next rebuild — now `pidx >= 0`.
  The launch's AIMARK then treats the slot-0 row as an AI (id >= 0), and
  the FUN_0081f880 copy-reconciler reassigns the human's static row to a
  free slot. Verified: AI on empire 1 + the player elsewhere = works.
- Earlier (milestone v2) notes — TWO self-inflicted blockers (both fixed):
  1. `apply_row_edit` case 10 (empire edit) allowed sel=0 and wrote
     row+0x8c = 0 — but `fix_row_fields`' preserve path required
     `pidx >= 1`, so the rebuild the edit itself triggers REVERTED the
     edit back to i+1. Fixed: `pidx >= 0`.
  2. The CLAIM0 hook (0x7be194) hard-reserves acStack[0] (slot 0) at
     claim time. In the original the claim loop skipped the HUMAN's slot
     because the human row was in the list at pick time; in the mod flow
     it is not. With the session-3 launch model this hook's launch impact
     is nil (the triples are ignored at launch — the AIMARK uses the
     rows), so it stays for the setup-time slot claims.
- Verified in-game: player chooses empire 5/6 (their row), an AI is set
  to empire 1 (slot 0) — the launch reassigns the human's static row to
  a free slot and marks slot 0 AI. The pairing "Empire"+(row.idx+1) vs
  the empire name "empireN" is CASE-INSENSITIVE and consistent.

## Other notes
- FUN_007bda04 = the popup on-change for ALL scenario popups
  (case 7 = AiPlayersPopup -> the count at +0x1cc; cases 9/10/11 = team/
  empire/race -> the aiStack triples in the original design).
- The rows live in TWO parts (verified): the VECTOR of list DAT_00c68604
  ([lst+8] base / [lst+0x10] count, 0x1fc bytes each; [lst+4] = 9 seen in
  the log) plus the STATIC rows of the mplayer.inf config DAT_00c66e98
  (FUN_0068fc30 = get/create; [cfg+8] = the static row list; static count
  = FUN_0056cd8d = [cfg+8]+0x20 - 1). FUN_00827fb0 (list count) = vector
  + static. FUN_0056cd8d/FUN_0056cc59/FUN_0056cc03/FUN_0056ccf3/
  FUN_0056cdb4 = config-object methods (lookup/count).
- FUN_00827fd5 (valid) = FUN_0068fc30 + FUN_0056cc03: "the template
  matches a config entry" — the gates in FUN_007bf616/FUN_007bebf4 test
  this, not row sanity.
- The row layout: +0x8 name[0x80], +0x88 ready/flag byte, +0x8c idx,
  +0x90 team, +0x94 race, +0x98 AI id (-1 = human/none), +0x9c active,
  +0x120.., +0x128 e8 (0=reset/1=set command), +0x134 name2?, +0x1b4..
  cfg ints, +0x1cc COUNT, +0x1d4/+0x1e0/+0x1ec three aiStack triples,
  +0x1f8 byte.

## MILESTONE v1 (2026-10-15 evening) — AI count 0-6 WORKS
- Saved as `binkw32_ai_milestone_v1.dll` + `hghook_ai_v1_milestone.c`
  (also copied into the game folder). Deployed as binkw32.dll.
- Pick 0-6: correct number of AI rows, teams/races from the MAP config.
- Remaining bug: **pick 7 resets the count to 0/1** (popup value drops).

## Fixes in milestone v1 (hghook_ai.c)
1. **fixrow patch (0x7cc609)**: the addR2 loop (row builder) now assigns
   idx = i+1, team = cfg slot[idx]+8, race = cfg slot[idx]+0xc (fallback
   0 when the slot is -1/SELECTABLE) instead of the stale aiStack triples
   in the list entries.
2. **addE loop bound (0x7cc75e)**: bound changed from the old broken
   `screen+0xab8`-based patch (never ran - the popup never writes 0xab8)
   to a constant 8: removes rows "AI ID count+1..8" after every pick.
   THIS was the root cause of the stuck table/phantom rows.
3. Race icon fix: race=-1 (SELECTABLE) -> 0 so the icon draw
   (screen+0x114[race]) gets a valid texture.

## Remaining problem: pick 7 resets to 0
- With the fixrow patch, the TABLE is correct for any count that arrives;
  but picking 7 makes the row's +0x1cc (the count field) become 0/1.
- Observed in the log: pick 7 -> addR2 loop bound = 1 -> addE removes
  2..8 -> table empties.
- The popup on-change handler has NOT been located yet. Known pieces:
  - The popup widget = screen+0x50 (AiPlayersPopup); its holder is
    registered at 0x7b28aa/0x7b2955 (FUN_007ace1b) via the widget table
    0x7BDA04 (id 7). The holder callback is NOT FUN_007befbe (that one
    is ParaScenarioButton9's, registered at 0x7b1634).
  - FUN_007befbe = the ROW-CLICK handler: reads screen+0xaf0/0xaf8 (row
    Y positions), FUN_00828ff4(row value) -> d30fc(count).
  - The row's +0x1cc = the AI count (drives the addR2/addE loop bounds).
  - The row serializes/deserializes (0x5707d0/0x570220, vt+24/vt+20)
    with full dwords - no 7-truncation there. Mid-function hooking of
    0x570220 CRASHES (game jumps into 0x57022d internally) - do not hook.
  - Next session: trace the popup's onchange via the widget table
    0x7BDA04 handler, or hook the popup's SetSel/GetSel vtable methods.

## Key facts (reverse-engineering notes)

### The scenario screen (Start.inf, loaded via Prism UI)
- Widgets are found by name via FUN_0054f4ee; FUN_007ace1b (screen builder)
  creates them with ids 0..11 per table (tables 0x7BD71D, 0x7BDA04,
  0x7B7CAF, all inside FUN_007ace1b's body).
- `[AiPlayersPopup] type=PopupMenu` rows = the `Text=` lines (0-7 with
  the mod; 0-3 originally).

### The AI count storage
- `[screen+0xab8]` = the selected AI count. Written by FUN_007d30fc
  (thiscall, ECX=screen, arg=count; also called with -1 = reset).
  Stored at 0x7d3129: `mov %ecx,0xab8(%eax)`.
  NOTE: in the skirmish flow the popup pick does NOT go through 0xab8 -
  the pick lands in the row's +0x1cc (the count the addR2/addE loops use).
- FUN_007d1ef2 (rebuild, called from FUN_007cb3f4 at 0x7CC007/0x7CC182)
  reads the count and rebuilds the row list (DAT_00c68604).
  NOTE: d1ef2 has `ret $4`; its callers push nothing (odd ABI).

### The row list
- DAT_00c68604 = the scenario row list obj (vtable 0xB3A4DC; [lst+4] = 9
  seen in the log). Rows = STATIC part + VECTOR part:
  - STATIC rows = the mplayer.inf config DAT_00c66e98 ([cfg+8] list;
    static count = FUN_0056cd8d = [cfg+8]+0x20 - 1; observed 0 or 1).
    FUN_0068fc30 = get/create the config; FUN_0056c3af = its mplayer.inf
    reader (MAXPLAYERNUM=8, JOINNUMBER=8).
  - VECTOR rows = [lst+8] base / [lst+0x10] count, 0x1fc bytes each
    (dynamically appended by addR1/addR2). The last-session log showed the
    VECTOR EMPTY for the whole session (base=0, count=0) — the visible
    table rows live elsewhere (static rows / widget-owned rows).
- addE = FUN_00828b49 (REMOVE the row whose name's last char = digit
  value; swap-remove), addR1 = FUN_00828312 (append), addR2 =
  FUN_00828663 (replace-by-name or append).
- valid = FUN_00827fd5 (FUN_0068fc30 + FUN_0056cc03: template-matches-
  config check), count = FUN_00827fb0 (VERIFIED: [lst+0x10] + static).
- FUN_00828ff4(pos) = pos < staticCount ? -1 : pos - staticCount (click
  pos -> VECTOR index; -1 = a static row).
- FUN_007cb3f4 (per-event handler):
  - 8-loop (0x7cb6b5): FUN_00827ff2(list, slot, &template) - copies list
    entry into the -0x20c template; per-entry race/team button handling.
  - addR2 loop (0x7cc3c2): i < template+0x1cc (the count from the entry)
    - builds rows "AI ID i+1" with idx/team/race from the template's
    +0x1d4/+0x1e0/+0x1ec triples (the aiStack arrays; zeroed each call).
    PATCHED (fixrow): idx/team/race from the map cfg slots instead.
  - addE loop (0x7cc742): continues the same counter i from count to
    the bound - removes rows "AI ID i+1". PATCHED: bound = 8.
  - 0x7cc820: consume block: count >= 0 -> d30fc(-1) + screen+0xabc=-1.
- FUN_007c3968 (table renderer) renders rows via FUN_007ce33c
  (row renderer: name draw screen+0xec[idx], race icon
  screen+0x114[race], flag screen+0x120[byte+0x88]).
  Force-render patch at 0x7c4711 (jne -> jmp).

### Row object layout (0x1fc bytes, vtable 0xb31e64 -> 0xb3249c)
- +0x08 name string ("AI ID N" for AI rows), +0x88 byte flag,
  +0x8c slot idx, +0x90 TEAM, +0x94 race (0/1/2, -1=SELECTABLE),
  +0x98 AI id (-1=none), +0x9c active byte, +0x120 cfg[0x18],
  +0x1b4..+0x1d0 cfg[0x1c..0x30], +0x1cc AI COUNT (loop bound!),
  +0x1d4/+0x1e0/+0x1ec three triples (idx/team/race aiStack),
  +0x1f8 byte.
- VTABLE 0xB3249C (510 slots; row methods): slot 2 = 0x570DB0 (the plain
  row ASSIGN/copy — used by FUN_00827ff2's copy and FUN_0055561b),
  slot 3 = 0x56FDB0 (returns 7 = the row type id), slot 5 = 0x570220
  (deser), slot 6 = 0x5707D0 (ser), slot 8 = 0x81F880 (the slot
  reconciler / human-slot assigner — VERIFIED by pointer scan: the only
  0x0081F880 dword in the exe is at 0xB324BC; called from the config
  command processor FUN_005605f0, ROWVAL caller 0x560900).

### The map config (screen+0xab0 = name->obj map; FUN_0072df01 lookup)
- cfg = map of the 10 MP levels: [cfg] = array of value ptrs,
  [cfg+8] = count. Value obj: [0]=players, [1]=teams, [0x38]=slot array,
  [0x18]=ptr, [0x1c..0x30]=ints, name string at [obj+8].
- Slot obj: +8 = TEAM (0-based from level.inf Team1..4), +0xc = race
  (0=HUMAN,1=MARS,2=KARIAK; -1=SELECTABLE).
- FUN_007d2f84(screen,row): row+0x94 = slot[row+0x8c]+0xc, row+0x90 =
  slot[row+0x8c]+8. FUN_007d2e1a = vis check (idx < players, team/race
  match slot or team < teams count).

### Level data
- Siege: Team1,Team1,Team2,Team2,Team3,Team3,Team4,Team4 (4x2).
  Friends: Team1x4, Team2x4. All empires Race=SELECTABLE.
- mplayer.inf: MAXPLAYERNUM=8, JOINNUMBER=8.

## Tooling / build
- Diag build: `hghook_ai.c` + `proxy.def` ->
  `gcc -m32 -shared -O2 hghook_ai.c proxy.def -o binkw32_ai_diag.dll`
  (deployed to the game folder as binkw32.dll; bink_orig.dll = real Bink).
- Baseline build (logging only, NO behavior patches — pair with the
  ORIGINAL Start.inf): `gcc -m32 -shared -O2 -DBASELINE hghook_ai.c
  proxy.def -o binkw32_baseline.dll`.
- Restore: copy binkw32_v58_stable.dll -> binkw32.dll, and
  Start.inf.bak_orig -> Inf\Prism\Start.inf.
- Log file: C:\Users\Nova\haegemonia-mod\proj_log.txt (per-run flush).
- Hook style: naked stubs. CRITICAL LESSON (re-learned many times):
  after `pushal`, saved regs are at [esp+0]=EDI,[+4]=ESI,[+8]=EBP,
  [+12]=ESP,[+16]=EBX,[+20]=EDX,[+24]=ECX,[+28]=EAX; retaddr at [+32],
  arg1 at [+36], arg2 [+40], arg3 [+44]. After N pushes the fixed
  offsets shift by 4*N.
  Also: the stub's jump-back address must be EXACTLY after the replayed
  prologue bytes (see session-3 hook lessons).

## Next steps
1. User-verify the merged v59 build in-game: (a) no row is yellow until
   clicked; the clicked row gets yellow text + arrows; (b) the editing row
   above the table live-updates (click an AI row, edit team/race/empire —
   the header row follows immediately).
2. Re-test the slot-0 AI scenario end-to-end (player empire 5/6 + AI on
   empire 1) and confirm the game camera follows the player.
3. Sanity-check QoLFeaturesEnabled=0 (hghook.ini) + the original
   Start.inf = vanilla 0-3 AI behavior, and confirm the widescreen fixes
   are unaffected.
   Do NOT mid-function-hook 0x570220 (game jumps into 0x57022d).

## Restore points
- v59 STABLE (the merge): hghook.c (=[QoL] section, diag-free) +
  binkw32_v59_stable.dll + hghook_v59clean.c. Deployed as binkw32.dll;
  Start.inf.mod = the mod Start.inf (8 popup rows) in the game folder.
- SESSION 3 final diag (research archive): hghook_ai.c +
  binkw32_ai_diag.dll: fixrow (idx=i+1 preserve>=0, f1cc=0, id=i), addE
  bound 8, claimcap, CLAIM0/CLAIM1, header live (hdr_live_stub 0x7c3c99)
  + skips for sites 3/4, AIMARK (0x7c8347), auto-select fix (0x7c49ba),
  yellow-selection fix (0x7c4a3f), edit propagation (7beb90),
  start-force, launch/cfgw/scmp/empire/gete-after/rowval diag hooks,
  noisy loggers silenced.
  BASELINE variant: `gcc ... -DBASELINE` -> binkw32_baseline.dll
  (logging only, no behavior patches — pair with the original Start.inf).
- Start.inf.mod = the MOD Start.inf (8 popup rows); Start.inf.bak_orig =
  the original (game folder copy). Baseline test used the original.
- MILESTONE v2: binkw32_ai_milestone_v2.dll + hghook_ai_v2.c (count pick
  + table + edits work; game start was broken — see RESOLVED section).
- MILESTONE v1: binkw32_ai_milestone_v1.dll + hghook_ai_v1_milestone.c
  (AI count 0-6 works, pick-7 broken).
- v58 stable DLL: binkw32_v58_stable.dll (pre-session behavior).
- The working dir keeps: hghook_ai.c, binkw32_ai_diag.dll,
  binkw32_baseline.dll, and the analysis scripts (scan_countidx.c,
  scan_calls.c, Ghidra scripts Ai*.java / run_ai*.bat and their reports,
  ailaunch2dec.txt, ailaunch3dec.txt, aicrashdec2.txt, aihdrdec.txt).
