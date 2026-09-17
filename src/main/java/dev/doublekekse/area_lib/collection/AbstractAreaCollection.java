package dev.doublekekse.area_lib.collection;

import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractAreaCollection implements AreaCollection {
    protected final Set<Identifier> areaIds = new HashSet<>();
    protected final AreaSavedData savedData;

    public AbstractAreaCollection(AreaSavedData savedData) {
        this.savedData = savedData;
    }

    public AbstractAreaCollection(AreaSavedData savedData, Collection<Identifier> areaIds) {
        this.savedData = savedData;
        this.areaIds.addAll(areaIds);
    }

    protected abstract void invalidate();

    @Override
    public void add(Identifier areaId) {
        var didAdd = areaIds.add(areaId);

        if (didAdd) {
            invalidate();
        }
    }

    @Override
    public void remove(Identifier areaId) {
        var didRemove = areaIds.remove(areaId);

        if (didRemove) {
            invalidate();
        }
    }

    @Override
    public void invalidate(Identifier areaId) {
        if (areaIds.contains(areaId)) {
            invalidate();
        }
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        var listTag = new ListTag();

        for (var areaId : areaIds) {
            listTag.add(StringTag.valueOf(areaId.toString()));
        }

        tag.put("area_ids", listTag);

        return tag;
    }

    public void load(CompoundTag tag) {
        var listTag = tag.getList("area_ids").get();
        areaIds.clear();

        for (var areaIdTag : listTag) {
            var areaId = Identifier.parse(areaIdTag.asString().get());

            areaIds.add(areaId);
        }
    }
}
