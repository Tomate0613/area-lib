package dev.doublekekse.area_lib.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.doublekekse.area_lib.AreaLib;
import dev.doublekekse.area_lib.command.argument.AreaArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@ApiStatus.Internal
public class DevAreaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("area_dev").then(literal("dump_saved_data").executes(ctx -> {
            var server = ctx.getSource().getServer();
            var savedData = AreaLib.getSavedData(server);

            ctx.getSource().sendSuccess(() -> Component.literal(savedData.toString()), false);

            return 1;
        })).then(literal("dump_area").then(argument("area", IdentifierArgument.id()).suggests(AreaArgument::listSuggestions).executes(ctx -> {
            var area = AreaArgument.getArea(ctx, "area");
            ctx.getSource().sendSuccess(() -> NbtUtils.toPrettyComponent(area.save()), false);

            return 1;
        }))));
    }
}
