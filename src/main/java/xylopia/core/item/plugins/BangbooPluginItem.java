package xylopia.core.item.plugins;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xylopia.core.entity.BangbooEntity;

import java.util.Map;

public class BangbooPluginItem extends Item {

    public BangbooPluginItem(Properties properties) {
        super(properties);
    }

    public void onInstalled(BangbooEntity bangboo, int slot, ItemStack stack) { }
    public void onRemoved(BangbooEntity bangboo, int slot, ItemStack stack) { }

    /** Returns named peripherals this plug-in contributes. Empty map for non-capability plug-ins. */
    public Map<String, IPeripheral> createPeripherals(int computerID) { return Map.of(); }

    // ── Capability flags ──────────────────────────────────────────────────────

    public boolean isHub()                    { return false; }
    public boolean providesAdvancedTerminal() { return false; }
    public boolean providesInventory()        { return false; }
    public boolean providesScanner()          { return false; }
    public boolean providesTool()             { return false; }
    public boolean providesWireless()         { return false; }
    public boolean providesRedstone()         { return false; }
    public boolean providesPathing()          { return false; }
    public boolean providesCompass()          { return false; }
    public boolean providesProximity()        { return false; }
    public boolean providesStorage()          { return false; }
    public boolean providesLantern()          { return false; }
    public boolean providesSpeaker()          { return false; }
    public boolean providesCosmetic()         { return false; }
    public boolean providesLanguage()         { return false; }
    public boolean providesBackpack()         { return false; }
}
