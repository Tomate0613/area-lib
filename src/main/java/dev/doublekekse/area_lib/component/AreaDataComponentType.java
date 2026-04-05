package dev.doublekekse.area_lib.component;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

public record AreaDataComponentType<T>(Identifier id, Codec<T> codec, Type type, int index) {
    @ApiStatus.Internal
    public enum Type {
        SIMPLE,
        ENTITY_TRACKED,
        SAMPLED
    }

    @ApiStatus.Internal
    public AreaDataComponentType {}

    @ApiStatus.Internal
    public boolean tracking() {
        return type == Type.ENTITY_TRACKED;
    }

    @ApiStatus.Internal
    public boolean sampling() {
        return type == Type.SAMPLED;
    }
}
