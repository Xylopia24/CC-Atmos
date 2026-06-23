package xylopia.core.backpack;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.Map;

public class ShulkerBoxAccessor implements BackpackAccessor {
    private static final int SIZE = 27;

    private final ItemStack backpackStack;
    private final SimpleContainer container;

    ShulkerBoxAccessor(ItemStack backpackStack, SimpleContainer container) {
        this.backpackStack = backpackStack;
        this.container = container;
    }

    @Override
    public int getSize() { return SIZE; }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= SIZE) return ItemStack.EMPTY;
        return readItems().get(slot);
    }

    @Override
    public ItemStack extract(int slot, int count) {
        if (slot < 0 || slot >= SIZE || count <= 0) return ItemStack.EMPTY;
        var items = readItems();
        var existing = items.get(slot);
        if (existing.isEmpty()) return ItemStack.EMPTY;
        int toTake = Math.min(count, existing.getCount());
        var extracted = existing.copyWithCount(toTake);
        existing.shrink(toTake);
        if (existing.isEmpty()) items.set(slot, ItemStack.EMPTY);
        writeItems(items);
        return extracted;
    }

    @Override
    public ItemStack insert(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SIZE || stack.isEmpty()) return stack;
        var items = readItems();
        var existing = items.get(slot);
        if (existing.isEmpty()) {
            int toPlace = Math.min(stack.getCount(), stack.getMaxStackSize());
            items.set(slot, stack.copyWithCount(toPlace));
            writeItems(items);
            return stack.copyWithCount(stack.getCount() - toPlace);
        } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
            int canAdd = existing.getMaxStackSize() - existing.getCount();
            int toAdd = Math.min(canAdd, stack.getCount());
            if (toAdd > 0) {
                existing.grow(toAdd);
                writeItems(items);
                return stack.copyWithCount(stack.getCount() - toAdd);
            }
        }
        return stack;
    }

    @Override
    public Map<Integer, String> listUpgrades() { return Map.of(); }

    @Override
    public boolean hasUpgrade(String itemId) { return false; }

    private NonNullList<ItemStack> readItems() {
        var items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        var contents = backpackStack.get(DataComponents.CONTAINER);
        if (contents != null) contents.copyInto(items);
        return items;
    }

    private void writeItems(NonNullList<ItemStack> items) {
        backpackStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
        container.setChanged();
    }
}
