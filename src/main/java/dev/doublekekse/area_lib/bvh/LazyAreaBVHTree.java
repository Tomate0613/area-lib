package dev.doublekekse.area_lib.bvh;

import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LazyAreaBVHTree {
    private @Nullable BVHNode<Area> node;
    private final List<ResourceLocation> areaIds = new ArrayList<>();
    private final AreaSavedData savedData;

    public LazyAreaBVHTree(AreaSavedData savedData) {
        this.savedData = savedData;
    }

    public LazyAreaBVHTree(AreaSavedData savedData, List<ResourceLocation> areaIds) {
        this.savedData = savedData;
        this.areaIds.addAll(areaIds);
    }

    public void add(ResourceLocation areaId) {
        areaIds.add(areaId);

        if (node == null) {
            return;
        }

        var area = savedData.get(areaId);
        node = node.with(area);
    }

    public void remove(ResourceLocation areaId) {
        areaIds.remove(areaId);

        if (node == null) {
            return;
        }

        var area = savedData.get(areaId);
        node = node.without(area);
    }

    private void build() {
        var areas = areaIds.stream().map(savedData::get).filter(Objects::nonNull).toList();
        node = new BVHNode<>(areas);
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

    public List<Area> listAllAreas() {
        if (areaIds.isEmpty()) {
            return Collections.emptyList();
        }
        if (node == null) {
            build();
        }

        return node.listAllAreas();
    }

    public List<ResourceLocation> getAreaIds() {
        return areaIds;
    }

    public AABB getBoundingBox() {
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
        var listTag = tag.getList("area_ids", Tag.TAG_STRING);
        areaIds.clear();

        for (var areaIdTag : listTag) {
            var areaId = ResourceLocation.parse(areaIdTag.getAsString());

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
