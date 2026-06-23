package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.ProximityPeripheral;
import xylopia.core.computer.peripheral.ScannerPeripheral;

import java.util.Map;

public class SensorCoreMk2Item extends CapabilityCoreItem {
    public SensorCoreMk2Item(Properties properties) {
        super(properties, "item.ccatmos.scanner_plugin", "item.ccatmos.proximity_plugin");
    }
    @Override public boolean providesScanner()   { return true; }
    @Override public boolean providesProximity() { return true; }
    @Override public Map<String, IPeripheral> createPeripherals(int computerID) {
        return Map.of("scanner",   new ScannerPeripheral(computerID),
                      "proximity", new ProximityPeripheral(computerID));
    }
}
