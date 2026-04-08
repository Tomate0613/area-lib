package dev.doublekekse.area_lib.component;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

public final class EntityTrackedAreaDataComponentType<T> extends BaseAreaDataComponentType<T> {
    @ApiStatus.Internal
    public EntityTrackedAreaDataComponentType(Identifier id, Codec<T> codec) {
        super(Type.ENTITY_TRACKED, id, codec);
    }
}
