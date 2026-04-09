package dev.doublekekse.area_lib.registry;

import com.mojang.serialization.Codec;
import dev.doublekekse.area_lib.component.AreaComponentType;
import dev.doublekekse.area_lib.component.EntityTrackedAreaComponentType;
import dev.doublekekse.area_lib.component.SampledAreaComponentType;
import dev.doublekekse.area_lib.component.SimpleAreaComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AreaComponentRegistry {
    private static final Map<Identifier, AreaComponentType<?>> REGISTRY = new HashMap<>();

    private static int sampledIndex;

    /**
     * Register a simple area data component
     *
     * @see #registerEntityTracked(Identifier, Codec)
     * @see #registerSampled(Identifier, Codec)
     */
    public static <T> AreaComponentType<T> register(Identifier id, Codec<T> codec) {
        return register(new SimpleAreaComponentType<>(id, codec));
    }

    /**
     * Register an entity tracked component
     *
     * <p>
     *     Entity tracked components should be used when continuously checking which areas (with the given component) an entity is in
     *     When only rarely checking or checking per position {@link #registerSampled(Identifier, Codec)} should be used instead
     * </p>
     * @see dev.doublekekse.area_lib.data.AreaSavedData#getEntityTrackedAreas(Entity)
     */
    public static <T> EntityTrackedAreaComponentType<T> registerEntityTracked(Identifier id, Codec<T> codec) {
        return register(new EntityTrackedAreaComponentType<>(id, codec));
    }

    public static <T> SampledAreaComponentType<T> registerSampled(Identifier id, Codec<T> codec) {
        return register(new SampledAreaComponentType<>(id, codec, sampledIndex++));
    }

    private static <T extends AreaComponentType<?>> T register(T type) {
        REGISTRY.put(type.id(), type);
        return type;
    }

    public static AreaComponentType<?> get(Identifier id) {
        return REGISTRY.get(id);
    }

    @ApiStatus.Internal
    public static int samplingCount() {
        return sampledIndex + 1;
    }

    @ApiStatus.Internal
    public static Collection<AreaComponentType<?>> getTypes() {
        return REGISTRY.values();
    }
}
