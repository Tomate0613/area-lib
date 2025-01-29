package dev.doublekekse.area_lib.areas;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public abstract class CompositeArea implements Area {
    Set<ResourceLocation> areaIds;

    private Collection<Area> cachedAreas;

    int priority = 0;

    public CompositeArea(Set<ResourceLocation> areaIds) {
        this.areaIds = areaIds;
    }

    public CompositeArea() {

    }

    public void addSubArea(Map.Entry<ResourceLocation, ? extends Area> area) {
        areaIds.add(area.getKey());

        if (cachedAreas == null) {
            return;
        }

        cachedAreas.add(area.getValue());
    }

    public void removeSubArea(Map.Entry<ResourceLocation, ? extends Area> area) {
        areaIds.remove(area.getKey());

        if (cachedAreas == null) {
            return;
        }

        cachedAreas.remove(area.getValue());
    }

    @Override
    public void load(CompoundTag compoundTag) {
        var listTag = compoundTag.getList("areas", 8);

        areaIds = new HashSet<>();
        listTag.forEach(tag -> areaIds.add(ResourceLocation.tryParse(tag.getAsString())));

        priority = compoundTag.getInt("priority");
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public CompoundTag save() {
        var compoundTag = new CompoundTag();

        var listTag = new ListTag();
        areaIds.forEach(areaId -> listTag.add(StringTag.valueOf(areaId.toString())));

        compoundTag.put("areas", listTag);

        compoundTag.putInt("priority", priority);

        return compoundTag;
    }

    protected Collection<Area> getAreas(AreaSavedData savedData) {
        if (cachedAreas != null) {
            return cachedAreas;
        }

        cachedAreas = new HashSet<>(areaIds.size());

        var iterator = areaIds.iterator();
        while (iterator.hasNext()) {
            var areaId = iterator.next();
            if (savedData.has(areaId)) {
                var area = savedData.get(areaId);
                cachedAreas.add(area);
            } else {
                iterator.remove();
            }
        }

        return cachedAreas;
    }

    @Override
    public void render(WorldRenderContext context, PoseStack poseStack) {
    }

    @Override
    public void setColor(float r, float g, float b) {
    }

    public String toString() {
        return "CompositeArea " + cachedAreas + " priority: " + priority;
    }
}
