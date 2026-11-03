# MMOMC — Minecraft MMORPG

MMOMC is a server-authoritative Minecraft Java RPG mod inspired by classic MMORPG progression. Its target is Minecraft Java 1.21.1, NeoForge 21.1.x, and Java 21.

## Current development features

- Persistent level, experience, STR / STA / DEX / INT, and unspent stat points
- Hostile-mob experience rewards and level-up progression (maximum level 120)
- First job selection at level 10 and second-job specialization at level 50
- Server-side stat effects, mana pool, and mana regeneration
- Skills: Power Strike, Heal, and Arcane Bolt
- First-job abilities: Blade Fury, Iron Guard, Hawkeye, Elemental Surge, and Blessing
- Persistent parties with create, invite, join, leave, and list commands
- Five-player party limit and five-minute invitations
- Nearby party EXP sharing, party-targeted healing, and shared Blessing

This is an active development build, **not yet a feature-complete MMORPG mod**. Equipment refinement, custom equipment and materials, bosses and loot, NPC hubs, quests, teleportation, world generation, minimap, and client UI remain under development.

## Build

Install a 64-bit Java 21 JDK. Run the included Gradle wrapper:

```sh
# Windows
gradlew.bat build

# Linux / macOS
./gradlew build
```

Launch the development client or dedicated server:

```sh
gradlew.bat runClient
gradlew.bat runServer
```

The first Gradle run downloads Minecraft and NeoForge development dependencies. For a dedicated-server test, accept the EULA in the generated run directory before starting the server again.

## Commands

### Character progression
- `/mmomc profile` — show level, EXP, job, stats, stat points, and mana.
- `/mmomc allocate <str|sta|dex|int> <points>` — spend unallocated stat points.
- `/mmomc job <swordsman|tank|archer|magician|assist>` — choose a first job at level 10.
- `/mmomc specialize <blade_master|berserker|paladin|guardian|ranger|sharpshooter|elementor|psykeeper|ringmaster|billposter>` — specialize at level 50.

### Skills
- `/mmomc skill list` — list available skills.
- `/mmomc skill cast <skill_id>` — cast a skill.
- Skill IDs: `power_strike`, `heal`, `arcane_bolt`, `blade_fury`, `iron_guard`, `hawkeye`, `elemental_surge`, `blessing`, `blade_dance`, `berserker_rage`, `paladin_aegis`, `guardian_fortress`, `ranger_volley`, `sharpshooter_focus`, `elementor_meteor`, `psykeeper_blast`, `ringmaster_sanctuary`, `billposter_combo`.
- First-job abilities require the matching first job. Skill mana costs and cooldowns are validated by the server.

### Parties
- `/mmomc party create` — create a party and become its leader.
- `/mmomc party invite <player>` — invite a player (leader only).
- `/mmomc party join` — accept a pending invitation.
- `/mmomc party leave` — leave the current party.
- `/mmomc party list` — list online party members.

Party membership is stored in player persistent data. Invitations expire after five minutes. If the leader leaves while members are online, leadership transfers to the next online member. EXP is shared among nearby online party members; Heal and Blessing only affect party members in range.

## Design principles

1. Server-authoritative gameplay; never trust client-provided stats or loot.
2. Persistent player progression with safe defaults for existing worlds.
3. Data-driven content wherever practical.
4. Multiplayer-safe party, loot, buff, and equipment systems.
5. Automated build checks before adding larger systems.
6. Original names, art, models, and content rather than redistributing proprietary Flyff assets.

## Roadmap

See [docs/ROADMAP.md](docs/ROADMAP.md) and [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).
