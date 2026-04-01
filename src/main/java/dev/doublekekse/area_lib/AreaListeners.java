package dev.doublekekse.area_lib;

import dev.doublekekse.area_lib.data.AreaSavedData;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@ApiStatus.Internal
public class AreaListeners {
    private static final List<Consumer<AreaSavedData>> listeners = new ArrayList<>();

    @ApiStatus.Internal
    static void add(Consumer<AreaSavedData> listener) {
        listeners.add(listener);
    }

    @ApiStatus.Internal
    public static void emit(AreaSavedData data) {
        for (var listener : listeners) {
            listener.accept(data);
        }
    }

    @ApiStatus.Internal
    public static void emitLoad(AreaSavedData data) {
        emit(data);
    }
}
