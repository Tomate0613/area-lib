package dev.doublekekse.area_lib.areas;

import dev.doublekekse.area_lib.AreaLib;
import dev.doublekekse.area_lib.bvh.LazyAreaBVHTree;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class UnionArea extends CompositeArea {
    public UnionArea(AreaSavedData savedData, ResourceLocation id, LazyAreaBVHTree areas) {
        super(savedData, id, areas);
    }

    public UnionArea(AreaSavedData savedData, ResourceLocation id) {
        super(savedData, id);
    }

    @Override
    public boolean contains(Level level, Vec3 position) {
        return areas.contains(level, position);
    }

    @Override
    public ResourceLocation getType() {
        return AreaLib.id("union");
    }
}
