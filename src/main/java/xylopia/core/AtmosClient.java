package xylopia.core;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import xylopia.core.client.BangbooRenderer;
import xylopia.core.client.BangbooScreen;
import xylopia.core.registry.AtmosEntities;
import xylopia.core.registry.AtmosMenus;

public class AtmosClient {
    static void register(IEventBus modEventBus) {
        modEventBus.addListener(AtmosClient::registerRenderers);
        modEventBus.addListener(AtmosClient::registerScreens);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(AtmosEntities.BANGBOO.get(), BangbooRenderer::new);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(AtmosMenus.BANGBOO.get(), BangbooScreen::new);
    }
}
