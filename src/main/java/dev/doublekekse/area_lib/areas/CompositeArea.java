package dev.doublekekse.area_lib.areas;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.bvh.LazyAreaBVHTree;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

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

    public void addSubArea(@Nullable MinecraftServer server, Area area) {
        if (area instanceof CompositeArea) {
            throw new IllegalArgumentException("Sub areas may not be composite areas");
        }

        areas.add(area.getId());
        invalidate(server);
    }

    public void removeSubArea(@Nullable MinecraftServer server, Area area) {
        areas.remove(area.getId());
        invalidate(server);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);

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
