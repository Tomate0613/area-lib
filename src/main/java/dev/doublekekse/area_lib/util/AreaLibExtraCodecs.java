package dev.doublekekse.area_lib.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.gizmos.GizmoStyle;

public class AreaLibExtraCodecs {
    public static final Codec<GizmoStyle> GIZMO_STYLE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("stroke").forGetter(GizmoStyle::stroke),
        Codec.FLOAT.fieldOf("stroke_width").forGetter(GizmoStyle::strokeWidth),
        Codec.INT.fieldOf("fill").forGetter(GizmoStyle::fill)
    ).apply(instance, GizmoStyle::new));
}
