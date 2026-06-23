package xylopia.core.computer.peripheral;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.asm.PeripheralMethodSupplier;
import dan200.computercraft.core.methods.MethodSupplier;
import dan200.computercraft.core.methods.PeripheralMethod;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sits on ComputerSide.TOP and acts as a virtual peripheral hub.
 *
 * CC:Tweaked's Lua peripheral.lua checks if any side peripheral has type
 * "peripheral_hub" and, if so, calls getNamesRemote() / hasTypeRemote() /
 * callRemote() on it. Each plugin peripheral is registered here by name and
 * appears to Lua as a fully independent, separately-typed peripheral — no
 * method name collisions, no merged dispatch table.
 *
 * peripheral.find("scanner")  → only scanner methods
 * peripheral.find("config")   → only config methods
 * etc.
 */
public class BangbooPeripheralHub implements IPeripheral {

    private static final MethodSupplier<PeripheralMethod> SUPPLIER =
        PeripheralMethodSupplier.create(List.of());

    private record ChildEntry(IPeripheral peripheral, Map<String, PeripheralMethod> methods) {}

    private Map<String, ChildEntry>  children  = Map.of();
    private final Set<IComputerAccess> computers = ConcurrentHashMap.newKeySet();

    // ── Peripheral identity ───────────────────────────────────────────────────

    @Override public String      getType()            { return "peripheral_hub"; }
    @Override public Set<String> getAdditionalTypes() { return Set.of(); }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void attach(IComputerAccess computer) {
        computers.add(computer);
        Map<String, ChildEntry> snapshot;
        synchronized (this) {
            snapshot = children;
            for (var e : snapshot.values())
                try { e.peripheral().attach(computer); } catch (Exception ignored) {}
        }
        for (var name : snapshot.keySet())
            computer.queueEvent("peripheral", name);
    }

    @Override
    public void detach(IComputerAccess computer) {
        Map<String, ChildEntry> snapshot;
        synchronized (this) {
            snapshot = children;
            for (var name : snapshot.keySet())
                computer.queueEvent("peripheral_detach", name);
            for (var e : snapshot.values())
                try { e.peripheral().detach(computer); } catch (Exception ignored) {}
        }
        computers.remove(computer);
    }

    // ── Plugin hot-swap ───────────────────────────────────────────────────────

    /**
     * Called whenever the plugin inventory changes.
     * Diffs old vs new: fires peripheral/peripheral_detach events only for
     * what actually changed, so existing Lua scripts aren't disrupted.
     */
    public synchronized void setPeripherals(Map<String, IPeripheral> newPeripherals) {
        var oldChildren = this.children;

        // Build new entry map with cached method tables
        var newChildren = new LinkedHashMap<String, ChildEntry>();
        for (var e : newPeripherals.entrySet()) {
            var methods = new LinkedHashMap<String, PeripheralMethod>();
            SUPPLIER.getSelfMethods(e.getValue()).forEach(methods::put);
            newChildren.put(e.getKey(), new ChildEntry(e.getValue(), Collections.unmodifiableMap(methods)));
        }
        this.children = Collections.unmodifiableMap(newChildren);

        // Compute diff
        var removed = new ArrayList<String>();
        for (var name : oldChildren.keySet())
            if (!newChildren.containsKey(name)) removed.add(name);

        var added = new ArrayList<String>();
        for (var name : newChildren.keySet())
            if (!oldChildren.containsKey(name)) added.add(name);

        // Apply to attached computers
        for (var computer : computers) {
            for (var name : removed) {
                computer.queueEvent("peripheral_detach", name);
                try { oldChildren.get(name).peripheral().detach(computer); } catch (Exception ignored) {}
            }
            for (var name : added) {
                try { newChildren.get(name).peripheral().attach(computer); } catch (Exception ignored) {}
                computer.queueEvent("peripheral", name);
            }
        }
    }

    // ── Hub protocol — called by CC:Tweaked Lua peripheral.lua ───────────────

    /** Returns all registered peripheral names visible to this computer. */
    @LuaFunction
    public final String[] getNamesRemote() {
        synchronized (this) {
            return children.keySet().toArray(String[]::new);
        }
    }

    /** Returns true if a peripheral with this name is registered. */
    @LuaFunction
    public final boolean isPresentRemote(String name) {
        synchronized (this) {
            return children.containsKey(name);
        }
    }

    /** Returns {type, ...additionalTypes} for the named peripheral, or null if absent. */
    @LuaFunction
    public final @Nullable Object[] getTypeRemote(String name) {
        synchronized (this) {
            var entry = children.get(name);
            if (entry == null) return null;
            var p = entry.peripheral();
            var types = new ArrayList<>();
            types.add(p.getType());
            types.addAll(p.getAdditionalTypes());
            return types.toArray();
        }
    }

    /** Returns true/false if the named peripheral has that type, or null if absent. */
    @LuaFunction
    public final @Nullable Boolean hasTypeRemote(String name, String type) {
        synchronized (this) {
            var entry = children.get(name);
            if (entry == null) return null;
            var p = entry.peripheral();
            return p.getType().equals(type) || p.getAdditionalTypes().contains(type);
        }
    }

    /** Returns the method names exposed by the named peripheral. */
    @LuaFunction
    public final @Nullable String[] getMethodsRemote(String name) {
        synchronized (this) {
            var entry = children.get(name);
            if (entry == null) return null;
            return entry.methods().keySet().toArray(String[]::new);
        }
    }

    /**
     * Dispatches a method call to a named peripheral.
     * Lua calls: peripheral.call(side, "callRemote", name, method, ...args)
     * args[0] = peripheral name, args[1] = method name, args[2..] = method args.
     */
    @LuaFunction
    public final MethodResult callRemote(IComputerAccess computer, ILuaContext context, IArguments args)
            throws LuaException {
        var name   = args.getString(0);
        var method = args.getString(1);
        var rest   = args.drop(2);

        ChildEntry entry;
        synchronized (this) {
            entry = children.get(name);
        }
        if (entry == null) throw new LuaException("No peripheral '" + name + "'");

        var m = entry.methods().get(method);
        if (m == null) throw new LuaException("No method '" + method + "' on '" + name + "'");

        return m.apply(entry.peripheral(), context, computer, rest);
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) { return this == other; }
}
