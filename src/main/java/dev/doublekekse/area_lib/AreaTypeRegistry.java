package dev.doublekekse.area_lib;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class AreaTypeRegistry {
    private static final Map<ResourceLocation, Class<? extends Area>> areas = new HashMap<>();

    public static Area getArea(ResourceLocation areaType) {
        try {
            return areas.get(areaType).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void register(Class<? extends Area> clazz, ResourceLocation location) {
        try {
            clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        areas.put(location, clazz);
    }
}
