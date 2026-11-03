# Architecture

## Non-negotiable rules

- **Server authority:** progression, stats, job choices, combat results, loot, parties, and teleport eligibility are decided by the logical server.
- **Persistent state:** player progression is stored in the player's persistent entity data under the `mmomc` compound. New fields must have safe defaults.
- **No client trust:** later GUI/network messages are requests only; the server validates levels, costs, ownership, cooldowns, distance, and party membership.
- **Data-driven content:** job definitions, skills, equipment, loot tables, and NPC offerings should move to data/config files as systems mature.
- **Compatibility:** avoid hard dependencies on minimap or animation mods unless explicitly chosen; integrate optionally.
- **Small milestones:** every new system needs a focused test or a reproducible in-game verification checklist.

## Initial package layout

- `MMOMC`: mod entry point and event registration.
- `RpgData`: persistent player profile and progression rules.
- `Job`: first-job and specialization mapping.
- `RpgEvents`: event-driven EXP awards and respawn data preservation.
- `RpgCommands`: initial development commands.

## Initial progression defaults

- Maximum level: 120.
- First job: level 10.
- Second job: level 50.
- Stat points: 3 per level gained.
- Stats: STR, STA, DEX, INT, initially zero allocated.
- EXP threshold for level L: `100 + 50L + 5L²`.

These are initial balance defaults, not permanent constraints. They should become server-configurable after the first gameplay prototype.
