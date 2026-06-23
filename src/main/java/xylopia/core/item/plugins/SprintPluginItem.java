package xylopia.core.item.plugins;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import xylopia.core.entity.BangbooEntity;

public class SprintPluginItem extends BangbooPluginItem {
    private static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("ccatmos", "sprint_speed");
    private static final AttributeModifier MODIFIER =
        new AttributeModifier(ID, 0.15, AttributeModifier.Operation.ADD_VALUE);

    public SprintPluginItem(Properties properties) { super(properties); }

    @Override
    public void onInstalled(BangbooEntity bangboo, int slot, ItemStack stack) {
        var attr = bangboo.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null && !attr.hasModifier(ID)) attr.addPermanentModifier(MODIFIER);
    }

    @Override
    public void onRemoved(BangbooEntity bangboo, int slot, ItemStack stack) {
        var attr = bangboo.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null) attr.removeModifier(ID);
    }
}
