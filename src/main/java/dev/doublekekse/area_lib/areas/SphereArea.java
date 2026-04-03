package dev.doublekekse.area_lib.areas;

import com.mojang.blaze3d.vertex.*;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.AreaLib;
import dev.doublekekse.area_lib.client.AreaLibClient;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_lib.gizmos.SphereGizmo;
import dev.doublekekse.area_lib.registry.BuiltInAreaComponents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SphereArea extends Area {
    Vec3 center;
    double radius;

    Identifier dimension;

    public SphereArea(AreaSavedData savedData, Identifier id, Identifier dimension, Vec3 center, double radius) {
        super(savedData, id);

        this.center = center;
        this.radius = radius;
        this.dimension = dimension;
    }

    public SphereArea(AreaSavedData savedData, Identifier id) {
        super(savedData, id);
    }

    @Override
    public void render(LevelRenderContext context, PoseStack poseStack, Identifier dim) {
        if (!dim.equals(dimension)) {
            return;
        }

        //var style = new GizmoStyle(0xffffffff, 2.5f, 0x223311AA);
        var style = getOrDefault(BuiltInAreaComponents.GIZMO_STYLE_COMPONENT, AreaLibClient.DEFAULT_GIZMO_STYLE);
        Gizmos.addGizmo(new SphereGizmo(center, radius, style));
    }

    @Override
    public CompoundTag save() {
        var tag = super.save();

        tag.putDouble("x", center.x);
        tag.putDouble("y", center.y);
        tag.putDouble("z", center.z);

        tag.putDouble("radius", radius);
        tag.putString("dimension", dimension.toString());

        return tag;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);

        var x = compoundTag.getDouble("x").orElse(0.0);
        var y = compoundTag.getDouble("y").orElse(0.0);
        var z = compoundTag.getDouble("z").orElse(0.0);

        center = new Vec3(x, y, z);
        radius = compoundTag.getDouble("radius").orElse(5.0);

        dimension = Identifier.parse(compoundTag.getString("dimension").get());
    }

    @Override
    public Identifier getType() {
        return AreaLib.id("sphere");
    }

    @Override
    public boolean contains(Level level, Vec3 position) {
        if (!Objects.equals(level.dimension().identifier(), dimension)) {
            return false;
        }

        return position.distanceToSqr(center) <= radius * radius;
    }

    @Override
    public @Nullable AABB getBoundingBox() {
        int r = (int) Math.ceil(radius);
        return new AABB(center.subtract(r, r, r), center.add(r, r, r));
    }
}
