package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.RedstonePeripheral;
import xylopia.core.computer.peripheral.WirelessPeripheral;

import java.util.Map;

public class CommsCoreMk1Item extends CapabilityCoreItem {
    public CommsCoreMk1Item(Properties properties) {
        super(properties, "item.ccatmos.wireless_plugin", "item.ccatmos.redstone_plugin");
    }
    @Override public boolean providesWireless() { return true; }
    @Override public boolean providesRedstone() { return true; }
    @Override public Map<String, IPeripheral> createPeripherals(int computerID) {
        return Map.of("wireless", new WirelessPeripheral(computerID),
                      "redstone", new RedstonePeripheral(computerID));
    }
}
