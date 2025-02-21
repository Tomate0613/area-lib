package dev.doublekekse.area_lib.data;

import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.packet.ClientboundAreaSyncPacket;
import dev.doublekekse.area_lib.registry.AreaTypeRegistry;
import dev.doublekekse.area_lib.areas.CompositeArea;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class AreaSavedData extends SavedData {
    private final List<Consumer<Area>> changeListeners = new ArrayList<>();
    private final Map<ResourceLocation, Area> areas = new HashMap<>();
    private boolean isInitialized = true;

    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        areas.forEach((key, value) -> {
            var tag = new CompoundTag();

            tag.putString("type", value.getType().toString());
            tag.put("data", value.save());

            compoundTag.put(key.toString(), tag);
        });

        return compoundTag;
    }

    public static AreaSavedData load(CompoundTag compoundTag, HolderLookup.Provider provider) {
        var data = new AreaSavedData();
        data.isInitialized = false;

        for (String key : compoundTag.getAllKeys()) {
            var tag = compoundTag.getCompound(key);
            var id = ResourceLocation.parse(key);

            var area = AreaTypeRegistry.getArea(ResourceLocation.parse(tag.getString("type")), data, id);
            area.load(tag.getCompound("data"));

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

    public Set<Map.Entry<ResourceLocation, Area>> getAreaEntries() {
        if (!isInitialized) {
            throw new IllegalStateException("Areas have not been initialized");
        }

        return areas.entrySet();
    }

    public void put(MinecraftServer server, Area area) {
        areas.put(area.getId(), area);

        invalidate(server, area);
    }

    public Area get(ResourceLocation id) {
        if (!isInitialized) {
            throw new IllegalStateException("Areas have not been initialized");
        }

        return areas.get(id);
    }

    public Area remove(MinecraftServer server, ResourceLocation id) {
        var removedArea = areas.remove(id);

        invalidate(server, removedArea);

        // Remove area from sub-area caches
        // This is definitely not an ideal way to deal with this, but it works
        for (var entry : areas.entrySet()) {
            if (entry.getValue() instanceof CompositeArea compositeArea) {
                compositeArea.removeSubArea(id);
            }
        }

        return removedArea;
    }

    public boolean has(ResourceLocation id) {
        if (!isInitialized) {
            throw new IllegalStateException("Areas have not been initialized");
        }

        return areas.containsKey(id);
    }

    public Map.Entry<ResourceLocation, Area> find(Level level, Vec3 pos) {
        if (!isInitialized) {
            throw new IllegalStateException("Areas have not been initialized");
        }

        for (var entry : areas.entrySet()) {
            if (entry.getValue().contains(level, pos)) {
                return entry;
            }
        }

        return null;
    }

    public void invalidate(MinecraftServer server, Area area) {
        for (var changeListener : changeListeners) {
            changeListener.accept(area);
        }

        setDirty();
        sync(server);
    }

    private void sync(MinecraftServer server) {
        server.getPlayerList().getPlayers().forEach(player -> ServerPlayNetworking.send(player, new ClientboundAreaSyncPacket(this)));
    }

    public void addChangeListener(Consumer<Area> listener) {
        changeListeners.add(listener);
    }

    public static AreaSavedData getServerData(MinecraftServer server) {
        DimensionDataStorage persistentStateManager = server.overworld().getDataStorage();
        AreaSavedData data = persistentStateManager.computeIfAbsent(factory, "areas");
        data.setDirty();

        return data;
    }

    private static final SavedData.Factory<AreaSavedData> factory = new SavedData.Factory<>(
        AreaSavedData::new,
        AreaSavedData::load,
        null
    );
}
