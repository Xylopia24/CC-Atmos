package xylopia.core.computer.peripheral;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;

public class ProximityPeripheral extends BangbooPeripheral {
    public ProximityPeripheral(int computerID) { super(computerID); }

    @Override public String getType() { return "proximity"; }

    @Override
    public void detach(IComputerAccess computer) {
        var b = bangboo();
        if (b != null) b.setProximityEnabled(false);
    }

    @LuaFunction(mainThread = true)
    public final void enable() {
        var b = bangboo();
        if (b != null) b.setProximityEnabled(true);
    }

    @LuaFunction(mainThread = true)
    public final void disable() {
        var b = bangboo();
        if (b != null) b.setProximityEnabled(false);
    }

    @LuaFunction(mainThread = true)
    public final void setRadius(int radius) {
        var b = bangboo();
        if (b != null) b.setProximityRadius(radius);
    }

    @LuaFunction(mainThread = true)
    public final int getRadius() {
        var b = bangboo();
        return b != null ? b.getProximityRadius() : 8;
    }

    @LuaFunction(mainThread = true)
    public final boolean isEnabled() {
        var b = bangboo();
        return b != null && b.isProximityEnabled();
    }
}
