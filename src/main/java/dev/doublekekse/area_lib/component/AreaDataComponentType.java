package dev.doublekekse.area_lib.component;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;

public record AreaDataComponentType<T>(Identifier id, Codec<T> codec, boolean tracking) {
}
