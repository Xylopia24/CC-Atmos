package xylopia.core.computer.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InventoryPeripheral extends BangbooPeripheral {
    public InventoryPeripheral(int computerID) { super(computerID); }

    @Override public String getType() { return "inventory"; }

    @LuaFunction(mainThread = true)
    public final Map<Integer, Object> list() throws LuaException {
        var inv = requireBangboo().getInternalInventory();
        var result = new HashMap<Integer, Object>();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                var entry = new HashMap<String, Object>();
                entry.put("name", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                entry.put("count", stack.getCount());
                result.put(i + 1, entry);
            }
        }
        return result;
    }

    @LuaFunction(mainThread = true)
    public final int size() throws LuaException {
        return requireBangboo().getInternalInventory().getContainerSize();
    }

    @LuaFunction(mainThread = true)
    public final boolean isEmpty() throws LuaException {
        var inv = requireBangboo().getInternalInventory();
        for (int i = 0; i < inv.getContainerSize(); i++)
            if (!inv.getItem(i).isEmpty()) return false;
        return true;
    }

    @LuaFunction(mainThread = true)
    public final void equip(int slot) throws LuaException {
        var b = requireBangboo();
        if (slot < 1 || slot > b.getInternalInventory().getContainerSize())
            throw new LuaException("Slot " + slot + " out of range");
        b.setEquippedSlot(slot - 1);
    }

    @LuaFunction(mainThread = true)
    public final Map<String, Object> getHeld() throws LuaException {
        var b = requireBangboo();
        ItemStack held = b.getInternalInventory().getItem(b.getEquippedSlot());
        var map = new HashMap<String, Object>();
        map.put("name", held.isEmpty() ? "minecraft:air"
            : BuiltInRegistries.ITEM.getKey(held.getItem()).toString());
        map.put("count", held.isEmpty() ? 0 : held.getCount());
        return map;
    }

    @LuaFunction(mainThread = true)
    public final void drop(int slot, Optional<Integer> count) throws LuaException {
        var b = requireBangboo();
        var inv = b.getInternalInventory();
        int index = slot - 1;
        if (index < 0 || index >= inv.getContainerSize())
            throw new LuaException("Slot " + slot + " out of range");
        ItemStack stack = inv.getItem(index);
        if (stack.isEmpty()) return;
        int amount = Math.min(count.orElse(stack.getCount()), stack.getCount());
        b.spawnAtLocation(stack.split(amount));
        inv.setItem(index, stack);
    }
}
