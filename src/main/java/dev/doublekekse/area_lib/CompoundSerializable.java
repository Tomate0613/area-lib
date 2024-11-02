package dev.doublekekse.area_lib;

import net.minecraft.nbt.CompoundTag;

public interface CompoundSerializable {
    void load(CompoundTag tag);

    CompoundTag save();
}
