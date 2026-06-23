# CC: Atmos
**A ComputerCraft addon for Minecraft 1.21.1 (NeoForge)**

CC: Atmos adds the **Bangboo** — a craftable entity with a built-in ComputerCraft computer. It does nothing on its own; you write the Lua programs. Equip it with plug-in cartridges to unlock peripherals, then program it to scan, navigate, communicate, and interact with the world.

---

## The Bangboo

The Bangboo is deployed from a **Boo Box** item (right-click on a block). Each Bangboo runs its own CC computer accessible by right-clicking it. Plug-ins are installed into up to 8 cartridge slots (4 base, +4 with a Plugin Hub installed).

### Core Plug-ins

| Plug-in | Peripheral | What it unlocks |
|---------|------------|-----------------|
| Sensor Core | `scanner` | `scan(radius, [filter])` — detects nearby blocks and entities |
| Navigation Core | `pathing` | `path(x,y,z)` — navigates to exact world coordinates |
| Compass Core | `compass` | Waypoint storage and distance/facing queries |
| Comms Core | `wireless` | Wireless modem messaging between Bangboos |
| Logistics Core | `inventory` / `storage` | Internal inventory management and block storage I/O |
| Field Core | `tool` | Block breaking, placement, and entity interaction |
| Proximity Core | `proximity` | Fires events when entities enter or leave a radius |
| Redstone Core | `redstone` | Reads redstone signals from adjacent blocks |
| Lantern Core | `lantern` | Places a dynamic light source above the Bangboo |
| Speaker Core | `speaker` | Plays sounds and note blocks |
| Language Core | `language` | Speaks to nearby players and listens for replies |
| Cosmetic Core | `cosmetic` | Changes the Bangboo's active skin at runtime |
| Anti-Grav Core | `anti_grav` | Switches the Bangboo to flight mode |
| Config Core | `config` | Adjusts stats (speed, health, attack) at runtime |
| Command Core | `command` | Runs server commands (creative/admin only) |
| Backpack Plug-in | `backpack` | Attaches a Shulker Box or Sophisticated Backpack |
| Advanced Terminal | — | Upgrades the built-in terminal to Advanced (colour) |
| Plugin Hub | — | Unlocks 4 extra plug-in slots (8 total) |

### Tiered Cores

Cores bundle multiple plug-in peripherals into a single slot. Higher marks add more peripherals — same one slot, more capability.

| Core | Mk1 | Mk2 | Mk3 |
|------|-----|-----|-----|
| **Sensor** | `scanner` | + `proximity` | — |
| **Navigation** | `pathing`, `compass` | + `scanner`, `proximity` | — |
| **Comms** | `wireless`, `redstone` | + `language` | + `speaker` |
| **Logistics** | `inventory` | + `storage` | — |
| **Field** | `tool` | + `inventory` | + `storage` |

### Skin System

Bangboo skins are data-pack driven — drop a JSON into `data/<namespace>/bangboo_skin/` to register a new skin with its own model, texture, animation set, hitbox, and scale. See `docs/CUSTOM_SKINS.md` for the full authoring guide.

---

## Dependencies

| Mod | Version | Role |
|-----|---------|------|
| [CC: Tweaked](https://modrinth.com/mod/cc-tweaked) | 1.119.0 | Required |
| [GeckoLib](https://modrinth.com/mod/geckolib) | 4.8.4 | Required |
| [Sable](https://modrinth.com/mod/sable) | 2.0.3 | Optional — enables ship boarding & sublevel pathfinding |
| Sable Companion Common | 1.6.0 | Optional — required if using Sable |
| [Create](https://modrinth.com/mod/create) | 6.0.10 | Optional |
| [Create Aeronautics](https://github.com/createmod/create-aeronautics) | 1.3.0 | Optional — addon for Sable |
| [Sophisticated Backpacks](https://modrinth.com/mod/sophisticated-backpacks) | — | Optional — enables Backpack Plug-in |
| Sophisticated Core | — | Optional — required if using Sophisticated Backpacks |

---

## Build & Dev Setup

**Requirements:** JDK 21, Gradle, VSCode with the Extension Pack for Java and Gradle for Java extensions.

```bash
# Compile (fast, for iteration)
./gradlew compileJava

# Build the mod jar
./gradlew build

# Launch dev client
./gradlew runClient

# Launch dev server
./gradlew runServer
```

> **Note:** Avoid `./gradlew clean` — it deletes NeoForge's generated artifacts and breaks VSCode's classpath. If you need a clean build, run `compileJava` afterwards and reload the Java language server.

---

## Lua API

Full peripheral API documentation is in [`docs/PLUGINS.md`](docs/PLUGINS.md).  
Custom skin authoring guide is in [`docs/CUSTOM_SKINS.md`](docs/CUSTOM_SKINS.md).
