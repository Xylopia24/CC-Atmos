package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import xylopia.core.computer.peripheral.AntiGravPeripheral;
import xylopia.core.entity.BangbooEntity;

import java.util.List;
import java.util.Map;

public class AntiGravCoreItem extends BangbooPluginItem {
    public AntiGravCoreItem(Properties properties) { super(properties); }

    @Override
    public Map<String, IPeripheral> createPeripherals(int computerID) {
        return Map.of("anti_grav", new AntiGravPeripheral(computerID));
    }

    @Override
    public void onInstalled(BangbooEntity bangboo, int slot, ItemStack stack) {
        bangboo.setAntiGrav(true);
    }

    @Override
    public void onRemoved(BangbooEntity bangboo, int slot, ItemStack stack) {
        bangboo.setAntiGrav(false);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ccatmos.antigrav.desc")
            .withStyle(ChatFormatting.GRAY));
    }
}
