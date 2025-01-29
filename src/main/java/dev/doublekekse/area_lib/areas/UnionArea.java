package dev.doublekekse.area_lib.areas;

import dev.doublekekse.area_lib.AreaLib;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_lib.util.AABBUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Set;

public class UnionArea extends CompositeArea {
    public UnionArea(Set<ResourceLocation> areaIds) {
        super(areaIds);
    }

    public UnionArea() {

    }

    @Override
    public boolean contains(Level level, Vec3 position) {
        var areas = getAreas(AreaLib.getSavedData(level));

        for (var area : areas) {
            if (area.contains(level, position)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public ResourceLocation getType() {
        return AreaLib.id("union");
    }

    @Override
    public AABB getBoundingBox(AreaSavedData savedData) {
        return getAreas(savedData).stream()
            .map((area) -> area.getBoundingBox(savedData))
            .filter(Objects::nonNull)
            .reduce(AABBUtils::encapsulate)
            .orElse(null);
    }

    @Override
    public String toString() {
        return "UnionArea " + super.toString();
    }
}
