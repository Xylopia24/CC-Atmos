package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.ScannerPeripheral;

import java.util.Map;

public class ScannerPluginItem extends BangbooPluginItem {
    public ScannerPluginItem(Properties properties) { super(properties); }
    @Override public boolean providesScanner() { return true; }
    @Override public Map<String, IPeripheral> createPeripherals(int computerID) { return Map.of("scanner", new ScannerPeripheral(computerID)); }
}
