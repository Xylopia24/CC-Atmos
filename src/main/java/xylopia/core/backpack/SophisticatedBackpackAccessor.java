package xylopia.core.backpack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.LinkedHashMap;
import java.util.Map;

public class SophisticatedBackpackAccessor implements BackpackAccessor {
    private final ItemStack backpackStack;
    private final SimpleContainer container;
    private final IItemHandler handler;

    SophisticatedBackpackAccessor(ItemStack backpackStack, SimpleContainer container, IItemHandler handler) {
        this.backpackStack = backpackStack;
        this.container = container;
        this.handler = handler;
    }

    @Override
    public int getSize() { return handler.getSlots(); }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= handler.getSlots()) return ItemStack.EMPTY;
        return handler.getStackInSlot(slot);
    }

    @Override
    public ItemStack extract(int slot, int count) {
        if (slot < 0 || slot >= handler.getSlots()) return ItemStack.EMPTY;
        var extracted = handler.extractItem(slot, count, false);
        if (!extracted.isEmpty()) container.setChanged();
        return extracted;
    }

    @Override
    public ItemStack insert(int slot, ItemStack stack) {
        if (slot < 0 || slot >= handler.getSlots() || stack.isEmpty()) return stack;
        var remainder = handler.insertItem(slot, stack, false);
        if (remainder.getCount() != stack.getCount()) container.setChanged();
        return remainder;
    }

    @Override
    public Map<Integer, String> listUpgrades() {
        var result = new LinkedHashMap<Integer, String>();
        net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper
            .fromExistingData(backpackStack)
            .ifPresent(wrapper ->
                wrapper.getUpgradeHandler().getSlotWrappers().forEach((upgradeSlot, upgradeWrapper) -> {
                    var name = BuiltInRegistries.ITEM.getKey(upgradeWrapper.getUpgradeStack().getItem());
                    if (name != null) result.put(upgradeSlot, name.toString());
                })
            );
        return result;
    }

    @Override
    public boolean hasUpgrade(String itemId) {
        return listUpgrades().containsValue(itemId);
    }
}
