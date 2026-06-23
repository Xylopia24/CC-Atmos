package xylopia.core.computer.peripheral;

import dan200.computercraft.api.lua.LuaFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CompassPeripheral extends BangbooPeripheral {
    public CompassPeripheral(int computerID) { super(computerID); }

    @Override public String getType() { return "compass"; }

    @LuaFunction(mainThread = true)
    public final void mark(String label, int x, int y, int z) {
        var b = bangboo();
        if (b != null) b.setWaypoint(label, x, y, z);
    }

    @LuaFunction(mainThread = true)
    public final boolean remove(String label) {
        var b = bangboo();
        return b != null && b.removeWaypoint(label);
    }

    @LuaFunction(mainThread = true)
    public final Map<String, Object> getWaypoints() {
        var b = bangboo();
        if (b == null) return Map.of();
        var result = new HashMap<String, Object>();
        for (var entry : b.getWaypoints().entrySet()) {
            int[] pos = entry.getValue();
            var posMap = new HashMap<String, Object>();
            posMap.put("x", pos[0]); posMap.put("y", pos[1]); posMap.put("z", pos[2]);
            result.put(entry.getKey(), posMap);
        }
        return result;
    }

    @LuaFunction(mainThread = true)
    public final Optional<Double> distanceTo(String label) {
        var b = bangboo();
        if (b == null) return Optional.empty();
        int[] pos = b.getWaypoints().get(label);
        if (pos == null) return Optional.empty();
        double dx = b.getX() - pos[0], dy = b.getY() - pos[1], dz = b.getZ() - pos[2];
        return Optional.of(Math.sqrt(dx*dx + dy*dy + dz*dz));
    }

    @LuaFunction(mainThread = true)
    public final Optional<Double> facingTo(String label) {
        var b = bangboo();
        if (b == null) return Optional.empty();
        int[] pos = b.getWaypoints().get(label);
        if (pos == null) return Optional.empty();
        double dx = pos[0] - b.getX(), dz = pos[2] - b.getZ();
        double yaw = Math.toDegrees(Math.atan2(-dx, dz));
        if (yaw < 0) yaw += 360;
        return Optional.of(yaw);
    }
}
