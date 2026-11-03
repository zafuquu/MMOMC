# MMOMC — Minecraft MMORPG

MMOMC is a server-authoritative Minecraft Java RPG mod inspired by classic MMORPG progression. The long-term goal includes jobs and second-job specializations, manually allocated stats, skills and Assist support abilities, +1–+20 equipment refinement with aura milestones, boss-exclusive loot, accessories, parties, NPC hubs, warp points, teleportation, dungeons, and a map/waypoint experience.

## Development target

- Minecraft Java Edition **1.21.1**
- NeoForge **21.1.x**
- Java **21**
- Single-player and dedicated-server support

The project deliberately starts with a small, testable core. Gameplay state must be owned by the logical server; client screens and visual effects will be layered on after the rules are stable.

## Current implementation status

The first milestone establishes the Gradle/NeoForge workspace and the initial RPG core:
- persistent character level and experience
- STR, STA, DEX, and INT stat points
- experience from defeating living enemies
- player commands to inspect character state and allocate stat points
- first-job selection at level 10 and second-job specialization at level 50 (initial defaults)

This is an early development build, **not yet a feature-complete MMORPG mod**. City generation, full job skills, loot tables, parties, teleportation, minimap, custom models, and refinement visuals are planned follow-on milestones.

## Build

Install a 64-bit Java 21 JDK. With Gradle installed, run:

```sh
gradle build
```

To launch the development client or dedicated server, use:

```sh
gradle runClient
gradle runServer
```

The first Gradle run downloads Minecraft and NeoForge development dependencies and may take a while. For a dedicated-server test, accept the EULA in the generated run directory before starting the server again.

## Initial commands

- `/mmomc profile` — display your character level, EXP, job, stats, and available stat points.
- `/mmomc allocate <str|sta|dex|int> <points>` — spend unallocated stat points.
- `/mmomc job <swordsman|tank|archer|magician|assist>` — choose a first job at level 10.
- `/mmomc specialize <blade_master|berserker|paladin|guardian|ranger|sharpshooter|elementor|psykeeper|ringmaster|billposter>` — specialize at level 50.

These commands are intentionally server-validated. Job advancement requirements and stat formulas will evolve as balance is tested.

## Design principles

1. Server-authoritative gameplay; never trust client-provided stats or loot.
2. Persistent player progression with safe defaults for existing worlds.
3. Data-driven content wherever practical.
4. Multiplayer-safe party, loot, buff, and equipment systems.
5. Automated build checks before adding larger systems.
6. Original names, art, models, and content rather than redistributing proprietary Flyff assets.

## Roadmap

See [docs/ROADMAP.md](docs/ROADMAP.md) and [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).
