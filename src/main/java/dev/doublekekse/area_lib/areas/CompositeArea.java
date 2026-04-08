package dev.doublekekse.area_lib.areas;

import dev.doublekekse.area_lib.Area;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public interface CompositeArea {
    void addSubArea(@Nullable MinecraftServer server, Area area);
    void removeSubArea(@Nullable MinecraftServer server, Area area);
    boolean hasSubArea(Area area);
}
