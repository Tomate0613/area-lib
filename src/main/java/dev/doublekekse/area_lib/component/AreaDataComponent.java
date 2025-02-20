package dev.doublekekse.area_lib.component;

import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.nbt.CompoundTag;

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
