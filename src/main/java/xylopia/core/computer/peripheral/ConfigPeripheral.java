package xylopia.core.computer.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigPeripheral extends BangbooPeripheral {

    // ── Attribute catalogue ───────────────────────────────────────────────────

    private record Spec(net.minecraft.core.Holder<Attribute> holder, double min, double max, double defaultValue) {}

    private static final Map<String, Spec> SPECS = new LinkedHashMap<>();
    static {
        SPECS.put("max_health",           new Spec(Attributes.MAX_HEALTH,           1,     200,  20.0));
        SPECS.put("movement_speed",       new Spec(Attributes.MOVEMENT_SPEED,       0.01,  1.5,   0.25));
        SPECS.put("flying_speed",         new Spec(Attributes.FLYING_SPEED,         0.01,  2.0,   0.6));
        SPECS.put("follow_range",         new Spec(Attributes.FOLLOW_RANGE,         1,     512,  256.0));
        SPECS.put("armor",                new Spec(Attributes.ARMOR,                0,     30,    0.0));
        SPECS.put("armor_toughness",      new Spec(Attributes.ARMOR_TOUGHNESS,      0,     20,    0.0));
        SPECS.put("knockback_resistance", new Spec(Attributes.KNOCKBACK_RESISTANCE, 0,     1,     0.0));
        SPECS.put("attack_damage",        new Spec(Attributes.ATTACK_DAMAGE,        0,     50,    2.0));
        SPECS.put("attack_speed",         new Spec(Attributes.ATTACK_SPEED,         0.1,   20,    4.0));
        SPECS.put("step_height",          new Spec(Attributes.STEP_HEIGHT,          0.5,   3.0,   0.6));
    }

    // ── Constructor ───────────────────────────────────────────────────────────

    public ConfigPeripheral(int computerID) { super(computerID); }

    @Override public String getType() { return "config"; }

    // ── Lua API ───────────────────────────────────────────────────────────────

    /**
     * Set a stat to a specific value.
     * Usage: config.set("max_health", 50)
     */
    @LuaFunction(mainThread = true)
    public final void set(String stat, double value) throws LuaException {
        var spec = requireSpec(stat);
        if (value < spec.min() || value > spec.max())
            throw new LuaException(stat + " must be between " + spec.min() + " and " + spec.max());
        var instance = requireBangboo().getAttribute(spec.holder());
        if (instance == null) throw new LuaException(stat + " is not available on this Bangboo");

        instance.setBaseValue(value);

        // Clamp current health if max_health was lowered
        if (stat.equals("max_health")) {
            var b = requireBangboo();
            if (b.getHealth() > b.getMaxHealth()) b.setHealth(b.getMaxHealth());
        }
    }

    /**
     * Get the current base value of a stat.
     * Usage: local hp = config.get("max_health")
     */
    @LuaFunction(mainThread = true)
    public final double get(String stat) throws LuaException {
        var spec = requireSpec(stat);
        var instance = requireBangboo().getAttribute(spec.holder());
        if (instance == null) throw new LuaException(stat + " is not available on this Bangboo");
        return instance.getBaseValue();
    }

    /**
     * Reset a single stat back to its default value.
     * Usage: config.reset("max_health")
     */
    @LuaFunction(mainThread = true)
    public final void reset(String stat) throws LuaException {
        var spec = requireSpec(stat);
        var instance = requireBangboo().getAttribute(spec.holder());
        if (instance != null) instance.setBaseValue(spec.defaultValue());
        if (stat.equals("max_health")) {
            var b = requireBangboo();
            if (b.getHealth() > b.getMaxHealth()) b.setHealth(b.getMaxHealth());
        }
    }

    /**
     * Reset all configured stats to their defaults.
     * Usage: config.resetAll()
     */
    @LuaFunction(mainThread = true)
    public final void resetAll() throws LuaException {
        var b = requireBangboo();
        for (var e : SPECS.entrySet()) {
            var instance = b.getAttribute(e.getValue().holder());
            if (instance != null) instance.setBaseValue(e.getValue().defaultValue());
        }
        if (b.getHealth() > b.getMaxHealth()) b.setHealth(b.getMaxHealth());
    }

    /**
     * List all configurable stats with their current value, default, and allowed range.
     * Returns a table keyed by stat name.
     * Usage: local stats = config.list()
     */
    @LuaFunction(mainThread = true)
    public final Map<String, Map<String, Object>> list() throws LuaException {
        var b    = requireBangboo();
        var out  = new LinkedHashMap<String, Map<String, Object>>();
        for (var e : SPECS.entrySet()) {
            var spec     = e.getValue();
            var instance = b.getAttribute(spec.holder());
            if (instance == null) continue;
            var entry = new HashMap<String, Object>();
            entry.put("value",   instance.getBaseValue());
            entry.put("default", spec.defaultValue());
            entry.put("min",     spec.min());
            entry.put("max",     spec.max());
            out.put(e.getKey(), entry);
        }
        return out;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static Spec requireSpec(String stat) throws LuaException {
        var spec = SPECS.get(stat);
        if (spec == null) throw new LuaException("Unknown stat '" + stat + "'. Use config.list() to see available stats.");
        return spec;
    }

    public static void resetAllFor(xylopia.core.entity.BangbooEntity bangboo) {
        for (var spec : SPECS.values()) {
            var instance = bangboo.getAttribute(spec.holder());
            if (instance != null) instance.setBaseValue(spec.defaultValue());
        }
        if (bangboo.getHealth() > bangboo.getMaxHealth()) bangboo.setHealth(bangboo.getMaxHealth());
    }
}
