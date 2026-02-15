package dev.doublekekse.area_lib.areas;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.AreaLib;
import dev.doublekekse.area_lib.component.GizmoStyleComponent;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_lib.registry.BuiltInAreaComponents;
import dev.doublekekse.area_lib.util.CompoundUtils;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class BoxArea extends Area {
    AABB aabb;
    Identifier dimension;

    public BoxArea(AreaSavedData savedData, Identifier id, Identifier dimension, AABB aabb) {
        super(savedData, id);

        this.dimension = dimension;
        this.aabb = aabb;
    }

    public BoxArea(AreaSavedData savedData, Identifier id) {
        super(savedData, id);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);

        aabb = compoundTag.getCompound("aabb").map(CompoundUtils::toAABB).orElseGet(() -> new AABB(BlockPos.ZERO));
        dimension = compoundTag.getString("dimension").map(Identifier::parse).orElse(null);
    }

    @Override
    public Identifier getType() {
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
        if (!Objects.equals(level.dimension().identifier(), dimension)) {
            return false;
        }

        return aabb.contains(position);
    }

    @Override
    public AABB getBoundingBox() {
        return aabb;
    }

    @Override
    public void render(WorldRenderContext context, PoseStack poseStack, Identifier dim) {
        if (!dim.equals(dimension)) {
            return;
        }

        var style = getOrDefault(BuiltInAreaComponents.GIZMO_STYLE_COMPONENT, GizmoStyleComponent.DEFAULT).style;
        Gizmos.cuboid(aabb, style);
    }
}
