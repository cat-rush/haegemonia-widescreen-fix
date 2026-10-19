# ScpEditor

Native Win32 GUI editor for Haegemonia SCP mission scripts. Replaces the original
ScriptEditor.exe workflow with plain building blocks. Built with w64devkit (GCC),
no external dependencies.

## Usage

- Left panel: command palette grouped by category. Double-click a command to insert it.
- Middle: script tree. Shows groups (Commands), commands, parameters, conditions,
  Select/Switch, Include/CallScript/RunScript, Return and On* event triggers.
- Right panel: inspector for the selected node (name, lifetime, rem, comment,
  parameter type/value, condition expression).
- Toolbar: New, Open, Save (text .scp), Pack (compressed .scp), Undo, Redo,
  Del, Up, Dn, Dup, Rem.
- Right-click the tree for an Insert menu (groups, conditions, selects, events...).
- Edit > Insert > Command... opens a picker of all 240+ known engine commands.

Keyboard: Ctrl+N/O/S/P file ops, Ctrl+Z/Y undo/redo, Del delete, Ctrl+D duplicate,
Ctrl+R toggle Rem, Alt+Up/Down move, F2 focus field.

## Format support

- Opens packed .scp (game format: u32 size + 78 DA + raw DEFLATE) and text .scp/.xml.
- Saves as text .scp (what ScriptEditor's normal save produces - the game loads it)
  or packed via the Pack button.
- Included DEFLATE decoder reads all shipped campaign scripts; the packer writes
  valid DEFLATE (stored blocks) accepted by the game.

## Notes

- Text nodes and attribute values round-trip without data loss (whitespace-only
  text is normalized).
- Undo keeps the last 100 snapshots.
- The game reads the editor state attributes (ScriptEditorExpanded etc.) but they
  are only cosmetic - new nodes get sensible defaults.

# Haegemonia SCP Scripting Reference

Reverse-engineered reference for the Haegemonia "SCP" mission/event script system.
Sources: shipped campaign scripts (decoded with ScpDec), the ScriptEditor.exe command
table, and the official Scripting.doc (Zoltan Gonda, Digital Reality, 2003).

## 1. What an SCP script is

An SCP file is the serialized form of Haegemonia's node-based scripting language
(Pascal-like semantics). Every mission/cutscene/random event is a tree of nodes:

- The game ships them **compressed**: `[u32 LE uncompressed size][78 DA][raw DEFLATE]`
  (decode with `ScpDec.java`; the editor's "packed" save = .scz, its normal save is
  plain text, so the game also accepts the plain XML text form).
- A level references its script from `level.inf`: `Script="Earth_1.scp"`.

## 2. XML anatomy

```
<Script>                                          root
  <Commands Rem="0" ...> LABEL                    group node; LABEL is a free text note
    <Command Lifetime="1" Rem="0" Comment="...">  leaf: script command
      NAME                                         command name (e.g. StartTimer)
      <Parameters ...>
        <Parameter Comment="..." Type="String" Value="&quot;MainTimer&quot;">
          ParamName</Parameter>
      </Parameters>
    </Command>
    <Conditions ...>                               condition block
      <Condition ReturnType="Number|Boolean">expr</Condition>
      ...
    </Conditions>
    <Select Lifetime="0" ...>                     iterator (see 5.3)
    <Switch ...>                                  branching (see 5.4)
    <Include Comment="..."/>                      text-includes another script file
    <CallScript .../> <RunScript .../>            subroutines / parallel scripts
    <Return .../>                                 ends script / subroutine
  </Commands>
</Script>
```

Attributes:
- `Lifetime` — how many times the node runs before being erased. `1` = run once
  (default), `0` = infinite (bold in editor, set with F11), `N` = N times.
- `Rem="0"` — commented out (F12 in editor, shown bold).
- `Comment`, `ScriptEditorExpanded`, `ScriptEditorSelected` — editor state, harmless.

### Node kinds (special structural nodes)

| Node | Meaning |
|---|---|
| `Script` | root |
| `Commands` | grouping node with a label |
| `Command` | single engine command |
| `Conditions` / `Condition` | condition block; see 5.2 |
| `Select` | iteration over entities (see 5.3) |
| `Switch` | like a case statement (see 5.4) |
| `Include` | textually embeds another script (shared variables) |
| `CallScript` | subroutine; `Return` returns to caller |
| `RunScript` | parallel independent script; MUST end with `Return` or it leaks |
| `Return` | ends current script/subroutine |
| `StepThru` | debug step marker |
| `InitVariables` | variable init block |
| `OnDeath` | block run when entity dies |
| `OnKeyPressed` | event trigger (NOT supported, per official doc) |
| `OnProductionFinished` | event trigger: production done |
| `OnDestinationReached` | event trigger: unit arrived |
| `OnJumpEnd`, `OnJumpStart`, `OnJumpToSignaller` | wormhole jump events |

Event triggers must be placed at the root of the tree.

## 3. Core semantics

- No real loops. Timed/conditional repetition via Lifetime=0 + Conditions.
- Commands execute top-to-bottom inside a `Commands` group.
- **Conditions on the same level are AND; conditions nested under each other are OR.**
- Variables are typed: Number, Float, Bool, String, VEC3 (+VEC4, FRGB/FRGBA/ARGB/color,
  Array, Anim, All).
- Variable prefixes seen in shipped scripts: `$name` (strings/values, e.g.
  `$SelectedSystemByRND`), `%name` (computed/flag vars, e.g. `%Failedmovement`),
  `^name` (booleans/flags, e.g. `^IcarusDead`).
- Values can be functions: `Value="Getpos(&quot;Venus&quot;)+[0-2000,2000,0]"`.

## 4. Condition expressions

Conditions are free-form expressions using the function library:
- `GetTimer("anim1")=18`, `GetTimer("x")>=10`
- `NOT IsMessageInPuffer("VC_1_1_3")`
- `IsSelected(...)`, `IsSigned(...)`, `IsDeployed(...)` etc. (see GET*/IS* below)
- Comparison operators: `=`, `<`, `>`, `<=`, `>=`, `<>`? and logic `NOT`, `AND`, `OR`.

## 5. Commands

### 5.1 Full engine command catalog (from ScriptEditor.exe tables)

Command names are case-insensitive in scripts; canonical form is UPPERCASE in the
editor. Parameters in parentheses.

**Heroes / XP / skills**
- `AddHeroSkill(skill, heroid)` — grant a skill to a hero
- `AddHeroXp(xpamount)`, `AddXP(entityid)` — grant experience
- `AssignHero(targetentityid)` — put a hero into an entity
- `MoveHeroPool` — move hero to/from pool
- `GenerateName` — random name from the name generator
- `FindHero`, `GetHeroLimit`, `GetHeroSkill`, `GetHeroType`, `GetHeroAssignedTo`,
  `GetHeroAssignedToType`, `GetMainHeroRank`, `HeroMessage` (accept/decline dialog)

**Money / population / research**
- `AddMoney(value, empireid)`, `SetMoney`, `GetMoney`, `SetContinousIncome`,
  `SetTraderCashInMission`, `GetTraderCashInMission`
- `AddPopulation(planetid)`, `SetPopulation`, `GetPopPlanet`, `GetPopSystem`,
  `SetPopGrowthPlanet`
- `AddResearchPoints(points)`, `SetResearchPoints`, `GetResearchPoints`,
  `GetSpentResearchPoints`, `GetResearchingInvention`, `CheatResearchInventions`,
  `SetInventionPercentage(inventionid)`, `SetInventionStatus(status)`,
  `GetInventionPercentage`, `GetInventionStatus`, `GetAccessibleInvention`

**Entities: create / destroy / damage**
- `CreateEntity(searchfreepos, herotype, upkeepmultiplier, visible, startrotangle,
  pathfinding, disableaicontrol, rank, needednrofships, startscale, startrot, startpos,
  raceid, name, baseid)` — the generic creator; BaseID = name of the inf in
  Game_Inf\Bases without extension
- `DeployEntity`, `DeployEntityInstant` — deploy station/base
- `DeployDrone`, `DeployMine` — special deployables
- `Destruct` (param: Detonation bool) — destroy entity
- `DamageEntity(percent)`, `ExplosionDamage`
- `Damaged`, `Destroyed`, `Detected`, `AllDestroyed`, `AllUnitDestroyed`,
  `AnyDetected`, `TypeDestroyed` — event/reaction predicates
- `MakeActive`, `MakePassive`, `MakeInvulOn`, `MakeInvulOff`
- `SetEntityName`, `SetEntityOwner`, `SetVisibleOn/Off`, `SetHiddenOn/Off`,
  `SetWorldOn/Off`, `EntitySetSignedOn/Off`, `StrandedEntityOn/Off`,
  `SetPathfindingOn/Off`, `SetTracktorBeamOn/Off`, `SetEntityGuardByAIOn/Off`,
  `SetSkyWalkerEffekt(weaponid)`
- `SaveEntity(entityname)`, `SaveSystem`, `SaveVariable`, `LoadSystem(slotnumber,
  systemname)`, `LoadVariable(variable)`, `SetGlobal` — persistence across missions
  (campaign carry-over)

**Movement / AI**
- `MoveEntity`, `MoveEntityInstant`, `MoveEntityRelative` (param: EntityID, Position
  VEC3)
- `AttackEntity`, `AttackEnemyIn`, `Follow`, `Find`, `FindShip`
- `AIAddPatrolPos`, `AISetMode(mode)`, `AISetDistance`, `AIControlOn/Off`,
  `EmpireAIControlOn/Off`, `StopEntity`, `MineAsteroid`, `StartSpying(action)`
- `GetShipSpeed`, `GetPos`, `GetRotEntity`, `GetPitchEntity`, `GetEntityType`,
  `GetEntityName`, `GetEntityOwner`, `GetEntitySystem`, `GetSquadNumber`,
  `GetCurrentHP`, `GetCurrentHPPercent`, `GetUpKeep`, `IsInBattle`, `IsTravelling`,
  `IsTravellingIn`, `IsDeployed`, `IsSigned`, `IsSignedAll`, `IsSelected`,
  `IsUnderProduction`, `IsProductable`, `IsRealEmpire`

**Empires / diplomacy / planets**
- `SetEmpireRelationAlly/Neutral/War`, `GetEmpireRelation(targetempireid)`,
  `GetPlayerEmpireID`, `IsPlayerEmpire`, `IsMultiplayer`, `GetCampaignMode`,
  `GetLevelNr`, `GetDifficultyLevel`, `GetTechLevel`, `GetAllColonies`,
  `GetSystemColonies`, `GetLargestColony`, `GetSmallestColony`,
  `GetStolenTechnologiesNumber`
- `SetPlanetOwner`, `SetPlanetSurface(surface)`, `SetMineAbundance`,
  `GetOreContent`, `GetOreMined`, `GetPlanetRadius`, `SetProdBonusPlanet`,
  `GetProductionCapacity`, `Terraform`, `ReverseTerraform`, `CanTerraform`,
  `CanReverseTerraform`, `Colonize`, `PopTransport`
- `GetMorPlanet` (morale), `SetMoral`

**Systems / wormholes / jumping**
- `SetActSolarSystem(systemid)`, `GetActSolarSystem`, `Jump`, `ReconSystem
  (targetsystemid)`, `EnableWormhole`, `DisableWormhole(wormholeid)`,
  `SetWormholeFast/Normal/Slow`, `IsEnableWormhole`

**Timers / flow control**
- `StartTimer(maxvalue)` — timers auto-increment each cycle
- `StopTimer`, `GetTimer(timerid)`, `GetTimerRemaining`
- `Pause`, `Random`, `RndSelect`, `RndSelectInit`, `GetRndEventProbability`,
  `Jump` (jump to a `Switch` target), `RestartLevel`, `SetNextScenario(scenarioid)`

**Messages / objectives / UI**
- `Message`, `GeneralMessage(param0..param9)`, `HeroMessage`
- `ChangeObjective(messageid)`, `SetMusicLevel(level)`, `Beep`,
  `EnableDemoModeLogos`, `IsMessageInPuffer`, `IsScreenActive`
- `Win`, `Lose(message)`, `Win2` (secondary win path)

**Camera (cinematics)**
- `CameraPlay(filename)`, `CameraPlayK`, `CameraRec` (record/playback camera data
  from Cameras\ folder)
- `CameraPosSet(+Instant/Relative/RelativeInstant)`, `CameraFollowPos(+Instant)`,
  `CameraFollowRot(rotate)`, `CameraSetDist(+Instant)`, `CameraSetPitch(pitch,
  +Instant)`, `CameraSetRot(+Instant)`, `CameraSpeedRot(speed)`, `CameraGetPos`
- `FCamFollow(rotlock)/Off`, `FCamSetPitch(+Rel)`, `FCamSetPos(+Rel)`,
  `FCamSetRot(rotation, +Rel)` — free camera
- `TCam*` — animated camera keyframes: `TCamSetPos/SetPosEntity/SetPosRel`,
  `TCamSetRot/SetRotEntity/SetRotRel`, `TCamSetPitch/SetPitchEntity/SetPitchRel`,
  `TCamSetDist/SetDistRel`, `TCamFollow/Off`, `TCamContRot/Off`, `TCamStopContRot`,
  `TCamGetDist/GetPitch/GetRot`, `IsCameraStopT`, `IsCameraRotStopT`
- `LinearCameraOn/Off`, `AnimModeOn/Off` (cutscene mode: input locked, "Y" camera
  toggle)

**Misc / spying**
- `EnableSpyInfo`, `IsAttackedBySpy`, `IsEnemyDetectedIn`, `IsEntityDetectedIn`,
  `IsEq`, `CountType`, `Distance`, `DistancePos`, `Wedge3D(earthlings)`,
  `SetRadarRangesOn/Off`, `SetSkyWalkerEffekt`

### 5.2 Conditions

`<Conditions>` holds `<Condition ReturnType="Boolean|Number">expression</Condition>`.
Same-level conditions AND together; nested are OR. Use with `Lifetime=0` commands to
poll.

### 5.3 Select (iteration)

Iterates a set of entities; parameters seen in shipped scripts:
- `SelectType` — one of: `"Squad"`, `"SolarSystem"`, `"Planet"`, `"Hero"`,
  `"JumpNode"`, `"Sun"`, `"Invention"`, `"Empire"`, `"SpaceEntity"`
- `EntityIDVariable` / `EntityNrVariable` — variables receiving the current item
- Filters: `SolarSystem`, `OwnerID`, `TypeID`, `Distance`, `Position`, `Hidden`,
  `MainHero`, `SelectEntityTypeName`, `SelectEntityID`, `SelectItemNr`
- Doc warning: don't nest Selects — huge slowdown.

### 5.4 Switch / Jump

Case-style branching; `Jump` jumps to a Switch target. Conditions at same level AND.

### 5.5 Set

Generic variable assignment: `Variable` + `Value` parameters (Type can be All to
accept any).

## 6. Data types

| Type | Examples |
|---|---|
| Number / Float | `Value="0"`, `Value="GetValue2Difficulty(300, 200, 100)"` |
| Boolean / Logical | `Value="true"` |
| String | `Value="&quot;MainTimer&quot;"` (quotes are XML-escaped inside) |
| VEC3 | `Value="[0-2000,2000,0]"` or function result |
| VEC4, FRGB, FRGBA, ARGB, color | color/position data |
| Array | lists |
| Anim | keyframe animation |
| All | any type |

## 7. Level wiring

- `level.inf` → `Script="xxx.scp"` (look in each level folder).
- `NextLevelName` / campaign flow in `level.inf`.
- Random events: organizer script (`RandomEvents\RANDOMEVENTS.scp`) uses RunScripts
  to launch `evt_*.scp` files; hero management works the same way.
- Message IDs: `"#iOM_MESSAGE#"` syntax, text lives in
  `Game_inf\Gametext\<lang>\ADDON_TEXT_E.txt`; message interface files in
  `Game_inf\Messages`.

## 8. Modding workflow with decoded files

1. Decode: `java -cp . ScpDec <LevelsDir> decoded-levels`
2. Edit the `.xml` (it's the script's source form; the game loads this text form too).
3. Either ship the edited text as `name.scp` next to `level.inf`, or re-pack to the
   `[size][78 DA][deflate]` container if the game insists on packed files.
