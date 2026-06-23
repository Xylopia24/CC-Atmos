package xylopia.core.computer.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StoragePeripheral extends BangbooPeripheral {
    public StoragePeripheral(int computerID) { super(computerID); }

    @Override public String getType() { return "storage"; }

    @LuaFunction(mainThread = true)
    public final Map<Integer, Object> scanStorage(String side) throws LuaException {
        var handler = getHandler(side);
        var result = new HashMap<Integer, Object>();
        for (int i = 0; i < handler.getSlots(); i++) {
            var stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                var entry = new HashMap<String, Object>();
                entry.put("name", stack.getDescriptionId());
                entry.put("count", stack.getCount());
                result.put(i + 1, entry);
            }
        }
        return result;
    }

    @LuaFunction(mainThread = true)
    public final int pull(String side, int slot, Optional<Integer> count) throws LuaException {
        var bangboo = requireBangboo();
        var handler = getHandler(side);
        int amount = count.orElse(64);
        var extracted = handler.extractItem(slot - 1, amount, false);
        if (extracted.isEmpty()) return 0;
        var remainder = bangboo.getInternalInventory().addItem(extracted);
        int transferred = extracted.getCount() - remainder.getCount();
        if (!remainder.isEmpty()) handler.insertItem(slot - 1, remainder, false);
        return transferred;
    }

    @LuaFunction(mainThread = true)
    public final int push(String side, int slot, Optional<Integer> count) throws LuaException {
        var bangboo = requireBangboo();
        var handler = getHandler(side);
        var source = bangboo.getInternalInventory().getItem(slot - 1);
        if (source.isEmpty()) return 0;
        int amount = Math.min(count.orElse(64), source.getCount());
        var toInsert = source.copyWithCount(amount);
        var remainder = ItemHandlerHelper.insertItemStacked(handler, toInsert, false);
        int transferred = amount - remainder.getCount();
        source.shrink(transferred);
        bangboo.getInternalInventory().setItem(slot - 1, source.isEmpty() ? ItemStack.EMPTY : source);
        return transferred;
    }

    private IItemHandler getHandler(String side) throws LuaException {
        var bangboo = bangboo();
        if (bangboo == null || !(bangboo.level() instanceof ServerLevel level))
            throw new LuaException("Not available");
        Direction dir = parseDirection(side);
        if (dir == null) throw new LuaException("Invalid side: " + side);
        var handler = level.getCapability(Capabilities.ItemHandler.BLOCK,
            bangboo.blockPosition().relative(dir), dir.getOpposite());
        if (handler == null) throw new LuaException("No inventory on " + side);
        return handler;
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
