package dev.doublekekse.area_lib.command.argument;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.HexColorArgument;
import net.minecraft.util.ARGB;

public class ARGBColorArgument {
    public static int getColor(final CommandContext<CommandSourceStack> context, final String name) throws CommandSyntaxException {
        var string = context.getArgument(name, String.class);

        try {
            return switch (string.length()) {
                case 3 -> ARGB.color(duplicateDigit(Integer.parseInt(string, 0, 1, 16)), duplicateDigit(Integer.parseInt(string, 1, 2, 16)), duplicateDigit(Integer.parseInt(string, 2, 3, 16)));
                case 6 -> ARGB.color(Integer.parseInt(string, 0, 2, 16), Integer.parseInt(string, 2, 4, 16), Integer.parseInt(string, 4, 6, 16));
                case 8 -> ARGB.color(Integer.parseInt(string, 0, 2, 16), Integer.parseInt(string, 2, 4, 16), Integer.parseInt(string, 4, 6, 16), Integer.parseInt(string, 6, 8, 16));
                default -> throw HexColorArgument.ERROR_INVALID_HEX.create(string);
            };
        } catch (Exception e) {
            throw HexColorArgument.ERROR_INVALID_HEX.create(string);
        }
    }

    private static int duplicateDigit(int i) {
        return i * 17;
    }
}
