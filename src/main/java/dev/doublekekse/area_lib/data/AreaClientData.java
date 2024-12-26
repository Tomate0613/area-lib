package dev.doublekekse.area_lib.data;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class AreaClientData {
    public static AreaSavedData instance;

    public static AreaSavedData getClientLevelData() {
        return instance;
    }

    public static void setInstance(AreaSavedData areaSavedData) {
        instance = areaSavedData;
    }
}
