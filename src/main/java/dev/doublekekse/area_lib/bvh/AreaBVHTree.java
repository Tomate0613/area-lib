package dev.doublekekse.area_lib.bvh;

import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class AreaBVHTree {
    private BVHNode<Area> node;
    private final List<ResourceLocation> areaIds = new ArrayList<>();
    private final AreaSavedData savedData;

    public AreaBVHTree(AreaSavedData savedData) {
        this.savedData = savedData;
    }

    public void add(ResourceLocation areaId) {
        var area = savedData.get(areaId);
        areaIds.add(areaId);

        if (node == null) {
            node = new BVHNode<>(savedData, Collections.singletonList(area));
        }

        node = node.with(savedData, area);
    }

    public void remove(ResourceLocation areaId) {
        var area = savedData.get(areaId);
        areaIds.remove(areaId);

        if (node == null) {
            return;
        }

        node = node.without(savedData, area);
    }

    public boolean contains(Level level, Vec3 position) {
        if (node == null) {
            return false;
        }

        return node.contains(level, position);
    }

    public List<Area> findAreasContaining(Level level, Vec3 position) {
        if (node == null) {
            return Collections.emptyList();
        }

        return node.findAreasContaining(level, position);
    }

    public List<Area> listAllAreas() {
        if (node == null) {
            return Collections.emptyList();
        }

        return node.listAllAreas();
    }

    public List<ResourceLocation> getAreaIds() {
        return areaIds;
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

        var areas = areaIds.stream().map(savedData::get).filter(Objects::nonNull).toList();
        node = new BVHNode<>(savedData, areas);
    }
}
