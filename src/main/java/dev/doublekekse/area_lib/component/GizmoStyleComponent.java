package dev.doublekekse.area_lib.component;

import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.nbt.CompoundTag;

public class GizmoStyleComponent implements AreaDataComponent {
    public GizmoStyle style;
    public static GizmoStyleComponent DEFAULT = new GizmoStyleComponent(GizmoStyle.stroke(0xffffffff));

    public GizmoStyleComponent(GizmoStyle style) {
        this.style = style;
    }

    public GizmoStyleComponent() {
        style = DEFAULT.style;
    }

    @Override
    public void load(AreaSavedData savedData, CompoundTag compoundTag) {
        this.style = new GizmoStyle(compoundTag.getInt("stroke").orElse(-1), compoundTag.getFloat("stroke_width").orElse(2.5f), compoundTag.getInt("fill").orElse(0));
    }

    @Override
    public CompoundTag save() {
        var tag = new CompoundTag();

        tag.putInt("stroke", style.stroke());
        tag.putFloat("stroke_width", style.strokeWidth());
        tag.putInt("fill", style.fill());

        return tag;
    }
}
