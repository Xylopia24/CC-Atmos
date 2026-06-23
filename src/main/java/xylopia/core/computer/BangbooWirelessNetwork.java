package xylopia.core.computer;

/**
 * @deprecated Replaced by WirelessPeripheral extending CC's WirelessModemPeripheral.
 * The old custom network only reached other Bangboos; the new system uses
 * ComputerCraftAPI.getWirelessNetwork() so any CC wireless modem can communicate
 * with the Bangboo.
 */
@Deprecated
public final class BangbooWirelessNetwork {
    private BangbooWirelessNetwork() {}
}
