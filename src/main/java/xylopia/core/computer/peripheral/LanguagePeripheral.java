package xylopia.core.computer.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import xylopia.core.computer.BangbooLanguageNetwork;

import java.util.ArrayList;
import java.util.List;

public class LanguagePeripheral extends BangbooPeripheral {
    public LanguagePeripheral(int computerID) { super(computerID); }

    @Override public String getType() { return "language"; }

    @Override
    public void detach(IComputerAccess computer) { BangbooLanguageNetwork.stopListening(computerID); }

    @LuaFunction(mainThread = true)
    public final void say(String message) throws LuaException {
        var bangboo = bangboo();
        if (bangboo == null || !(bangboo.level() instanceof ServerLevel level))
            throw new LuaException("Not available");
        var display = Component.literal("<" + bangboo.getDisplayName().getString() + "> " + message);
        for (var player : level.players()) player.sendSystemMessage(display);
    }

    @LuaFunction(mainThread = true)
    public final boolean whisper(String playerName, String message) throws LuaException {
        var bangboo = bangboo();
        if (bangboo == null || !(bangboo.level() instanceof ServerLevel level))
            throw new LuaException("Not available");
        var target = level.getServer().getPlayerList().getPlayerByName(playerName);
        if (target == null) return false;
        target.sendSystemMessage(Component.literal(
            "[" + bangboo.getDisplayName().getString() + " whispers] " + message));
        return true;
    }

    @LuaFunction(mainThread = true)
    public final List<String> getPlayers() throws LuaException {
        var bangboo = bangboo();
        if (bangboo == null || !(bangboo.level() instanceof ServerLevel level))
            throw new LuaException("Not available");
        var names = new ArrayList<String>();
        for (var player : level.players()) names.add(player.getName().getString());
        return names;
    }

    @LuaFunction
    public final void listen() { BangbooLanguageNetwork.startListening(computerID); }

    @LuaFunction
    public final void unlisten() { BangbooLanguageNetwork.stopListening(computerID); }

    @LuaFunction
    public final boolean isListening() { return BangbooLanguageNetwork.isListening(computerID); }
}
