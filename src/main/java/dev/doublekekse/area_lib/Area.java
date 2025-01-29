package dev.doublekekse.area_lib;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.doublekekse.area_lib.bvh.BVHItem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public interface Area extends BVHItem {
    /**
     * Checks whether the given entity is within the area.
     *
     * @param entity the entity to check
     * @return true if the entity is inside the area, false otherwise
     */
    default boolean contains(Entity entity) {
        return contains(entity.level(), entity.position());
    }

    /**
     * Saves the area's data to a {@link CompoundTag}.
     *
     * @return a {@link CompoundTag} containing the saved state of the area
     */
    CompoundTag save();

    /**
     * Loads the area's data from a {@link CompoundTag}.
     *
     * @param compoundTag the tag containing saved area data
     */
    void load(CompoundTag compoundTag);

    /**
     * Sets the color used to render this area.
     *
     * @param r the red component (0.0 - 1.0)
     * @param g the green component (0.0 - 1.0)
     * @param b the blue component (0.0 - 1.0)
     */
    void setColor(float r, float g, float b);

    /**
     * Renders this area in the world.
     *
     * @param context   the world render context
     * @param poseStack the pose stack used for transformations
     */
    void render(WorldRenderContext context, PoseStack poseStack);

    /**
     * Gets the unique type identifier of this type of area.
     *
     * @return the type as a {@link ResourceLocation}
     */
    ResourceLocation getType();

    /**
     * Gets the priority of the area. Priority can be used to determine rendering order
     * or processing importance.
     *
     * @return the priority value
     */
    int getPriority();

    /**
     * Sets the priority of the area.
     *
     * @param priority the new priority value
     */
    void setPriority(int priority);
}
