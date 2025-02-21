package dev.doublekekse.area_lib.registry;

import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class AreaTypeRegistry {
    private static final Map<ResourceLocation, BiFunction<AreaSavedData, ResourceLocation, Area>> areas = new HashMap<>();

    public static Area getArea(ResourceLocation areaType, AreaSavedData savedData, ResourceLocation id) {
        try {
            return areas.get(areaType).apply(savedData, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void register(BiFunction<AreaSavedData, ResourceLocation, Area> areaFactory, ResourceLocation location) {
        areas.put(location, areaFactory);
    }
}
