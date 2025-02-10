package dev.doublekekse.area_lib.areas;

import dev.doublekekse.area_lib.AreaLib;
import dev.doublekekse.area_lib.bvh.LazyAreaBVHTree;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class UnionArea extends CompositeArea {
    public UnionArea(LazyAreaBVHTree areas) {
        super(areas);
    }

    public UnionArea() {

    }

    @Override
    public boolean contains(Level level, Vec3 position) {
        return areas.contains(level, position);
    }

    @Override
    public ResourceLocation getType() {
        return AreaLib.id("union");
    }

    @Override
    public String toString() {
        return "UnionArea " + super.toString();
    }
}
