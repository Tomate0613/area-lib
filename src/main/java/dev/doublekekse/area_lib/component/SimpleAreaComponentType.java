package dev.doublekekse.area_lib.component;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

public final class SimpleAreaComponentType<T> extends BaseAreaComponentType<T> {
    @ApiStatus.Internal
    public SimpleAreaComponentType(Identifier id, Codec<T> codec) {
        super(Type.SIMPLE, id, codec);
    }
}
