package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.InventoryPeripheral;
import xylopia.core.computer.peripheral.StoragePeripheral;
import xylopia.core.computer.peripheral.ToolPeripheral;

import java.util.Map;

public class FieldCoreMk3Item extends CapabilityCoreItem {
    public FieldCoreMk3Item(Properties properties) {
        super(properties, "item.ccatmos.tool_plugin", "item.ccatmos.inventory_plugin",
              "item.ccatmos.storage_plugin");
    }
    @Override public boolean providesTool()      { return true; }
    @Override public boolean providesInventory() { return true; }
    @Override public boolean providesStorage()   { return true; }
    @Override public Map<String, IPeripheral> createPeripherals(int computerID) {
        return Map.of("tool",      new ToolPeripheral(computerID),
                      "inventory", new InventoryPeripheral(computerID),
                      "storage",   new StoragePeripheral(computerID));
    }
}
