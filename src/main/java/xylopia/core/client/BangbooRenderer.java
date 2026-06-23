package xylopia.core.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import xylopia.core.entity.BangbooEntity;
import xylopia.core.skin.BangbooSkinRegistry;

public class BangbooRenderer extends GeoEntityRenderer<BangbooEntity> {
    public BangbooRenderer(EntityRendererProvider.Context context) {
        super(context, new BangbooModel());
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack,
                                       BangbooEntity animatable, BakedGeoModel model, boolean isReRender,
                                       float partialTick, int packedLight, int packedOverlay) {
        float scale = BangbooSkinRegistry.INSTANCE.get(animatable.getSkinId()).scale();
        poseStack.scale(scale, scale, scale);
    }
}
