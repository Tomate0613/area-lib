package dev.doublekekse.area_lib.registry;

import com.mojang.serialization.Codec;
import dev.doublekekse.area_lib.AreaLib;
import dev.doublekekse.area_lib.component.AreaDataComponentType;
import dev.doublekekse.area_lib.util.AreaLibExtraCodecs;
import net.minecraft.gizmos.GizmoStyle;

public class BuiltInAreaComponents {
    public static final AreaDataComponentType<GizmoStyle> GIZMO_STYLE_COMPONENT = register("gizmo_style", AreaLibExtraCodecs.GIZMO_STYLE_CODEC);

    private static <T> AreaDataComponentType<T> registerTracking(String path, Codec<T> codec) {
        var id = AreaLib.id(path);

        return AreaDataComponentTypeRegistry.registerTracking(id, codec);
    }

    private static <T> AreaDataComponentType<T> register(String path, Codec<T> codec) {
        var id = AreaLib.id(path);

        return AreaDataComponentTypeRegistry.register(id, codec);
    }

    public static void register() {

    }
}
