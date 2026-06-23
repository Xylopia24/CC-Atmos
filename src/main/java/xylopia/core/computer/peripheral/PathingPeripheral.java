package xylopia.core.computer.peripheral;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;

public class PathingPeripheral extends BangbooPeripheral {
    public PathingPeripheral(int computerID) { super(computerID); }

    @Override public String getType() { return "pathing"; }

    @Override
    public void detach(IComputerAccess computer) {
        var b = bangboo();
        if (b != null) {
            b.cancelPathing();
            b.stopSteering();
        }
    }

    @LuaFunction
    public final MethodResult path(double x, double y, double z) {
        var b = bangboo();
        if (b == null) return MethodResult.of(false, "unavailable");
        var server = b.level().getServer();
        if (server == null) return MethodResult.of(false, "unavailable");
        // Queue pathing start on the main thread (returns immediately), then yield for the done event.
        // The event fires after at least one tick so the pullEvent below always wins the race.
        server.execute(() -> b.startPathing(x, y, z));
        return MethodResult.pullEvent("bangboo_path_done", args -> MethodResult.of(args[1], args[2]));
    }

    @LuaFunction(mainThread = true)
    public final void moveTo(double x, double y, double z) {
        var b = bangboo();
        if (b != null) b.redirectTo(x, y, z);
    }

    @LuaFunction(mainThread = true)
    public final void steerTo(double x, double y, double z) {
        var b = bangboo();
        if (b != null) b.startSteering(x, y, z);
    }

    @LuaFunction(mainThread = true)
    public final void stopSteering() {
        var b = bangboo();
        if (b != null) b.stopSteering();
    }

    @LuaFunction(mainThread = true)
    public final void cancel() {
        var b = bangboo();
        if (b != null) b.cancelPathing();
    }

    @LuaFunction(mainThread = true)
    public final void setSpeed(double multiplier) {
        var b = bangboo();
        if (b != null) b.setPathingSpeed(multiplier);
    }

    @LuaFunction(mainThread = true)
    public final double getSpeed() {
        var b = bangboo();
        return b != null ? b.getPathingSpeed() : 1.0;
    }

    @LuaFunction(mainThread = true)
    public final boolean isPathing() {
        var b = bangboo();
        return b != null && b.isActivelyPathing();
    }
}
