package dev.doublekekse.area_lib.component;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

public final class SampledAreaComponentType<T> extends BaseAreaComponentType<T> {
    @ApiStatus.Internal
    public final int index;

    @ApiStatus.Internal
    public SampledAreaComponentType(Identifier id, Codec<T> codec, int index) {
        super(Type.SAMPLED, id, codec);

        this.index = index;
    }
}
