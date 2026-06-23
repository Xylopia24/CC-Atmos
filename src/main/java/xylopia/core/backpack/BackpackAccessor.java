package xylopia.core.backpack;

import net.minecraft.world.item.ItemStack;

import java.util.Map;

public interface BackpackAccessor {
    int getSize();
    ItemStack getItem(int slot);
    ItemStack extract(int slot, int count);
    ItemStack insert(int slot, ItemStack stack);
    Map<Integer, String> listUpgrades();
    boolean hasUpgrade(String itemId);
}
