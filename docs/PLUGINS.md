# CC: Atmos — Plug-In System Reference

## Table of Contents
1. [System Overview](#system-overview)
2. [The `bangboo` Global](#the-bangboo-global)
3. [Hub Items](#hub-items)
4. [Capability Plug-Ins](#capability-plug-ins)
5. [Modification Plug-Ins](#modification-plug-ins)
6. [Capability Core Tiers](#capability-core-tiers)
7. [Standalone Cores](#standalone-cores)
8. [Event Reference](#event-reference)
9. [Slot Planning Reference](#slot-planning-reference)

---

## System Overview

### What Plug-Ins Do

A Bangboo has a built-in CC computer that runs Lua programs. Out of the box it exposes only the base `bangboo` API (movement, health, position, facing, scan) as a Lua global. Plug-Ins expand this in two ways:

- **Capability Plug-Ins** — add a new CC peripheral to the computer (e.g. `scanner`, `tool`, `storage`), accessible via `peripheral.find()`.
- **Modification Plug-Ins** — change the Bangboo itself: stat buffs, terminal upgrade, skin changes. No new peripheral.

**Capability Cores** are a third type: they bundle two or more Capability Plug-Ins into a single slot. They come in **Mark tiers** (Mk1, Mk2, Mk3) — each higher mark adds more peripherals to the same slot. You can mix and match Marks across core lines to build exactly the capability set you need.

### Slot System

Every Bangboo has **4 base plug-in slots**. Installing a **Plug-In Hub** (which takes one base slot) unlocks **4 extra slots**, giving 7 total. Only one Hub may be installed at a time.

| Setup | Usable slots |
|---|---|
| No hub | 4 |
| Plug-In Hub or Advanced Plug-In Hub installed | 7 (Hub takes 1, adds 4 extra) |

### Accessing Plug-In APIs in Lua

Each installed Capability Plug-In registers as its own **independent CC peripheral**, accessible by type via `peripheral.find()`. Peripherals are completely isolated from each other — no method name collisions, no shared dispatch table.

```lua
-- Recommended: assign to a local at the top of your script
local scanner = peripheral.find("scanner")
local pathing = peripheral.find("pathing")
local storage = peripheral.find("storage")

-- Check whether a plug-in is installed before using it
if scanner then
    local results = scanner.scan(16)
    print("Found " .. #results .. " things")
end
```

`peripheral.find("type")` returns the peripheral proxy if that plug-in is installed, or `nil` if it isn't.

The `bangboo` global (movement, position, health, etc.) is always present regardless of installed plug-ins.

### Discovering Installed Plug-Ins

Use standard CC peripheral APIs to discover what's installed:

```lua
-- List every installed plug-in peripheral by name and type
for _, name in ipairs(peripheral.getNames()) do
    local types = { peripheral.getType(name) }
    print(name .. " → " .. table.concat(types, ", "))
end
```

Example output for a Bangboo with Sensor Core Mk2 + Navigation Core Mk1 installed:
```
scanner    → scanner
proximity  → proximity
pathing    → pathing
compass    → compass
```

Defensive check at script startup — fail fast if a needed plug-in isn't installed:
```lua
local pathing = peripheral.find("pathing")
    or error("This script needs a Pathing Plug-In or Navigation Core")
local scanner = peripheral.find("scanner")
    or error("This script needs a Scanner Plug-In or Sensor Core")
```

### How Plug-In Install / Remove Works

When a plug-in is **installed or removed**, the Bangboo's peripheral table updates **immediately without a reboot**. The peripheral appears or disappears as a standard CC `peripheral` / `peripheral_detach` event — no need to restart your script.

The only operations that **do trigger a full computer reboot** are:

- Installing or removing the **Advanced Terminal Plug-In**
- Installing or removing the **Advanced Plug-In Hub**

These change the CC terminal family (Normal ↔ Advanced), which requires a new `ServerComputer` instance. On reboot, saved files on the computer (`/startup.lua`, your programs) are **never deleted** — they live in CC's save directory keyed to the computer ID.

### Removing a Hub

Removing a Plug-In Hub (or Advanced Plug-In Hub) **drops all plug-ins in the extra slots** at the Bangboo's feet before the computer reboots. Base slot plug-ins are unaffected.

---

## The `bangboo` Global

The `bangboo` table is **always present** in every Bangboo's computer — no plug-in required, no `peripheral.find()`. It covers identity, status, basic movement, and a rough proximity scan.

```lua
-- All methods are accessed directly on the bangboo global:
local pos = bangboo.getPos()
print(pos.x, pos.y, pos.z)
```

### Status

```lua
bangboo.getPos()
-- Returns { x, y, z, yaw, pitch }
-- yaw:   0 = south, 90 = west, 180 = north, 270 = east
-- pitch: negative = looking up, positive = looking down

bangboo.getHealth()       -- current HP (number)
bangboo.getMaxHealth()    -- maximum HP (number)
bangboo.isOnGround()      -- true if standing on a solid surface

bangboo.getFacing()       -- yaw in degrees (same as pos.yaw)
bangboo.getID()           -- this Bangboo's persistent integer ID
bangboo.getName()         -- display name (custom name if set, else "Bangboo")
bangboo.setName(name)     -- set a custom name tag; pass "" or nil to clear it
```

### Movement

> `moveTo()` is **fire-and-forget** — it does not block or yield. Use the **Pathing Plug-In's** `path()` if you need to wait until the Bangboo arrives.

```lua
bangboo.moveTo(x, y, z)   -- begin moving toward a position (non-blocking)
bangboo.stop()             -- cancel current movement
bangboo.isMoving()         -- true while the Bangboo is navigating

bangboo.face(yaw)          -- snap horizontal facing to yaw degrees
                           -- 0 = south, 90 = west, 180 = north, 270 = east
```

### Basic Scan

A lightweight proximity overview. For full entity/block detail, use the **Scanner Plug-In**.

```lua
local info = bangboo.scan()
-- Returns {
--   entities = <number of living entities within 16 blocks>,
--   nearest  = <distance to closest one, or -1 if none>,
--   health   = <this Bangboo's HP as a 0–1 fraction>,
-- }
```

**Example — flee when hurt and something is nearby:**
```lua
while true do
    local info = bangboo.scan()
    if info.health < 0.3 and info.nearest > 0 and info.nearest < 8 then
        local home = compass.getWaypoints()["home"]
        if pathing and home then pathing.path(home.x, home.y, home.z) end
    end
    os.sleep(1)
end
```

---

## Hub Items

### Plug-In Hub
**Item:** `plugin_hub` | **Lua table:** none

Unlocks the 4 extra plug-in slots. Only one hub may be installed at a time. Removing it drops any plug-ins in the extra slots.

---

### Advanced Plug-In Hub
**Item:** `advanced_plugin_hub` | **Lua table:** none

Combines the Plug-In Hub with an Advanced Terminal upgrade in one slot.

- Unlocks 4 extra plug-in slots
- Upgrades the CC terminal to **Advanced** (full colour support, `paintutils` API)
- Removing it reverts the terminal to Normal, drops extra-slot plug-ins, and reboots the computer

---

## Capability Plug-Ins

Each Capability Plug-In registers one peripheral type. Access it with `peripheral.find("type")`.

---

### Inventory Plug-In
**Item:** `inventory_plugin` | **Peripheral type:** `inventory`

Adds a **16-slot internal inventory** to the Bangboo (visible in the GUI). The Bangboo uses one of these slots as its "held item" for tool interactions.

```lua
local inventory = peripheral.find("inventory")

inventory.list()                -- { [slot] = { name, count }, ... } — occupied slots only
inventory.size()                -- total slot count (16)
inventory.isEmpty()             -- true if every slot is empty

inventory.equip(slot)           -- set slot as the held item for tool use (1-indexed)
inventory.getHeld()             -- { name, count } of the currently held item, or nil

inventory.drop(slot)            -- drop all items from slot at Bangboo's feet
inventory.drop(slot, count)     -- drop a specific count
```

**Notes:**
- The `tool` peripheral reads the equipped slot when swinging or placing.
- `inventory.list()` returns only occupied slots; empty slots are nil.

---

### Scanner Plug-In
**Item:** `scanner_plugin` | **Peripheral type:** `scanner`

Detailed world scan returning entities, dropped items, and blocks within a radius.

```lua
local scanner = peripheral.find("scanner")

local results = scanner.scan(radius)
-- radius: 1–64 for entities/items, capped at 8 for blocks

-- Each entry is one of:
-- { type="entity", name, id, distance, x, y, z }
-- { type="entity", name, id, distance, health, maxHealth, x, y, z }  -- living entities
-- { type="item",   name, count, distance, x, y, z }                  -- dropped item stacks
-- { type="block",  name, x, y, z }                                   -- non-air blocks (radius ≤ 8)
```

**Example — find the nearest entity:**
```lua
local scanner = peripheral.find("scanner")
local results = scanner.scan(16)
local nearest, nearestDist = nil, math.huge
for _, r in ipairs(results) do
  if r.type == "entity" and r.distance < nearestDist then
    nearest, nearestDist = r, r.distance
  end
end
if nearest then
  print("Nearest entity: " .. nearest.name .. " at " .. nearestDist .. " blocks")
end
```

**Notes:**
- The `id` field is the integer runtime entity ID, compatible with `tool.attack(id)`.
- Block scanning is capped at 8 blocks radius for performance. Entity and item scanning go up to 64.
- Results are capped at 1000 entries per call.

---

### Pathing Plug-In
**Item:** `pathing_plugin` | **Peripheral type:** `pathing`

Blocking autonomous pathfinding. Unlike `bangboo.moveTo()` which fires and forgets, `pathing.path()` **yields** the Lua coroutine until the Bangboo arrives or gives up.

```lua
local pathing = peripheral.find("pathing")

local ok, reason = pathing.path(x, y, z)
-- ok:     true on arrival, false if movement failed
-- reason: "arrived" | "timeout" | "cancelled"

pathing.moveTo(x, y, z)        -- non-blocking redirect: steers mid-flight without cancelling
pathing.cancel()               -- abort current path (fires bangboo_path_done with "cancelled")
pathing.setSpeed(multiplier)   -- 0.5–2.0, default 1.0
pathing.getSpeed()             -- current speed multiplier
pathing.isPathing()            -- true while a path is active
```

**Example — patrol between two points:**
```lua
local pathing = peripheral.find("pathing")
while true do
  local ok, r = pathing.path(100, 64, 200)
  if not ok then print("Could not reach point A: " .. r) end
  local ok, r = pathing.path(120, 64, 200)
  if not ok then print("Could not reach point B: " .. r) end
end
```

**Notes:**
- Timeout is 1200 ticks (60 seconds).
- Works naturally with the Compass Plug-In: `pathing.path(table.unpack(compass.getWaypoints()["base"]))`.

---

### Wireless Plug-In
**Item:** `wireless_plugin` | **Peripheral type:** `wireless`

Gives the Bangboo a standard CC wireless modem. It participates in CC's **global wireless network**, so it can send and receive messages with any CC computer that has a wireless modem — not just other Bangboos.

```lua
local wireless = peripheral.find("wireless")

wireless.open(channel)                             -- start listening on a channel (0–65535)
wireless.close(channel)                            -- stop listening on a channel
wireless.closeAll()                                -- close all open channels
wireless.isOpen(channel)                           -- true if channel is currently open
wireless.isWireless()                              -- always true

wireless.transmit(channel, replyChannel, message)  -- send a packet (channel must be open)
wireless.broadcast(channel, message)               -- shorthand: transmit(ch, ch, msg)

-- Incoming messages arrive as:
-- "modem_message", side, channel, replyChannel, message, distance
```

**Example — ping a CC computer and wait for reply:**
```lua
local wireless = peripheral.find("wireless")
local CH = 42
wireless.open(CH)
wireless.broadcast(CH, { from = bangboo.getName(), text = "Hello!" })

local timer = os.startTimer(10)
while true do
  local ev, _, ch, _, msg, dist = os.pullEvent()
  if ev == "modem_message" and ch == CH then
    print("Reply from " .. tostring(msg.from) .. " (" .. math.floor(dist) .. "m)")
    break
  elseif ev == "timer" then
    print("No reply in 10 seconds.")
    break
  end
end
wireless.close(CH)
```

**Notes:**
- Range uses CC's standard wireless modem range (configurable in CC's server config).
- Channels reset on computer reboot — reopen them in `startup.lua`.

---

### Tool Plug-In
**Item:** `tool_plugin` | **Peripheral type:** `tool`

Lets the Bangboo interact with the world as a player would. Requires the **Inventory Plug-In** (or a Core containing it).

```lua
local tool = peripheral.find("tool")

tool.swing()                    -- left-click in look direction: hits entity or breaks block
tool.use()                      -- right-click in look direction: uses item on block/entity

tool.dig(x, y, z)               -- break a specific block
tool.place(x, y, z)             -- place the held block item at the given position
tool.attack(entityId)           -- attack a specific entity by ID (from scanner.scan)
```

**Return values** — all tool functions return `(ok, reason)`:
```lua
local ok, what = tool.swing()
-- ok:   true if something was hit
-- what: "entity" | "block" | "nothing"
```

**Notes:**
- `tool.attack(id)` uses the runtime entity ID from `scanner.scan()`. IDs change when entities reload — get a fresh scan before attacking.

---

### Proximity Alert Plug-In
**Item:** `proximity_plugin` | **Peripheral type:** `proximity`

Fires CC events when entities enter or leave a configurable radius around the Bangboo.

```lua
local proximity = peripheral.find("proximity")

proximity.setRadius(radius)     -- set detection radius in blocks (1–64, default 8)
proximity.getRadius()           -- current radius
proximity.enable()              -- start scanning
proximity.disable()             -- stop scanning
proximity.isEnabled()           -- true if active

-- Events fired while enabled:
-- "proximity_enter", entityName, entityId, distance
-- "proximity_leave", entityName
```

**Notes:**
- Scanning happens every 10 server ticks (0.5 seconds).
- Proximity state (radius and enabled flag) **persists** through reboots — saved on the entity.

---

### Redstone Plug-In
**Item:** `redstone_plugin` | **Peripheral type:** `redstone`

Read redstone signal levels at blocks adjacent to the Bangboo's current position.

```lua
local redstone = peripheral.find("redstone")

redstone.getInput(side)         -- signal strength (0–15) on the given side
redstone.getAnalogInput(side)   -- true if any signal is present on that side

-- side: "north" | "south" | "east" | "west" | "up" | "down"
```

**Notes:**
- Reading is based on the block adjacent to the Bangboo's block position.
- The Bangboo cannot output redstone signals. Output support is a future feature.

---

### Storage Link Plug-In
**Item:** `storage_plugin` | **Peripheral type:** `storage`

Interact with any inventory-holding block adjacent to the Bangboo (chests, barrels, hoppers, furnaces, etc.).

```lua
local storage = peripheral.find("storage")

local contents = storage.scanStorage(side)
-- returns { [slot] = { name, count }, ... } for occupied slots (1-indexed)

storage.pull(side, slot)           -- take all items from external slot into Bangboo inventory
storage.pull(side, slot, count)    -- take up to count items
-- returns: number of items actually transferred

storage.push(side, slot)           -- put items from Bangboo slot into external container
storage.push(side, slot, count)    -- push up to count items
-- returns: number of items actually transferred

-- side: "north" | "south" | "east" | "west" | "up" | "down"
-- slot: 1-indexed slot number in the external inventory (from scanStorage results)
```

**Example — unload the Bangboo's inventory into a chest to the north:**
```lua
local inventory = peripheral.find("inventory")
local storage   = peripheral.find("storage")
for slot, item in pairs(inventory.list()) do
  storage.push("north", slot)
end
print("Inventory unloaded.")
```

**Notes:**
- Requires **Inventory Plug-In** (or a Core providing `inventory`) to be installed.
- Works with any block that has an item handler capability.

---

### Compass Plug-In
**Item:** `compass_plugin` | **Peripheral type:** `compass`

A persistent waypoint system. Waypoints are saved to the Bangboo's NBT and survive reboots, chunk unloads, and world restarts.

```lua
local compass = peripheral.find("compass")

compass.mark(label, x, y, z)   -- save a waypoint
compass.remove(label)          -- delete a waypoint (returns true if it existed)
compass.getWaypoints()         -- { label = { x, y, z }, ... }

compass.distanceTo(label)      -- straight-line distance to waypoint (nil if not found)
compass.facingTo(label)        -- yaw angle (0–360) toward waypoint (nil if not found)
```

**Example — mark home, then return to it:**
```lua
local compass = peripheral.find("compass")
local pathing = peripheral.find("pathing")

local pos = bangboo.getPos()
compass.mark("home", math.floor(pos.x), math.floor(pos.y), math.floor(pos.z))

local wp = compass.getWaypoints()["home"]
if wp then pathing.path(wp.x, wp.y, wp.z) end
```

**Notes:**
- `compass.getWaypoints()` returns positions with **named fields**: `wp.x`, `wp.y`, `wp.z`.
- `facingTo` returns yaw: 0 = south, 90 = west, 180 = north, 270 = east.

---

### Lantern Plug-In
**Item:** `lantern_plugin` | **Peripheral type:** `lantern`

Emits dynamic light around the Bangboo by placing an invisible light block above it that follows as it moves.

```lua
local lantern = peripheral.find("lantern")

lantern.on()                   -- turn lantern on at current level
lantern.off()                  -- turn off
lantern.toggle()               -- flip on/off
lantern.setLevel(brightness)   -- 0–15 (0 = off even if on is true)
lantern.getLevel()             -- current brightness setting
lantern.isOn()                 -- true if currently emitting light
```

**Notes:**
- Lantern state **persists** through reboots — saved in the entity's NBT.
- If the block above is solid, the light block retries each tick once the space clears.
- The light block is automatically removed when the plug-in is uninstalled or the entity dies.

---

### Speaker Plug-In
**Item:** `speaker_plugin` | **Peripheral type:** `speaker`

Play Minecraft sounds from the Bangboo's position in the world.

```lua
local speaker = peripheral.find("speaker")

speaker.playSound(name)                     -- play a sound at default volume/pitch
speaker.playSound(name, volume)             -- volume: 0.0–1.0 (default 1.0)
speaker.playSound(name, volume, pitch)      -- pitch: 0.5–2.0 (default 1.0)

speaker.playNote(instrument)                -- play a note block sound
speaker.playNote(instrument, volume, pitch)

-- name:       any Minecraft sound ID, e.g. "minecraft:entity.villager.ambient"
-- instrument: "harp" | "bass" | "basedrum" | "snare" | "hat" | "guitar" |
--             "flute" | "bell" | "chime" | "xylophone" | "iron_xylophone" |
--             "cow_bell" | "didgeridoo" | "bit" | "banjo" | "pling"
```

**Notes:**
- Sounds are played server-side and heard by all nearby players.
- Any registered Minecraft sound ID works. Unrecognised IDs play silently on clients.

---

### Language Plug-In
**Item:** `language_plugin` | **Peripheral type:** `language`

Gives the Bangboo the ability to speak in world chat, whisper to specific players, and listen for incoming messages.

```lua
local language = peripheral.find("language")

language.say(message)               -- broadcast to all players in the same dimension
language.whisper(playerName, msg)   -- private system message to one player
                                    -- returns true if player was found, false if offline

language.getPlayers()               -- list of player names online in the same dimension

language.listen()                   -- subscribe to public chat events
language.unlisten()                 -- unsubscribe
language.isListening()              -- true if currently subscribed

-- While listening, every player chat message fires:
-- "chat_message", playerName, message
```

**Example — greeter that responds to its name:**
```lua
local language = peripheral.find("language")
local myName = bangboo.getName() or "Bangboo"
language.listen()
while true do
  local _, player, msg = os.pullEvent("chat_message")
  if msg:lower():find(myName:lower()) then
    language.say("Did someone call for me? Hello, " .. player .. "!")
  end
end
```

**Notes:**
- `language.say()` sends a system message prefixed with the Bangboo's name.
- `language.listen()` resets on computer reboot — call it in `startup.lua` to survive reboots.

---

### Backpack Plug-In
**Item:** `backpack_plugin` | **Peripheral type:** `backpack`

Opens a dedicated backpack slot in the GUI that accepts a vanilla **Shulker Box** or a **Sophisticated Backpacks** backpack. The container item stays on the entity — its contents are accessible via the `backpack` peripheral.

```lua
local backpack = peripheral.find("backpack")

backpack.hasBackpack()                        -- true if a container is in the backpack slot
backpack.size()                               -- total slot count of the container
backpack.list()                               -- { [slot] = { name, count }, ... } occupied slots only
backpack.getItem(slot)                        -- { name, count } for one slot, or nil if empty

-- Transfer between Bangboo's internal inventory and the backpack (1-indexed slots)
backpack.pushItem(fromInternal, toBackpack)          -- move item from internal to backpack
backpack.pushItem(fromInternal, toBackpack, limit)
backpack.pullItem(fromBackpack, toInternal)          -- move item from backpack to internal
backpack.pullItem(fromBackpack, toInternal, limit)

-- Sophisticated Backpacks upgrade awareness (returns empty/false for Shulker Boxes)
backpack.listUpgrades()       -- { [slot] = "mod:upgrade_item_id", ... }
backpack.hasUpgrade(id)       -- true if the given upgrade item is installed
```

**Notes:**
- `pushItem` / `pullItem` require the **Inventory Plug-In** (or a Core providing `inventory`).
- Shulker Boxes have 27 fixed slots. Sophisticated Backpacks vary based on size upgrades.
- Sophisticated Backpacks support is optional — the plug-in works without the mod installed.
- The backpack item drops at the Bangboo's feet on death.

---

## Modification Plug-Ins

Modification Plug-Ins change what the Bangboo **is**, not what it can do in Lua. They add no peripheral. Install and forget — the effect is active as long as the plug-in is in a slot.

---

### Advanced Terminal Plug-In
**Item:** `advanced_terminal_plugin`

Upgrades the CC terminal from Normal to **Advanced**, unlocking full colour support and the `paintutils` API. Installing or removing this plug-in triggers a computer reboot (files are never lost).

---

### Reinforcement Plug-In
**Item:** `reinforcement_plugin`

Adds **+10 max health** (+5 hearts) while installed. Removing it caps current health to the new lower maximum.

---

### Sprint Module Plug-In
**Item:** `sprint_plugin`

Increases base movement speed by **+0.15** (roughly 60% faster than default). Affects both Lua-commanded movement and autonomous pathfinding.

---

### Cosmetic Plug-In
**Item:** `cosmetic_plugin` | **Peripheral type:** `cosmetic` *(only while installed)*

Change the Bangboo's visual skin. The chosen skin **persists permanently** after uninstalling — you only need the plug-in installed while picking or changing the skin.

```lua
local cosmetic = peripheral.find("cosmetic")

cosmetic.getSkin()            -- currently active skin ID, e.g. "ccatmos:eous"
cosmetic.setSkin(id)          -- apply skin by resource location ID
cosmetic.listSkins()          -- sorted array of all registered skin IDs
```

Skin IDs follow the `namespace:name` format matching the skin's data pack JSON filename:

```lua
cosmetic.setSkin("ccatmos:eous")       -- built-in default skin
cosmetic.setSkin("mypack:custom_bot")  -- from a data pack or addon mod

-- List and pick interactively
local skins = cosmetic.listSkins()
for i, id in ipairs(skins) do print(i, id) end
cosmetic.setSkin(skins[1])
```

**Notes:**
- Skins are defined in data packs under `data/<namespace>/bangboo_skin/<name>.json`. See [CUSTOM_SKINS.md](CUSTOM_SKINS.md) for the full authoring guide.
- The `cosmetic` peripheral is only present while the plug-in is installed. Uninstall after picking a skin to free the slot.
- The skin syncs to all clients automatically — no rejoin required.

---

## Capability Core Tiers

Cores bundle multiple plug-in peripherals into a single slot. They come in **Mark tiers** — each higher mark adds more peripherals while keeping the same item slot footprint.

**Mix and match:** A Sensor Core Mk1 + Navigation Core Mk1 gives you the same peripherals as Navigation Core Mk2, split across two slots. Upgrade one core at a time as resources allow.

**Duplicate peripherals:** If two installed cores both provide the same peripheral type (e.g. two cores with `inventory`), first-installed wins. Both remain findable via `peripheral.find()`, but the duplicate is silently ignored.

---

### Sensor Core — detection & awareness

| Mark | Item ID | Peripherals |
|---|---|---|
| **Mk1** | `sensor_core_mk1` | `scanner` |
| **Mk2** | `sensor_core_mk2` | `scanner`, `proximity` |

For full API docs see: [Scanner Plug-In](#scanner-plug-in), [Proximity Alert Plug-In](#proximity-alert-plug-in)

---

### Navigation Core — movement & orientation

| Mark | Item ID | Peripherals |
|---|---|---|
| **Mk1** | `navigation_core_mk1` | `pathing`, `compass` |
| **Mk2** | `navigation_core_mk2` | `pathing`, `compass`, `scanner`, `proximity` |

> Navigation Core Mk2 supersedes the former **Recon Core** (retired).

For full API docs see: [Pathing Plug-In](#pathing-plug-in), [Compass Plug-In](#compass-plug-in), [Scanner Plug-In](#scanner-plug-in), [Proximity Alert Plug-In](#proximity-alert-plug-in)

---

### Comms Core — signals & communication

| Mark | Item ID | Peripherals |
|---|---|---|
| **Mk1** | `comms_core_mk1` | `wireless`, `redstone` |
| **Mk2** | `comms_core_mk2` | `wireless`, `redstone`, `language` |
| **Mk3** | `comms_core_mk3` | `wireless`, `redstone`, `language`, `speaker` |

For full API docs see: [Wireless Plug-In](#wireless-plug-in), [Redstone Plug-In](#redstone-plug-in), [Language Plug-In](#language-plug-in), [Speaker Plug-In](#speaker-plug-in)

---

### Logistics Core — item storage & transfer

| Mark | Item ID | Peripherals |
|---|---|---|
| **Mk1** | `logistics_core_mk1` | `inventory` |
| **Mk2** | `logistics_core_mk2` | `inventory`, `storage` |

For full API docs see: [Inventory Plug-In](#inventory-plug-in), [Storage Link Plug-In](#storage-link-plug-in)

---

### Field Core — tool work & task automation

| Mark | Item ID | Peripherals |
|---|---|---|
| **Mk1** | `field_core_mk1` | `tool` |
| **Mk2** | `field_core_mk2` | `tool`, `inventory` |
| **Mk3** | `field_core_mk3` | `tool`, `inventory`, `storage` |

> Field Core Mk3 supersedes the former **Operations Core** (retired).

For full API docs see: [Tool Plug-In](#tool-plug-in), [Inventory Plug-In](#inventory-plug-in), [Storage Link Plug-In](#storage-link-plug-in)

---

## Standalone Cores

These cores don't follow the Mark tier system — each is a one-of-a-kind item with unique behaviour or special install logic that doesn't stack meaningfully with other peripherals.

---

### Anti-Grav Core
**Item:** `anti_grav_core` | **Peripheral type:** `anti_grav`

Enables gravity-free 3D navigation. Flight must be activated via Lua — installing the core alone does nothing. The Bangboo is immune to fall damage while this core is installed, regardless of flight state.

| Method | Returns | Description |
|---|---|---|
| `setEnabled(bool)` | — | Enable (`true`) or disable (`false`) flight mode |
| `isEnabled()` | `boolean` | Whether flight is currently active |
| `setBobbing(bool)` | — | Toggle idle hover oscillation |
| `getBobbing()` | `boolean` | Current bobbing state |

**Example:**
```lua
local ag = peripheral.find("anti_grav")
ag.setEnabled(true)
ag.setBobbing(true)

-- Fly up 10 blocks, then hover
local pos = bangboo.getPos()
bangboo.moveTo(pos.x, pos.y + 10, pos.z)
```

---

### Configuration Core
**Item:** `config_core` | **Peripheral type:** `config`

Exposes all Bangboo attributes for direct Lua control. Stats reset to defaults when the core is removed. `set()` changes the **base value** — other modifiers (e.g. Reinforcement Plug-In's +10 health) stack on top unchanged.

> **Future — Power System:** The Configuration Core will consume Bangboo power proportional to how far stats deviate from defaults. Power sources planned:
> - **Coal/fuel consumption** — the Bangboo burns fuel from its internal inventory over time
> - **Forge Energy (FE) integration** — a future "Bangboo Charging Dock" block will accept FE from any compatible generator mod and charge a Bangboo that docks with it
>
> For now the power cost is stubbed — all stat changes are free.

| Method | Returns | Description |
|---|---|---|
| `set(name, value)` | — | Set a stat's base value |
| `get(name)` | `number` | Get a stat's current base value |
| `reset(name)` | — | Reset one stat to its default |
| `resetAll()` | — | Reset all stats to defaults |
| `list()` | `table` | All stats: `{ value, default, min, max }` per entry |

**Configurable stats:**

| Name | Default | Min | Max | Notes |
|---|---|---|---|---|
| `max_health` | 20 | 1 | 200 | Clamps current HP on reduction |
| `movement_speed` | 0.25 | 0.01 | 1.5 | Ground walk speed |
| `flying_speed` | 0.6 | 0.01 | 2.0 | Anti-Grav flight speed |
| `follow_range` | 256 | 1 | 512 | Sensor/detection radius |
| `armor` | 0 | 0 | 30 | Damage reduction |
| `armor_toughness` | 0 | 0 | 20 | Armor penetration resistance |
| `knockback_resistance` | 0 | 0 | 1 | 1 = fully immune to knockback |
| `attack_damage` | 2 | 0 | 50 | Used by Tool Plug-In |
| `attack_speed` | 4 | 0.1 | 20 | Swing cooldown |
| `step_height` | 0.6 | 0.5 | 3.0 | Max step-up height without jumping |

**Example:**
```lua
local cfg = peripheral.find("config")

-- Print current stat sheet
for name, data in pairs(cfg.list()) do
    print(name .. ": " .. data.value .. " (default " .. data.default .. ")")
end

-- Tank build
cfg.set("max_health", 80)
cfg.set("armor", 15)
cfg.set("knockback_resistance", 0.8)

-- Undo one change, or wipe all
cfg.reset("armor")
cfg.resetAll()
```

---

### Command Block Core *(Creative only)*
**Item:** `command_core` | **Peripheral type:** `command`

Grants the Bangboo command block authority (permission level 2). No crafting recipe — obtain via creative menu or `/give`. All commands run as the Bangboo entity at its current position.

| Method | Returns | Description |
|---|---|---|
| `exec(command)` | `ok, { lines }` | Run a command; returns success flag and output lines |

**Example:**
```lua
local cmd = peripheral.find("command")

-- Simple commands
cmd.exec("say Bangboo online.")
cmd.exec("give @p minecraft:diamond 64")

-- Capture output
local ok, output = cmd.exec("data get entity @p")
for _, line in ipairs(output) do print(line) end

-- Position-relative builds using bangboo.getPos()
local pos = bangboo.getPos()
local x, y, z = math.floor(pos.x), math.floor(pos.y), math.floor(pos.z)
cmd.exec(("fill %d %d %d %d %d %d minecraft:stone"):format(x, y, z, x+5, y, z+5))
```

---

## Event Reference

Events fired by plug-ins that your Lua scripts can listen to with `os.pullEvent()`.

| Event | Arguments | Fired by |
|---|---|---|
| `bangboo_path_done` | `ok (bool), reason (string)` | Pathing — on arrival, timeout, or cancel |
| `proximity_enter` | `name (string), id (int), distance (number)` | Proximity — entity entered radius |
| `proximity_leave` | `name (string)` | Proximity — entity left radius |
| `modem_message` | `side, channel, replyChannel, message, distance` | Wireless — incoming message |
| `chat_message` | `playerName (string), message (string)` | Language — player sent public chat |
| `peripheral` | `name (string)` | Any plug-in installed at runtime |
| `peripheral_detach` | `name (string)` | Any plug-in removed at runtime |

**`bangboo_path_done` reason values:**

| Reason | Meaning |
|---|---|
| `"arrived"` | Bangboo reached the target position |
| `"timeout"` | Path took longer than 60 seconds (1200 ticks) |
| `"cancelled"` | `pathing.cancel()` was called |

---

## Slot Planning Reference

### Minimal Scout (4 slots, no hub)

| Slot | Installed | Provides |
|---|---|---|
| Base 1 | Navigation Core Mk2 | `pathing`, `compass`, `scanner`, `proximity` |
| Base 2 | Comms Core Mk1 | `wireless`, `redstone` |
| Base 3 | Lantern Plug-In | `lantern` |
| Base 4 | Language Plug-In | `language` |

### Budget Scout (4 slots — split cores, same coverage)

| Slot | Installed | Provides |
|---|---|---|
| Base 1 | Sensor Core Mk2 | `scanner`, `proximity` |
| Base 2 | Navigation Core Mk1 | `pathing`, `compass` |
| Base 3 | Wireless Plug-In | `wireless` |
| Base 4 | Lantern Plug-In | `lantern` |

> Same peripheral coverage as the Minimal Scout above, split across two cheaper cores instead of one expensive Mk2.

### Autonomous Worker (7 slots with Advanced Plug-In Hub)

| Slot | Installed | Provides |
|---|---|---|
| Base 1 | Advanced Plug-In Hub | +4 slots, Advanced terminal |
| Base 2 | Field Core Mk3 | `tool`, `inventory`, `storage` |
| Base 3 | Navigation Core Mk1 | `pathing`, `compass` |
| Base 4 | Sensor Core Mk2 | `scanner`, `proximity` |
| Extra 1 | Comms Core Mk1 | `wireless`, `redstone` |
| Extra 2 | Language Plug-In | `language` |
| Extra 3 | Lantern Plug-In | `lantern` |
| Extra 4 | Sprint Module Plug-In | *(speed boost)* |

### Conversational Companion (4 slots, no hub)

| Slot | Installed | Provides |
|---|---|---|
| Base 1 | Comms Core Mk3 | `wireless`, `redstone`, `language`, `speaker` |
| Base 2 | Advanced Terminal Plug-In | Advanced terminal |
| Base 3 | Cosmetic Plug-In | `cosmetic` |
| Base 4 | Reinforcement Plug-In | *(+10 health)* |

### Freight Bot (7 slots with Plug-In Hub)

| Slot | Installed | Provides |
|---|---|---|
| Base 1 | Plug-In Hub | +4 slots |
| Base 2 | Logistics Core Mk2 | `inventory`, `storage` |
| Base 3 | Navigation Core Mk1 | `pathing`, `compass` |
| Base 4 | Comms Core Mk1 | `wireless`, `redstone` |
| Extra 1 | Reinforcement Plug-In | *(+10 health)* |
| Extra 2 | Sprint Module Plug-In | *(speed boost)* |
| Extra 3 | Proximity Alert Plug-In | `proximity` |
| Extra 4 | Lantern Plug-In | `lantern` |
