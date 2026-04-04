package dev.doublekekse.area_lib.component;

import com.mojang.serialization.Codec;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

/**
 * No longer necessary
 * @see dev.doublekekse.area_lib.registry.AreaDataComponentTypeRegistry#register(Identifier, Codec)
 * @see dev.doublekekse.area_lib.registry.AreaDataComponentTypeRegistry#registerTracking(Identifier, Codec)
 */
@Deprecated
public interface AreaDataComponent {
    /**
     * Loads the component's data from a {@link CompoundTag}.
     *
     * @param savedData   the area saved data. Areas are not loaded yet
     * @param compoundTag the tag containing component data
     */
    void load(AreaSavedData savedData, CompoundTag compoundTag);

    /**
     * Saves the component's data to a {@link CompoundTag}.
     *
     * @return a {@link CompoundTag} containing the saved state of the component
     */
    CompoundTag save();
}
