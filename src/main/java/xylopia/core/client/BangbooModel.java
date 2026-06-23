package xylopia.core.client;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import xylopia.core.skin.BangbooSkinRegistry;
import xylopia.core.entity.BangbooEntity;

public class BangbooModel extends GeoModel<BangbooEntity> {

    @Override
    public ResourceLocation getModelResource(BangbooEntity entity) {
        return BangbooSkinRegistry.INSTANCE.get(entity.getSkinId()).geo();
    }

    @Override
    public ResourceLocation getTextureResource(BangbooEntity entity) {
        return BangbooSkinRegistry.INSTANCE.get(entity.getSkinId()).texture();
    }

    @Override
    public ResourceLocation getAnimationResource(BangbooEntity entity) {
        return BangbooSkinRegistry.INSTANCE.get(entity.getSkinId()).animation();
    }
}
