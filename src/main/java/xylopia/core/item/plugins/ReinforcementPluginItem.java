package xylopia.core.item.plugins;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import xylopia.core.entity.BangbooEntity;

public class ReinforcementPluginItem extends BangbooPluginItem {
    private static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("ccatmos", "reinforcement_health");
    private static final AttributeModifier MODIFIER =
        new AttributeModifier(ID, 10.0, AttributeModifier.Operation.ADD_VALUE);

    public ReinforcementPluginItem(Properties properties) { super(properties); }

    @Override
    public void onInstalled(BangbooEntity bangboo, int slot, ItemStack stack) {
        var attr = bangboo.getAttribute(Attributes.MAX_HEALTH);
        if (attr != null && !attr.hasModifier(ID)) attr.addPermanentModifier(MODIFIER);
    }

    @Override
    public void onRemoved(BangbooEntity bangboo, int slot, ItemStack stack) {
        var attr = bangboo.getAttribute(Attributes.MAX_HEALTH);
        if (attr != null) attr.removeModifier(ID);
        if (bangboo.getHealth() > bangboo.getMaxHealth())
            bangboo.setHealth(bangboo.getMaxHealth());
    }
}
