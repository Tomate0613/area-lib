package dev.doublekekse.area_lib.registry;

import dev.doublekekse.area_lib.component.AreaDataComponent;
import dev.doublekekse.area_lib.component.AreaDataComponentType;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AreaDataComponentTypeRegistry {
    private static final Map<ResourceLocation, AreaDataComponentType<?>> REGISTRY = new HashMap<>();

    public static <T extends AreaDataComponent> AreaDataComponentType<T> register(ResourceLocation id, Supplier<T> factory) {
        var type = new AreaDataComponentType<>(id, factory);
        REGISTRY.put(id, type);
        return type;
    }

    public static AreaDataComponentType<?> get(ResourceLocation id) {
        return REGISTRY.get(id);
    }
}
