package xylopia.core.registry;

import dan200.computercraft.shared.network.container.ComputerContainerData;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import xylopia.core.Atmos;
import xylopia.core.menu.BangbooMenu;

public class AtmosMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY =
            DeferredRegister.create(Registries.MENU, Atmos.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<BangbooMenu>> BANGBOO =
            REGISTRY.register("bangboo", () -> IMenuTypeExtension.create(
                    (id, inv, buf) -> new BangbooMenu(
                            AtmosMenus.BANGBOO.get(), id, inv,
                            ComputerContainerData.STREAM_CODEC.decode(buf))));
}
