package dev.doublekekse.area_lib.component;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

public record AreaDataComponentType<T>(Identifier id, Codec<T> codec, boolean tracking) {
    @ApiStatus.Internal
    public AreaDataComponentType {}
}
