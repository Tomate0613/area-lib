package dev.doublekekse.area_lib.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.data.AreaClientData;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_lib.util.Pair;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AreaArgument implements ArgumentType<ResourceLocation> {
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_AREA = new DynamicCommandExceptionType((object) -> Component.translatableEscape("area_lib.commands.area.error_does_not_exist", object));

    @Override
    public ResourceLocation parse(StringReader reader) throws CommandSyntaxException {
        return ResourceLocation.read(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (context.getSource() instanceof ClientSuggestionProvider) {
            var savedData = AreaClientData.getClientLevelData();

            return SharedSuggestionProvider.suggest(savedData.getAreaEntries().stream().map(
                (entry) -> entry.getKey().toString()
            ), builder);
        }

        return Suggestions.empty();
    }

    public static AreaArgument area() {
        return new AreaArgument();
    }

    public static ResourceLocation getAreaId(final CommandContext<CommandSourceStack> context, final String name) throws CommandSyntaxException {
        var resourceLocation = context.getArgument(name, ResourceLocation.class);
        var savedData = AreaSavedData.getServerData(context.getSource().getServer());

        if (savedData.has(resourceLocation)) {
            return resourceLocation;
        }

        throw ERROR_UNKNOWN_AREA.create(resourceLocation);
    }

    public static Map.Entry<ResourceLocation, ? extends Area> getArea(final CommandContext<CommandSourceStack> context, final String name) throws CommandSyntaxException {
        var resourceLocation = context.getArgument(name, ResourceLocation.class);
        var savedData = AreaSavedData.getServerData(context.getSource().getServer());

        if (savedData.has(resourceLocation)) {
            return new Pair<>(resourceLocation, savedData.get(resourceLocation));
        }

        throw ERROR_UNKNOWN_AREA.create(resourceLocation);
    }
}
