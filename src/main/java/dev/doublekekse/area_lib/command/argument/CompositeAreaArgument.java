package dev.doublekekse.area_lib.command.argument;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.doublekekse.area_lib.areas.CompositeArea;
import dev.doublekekse.area_lib.data.AreaClientData;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class CompositeAreaArgument extends AreaArgument {
    public static final DynamicCommandExceptionType ERROR_NOT_COMPOSITE_AREA = new DynamicCommandExceptionType((object) -> Component.translatableEscape("area_lib.commands.area.error_is_not_composite", object));

    public static CompositeArea getArea(final CommandContext<CommandSourceStack> context, final String name) throws CommandSyntaxException {
        var resourceLocation = context.getArgument(name, ResourceLocation.class);
        var savedData = AreaSavedData.getServerData(context.getSource().getServer());

        if (!savedData.has(resourceLocation)) {
            throw ERROR_UNKNOWN_AREA.create(resourceLocation);
        }

        var area = savedData.get(resourceLocation);

        if (area instanceof CompositeArea compositeArea) {
            return compositeArea;
        }

        throw ERROR_NOT_COMPOSITE_AREA.create(resourceLocation);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (context.getSource() instanceof ClientSuggestionProvider) {
            var savedData = AreaClientData.getClientLevelData();

            return SharedSuggestionProvider.suggest(savedData.getAreaEntries().stream().filter(entry -> entry.getValue() instanceof CompositeArea).map(
                (entry) -> entry.getKey().toString()
            ), builder);
        }

        return Suggestions.empty();
    }

    public static CompositeAreaArgument area() {
        return new CompositeAreaArgument();
    }
}
