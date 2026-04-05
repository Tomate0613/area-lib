package dev.doublekekse.area_lib.registry;

import com.mojang.serialization.Codec;
import dev.doublekekse.area_lib.component.AreaDataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

public class AreaDataComponentTypeRegistry {
    private static final Map<Identifier, AreaDataComponentType<?>> REGISTRY = new HashMap<>();

    private static int simpleIndex;
    private static int entityTrackedIndex;
    private static int sampledIndex;

    /**
     * Register a simple area data component
     *
     * @see #registerEntityTracked(Identifier, Codec)
     * @see #registerSampled(Identifier, Codec)
     */
    public static <T> AreaDataComponentType<T> register(Identifier id, Codec<T> codec) {
        return register(new AreaDataComponentType<>(id, codec, AreaDataComponentType.Type.SIMPLE, simpleIndex++));
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
    public static <T> AreaDataComponentType<T> registerEntityTracked(Identifier id, Codec<T> codec) {
        return register(new AreaDataComponentType<>(id, codec, AreaDataComponentType.Type.ENTITY_TRACKED, entityTrackedIndex++));
    }

    public static <T> AreaDataComponentType<T> registerSampled(Identifier id, Codec<T> codec) {
        return register(new AreaDataComponentType<>(id, codec, AreaDataComponentType.Type.SAMPLED, sampledIndex++));
    }

    private static <T> AreaDataComponentType<T> register(AreaDataComponentType<T> type) {
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
}
