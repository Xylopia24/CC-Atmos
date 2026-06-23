package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.InventoryPeripheral;

import java.util.Map;

public class LogisticsCoreMk1Item extends CapabilityCoreItem {
    public LogisticsCoreMk1Item(Properties properties) {
        super(properties, "item.ccatmos.inventory_plugin");
    }
    @Override public boolean providesInventory() { return true; }
    @Override public Map<String, IPeripheral> createPeripherals(int computerID) {
        return Map.of("inventory", new InventoryPeripheral(computerID));
    }
}
