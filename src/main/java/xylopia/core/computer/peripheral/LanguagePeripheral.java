package xylopia.core.computer.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import xylopia.core.computer.BangbooLanguageNetwork;

import java.util.ArrayList;
import java.util.List;

public class LanguagePeripheral extends BangbooPeripheral {
    public LanguagePeripheral(int computerID) { super(computerID); }

    @Override public String getType() { return "language"; }

    @Override
    public void detach(IComputerAccess computer) { BangbooLanguageNetwork.stopListening(computerID); }

    /**
     * Parses & color codes (e.g. &c = red, &l = bold, &r = reset).
     * Both & and § are accepted as the prefix character.
     */
    private static Component parseFormatted(String raw) {
        MutableComponent result = Component.empty();
        Style current = Style.EMPTY;
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if ((c == '&' || c == '§') && i + 1 < raw.length()) {
                ChatFormatting fmt = ChatFormatting.getByCode(Character.toLowerCase(raw.charAt(i + 1)));
                if (fmt != null) {
                    if (!text.isEmpty()) {
                        result = result.append(Component.literal(text.toString()).withStyle(current));
                        text.setLength(0);
                    }
                    current = (fmt == ChatFormatting.RESET) ? Style.EMPTY : current.applyFormat(fmt);
                    i++;
                    continue;
                }
            }
            text.append(c);
        }
        if (!text.isEmpty()) result = result.append(Component.literal(text.toString()).withStyle(current));
        return result;
    }

    @LuaFunction(mainThread = true)
    public final void say(String message) throws LuaException {
        var bangboo = bangboo();
        if (bangboo == null || !(bangboo.level() instanceof ServerLevel level))
            throw new LuaException("Not available");
        MutableComponent display = Component.empty()
                .append(Component.literal("<" + bangboo.getDisplayName().getString() + "> "))
                .append(parseFormatted(message));
        for (var player : level.players()) player.sendSystemMessage(display);
    }

    @LuaFunction(mainThread = true)
    public final boolean whisper(String playerName, String message) throws LuaException {
        var bangboo = bangboo();
        if (bangboo == null || !(bangboo.level() instanceof ServerLevel level))
            throw new LuaException("Not available");
        var target = level.getServer().getPlayerList().getPlayerByName(playerName);
        if (target == null) return false;
        MutableComponent display = Component.empty()
                .append(Component.literal("[" + bangboo.getDisplayName().getString() + " whispers] "))
                .append(parseFormatted(message));
        target.sendSystemMessage(display);
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
