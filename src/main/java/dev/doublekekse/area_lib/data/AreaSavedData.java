package dev.doublekekse.area_lib.data;

import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.AreaTypeRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AreaSavedData extends SavedData {
    private final Map<ResourceLocation, Area> areas = new HashMap<>();

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

        for (String key : compoundTag.getAllKeys()) {
            var tag = compoundTag.getCompound(key);

            var area = AreaTypeRegistry.getArea(ResourceLocation.parse(tag.getString("type")));
            area.load(tag.getCompound("data"));

            data.put(ResourceLocation.parse(key), area);
        }

        return data;
    }

    public Collection<Area> getAreas() {
        return areas.values();
    }

    public void put(ResourceLocation key, Area area) {
        areas.put(key, area);
        setDirty();
    }

    public Area get(ResourceLocation id) {
        return areas.get(id);
    }

    public Area remove(ResourceLocation id) {
        return areas.remove(id);
    }

    public boolean has(ResourceLocation id) {
        return areas.containsKey(id);
    }

    public record IdentifiableArea(Area area, ResourceLocation id) {

    }

    public @Nullable IdentifiableArea find(Level level, Vec3 pos) {
        for (var location : areas.keySet()) {
            var area = areas.get(location);

            if (area.contains(level, pos)) {
                return new IdentifiableArea(area, location);
            }
        }

        return null;
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
