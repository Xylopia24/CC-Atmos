package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.InventoryPeripheral;
import xylopia.core.computer.peripheral.ToolPeripheral;

import java.util.Map;

public class FieldCoreMk2Item extends CapabilityCoreItem {
    public FieldCoreMk2Item(Properties properties) {
        super(properties, "item.ccatmos.tool_plugin", "item.ccatmos.inventory_plugin");
    }
    @Override public boolean providesTool()      { return true; }
    @Override public boolean providesInventory() { return true; }
    @Override public Map<String, IPeripheral> createPeripherals(int computerID) {
        return Map.of("tool",      new ToolPeripheral(computerID),
                      "inventory", new InventoryPeripheral(computerID));
    }
}
