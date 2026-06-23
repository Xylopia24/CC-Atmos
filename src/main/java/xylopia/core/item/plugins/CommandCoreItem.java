package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import xylopia.core.computer.peripheral.CommandPeripheral;

import java.util.List;
import java.util.Map;

public class CommandCoreItem extends BangbooPluginItem {
    public CommandCoreItem(Properties properties) { super(properties); }

    @Override
    public Map<String, IPeripheral> createPeripherals(int computerID) {
        return Map.of("command", new CommandPeripheral(computerID));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ccatmos.command_core.desc").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("Creative only — no crafting recipe.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
