package xylopia.core.computer.peripheral;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.asm.PeripheralMethodSupplier;
import dan200.computercraft.core.methods.MethodSupplier;
import dan200.computercraft.core.methods.PeripheralMethod;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A single peripheral that merges all @LuaFunction methods from every installed
 * Bangboo plug-in peripheral into one dispatch table on one ComputerSide.
 * peripheral.find("scanner"), peripheral.find("storage"), etc. all resolve to
 * this peripheral via getAdditionalTypes().
 *
 * Also exposes modules() — returns a table of { type -> [method names] } so
 * Lua scripts can discover what plug-ins are installed and what each one provides.
 */
public class BangbooCombinedPeripheral implements IDynamicPeripheral {
    private static final MethodSupplier<PeripheralMethod> SUPPLIER =
        PeripheralMethodSupplier.create(List.of());

    private record Binding(IPeripheral peripheral, PeripheralMethod method) {}

    private List<IPeripheral>           children    = List.of();
    private String[]                    methodNames = new String[0];
    private Binding[]                   bindings    = new Binding[0];
    private Set<String>                 additionalTypes = Set.of();
    private Map<String, List<String>>   moduleMap   = Map.of();

    private final Set<IComputerAccess> computers = ConcurrentHashMap.newKeySet();

    /**
     * Replaces the set of active child peripherals. Detaches old children,
     * rebuilds the combined method/type table, attaches new children.
     * The caller must still call serverComputer.setPeripheral(side, null) then
     * setPeripheral(side, this) afterward so CC re-queries method names and types.
     */
    public synchronized void setPeripherals(List<IPeripheral> newChildren) {
        for (var child : children)
            for (var computer : computers)
                try { child.detach(computer); } catch (Exception ignored) {}

        children = List.copyOf(newChildren);

        var methods  = new LinkedHashMap<String, Binding>();
        var types    = new LinkedHashSet<String>();
        var modules  = new LinkedHashMap<String, List<String>>();

        for (var child : children) {
            types.add(child.getType());
            types.addAll(child.getAdditionalTypes());

            var childMethodNames = new ArrayList<String>();
            SUPPLIER.getSelfMethods(child).forEach((name, method) -> {
                childMethodNames.add(name);
                methods.putIfAbsent(name, new Binding(child, method));
            });
            Collections.sort(childMethodNames);
            if (!childMethodNames.isEmpty())
                modules.put(child.getType(), Collections.unmodifiableList(childMethodNames));
        }

        // Inject modules() as a synthetic method — null binding handled in callMethod
        methods.put("modules", null);

        moduleMap       = Collections.unmodifiableMap(modules);
        additionalTypes = Set.copyOf(types);
        methodNames     = methods.keySet().toArray(new String[0]);
        bindings        = methods.values().toArray(new Binding[0]);

        for (var child : children)
            for (var computer : computers)
                try { child.attach(computer); } catch (Exception ignored) {}
    }

    @Override public String       getType()            { return "bangboo_unit"; }
    @Override public Set<String>  getAdditionalTypes() { return additionalTypes; }
    @Override public String[]     getMethodNames()     { return methodNames;     }

    @Override
    public MethodResult callMethod(IComputerAccess access, ILuaContext context, int index, IArguments args)
            throws LuaException {
        Binding b;
        synchronized (this) {
            if (index < 0 || index >= bindings.length) throw new LuaException("Invalid method index");
            b = bindings[index];
        }
        // null binding = synthetic modules() method
        if (b == null) return MethodResult.of(moduleMap);
        return b.method().apply(b.peripheral(), context, access, args);
    }

    @Override
    public void attach(IComputerAccess computer) {
        computers.add(computer);
        synchronized (this) {
            for (var child : children)
                try { child.attach(computer); } catch (Exception ignored) {}
        }
    }

    @Override
    public void detach(IComputerAccess computer) {
        computers.remove(computer);
        synchronized (this) {
            for (var child : children)
                try { child.detach(computer); } catch (Exception ignored) {}
        }
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) { return this == other; }
}
