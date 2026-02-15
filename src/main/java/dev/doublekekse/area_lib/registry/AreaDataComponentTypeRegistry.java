package dev.doublekekse.area_lib.registry;

import dev.doublekekse.area_lib.component.AreaDataComponent;
import dev.doublekekse.area_lib.component.AreaDataComponentType;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AreaDataComponentTypeRegistry {
    private static final Map<Identifier, AreaDataComponentType<?>> REGISTRY = new HashMap<>();

    public static <T extends AreaDataComponent> AreaDataComponentType<T> register(Identifier id, Supplier<T> factory) {
        return register(new AreaDataComponentType<T>(id, factory, false));
    }

    public static <T extends AreaDataComponent> AreaDataComponentType<T> registerTracking(Identifier id, Supplier<T> factory) {
        return register(new AreaDataComponentType<T>(id, factory, true));
    }

    private static <T extends AreaDataComponent> AreaDataComponentType<T> register(AreaDataComponentType<T> type) {
        REGISTRY.put(type.id(), type);
        return type;
    }

    public static AreaDataComponentType<?> get(Identifier id) {
        return REGISTRY.get(id);
    }
}
