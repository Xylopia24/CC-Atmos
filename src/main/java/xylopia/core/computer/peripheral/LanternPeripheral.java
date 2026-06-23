package xylopia.core.computer.peripheral;

import dan200.computercraft.api.lua.LuaFunction;

public class LanternPeripheral extends BangbooPeripheral {
    public LanternPeripheral(int computerID) { super(computerID); }

    @Override public String getType() { return "lantern"; }

    @LuaFunction(mainThread = true)
    public final void on() { var b = bangboo(); if (b != null) b.setLanternOn(true); }

    @LuaFunction(mainThread = true)
    public final void off() { var b = bangboo(); if (b != null) b.setLanternOn(false); }

    @LuaFunction(mainThread = true)
    public final void toggle() { var b = bangboo(); if (b != null) b.toggleLantern(); }

    @LuaFunction(mainThread = true)
    public final void setLevel(int brightness) { var b = bangboo(); if (b != null) b.setLanternLevel(brightness); }

    @LuaFunction(mainThread = true)
    public final int getLevel() { var b = bangboo(); return b != null ? b.getLanternLevel() : 0; }

    @LuaFunction(mainThread = true)
    public final boolean isOn() { var b = bangboo(); return b != null && b.isLanternOn(); }
}
