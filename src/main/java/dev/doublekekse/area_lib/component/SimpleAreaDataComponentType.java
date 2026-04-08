package dev.doublekekse.area_lib.component;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

public final class SimpleAreaDataComponentType<T> extends BaseAreaDataComponentType<T> {
    @ApiStatus.Internal
    public SimpleAreaDataComponentType(Identifier id, Codec<T> codec) {
        super(Type.SIMPLE, id, codec);
    }
}
