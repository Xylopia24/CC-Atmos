package xylopia.core.computer.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandPeripheral extends BangbooPeripheral {
    public CommandPeripheral(int computerID) { super(computerID); }

    @Override public String getType() { return "command"; }

    // ── Command execution ─────────────────────────────────────────────────────

    /**
     * Execute a command as the Bangboo at permission level 2 (command block).
     * Returns success flag and a list of output lines.
     *
     * Usage:
     *   local ok, output = command.exec("say Hello!")
     *   local ok, output = command.exec("data get entity @p")
     */
    @LuaFunction(mainThread = true)
    public final Object[] exec(String cmd) throws LuaException {
        var bangboo = requireBangboo();
        var server  = bangboo.getServer();
        if (server == null) throw new LuaException("Not on a server");

        var capture = new OutputCapture();
        var source  = buildSource(bangboo, server, capture);

        server.getCommands().performPrefixedCommand(source, cmd);
        return new Object[]{ true, capture.lines() };
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static CommandSourceStack buildSource(
            xylopia.core.entity.BangbooEntity bangboo,
            MinecraftServer server,
            OutputCapture capture) {
        return new CommandSourceStack(
            capture,
            Vec3.atCenterOf(bangboo.blockPosition()),
            new Vec2(bangboo.getXRot(), bangboo.getYRot()),
            (net.minecraft.server.level.ServerLevel) bangboo.level(),
            2,                          // permission level — matches command block
            bangboo.getName().getString(),
            bangboo.getDisplayName(),
            server,
            bangboo
        );
    }

    // ── Output capture ────────────────────────────────────────────────────────

    private static class OutputCapture implements CommandSource {
        private final List<String> output = new ArrayList<>();

        @Override
        public void sendSystemMessage(Component message) {
            output.add(message.getString());
        }

        @Override public boolean acceptsSuccess()     { return true; }
        @Override public boolean acceptsFailure()     { return true; }
        @Override public boolean shouldInformAdmins() { return false; }

        /** Return output as a 1-indexed Lua-compatible list. */
        Map<Integer, String> lines() {
            var map = new java.util.LinkedHashMap<Integer, String>();
            for (int i = 0; i < output.size(); i++) map.put(i + 1, output.get(i));
            return map;
        }
    }
}
