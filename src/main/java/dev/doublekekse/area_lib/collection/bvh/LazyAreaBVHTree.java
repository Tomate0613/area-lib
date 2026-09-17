package dev.doublekekse.area_lib.collection.bvh;

import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.collection.AbstractAreaCollection;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

@ApiStatus.Experimental
public class LazyAreaBVHTree extends AbstractAreaCollection {
    private @Nullable BVHNode node;

    public LazyAreaBVHTree(AreaSavedData savedData) {
        super(savedData);
    }

    public LazyAreaBVHTree(AreaSavedData savedData, Collection<Identifier> areaIds) {
        super(savedData, areaIds);
    }

    @Override
    protected void invalidate() {
        node = null;
    }

    private void build() {
        var areas = areaIds.stream().map(savedData::get).filter(Objects::nonNull).toList();
        node = new BVHNode(areas);
    }

    public boolean contains(Level level, Vec3 position) {
        if (areaIds.isEmpty()) {
            return false;
        }
        if (node == null) {
            build();
        }


        return node.contains(level, position);
    }

    public List<Area> findAreasContaining(Level level, Vec3 position) {
        if (areaIds.isEmpty()) {
            return Collections.emptyList();
        }
        if (node == null) {
            build();
        }

        return node.findAreasContaining(level, position);
    }

    @ApiStatus.Experimental
    public List<Area> findAreasContaining(Level level, Vec3 position, Predicate<Area> predicate) {
        if (areaIds.isEmpty()) {
            return Collections.emptyList();
        }
        if (node == null) {
            build();
        }

        return node.findAreasContaining(level, position, predicate);
    }

    public List<Area> listAllAreas() {
        if (areaIds.isEmpty()) {
            return Collections.emptyList();
        }
        if (node == null) {
            build();
        }

        return node.listAllAreas();
    }

    public Set<Identifier> getAreaIds() {
        return areaIds;
    }

    public @Nullable AABB getBoundingBox() {
        if (areaIds.isEmpty()) {
            return null;
        }
        if (node == null) {
            build();
        }

        return node.getBoundingBox();
    }

    @Override
    public String toString() {
        return "LazyAreaBVHTree{" +
            "areaIds=" + areaIds +
            '}';
    }
}
