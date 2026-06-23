package xylopia.core.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import xylopia.core.Atmos;

public class AtmosCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTRY =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Atmos.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ATMOS_TAB =
        REGISTRY.register("atmos_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ccatmos"))
            .icon(() -> AtmosItems.BOO_BOX.get().getDefaultInstance())
            .displayItems((params, output) -> {
                // Spawn item
                output.accept(AtmosItems.BOO_BOX.get());
                // Hub items
                output.accept(AtmosItems.BLANK_CARTRIDGE.get());
                output.accept(AtmosItems.ADVANCED_BLANK_CARTRIDGE.get());
                output.accept(AtmosItems.PLUGIN_HUB.get());
                output.accept(AtmosItems.ADVANCED_PLUGIN_HUB.get());
                // Capability Plug-Ins
                output.accept(AtmosItems.INVENTORY_PLUGIN.get());
                output.accept(AtmosItems.SCANNER_PLUGIN.get());
                output.accept(AtmosItems.PATHING_PLUGIN.get());
                output.accept(AtmosItems.WIRELESS_PLUGIN.get());
                output.accept(AtmosItems.TOOL_PLUGIN.get());
                output.accept(AtmosItems.PROXIMITY_PLUGIN.get());
                output.accept(AtmosItems.REDSTONE_PLUGIN.get());
                output.accept(AtmosItems.STORAGE_PLUGIN.get());
                output.accept(AtmosItems.COMPASS_PLUGIN.get());
                output.accept(AtmosItems.LANTERN_PLUGIN.get());
                output.accept(AtmosItems.SPEAKER_PLUGIN.get());
                output.accept(AtmosItems.BACKPACK_PLUGIN.get());
                output.accept(AtmosItems.LANGUAGE_PLUGIN.get());
                // Modification Plug-Ins
                output.accept(AtmosItems.ADVANCED_TERMINAL_PLUGIN.get());
                output.accept(AtmosItems.REINFORCEMENT_PLUGIN.get());
                output.accept(AtmosItems.SPRINT_PLUGIN.get());
                output.accept(AtmosItems.COSMETIC_PLUGIN.get());
                // Capability Cores — Sensor line
                output.accept(AtmosItems.SENSOR_CORE_MK1.get());
                output.accept(AtmosItems.SENSOR_CORE_MK2.get());
                // Capability Cores — Navigation line
                output.accept(AtmosItems.NAVIGATION_CORE_MK1.get());
                output.accept(AtmosItems.NAVIGATION_CORE_MK2.get());
                // Capability Cores — Comms line
                output.accept(AtmosItems.COMMS_CORE_MK1.get());
                output.accept(AtmosItems.COMMS_CORE_MK2.get());
                output.accept(AtmosItems.COMMS_CORE_MK3.get());
                // Capability Cores — Logistics line
                output.accept(AtmosItems.LOGISTICS_CORE_MK1.get());
                output.accept(AtmosItems.LOGISTICS_CORE_MK2.get());
                // Capability Cores — Field line
                output.accept(AtmosItems.FIELD_CORE_MK1.get());
                output.accept(AtmosItems.FIELD_CORE_MK2.get());
                output.accept(AtmosItems.FIELD_CORE_MK3.get());
                // Capability Cores — Standalone
                output.accept(AtmosItems.ANTI_GRAV_CORE.get());
                output.accept(AtmosItems.CONFIG_CORE.get());
                // Creative-only
                output.accept(AtmosItems.COMMAND_CORE.get());
            })
            .build());
}
