package dev.doublekekse.area_lib.registry;

import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class AreaTypeRegistry {
    private static final Map<Identifier, BiFunction<AreaSavedData, Identifier, Area>> areas = new HashMap<>();

    public static Area getArea(Identifier areaType, AreaSavedData savedData, Identifier id) {
        try {
            return areas.get(areaType).apply(savedData, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void register(BiFunction<AreaSavedData, Identifier, Area> areaFactory, Identifier location) {
        areas.put(location, areaFactory);
    }
}
