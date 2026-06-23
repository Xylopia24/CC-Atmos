package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.InventoryPeripheral;
import xylopia.core.computer.peripheral.StoragePeripheral;

import java.util.Map;

public class LogisticsCoreMk2Item extends CapabilityCoreItem {
    public LogisticsCoreMk2Item(Properties properties) {
        super(properties, "item.ccatmos.inventory_plugin", "item.ccatmos.storage_plugin");
    }
    @Override public boolean providesInventory() { return true; }
    @Override public boolean providesStorage()   { return true; }
    @Override public Map<String, IPeripheral> createPeripherals(int computerID) {
        return Map.of("inventory", new InventoryPeripheral(computerID),
                      "storage",   new StoragePeripheral(computerID));
    }
}
