package dev.doublekekse.area_lib;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.doublekekse.area_lib.bvh.BVHItem;
import dev.doublekekse.area_lib.component.AreaDataComponent;
import dev.doublekekse.area_lib.component.AreaDataComponentType;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_lib.registry.AreaDataComponentTypeRegistry;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.Map;

public abstract class Area implements BVHItem {
    protected float r = 1;
    protected float g = 1;
    protected float b = 1;

    protected int priority = 0;
    private final Map<AreaDataComponentType<?>, AreaDataComponent> components = new Reference2ObjectArrayMap<>();

    @SuppressWarnings("unchecked")
    public <T extends AreaDataComponent> T get(AreaDataComponentType<T> type) {
        return (T) components.get(type);
    }

    @SuppressWarnings("unchecked")
    public <T extends AreaDataComponent> T getOrDefault(AreaDataComponentType<T> type, T defaultValue) {
        return (T) components.getOrDefault(type, defaultValue);
    }

    public <T extends AreaDataComponent> void put(AreaDataComponentType<T> type, T component) {
        components.put(type, component);
    }

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

        var componentsTag = new CompoundTag();
        for(var entry : components.entrySet()) {
            componentsTag.put(entry.getKey().id().toString(), entry.getValue().save());
        }
        compoundTag.put("components", componentsTag);

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

        var componentsTag = compoundTag.getCompound("components");
        for(var key : componentsTag.getAllKeys()) {
            var id = ResourceLocation.tryParse(key);
            var type = AreaDataComponentTypeRegistry.get(id);

            if(type == null) {
                continue;
            }

            var component = type.factory().get();
            component.load(savedData, componentsTag.getCompound(key));

            components.put(type, component);
        }
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
