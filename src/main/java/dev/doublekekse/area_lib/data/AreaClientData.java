package dev.doublekekse.area_lib.data;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

@Environment(EnvType.CLIENT)
public class AreaClientData {
    @ApiStatus.Experimental
    public static AreaSavedData INSTANCE;

    @ApiStatus.Experimental
    public static AreaSavedData getClientLevelData() {
        return INSTANCE;
    }

    @ApiStatus.Internal
    public static void setInstance(AreaSavedData areaSavedData) {
        INSTANCE = areaSavedData;
    }
}
