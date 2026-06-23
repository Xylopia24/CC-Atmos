package xylopia.core.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import xylopia.core.Atmos;
import xylopia.core.entity.BangbooEntity;

public class AtmosEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY =
            DeferredRegister.create(Registries.ENTITY_TYPE, Atmos.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<BangbooEntity>> BANGBOO =
            REGISTRY.register("bangboo", () -> EntityType.Builder.<BangbooEntity>of(BangbooEntity::new, MobCategory.CREATURE)
                    .sized(0.7f, 1.0f)
                    .build(Atmos.MODID + ":bangboo"));
}
