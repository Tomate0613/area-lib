package dev.doublekekse.area_lib.areas;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.AreaLib;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlockArea implements Area {
    public List<AABB> aabbs;
    public ResourceLocation dimension;

    float r = 1;
    float g = 1;
    float b = 1;

    int priority = 0;

    public BlockArea(ResourceLocation dimension, AABB... aabb) {
        this.dimension = dimension;
        aabbs = new ArrayList<>(List.of(aabb));
    }

    public BlockArea() {

    }

    @Override
    public void load(CompoundTag compoundTag) {
        var listTag = compoundTag.getList("aabbs", 10);

        aabbs = new ArrayList<>();
        listTag.forEach(tag -> aabbs.add(CompoundUtils.toAABB((CompoundTag) tag)));

        r = compoundTag.getFloat("r");
        g = compoundTag.getFloat("g");
        b = compoundTag.getFloat("b");

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
        aabbs.forEach(aabb -> listTag.add(CompoundUtils.fromAABB(aabb)));

        compoundTag.put("aabbs", listTag);

        compoundTag.putFloat("r", r);
        compoundTag.putFloat("g", g);
        compoundTag.putFloat("b", b);

        compoundTag.putString("dimension", dimension.toString());

        compoundTag.putInt("priority", priority);

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
    public void render(WorldRenderContext context, PoseStack poseStack) {
        if (!context.world().dimension().location().equals(dimension)) {
            return;
        }

        aabbs.forEach(aabb -> {
            LevelRenderer.renderLineBox(poseStack, context.consumers().getBuffer(RenderType.lines()), aabb, r, g, b, 1);
        });
    }

    @Override
    public void setColor(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public String toString() {
        return "BlockArea " + aabbs + " priority: " + priority;
    }
}
