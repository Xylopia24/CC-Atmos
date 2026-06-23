package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.BackpackPeripheral;

import java.util.Map;

public class BackpackPluginItem extends BangbooPluginItem {
    public BackpackPluginItem(Properties properties) { super(properties); }

    @Override public boolean providesBackpack() { return true; }

    @Override
    public Map<String, IPeripheral> createPeripherals(int computerID) {
        return Map.of("backpack", new BackpackPeripheral(computerID));
    }
}
