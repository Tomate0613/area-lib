package dev.doublekekse.area_lib.collection;

import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

public class AreaIntersection extends AbstractAreaCollection {
    private @Nullable Collection<Area> areas;
    private @Nullable AABB boundingBox;

    public AreaIntersection(AreaSavedData savedData) {
        super(savedData);
    }

    public AreaIntersection(AreaSavedData savedData, Collection<Identifier> areaIds) {
        super(savedData, areaIds);
    }

    @Override
    protected void invalidate() {
        areas = null;
        boundingBox = null;
    }

    private void compute() {
        if (areas != null) {
            return;
        }

        areas = areaIds.stream().map(savedData::get).toList();
        boundingBox = null;

        for (var area : areas) {
            var aabb = area.getBoundingBox();

            if (aabb == null) {
                return;
            }

            if (boundingBox == null) {
                boundingBox = aabb;
            } else {
                boundingBox = boundingBox.intersect(aabb);
            }
        }
    }

    @Override
    public boolean contains(Level level, Vec3 position) {
        compute();

        if (boundingBox == null || !boundingBox.contains(position)) {
            return false;
        }

        for (var area : areas) {
            if (!area.contains(level, position)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Collection<Area> listAllAreas() {
        compute();

        return areas;
    }

    @Override
    public @Nullable AABB getBoundingBox() {
        compute();

        return boundingBox;
    }
}
