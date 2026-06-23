package xylopia.core.backpack;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;

import java.util.Optional;

public final class BackpackAccessors {
    private static final boolean SB_LOADED = ModList.get().isLoaded("sophisticatedbackpacks");

    private BackpackAccessors() {}

    public static Optional<BackpackAccessor> from(ItemStack stack, SimpleContainer container) {
        if (stack.isEmpty()) return Optional.empty();
        if (isShulkerBox(stack)) return Optional.of(new ShulkerBoxAccessor(stack, container));
        if (SB_LOADED && isSophisticatedBackpack(stack)) {
            var handler = stack.getCapability(Capabilities.ItemHandler.ITEM, null);
            if (handler != null) return Optional.of(new SophisticatedBackpackAccessor(stack, container, handler));
        }
        return Optional.empty();
    }

    public static boolean isValidBackpack(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (isShulkerBox(stack)) return true;
        if (SB_LOADED && isSophisticatedBackpack(stack)) return true;
        return false;
    }

    private static boolean isShulkerBox(ItemStack stack) {
        return stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock;
    }

    private static boolean isSophisticatedBackpack(ItemStack stack) {
        return stack.getItem() instanceof net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
    }
}
