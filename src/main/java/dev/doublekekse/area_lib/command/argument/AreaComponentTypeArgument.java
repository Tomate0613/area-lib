package dev.doublekekse.area_lib.command.argument;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.doublekekse.area_lib.component.AreaComponentType;
import dev.doublekekse.area_lib.registry.AreaComponentRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.suggestions.SuggestionContext;
import net.minecraft.commands.suggestions.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class AreaComponentTypeArgument {
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_COMPONENT_TYPE = new DynamicCommandExceptionType((type) -> Component.translatableEscape("area_lib.commands.area.error_component_type_does_not_exist", type));

    public static SuggestionProvider<CommandSourceStack> SUGGESTIONS = SuggestionProviders.serverOnly(((_, _, builder) -> {
        return SuggestionContext.suggest(AreaComponentRegistry.getTypes().stream().map(
            (type) -> type.id().toString()
        ), builder);
    }));

    public static SuggestionProvider<CommandSourceStack> PRESENT_SUGGESTIONS = SuggestionProviders.serverOnly(((server, context, builder) -> {
        var area = AreaArgument.getArea(server, context, "id");

        return SuggestionContext.suggest(AreaComponentRegistry.getTypes()
            .stream()
            .filter(area::has)
            .map(type -> type.id().toString()), builder);
    }));

    public static AreaComponentType<?> getComponentType(final CommandContext<CommandSourceStack> context, final String name) throws CommandSyntaxException {
        var identifier = context.getArgument(name, Identifier.class);
        var type = AreaComponentRegistry.get(identifier);

        if (type == null) {
            throw ERROR_UNKNOWN_COMPONENT_TYPE.create(identifier);
        }

        return type;
    }
}
