package dev.doublekekse.area_lib.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
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
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AreaArgument {
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_AREA = new DynamicCommandExceptionType((object) -> Component.translatableEscape("area_lib.commands.area.error_does_not_exist", object));
    public static final DynamicCommandExceptionType ERROR_NOT_COMPOSITE_AREA = new DynamicCommandExceptionType((object) -> Component.translatableEscape("area_lib.commands.area.error_is_not_composite", object));

    /**
     * Use {@link ResourceLocationArgument#id()}
     * with {@link AreaArgument#listSuggestions(CommandContext, SuggestionsBuilder)}
     * as suggestions instead.
     */
    @Deprecated
    public static ResourceLocationArgument area() {
        return ResourceLocationArgument.id();
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
        var resourceLocation = context.getArgument(name, ResourceLocation.class);
        var savedData = AreaSavedData.getServerData(context.getSource().getServer());

        if (savedData.has(resourceLocation)) {
            return savedData.get(resourceLocation);
        }

        throw ERROR_UNKNOWN_AREA.create(resourceLocation);
    }

    public static CompositeArea getCompositeArea(final CommandContext<CommandSourceStack> context, final String name) throws CommandSyntaxException {
        var resourceLocation = context.getArgument(name, ResourceLocation.class);
        var savedData = AreaSavedData.getServerData(context.getSource().getServer());

        if (!savedData.has(resourceLocation)) {
            throw ERROR_UNKNOWN_AREA.create(resourceLocation);
        }

        var area = savedData.get(resourceLocation);

        if (area instanceof CompositeArea compositeArea) {
            return compositeArea;
        }

        throw ERROR_NOT_COMPOSITE_AREA.create(resourceLocation.toString());
    }

    public static List<Area> getAreas(final CommandContext<CommandSourceStack> context, final String name) throws CommandSyntaxException {
        var string = StringArgumentType.getString(context, name);
        var ids = List.of(string.split(" "));
        var list = new ArrayList<Area>();

        var savedData = AreaSavedData.getServerData(context.getSource().getServer());

        for (String id : ids) {
            var resourceLocation = ResourceLocation.tryParse(id);

            if (resourceLocation == null) {
                throw ResourceLocation.ERROR_INVALID.create();
            }

            if (!savedData.has(resourceLocation)) {
                throw ERROR_UNKNOWN_AREA.create(resourceLocation);
            }

            list.add(savedData.get(resourceLocation));
        }

        return list;
    }
}
