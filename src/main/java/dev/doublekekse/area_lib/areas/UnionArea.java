package dev.doublekekse.area_lib.areas;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.AreaLib;
import dev.doublekekse.area_lib.bvh.LazyAreaBVHTree;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class UnionArea extends Area implements CompositeArea {
    protected LazyAreaBVHTree areas;

    public UnionArea(AreaSavedData savedData, Identifier id, LazyAreaBVHTree areas) {
        super(savedData, id);

        this.areas = areas;
    }

    public UnionArea(AreaSavedData savedData, Identifier id) {
        super(savedData, id);
    }

    @Override
    public boolean contains(Level level, Vec3 position) {
        return areas.contains(level, position);
    }

    @Override
    public Identifier getType() {
        return AreaLib.id("union");
    }

    @Override
    public void addSubArea(@Nullable MinecraftServer server, Area area) {
        if (area instanceof CompositeArea) {
            throw new IllegalArgumentException("Sub areas may not be composite areas");
        }

        areas.add(area.getId());
        invalidate(server);
    }

    @Override
    public void removeSubArea(@Nullable MinecraftServer server, Area area) {
        areas.remove(area.getId());
        invalidate(server);
    }

    @Override
    public boolean hasSubArea(Area area) {
        return areas.listAllAreas().contains(area);
    }

    @Override
    public void render(LevelRenderContext context, PoseStack poseStack, Identifier dimension) {

    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);

        areas = new LazyAreaBVHTree(savedData);
        areas.load(compoundTag.getCompound("areas").get());
    }

    @Override
    public CompoundTag save() {
        var compoundTag = super.save();

        compoundTag.put("areas", areas.save());

        return compoundTag;
    }

    @Override
    public @Nullable AABB getBoundingBox() {
        return areas.getBoundingBox();
    }
}
