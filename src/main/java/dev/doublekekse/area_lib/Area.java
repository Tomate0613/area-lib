package dev.doublekekse.area_lib;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.doublekekse.area_lib.client.AreaLibClient;
import dev.doublekekse.area_lib.component.AreaDataComponentType;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_lib.registry.AreaDataComponentTypeRegistry;
import dev.doublekekse.area_lib.registry.BuiltInAreaComponents;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class Area {
    protected int priority = 0;

    protected final AreaSavedData savedData;
    protected final Identifier id;

    private final Map<AreaDataComponentType<Object>, Object> components = new Reference2ObjectArrayMap<>();

    public Area(AreaSavedData savedData, Identifier id) {
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
    public <T> T get(AreaDataComponentType<T> type) {
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
    public <T> T getOrDefault(AreaDataComponentType<T> type, T defaultValue) {
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
    public <T> void put(@Nullable MinecraftServer server, AreaDataComponentType<T> type, T component) {
        //noinspection unchecked
        components.put((AreaDataComponentType<Object>) type, component);

        if (type.tracking()) {
            savedData.startTracking(this);
        }
        if (type.sampling()) {
            savedData.startSampling(this, type);
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
    public <T> T remove(@Nullable MinecraftServer server, AreaDataComponentType<T> type) {
        var component = (T) components.remove(type);
        invalidate(server);

        if (type.tracking() && !shouldBeTracked()) {
            savedData.stopTracking(this);
        }
        if (type.sampling() && !shouldSample(type)) {
            savedData.stopSampling(this, type);
        }

        return component;
    }

    /**
     * Adds all components from the other area to this one.
     * If a MinecraftServer is provided, the change is synchronized with the client.
     *
     * @param server the MinecraftServer instance for synchronization, or null if not needed
     * @param other  the area to copy the components from
     */
    public void copyComponentsFrom(@Nullable MinecraftServer server, Area other) {
        components.putAll(other.components);
        invalidate(server);

        if (shouldBeTracked()) {
            savedData.startTracking(this);
        } else {
            // I am relatively sure I can remove this else
            savedData.stopTracking(this);
        }

        for (var c : other.components.keySet()) {
            if (c.sampling()) {
                savedData.startSampling(this, c);
            }
        }
    }

    private boolean shouldBeTracked() {
        return components.keySet().stream().anyMatch(AreaDataComponentType::tracking);
    }

    private boolean shouldSample(AreaDataComponentType<?> type) {
        return components.keySet().stream().anyMatch(s -> s.equals(type));
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
     * Checks whether the given position in the specified level is contained within the item.
     *
     * @param level    the level (world) to check in
     * @param position the position to check
     * @return true if the position is within the item, false otherwise
     */
    public abstract boolean contains(Level level, Vec3 position);

    /**
     * Gets the bounding box of the item. The bounding box is used for
     * spatial partitioning and optimization in BVH structures.
     *
     * @return the bounding box as an {@link AABB}
     */
    @Nullable
    public abstract AABB getBoundingBox();

    /**
     * Saves the area's data to a {@link CompoundTag}.
     *
     * @return a {@link CompoundTag} containing the saved state of the area
     */
    public CompoundTag save() {
        var compoundTag = new CompoundTag();

        compoundTag.putInt("priority", priority);

        var componentsTag = new CompoundTag();
        for (var entry : components.entrySet()) {
            var r = entry.getKey().codec().encodeStart(NbtOps.INSTANCE, entry.getValue());
            componentsTag.put(entry.getKey().id().toString(), r.getOrThrow());
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
        priority = compoundTag.getInt("priority").orElse(0);

        var componentsTag = compoundTag.getCompound("components").orElseGet(CompoundTag::new);
        for (var entry : componentsTag.entrySet()) {
            var id = Identifier.tryParse(entry.getKey());
            var type = AreaDataComponentTypeRegistry.get(id);

            if (type == null) {
                continue;
            }

            if (type.tracking()) {
                savedData.startTracking(this);
            }

            if (type.sampling()) {
                savedData.startSampling(this, type);
            }

            var r = type.codec().parse(NbtOps.INSTANCE, entry.getValue());
            //noinspection unchecked
            components.put((AreaDataComponentType<Object>) type, r.getOrThrow());
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
    @Deprecated
    public final void setColor(@Nullable MinecraftServer server, float r, float g, float b) {
        var style = getOrDefault(BuiltInAreaComponents.GIZMO_STYLE_COMPONENT, AreaLibClient.DEFAULT_GIZMO_STYLE);
        var color = ARGB.color((int) r * 255, (int) g * 255, (int) b * 255);
        put(server, BuiltInAreaComponents.GIZMO_STYLE_COMPONENT, new GizmoStyle(color, style.strokeWidth(), style.fill()));
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
    @ApiStatus.Internal
    public abstract void render(LevelRenderContext context, PoseStack poseStack, Identifier dimension);

    public Identifier getId() {
        return id;
    }

    /**
     * Gets the unique type identifier of this type of area.
     *
     * @return the type as a {@link Identifier}
     */
    public abstract Identifier getType();

    @Override
    public String toString() {
        return id.toString();
    }
}
