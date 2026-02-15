package dev.doublekekse.area_lib.component;

import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public record AreaDataComponentType<T extends AreaDataComponent>(Identifier id, Supplier<T> factory, boolean tracking) {
}
