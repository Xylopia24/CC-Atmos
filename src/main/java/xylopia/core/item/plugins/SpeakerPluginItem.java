package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.SpeakerPeripheral;

import java.util.Map;

public class SpeakerPluginItem extends BangbooPluginItem {
    public SpeakerPluginItem(Properties properties) { super(properties); }
    @Override public boolean providesSpeaker() { return true; }
    @Override public Map<String, IPeripheral> createPeripherals(int computerID) { return Map.of("speaker", new SpeakerPeripheral(computerID)); }
}
