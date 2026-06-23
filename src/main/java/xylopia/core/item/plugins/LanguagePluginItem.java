package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.LanguagePeripheral;

import java.util.Map;

public class LanguagePluginItem extends BangbooPluginItem {
    public LanguagePluginItem(Properties properties) { super(properties); }
    @Override public boolean providesLanguage() { return true; }
    @Override public Map<String, IPeripheral> createPeripherals(int computerID) { return Map.of("language", new LanguagePeripheral(computerID)); }
}
