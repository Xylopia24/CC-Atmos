package xylopia.core.computer.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class RedstonePeripheral extends BangbooPeripheral {
    public RedstonePeripheral(int computerID) { super(computerID); }

    @Override public String getType() { return "redstone"; }

    @LuaFunction(mainThread = true)
    public final int getInput(String side) throws LuaException {
        var bangboo = bangboo();
        if (bangboo == null || !(bangboo.level() instanceof ServerLevel level)) return 0;
        Direction dir = parseDirection(side);
        if (dir == null) throw new LuaException("Invalid side: " + side);
        return level.getSignal(bangboo.blockPosition().relative(dir), dir);
    }

    @LuaFunction(mainThread = true)
    public final void setOutput(boolean powered) {
        var b = bangboo();
        if (b != null) b.setRedstoneOutput(powered);
    }

    @LuaFunction(mainThread = true)
    public final boolean getOutput() {
        var b = bangboo();
        return b != null && b.getRedstoneOutput();
    }

    @LuaFunction(mainThread = true)
    public final boolean getAnalogInput(String side) throws LuaException {
        var bangboo = bangboo();
        if (bangboo == null || !(bangboo.level() instanceof ServerLevel level)) return false;
        Direction dir = parseDirection(side);
        if (dir == null) throw new LuaException("Invalid side: " + side);
        return level.getBestNeighborSignal(bangboo.blockPosition().relative(dir)) > 0;
    }

    private static Direction parseDirection(String side) {
        return switch (side.toLowerCase()) {
            case "north" -> Direction.NORTH;
            case "south" -> Direction.SOUTH;
            case "east"  -> Direction.EAST;
            case "west"  -> Direction.WEST;
            case "up"    -> Direction.UP;
            case "down"  -> Direction.DOWN;
            default      -> null;
        };
    }
}
