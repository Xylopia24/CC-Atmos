package xylopia.core.client.skin;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.List;
import java.util.Map;

public record BangbooSkinDefinition(
        ResourceLocation geo,
        ResourceLocation texture,
        ResourceLocation animation,
        Map<String, AnimEntry> animations
) {

    // ── Data types ─────────────────────────────────────────────────────────────

    /**
     * A single weighted option inside a variant list.
     * @param name         Key in the animation JSON (e.g. "idle2")
     * @param weight       Relative probability vs other variants (integer, ≥ 1)
     * @param loop         True = loop this variant; false = play once then return to base
     * @param minInterval  Minimum seconds between triggers
     * @param maxInterval  Maximum seconds between triggers
     * @param duration     How long to play before handing back to the base (seconds).
     *                     Required for non-looping variants — set this to your animation's length.
     *                     Ignored when loop=true.
     */
    public record Variant(String name, int weight, boolean loop, float minInterval, float maxInterval, float duration) {}

    /**
     * The full definition for one animation slot (e.g. "idle").
     * @param baseName  Animation key for the base/default animation
     * @param baseLoop  Whether the base animation loops (almost always true)
     * @param variants  Optional list of weighted variants; empty = simple single animation
     */
    public record AnimEntry(String baseName, boolean baseLoop, List<Variant> variants) {
        public boolean hasVariants() { return !variants.isEmpty(); }

        public RawAnimation baseRaw() {
            return baseLoop
                ? RawAnimation.begin().thenLoop(baseName)
                : RawAnimation.begin().thenPlay(baseName);
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    public boolean supports(String slot) { return animations.containsKey(slot); }
    public AnimEntry getAnimation(String slot) { return animations.get(slot); }
}
