package xylopia.core.computer.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import xylopia.core.skin.BangbooSkinRegistry;


public class CosmeticPeripheral extends BangbooPeripheral {
    public CosmeticPeripheral(int computerID) { super(computerID); }

    @Override public String getType() { return "cosmetic"; }

    @LuaFunction(mainThread = true)
    public final String getSkin() throws LuaException {
        return requireBangboo().getSkinId();
    }

    @LuaFunction(mainThread = true)
    public final void setSkin(String id) throws LuaException {
        requireBangboo().setSkinId(id);
    }

    @LuaFunction
    public final String[] listSkins() {
        return BangbooSkinRegistry.INSTANCE.ids().stream()
                .map(Object::toString)
                .sorted()
                .toArray(String[]::new);
    }
}
