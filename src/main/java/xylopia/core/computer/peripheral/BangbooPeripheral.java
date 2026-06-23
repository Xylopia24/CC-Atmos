package xylopia.core.computer.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.BangbooComputerRegistry;
import xylopia.core.entity.BangbooEntity;

import javax.annotation.Nullable;

/** Base class for all Bangboo plug-in peripherals. */
public abstract class BangbooPeripheral implements IPeripheral {
    protected final int computerID;

    protected BangbooPeripheral(int computerID) { this.computerID = computerID; }

    protected @Nullable BangbooEntity bangboo() {
        return BangbooComputerRegistry.get(computerID);
    }

    protected BangbooEntity requireBangboo() throws LuaException {
        var b = BangbooComputerRegistry.get(computerID);
        if (b == null) throw new LuaException("Bangboo not available");
        return b;
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other != null && other.getClass() == getClass()
            && ((BangbooPeripheral) other).computerID == computerID;
    }
}
