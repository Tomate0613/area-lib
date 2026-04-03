package dev.doublekekse.area_lib.registry;

import com.mojang.serialization.Codec;
import dev.doublekekse.area_lib.component.AreaDataComponentType;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class AreaDataComponentTypeRegistry {
    private static final Map<Identifier, AreaDataComponentType<?>> REGISTRY = new HashMap<>();

    public static <T> AreaDataComponentType<T> register(Identifier id, Codec<T> codec) {
        return register(new AreaDataComponentType<>(id, codec, false));
    }

    public static <T> AreaDataComponentType<T> registerTracking(Identifier id, Codec<T> codec) {
        return register(new AreaDataComponentType<>(id, codec, true));
    }

    private static <T> AreaDataComponentType<T> register(AreaDataComponentType<T> type) {
        REGISTRY.put(type.id(), type);
        return type;
    }

    public static AreaDataComponentType<?> get(Identifier id) {
        return REGISTRY.get(id);
    }
}
