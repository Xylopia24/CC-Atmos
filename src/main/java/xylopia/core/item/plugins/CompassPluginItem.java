package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.CompassPeripheral;

import java.util.Map;

public class CompassPluginItem extends BangbooPluginItem {
    public CompassPluginItem(Properties properties) { super(properties); }
    @Override public boolean providesCompass() { return true; }
    @Override public Map<String, IPeripheral> createPeripherals(int computerID) { return Map.of("compass", new CompassPeripheral(computerID)); }
}
