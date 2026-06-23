# Bangboo — Implementation Plan

The Bangboo is a craftable, programmable entity powered by a built-in ComputerCraft computer.
Right-clicking opens its terminal + plug-in GUI. Lua code controls everything it does. It does
nothing on its own — no AI goals, no natural spawning. It moves and interacts with the world
like a player, not a Turtle.

---

## Status

### Foundation
- [x] BooBox item — deploys Bangboo on right-click on a block (crafted, not a spawn egg)
- [x] Embed CC ServerComputer into BangbooEntity (computer ID persists in NBT)
- [x] Computer lifecycle — tick, startup on chunk load, shutdown on chunk unload/death
- [x] Right-click opens Bangboo GUI (terminal + plug-in slots + player inventory)
- [x] Custom GUI screen — pure code rendering, no texture files

### Plug-In System
- [x] `BangbooPluginItem` base class — plug-ins declare what peripherals they provide
- [x] Plug-in slot storage on the entity (4 base slots, NBT persisted)
- [x] Plug-In Hub — one slot, unlocks 4 extra (7 total usable)
- [x] Advanced Plug-In Hub — hub + Advanced terminal in one slot
- [x] `BangbooPeripheralHub` — exposes each plug-in as a fully independent CC peripheral via `"peripheral_hub"` type; hot-swap on install/remove with no reboot
- [x] API removal — plug-in removed → peripheral disappears, fires `peripheral_detach` event

### World Interaction
- [x] Internal inventory — 16 slots (requires Inventory Plug-In)
- [x] Backpack slot — 1 slot for Shulker Box or Sophisticated Backpacks container; drops on death
- [x] Fake player — lets the Bangboo interact with the world as a player

### Lua API — Base Kit (no plug-ins required)
- [x] Movement — `moveTo(x,y,z)`, `face(yaw)`, `stop()`, `isMoving()`
- [x] Status — `getPos()`, `getHealth()`, `getMaxHealth()`, `isOnGround()`, `getFacing()`, `getID()`, `getName()`, `setName()`
- [x] Basic scan — `bangboo.scan()` returns nearby entity count, nearest distance, health fraction

### Lua API — Plug-In Gated (all implemented)
- [x] **Scanner** — `scan(radius, [filter])` typed entity/block/item list, nearest-first, up to 64-block radius for entities
- [x] **Pathing** — `path(x,y,z)` blocking navigation; returns `ok, reason`; `cancel()`, `setSpeed()`, `isPathing()`
- [x] **Compass** — `mark()`, `remove()`, `getWaypoints()`, `distanceTo()`, `facingTo()`
- [x] **Inventory** — `list()`, `equip(slot)`, `getHeld()`, `drop(slot)`
- [x] **Storage** — `scanStorage(side)`, `pull()`, `push()` for adjacent inventory blocks
- [x] **Wireless** — standard CC wireless modem; `open()`, `close()`, `transmit()`, `broadcast()`
- [x] **Redstone** — `getInput(side)`, `getAnalogInput(side)`
- [x] **Tool** — `swing()`, `use()`, `dig()`, `place()`, `attack(entityId)`
- [x] **Proximity** — fires `proximity_enter` / `proximity_leave` events; configurable radius
- [x] **Lantern** — dynamic light block above Bangboo; `on()`, `off()`, `setLevel()`
- [x] **Speaker** — `playSound()`, `playNote()`
- [x] **Language** — `say()`, `whisper()`, `listen()` / chat event system
- [x] **Cosmetic** — `getSkin()`, `setSkin(id)`, `listSkins()`
- [x] **Backpack** — full Shulker Box + Sophisticated Backpacks support; `list()`, `pushItem()`, `pullItem()`
- [x] **Anti-Grav** — `setEnabled()`, `isEnabled()`, `setBobbing()`; immune to fall damage while installed
- [x] **Config** — `set(name, val)`, `get(name)`, `reset()`, `resetAll()`, `list()` for all entity attributes
- [x] **Command** *(creative only)* — `exec(cmd)` → `ok, { lines }`

### Tiered Cores (all 5 lines implemented)
- [x] **Sensor** — Mk1 (`scanner`), Mk2 (`scanner`, `proximity`)
- [x] **Navigation** — Mk1 (`pathing`, `compass`), Mk2 (`pathing`, `compass`, `scanner`, `proximity`)
- [x] **Comms** — Mk1 (`wireless`, `redstone`), Mk2 (+ `language`), Mk3 (+ `speaker`)
- [x] **Logistics** — Mk1 (`inventory`), Mk2 (`inventory`, `storage`)
- [x] **Field** — Mk1 (`tool`), Mk2 (`tool`, `inventory`), Mk3 (`tool`, `inventory`, `storage`)

### Modification Plug-Ins (all implemented)
- [x] Advanced Terminal Plug-In — upgrades CC terminal to Advanced (colour); triggers reboot
- [x] Reinforcement Plug-In — +10 max health
- [x] Sprint Module Plug-In — +0.15 movement speed

### Skin System
- [x] `BangbooSkinDefinition` + `BangbooSkinRegistry` — data-pack driven; JSONs in `data/*/bangboo_skin/`
- [x] Per-skin geo, texture, animation, scale, hitbox, hitbox offset
- [x] Two-controller GeckoLib animation — `locomotion` (base) + `overlay` (weighted random variants)
- [x] Built-in skins: Eous, Eyebot

### Navigation
- [x] `BangbooMoveControl` — GROUND / FLIGHT / STEER modes
- [x] Anti-grav flight (`FlyingPathNavigation` + direct velocity)
- [x] Ground final-approach phase — direct `MoveControl` walk for last ~2 blocks, bypasses vanilla nav stopping short
- [x] Arrival detection — horizontal distance + Y tolerance (entity feet sit above solid block targets)
- [x] Sable sublevel boarding — detects target on ship, switches to direct-walk boarding approach, resumes nav once tracked to sublevel

### Polish
- [x] Name tag — label above head when named
- [x] Death & drop — drops plug-ins, internal inventory, backpack slot on death; computer state preserved
- [x] `BangbooComputerRegistry` — entity lookup by CC computer ID for peripheral resolution
- [x] Recipes — all plug-ins, both cartridges, BooBox, tiered cores

---

## Pending

- [ ] **Energy system** — coal/fuel consumption from internal inventory + Forge Energy integration; Bangboo Charging Dock block. Config Core tooltip already stubs this.
- [ ] **BooBox texture** — user art asset, not blocking anything
- [ ] **Delete `BangbooCombinedPeripheral.java`** — orphaned file, not used anywhere, safe to remove
- [ ] **`getHunger()` / `getEffect()`** — originally planned for base API; Bangboo has no hunger so `getEffect()` (active potion effects) is the only one worth adding

---

## Architecture

```
BangbooEntity (PathfinderMob, GeoEntity, MenuProvider)
  ├── ServerComputer              ← real CC computer; terminal opens on right-click
  ├── BangbooPeripheralHub        ← peripheral_hub on ComputerSide.TOP; exposes each
  │     └── [per plug-in]           plugin as its own independent CC peripheral
  ├── BangbooBaseAPI              ← "bangboo" Lua global; always present, no plug-in needed
  ├── PluginSlots[8]              ← 4 base + 4 hub-unlocked; NBT persisted
  ├── InternalInventory[16]       ← requires Inventory Plug-In
  ├── BackpackSlot[1]             ← requires Backpack Plug-In; drops on death
  ├── BangbooMoveControl          ← GROUND (vanilla) / FLIGHT (velocity) / STEER (direct)
  └── Skin
        ├── BangbooSkinRegistry   ← server-side data pack listener
        ├── BangbooSkinDefinition ← geo, texture, animation, scale, hitbox
        ├── BangbooModel          ← GeckoLib; reads skin per entity
        └── BangbooRenderer       ← applies per-skin scale
```

## GUI Layout

```
┌─────────────────────────────────┬──────────────────┐
│                                 │   [Plug-In  1]   │
│                                 │   [Plug-In  2]   │
│         Terminal                │   [Plug-In  3]   │
│                                 │   [Plug-In  4]   │
│                                 │   [Plug-In  5]   │
│                                 │   [Plug-In  6]   │
│                                 │   [Plug-In  7]   │
├─────────────────────────────────┴──────────────────┤
│              Player Inventory (9×4)                │
└────────────────────────────────────────────────────┘
```

Slots 5–7 only appear when a Plug-In Hub or Advanced Plug-In Hub is installed.
