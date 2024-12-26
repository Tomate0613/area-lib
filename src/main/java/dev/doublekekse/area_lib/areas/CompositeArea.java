package dev.doublekekse.area_lib.areas;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.AreaLib;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public abstract class CompositeArea implements Area {
    List<Area> areas;
    List<ResourceLocation> areaIds;

    ResourceLocation dimension;

    int priority = 0;

    public CompositeArea(ResourceLocation dimension, Area... areas) {
        this.dimension = dimension;
        this.areas = new ArrayList<>(List.of(areas));
    }

    public CompositeArea() {

    }

    @Override
    public void load(CompoundTag compoundTag) {
        var listTag = compoundTag.getList("areas", 8);

        areaIds = new ArrayList<>();
        listTag.forEach(tag -> areaIds.add(ResourceLocation.tryParse(tag.getAsString())));

        dimension = ResourceLocation.parse(compoundTag.getString("dimension"));

        priority = compoundTag.getInt("priority");
    }

    @Override
    public ResourceLocation getType() {
        return AreaLib.id("block");
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

        compoundTag.putString("dimension", dimension.toString());

        compoundTag.putInt("priority", priority);

        return compoundTag;
    }

    protected void loadAreas(Level level) {
        if (areas != null) {
            return;
        }

        areas = new ArrayList<>(areaIds.size());
        var savedData = AreaLib.getSavedData(level);

        for (var areaId : areaIds) {
            var area = savedData.get(areaId);
            areas.add(area);
        }
    }

    @Override
    public void render(WorldRenderContext context, PoseStack poseStack) {
    }

    @Override
    public void setColor(float r, float g, float b) {
    }

    public String toString() {
        return "CompositeArea " + areas + " priority: " + priority;
    }
}
