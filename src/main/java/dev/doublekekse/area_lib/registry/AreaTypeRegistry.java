package dev.doublekekse.area_lib.registry;

import dev.doublekekse.area_lib.Area;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AreaTypeRegistry {
    private static final Map<ResourceLocation, Supplier<Area>> areas = new HashMap<>();

    public static Area getArea(ResourceLocation areaType) {
        try {
            return areas.get(areaType).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void register(Supplier<Area> areaFactory, ResourceLocation location) {
        areas.put(location, areaFactory);
    }
}
