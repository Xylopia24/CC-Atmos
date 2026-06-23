package xylopia.core.registry;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import xylopia.core.Atmos;
import xylopia.core.item.BooBoxItem;
import xylopia.core.item.plugins.*;

public class AtmosItems {
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(Atmos.MODID);

    // ── Base items ────────────────────────────────────────────────────────────
    public static final DeferredItem<BooBoxItem> BOO_BOX =
        REGISTRY.register("boo_box", () -> new BooBoxItem(new Item.Properties().stacksTo(16)));

    // ── Hub items ─────────────────────────────────────────────────────────────
    public static final DeferredItem<BangbooPluginItem> BLANK_CARTRIDGE =
        REGISTRY.register("blank_cartridge", () -> new BangbooPluginItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<BangbooPluginItem> ADVANCED_BLANK_CARTRIDGE =
        REGISTRY.register("advanced_blank_cartridge", () -> new BangbooPluginItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<PluginHubItem> PLUGIN_HUB =
        REGISTRY.register("plugin_hub", () -> new PluginHubItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<AdvancedPluginHubItem> ADVANCED_PLUGIN_HUB =
        REGISTRY.register("advanced_plugin_hub", () -> new AdvancedPluginHubItem(new Item.Properties().stacksTo(1)));

    // ── Capability Plug-Ins ───────────────────────────────────────────────────
    public static final DeferredItem<InventoryPluginItem> INVENTORY_PLUGIN =
        REGISTRY.register("inventory_plugin", () -> new InventoryPluginItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ScannerPluginItem> SCANNER_PLUGIN =
        REGISTRY.register("scanner_plugin", () -> new ScannerPluginItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<PathingPluginItem> PATHING_PLUGIN =
        REGISTRY.register("pathing_plugin", () -> new PathingPluginItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<WirelessPluginItem> WIRELESS_PLUGIN =
        REGISTRY.register("wireless_plugin", () -> new WirelessPluginItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ToolPluginItem> TOOL_PLUGIN =
        REGISTRY.register("tool_plugin", () -> new ToolPluginItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ProximityPluginItem> PROXIMITY_PLUGIN =
        REGISTRY.register("proximity_plugin", () -> new ProximityPluginItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<RedstonePluginItem> REDSTONE_PLUGIN =
        REGISTRY.register("redstone_plugin", () -> new RedstonePluginItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<StoragePluginItem> STORAGE_PLUGIN =
        REGISTRY.register("storage_plugin", () -> new StoragePluginItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CompassPluginItem> COMPASS_PLUGIN =
        REGISTRY.register("compass_plugin", () -> new CompassPluginItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<LanternPluginItem> LANTERN_PLUGIN =
        REGISTRY.register("lantern_plugin", () -> new LanternPluginItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<SpeakerPluginItem> SPEAKER_PLUGIN =
        REGISTRY.register("speaker_plugin", () -> new SpeakerPluginItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<BackpackPluginItem> BACKPACK_PLUGIN =
        REGISTRY.register("backpack_plugin", () -> new BackpackPluginItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<LanguagePluginItem> LANGUAGE_PLUGIN =
        REGISTRY.register("language_plugin", () -> new LanguagePluginItem(new Item.Properties().stacksTo(1)));

    // ── Modification Plug-Ins ─────────────────────────────────────────────────
    public static final DeferredItem<AdvancedTerminalPluginItem> ADVANCED_TERMINAL_PLUGIN =
        REGISTRY.register("advanced_terminal_plugin", () -> new AdvancedTerminalPluginItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ReinforcementPluginItem> REINFORCEMENT_PLUGIN =
        REGISTRY.register("reinforcement_plugin", () -> new ReinforcementPluginItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<SprintPluginItem> SPRINT_PLUGIN =
        REGISTRY.register("sprint_plugin", () -> new SprintPluginItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CosmeticPluginItem> COSMETIC_PLUGIN =
        REGISTRY.register("cosmetic_plugin", () -> new CosmeticPluginItem(new Item.Properties().stacksTo(1)));

    // ── Capability Cores — Sensor line ───────────────────────────────────────
    public static final DeferredItem<SensorCoreMk1Item> SENSOR_CORE_MK1 =
        REGISTRY.register("sensor_core_mk1", () -> new SensorCoreMk1Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<SensorCoreMk2Item> SENSOR_CORE_MK2 =
        REGISTRY.register("sensor_core_mk2", () -> new SensorCoreMk2Item(new Item.Properties().stacksTo(1)));

    // ── Capability Cores — Navigation line ───────────────────────────────────
    public static final DeferredItem<NavigationCoreMk1Item> NAVIGATION_CORE_MK1 =
        REGISTRY.register("navigation_core_mk1", () -> new NavigationCoreMk1Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<NavigationCoreMk2Item> NAVIGATION_CORE_MK2 =
        REGISTRY.register("navigation_core_mk2", () -> new NavigationCoreMk2Item(new Item.Properties().stacksTo(1)));

    // ── Capability Cores — Comms line ────────────────────────────────────────
    public static final DeferredItem<CommsCoreMk1Item> COMMS_CORE_MK1 =
        REGISTRY.register("comms_core_mk1", () -> new CommsCoreMk1Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CommsCoreMk2Item> COMMS_CORE_MK2 =
        REGISTRY.register("comms_core_mk2", () -> new CommsCoreMk2Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CommsCoreMk3Item> COMMS_CORE_MK3 =
        REGISTRY.register("comms_core_mk3", () -> new CommsCoreMk3Item(new Item.Properties().stacksTo(1)));

    // ── Capability Cores — Logistics line ────────────────────────────────────
    public static final DeferredItem<LogisticsCoreMk1Item> LOGISTICS_CORE_MK1 =
        REGISTRY.register("logistics_core_mk1", () -> new LogisticsCoreMk1Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<LogisticsCoreMk2Item> LOGISTICS_CORE_MK2 =
        REGISTRY.register("logistics_core_mk2", () -> new LogisticsCoreMk2Item(new Item.Properties().stacksTo(1)));

    // ── Capability Cores — Field line ────────────────────────────────────────
    public static final DeferredItem<FieldCoreMk1Item> FIELD_CORE_MK1 =
        REGISTRY.register("field_core_mk1", () -> new FieldCoreMk1Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<FieldCoreMk2Item> FIELD_CORE_MK2 =
        REGISTRY.register("field_core_mk2", () -> new FieldCoreMk2Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<FieldCoreMk3Item> FIELD_CORE_MK3 =
        REGISTRY.register("field_core_mk3", () -> new FieldCoreMk3Item(new Item.Properties().stacksTo(1)));

    // ── Capability Cores — Standalone ────────────────────────────────────────
    public static final DeferredItem<AntiGravCoreItem> ANTI_GRAV_CORE =
        REGISTRY.register("anti_grav_core", () -> new AntiGravCoreItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ConfigCoreItem> CONFIG_CORE =
        REGISTRY.register("config_core", () -> new ConfigCoreItem(new Item.Properties().stacksTo(1)));

    // ── Creative-only ─────────────────────────────────────────────────────────
    public static final DeferredItem<CommandCoreItem> COMMAND_CORE =
        REGISTRY.register("command_core", () -> new CommandCoreItem(new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC)));
}
