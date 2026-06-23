package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import xylopia.core.computer.peripheral.LanguagePeripheral;
import xylopia.core.computer.peripheral.RedstonePeripheral;
import xylopia.core.computer.peripheral.SpeakerPeripheral;
import xylopia.core.computer.peripheral.WirelessPeripheral;

import java.util.Map;

public class CommsCoreMk3Item extends CapabilityCoreItem {
    public CommsCoreMk3Item(Properties properties) {
        super(properties, "item.ccatmos.wireless_plugin", "item.ccatmos.redstone_plugin",
              "item.ccatmos.language_plugin", "item.ccatmos.speaker_plugin");
    }
    @Override public boolean providesWireless() { return true; }
    @Override public boolean providesRedstone() { return true; }
    @Override public boolean providesLanguage() { return true; }
    @Override public boolean providesSpeaker()  { return true; }
    @Override public Map<String, IPeripheral> createPeripherals(int computerID) {
        return Map.of("wireless", new WirelessPeripheral(computerID),
                      "redstone", new RedstonePeripheral(computerID),
                      "language", new LanguagePeripheral(computerID),
                      "speaker",  new SpeakerPeripheral(computerID));
    }
}
