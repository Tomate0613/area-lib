package dev.doublekekse.area_lib.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.AreaLib;
import dev.doublekekse.area_lib.areas.CompositeArea;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AreaArgument {
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_AREA = new DynamicCommandExceptionType((object) -> Component.translatableEscape("area_lib.commands.area.error_does_not_exist", object));
    public static final DynamicCommandExceptionType ERROR_NOT_COMPOSITE_AREA = new DynamicCommandExceptionType((object) -> Component.translatableEscape("area_lib.commands.area.error_is_not_composite", object));

    /**
     * Use {@link IdentifierArgument#id()}
     * with {@link AreaArgument#listSuggestions(CommandContext, SuggestionsBuilder)}
     * as suggestions instead.
     */
    @Deprecated
    public static IdentifierArgument area() {
        return IdentifierArgument.id();
    }

    public static CompletableFuture<Suggestions> listSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        var savedData = AreaLib.getSavedData(context.getSource().getLevel());

        return SharedSuggestionProvider.suggest(savedData.getAreas().stream().map(
            (area) -> area.getId().toString()
        ), builder);
    }

    public static CompletableFuture<Suggestions> listMultipleSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        var input = builder.getInput();
        var lastSpace = input.lastIndexOf(' ');

        var builderFromLastSpace = new SuggestionsBuilder(input, lastSpace + 1);
        return listSuggestions(context, builderFromLastSpace);
    }

    public static CompletableFuture<Suggestions> listCompositeSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        var savedData = AreaLib.getSavedData(context.getSource().getLevel());

        return SharedSuggestionProvider.suggest(savedData.getAreas().stream().filter(area -> area instanceof CompositeArea).map(
            (area) -> area.getId().toString()
        ), builder);
    }

    public static Area getArea(final CommandContext<CommandSourceStack> context, final String name) throws CommandSyntaxException {
        var Identifier = context.getArgument(name, Identifier.class);
        var savedData = AreaSavedData.getServerData(context.getSource().getServer());

        if (savedData.has(Identifier)) {
            return savedData.get(Identifier);
        }

        throw ERROR_UNKNOWN_AREA.create(Identifier);
    }

    public static CompositeArea getCompositeArea(final CommandContext<CommandSourceStack> context, final String name) throws CommandSyntaxException {
        var Identifier = context.getArgument(name, Identifier.class);
        var savedData = AreaSavedData.getServerData(context.getSource().getServer());

        if (!savedData.has(Identifier)) {
            throw ERROR_UNKNOWN_AREA.create(Identifier);
        }

        var area = savedData.get(Identifier);

        if (area instanceof CompositeArea compositeArea) {
            return compositeArea;
        }

        throw ERROR_NOT_COMPOSITE_AREA.create(Identifier.toString());
    }

    public static List<Area> getAreas(final CommandContext<CommandSourceStack> context, final String name) throws CommandSyntaxException {
        var string = StringArgumentType.getString(context, name);
        var ids = List.of(string.split(" "));
        var list = new ArrayList<Area>();

        var savedData = AreaSavedData.getServerData(context.getSource().getServer());

        for (String id : ids) {
            var identifier = Identifier.tryParse(id);

            if (identifier == null) {
                throw Identifier.ERROR_INVALID.create();
            }

            if (!savedData.has(identifier)) {
                throw ERROR_UNKNOWN_AREA.create(identifier);
            }

            list.add(savedData.get(identifier));
        }

        return list;
    }
}
