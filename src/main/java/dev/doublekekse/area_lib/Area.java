package dev.doublekekse.area_lib;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.doublekekse.area_lib.bvh.BVHItem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public interface Area extends BVHItem {
    default boolean contains(Entity entity) {
        return contains(entity.level(), entity.position());
    }

    CompoundTag save();

    void load(CompoundTag compoundTag);

    void setColor(float r, float g, float b);

    void render(WorldRenderContext context, PoseStack poseStack);

    ResourceLocation getType();

    int getPriority();

    void setPriority(int priority);
}
