package dev.doublekekse.area_lib.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;

public class CompoundUtils {
    public static CompoundTag fromAABB(AABB aabb) {
        var tag = new CompoundTag();

        tag.putDouble("minX", aabb.minX);
        tag.putDouble("minY", aabb.minY);
        tag.putDouble("minZ", aabb.minZ);

        tag.putDouble("maxX", aabb.maxX);
        tag.putDouble("maxY", aabb.maxY);
        tag.putDouble("maxZ", aabb.maxZ);

        return tag;
    }

    public static AABB toAABB(CompoundTag tag) {
        var minX = tag.getDouble("minX").orElse(0.0);
        var minY = tag.getDouble("minY").orElse(0.0);
        var minZ = tag.getDouble("minZ").orElse(0.0);

        var maxX = tag.getDouble("maxX").orElse(0.0);
        var maxY = tag.getDouble("maxY").orElse(0.0);
        var maxZ = tag.getDouble("maxZ").orElse(0.0);

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
