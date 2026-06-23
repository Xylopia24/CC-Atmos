package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.CosmeticPeripheral;

import java.util.Map;

public class CosmeticPluginItem extends BangbooPluginItem {
    public CosmeticPluginItem(Properties properties) { super(properties); }
    @Override public boolean providesCosmetic() { return true; }
    @Override public Map<String, IPeripheral> createPeripherals(int computerID) { return Map.of("cosmetic", new CosmeticPeripheral(computerID)); }
}
