package dev.doublekekse.area_lib.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.ArrayList;
import java.util.List;
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
        return argumentType.listSuggestions(context, builder);
    }

    public static <T> ListArgument<T> list(ArgumentType<T> argumentType) {
        return new ListArgument<>(argumentType);
    }

    public static <T> List<T> getList(CommandContext<?> context, String name) {
        //noinspection unchecked
        return (List<T>) context.getArgument(name, List.class);
    }
}
