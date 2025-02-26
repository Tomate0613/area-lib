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
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class Area implements BVHItem {
    protected float r = 1;
    protected float g = 1;
    protected float b = 1;

    protected int priority = 0;

    protected final AreaSavedData savedData;
    protected final ResourceLocation id;

    private final Map<AreaDataComponentType<?>, AreaDataComponent> components = new Reference2ObjectArrayMap<>();

    public Area(AreaSavedData savedData, ResourceLocation id) {
        this.savedData = savedData;
        this.id = id;
    }

    /**
     * Retrieves a component of the specified type from this area.
     *
     * @param type the type of component to retrieve
     * @param <T>  the component type
     * @return the component if present, otherwise null
     */
    @SuppressWarnings("unchecked")
    public <T extends AreaDataComponent> T get(AreaDataComponentType<T> type) {
        return (T) components.get(type);
    }

    /**
     * Checks whether this area has a component of the specified type.
     *
     * @param type the type of component to check for
     * @return true if the component is present, false otherwise
     */
    public boolean has(AreaDataComponentType<?> type) {
        return components.containsKey(type);
    }

    /**
     * Retrieves a component of the specified type, or returns the default value if not present.
     *
     * @param type         the type of component to retrieve
     * @param defaultValue the default value to return if the component is missing
     * @param <T>          the component type
     * @return the component if present, otherwise the default value
     */
    @SuppressWarnings("unchecked")
    public <T extends AreaDataComponent> T getOrDefault(AreaDataComponentType<T> type, T defaultValue) {
        return (T) components.getOrDefault(type, defaultValue);
    }

    /**
     * Adds or updates a component in this area. If a MinecraftServer is provided,
     * the change is synchronized with the client.
     *
     * @param server    the MinecraftServer instance for synchronization, or null if not needed
     * @param type      the type of component being added
     * @param component the component instance to store
     * @param <T>       the component type
     */
    public <T extends AreaDataComponent> void put(@Nullable MinecraftServer server, AreaDataComponentType<T> type, T component) {
        components.put(type, component);

        if(type.tracking()) {
            savedData.startTracking(this);
        }

        invalidate(server);
    }

    /**
     * Removes a component from this area. If a MinecraftServer is provided,
     * the change is synchronized with the client.
     *
     * @param server the MinecraftServer instance for synchronization, or null if not needed
     * @param type   the type of component to remove
     * @param <T>    the component type
     * @return the removed component if present, otherwise null
     */
    @SuppressWarnings("unchecked")
    public <T extends AreaDataComponent> T remove(@Nullable MinecraftServer server, AreaDataComponentType<T> type) {
        var component = (T) components.remove(type);
        invalidate(server);

        if (type.tracking() && !shouldBeTracked()) {
            savedData.stopTracking(this);
        }

        return component;
    }

    private boolean shouldBeTracked() {
        return components.keySet().stream().anyMatch(AreaDataComponentType::tracking);
    }

    /**
     * Invalidates this area's data, marking it for reprocessing and notifying change listeners.
     * If a MinecraftServer is provided, the change is synchronized with the client.
     *
     * @param server the MinecraftServer instance for client synchronization, or null if not needed
     */
    public void invalidate(@Nullable MinecraftServer server) {
        savedData.invalidate(server, this);
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
        for (var entry : components.entrySet()) {
            componentsTag.put(entry.getKey().id().toString(), entry.getValue().save());
        }
        compoundTag.put("components", componentsTag);

        return compoundTag;
    }

    /**
     * Loads the area's data from a {@link CompoundTag}.
     *
     * @param compoundTag the tag containing saved area data
     */
    public void load(CompoundTag compoundTag) {
        r = compoundTag.getFloat("r");
        g = compoundTag.getFloat("g");
        b = compoundTag.getFloat("b");

        priority = compoundTag.getInt("priority");

        var componentsTag = compoundTag.getCompound("components");
        for (var key : componentsTag.getAllKeys()) {
            var id = ResourceLocation.tryParse(key);
            var type = AreaDataComponentTypeRegistry.get(id);

            if (type == null) {
                continue;
            }

            if(type.tracking()) {
                savedData.startTracking(this);
            }

            var component = type.factory().get();
            component.load(savedData, componentsTag.getCompound(key));

            components.put(type, component);
        }
    }

    /**
     * Sets the color used to render this area. If a MinecraftServer is provided,
     * the change is synchronized with the client.
     *
     * @param server the MinecraftServer instance for client synchronization, or null if not needed
     * @param r      the red component (0.0 - 1.0)
     * @param g      the green component (0.0 - 1.0)
     * @param b      the blue component (0.0 - 1.0)
     */
    public final void setColor(@Nullable MinecraftServer server, float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;

        invalidate(server);
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
     * Sets the priority of the area. If a MinecraftServer is provided,
     * the change is synchronized with the client.
     *
     * @param server   the MinecraftServer instance for client synchronization, or null if not needed
     * @param priority the new priority value
     */
    public final void setPriority(@Nullable MinecraftServer server, int priority) {
        this.priority = priority;
        invalidate(server);
    }

    /**
     * Renders this area in the world.
     *
     * @param context   the world render context
     * @param poseStack the pose stack used for transformations
     */
    public abstract void render(WorldRenderContext context, PoseStack poseStack);

    public ResourceLocation getId() {
        return id;
    }

    /**
     * Gets the unique type identifier of this type of area.
     *
     * @return the type as a {@link ResourceLocation}
     */
    public abstract ResourceLocation getType();

}
