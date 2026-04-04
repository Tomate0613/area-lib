package dev.doublekekse.area_lib.registry;

import com.mojang.serialization.Codec;
import dev.doublekekse.area_lib.component.AreaDataComponent;
import dev.doublekekse.area_lib.component.AreaDataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

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


    /**
     * Use {@link #register(Identifier, Codec)} instead
     */
    @Deprecated
    public static <T extends AreaDataComponent> AreaDataComponentType<T> register(Identifier id, Supplier<T> supplier) {
        return register(new AreaDataComponentType<>(id, legacyComponent(supplier), false));
    }

    @Deprecated
    private static <T extends AreaDataComponent> Codec<T> legacyComponent(Supplier<T> supplier) {
        return CompoundTag.CODEC.xmap(tag -> {
            var component = supplier.get();
            component.load(null, tag);
            return component;
        }, AreaDataComponent::save);
    }

    /**
     * Use {@link #registerTracking(Identifier, Codec)} instead
     */
    @Deprecated
    public static <T extends AreaDataComponent> AreaDataComponentType<T> registerTracking(Identifier id, Supplier<T> supplier) {
        return register(new AreaDataComponentType<>(id, legacyComponent(supplier), true));
    }

}
