package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.InventoryPeripheral;

import java.util.Map;

public class InventoryPluginItem extends BangbooPluginItem {
    public InventoryPluginItem(Properties properties) { super(properties); }
    @Override public boolean providesInventory() { return true; }
    @Override public Map<String, IPeripheral> createPeripherals(int computerID) { return Map.of("inventory", new InventoryPeripheral(computerID)); }
}
