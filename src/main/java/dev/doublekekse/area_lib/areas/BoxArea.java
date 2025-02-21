package dev.doublekekse.area_lib.areas;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.AreaLib;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_lib.util.CompoundUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class BoxArea extends Area {
    AABB aabb;
    ResourceLocation dimension;

    public BoxArea(AreaSavedData savedData, ResourceLocation id, ResourceLocation dimension, AABB aabb) {
        super(savedData, id);

        this.dimension = dimension;
        this.aabb = aabb;
    }

    public BoxArea(AreaSavedData savedData, ResourceLocation id) {
        super(savedData, id);
    }

    @Override
    public void load(AreaSavedData savedData, CompoundTag compoundTag) {
        super.load(savedData, compoundTag);

        aabb = CompoundUtils.toAABB(compoundTag.getCompound("aabb"));
        dimension = ResourceLocation.parse(compoundTag.getString("dimension"));
    }

    @Override
    public ResourceLocation getType() {
        return AreaLib.id("box");
    }

    @Override
    public CompoundTag save() {
        var compoundTag = super.save();

        compoundTag.put("aabb", CompoundUtils.fromAABB(aabb));
        compoundTag.putString("dimension", dimension.toString());

        return compoundTag;
    }

    @Override
    public boolean contains(Level level, Vec3 position) {
        if (!Objects.equals(level.dimension().location(), dimension)) {
            return false;
        }

        return aabb.contains(position);
    }

    @Override
    public AABB getBoundingBox() {
        return aabb;
    }

    @Override
    public void render(WorldRenderContext context, PoseStack poseStack) {
        if (!context.world().dimension().location().equals(dimension)) {
            return;
        }

        LevelRenderer.renderLineBox(poseStack, context.consumers().getBuffer(RenderType.lines()), aabb, r, g, b, 1);
    }

    public String toString() {
        return "BoxArea " + aabb + " priority: " + priority;
    }
}
