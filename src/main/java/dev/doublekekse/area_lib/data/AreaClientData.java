package dev.doublekekse.area_lib.data;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class AreaClientData {
    public static AreaSavedData instances;

    public static AreaSavedData getClientLevelData() {
        return instances;
    }

    public static void setInstance(AreaSavedData areaSavedData) {
        instances = areaSavedData;
    }
}
