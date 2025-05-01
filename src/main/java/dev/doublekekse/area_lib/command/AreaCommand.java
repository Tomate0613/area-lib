package dev.doublekekse.area_lib.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.areas.BoxArea;
import dev.doublekekse.area_lib.areas.CompositeArea;
import dev.doublekekse.area_lib.areas.SphereArea;
import dev.doublekekse.area_lib.areas.UnionArea;
import dev.doublekekse.area_lib.bvh.LazyAreaBVHTree;
import dev.doublekekse.area_lib.command.argument.AreaArgument;
import dev.doublekekse.area_lib.command.argument.CompositeAreaArgument;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;

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
                var id = ResourceLocationArgument.getId(ctx, "id");

                var area = new BoxArea(savedData, id, level.dimension().location(), new AABB(from, to));

                return create(savedData, ctx, area);
            })))).then(literal("union").then(argument("areas", AREA_LIST_ARGUMENT).executes(ctx -> {
                var server = ctx.getSource().getServer();
                var areas = AREA_LIST_ARGUMENT.getList(ctx, "areas");
                var savedData = AreaSavedData.getServerData(server);

                for (var areaId : areas) {
                    var area = savedData.get(areaId);

                    if (area == null) {
                        ctx.getSource().sendFailure(Component.translatable("area_lib.commands.area.error_does_not_exist", areaId.toString()));

                        return 0;
                    }

                    if (area instanceof CompositeArea) {
                        ctx.getSource().sendFailure(Component.translatable("area_lib.commands.area.error_composite_sub_area"));

                        return 0;
                    }
                }

                var bvhTree = new LazyAreaBVHTree(savedData, areas);
                var id = ResourceLocationArgument.getId(ctx, "id");

                var area = new UnionArea(savedData, id, bvhTree);

                return create(savedData, ctx, area);
            }))).then(literal("sphere").then(argument("center", Vec3Argument.vec3()).then(argument("radius", DoubleArgumentType.doubleArg()).executes(ctx -> {
                var level = ctx.getSource().getLevel();
                var server = ctx.getSource().getServer();

                var center = Vec3Argument.getVec3(ctx, "center");
                var radius = DoubleArgumentType.getDouble(ctx, "radius");

                var savedData = AreaSavedData.getServerData(server);
                var id = ResourceLocationArgument.getId(ctx, "id");

                var area = new SphereArea(savedData, id, level.dimension().location(), center, radius);

                return create(savedData, ctx, area);
            })))))).then(literal("modify").then(argument("id", AreaArgument.area())
                .then(literal("priority").then(argument("priority", IntegerArgumentType.integer()).executes(ctx -> {
                    var server = ctx.getSource().getServer();

                    var area = AreaArgument.getArea(ctx, "id");
                    var priority = IntegerArgumentType.getInteger(ctx, "priority");

                    area.setPriority(server, priority);

                    ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.modify.priority.success", area.toString(), priority), true);

                    return 1;
                }))).then(literal("color").then(argument("r", FloatArgumentType.floatArg(0, 1)).then(argument("g", FloatArgumentType.floatArg(0, 1)).then(argument("b", FloatArgumentType.floatArg(0, 1)).executes(ctx -> {
                    var server = ctx.getSource().getServer();

                    var area = AreaArgument.getArea(ctx, "id");

                    var r = FloatArgumentType.getFloat(ctx, "r");
                    var g = FloatArgumentType.getFloat(ctx, "g");
                    var b = FloatArgumentType.getFloat(ctx, "b");

                    area.setColor(server, r, g, b);

                    ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.modify.color.success", area.toString()), true);

                    return 1;
                })))))
            )).then(literal("delete").then(argument("id", AreaArgument.area()).executes(ctx -> {
                var server = ctx.getSource().getServer();

                var savedData = AreaSavedData.getServerData(server);

                var area = AreaArgument.getArea(ctx, "id");
                savedData.remove(server, area);

                ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.delete.success", area.toString()), true);

                return 1;
            }))).then(literal("query").executes(ctx -> {
                var level = ctx.getSource().getLevel();
                var server = ctx.getSource().getServer();

                var savedData = AreaSavedData.getServerData(server);

                var pos = ctx.getSource().getPosition();

                var areas = savedData.getAreas();

                var count = 0;
                for (var area : areas) {
                    if (area.contains(level, pos)) {
                        ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.query.entry", area.toString()), false);
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

                    var area = CompositeAreaArgument.getArea(ctx, "id");
                    var subArea = AreaArgument.getArea(ctx, "sub_area");

                    if (subArea instanceof CompositeArea) {
                        ctx.getSource().sendFailure(Component.translatable("area_lib.commands.area.error_composite_sub_area"));

                        return 0;
                    }

                    ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.modify_composite.add.success", subArea.toString(), area.toString()), false);

                    area.addSubArea(server, subArea);

                    return 1;
                }))).then(literal("remove").then(argument("sub_area", AreaArgument.area()).executes(ctx -> {
                    var server = ctx.getSource().getServer();

                    var area = CompositeAreaArgument.getArea(ctx, "id");
                    var subArea = AreaArgument.getArea(ctx, "sub_area");

                    ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.modify_composite.remove.success", subArea.toString(), area.toString()), false);

                    area.removeSubArea(server, subArea);

                    return 1;
                }))))
            )
        );
    }

    private static int create(AreaSavedData savedData, CommandContext<CommandSourceStack> ctx, Area area) {
        var server = ctx.getSource().getServer();

        if (savedData.has(area.getId())) {
            ctx.getSource().sendFailure(Component.translatable("area_lib.commands.area.create.error_existing"));

            return 0;
        }

        savedData.put(server, area);

        ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.create.success", area.toString()), true);

        return 1;
    }
}
