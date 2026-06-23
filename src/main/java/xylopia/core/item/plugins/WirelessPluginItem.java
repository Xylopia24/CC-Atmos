package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.WirelessPeripheral;

import java.util.Map;

public class WirelessPluginItem extends BangbooPluginItem {
    public WirelessPluginItem(Properties properties) { super(properties); }
    @Override public boolean providesWireless() { return true; }
    @Override public Map<String, IPeripheral> createPeripherals(int computerID) { return Map.of("wireless", new WirelessPeripheral(computerID)); }

}
