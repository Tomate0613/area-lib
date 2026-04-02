package dev.doublekekse.area_lib.bvh;

import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

@ApiStatus.Experimental
public class LazyAreaBVHTree {
    private @Nullable BVHNode node;
    private final Set<Identifier> areaIds = new HashSet<>();
    private final AreaSavedData savedData;

    public LazyAreaBVHTree(AreaSavedData savedData) {
        this.savedData = savedData;
    }

    public LazyAreaBVHTree(AreaSavedData savedData, Collection<Identifier> areaIds) {
        this.savedData = savedData;
        this.areaIds.addAll(areaIds);
    }

    private void invalidate() {
        node = null;
    }

    public void add(Identifier areaId) {
        var didAdd = areaIds.add(areaId);

        if (didAdd) {
            invalidate();
        }
    }

    public void remove(Identifier areaId) {
        var didRemove = areaIds.remove(areaId);

        if (didRemove) {
            invalidate();
        }
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

    public CompoundTag save() {
        var tag = new CompoundTag();
        var listTag = new ListTag();

        for (var areaId : areaIds) {
            listTag.add(StringTag.valueOf(areaId.toString()));
        }

        tag.put("area_ids", listTag);

        return tag;
    }

    public void load(CompoundTag tag) {
        var listTag = tag.getList("area_ids").get();
        areaIds.clear();

        for (var areaIdTag : listTag) {
            var areaId = Identifier.parse(areaIdTag.asString().get());

            areaIds.add(areaId);
        }
    }

    @Override
    public String toString() {
        return "LazyAreaBVHTree{" +
            "areaIds=" + areaIds +
            '}';
    }
}
