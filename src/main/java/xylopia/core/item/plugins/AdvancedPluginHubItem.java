package xylopia.core.item.plugins;

public class AdvancedPluginHubItem extends PluginHubItem {
    public AdvancedPluginHubItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean providesAdvancedTerminal() { return true; }
    // isHub() and onRemoved() are inherited from PluginHubItem
}
