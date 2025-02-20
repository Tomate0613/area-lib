package dev.doublekekse.area_lib;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.doublekekse.area_lib.bvh.BVHItem;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public abstract class Area implements BVHItem {
    protected float r = 1;
    protected float g = 1;
    protected float b = 1;

    protected int priority = 0;

    /**
     * Checks whether the given entity is within the area.
     *
     * @param entity the entity to check
     * @return true if the entity is inside the area, false otherwise
     */
    public boolean contains(Entity entity) {
        return contains(entity.level(), entity.position());
    }

    /**
     * Saves the area's data to a {@link CompoundTag}.
     *
     * @return a {@link CompoundTag} containing the saved state of the area
     */
    public CompoundTag save() {
        var compoundTag = new CompoundTag();

        compoundTag.putFloat("r", r);
        compoundTag.putFloat("g", g);
        compoundTag.putFloat("b", b);

        compoundTag.putInt("priority", priority);

        return compoundTag;
    }

    /**
     * Loads the area's data from a {@link CompoundTag}.
     *
     * @param savedData   the area saved data. Areas are not loaded yet
     * @param compoundTag the tag containing saved area data
     */
    public void load(AreaSavedData savedData, CompoundTag compoundTag) {
        r = compoundTag.getFloat("r");
        g = compoundTag.getFloat("g");
        b = compoundTag.getFloat("b");

        priority = compoundTag.getInt("priority");
    }

    /**
     * Sets the color used to render this area.
     *
     * @param r the red component (0.0 - 1.0)
     * @param g the green component (0.0 - 1.0)
     * @param b the blue component (0.0 - 1.0)
     */
    public final void setColor(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    /**
     * Gets the priority of the area. Priority can be used to determine rendering order
     * or processing importance.
     *
     * @return the priority value
     */
    public final int getPriority() {
        return priority;
    }

    /**
     * Sets the priority of the area.
     *
     * @param priority the new priority value
     */
    public final void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Renders this area in the world.
     *
     * @param context   the world render context
     * @param poseStack the pose stack used for transformations
     */
    public abstract void render(WorldRenderContext context, PoseStack poseStack);

    /**
     * Gets the unique type identifier of this type of area.
     *
     * @return the type as a {@link ResourceLocation}
     */
    public abstract ResourceLocation getType();

}
