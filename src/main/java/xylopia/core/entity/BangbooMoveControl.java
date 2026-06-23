package xylopia.core.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

public class BangbooMoveControl extends MoveControl {

    public enum Mode {
        GROUND,
        FLIGHT,
        STEER,  // smooth direct velocity steering, bypasses pathfinding
        SWIM    // stub for future implementation
    }

    private Mode mode    = Mode.GROUND;
    private boolean bobbing   = true;
    private int     bobbingTick = 0;

    private double steerX = 0, steerY = 0, steerZ = 0;

    public BangbooMoveControl(BangbooEntity mob) {
        super(mob);
    }

    // ── Mode & bobbing ────────────────────────────────────────────────────────

    public void setMode(Mode mode) {
        this.mode = mode;
        mob.setDeltaMovement(Vec3.ZERO);
        bobbingTick = 0;
    }

    public void setSteerTarget(double x, double y, double z) {
        steerX = x; steerY = y; steerZ = z;
    }

    public Mode getMode()              { return mode; }
    public void setBobbing(boolean b)  { bobbing = b; }
    public boolean isBobbing()         { return bobbing; }

    // ── Tick ──────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        switch (mode) {
            case FLIGHT -> tickFlight();
            case STEER  -> tickSteer();
            default     -> super.tick();   // GROUND and SWIM-stub use vanilla
        }
    }

    // ── Flight movement ───────────────────────────────────────────────────────

    private void tickFlight() {
        if (operation != Operation.MOVE_TO) {
            tickFlightIdle();
            return;
        }

        double dx = wantedX - mob.getX();
        double dy = wantedY - (mob.getY() + mob.getBbHeight() / 2.0);
        double dz = wantedZ - mob.getZ();
        double distSq = dx * dx + dy * dy + dz * dz;

        if (distSq < 0.25) {
            operation = Operation.WAIT;
            mob.setDeltaMovement(mob.getDeltaMovement().scale(0.5));
            return;
        }

        double dist  = Math.sqrt(distSq);
        double speed = mob.getAttributeValue(Attributes.FLYING_SPEED) * speedModifier;

        mob.setDeltaMovement(dx / dist * speed, dy / dist * speed, dz / dist * speed);

        // Smoothly turn to face direction of travel
        float targetYaw = (float)(Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        mob.setYRot(mob.getYRot() + Mth.wrapDegrees(targetYaw - mob.getYRot()) * 0.3f);
        bobbingTick = 0;
    }

    private void tickFlightIdle() {
        var cur = mob.getDeltaMovement();
        if (bobbing) {
            bobbingTick++;
            // Set Y directly to a small sine value — never accumulates
            double wave = Math.sin(bobbingTick * 0.05) * 0.004;
            mob.setDeltaMovement(cur.x * 0.85, wave, cur.z * 0.85);
        } else {
            mob.setDeltaMovement(cur.x * 0.85, 0.0, cur.z * 0.85);
        }
    }

    // ── Ground final-approach (bypasses nav, walks directly to exact coords) ──

    public void setGroundTarget(double x, double y, double z, double speed) {
        wantedX = x;
        wantedY = y;
        wantedZ = z;
        speedModifier = speed;
        operation = Operation.MOVE_TO;
    }

    // ── Smooth steering (bypasses pathfinding, velocity-blended each tick) ────

    private void tickSteer() {
        double dx = steerX - mob.getX();
        double dy = steerY - (mob.getY() + mob.getBbHeight() / 2.0);
        double dz = steerZ - mob.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist < 0.15) {
            // At target — damp to a stop
            mob.setDeltaMovement(mob.getDeltaMovement().scale(0.5));
            return;
        }

        // Proportional speed: full speed far away, slows as it approaches
        double maxSpeed = mob.getAttributeValue(Attributes.FLYING_SPEED);
        double desiredSpeed = Math.min(maxSpeed, dist * 0.4);

        double dvx = (dx / dist) * desiredSpeed;
        double dvy = (dy / dist) * desiredSpeed;
        double dvz = (dz / dist) * desiredSpeed;

        // Blend: 70% current velocity + 30% toward desired — smooth acceleration & deceleration
        var cur = mob.getDeltaMovement();
        mob.setDeltaMovement(
            cur.x * 0.7 + dvx * 0.3,
            cur.y * 0.7 + dvy * 0.3,
            cur.z * 0.7 + dvz * 0.3
        );

        // Smooth yaw toward direction of travel
        float targetYaw = (float)(Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        mob.setYRot(mob.getYRot() + Mth.wrapDegrees(targetYaw - mob.getYRot()) * 0.15f);
        bobbingTick = 0;
    }
}
