package dev.doublekekse.area_lib.data;

import com.mojang.serialization.*;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.AreaLib;
import dev.doublekekse.area_lib.AreaListeners;
import dev.doublekekse.area_lib.bvh.LazyAreaBVHTree;
import dev.doublekekse.area_lib.component.AreaDataComponentType;
import dev.doublekekse.area_lib.duck.EntityDuck;
import dev.doublekekse.area_lib.packet.ClientboundAreaSyncPacket;
import dev.doublekekse.area_lib.registry.AreaDataComponentTypeRegistry;
import dev.doublekekse.area_lib.registry.AreaTypeRegistry;
import dev.doublekekse.area_lib.areas.CompositeArea;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class AreaSavedData extends SavedData {
    private final Map<Identifier, Area> areas = new HashMap<>();

    private final LazyAreaBVHTree trackedAreas = new LazyAreaBVHTree(this);
    private final LazyAreaBVHTree[] samplingAreas = new LazyAreaBVHTree[AreaDataComponentTypeRegistry.samplingCount()];

    private boolean isInitialized = true;

    private AreaSavedData() {
        Arrays.setAll(samplingAreas, _ -> new LazyAreaBVHTree(this));
    }

    @ApiStatus.Internal
    public static final Codec<AreaSavedData> CODEC = CompoundTag.CODEC.xmap(
        AreaSavedData::load,
        AreaSavedData::save
    );

    private static final SavedDataType<AreaSavedData> type = new SavedDataType<>(AreaLib.id("areas"), AreaSavedData::new,
        AreaSavedData.CODEC,
        null);


    @ApiStatus.Internal
    public @NotNull CompoundTag save() {
        var compoundTag = new CompoundTag();

        areas.forEach((key, value) -> {
            var tag = new CompoundTag();

            tag.putString("type", value.getType().toString());
            tag.put("data", value.save());

            compoundTag.put(key.toString(), tag);
        });

        return compoundTag;
    }

    @ApiStatus.Internal
    public static AreaSavedData load(CompoundTag compoundTag) {
        var data = new AreaSavedData();
        data.isInitialized = false;

        for (var entry : compoundTag.entrySet()) {
            var tag = entry.getValue().asCompound().get();
            var id = Identifier.parse(entry.getKey());

            var area = AreaTypeRegistry.getArea(Identifier.parse(tag.getString("type").get()), data, id);
            area.load(tag.getCompound("data").get());

            data.areas.put(id, area);
        }

        data.isInitialized = true;
        AreaListeners.emitLoad(data);
        return data;
    }

    public Collection<Area> getAreas() {
        if (!isInitialized) {
            throw new IllegalStateException("Areas have not been initialized");
        }

        return areas.values();
    }

    public void put(MinecraftServer server, Area area) {
        var previous = areas.put(area.getId(), area);
        if (previous != null) {
            stopTracking(previous);
        }

        invalidate(server, area);
    }

    public Area get(Identifier id) {
        if (!isInitialized) {
            throw new IllegalStateException("Areas have not been initialized");
        }

        return areas.get(id);
    }

    public void remove(MinecraftServer server, Area area) {
        areas.remove(area.getId());
        stopTracking(area);
        invalidate(server, area);

        // Remove area from sub-area caches
        // This is definitely not an ideal way to deal with this, but it works
        for (var entry : areas.entrySet()) {
            if (entry.getValue() instanceof CompositeArea compositeArea) {
                compositeArea.removeSubArea(null, area);
            }
        }
    }

    public boolean has(Identifier id) {
        if (!isInitialized) {
            throw new IllegalStateException("Areas have not been initialized");
        }

        return areas.containsKey(id);
    }

    /**
     * Finds all areas containing the specified position.
     * Note: This method performs a linear search through all areas and might be slow.
     * For regular position checks, consider using {@link #getEntityTrackedAreas} instead.
     *
     * @param level the level to check in
     * @param pos   the position to check for
     * @return a collection of all areas containing the position
     */
    public Collection<Area> findAllAreasContaining(Level level, Vec3 pos) {
        if (!isInitialized) {
            throw new IllegalStateException("Areas have not been initialized");
        }

        return areas.values()
            .stream()
            .filter(area -> area.contains(level, pos))
            .toList();
    }

    /**
     * Finds any areas containing the specified position.
     * Note: This method performs a linear search through all areas and might be slow.
     * For regular position checks, consider using {@link #getEntityTrackedAreas} instead.
     *
     * @param level the level to check in
     * @param pos   the position to check for
     * @return area containing the position or null
     */
    public Area findAnyAreaContaining(Level level, Vec3 pos) {
        if (!isInitialized) {
            throw new IllegalStateException("Areas have not been initialized");
        }

        for (var area : areas.values()) {
            if (area.contains(level, pos)) {
                return area;
            }
        }

        return null;
    }

    /**
     * Returns all tracked areas containing the specified entity.
     * Return value is cached per entity per tick
     *
     * <p>
     * An area is considered tracked, if it has at least one entity tracked component
     * </p>
     *
     * @param entity the entity to check for
     * @return a list of all entity tracked areas containing the entity
     * @see AreaDataComponentTypeRegistry#registerEntityTracked(Identifier, Codec)
     */
    public Collection<Area> getEntityTrackedAreas(Entity entity) {
        return ((EntityDuck) entity).area_lib$getAreas(this);
    }

    /**
     * Checks if the given entity is currently inside any entity-tracked area
     * containing the specified component type.
     *
     * <p>
     * This is primarily intended for unit area components
     * </p>
     * <p>
     * If only checking irregularly using a {@code SAMPLED} component should be preferred
     * {@link #isInSampledAreaWith(AreaDataComponentType, Entity)}
     * </p>
     *
     * @param type   the tracked component type to look for
     * @param entity the entity to check
     * @return true if the entity is inside at least one entity-tracked area
     * containing the component, false otherwise
     */
    public boolean isInEntityTrackedAreaWith(AreaDataComponentType<?> type, Entity entity) {
        assert type.type() == AreaDataComponentType.Type.ENTITY_TRACKED;

        for (Area area : getEntityTrackedAreas(entity)) {
            if (area.has(type)) return true;
        }

        return false;
    }

    /**
     * Returns all sampled areas that include the given point with a matching component
     *
     * <p>
     * <strong>Important:</strong> The provided {@code type} must be of type
     * {@code SAMPLED}. Passing a {@code SIMPLE} or {@code ENTITY_TRACKED} component
     * will result in incorrect behavior.
     * </p>
     *
     * @param type  the component type
     * @param level the level to check in
     * @param pos   the position to check for
     * @return a list of all sampled areas with matching component containing the position
     * @see AreaDataComponentTypeRegistry#registerSampled(Identifier, Codec)
     * @see #getSampledAreas(AreaDataComponentType, Entity)
     */
    public List<Area> getSampledAreas(AreaDataComponentType<?> type, Level level, Vec3 pos) {
        assert type.type() == AreaDataComponentType.Type.SAMPLED;

        return samplingAreas[type.index()].findAreasContaining(level, pos);
    }

    /**
     * Returns all sampled areas that include the entities position
     *
     * <p>
     * <strong>Important:</strong> The provided {@code type} must be of type
     * {@code SAMPLED}. Passing a {@code SIMPLE} or {@code ENTITY_TRACKED} component
     * will result in incorrect behavior.
     * </p>
     *
     * <p>
     * If caching of areas might make sense, such as checking which areas the player is in every tick
     * (Which other mods using area lib might also do)
     * a {@code ENTITY_TRACKED} component might be preferable
     * </p>
     *
     * @param type   the component type
     * @param entity the entity
     * @return a list of all sampled areas with matching component containing the position of the entity
     * @see AreaDataComponentTypeRegistry#registerSampled(Identifier, Codec)
     * @see #getSampledAreas(AreaDataComponentType, Level, Vec3)
     */
    public List<Area> getSampledAreas(AreaDataComponentType<?> type, Entity entity) {
        assert type.type() == AreaDataComponentType.Type.SAMPLED;

        return samplingAreas[type.index()].findAreasContaining(entity.level(), entity.position());
    }

    /**
     * Checks if the given position is inside any sampled area
     * containing the specified component type.
     *
     * <p>
     * This is primarily intended for unit area components
     * </p>
     * <p>
     * <strong>Important:</strong> The provided {@code type} must be of type
     * {@code SAMPLED}. Passing a {@code SIMPLE} or {@code ENTITY_TRACKED} component
     * will result in incorrect behavior.
     * </p>
     *
     * @param type  the tracked component type to look for
     * @param level the level to check in
     * @param pos   the position to check for
     * @return true if the entity is inside at least one sampled area
     * containing the component, false otherwise
     */
    public boolean isInSampledAreaWith(AreaDataComponentType<?> type, Level level, Vec3 pos) {
        assert type.type() == AreaDataComponentType.Type.SAMPLED;

        return samplingAreas[type.index()].contains(level, pos);
    }

    /**
     * Checks if the given entity is currently inside any sampled area
     * containing the specified component type.
     *
     * <p>
     * This is primarily intended for unit area components
     * </p>
     * <p>
     * If checking regularly on an entity where caching makes sense
     * using a {@code ENTITY_TRACKED} component should be preferred
     * {@link #isInEntityTrackedAreaWith(AreaDataComponentType, Entity)}
     * </p>
     * <p>
     * <strong>Important:</strong> The provided {@code type} must be of type
     * {@code SAMPLED}. Passing a {@code SIMPLE} or {@code ENTITY_TRACKED} component
     * will result in incorrect behavior.
     * </p>
     *
     * @param type   the tracked component type to look for
     * @param entity the entity to check
     * @return true if the position is inside at least one sampled area
     * containing the component, false otherwise
     */
    public boolean isInSampledAreaWith(AreaDataComponentType<?> type, Entity entity) {
        assert type.type() == AreaDataComponentType.Type.SAMPLED;

        return samplingAreas[type.index()].contains(entity.level(), entity.position());
    }

    private void sync(MinecraftServer server) {
        server.getPlayerList().getPlayers().forEach(player -> ServerPlayNetworking.send(player, new ClientboundAreaSyncPacket(this)));
    }

    public static AreaSavedData getServerData(MinecraftServer server) {
        var storage = server.getDataStorage();
        var data = storage.computeIfAbsent(type);
        data.setDirty();

        return data;
    }

    /**
     * For internal usage.
     * Use {@link Area#invalidate(MinecraftServer)} instead
     */
    @ApiStatus.Internal
    public void invalidate(@Nullable MinecraftServer server, Area area) {
        AreaListeners.emit(this);

        setDirty();

        if (server != null) {
            sync(server);
        }
    }

    @ApiStatus.Internal
    public List<Area> getEntityTrackedAreas(Level level, Vec3 pos) {
        return trackedAreas.findAreasContaining(level, pos);
    }

    @ApiStatus.Experimental
    @ApiStatus.Internal
    public List<Area> getEntityTrackedAreas(Level level, Vec3 pos, Predicate<Area> predicate) {
        return trackedAreas.findAreasContaining(level, pos, predicate);
    }

    @ApiStatus.Internal
    public void startTracking(Area area) {
        trackedAreas.add(area.getId());
    }

    @ApiStatus.Internal
    public void stopTracking(Area area) {
        trackedAreas.remove(area.getId());
    }

    @ApiStatus.Internal
    public void startSampling(Area area, AreaDataComponentType<?> type) {
        samplingAreas[type.index()].add(area.getId());
    }

    @ApiStatus.Internal
    public void stopSampling(Area area, AreaDataComponentType<?> type) {
        samplingAreas[type.index()].remove(area.getId());
    }
}
