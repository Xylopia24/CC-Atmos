package xylopia.core.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import xylopia.core.entity.BangbooEntity;
import xylopia.core.registry.AtmosEntities;

public class BooBoxItem extends Item {

    public BooBoxItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());
        BangbooEntity bangboo = AtmosEntities.BANGBOO.get().create(level);
        if (bangboo == null) return InteractionResult.FAIL;

        bangboo.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                context.getPlayer() != null ? context.getPlayer().getYRot() : 0f, 0f);
        level.addFreshEntity(bangboo);

        var player = context.getPlayer();
        if (player == null || !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }

        return InteractionResult.CONSUME;
    }
}
