package dev.doublekekse.area_lib.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.doublekekse.area_lib.areas.BoxArea;
import dev.doublekekse.area_lib.areas.UnionArea;
import dev.doublekekse.area_lib.command.argument.AreaArgument;
import dev.doublekekse.area_lib.command.argument.CompositeAreaArgument;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_lib.packet.ClientboundAreaSyncPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;

import static dev.doublekekse.area_lib.AreaLib.AREA_LIST_ARGUMENT;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class AreaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
            literal("area").requires((s) -> s.hasPermission(2)).then(literal("create").then(argument("id", ResourceLocationArgument.id()).then(literal("box").then(argument("from", Vec3Argument.vec3()).then(argument("to", Vec3Argument.vec3()).executes((ctx) -> {
                var level = ctx.getSource().getLevel();
                var server = ctx.getSource().getServer();

                var from = Vec3Argument.getVec3(ctx, "from");
                var to = Vec3Argument.getVec3(ctx, "to");

                var savedData = AreaSavedData.getServerData(server);

                var area = new BoxArea(level.dimension().location(), new AABB(from, to));

                var location = ResourceLocationArgument.getId(ctx, "id");

                if (savedData.has(location)) {
                    ctx.getSource().sendFailure(Component.translatable("area_lib.commands.area.create.error_existing"));

                    return 0;
                }

                savedData.put(location, area);
                saveAndSync(savedData, server);

                ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.create.success", location.toString()), true);

                return 1;
            })))).then(literal("union").then(argument("areas", AREA_LIST_ARGUMENT).executes(ctx -> {
                var server = ctx.getSource().getServer();
                var areas = AREA_LIST_ARGUMENT.getList(ctx, "areas");

                var savedData = AreaSavedData.getServerData(server);
                var area = new UnionArea(new HashSet<>(areas));

                var location = ResourceLocationArgument.getId(ctx, "id");

                if (savedData.has(location)) {
                    ctx.getSource().sendFailure(Component.translatable("area_lib.commands.area.create.error_existing"));

                    return 0;
                }

                savedData.put(location, area);
                saveAndSync(savedData, server);

                ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.create.success", location.toString()), true);

                return 1;
            }))))).then(literal("modify").then(argument("id", AreaArgument.area())
                .then(literal("priority").then(argument("priority", IntegerArgumentType.integer()).executes(ctx -> {
                    var server = ctx.getSource().getServer();

                    var savedData = AreaSavedData.getServerData(server);

                    var area = AreaArgument.getArea(ctx, "id");
                    var priority = IntegerArgumentType.getInteger(ctx, "priority");

                    area.getValue().setPriority(priority);

                    ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.modify.priority.success", area.getKey().toString(), priority), true);

                    saveAndSync(savedData, server);

                    return 1;
                }))).then(literal("color").then(argument("r", FloatArgumentType.floatArg(0, 1)).then(argument("g", FloatArgumentType.floatArg(0, 1)).then(argument("b", FloatArgumentType.floatArg(0, 1)).executes(ctx -> {
                    var server = ctx.getSource().getServer();

                    var savedData = AreaSavedData.getServerData(server);

                    var area = AreaArgument.getArea(ctx, "id");

                    var r = FloatArgumentType.getFloat(ctx, "r");
                    var g = FloatArgumentType.getFloat(ctx, "g");
                    var b = FloatArgumentType.getFloat(ctx, "b");

                    area.getValue().setColor(r, g, b);

                    ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.modify.color.success", area.getKey().toString()), true);

                    saveAndSync(savedData, server);

                    return 1;
                })))))
            )).then(literal("delete").then(argument("id", AreaArgument.area()).executes(ctx -> {
                var server = ctx.getSource().getServer();

                var savedData = AreaSavedData.getServerData(server);

                var location = AreaArgument.getAreaId(ctx, "id");

                var area = savedData.remove(location);

                saveAndSync(savedData, server);

                ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.delete.success", location.toString(), area.toString()), true);

                return 1;
            }))).then(literal("query").executes(ctx -> {
                var level = ctx.getSource().getLevel();
                var server = ctx.getSource().getServer();

                var savedData = AreaSavedData.getServerData(server);

                var pos = ctx.getSource().getPosition();

                var entries = savedData.getAreaEntries();

                var count = 0;
                for (var entry : entries) {
                    if (entry.getValue().contains(level, pos)) {
                        ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.query.entry", entry.getKey().toString()), false);
                        count++;
                    }
                }

                if (count == 0) {
                    ctx.getSource().sendFailure(Component.translatable("area_lib.commands.area.error_not_in_area"));
                }

                return count;
            })).then(literal("modify_composite").then(argument("id", CompositeAreaArgument.area())
                .then(literal("add").then(argument("sub_area", AreaArgument.area()).executes(ctx -> {
                    var server = ctx.getSource().getServer();

                    var savedData = AreaSavedData.getServerData(server);
                    var area = CompositeAreaArgument.getArea(ctx, "id");
                    var subArea = AreaArgument.getArea(ctx, "sub_area");

                    ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.modify_composite.add.success", subArea.getKey().toString(), area.getKey().toString()), false);

                    area.getValue().addSubArea(subArea);
                    saveAndSync(savedData, server);

                    return 1;
                }))).then(literal("remove").then(argument("sub_area", AreaArgument.area()).executes(ctx -> {
                    var server = ctx.getSource().getServer();

                    var savedData = AreaSavedData.getServerData(server);
                    var area = CompositeAreaArgument.getArea(ctx, "id");
                    var subArea = AreaArgument.getArea(ctx, "sub_area");

                    ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.modify_composite.remove.success", subArea.getKey().toString(), area.getKey().toString()), false);

                    area.getValue().removeSubArea(subArea);
                    saveAndSync(savedData, server);

                    return 1;
                }))))
            )
        );
    }

    static void saveAndSync(AreaSavedData savedData, MinecraftServer server) {
        savedData.setDirty();

        server.getPlayerList().getPlayers().forEach(player -> ServerPlayNetworking.send(player, new ClientboundAreaSyncPacket(savedData)));
    }
}
