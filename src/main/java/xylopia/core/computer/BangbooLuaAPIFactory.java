package xylopia.core.computer;

import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.ILuaAPIFactory;
import dan200.computercraft.api.lua.IComputerSystem;

import javax.annotation.Nullable;

/** Registered once at startup; injects bangboo APIs into any Bangboo computer that boots. */
public class BangbooLuaAPIFactory implements ILuaAPIFactory {

    @Override
    @Nullable
    public ILuaAPI create(IComputerSystem system) {
        int id = system.getID();
        if (BangbooComputerRegistry.get(id) == null) return null;
        return new BangbooBaseAPI(id);
    }
}
