package dev.doublekekse.area_lib.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.areas.BoxArea;
import dev.doublekekse.area_lib.areas.CompositeArea;
import dev.doublekekse.area_lib.areas.SphereArea;
import dev.doublekekse.area_lib.areas.UnionArea;
import dev.doublekekse.area_lib.bvh.LazyAreaBVHTree;
import dev.doublekekse.area_lib.command.argument.ARGBColorArgument;
import dev.doublekekse.area_lib.command.argument.AreaArgument;
import dev.doublekekse.area_lib.component.GizmoStyleComponent;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_lib.registry.BuiltInAreaComponents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class AreaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("area").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(literal("create").then(forEachAreaShape(argument("id", IdentifierArgument.id()), AreaCommand::create, "id")))
            .then(literal("modify").then(argument("id", IdentifierArgument.id()).suggests(AreaArgument::listSuggestions)
                .then(forEachAreaShape(literal("replace_shape"), AreaCommand::replace, "id"))
                .then(literal("priority").then(argument("priority", IntegerArgumentType.integer()).executes(ctx -> {
                    var server = ctx.getSource().getServer();

                    var area = AreaArgument.getArea(ctx, "id");
                    var priority = IntegerArgumentType.getInteger(ctx, "priority");

                    area.setPriority(server, priority);

                    ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.modify.priority.success", area.toString(), priority), true);

                    return 1;
                }))).then(literal("copy_components_from").then(argument("other_id", IdentifierArgument.id()).suggests(AreaArgument::listSuggestions).executes(ctx -> {
                    var server = ctx.getSource().getServer();

                    var area = AreaArgument.getArea(ctx, "id");
                    var other = AreaArgument.getArea(ctx, "other_id");

                    area.copyComponentsFrom(server, other);
                    ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.modify.copy_components_from.success", other.toString(), area.toString()), true);
                    return 1;
                }))).then(literal("gizmo_style")
                    .then(literal("stroke_color").then(argument("color", StringArgumentType.word())
                        .executes(updateGizmoStyle((s, ctx) -> new GizmoStyle(ARGBColorArgument.getColor(ctx, "color"), s.strokeWidth(), s.fill())))))
                    .then(literal("stroke_width").then(argument("width", FloatArgumentType.floatArg(0))
                        .executes(updateGizmoStyle((s, ctx) -> new GizmoStyle(s.stroke(), FloatArgumentType.getFloat(ctx, "width"), s.fill())))))
                    .then(literal("fill_color").then(argument("color", StringArgumentType.word())
                        .executes(updateGizmoStyle((s, ctx) -> new GizmoStyle(s.stroke(), s.strokeWidth(), ARGBColorArgument.getColor(ctx, "color"))))))
                )
            )).then(literal("delete").then(argument("id", IdentifierArgument.id()).suggests(AreaArgument::listSuggestions).executes(ctx -> {
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
            })).then(literal("modify_composite").then(argument("id", IdentifierArgument.id()).suggests(AreaArgument::listCompositeSuggestions)
                .then(literal("add").then(argument("sub_area", IdentifierArgument.id()).suggests(AreaArgument::listSuggestions).executes(ctx -> {
                    var server = ctx.getSource().getServer();

                    var area = AreaArgument.getCompositeArea(ctx, "id");
                    var subArea = AreaArgument.getArea(ctx, "sub_area");

                    if (subArea instanceof CompositeArea) {
                        ctx.getSource().sendFailure(Component.translatable("area_lib.commands.area.error_composite_sub_area"));

                        return 0;
                    }

                    ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.modify_composite.add.success", subArea.toString(), area.toString()), false);

                    area.addSubArea(server, subArea);

                    return 1;
                }))).then(literal("remove").then(argument("sub_area", IdentifierArgument.id()).suggests(AreaArgument::listSuggestions).executes(ctx -> {
                    var server = ctx.getSource().getServer();

                    var area = AreaArgument.getCompositeArea(ctx, "id");
                    var subArea = AreaArgument.getArea(ctx, "sub_area");

                    ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.modify_composite.remove.success", subArea.toString(), area.toString()), false);

                    area.removeSubArea(server, subArea);

                    return 1;
                }))))
            ).then(literal("list").executes(ctx -> {
                var source = ctx.getSource();
                var server = source.getServer();
                var savedData = AreaSavedData.getServerData(server);
                var areas = savedData.getAreas();
                var size = areas.size();

                source.sendSuccess(() -> Component.translatable("area_lib.commands.area.list." + (size == 0 ? "none" : size == 1 ? "singular" : "plural"), size), false);
                for (var area : areas) {
                    source.sendSuccess(() -> Component.literal("- " + area.toString()), false);
                }

                return size;
            }))
        );
    }

    @FunctionalInterface
    interface ShapeAction {
        int apply(AreaSavedData savedData, CommandContext<CommandSourceStack> ctx, Area area) throws CommandSyntaxException;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> forEachAreaShape(ArgumentBuilder<CommandSourceStack, ?> builder, ShapeAction action, String areaArgumentName) {
        return (builder.then(literal("box").then(argument("from", Vec3Argument.vec3()).then(argument("to", Vec3Argument.vec3()).executes((ctx) -> {
            var level = ctx.getSource().getLevel();
            var server = ctx.getSource().getServer();

            var from = Vec3Argument.getVec3(ctx, "from");
            var to = Vec3Argument.getVec3(ctx, "to");

            var savedData = AreaSavedData.getServerData(server);
            var id = IdentifierArgument.getId(ctx, areaArgumentName);

            var area = new BoxArea(savedData, id, level.dimension().identifier(), new AABB(from, to));

            return action.apply(savedData, ctx, area);
        })))).then(literal("union").then(argument("areas", StringArgumentType.greedyString()).suggests(AreaArgument::listMultipleSuggestions).executes(ctx -> {
            var server = ctx.getSource().getServer();
            var areas = AreaArgument.getAreas(ctx, "areas");
            var savedData = AreaSavedData.getServerData(server);

            for (var area : areas) {
                if (area instanceof CompositeArea) {
                    ctx.getSource().sendFailure(Component.translatable("area_lib.commands.area.error_composite_sub_area"));

                    return 0;
                }
            }

            var bvhTree = new LazyAreaBVHTree(savedData, areas.stream().map(Area::getId).toList());
            var id = IdentifierArgument.getId(ctx, areaArgumentName);

            var area = new UnionArea(savedData, id, bvhTree);

            return action.apply(savedData, ctx, area);
        }))).then(literal("sphere").then(argument("center", Vec3Argument.vec3()).then(argument("radius", DoubleArgumentType.doubleArg()).executes(ctx -> {
            var level = ctx.getSource().getLevel();
            var server = ctx.getSource().getServer();

            var center = Vec3Argument.getVec3(ctx, "center");
            var radius = DoubleArgumentType.getDouble(ctx, "radius");

            var savedData = AreaSavedData.getServerData(server);
            var id = IdentifierArgument.getId(ctx, areaArgumentName);

            var area = new SphereArea(savedData, id, level.dimension().identifier(), center, radius);

            return action.apply(savedData, ctx, area);
        })))));
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

    private static int replace(AreaSavedData savedData, CommandContext<CommandSourceStack> ctx, Area area) throws CommandSyntaxException {
        var server = ctx.getSource().getServer();


        if (!savedData.has(area.getId())) {
            throw AreaArgument.ERROR_UNKNOWN_AREA.create(area.getId());
        }


        var previousArea = savedData.get(area.getId());

        // TODO
        if (area instanceof CompositeArea compositeArea) {
            if (compositeArea.hasSubArea(previousArea)) {
                ctx.getSource().sendFailure(Component.translatable("area_lib.commands.area.modify.replace.error.self_composite"));

                return 0;
            }
        }

        var compositeAreas = new ArrayList<CompositeArea>();

        for (var other : savedData.getAreas()) {
            if (other instanceof CompositeArea compositeArea) {
                if (compositeArea.hasSubArea(previousArea)) {
                    compositeAreas.add(compositeArea);
                }
            }
        }

        savedData.remove(null, previousArea);
        savedData.put(null, area);

        for (var compositeArea : compositeAreas) {
            compositeArea.addSubArea(null, area);
        }

        area.copyComponentsFrom(server, previousArea);

        ctx.getSource().sendSuccess(() -> Component.translatable("area_lib.commands.area.modify.replace.success", area.toString()), true);

        return 1;
    }


    @FunctionalInterface
    interface GizmoStyleAction {
        GizmoStyle apply(GizmoStyle s, CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException;
    }

    private static Command<CommandSourceStack> updateGizmoStyle(GizmoStyleAction modify) {
        return (ctx) -> {
            var source = ctx.getSource();
            var server = source.getServer();
            var area = AreaArgument.getArea(ctx, "id");

            var style = area.getOrDefault(BuiltInAreaComponents.GIZMO_STYLE_COMPONENT, GizmoStyleComponent.DEFAULT).style;
            var modifiedStyle = modify.apply(style, ctx);

            area.put(server, BuiltInAreaComponents.GIZMO_STYLE_COMPONENT, new GizmoStyleComponent(modifiedStyle));

            source.sendSuccess(() -> Component.translatable("area_lib.commands.area.modify.gizmo_style.success", area.toString()), false);

            return 1;
        };
    }
}
