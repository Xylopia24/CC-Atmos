package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.ToolPeripheral;

import java.util.Map;

public class FieldCoreMk1Item extends CapabilityCoreItem {
    public FieldCoreMk1Item(Properties properties) {
        super(properties, "item.ccatmos.tool_plugin");
    }
    @Override public boolean providesTool() { return true; }
    @Override public Map<String, IPeripheral> createPeripherals(int computerID) {
        return Map.of("tool", new ToolPeripheral(computerID));
    }
}
