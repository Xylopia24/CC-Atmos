package xylopia.core.item.plugins;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public abstract class CapabilityCoreItem extends BangbooPluginItem {
    private final String[] bundledKeys;

    public CapabilityCoreItem(Properties properties, String... bundledKeys) {
        super(properties);
        this.bundledKeys = bundledKeys;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ccatmos.core.includes")
            .withStyle(ChatFormatting.GRAY));
        for (String key : bundledKeys) {
            tooltip.add(Component.literal("  • ")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.translatable(key).withStyle(ChatFormatting.AQUA)));
        }
    }
}
