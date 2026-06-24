package xylopia.core.computer;

import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import xylopia.core.entity.BangbooEntity;
import xylopia.core.skin.BangbooSkinRegistry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The built-in "bangboo" Lua table available in every Bangboo's computer.
 * Plug-in APIs will be added as separate tables (e.g. "scanner", "pathing").
 */
public class BangbooBaseAPI implements ILuaAPI {

    private final int computerID;

    public BangbooBaseAPI(int computerID) {
        this.computerID = computerID;
    }

    @Override
    public String[] getNames() { return new String[]{"bangboo"}; }

    @Nullable
    private BangbooEntity bangboo() { return BangbooComputerRegistry.get(computerID); }

    private BangbooEntity require() throws LuaException {
        var b = bangboo();
        if (b == null) throw new LuaException("Bangboo is not available");
        return b;
    }

    // ── Status ────────────────────────────────────────────────────────────────

    @LuaFunction
    public final Map<String, Object> getPos() throws LuaException {
        var b = require();
        var pos = b.position();
        var map = new HashMap<String, Object>();
        map.put("x", pos.x);
        map.put("y", pos.y);
        map.put("z", pos.z);
        map.put("yaw", (double) b.getYRot());
        map.put("pitch", (double) b.getXRot());
        return map;
    }

    @LuaFunction
    public final double getHealth() throws LuaException {
        return require().getHealth();
    }

    @LuaFunction
    public final double getMaxHealth() throws LuaException {
        return require().getMaxHealth();
    }

    @LuaFunction
    public final boolean isOnGround() throws LuaException {
        return require().onGround();
    }

    @LuaFunction
    public final double getFacing() throws LuaException {
        return require().getYRot();
    }

    @LuaFunction
    public final int getID() throws LuaException {
        return require().getBangbooID();
    }

    @LuaFunction
    public final String getName() throws LuaException {
        return require().getDisplayName().getString();
    }

    @LuaFunction(mainThread = true)
    public final void setName(String name) throws LuaException {
        var b = require();
        if (name == null || name.isBlank()) {
            b.setCustomName(null);
        } else {
            b.setCustomName(Component.literal(name));
        }
    }

    // ── Movement ──────────────────────────────────────────────────────────────

    @LuaFunction(mainThread = true)
    public final void moveTo(double x, double y, double z) throws LuaException {
        require().getNavigation().moveTo(x, y, z, 1.0);
    }

    @LuaFunction(mainThread = true)
    public final void stop() throws LuaException {
        require().getNavigation().stop();
    }

    @LuaFunction
    public final boolean isMoving() throws LuaException {
        return !require().getNavigation().isDone();
    }

    /** Set the Bangboo's horizontal facing in degrees (0 = south, 90 = west, etc.). */
    @LuaFunction(mainThread = true)
    public final void face(double yaw) throws LuaException {
        var b = require();
        b.setYRot((float) yaw);
        b.yRotO = (float) yaw;
    }

    // ── Animations ───────────────────────────────────────────────────────────

    @LuaFunction(mainThread = true)
    public final void playAnimation(String name) throws LuaException {
        var b = require();
        if (!BangbooSkinRegistry.INSTANCE.get(b.getSkinId()).supportsCallable(name))
            throw new LuaException("animation '" + name + "' is not defined for this skin");
        b.requestCallableAnim(name);
    }

    @LuaFunction(mainThread = true)
    public final void stopAnimation() throws LuaException {
        require().stopCallableAnim();
    }

    @LuaFunction
    public final List<String> listAnimations() throws LuaException {
        return new ArrayList<>(BangbooSkinRegistry.INSTANCE.get(require().getSkinId()).callableAnimations().keySet());
    }

    // ── Basic scan ────────────────────────────────────────────────────────────

    /**
     * Returns a rough picture of nearby surroundings:
     *   entities  – number of living entities within 16 blocks
     *   nearest   – distance to the closest one (-1 if none)
     *   health    – this Bangboo's health fraction (0–1)
     */
    @LuaFunction
    public final Map<String, Object> scan() throws LuaException {
        var b = require();
        var nearby = b.level().getEntitiesOfClass(
                LivingEntity.class,
                b.getBoundingBox().inflate(16),
                e -> e != b);

        double nearest = nearby.stream()
                .mapToDouble(b::distanceTo)
                .min().orElse(-1);

        var map = new HashMap<String, Object>();
        map.put("entities", nearby.size());
        map.put("nearest", nearest);
        map.put("health", b.getHealth() / b.getMaxHealth());
        return map;
    }
}
