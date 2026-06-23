package xylopia.core.item.plugins;

public class AdvancedTerminalPluginItem extends BangbooPluginItem {
    public AdvancedTerminalPluginItem(Properties properties) {
        super(properties);
    }
    @Override
    public boolean providesAdvancedTerminal() { return true; }
}
