package dev.doublekekse.area_lib.bvh;

import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface BVHItem {
    boolean contains(Level level, Vec3 position);

    @Nullable AABB getBoundingBox(AreaSavedData savedData);
}
