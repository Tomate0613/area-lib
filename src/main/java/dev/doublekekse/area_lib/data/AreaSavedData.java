package dev.doublekekse.area_lib.data;

import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.AreaTypeRegistry;
import dev.doublekekse.area_lib.areas.CompositeArea;
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
import java.util.function.BiConsumer;

public class AreaSavedData extends SavedData {
    private final List<BiConsumer<ResourceLocation, Area>> changeListeners = new ArrayList<>();
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

            var area = AreaTypeRegistry.getArea(ResourceLocation.parse(tag.getString("type")));
            area.load(data, tag.getCompound("data"));

            data.put(ResourceLocation.parse(key), area);
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

    public void put(ResourceLocation id, Area area) {
        areas.put(id, area);

        updated(id, area);
    }

    public Area get(ResourceLocation id) {
        if (!isInitialized) {
            throw new IllegalStateException("Areas have not been initialized");
        }

        return areas.get(id);
    }

    public Area remove(ResourceLocation id) {
        var removedArea = areas.remove(id);

        updated(id, removedArea);

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

    public void updated(ResourceLocation id, Area area) {
        for (var changeListener : changeListeners) {
            changeListener.accept(id, area);
        }

        setDirty();
    }

    public void addChangeListener(BiConsumer<ResourceLocation, Area> listener) {
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
