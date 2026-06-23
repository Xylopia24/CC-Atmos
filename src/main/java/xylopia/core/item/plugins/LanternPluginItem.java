package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.LanternPeripheral;

import java.util.Map;

public class LanternPluginItem extends BangbooPluginItem {
    public LanternPluginItem(Properties properties) { super(properties); }
    @Override public boolean providesLantern() { return true; }
    @Override public Map<String, IPeripheral> createPeripherals(int computerID) { return Map.of("lantern", new LanternPeripheral(computerID)); }
}
