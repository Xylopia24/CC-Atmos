package xylopia.core.entity;

import com.mojang.authlib.GameProfile;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.core.computer.ComputerSide;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.core.TerminalSize;
import dan200.computercraft.shared.network.container.ComputerContainerData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayer;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import xylopia.core.computer.BangbooComputerRegistry;
import xylopia.core.computer.peripheral.BangbooPeripheralHub;
import xylopia.core.item.plugins.AntiGravCoreItem;
import xylopia.core.item.plugins.BangbooPluginItem;
import xylopia.core.skin.BangbooSkinDefinition;
import xylopia.core.skin.BangbooSkinRegistry;
import xylopia.core.menu.BangbooMenu;
import xylopia.core.registry.AtmosMenus;

public class BangbooEntity extends PathfinderMob implements GeoEntity, MenuProvider {
    private static final TerminalSize TERMINAL_SIZE = new TerminalSize(51, 19);
    private static final double INTERACT_RANGE = 8.0;
    private static final int PATHING_TIMEOUT   = 1200; // 60 s
    private static final int PROXIMITY_INTERVAL = 10;   // ticks between proximity scans

    private static final EntityDataAccessor<String> SKIN_ID =
            SynchedEntityData.defineId(BangbooEntity.class, EntityDataSerializers.STRING);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // ── Computer ──────────────────────────────────────────────────────────────
    private int bangbooID = -1;
    private int computerID = -1;
    private ComputerFamily computerFamily = ComputerFamily.NORMAL;
    @Nullable private ServerComputer serverComputer;
    @Nullable private BangbooPeripheralHub peripheralHub;

    // ── Inventories ───────────────────────────────────────────────────────────
    private final SimpleContainer pluginSlots       = new SimpleContainer(BangbooMenu.TOTAL_PLUGIN_COUNT);
    private final SimpleContainer internalInventory = new SimpleContainer(16);
    private final SimpleContainer backpackSlot      = new SimpleContainer(1);
    private int equippedSlot = 0;
    @Nullable private FakePlayer fakePlayer;

    // ── Compass ───────────────────────────────────────────────────────────────
    private final Map<String, int[]> waypoints = new LinkedHashMap<>();

    // ── Proximity ─────────────────────────────────────────────────────────────
    private int  proximityRadius  = 8;
    private boolean proximityEnabled = false;
    private final Map<UUID, String> proximityTracked = new LinkedHashMap<>();
    private int proximityTimer = 0;

    // ── Redstone output ───────────────────────────────────────────────────────
    private boolean redstoneOutput = false;
    private final java.util.Set<BlockPos> redstoneForced = new java.util.HashSet<>();

    // ── Lantern ───────────────────────────────────────────────────────────────
    private int      lanternLevel   = 0;
    private boolean  lanternOn      = false;
    @Nullable private BlockPos lastLanternPos = null;

    // ── Pathing ───────────────────────────────────────────────────────────────
    private boolean activePathing = false;
    private int     pathingTicks  = 0;
    private double  pathingSpeed  = 1.0;
    private double  pathTargetX   = 0;
    private double  pathTargetY   = 0;
    private double  pathTargetZ   = 0;
    private net.minecraft.world.phys.Vec3 lastPathPos   = null;
    private int     stuckTicks      = 0;
    private boolean finalApproach   = false;
    private boolean boardingApproach = false;

    // ─────────────────────────────────────────────────────────────────────────

    public BangbooEntity(EntityType<? extends BangbooEntity> type, Level level) {
        super(type, level);
        this.moveControl = new BangbooMoveControl(this);
        setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SKIN_ID, "ccatmos:eous");
    }

    // ── GeoEntity ─────────────────────────────────────────────────────────────

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Shared slot so the overlay controller knows which entry to consult
        String[] currentSlot = {""};

        // ── Base locomotion: always plays, never stops ────────────────────────
        controllers.add(new AnimationController<>(this, "locomotion", 5, state -> {
            var skin  = BangbooSkinRegistry.INSTANCE.get(getSkinId());
            String slot = state.isMoving() ? BangbooAnimations.WALK : BangbooAnimations.IDLE;
            currentSlot[0] = slot;
            var entry = skin.getAnimation(slot);
            if (entry == null) return software.bernie.geckolib.animation.PlayState.STOP;
            return state.setAndContinue(entry.baseRaw());
        }));

        // ── Overlay: plays variant animations on top, stops when done ─────────
        RawAnimation[]  overlayRaw   = {null};
        long[]          overlayEndMs = {-1};
        long[]          nextTriggerMs = {-1};

        controllers.add(new AnimationController<>(this, "overlay", 0, state -> {
            var skin  = BangbooSkinRegistry.INSTANCE.get(getSkinId());
            var entry = skin.getAnimation(currentSlot[0]);
            if (entry == null || !entry.hasVariants())
                return software.bernie.geckolib.animation.PlayState.STOP;

            long now = System.currentTimeMillis();

            // Initialise trigger schedule when first entering a slot with variants
            if (nextTriggerMs[0] < 0) nextTriggerMs[0] = scheduleNext(entry);

            // If a non-looping overlay is active, stop it once its duration expires
            if (overlayRaw[0] != null && overlayEndMs[0] > 0 && now >= overlayEndMs[0]) {
                overlayRaw[0]   = null;
                overlayEndMs[0] = -1;
                nextTriggerMs[0] = scheduleNext(entry);
            }

            // Fire a new variant when the trigger time arrives and no overlay is running
            if (overlayRaw[0] == null && now >= nextTriggerMs[0]) {
                var variant = pickVariant(entry);
                if (variant.loop()) {
                    overlayRaw[0]   = RawAnimation.begin().thenLoop(variant.name());
                    overlayEndMs[0] = -1; // looping — cleared by next trigger
                    nextTriggerMs[0] = scheduleNext(entry);
                } else {
                    overlayRaw[0]   = RawAnimation.begin().thenPlay(variant.name());
                    overlayEndMs[0] = now + (long)(variant.duration() * 1000L);
                    // nextTriggerMs reset after overlay ends (see above)
                }
            }

            if (overlayRaw[0] != null) return state.setAndContinue(overlayRaw[0]);
            return software.bernie.geckolib.animation.PlayState.STOP;
        }));
    }

    private static long scheduleNext(BangbooSkinDefinition.AnimEntry entry) {
        float lo = (float) entry.variants().stream().mapToDouble(v -> v.minInterval()).min().orElse(15);
        float hi = (float) entry.variants().stream().mapToDouble(v -> v.maxInterval()).max().orElse(45);
        long  ms = (long)((lo + Math.random() * (hi - lo)) * 1000L);
        return System.currentTimeMillis() + ms;
    }

    private static BangbooSkinDefinition.Variant pickVariant(
            BangbooSkinDefinition.AnimEntry entry) {
        int total = entry.variants().stream().mapToInt(v -> v.weight()).sum();
        int roll  = (int)(Math.random() * total);
        int sum   = 0;
        for (var v : entry.variants()) {
            sum += v.weight();
            if (roll < sum) return v;
        }
        return entry.variants().get(entry.variants().size() - 1);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 0.6)
                .add(Attributes.FOLLOW_RANGE, 256.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.ATTACK_SPEED, 4.0);
    }

    // ── Plugin helper ─────────────────────────────────────────────────────────

    /** True if any installed plug-in satisfies test. */
    public boolean hasPluginProviding(Predicate<BangbooPluginItem> test) {
        for (int i = 0; i < pluginSlots.getContainerSize(); i++) {
            var item = pluginSlots.getItem(i).getItem();
            if (item instanceof BangbooPluginItem p && test.test(p)) return true;
        }
        return false;
    }

    // ── Computer ──────────────────────────────────────────────────────────────

    public int getBangbooID() { return bangbooID; }

    private void createComputer(ServerLevel serverLevel) {
        if (bangbooID < 0)
            bangbooID = ComputerCraftAPI.createUniqueNumberedSaveDir(serverLevel.getServer(), "bangboo");
        if (computerID < 0)
            computerID = ComputerCraftAPI.createUniqueNumberedSaveDir(serverLevel.getServer(), "computer");
        computerFamily = hasPluginProviding(BangbooPluginItem::providesAdvancedTerminal)
            ? ComputerFamily.ADVANCED : ComputerFamily.NORMAL;
        BangbooComputerRegistry.register(computerID, this);
        var properties = ServerComputer.properties(computerID, computerFamily).terminalSize(TERMINAL_SIZE);
        serverComputer = new ServerComputer(serverLevel, blockPosition(), properties);
        serverComputer.register();
        serverComputer.turnOn();
        peripheralHub = new BangbooPeripheralHub();
        peripheralHub.setPeripherals(buildPeripheralMap());
        serverComputer.setPeripheral(ComputerSide.TOP, peripheralHub);
    }

    public void rebootComputer() {
        if (!(level() instanceof ServerLevel serverLevel) || serverComputer == null) return;
        peripheralHub = null;
        serverComputer.close();
        serverComputer = null;
        BangbooComputerRegistry.unregister(computerID);
        createComputer(serverLevel);
    }

    private Map<String, dan200.computercraft.api.peripheral.IPeripheral> buildPeripheralMap() {
        var map = new LinkedHashMap<String, dan200.computercraft.api.peripheral.IPeripheral>();
        for (int slot = 0; slot < pluginSlots.getContainerSize(); slot++) {
            var stack = pluginSlots.getItem(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof BangbooPluginItem plugin)) continue;
            plugin.createPeripherals(computerID).forEach(map::putIfAbsent);
        }
        return map;
    }

    @Nullable public ServerComputer getServerComputer() { return serverComputer; }

    // ── Inventories ───────────────────────────────────────────────────────────

    public SimpleContainer getPluginSlots()       { return pluginSlots; }
    public SimpleContainer getInternalInventory() { return internalInventory; }
    public SimpleContainer getBackpackSlot()      { return backpackSlot; }

    public int  getEquippedSlot() { return equippedSlot; }
    public void setEquippedSlot(int slot) {
        equippedSlot = Math.max(0, Math.min(slot, internalInventory.getContainerSize() - 1));
    }

    public FakePlayer getFakePlayer(ServerLevel serverLevel) {
        if (fakePlayer == null || fakePlayer.level() != serverLevel) {
            UUID uuid = UUID.nameUUIDFromBytes(("bangboo-" + bangbooID).getBytes(StandardCharsets.UTF_8));
            fakePlayer = new FakePlayer(serverLevel, new GameProfile(uuid, "Bangboo#" + (bangbooID >= 0 ? bangbooID : "?")));
        }
        fakePlayer.setPos(getX(), getY(), getZ());
        fakePlayer.setYRot(getYRot());
        fakePlayer.setXRot(getXRot());
        fakePlayer.getInventory().selected = 0;
        fakePlayer.getInventory().items.set(0, internalInventory.getItem(equippedSlot).copy());
        return fakePlayer;
    }

    // ── Compass ───────────────────────────────────────────────────────────────

    public Map<String, int[]> getWaypoints()                     { return Map.copyOf(waypoints); }
    public void setWaypoint(String label, int x, int y, int z)  { waypoints.put(label, new int[]{x, y, z}); }
    public boolean removeWaypoint(String label)                  { return waypoints.remove(label) != null; }

    // ── Proximity ─────────────────────────────────────────────────────────────

    public int     getProximityRadius()         { return proximityRadius; }
    public void    setProximityRadius(int r)    { proximityRadius = Math.max(1, Math.min(64, r)); }
    public boolean isProximityEnabled()         { return proximityEnabled; }
    public void    setProximityEnabled(boolean e) { proximityEnabled = e; }

    // ── Lantern ───────────────────────────────────────────────────────────────

    public int     getLanternLevel()            { return lanternLevel; }
    public void    setLanternLevel(int level)   { lanternLevel = Math.max(0, Math.min(15, level)); }
    public boolean isLanternOn()                { return lanternOn; }
    public void    setLanternOn(boolean on)     { lanternOn = on; }
    public void    toggleLantern()              { lanternOn = !lanternOn; }

    // ── Pathing ───────────────────────────────────────────────────────────────

    public void startPathing(double x, double y, double z) {
        pathTargetX = x;
        pathTargetY = y;
        pathTargetZ = z;
        issueNavigation();
        activePathing = true;
        pathingTicks  = 0;
    }

    public void redirectTo(double x, double y, double z) {
        pathTargetX = x;
        pathTargetY = y;
        pathTargetZ = z;
        issueNavigation();
    }

    private void issueNavigation() {
        var path = getNavigation().createPath(pathTargetX, pathTargetY, pathTargetZ, 0);
        if (path != null) {
            getNavigation().moveTo(path, pathingSpeed);
        } else {
            getNavigation().moveTo(pathTargetX, pathTargetY, pathTargetZ, pathingSpeed);
        }
    }

    public void startSteering(double x, double y, double z) {
        if (activePathing) cancelPathing();
        getNavigation().stop();
        var ctrl = (BangbooMoveControl) moveControl;
        ctrl.setSteerTarget(x, y, z);
        ctrl.setMode(BangbooMoveControl.Mode.STEER);
    }

    public void stopSteering() {
        var ctrl = (BangbooMoveControl) moveControl;
        if (ctrl.getMode() == BangbooMoveControl.Mode.STEER) {
            ctrl.setMode(BangbooMoveControl.Mode.FLIGHT);
        }
    }

    public void cancelPathing() {
        if (!activePathing) return;
        getNavigation().stop();
        activePathing    = false;
        pathingTicks     = 0;
        stuckTicks       = 0;
        lastPathPos      = null;
        finalApproach    = false;
        boardingApproach = false;
        if (serverComputer != null) serverComputer.queueEvent("bangboo_path_done", new Object[]{false, "cancelled"});
    }

    public boolean isActivelyPathing()        { return activePathing; }
    public double  getPathingSpeed()          { return pathingSpeed; }
    public void    setPathingSpeed(double s)  { pathingSpeed = Math.max(0.5, Math.min(2.0, s)); }

    // ── Redstone output ───────────────────────────────────────────────────────

    public boolean getRedstoneOutput() { return redstoneOutput; }
    public void setRedstoneOutput(boolean on) { redstoneOutput = on; }

    // ── Anti-gravity ──────────────────────────────────────────────────────────

    public void setAntiGrav(boolean enable) {
        setNoGravity(enable);
        var ctrl = (BangbooMoveControl) this.moveControl;
        ctrl.setMode(enable ? BangbooMoveControl.Mode.FLIGHT : BangbooMoveControl.Mode.GROUND);
        this.navigation = enable
            ? new FlyingPathNavigation(this, level())
            : new GroundPathNavigation(this, level());
    }

    public boolean isAntiGravActive()  { return ((BangbooMoveControl) moveControl).getMode() == BangbooMoveControl.Mode.FLIGHT; }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, net.minecraft.world.damagesource.DamageSource source) {
        if (hasPluginProviding(p -> p instanceof AntiGravCoreItem)) return false;
        return super.causeFallDamage(fallDistance, damageMultiplier, source);
    }
    public boolean isAntiGravBobbing() { return ((BangbooMoveControl) moveControl).isBobbing(); }

    public void setAntiGravBobbing(boolean bobbing) {
        ((BangbooMoveControl) moveControl).setBobbing(bobbing);
    }

    // ── Cosmetic ──────────────────────────────────────────────────────────────

    public String getSkinId() { return entityData.get(SKIN_ID); }

    public void setSkinId(String id) {
        entityData.set(SKIN_ID, id);
        refreshDimensions();
    }

    @Override
    protected net.minecraft.world.entity.EntityDimensions getDefaultDimensions(net.minecraft.world.entity.Pose pose) {
        var skin = BangbooSkinRegistry.INSTANCE.get(getSkinId());
        return net.minecraft.world.entity.EntityDimensions.scalable(skin.hitboxWidth(), skin.hitboxHeight());
    }

    @Override
    public net.minecraft.world.phys.AABB makeBoundingBox() {
        var skin = BangbooSkinRegistry.INSTANCE.get(getSkinId());
        float ox = skin.hitboxOffsetX(), oy = skin.hitboxOffsetY(), oz = skin.hitboxOffsetZ();
        var box = super.makeBoundingBox();
        return (ox == 0 && oy == 0 && oz == 0) ? box : box.move(ox, oy, oz);
    }

    // ── Tick & Lifecycle ──────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel serverLevel) {
            if (serverComputer == null) createComputer(serverLevel);
            serverComputer.keepAlive();
            serverComputer.setPosition(serverLevel, blockPosition());
            tickLantern(serverLevel);
            tickRedstoneOutput(serverLevel);
            tickProximity(serverLevel);
            tickPathing();
        }
    }

    private void tickLantern(ServerLevel level) {
        if (!hasPluginProviding(BangbooPluginItem::providesLantern)) {
            clearLanternBlock(level);
            return;
        }
        BlockPos target = blockPosition().above();
        if (!target.equals(lastLanternPos)) clearLanternBlock(level);
        if (lanternOn && lanternLevel > 0) {
            BlockState current = level.getBlockState(target);
            if (current.isAir() || current.is(Blocks.LIGHT)) {
                level.setBlock(target,
                    Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, Math.max(1, lanternLevel)), 3);
                lastLanternPos = target;
            }
        } else {
            clearLanternBlock(level);
        }
    }

    private void clearLanternBlock(ServerLevel level) {
        if (lastLanternPos != null) {
            if (level.getBlockState(lastLanternPos).is(Blocks.LIGHT))
                level.removeBlock(lastLanternPos, false);
            lastLanternPos = null;
        }
    }

    private void tickRedstoneOutput(ServerLevel level) {
        if (!hasPluginProviding(BangbooPluginItem::providesRedstone)) {
            deactivateForcedLamps(level);
            return;
        }
        if (redstoneOutput) {
            BlockPos myPos = blockPosition();
            for (Direction dir : Direction.values()) {
                BlockPos adj = myPos.relative(dir);
                BlockState state = level.getBlockState(adj);
                if (state.is(Blocks.REDSTONE_LAMP)
                        && !state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)) {
                    level.setBlock(adj, state.setValue(
                            net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT, true), 3);
                    redstoneForced.add(adj.immutable());
                }
            }
        } else {
            deactivateForcedLamps(level);
        }
    }

    private void deactivateForcedLamps(ServerLevel level) {
        for (BlockPos pos : redstoneForced) {
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.REDSTONE_LAMP)
                    && state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)
                    && level.getBestNeighborSignal(pos) == 0) {
                level.setBlock(pos, state.setValue(
                        net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT, false), 3);
            }
        }
        redstoneForced.clear();
    }

    private void tickProximity(ServerLevel level) {
        if (++proximityTimer < PROXIMITY_INTERVAL) return;
        proximityTimer = 0;
        if (!proximityEnabled || serverComputer == null) return;
        if (!hasPluginProviding(BangbooPluginItem::providesProximity)) return;

        AABB area = getBoundingBox().inflate(proximityRadius);
        var nowNearby = new LinkedHashMap<UUID, String>();
        for (Entity entity : level.getEntities(this, area, Entity::isAlive)) {
            nowNearby.put(entity.getUUID(), entity.getName().getString());
            if (!proximityTracked.containsKey(entity.getUUID()))
                serverComputer.queueEvent("proximity_enter",
                    new Object[]{entity.getName().getString(), entity.getId(), distanceTo(entity)});
        }
        for (var entry : proximityTracked.entrySet())
            if (!nowNearby.containsKey(entry.getKey()))
                serverComputer.queueEvent("proximity_leave", new Object[]{entry.getValue()});
        proximityTracked.clear();
        proximityTracked.putAll(nowNearby);
    }

    private static final double ARRIVAL_THRESHOLD_SQ   = 0.75 * 0.75;
    private static final double FINAL_APPROACH_SQ      = 2.5  * 2.5;
    private static final double STUCK_MOVE_THRESHOLD_SQ = 0.05 * 0.05;
    private static final int    MAX_STUCK_TICKS         = 20;
    private static final int    MAX_BOARD_STUCK_TICKS   = 60; // more patience while walking onto ship

    private dev.ryanhcode.sable.sublevel.SubLevel currentSubLevel() {
        if (this instanceof dev.ryanhcode.sable.mixinterface.entity.entity_sublevel_collision.EntityMovementExtension ext)
            return ext.sable$getTrackingSubLevel();
        return null;
    }

    private boolean targetIsOnSubLevel() {
        var container = dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(level());
        if (container == null) return false;
        var bbox = new dev.ryanhcode.sable.companion.math.BoundingBox3d(
                BlockPos.containing(pathTargetX, pathTargetY, pathTargetZ));
        return container.queryIntersecting(bbox).iterator().hasNext();
    }

    private void finishPathing(boolean success, String reason) {
        activePathing    = false;
        pathingTicks     = 0;
        stuckTicks       = 0;
        lastPathPos      = null;
        finalApproach    = false;
        boardingApproach = false;
        getNavigation().stop();
        serverComputer.queueEvent("bangboo_path_done", new Object[]{success, reason});
    }

    private boolean isArrived() {
        double dx = getX() - pathTargetX;
        double dy = getY() - pathTargetY;
        double dz = getZ() - pathTargetZ;
        // Horizontal distance + Y tolerance: entity feet sit ~1 block above a solid
        // block target, so we allow up to 2 blocks of vertical offset.
        return (dx*dx + dz*dz) <= ARRIVAL_THRESHOLD_SQ && Math.abs(dy) <= 2.0;
    }

    private void tickPathing() {
        if (!activePathing || serverComputer == null) return;

        if (++pathingTicks > PATHING_TIMEOUT) {
            finishPathing(false, "timeout");
            return;
        }

        if (boardingApproach) { tickBoardingApproach(); return; }
        if (finalApproach)    { tickFinalApproach();    return; }

        if (getNavigation().isDone()) {
            if (isArrived()) {
                finishPathing(true, "arrived");
                return;
            }
            double dx = getX() - pathTargetX;
            double dy = getY() - pathTargetY;
            double dz = getZ() - pathTargetZ;
            double distSq = dx*dx + dy*dy + dz*dz;

            if (distSq <= FINAL_APPROACH_SQ) {
                // Vanilla nav stopped short — walk the last stretch directly.
                getNavigation().stop();
                ((BangbooMoveControl) moveControl).setGroundTarget(
                        pathTargetX, pathTargetY, pathTargetZ, pathingSpeed);
                finalApproach = true;
                stuckTicks = 0;
                lastPathPos = position();
            } else {
                // Nav finished but entity is still far — accumulate stuck counter.
                var curPos = position();
                if (lastPathPos != null && curPos.distanceToSqr(lastPathPos) < STUCK_MOVE_THRESHOLD_SQ) {
                    if (++stuckTicks >= MAX_STUCK_TICKS) {
                        // If the target is on a Sable sublevel, switch to boarding walk.
                        if (currentSubLevel() == null && targetIsOnSubLevel()) {
                            getNavigation().stop();
                            ((BangbooMoveControl) moveControl).setGroundTarget(
                                    pathTargetX, pathTargetY, pathTargetZ, pathingSpeed);
                            boardingApproach = true;
                            stuckTicks = 0;
                            lastPathPos = curPos;
                        } else {
                            finishPathing(false, "stuck");
                        }
                    } else {
                        lastPathPos = curPos;
                    }
                } else {
                    stuckTicks = 0;
                    lastPathPos = curPos;
                    issueNavigation();
                }
            }
        } else {
            lastPathPos = position();
            stuckTicks = 0;
        }
    }

    private void tickFinalApproach() {
        if (isArrived()) { finishPathing(true, "arrived"); return; }

        var curPos = position();
        if (lastPathPos != null && curPos.distanceToSqr(lastPathPos) < STUCK_MOVE_THRESHOLD_SQ) {
            if (++stuckTicks >= MAX_STUCK_TICKS) { finishPathing(false, "stuck"); return; }
        } else {
            stuckTicks = 0;
        }
        lastPathPos = curPos;
        ((BangbooMoveControl) moveControl).setGroundTarget(
                pathTargetX, pathTargetY, pathTargetZ, pathingSpeed);
    }

    private void tickBoardingApproach() {
        // Once Sable tracks us to the sublevel, hand back to regular nav.
        if (currentSubLevel() != null) {
            boardingApproach = false;
            stuckTicks = 0;
            lastPathPos = position();
            issueNavigation();
            return;
        }

        if (isArrived()) { finishPathing(true, "arrived"); return; }

        var curPos = position();
        if (lastPathPos != null && curPos.distanceToSqr(lastPathPos) < STUCK_MOVE_THRESHOLD_SQ) {
            if (++stuckTicks >= MAX_BOARD_STUCK_TICKS) { finishPathing(false, "stuck"); return; }
        } else {
            stuckTicks = 0;
        }
        lastPathPos = curPos;
        // Walk directly toward the ship — gravity + step-height handle the slabs.
        ((BangbooMoveControl) moveControl).setGroundTarget(
                pathTargetX, pathTargetY, pathTargetZ, pathingSpeed);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (level() instanceof ServerLevel sl) clearLanternBlock(sl);
        if (serverComputer != null) {
            BangbooComputerRegistry.unregister(computerID);
            serverComputer.close();
            serverComputer = null;
        }
        super.remove(reason);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        for (int i = 0; i < pluginSlots.getContainerSize(); i++) {
            ItemStack s = pluginSlots.getItem(i);
            if (!s.isEmpty()) spawnAtLocation(s);
        }
        for (int i = 0; i < internalInventory.getContainerSize(); i++) {
            ItemStack s = internalInventory.getItem(i);
            if (!s.isEmpty()) spawnAtLocation(s);
        }
        ItemStack bp = backpackSlot.getItem(0);
        if (!bp.isEmpty()) spawnAtLocation(bp);
    }

    // ── Plug-in callbacks ─────────────────────────────────────────────────────

    public void onPluginSlotChanged(int slotIndex, ItemStack oldStack, ItemStack newStack) {
        if (ItemStack.isSameItemSameComponents(oldStack, newStack)) return;
        boolean terminalChanged =
            (oldStack.getItem() instanceof BangbooPluginItem op && op.providesAdvancedTerminal()) ||
            (newStack.getItem() instanceof BangbooPluginItem np && np.providesAdvancedTerminal());
        if (!oldStack.isEmpty() && oldStack.getItem() instanceof BangbooPluginItem p)
            p.onRemoved(this, slotIndex, oldStack);
        if (!newStack.isEmpty() && newStack.getItem() instanceof BangbooPluginItem p)
            p.onInstalled(this, slotIndex, newStack);
        if (terminalChanged) {
            rebootComputer();
        } else if (peripheralHub != null) {
            peripheralHub.setPeripherals(buildPeripheralMap());
        }
    }

    // ── Interaction ───────────────────────────────────────────────────────────

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult superResult = super.mobInteract(player, hand);
        if (superResult.consumesAction()) return superResult;
        if (!level().isClientSide && serverComputer != null && player instanceof ServerPlayer sp) {
            sp.openMenu(this, buf -> new ComputerContainerData(serverComputer, ItemStack.EMPTY).toBytes(buf));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public boolean shouldShowName() { return hasCustomName(); }

    // ── MenuProvider ──────────────────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        if (hasCustomName()) return getCustomName();
        return bangbooID >= 0 ? Component.literal("Bangboo #" + bangbooID) : Component.literal("Bangboo");
    }

    @Override @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        if (serverComputer == null) return null;
        return new BangbooMenu(AtmosMenus.BANGBOO.get(), containerId, playerInventory,
            p -> p.distanceTo(this) <= INTERACT_RANGE, serverComputer, pluginSlots, internalInventory, this);
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (bangbooID >= 0)  tag.putInt("BangbooID", bangbooID);
        if (computerID >= 0) tag.putInt("ComputerID", computerID);
        ListTag pluginList = pluginSlots.createTag(level().registryAccess());
        if (!pluginList.isEmpty()) tag.put("PluginSlots", pluginList);
        ListTag invList = internalInventory.createTag(level().registryAccess());
        if (!invList.isEmpty()) tag.put("InternalInventory", invList);
        ListTag bpList = backpackSlot.createTag(level().registryAccess());
        if (!bpList.isEmpty()) tag.put("BackpackSlot", bpList);
        tag.putInt("EquippedSlot", equippedSlot);

        // Compass waypoints
        if (!waypoints.isEmpty()) {
            var wptTag = new CompoundTag();
            for (var entry : waypoints.entrySet()) wptTag.putIntArray(entry.getKey(), entry.getValue());
            tag.put("Waypoints", wptTag);
        }

        // Proximity
        tag.putInt("ProximityRadius", proximityRadius);
        tag.putBoolean("ProximityEnabled", proximityEnabled);

        // Lantern
        tag.putInt("LanternLevel", lanternLevel);
        tag.putBoolean("LanternOn", lanternOn);

        // Cosmetic
        tag.putString("SkinId", getSkinId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        bangbooID  = tag.contains("BangbooID")  ? tag.getInt("BangbooID")  : -1;
        computerID = tag.contains("ComputerID") ? tag.getInt("ComputerID") : -1;
        if (tag.contains("PluginSlots")) {
            pluginSlots.fromTag(tag.getList("PluginSlots", 10), level().registryAccess());
            for (int i = 0; i < pluginSlots.getContainerSize(); i++) {
                var s = pluginSlots.getItem(i);
                if (!s.isEmpty() && s.getItem() instanceof BangbooPluginItem p) p.onInstalled(this, i, s);
            }
        }
        if (tag.contains("InternalInventory"))
            internalInventory.fromTag(tag.getList("InternalInventory", 10), level().registryAccess());
        if (tag.contains("BackpackSlot"))
            backpackSlot.fromTag(tag.getList("BackpackSlot", 10), level().registryAccess());
        equippedSlot = tag.contains("EquippedSlot") ? tag.getInt("EquippedSlot") : 0;

        // Compass waypoints
        waypoints.clear();
        if (tag.contains("Waypoints", 10)) {
            var wptTag = tag.getCompound("Waypoints");
            for (String key : wptTag.getAllKeys()) waypoints.put(key, wptTag.getIntArray(key));
        }

        // Proximity
        proximityRadius  = tag.contains("ProximityRadius")  ? tag.getInt("ProximityRadius")      : 8;
        proximityEnabled = tag.contains("ProximityEnabled") && tag.getBoolean("ProximityEnabled");

        // Lantern
        lanternLevel = tag.contains("LanternLevel") ? tag.getInt("LanternLevel") : 0;
        lanternOn    = tag.contains("LanternOn")    && tag.getBoolean("LanternOn");

        // Cosmetic
        if (tag.contains("SkinId")) setSkinId(tag.getString("SkinId"));
    }
}
