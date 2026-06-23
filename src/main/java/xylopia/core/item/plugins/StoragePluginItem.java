package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.StoragePeripheral;

import java.util.Map;

public class StoragePluginItem extends BangbooPluginItem {
    public StoragePluginItem(Properties properties) { super(properties); }
    @Override public boolean providesStorage() { return true; }
    @Override public Map<String, IPeripheral> createPeripherals(int computerID) { return Map.of("storage", new StoragePeripheral(computerID)); }
}
