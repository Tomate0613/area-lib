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
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Deprecated
public class BlockArea extends Area {
    public List<AABB> aabbs;
    public ResourceLocation dimension;

    public BlockArea(ResourceLocation dimension, AABB... aabb) {
        this.dimension = dimension;
        aabbs = new ArrayList<>(List.of(aabb));
    }

    public BlockArea() {

    }

    @Override
    public void load(AreaSavedData savedData, CompoundTag compoundTag) {
        super.load(savedData, compoundTag);

        var listTag = compoundTag.getList("aabbs", 10);

        aabbs = new ArrayList<>();
        listTag.forEach(tag -> aabbs.add(CompoundUtils.toAABB((CompoundTag) tag)));

        dimension = ResourceLocation.parse(compoundTag.getString("dimension"));
    }

    @Override
    public ResourceLocation getType() {
        return AreaLib.id("block");
    }

    @Override
    public CompoundTag save() {
        var compoundTag = super.save();

        var listTag = new ListTag();
        aabbs.forEach(aabb -> listTag.add(CompoundUtils.fromAABB(aabb)));

        compoundTag.put("aabbs", listTag);
        compoundTag.putString("dimension", dimension.toString());

        return compoundTag;
    }

    @Override
    public boolean contains(Level level, Vec3 position) {
        if (!Objects.equals(level.dimension().location(), dimension)) {
            return false;
        }

        for (var aabb : aabbs) {
            if (aabb.contains(position)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public @Nullable AABB getBoundingBox() {
        return null;
    }

    @Override
    public void render(WorldRenderContext context, PoseStack poseStack) {
        if (!context.world().dimension().location().equals(dimension)) {
            return;
        }

        aabbs.forEach(aabb -> {
            LevelRenderer.renderLineBox(poseStack, context.consumers().getBuffer(RenderType.lines()), aabb, r, g, b, 1);
        });
    }

    public String toString() {
        return "BlockArea " + aabbs + " priority: " + priority;
    }
}
