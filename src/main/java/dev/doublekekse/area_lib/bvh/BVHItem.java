package dev.doublekekse.area_lib.bvh;

import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface BVHItem {
    /**
     * Checks whether the given position in the specified level is contained within the item.
     *
     * @param level    the level (world) to check in
     * @param position the position to check
     * @return true if the position is within the item, false otherwise
     */
    boolean contains(Level level, Vec3 position);

    /**
     * Gets the bounding box of the item, if not empty. The bounding box is used for
     * spatial partitioning and optimization in BVH structures.
     *
     * @param savedData the saved area data that may influence the bounding box
     * @return the bounding box as an {@link AABB}, or null if empty
     */
    @Nullable AABB getBoundingBox(AreaSavedData savedData);
}
