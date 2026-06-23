package xylopia.core;

import com.mojang.logging.LogUtils;
import dan200.computercraft.api.ComputerCraftAPI;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import xylopia.core.skin.BangbooSkinRegistry;
import org.slf4j.Logger;
import xylopia.core.computer.BangbooLanguageNetwork;
import xylopia.core.computer.BangbooLuaAPIFactory;
import xylopia.core.entity.BangbooEntity;
import xylopia.core.registry.AtmosCreativeTabs;
import xylopia.core.registry.AtmosEntities;
import xylopia.core.registry.AtmosItems;
import xylopia.core.registry.AtmosMenus;

@Mod(Atmos.MODID)
public class Atmos {
    public static final String MODID = "ccatmos";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Atmos(IEventBus modEventBus, ModContainer modContainer) {
        // Base Lua API (always present on every Bangboo computer)
        ComputerCraftAPI.registerAPIFactory(new BangbooLuaAPIFactory());

NeoForge.EVENT_BUS.addListener(this::onServerChat);
        NeoForge.EVENT_BUS.addListener(this::registerDataListeners);

        AtmosItems.REGISTRY.register(modEventBus);
        AtmosEntities.REGISTRY.register(modEventBus);
        AtmosMenus.REGISTRY.register(modEventBus);
        AtmosCreativeTabs.REGISTRY.register(modEventBus);
        modEventBus.addListener(this::registerAttributes);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            AtmosClient.register(modEventBus);
        }
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(AtmosEntities.BANGBOO.get(), BangbooEntity.createAttributes().build());
    }

    private void registerDataListeners(AddReloadListenerEvent event) {
        event.addListener(BangbooSkinRegistry.INSTANCE);
    }

    private void onServerChat(ServerChatEvent event) {
        BangbooLanguageNetwork.onChatMessage(
            event.getPlayer().getName().getString(),
            event.getMessage().getString()
        );
    }
}
