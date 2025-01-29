package dev.doublekekse.area_lib.util;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AABBUtils {
    public static AABB encapsulate(AABB a, AABB b) {
        Vec3 newMin = new Vec3(Math.min(a.minX, b.minX), Math.min(a.minY, b.minY), Math.min(a.minZ, b.minZ));

        Vec3 newMax = new Vec3(Math.max(a.maxX, b.maxX), Math.max(a.maxY, b.maxY), Math.max(a.maxZ, b.maxZ));

        return new AABB(newMin, newMax);
    }

    public static Direction.Axis longestAxis(AABB aabb) {
        double xLength = aabb.getXsize();
        double yLength = aabb.getYsize();
        double zLength = aabb.getZsize();

        if (xLength >= yLength && xLength >= zLength) {
            return Direction.Axis.X;
        } else if (yLength >= zLength) {
            return Direction.Axis.Y;
        } else {
            return Direction.Axis.Z;
        }
    }
}
