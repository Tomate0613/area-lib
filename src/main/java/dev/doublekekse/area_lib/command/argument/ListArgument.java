package dev.doublekekse.area_lib.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.doublekekse.area_lib.AreaLib;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class ListArgument<T> implements ArgumentType<List<T>> {
    final ArgumentType<T> argumentType;

    ListArgument(ArgumentType<T> argumentType) {
        this.argumentType = argumentType;
    }

    @Override
    public List<T> parse(StringReader reader) throws CommandSyntaxException {
        var list = new ArrayList<T>();

        while (reader.canRead()) {
            var result = argumentType.parse(reader);
            list.add(result);

            if (reader.canRead() && reader.peek() == ' ') {
                reader.read();
            }
        }


        return list;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        var input = builder.getInput();
        var lastSpace = input.lastIndexOf(' ');

        var builderFromLastSpace = new SuggestionsBuilder(input, lastSpace + 1);
        return argumentType.listSuggestions(context, builderFromLastSpace);
    }

    public static <T> ListArgument<T> create(ArgumentType<T> argumentType) {
        var listArgument = new ListArgument<>(argumentType);

        ArgumentTypeRegistry.registerArgumentType(AreaLib.id("list_" + argumentType.getClass().getName().toLowerCase(Locale.ROOT)), ListArgument.class, SingletonArgumentInfo.contextFree(() -> listArgument));

        return listArgument;
    }

    public List<T> getList(CommandContext<?> context, String name) {
        //noinspection unchecked
        return context.getArgument(name, List.class);
    }
}
