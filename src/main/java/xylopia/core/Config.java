package xylopia.core;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {

    public static final ModConfigSpec SPEC;

    // ── Energy ────────────────────────────────────────────────────────────────
    public static final ModConfigSpec.IntValue MAX_ENERGY;
    public static final ModConfigSpec.IntValue FUEL_ENERGY_MULT;

    // ── Navigation ────────────────────────────────────────────────────────────
    public static final ModConfigSpec.IntValue PATH_TIMEOUT_TICKS;

    // ── Scanner ───────────────────────────────────────────────────────────────
    public static final ModConfigSpec.IntValue SCAN_MAX_ENTITY_RADIUS;
    public static final ModConfigSpec.IntValue SCAN_MAX_BLOCK_RADIUS;
    public static final ModConfigSpec.IntValue SCAN_RESULT_CAP;

    // ── Proximity ─────────────────────────────────────────────────────────────
    public static final ModConfigSpec.IntValue PROXIMITY_SCAN_INTERVAL_TICKS;
    public static final ModConfigSpec.IntValue PROXIMITY_MAX_RADIUS;

    static {
        var builder = new ModConfigSpec.Builder();

        builder.push("energy");
        MAX_ENERGY = builder
                .comment("Maximum energy capacity of the Bangboo when a Config Core is installed.")
                .defineInRange("maxEnergy", 50_000, 1_000, 10_000_000);
        FUEL_ENERGY_MULT = builder
                .comment("Energy restored per fuel tick consumed from the internal inventory. Coal = 1600 ticks, so coal gives 1600 × this value in energy.")
                .defineInRange("fuelEnergyMultiplier", 20, 1, 1_000);
        builder.pop();

        builder.push("navigation");
        PATH_TIMEOUT_TICKS = builder
                .comment("Ticks before a path() call gives up (20 ticks = 1 second). Default: 1200 (60 seconds).")
                .defineInRange("pathTimeoutTicks", 1200, 100, 72_000);
        builder.pop();

        builder.push("scanner");
        SCAN_MAX_ENTITY_RADIUS = builder
                .comment("Maximum radius for entity and item scanning. Higher values may impact server performance.")
                .defineInRange("maxEntityRadius", 64, 1, 256);
        SCAN_MAX_BLOCK_RADIUS = builder
                .comment("Maximum radius for block scanning. Higher values significantly impact server performance.")
                .defineInRange("maxBlockRadius", 32, 1, 64);
        SCAN_RESULT_CAP = builder
                .comment("Maximum number of entries returned by a single scan() call.")
                .defineInRange("resultCap", 1000, 10, 10_000);
        builder.pop();

        builder.push("proximity");
        PROXIMITY_SCAN_INTERVAL_TICKS = builder
                .comment("How often the proximity peripheral checks for nearby entities, in ticks. Lower = more responsive but more CPU usage.")
                .defineInRange("scanIntervalTicks", 10, 1, 100);
        PROXIMITY_MAX_RADIUS = builder
                .comment("Maximum detection radius for the proximity peripheral.")
                .defineInRange("maxRadius", 64, 1, 256);
        builder.pop();

        SPEC = builder.build();
    }
}
