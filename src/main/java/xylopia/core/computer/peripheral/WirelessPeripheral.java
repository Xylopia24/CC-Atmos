package xylopia.core.computer.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.modem.ModemState;
import dan200.computercraft.shared.peripheral.modem.wireless.WirelessModemPeripheral;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import xylopia.core.computer.BangbooComputerRegistry;

import javax.annotation.Nullable;

/**
 * Wireless plug-in peripheral.
 *
 * Extends CC's WirelessModemPeripheral so the Bangboo participates in the
 * global CC wireless network and can exchange modem_message events with any
 * CC computer that has a wireless modem.
 *
 * Inherited Lua API: open, close, closeAll, isOpen, transmit, isWireless
 * Added alias:       broadcast(channel, message) — shorthand for transmit(ch, ch, msg)
 */
public class WirelessPeripheral extends WirelessModemPeripheral {
    private final int computerID;

    public WirelessPeripheral(int computerID) {
        super(new ModemState(), false); // false = normal range, not advanced/ender
        this.computerID = computerID;
    }

    @Override public String getType() { return "wireless"; }

    // ── PacketReceiver / PacketSender ─────────────────────────────────────────

    @Override
    public @Nullable Level getLevel() {
        var b = BangbooComputerRegistry.get(computerID);
        return b != null ? b.level() : null;
    }

    @Override
    public Vec3 getPosition() {
        var b = BangbooComputerRegistry.get(computerID);
        return b != null ? b.position() : Vec3.ZERO;
    }

    @Override
    public String getSenderID() { return "bangboo_wireless_" + computerID; }

    // ── Convenience alias ─────────────────────────────────────────────────────

    /** Shorthand for transmit(channel, channel, message). Channel must be open first. */
    @LuaFunction(mainThread = true)
    public final void broadcast(int channel, Object message) throws LuaException {
        transmit(channel, channel, message);
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void detach(dan200.computercraft.api.peripheral.IComputerAccess computer) {
        super.detach(computer);
        removed(); // deregisters from WirelessNetwork so we don't leak as a receiver
    }

    // ── Equality ──────────────────────────────────────────────────────────────

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other != null && other.getClass() == getClass()
            && ((WirelessPeripheral) other).computerID == computerID;
    }
}
