package xylopia.core.computer.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;

public class AntiGravPeripheral extends BangbooPeripheral {
    public AntiGravPeripheral(int computerID) { super(computerID); }

    @Override public String getType() { return "anti_grav"; }

    @LuaFunction(mainThread = true)
    public final void setEnabled(boolean enable) throws LuaException {
        requireBangboo().setAntiGrav(enable);
    }

    @LuaFunction
    public final boolean isEnabled() throws LuaException {
        return requireBangboo().isAntiGravActive();
    }

    @LuaFunction(mainThread = true)
    public final void setBobbing(boolean bob) throws LuaException {
        requireBangboo().setAntiGravBobbing(bob);
    }

    @LuaFunction
    public final boolean getBobbing() throws LuaException {
        return requireBangboo().isAntiGravBobbing();
    }
}
