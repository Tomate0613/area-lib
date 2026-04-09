package dev.doublekekse.area_lib.component;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

public sealed interface AreaComponentType<T> permits BaseAreaComponentType {
    Identifier id();
    Codec<T> codec();

    @ApiStatus.Internal
    boolean entityTracked();

    @ApiStatus.Internal
    boolean sampled();
}
