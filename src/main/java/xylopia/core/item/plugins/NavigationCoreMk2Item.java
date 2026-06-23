package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.CompassPeripheral;
import xylopia.core.computer.peripheral.PathingPeripheral;
import xylopia.core.computer.peripheral.ProximityPeripheral;
import xylopia.core.computer.peripheral.ScannerPeripheral;

import java.util.Map;

public class NavigationCoreMk2Item extends CapabilityCoreItem {
    public NavigationCoreMk2Item(Properties properties) {
        super(properties, "item.ccatmos.pathing_plugin", "item.ccatmos.compass_plugin",
              "item.ccatmos.scanner_plugin", "item.ccatmos.proximity_plugin");
    }
    @Override public boolean providesPathing()   { return true; }
    @Override public boolean providesCompass()   { return true; }
    @Override public boolean providesScanner()   { return true; }
    @Override public boolean providesProximity() { return true; }
    @Override public Map<String, IPeripheral> createPeripherals(int computerID) {
        return Map.of("pathing",   new PathingPeripheral(computerID),
                      "compass",   new CompassPeripheral(computerID),
                      "scanner",   new ScannerPeripheral(computerID),
                      "proximity", new ProximityPeripheral(computerID));
    }
}
