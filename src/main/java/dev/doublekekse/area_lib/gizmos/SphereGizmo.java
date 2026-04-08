package dev.doublekekse.area_lib.gizmos;

import net.minecraft.gizmos.Gizmo;
import net.minecraft.gizmos.GizmoPrimitives;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

@ApiStatus.Internal
public record SphereGizmo(Vec3 pos, double radius, GizmoStyle style) implements Gizmo {
    private static final int SLICES = 16;
    private static final int STACKS = 10;

    @Override
    public void emit(@NonNull GizmoPrimitives gizmoPrimitives, float f) {
        if (!this.style.hasStroke() && !this.style.hasFill()) {
            return;
        }

        for (int i = 0; i < STACKS; i++) {
            float theta1 = (float) (Math.PI * i / STACKS);
            float theta2 = (float) (Math.PI * (i + 1) / STACKS);

            for (int j = 0; j < SLICES; j++) {
                float phi1 = (float) (2 * Math.PI * j / SLICES);
                float phi2 = (float) (2 * Math.PI * (j + 1) / SLICES);

                var sp11 = spherePos(theta1, phi1);
                var sp12 = spherePos(theta1, phi2);
                var sp21 = spherePos(theta2, phi1);
                var sp22 = spherePos(theta2, phi2);

                if (style.hasFill()) {
                    gizmoPrimitives.addQuad(sp12, sp22, sp21, sp11, style.multipliedFill(f));
                }


                if (style.hasStroke()) {
                    int strokeColor = this.style.multipliedStroke(f);
                    gizmoPrimitives.addLine(sp11, sp21, strokeColor, style.strokeWidth());
                    gizmoPrimitives.addLine(sp11, sp12, strokeColor, style.strokeWidth());
                }
            }
        }
    }

    private Vec3 spherePos(float theta, float phi) {
        float x = (float) (radius * Math.sin(theta) * Math.cos(phi));
        float y = (float) (radius * Math.cos(theta));
        float z = (float) (radius * Math.sin(theta) * Math.sin(phi));

        return pos.add(x, y, z);
    }
}
