package xylopia.core.computer.peripheral;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScannerPeripheral extends BangbooPeripheral {
    private static final boolean SABLE_LOADED = ModList.get().isLoaded("sable");

    public ScannerPeripheral(int computerID) { super(computerID); }

    @Override public String getType() { return "scanner"; }

    @LuaFunction(mainThread = true)
    public final List<Map<String, Object>> scan(IArguments args) throws LuaException {
        var bangboo = bangboo();
        if (bangboo == null || !(bangboo.level() instanceof ServerLevel level)) return List.of();
        int radius = Math.min(Math.max(1, args.getInt(0)), 64);

        // Optional second arg: table of name strings to include.
        // Matches against the "name" field of each result (registry name for blocks,
        // display name for entities/items). Omit to return everything.
        Set<String> filter = null;
        if (args.count() >= 2) {
            filter = new HashSet<>();
            for (var value : args.getTable(1).values()) {
                if (value instanceof String s) filter.add(s);
            }
        }
        final var filterSet = filter;

        AABB box = bangboo.getBoundingBox().inflate(radius);
        List<Map<String, Object>> results = new ArrayList<>();

        for (Entity entity : level.getEntities(bangboo, box, e -> e.isAlive() && !(e instanceof ItemEntity))) {
            if (bangboo.distanceTo(entity) > radius) continue;
            String name = entity.getName().getString();
            if (filterSet != null && !filterSet.contains(name)) continue;
            var entry = new HashMap<String, Object>();
            entry.put("type", "entity");
            entry.put("name", name);
            entry.put("id", entity.getId());
            entry.put("distance", bangboo.distanceTo(entity));
            entry.put("x", entity.getX()); entry.put("y", entity.getY()); entry.put("z", entity.getZ());
            if (entity instanceof LivingEntity living) {
                entry.put("health", (double) living.getHealth());
                entry.put("maxHealth", (double) living.getMaxHealth());
            }
            results.add(entry);
        }

        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box)) {
            if (bangboo.distanceTo(item) > radius) continue;
            String name = item.getItem().getDisplayName().getString();
            if (filterSet != null && !filterSet.contains(name)) continue;
            var entry = new HashMap<String, Object>();
            entry.put("type", "item");
            entry.put("name", name);
            entry.put("count", item.getItem().getCount());
            entry.put("distance", bangboo.distanceTo(item));
            entry.put("x", item.getX()); entry.put("y", item.getY()); entry.put("z", item.getZ());
            results.add(entry);
        }

        int blockR = Math.min(radius, 32);
        var origin = bangboo.blockPosition();
        var bangbooPos = bangboo.position();

        // Collect all non-air blocks, sort nearest-first, then truncate to 1000.
        // This ensures the cap is hit on the closest blocks rather than whatever
        // happens to sit at the far edge of the scan cube (e.g. underground terrain).
        record BlockEntry(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state, double distSq) {}
        var blockEntries = new ArrayList<BlockEntry>();
        for (int dx = -blockR; dx <= blockR; dx++) {
            for (int dy = -blockR; dy <= blockR; dy++) {
                for (int dz = -blockR; dz <= blockR; dz++) {
                    var pos = origin.offset(dx, dy, dz);
                    var state = level.getBlockState(pos);
                    if (state.isAir()) continue;
                    String name = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
                    if (filterSet != null && !filterSet.contains(name)) continue;
                    double distSq = bangbooPos.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    blockEntries.add(new BlockEntry(pos, state, distSq));
                }
            }
        }
        blockEntries.sort(Comparator.comparingDouble(BlockEntry::distSq));
        for (var entry : blockEntries) {
            if (results.size() >= 1000) break;
            var map = new HashMap<String, Object>();
            map.put("type", "block");
            map.put("name", net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(entry.state().getBlock()).toString());
            map.put("x", entry.pos().getX()); map.put("y", entry.pos().getY()); map.put("z", entry.pos().getZ());
            results.add(map);
        }

        if (SABLE_LOADED && results.size() < 1000) {
            SableSubLevelScanner.scan(level, bangboo, blockR, results, 1000);
        }
        return results;
    }
}
