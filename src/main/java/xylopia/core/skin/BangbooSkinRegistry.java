package xylopia.core.skin;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import xylopia.core.Atmos;
import xylopia.core.entity.BangbooAnimations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BangbooSkinRegistry extends SimpleJsonResourceReloadListener {
    public static final BangbooSkinRegistry INSTANCE = new BangbooSkinRegistry();

    /** Defaults match the entity type's .sized(0.7, 1.0) and renderer's withScale(0.5). */
    public static final BangbooSkinDefinition FALLBACK = new BangbooSkinDefinition(
            ResourceLocation.fromNamespaceAndPath(Atmos.MODID, "geo/eous.geo.json"),
            ResourceLocation.fromNamespaceAndPath(Atmos.MODID, "textures/entity/skins/eous.png"),
            ResourceLocation.fromNamespaceAndPath(Atmos.MODID, "animations/eous.animation.json"),
            Map.of(
                BangbooAnimations.IDLE, new BangbooSkinDefinition.AnimEntry("animation.bangboo.idle", true, List.of()),
                BangbooAnimations.WALK, new BangbooSkinDefinition.AnimEntry("animation.bangboo.walk", true, List.of())
            ),
            Map.of(
                "happy", new BangbooSkinDefinition.CallableAnim("animation.bangboo.happy", false, 1.0f)
            ),
            0.5f,  // scale
            0.7f,  // hitboxWidth
            1.0f,  // hitboxHeight
            0f, 0f, 0f  // hitboxOffset x/y/z
    );

    private Map<ResourceLocation, BangbooSkinDefinition> skins = Collections.emptyMap();

    private BangbooSkinRegistry() { super(new Gson(), "bangboo_skin"); }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager manager, ProfilerFiller profiler) {
        var map = new HashMap<ResourceLocation, BangbooSkinDefinition>();
        for (var entry : objects.entrySet()) {
            try {
                JsonObject json     = entry.getValue().getAsJsonObject();
                var geo             = ResourceLocation.parse(json.get("geo").getAsString());
                var texture         = ResourceLocation.parse(json.get("texture").getAsString());
                var animation       = ResourceLocation.parse(json.get("animation").getAsString());
                var animations         = parseAnimations(json);
                var callableAnimations = parseCallableAnimations(json);
                float scale        = json.has("scale")         ? json.get("scale").getAsFloat()         : 0.5f;
                float hitboxWidth  = json.has("hitbox_width")  ? json.get("hitbox_width").getAsFloat()  : 0.7f;
                float hitboxHeight = json.has("hitbox_height") ? json.get("hitbox_height").getAsFloat() : 1.0f;
                float offX = 0f, offY = 0f, offZ = 0f;
                if (json.has("hitbox_offset")) {
                    var arr = json.getAsJsonArray("hitbox_offset");
                    if (arr.size() >= 1) offX = arr.get(0).getAsFloat();
                    if (arr.size() >= 2) offY = arr.get(1).getAsFloat();
                    if (arr.size() >= 3) offZ = arr.get(2).getAsFloat();
                }
                map.put(entry.getKey(), new BangbooSkinDefinition(geo, texture, animation, animations, callableAnimations, scale, hitboxWidth, hitboxHeight, offX, offY, offZ));
            } catch (Exception e) {
                Atmos.LOGGER.error("Failed to load bangboo skin {}: {}", entry.getKey(), e.getMessage());
            }
        }
        skins = Collections.unmodifiableMap(map);
        Atmos.LOGGER.info("Loaded {} bangboo skin(s)", skins.size());
    }

    private static Map<String, BangbooSkinDefinition.AnimEntry> parseAnimations(JsonObject json) {
        var out = new HashMap<String, BangbooSkinDefinition.AnimEntry>();

        if (json.has("animations")) {
            // ── New format ────────────────────────────────────────────────────
            for (var kv : json.getAsJsonObject("animations").entrySet()) {
                String slot    = kv.getKey();
                JsonElement val = kv.getValue();

                if (val.isJsonPrimitive()) {
                    out.put(slot, new BangbooSkinDefinition.AnimEntry(val.getAsString(), true, List.of()));
                } else {
                    JsonObject obj      = val.getAsJsonObject();
                    String baseName     = obj.get("base").getAsString();
                    boolean baseLoop    = !obj.has("loop") || obj.get("loop").getAsBoolean();
                    var variants        = new ArrayList<BangbooSkinDefinition.Variant>();

                    if (obj.has("variants")) {
                        for (var ve : obj.getAsJsonArray("variants")) {
                            JsonObject vo = ve.getAsJsonObject();
                            variants.add(new BangbooSkinDefinition.Variant(
                                vo.get("name").getAsString(),
                                vo.has("weight")       ? vo.get("weight").getAsInt()        : 1,
                                vo.has("loop")         && vo.get("loop").getAsBoolean(),
                                vo.has("min_interval") ? vo.get("min_interval").getAsFloat() : 15f,
                                vo.has("max_interval") ? vo.get("max_interval").getAsFloat() : 45f,
                                vo.has("duration")     ? vo.get("duration").getAsFloat()     : 5f
                            ));
                        }
                    }
                    out.put(slot, new BangbooSkinDefinition.AnimEntry(baseName, baseLoop, List.copyOf(variants)));
                }
            }
        } else if (json.has("supports")) {
            // ── Legacy format ─────────────────────────────────────────────────
            for (var elem : json.getAsJsonArray("supports"))
                out.put(elem.getAsString(),
                    new BangbooSkinDefinition.AnimEntry(elem.getAsString(), true, List.of()));
        } else {
            out.put(BangbooAnimations.IDLE, new BangbooSkinDefinition.AnimEntry("animation.bangboo.idle", true, List.of()));
            out.put(BangbooAnimations.WALK, new BangbooSkinDefinition.AnimEntry("animation.bangboo.walk", true, List.of()));
        }

        return Collections.unmodifiableMap(out);
    }

    private static Map<String, BangbooSkinDefinition.CallableAnim> parseCallableAnimations(JsonObject json) {
        if (!json.has("callable_animations")) return Map.of();
        var out = new HashMap<String, BangbooSkinDefinition.CallableAnim>();
        for (var kv : json.getAsJsonObject("callable_animations").entrySet()) {
            JsonObject obj = kv.getValue().getAsJsonObject();
            String animKey = obj.get("animation").getAsString();
            boolean loop   = obj.has("loop") && obj.get("loop").getAsBoolean();
            float duration = obj.has("duration") ? obj.get("duration").getAsFloat() : 1.0f;
            out.put(kv.getKey(), new BangbooSkinDefinition.CallableAnim(animKey, loop, duration));
        }
        return Collections.unmodifiableMap(out);
    }

    /** Returns the skin definition for the given ID, or Eous as fallback. */
    public BangbooSkinDefinition get(String skinId) {
        try {
            return skins.getOrDefault(ResourceLocation.parse(skinId), FALLBACK);
        } catch (Exception e) {
            return FALLBACK;
        }
    }

    public Set<ResourceLocation> ids() { return skins.keySet(); }
}
