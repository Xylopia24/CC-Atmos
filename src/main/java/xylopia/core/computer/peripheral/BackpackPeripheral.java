package xylopia.core.computer.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import xylopia.core.backpack.BackpackAccessor;
import xylopia.core.backpack.BackpackAccessors;
import xylopia.core.entity.BangbooEntity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class BackpackPeripheral extends BangbooPeripheral {
    public BackpackPeripheral(int computerID) { super(computerID); }

    @Override public String getType() { return "backpack"; }

    @LuaFunction(mainThread = true)
    public final boolean hasBackpack() throws LuaException {
        var b = requireBangboo();
        return BackpackAccessors.isValidBackpack(b.getBackpackSlot().getItem(0));
    }

    @LuaFunction(mainThread = true)
    public final int size() throws LuaException {
        return accessor().getSize();
    }

    @LuaFunction(mainThread = true)
    public final Map<Integer, Object> list() throws LuaException {
        var acc = accessor();
        var result = new LinkedHashMap<Integer, Object>();
        for (int i = 0; i < acc.getSize(); i++) {
            var stack = acc.getItem(i);
            if (!stack.isEmpty()) result.put(i + 1, itemToMap(stack));
        }
        return result;
    }

    @LuaFunction(mainThread = true)
    public final Object getItem(int slot) throws LuaException {
        var stack = accessor().getItem(slot - 1);
        return stack.isEmpty() ? null : itemToMap(stack);
    }

    /** Move an item from Bangboo's internal inventory into the backpack. */
    @LuaFunction(mainThread = true)
    public final int pushItem(int fromInternalSlot, int toBackpackSlot, Optional<Integer> limit) throws LuaException {
        var b = requireBangboo();
        var inv = b.getInternalInventory();
        if (fromInternalSlot < 1 || fromInternalSlot > inv.getContainerSize())
            throw new LuaException("Invalid internal slot: " + fromInternalSlot);
        var acc = accessor(b);
        if (toBackpackSlot < 1 || toBackpackSlot > acc.getSize())
            throw new LuaException("Invalid backpack slot " + toBackpackSlot + " (size: " + acc.getSize() + ")");

        var source = inv.getItem(fromInternalSlot - 1);
        if (source.isEmpty()) return 0;
        int toMove = Math.min(limit.orElse(source.getMaxStackSize()), source.getCount());
        var remainder = acc.insert(toBackpackSlot - 1, source.copyWithCount(toMove));
        int transferred = toMove - remainder.getCount();
        if (transferred > 0) {
            source.shrink(transferred);
            inv.setChanged();
        }
        return transferred;
    }

    /** Move an item from the backpack into Bangboo's internal inventory. */
    @LuaFunction(mainThread = true)
    public final int pullItem(int fromBackpackSlot, int toInternalSlot, Optional<Integer> limit) throws LuaException {
        var b = requireBangboo();
        var inv = b.getInternalInventory();
        if (toInternalSlot < 1 || toInternalSlot > inv.getContainerSize())
            throw new LuaException("Invalid internal slot: " + toInternalSlot);
        var acc = accessor(b);
        if (fromBackpackSlot < 1 || fromBackpackSlot > acc.getSize())
            throw new LuaException("Invalid backpack slot: " + fromBackpackSlot);

        var inBackpack = acc.getItem(fromBackpackSlot - 1);
        if (inBackpack.isEmpty()) return 0;

        var dest = inv.getItem(toInternalSlot - 1);
        if (!dest.isEmpty() && !ItemStack.isSameItemSameComponents(dest, inBackpack)) return 0;

        int canAccept = dest.isEmpty()
            ? inBackpack.getMaxStackSize()
            : inBackpack.getMaxStackSize() - dest.getCount();
        int toMove = Math.min(limit.orElse(inBackpack.getMaxStackSize()), Math.min(inBackpack.getCount(), canAccept));
        if (toMove <= 0) return 0;

        var extracted = acc.extract(fromBackpackSlot - 1, toMove);
        if (extracted.isEmpty()) return 0;

        if (dest.isEmpty()) {
            inv.setItem(toInternalSlot - 1, extracted);
        } else {
            dest.grow(extracted.getCount());
            inv.setChanged();
        }
        return extracted.getCount();
    }

    @LuaFunction(mainThread = true)
    public final Map<Integer, String> listUpgrades() throws LuaException {
        return accessor().listUpgrades();
    }

    @LuaFunction(mainThread = true)
    public final boolean hasUpgrade(String id) throws LuaException {
        return accessor().hasUpgrade(id);
    }

    private BackpackAccessor accessor() throws LuaException {
        return accessor(requireBangboo());
    }

    private BackpackAccessor accessor(BangbooEntity b) throws LuaException {
        var slot = b.getBackpackSlot();
        return BackpackAccessors.from(slot.getItem(0), slot)
            .orElseThrow(() -> new LuaException("No backpack installed. Place a Shulker Box or Sophisticated Backpack in the backpack slot."));
    }

    private Map<String, Object> itemToMap(ItemStack stack) {
        var map = new LinkedHashMap<String, Object>();
        map.put("name", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        map.put("count", stack.getCount());
        return map;
    }
}
