package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.ProximityPeripheral;

import java.util.Map;

public class ProximityPluginItem extends BangbooPluginItem {
    public ProximityPluginItem(Properties properties) { super(properties); }
    @Override public boolean providesProximity() { return true; }
    @Override public Map<String, IPeripheral> createPeripherals(int computerID) { return Map.of("proximity", new ProximityPeripheral(computerID)); }
}
