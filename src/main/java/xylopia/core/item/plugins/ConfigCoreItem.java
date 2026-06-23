package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import xylopia.core.computer.peripheral.ConfigPeripheral;
import xylopia.core.entity.BangbooEntity;

import java.util.List;
import java.util.Map;

public class ConfigCoreItem extends BangbooPluginItem {
    public ConfigCoreItem(Properties properties) { super(properties); }

    @Override
    public Map<String, IPeripheral> createPeripherals(int computerID) {
        return Map.of("config", new ConfigPeripheral(computerID));
    }

    @Override
    public void onRemoved(BangbooEntity bangboo, int slot, ItemStack stack) {
        // Reset all configured stats when the core is pulled out
        ConfigPeripheral.resetAllFor(bangboo);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ccatmos.config_core.desc").withStyle(ChatFormatting.GRAY));
        // TODO: show power draw once energy system is implemented
    }
}
