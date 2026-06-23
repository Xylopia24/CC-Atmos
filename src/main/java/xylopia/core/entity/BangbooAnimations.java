package xylopia.core.entity;

import software.bernie.geckolib.animation.RawAnimation;

import java.util.Set;

public final class BangbooAnimations {

    // ── Short name catalogue ───────────────────────────────────────────────────
    public static final String IDLE       = "idle";
    public static final String WALK       = "walk";
    public static final String RUN        = "run";
    public static final String FLY        = "fly";
    public static final String SWIM       = "swim";
    public static final String JUMP       = "jump";
    public static final String FALL       = "fall";
    public static final String TOOL_SWING = "tool_swing";
    public static final String HURT       = "hurt";
    public static final String DEATH      = "death";

    /** Every valid short name — skin creators use these in the "supports" array. */
    public static final Set<String> ALL = Set.of(
            IDLE, WALK, RUN, FLY, SWIM, JUMP, FALL, TOOL_SWING, HURT, DEATH
    );

    // ── Helpers ───────────────────────────────────────────────────────────────

    public static RawAnimation looping(String shortName) {
        return RawAnimation.begin().thenLoop("animation.bangboo." + shortName);
    }

    public static RawAnimation once(String shortName) {
        return RawAnimation.begin().thenPlay("animation.bangboo." + shortName);
    }

    private BangbooAnimations() {}
}
