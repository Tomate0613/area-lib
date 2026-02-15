package dev.doublekekse.area_lib.registry;

import dev.doublekekse.area_lib.AreaLib;
import dev.doublekekse.area_lib.component.AreaDataComponent;
import dev.doublekekse.area_lib.component.AreaDataComponentType;
import dev.doublekekse.area_lib.component.GizmoStyleComponent;

import java.util.function.Supplier;

public class BuiltInAreaComponents {
    public static final AreaDataComponentType<GizmoStyleComponent> GIZMO_STYLE_COMPONENT = registerTracking(GizmoStyleComponent::new, "gizmo_style");

    private static <T extends AreaDataComponent> AreaDataComponentType<T> registerTracking(Supplier<T> factory, String path) {
        var id = AreaLib.id(path);

        return AreaDataComponentTypeRegistry.registerTracking(id, factory);
    }

    public static void register() {

    }
}
