package dev.doublekekse.area_lib.component;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

public final class EntityTrackedAreaComponentType<T> extends BaseAreaComponentType<T> {
    @ApiStatus.Internal
    public EntityTrackedAreaComponentType(Identifier id, Codec<T> codec) {
        super(Type.ENTITY_TRACKED, id, codec);
    }
}
