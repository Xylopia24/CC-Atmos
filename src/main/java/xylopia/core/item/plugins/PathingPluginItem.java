package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.PathingPeripheral;

import java.util.Map;

public class PathingPluginItem extends BangbooPluginItem {
    public PathingPluginItem(Properties properties) { super(properties); }
    @Override public boolean providesPathing() { return true; }
    @Override public Map<String, IPeripheral> createPeripherals(int computerID) { return Map.of("pathing", new PathingPeripheral(computerID)); }
}
