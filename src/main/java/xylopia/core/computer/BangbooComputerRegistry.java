package xylopia.core.computer;

import xylopia.core.entity.BangbooEntity;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

/** Maps CC computer IDs to their owning Bangboo entity. Populated server-side only. */
public final class BangbooComputerRegistry {
    private static final ConcurrentHashMap<Integer, WeakReference<BangbooEntity>> MAP =
            new ConcurrentHashMap<>();

    private BangbooComputerRegistry() {}

    public static void register(int id, BangbooEntity entity) {
        MAP.put(id, new WeakReference<>(entity));
    }

    public static void unregister(int id) {
        MAP.remove(id);
    }

    @Nullable
    public static BangbooEntity get(int id) {
        var ref = MAP.get(id);
        if (ref == null) return null;
        var entity = ref.get();
        if (entity == null || entity.isRemoved()) {
            MAP.remove(id);
            return null;
        }
        return entity;
    }
}
