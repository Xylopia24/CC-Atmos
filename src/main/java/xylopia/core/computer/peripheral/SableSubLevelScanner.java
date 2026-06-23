package xylopia.core.computer.peripheral;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scans blocks on Sable physics SubLevels (ships, aircraft, etc.).
 * Isolated in its own class so Sable imports are never loaded when Sable is absent.
 */
final class SableSubLevelScanner {
    private SableSubLevelScanner() {}

    static void scan(ServerLevel level, Entity bangboo, int blockR, List<Map<String, Object>> results, int limit) {
        var container = SubLevelContainer.getContainer(level);
        var bangbooPos = new Vec3(bangboo.getX(), bangboo.getY(), bangboo.getZ());
        var worldAABB = bangboo.getBoundingBox().inflate(blockR);

        for (var subLevel : container.getAllSubLevels()) {
            if (!subLevel.boundingBox().intersects(worldAABB)) continue;

            var subLevelLevel = subLevel.getLevel();
            var pose = subLevel.logicalPose();

            // Find where the Bangboo sits in this SubLevel's local coordinate space
            var localBangboo = pose.transformPositionInverse(bangbooPos);
            int lx = (int) Math.floor(localBangboo.x);
            int ly = (int) Math.floor(localBangboo.y);
            int lz = (int) Math.floor(localBangboo.z);

            for (int dlx = -blockR; dlx <= blockR; dlx++) {
                for (int dly = -blockR; dly <= blockR; dly++) {
                    for (int dlz = -blockR; dlz <= blockR; dlz++) {
                        var localPos = new BlockPos(lx + dlx, ly + dly, lz + dlz);
                        var state = subLevelLevel.getBlockState(localPos);
                        if (state.isAir()) continue;

                        // Transform block center back to world space for distance check and reporting
                        var worldCenter = pose.transformPosition(new Vec3(
                            localPos.getX() + 0.5, localPos.getY() + 0.5, localPos.getZ() + 0.5
                        ));
                        if (bangbooPos.distanceTo(worldCenter) > blockR) continue;

                        var entry = new HashMap<String, Object>();
                        entry.put("type", "block");
                        entry.put("name", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
                        entry.put("x", worldCenter.x);
                        entry.put("y", worldCenter.y);
                        entry.put("z", worldCenter.z);
                        entry.put("sublevel", true);
                        results.add(entry);
                        if (results.size() >= limit) return;
                    }
                }
            }
        }
    }
}
