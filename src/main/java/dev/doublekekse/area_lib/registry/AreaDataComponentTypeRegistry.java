package dev.doublekekse.area_lib.registry;

import com.mojang.serialization.Codec;
import dev.doublekekse.area_lib.component.AreaDataComponentType;
import dev.doublekekse.area_lib.component.EntityTrackedAreaDataComponentType;
import dev.doublekekse.area_lib.component.SampledAreaDataComponentType;
import dev.doublekekse.area_lib.component.SimpleAreaDataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AreaDataComponentTypeRegistry {
    private static final Map<Identifier, AreaDataComponentType<?>> REGISTRY = new HashMap<>();

    private static int sampledIndex;

    /**
     * Register a simple area data component
     *
     * @see #registerEntityTracked(Identifier, Codec)
     * @see #registerSampled(Identifier, Codec)
     */
    public static <T> AreaDataComponentType<T> register(Identifier id, Codec<T> codec) {
        return register(new SimpleAreaDataComponentType<>(id, codec));
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
    public static <T> EntityTrackedAreaDataComponentType<T> registerEntityTracked(Identifier id, Codec<T> codec) {
        return register(new EntityTrackedAreaDataComponentType<>(id, codec));
    }

    public static <T> SampledAreaDataComponentType<T> registerSampled(Identifier id, Codec<T> codec) {
        return register(new SampledAreaDataComponentType<>(id, codec, sampledIndex++));
    }

    private static <T extends AreaDataComponentType<?>> T register(T type) {
        REGISTRY.put(type.id(), type);
        return type;
    }

    public static AreaDataComponentType<?> get(Identifier id) {
        return REGISTRY.get(id);
    }

    @ApiStatus.Internal
    public static int samplingCount() {
        return sampledIndex + 1;
    }

    @ApiStatus.Internal
    public static Collection<AreaDataComponentType<?>> getTypes() {
        return REGISTRY.values();
    }
}
