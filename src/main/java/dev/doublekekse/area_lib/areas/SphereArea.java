package dev.doublekekse.area_lib.areas;

import com.mojang.blaze3d.vertex.*;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.AreaLib;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SphereArea extends Area {
    Vec3 center;
    double radius;

    ResourceLocation dimension;

    public SphereArea(AreaSavedData savedData, ResourceLocation id, ResourceLocation dimension, Vec3 center, double radius) {
        super(savedData, id);

        this.center = center;
        this.radius = radius;
        this.dimension = dimension;
    }

    public SphereArea(AreaSavedData savedData, ResourceLocation id) {
        super(savedData, id);
    }

    @Override
    public void render(WorldRenderContext context, PoseStack poseStack) {
        if (!context.world().dimension().location().equals(dimension)) {
            return;
        }

        poseStack.pushPose();

        poseStack.translate(center.x, center.y, center.z);
        renderSphere(poseStack, context.consumers());


        poseStack.popPose();
    }

    // TODO: Replace this with better sphere rendering
    private void renderSphere(PoseStack poseStack, MultiBufferSource buffer) {
        var pose = poseStack.last();
        var matrix = pose.pose();
        var normal = pose.normal();

        var vertexConsumer = buffer.getBuffer(RenderType.lineStrip());

        int slices = 16;
        int stacks = 10;

        for (int i = 0; i <= stacks; i++) {
            float theta1 = (float) (Math.PI * i / stacks);
            float theta2 = (float) (Math.PI * (i + 1) / stacks);

            for (int j = 0; j <= slices; j++) {
                float phi = (float) (2 * Math.PI * j / slices);

                float x1 = (float) (radius * Math.sin(theta1) * Math.cos(phi));
                float y1 = (float) (radius * Math.cos(theta1));
                float z1 = (float) (radius * Math.sin(theta1) * Math.sin(phi));

                float x2 = (float) (radius * Math.sin(theta2) * Math.cos(phi));
                float y2 = (float) (radius * Math.cos(theta2));
                float z2 = (float) (radius * Math.sin(theta2) * Math.sin(phi));

                vertexConsumer.vertex(matrix, x1, y1, z1).color(r, g, b, 1).normal(normal, 0, 1, 0).endVertex();
                vertexConsumer.vertex(matrix, x2, y2, z2).color(r, g, b, 1).normal(normal, 1, 0, 0).endVertex();
            }
        }

        /*
        var vertexConsumer = buffer.getBuffer(RenderType.debugLineStrip(10000));

        ring(pose, vertexConsumer, (x, y) -> new Vector3f(x, y, 0), new Vector3f(0, 0, 1));
        ring(pose, vertexConsumer, (x, y) -> new Vector3f(x, 0, y), new Vector3f(0, 1, 0));
        ring(pose, vertexConsumer, (x, y) -> new Vector3f(0, y, x), new Vector3f(1, 0, 0));
         */
    }

    /*
    private void ring(PoseStack.Pose pose, VertexConsumer consumer, BiFunction<Float, Float, Vector3f> transform, Vector3f normal) {
        int segments = 32;

        for (int i = 0; i < segments; i++) {
            float theta1 = (float) (2 * Math.PI * i / segments);
            float theta2 = (float) (2 * Math.PI * (i + 1) / segments);

            float x1 = (float) (radius * Math.sin(theta1));
            float y1 = (float) (radius * Math.cos(theta1));

            float x2 = (float) (radius * Math.sin(theta2));
            float y2 = (float) (radius * Math.cos(theta2));

            var from = transform.apply(x1, y1);
            var to = transform.apply(x2, y2);

            consumer.addVertex(pose, from).setColor(r, g, b, 1).setNormal(normal.x, normal.y, normal.z);
            consumer.addVertex(pose, to).setColor(r, g, b, 1).setNormal(normal.x, normal.y, normal.z);
        }
    }
     */

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

        var x = compoundTag.getDouble("x");
        var y = compoundTag.getDouble("y");
        var z = compoundTag.getDouble("z");

        center = new Vec3(x, y, z);
        radius = compoundTag.getDouble("radius");

        dimension = ResourceLocation.tryParse(compoundTag.getString("dimension"));
    }

    @Override
    public ResourceLocation getType() {
        return AreaLib.id("sphere");
    }

    @Override
    public boolean contains(Level level, Vec3 position) {
        if (!Objects.equals(level.dimension().location(), dimension)) {
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
