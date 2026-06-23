-- bangboo  - Command-line control for your Bangboo's built-in API.
-- Only works on a Bangboo computer (where the 'bangboo' global is present).
--
-- Usage: bangboo <command> [args...]

if not bangboo then
    print("This program only works on a Bangboo computer.")
    return
end

local args = { ... }
local cmd  = args[1]

local function usage()
    print("Usage: bangboo <command> [args]")
    print("")
    print("  info           Name, ID, health, position")
    print("  name           Show current name")
    print("  name <text>    Set a new name (spaces allowed)")
    print("  pos            Current position and facing")
    print("  health         Health with percentage")
    print("  face <yaw>     Set facing (0=S 90=W 180=N 270=E)")
    print("  stop           Stop any current movement")
    print("  scan           Basic surroundings summary")
    print("  help           Show this message")
end

-- ── Commands ──────────────────────────────────────────────────────────────────

if cmd == nil or cmd == "help" then
    usage()

elseif cmd == "info" then
    local pos   = bangboo.getPos()
    local hp    = bangboo.getHealth()
    local maxHp = bangboo.getMaxHealth()
    print("Name   : " .. bangboo.getName())
    print("ID     : " .. bangboo.getID())
    print(("Health : %.1f / %.1f"):format(hp, maxHp))
    print(("Pos    : %.1f, %.1f, %.1f"):format(pos.x, pos.y, pos.z))
    print(("Facing : %.0f deg"):format(pos.yaw))
    print("Ground : " .. tostring(bangboo.isOnGround()))

elseif cmd == "name" then
    if args[2] then
        local name = table.concat(args, " ", 2)
        bangboo.setName(name)
        print("Name set to: " .. name)
    else
        print(bangboo.getName())
    end

elseif cmd == "pos" then
    local pos = bangboo.getPos()
    print(("X: %.2f  Y: %.2f  Z: %.2f"):format(pos.x, pos.y, pos.z))
    print(("Yaw: %.1f  Pitch: %.1f"):format(pos.yaw, pos.pitch))

elseif cmd == "health" then
    local hp  = bangboo.getHealth()
    local max = bangboo.getMaxHealth()
    print(("%.1f / %.1f  (%d%%)"):format(hp, max, math.floor(hp / max * 100)))

elseif cmd == "face" then
    local yaw = tonumber(args[2])
    if not yaw then
        print("Usage: bangboo face <yaw>")
        print("  0=south  90=west  180=north  270=east")
        return
    end
    bangboo.face(yaw)
    print(("Facing set to %.0f degrees."):format(yaw))

elseif cmd == "stop" then
    bangboo.stop()
    print("Stopped.")

elseif cmd == "scan" then
    local r = bangboo.scan()
    print("Nearby entities : " .. r.entities)
    if r.nearest >= 0 then
        print(("Nearest         : %.1f blocks"):format(r.nearest))
    else
        print("Nearest         : none")
    end
    print(("Health          : %d%%"):format(math.floor(r.health * 100)))

else
    print("Unknown command: " .. cmd)
    print("Run 'bangboo help' for usage.")
end
