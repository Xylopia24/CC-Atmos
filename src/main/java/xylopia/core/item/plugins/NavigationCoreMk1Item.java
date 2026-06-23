package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.CompassPeripheral;
import xylopia.core.computer.peripheral.PathingPeripheral;

import java.util.Map;

public class NavigationCoreMk1Item extends CapabilityCoreItem {
    public NavigationCoreMk1Item(Properties properties) {
        super(properties, "item.ccatmos.pathing_plugin", "item.ccatmos.compass_plugin");
    }
    @Override public boolean providesPathing() { return true; }
    @Override public boolean providesCompass() { return true; }
    @Override public Map<String, IPeripheral> createPeripherals(int computerID) {
        return Map.of("pathing", new PathingPeripheral(computerID),
                      "compass", new CompassPeripheral(computerID));
    }
}
