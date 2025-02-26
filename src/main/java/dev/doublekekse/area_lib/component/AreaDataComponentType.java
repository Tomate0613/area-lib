package dev.doublekekse.area_lib.component;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public record AreaDataComponentType<T extends AreaDataComponent>(ResourceLocation id, Supplier<T> factory, boolean tracking) {
}
