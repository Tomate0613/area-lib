package dev.doublekekse.area_lib;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface Area {
    boolean contains(Level level, Vec3 position);

    default boolean contains(Entity entity) {
        return contains(entity.level(), entity.position());
    }

    CompoundTag save();

    void load(CompoundTag compoundTag);

    void setColor(float r, float g, float b);

    void render(WorldRenderContext context, PoseStack poseStack);

    ResourceLocation getType();
}
