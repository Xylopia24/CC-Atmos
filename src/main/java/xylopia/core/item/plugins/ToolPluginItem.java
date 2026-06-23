package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.ToolPeripheral;

import java.util.Map;

public class ToolPluginItem extends BangbooPluginItem {
    public ToolPluginItem(Properties properties) { super(properties); }
    @Override public boolean providesTool() { return true; }
    @Override public Map<String, IPeripheral> createPeripherals(int computerID) { return Map.of("tool", new ToolPeripheral(computerID)); }
}
