package xylopia.core.computer.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.shared.util.DropConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import xylopia.core.entity.BangbooEntity;


public class ToolPeripheral extends BangbooPeripheral {
    public ToolPeripheral(int computerID) { super(computerID); }

    @Override public String getType() { return "tool"; }

    @LuaFunction(mainThread = true)
    public final Object[] swing() throws LuaException {
        var bangboo = requireBangboo();
        var level = requireLevel(bangboo);
        var player = bangboo.getFakePlayer(level);
        Vec3 eye = bangboo.getEyePosition(), look = bangboo.getLookAngle(), end = eye.add(look.scale(4.5));
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
            level, bangboo, eye, end,
            bangboo.getBoundingBox().expandTowards(look.scale(4.5)).inflate(1.0),
            e -> e.isAlive() && e != bangboo, 0.0f);
        if (entityHit != null) { player.attack(entityHit.getEntity()); return new Object[]{true, "entity"}; }
        BlockHitResult blockHit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (blockHit.getType() != HitResult.Type.MISS) {
            var pos = blockHit.getBlockPos();
            DropConsumer.set(level, pos, stack -> bangboo.getInternalInventory().addItem(stack));
            level.destroyBlock(pos, true, player);
            DropConsumer.clearAndDrop(level, pos, Direction.UP);
            return new Object[]{true, "block"};
        }
        return new Object[]{false, "nothing"};
    }

    @LuaFunction(mainThread = true)
    public final Object[] use() throws LuaException {
        var bangboo = requireBangboo();
        var level = requireLevel(bangboo);
        var player = bangboo.getFakePlayer(level);
        Vec3 eye = bangboo.getEyePosition(), look = bangboo.getLookAngle(), end = eye.add(look.scale(4.5));
        BlockHitResult blockHit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player));
        if (blockHit.getType() != HitResult.Type.MISS) {
            var held = player.getMainHandItem();
            var itemResult = held.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, blockHit));
            if (itemResult.consumesAction()) return new Object[]{true, "block"};
            var blockResult = level.getBlockState(blockHit.getBlockPos())
                .useItemOn(held, level, player, InteractionHand.MAIN_HAND, blockHit);
            return new Object[]{blockResult.consumesAction(), "block"};
        }
        return new Object[]{false, "nothing"};
    }

    @LuaFunction(mainThread = true)
    public final Object[] dig(double x, double y, double z) throws LuaException {
        var bangboo = requireBangboo();
        var level = requireLevel(bangboo);
        var pos = BlockPos.containing(x, y, z);
        var state = level.getBlockState(pos);
        if (state.isAir()) return new Object[]{false, "air"};
        if (state.getDestroySpeed(level, pos) < 0) return new Object[]{false, "unbreakable"};
        DropConsumer.set(level, pos, stack -> bangboo.getInternalInventory().addItem(stack));
        level.destroyBlock(pos, true, bangboo.getFakePlayer(level));
        DropConsumer.clearAndDrop(level, pos, Direction.UP);
        return new Object[]{true};
    }

    @LuaFunction(mainThread = true)
    public final Object[] place(double x, double y, double z) throws LuaException {
        var bangboo = requireBangboo();
        var level = requireLevel(bangboo);
        var player = bangboo.getFakePlayer(level);
        var held = player.getMainHandItem();
        if (held.isEmpty()) return new Object[]{false, "no item"};
        if (!(held.getItem() instanceof BlockItem blockItem)) return new Object[]{false, "not a block"};
        var pos = BlockPos.containing(x, y, z);
        var hitResult = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        var result = blockItem.place(new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND, hitResult)));
        return new Object[]{result.consumesAction()};
    }

    /** Collect nearby dropped item stacks into the Bangboo's internal inventory. Returns items collected. */
    @LuaFunction(mainThread = true)
    public final int pickup() throws LuaException {
        var bangboo = requireBangboo();
        var level = requireLevel(bangboo);
        int collected = 0;
        for (var item : level.getEntitiesOfClass(ItemEntity.class, bangboo.getBoundingBox().inflate(1.5))) {
            if (item.isRemoved()) continue;
            var stack = item.getItem().copy();
            var remainder = bangboo.getInternalInventory().addItem(stack);
            if (remainder.getCount() < stack.getCount()) {
                collected++;
                if (remainder.isEmpty()) item.discard();
                else item.setItem(remainder);
            }
        }
        return collected;
    }

    @LuaFunction(mainThread = true)
    public final Object[] attack(int entityId) throws LuaException {
        var bangboo = requireBangboo();
        var level = requireLevel(bangboo);
        Entity target = level.getEntity(entityId);
        if (target == null || !target.isAlive()) return new Object[]{false, "entity not found"};
        bangboo.getFakePlayer(level).attack(target);
        return new Object[]{true};
    }

    private static ServerLevel requireLevel(BangbooEntity b) throws LuaException {
        if (!(b.level() instanceof ServerLevel sl)) throw new LuaException("Not in server level");
        return sl;
    }
}
