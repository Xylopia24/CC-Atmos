package xylopia.core.client.skin;

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

public class BangbooSkinRegistry extends SimpleJsonResourceReloadListener {
    public static final BangbooSkinRegistry INSTANCE = new BangbooSkinRegistry();

    public static final ResourceLocation EOUS_ID = ResourceLocation.fromNamespaceAndPath(Atmos.MODID, "eous");
    public static final BangbooSkinDefinition FALLBACK = new BangbooSkinDefinition(
            ResourceLocation.fromNamespaceAndPath(Atmos.MODID, "geo/eous.geo.json"),
            ResourceLocation.fromNamespaceAndPath(Atmos.MODID, "textures/entity/skins/eous.png"),
            ResourceLocation.fromNamespaceAndPath(Atmos.MODID, "animations/eous.animation.json"),
            Map.of(
                BangbooAnimations.IDLE, new BangbooSkinDefinition.AnimEntry("idle", true, List.of()),
                BangbooAnimations.WALK, new BangbooSkinDefinition.AnimEntry("walk", true, List.of())
            )
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
                var animations      = parseAnimations(json);
                map.put(entry.getKey(), new BangbooSkinDefinition(geo, texture, animation, animations));
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
                String slot = kv.getKey();
                JsonElement val = kv.getValue();

                if (val.isJsonPrimitive()) {
                    // "idle": "idle"  →  simple looping single animation
                    out.put(slot, new BangbooSkinDefinition.AnimEntry(val.getAsString(), true, List.of()));
                } else {
                    JsonObject obj = val.getAsJsonObject();
                    String baseName = obj.get("base").getAsString();
                    boolean baseLoop = !obj.has("loop") || obj.get("loop").getAsBoolean();
                    var variants = new ArrayList<BangbooSkinDefinition.Variant>();

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
            // ── Legacy format — "supports": ["idle", "walk"] ──────────────────
            // Animation key is assumed to match the slot name exactly.
            for (var elem : json.getAsJsonArray("supports"))
                out.put(elem.getAsString(),
                    new BangbooSkinDefinition.AnimEntry(elem.getAsString(), true, List.of()));
        } else {
            // Absolute fallback
            out.put(BangbooAnimations.IDLE, new BangbooSkinDefinition.AnimEntry("idle", true, List.of()));
            out.put(BangbooAnimations.WALK, new BangbooSkinDefinition.AnimEntry("walk", true, List.of()));
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

    public java.util.Set<ResourceLocation> ids() { return skins.keySet(); }
}
