# CC: Atmos — Custom Bangboo Skins

Custom skins are fully data-driven and live in resource packs — no Java required. A skin is a JSON definition that points to a GeckoLib geo model, texture, and animation file.

---

## File Layout

```
assets/
└── <your_namespace>/
    ├── bangboo_skin/
    │   └── my_skin.json          ← skin definition (this is what you register)
    ├── geo/
    │   └── my_skin.geo.json      ← GeckoLib block/entity model
    ├── textures/
    │   └── entity/
    │       └── skins/
    │           └── my_skin.png   ← texture atlas (match your geo's UV layout)
    └── animations/
        └── my_skin.animation.json ← GeckoLib animation file
```

Your namespace can be `ccatmos` (to override built-in skins), your own mod ID, or any valid resource pack namespace.

---

## The Skin Definition JSON

`assets/<namespace>/bangboo_skin/<id>.json`

```json
{
  "geo":       "ccatmos:geo/my_skin.geo.json",
  "texture":   "ccatmos:textures/entity/skins/my_skin.png",
  "animation": "ccatmos:animations/my_skin.animation.json",
  "supports":  ["idle", "walk", "swim"]
}
```

| Field       | Required | Description |
|-------------|----------|-------------|
| `geo`       | ✅        | ResourceLocation of the GeckoLib `.geo.json` model |
| `texture`   | ✅        | ResourceLocation of the PNG texture |
| `animation` | ✅        | ResourceLocation of the GeckoLib `.animation.json` file |
| `supports`  | ⬜ optional | List of animation short names this skin implements (see below). Defaults to `["idle", "walk"]` if omitted. |

The full skin ID used in-game is `<namespace>:<id>`, e.g. `ccatmos:eous` or `mypack:robo`.

---

## Valid Animation Names

Each entry in `"supports"` tells the game which animations actually exist in your animation file. If a condition is met (e.g. the Bangboo enters water) but the skin doesn't declare `"swim"`, the game silently falls back to `"idle"` instead of erroring.

Animation names inside the `.animation.json` file must follow the pattern:

```
animation.bangboo.<short_name>
```

| Short name   | GeckoLib key                    | When it plays |
|--------------|---------------------------------|---------------|
| `idle`       | `animation.bangboo.idle`        | Standing still (always required) |
| `walk`       | `animation.bangboo.walk`        | Moving on the ground |
| `run`        | `animation.bangboo.run`         | Moving fast (Sprint Plug-In active) |
| `fly`        | `animation.bangboo.fly`         | Airborne (AntiGrav Core / Jetpack upgrade) |
| `swim`       | `animation.bangboo.swim`        | Submerged in water |
| `jump`       | `animation.bangboo.jump`        | Rising after a jump |
| `fall`       | `animation.bangboo.fall`        | Falling downward |
| `tool_swing` | `animation.bangboo.tool_swing`  | Using a tool (Tool Plug-In) |
| `hurt`       | `animation.bangboo.hurt`        | Taking damage |
| `death`      | `animation.bangboo.death`       | Death sequence |

> **`idle` is the universal fallback.** Every skin must have an `idle` animation or the Bangboo will display its last played animation indefinitely.

---

## Minimal Example

A skin that only has idle and walk (the safe starting point):

**`assets/mypack/bangboo_skin/mybot.json`**
```json
{
  "geo":       "mypack:geo/mybot.geo.json",
  "texture":   "mypack:textures/entity/skins/mybot.png",
  "animation": "mypack:animations/mybot.animation.json",
  "supports":  ["idle", "walk"]
}
```

**`assets/mypack/animations/mybot.animation.json`** (GeckoLib format)
```json
{
  "format_version": "1.8.0",
  "animations": {
    "animation.bangboo.idle": {
      "loop": true,
      "animation_length": 2.0,
      "bones": { }
    },
    "animation.bangboo.walk": {
      "loop": true,
      "animation_length": 1.0,
      "bones": { }
    }
  }
}
```

---

## Applying a Skin In-Game

Skins are set at runtime via the **Cosmetic Plug-In** and a CC:Tweaked script running on the Bangboo:

```lua
local cosmetic = peripheral.find("cosmetic")
if cosmetic then
    cosmetic.setSkin("mypack:mybot")
    print("Skin applied:", cosmetic.getSkin())
end
```

The skin ID is `<namespace>:<json_filename_without_extension>`.

The active skin persists in the Bangboo's NBT and is synced to all clients automatically.

---

## Overriding Built-In Skins

To replace the default Eous skin, create `assets/ccatmos/bangboo_skin/eous.json` in your resource pack. Resource pack priority applies — the highest-priority pack wins. The fallback hardcoded in Java always points to Eous, so at minimum Eous's files should remain valid.

---

## Tools

- **Blockbench** — use the *GeckoLib Entity Model* format for the `.geo.json` and `.animation.json` files.
- The geo and animation files must target the same bone structure for animations to work correctly.
- UV mapping in Blockbench exports directly as a compatible `.png` texture.
