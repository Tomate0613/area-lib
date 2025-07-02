package dev.doublekekse.area_lib.data;

import com.mojang.serialization.*;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.bvh.LazyAreaBVHTree;
import dev.doublekekse.area_lib.packet.ClientboundAreaSyncPacket;
import dev.doublekekse.area_lib.registry.AreaTypeRegistry;
import dev.doublekekse.area_lib.areas.CompositeArea;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class AreaSavedData extends SavedData {
    private final List<Consumer<Area>> changeListeners = new ArrayList<>();
    private final Map<ResourceLocation, Area> areas = new HashMap<>();
    private final LazyAreaBVHTree trackedAreas = new LazyAreaBVHTree(this);
    private boolean isInitialized = true;


    public static final Codec<AreaSavedData> CODEC = CompoundTag.CODEC.xmap(
        AreaSavedData::load,
        AreaSavedData::save
    );

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

    public static AreaSavedData load(CompoundTag compoundTag) {
        var data = new AreaSavedData();
        data.isInitialized = false;

        for (var entry : compoundTag.entrySet()) {
            var tag = entry.getValue().asCompound().get();
            var id = ResourceLocation.parse(entry.getKey());

            var area = AreaTypeRegistry.getArea(ResourceLocation.parse(tag.getString("type").get()), data, id);
            area.load(tag.getCompound("data").get());

            data.areas.put(id, area);
        }

        data.isInitialized = true;
        return data;
    }

    public Collection<Area> getAreas() {
        if (!isInitialized) {
            throw new IllegalStateException("Areas have not been initialized");
        }

        return areas.values();
    }

    @Deprecated
    public Set<Map.Entry<ResourceLocation, Area>> getAreaEntries() {
        if (!isInitialized) {
            throw new IllegalStateException("Areas have not been initialized");
        }

        return areas.entrySet();
    }

    public void put(MinecraftServer server, Area area) {
        var previous = areas.put(area.getId(), area);
        if (previous != null) {
            stopTracking(previous);
        }

        invalidate(server, area);
    }

    public Area get(ResourceLocation id) {
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

    public boolean has(ResourceLocation id) {
        if (!isInitialized) {
            throw new IllegalStateException("Areas have not been initialized");
        }

        return areas.containsKey(id);
    }

    /**
     * Finds all areas containing the specified position.
     * Note: This method performs a linear search through all areas and might be slow.
     * For regular position checks, consider using {@link #findTrackedAreasContaining} instead.
     *
     * @param level the level to check in
     * @param pos   the position to check for
     * @return a list of all areas containing the position
     */
    public Area findAllAreasContaining(Level level, Vec3 pos) {
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

    public void invalidate(@Nullable MinecraftServer server, Area area) {
        for (var changeListener : changeListeners) {
            changeListener.accept(area);
        }

        setDirty();

        if (server != null) {
            sync(server);
        }
    }

    private void sync(MinecraftServer server) {
        server.getPlayerList().getPlayers().forEach(player -> ServerPlayNetworking.send(player, new ClientboundAreaSyncPacket(this)));
    }

    public void addChangeListener(Consumer<Area> listener) {
        changeListeners.add(listener);
    }

    public static AreaSavedData getServerData(MinecraftServer server) {
        DimensionDataStorage persistentStateManager = server.overworld().getDataStorage();
        AreaSavedData data = persistentStateManager.computeIfAbsent(type);
        data.setDirty();

        return data;
    }

    private static final SavedDataType<AreaSavedData> type = new SavedDataType<>("areas", AreaSavedData::new,
        AreaSavedData.CODEC,
        null);

    /**
     * Finds all tracked areas containing the specified position.
     *
     * @param level the level to check in
     * @param pos   the position to check for
     * @return a list of all tracked areas containing the position
     */
    public List<Area> findTrackedAreasContaining(Level level, Vec3 pos) {
        return trackedAreas.findAreasContaining(level, pos);
    }

    /**
     * Finds all tracked areas containing the specified entity.
     *
     * @param entity the entity to check for
     * @return a list of all tracked areas containing the entity
     */
    public List<Area> findTrackedAreasContaining(Entity entity) {
        return findTrackedAreasContaining(entity.level(), entity.position());
    }

    @ApiStatus.Internal
    public void startTracking(Area area) {
        trackedAreas.add(area.getId());
    }

    @ApiStatus.Internal
    public void stopTracking(Area area) {
        trackedAreas.remove(area.getId());
    }
}
