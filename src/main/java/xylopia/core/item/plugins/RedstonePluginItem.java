package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.RedstonePeripheral;

import java.util.Map;

public class RedstonePluginItem extends BangbooPluginItem {
    public RedstonePluginItem(Properties properties) { super(properties); }
    @Override public boolean providesRedstone() { return true; }
    @Override public Map<String, IPeripheral> createPeripherals(int computerID) { return Map.of("redstone", new RedstonePeripheral(computerID)); }
}
