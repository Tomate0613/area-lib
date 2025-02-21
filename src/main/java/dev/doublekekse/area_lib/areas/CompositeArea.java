package dev.doublekekse.area_lib.areas;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.bvh.LazyAreaBVHTree;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class CompositeArea extends Area {
    protected LazyAreaBVHTree areas;

    int priority = 0;

    public CompositeArea(AreaSavedData savedData, ResourceLocation id, LazyAreaBVHTree areas) {
        super(savedData, id);

        this.areas = areas;
    }

    public CompositeArea(AreaSavedData savedData, ResourceLocation id) {
        super(savedData, id);
    }

    public void addSubArea(Map.Entry<ResourceLocation, ? extends Area> area) {
        if (area.getValue() instanceof CompositeArea) {
            throw new IllegalArgumentException("Sub areas may not be composite areas");
        }

        areas.add(area.getKey());
    }

    public void removeSubArea(ResourceLocation areaId) {
        areas.remove(areaId);
    }

    @Override
    public void load(AreaSavedData savedData, CompoundTag compoundTag) {
        super.load(savedData, compoundTag);

        areas = new LazyAreaBVHTree(savedData);
        areas.load(compoundTag.getCompound("areas"));

        priority = compoundTag.getInt("priority");
    }

    @Override
    public CompoundTag save() {
        var compoundTag = super.save();

        compoundTag.put("areas", areas.save());
        compoundTag.putInt("priority", priority);

        return compoundTag;
    }

    @Override
    public @Nullable AABB getBoundingBox() {
        return areas.getBoundingBox();
    }

    @Override
    public void render(WorldRenderContext context, PoseStack poseStack) {
    }

    public String toString() {
        return "CompositeArea " + areas + " priority: " + priority;
    }
}
