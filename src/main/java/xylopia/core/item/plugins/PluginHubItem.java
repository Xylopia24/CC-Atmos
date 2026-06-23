package xylopia.core.item.plugins;

import net.minecraft.world.item.ItemStack;
import xylopia.core.entity.BangbooEntity;
import xylopia.core.menu.BangbooMenu;

public class PluginHubItem extends BangbooPluginItem {

    public PluginHubItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isHub() { return true; }

    @Override
    public void onRemoved(BangbooEntity bangboo, int slot, ItemStack removedStack) {
        var container = bangboo.getPluginSlots();
        for (int i = BangbooMenu.BASE_PLUGIN_COUNT; i < BangbooMenu.TOTAL_PLUGIN_COUNT; i++) {
            ItemStack extra = container.getItem(i);
            if (!extra.isEmpty()) {
                if (extra.getItem() instanceof BangbooPluginItem plugin)
                    plugin.onRemoved(bangboo, i, extra);
                bangboo.spawnAtLocation(extra);
                container.setItem(i, ItemStack.EMPTY);
            }
        }
    }
}
