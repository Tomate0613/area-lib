package dev.doublekekse.area_lib.component;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

sealed class BaseAreaComponentType<T> implements AreaComponentType<T> permits EntityTrackedAreaComponentType, SampledAreaComponentType, SimpleAreaComponentType {
    final Type type;
    final Identifier id;
    final Codec<T> codec;

    BaseAreaComponentType(Type type, Identifier id, Codec<T> codec) {
        this.type = type;
        this.id = id;
        this.codec = codec;
    }

    @ApiStatus.Internal
    enum Type {
        SIMPLE,
        ENTITY_TRACKED,
        SAMPLED
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public Codec<T> codec() {
        return codec;
    }

    @ApiStatus.Internal
    public boolean entityTracked() {
        return type == Type.ENTITY_TRACKED;
    }

    @ApiStatus.Internal
    public boolean sampled() {
        return type == Type.SAMPLED;
    }
}
