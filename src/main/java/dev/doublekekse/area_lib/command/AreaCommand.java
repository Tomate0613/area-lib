package dev.doublekekse.area_lib.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import dev.doublekekse.area_lib.areas.BlockArea;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_lib.packet.ClientboundAreaSyncPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.AABB;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class AreaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            literal("area").requires((s) -> s.hasPermission(2)).then(literal("create").then(argument("id", ResourceLocationArgument.id()).then(argument("pos1", Vec3Argument.vec3()).then(argument("pos2", Vec3Argument.vec3()).executes((ctx) -> {
                var level = ctx.getSource().getLevel();
                var server = ctx.getSource().getServer();

                var pos1 = Vec3Argument.getVec3(ctx, "pos1");
                var pos2 = Vec3Argument.getVec3(ctx, "pos2");

                var savedData = AreaSavedData.getServerData(server);

                var area = new BlockArea(level.dimension().location(), new AABB(pos1, pos2));

                var location = ResourceLocationArgument.getId(ctx, "id");

                if (savedData.has(location)) {
                    ctx.getSource().sendFailure(Component.translatable("area_lib.commands.area.create.error_existing"));

                    return 0;
                }

                savedData.put(location, area);
                saveAndSync(savedData, server);

                ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.create.success", location.toString()), true);

                return 1;
            }))))).then(literal("add_to").then(argument("id", ResourceLocationArgument.id()).then(argument("pos1", Vec3Argument.vec3()).then(argument("pos2", Vec3Argument.vec3()).executes((ctx) ->
            {
                var server = ctx.getSource().getServer();

                var pos1 = Vec3Argument.getVec3(ctx, "pos1");
                var pos2 = Vec3Argument.getVec3(ctx, "pos2");

                var savedData = AreaSavedData.getServerData(server);

                var location = ResourceLocationArgument.getId(ctx, "id");

                if (!savedData.has(location)) {
                    ctx.getSource().sendFailure(Component.translatable("area_lib.commands.area.error_does_not_exist"));

                    return 0;
                }

                var area = savedData.get(location);

                if (!(area instanceof BlockArea blockArea)) {
                    ctx.getSource().sendFailure(Component.translatable("area_lib.commands.area.error_only_block"));

                    return 0;
                }

                blockArea.aabbs.add(new AABB(pos1, pos2));

                saveAndSync(savedData, server);

                ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.add_to.success", location.toString()), true);

                return 1;

            }))))).then(literal("remove_from").executes(ctx -> {
                var level = ctx.getSource().getLevel();
                var server = ctx.getSource().getServer();

                var savedData = AreaSavedData.getServerData(server);

                var pos = ctx.getSource().getPosition();

                var identifiableArea = savedData.find(level, pos);

                if (identifiableArea == null) {
                    ctx.getSource().sendFailure(Component.translatable("area_lib.commands.area.error_not_in_area"));

                    return 0;
                }

                if (!(identifiableArea.area() instanceof BlockArea blockArea)) {
                    ctx.getSource().sendFailure(Component.translatable("area_lib.commands.area.error_only_block"));

                    return 0;
                }

                if (blockArea.aabbs.size() <= 1) {
                    ctx.getSource().sendFailure(Component.translatable("area_lib.commands.area.remove_from.error_to_few"));

                    return 0;
                }

                for (var aabb : blockArea.aabbs) {
                    if (aabb.contains(pos)) {
                        blockArea.aabbs.remove(aabb);
                        saveAndSync(savedData, server);

                        ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.remove_from.success", identifiableArea.id().toString()), true);

                        return 1;
                    }
                }

                ctx.getSource().sendFailure(Component.translatable("area_lib.commands.area.error_impossible"));
                return 0;
            })).then(literal("set_color").then(argument("r", FloatArgumentType.floatArg(0, 1)).then(argument("g", FloatArgumentType.floatArg(0, 1)).then(argument("b", FloatArgumentType.floatArg(0, 1)).executes(ctx -> {
                var level = ctx.getSource().getLevel();
                var server = ctx.getSource().getServer();

                var savedData = AreaSavedData.getServerData(server);

                var pos = ctx.getSource().getPosition();

                var identifiableArea = savedData.find(level, pos);

                if (identifiableArea == null) {
                    ctx.getSource().sendFailure(Component.translatable("area_lib.commands.area.error_not_in_area"));

                    return 0;
                }

                identifiableArea.area().setColor(FloatArgumentType.getFloat(ctx, "r"), FloatArgumentType.getFloat(ctx, "g"), FloatArgumentType.getFloat(ctx, "b"));

                ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.set_color.success", identifiableArea.id().toString()), true);

                saveAndSync(savedData, server);

                return 1;
            }))))).then(literal("delete").then(argument("id", ResourceLocationArgument.id()).executes(ctx -> {
                var server = ctx.getSource().getServer();

                var savedData = AreaSavedData.getServerData(server);

                var location = ResourceLocationArgument.getId(ctx, "id");

                if (!savedData.has(location)) {
                    ctx.getSource().sendFailure(Component.translatable("area_lib.commands.area.error_does_not_exist"));

                    return 0;
                }

                var area = savedData.remove(location);

                saveAndSync(savedData, server);

                ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.delete.success", location.toString(), area.toString()), true);

                return 1;
            })))
        );
    }

    static void saveAndSync(AreaSavedData savedData, MinecraftServer server) {
        savedData.setDirty();

        server.getPlayerList().getPlayers().forEach(player -> ServerPlayNetworking.send(player, new ClientboundAreaSyncPacket(savedData)));
    }
}
