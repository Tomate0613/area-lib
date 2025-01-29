package dev.doublekekse.area_lib.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.network.FriendlyByteBuf;

public class ListArgumentTypeInfo<T> implements ArgumentTypeInfo<ListArgument<T>, ListArgumentTypeInfo.Template<T>> {
    @Override
    public void serializeToNetwork(Template<T> template, FriendlyByteBuf friendlyByteBuf) {
        ArgumentTypeInfos.byClass(template.argumentType).serializeToNetwork(template.argumentType.teplate, friendlyByteBuf);
        //template.argumentType
    }

    @Override
    public Template<T> deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return null;
    }

    @Override
    public Template<T> unpack(ListArgument<T> argumentType) {
        return null;
    }

    @Override
    public void serializeToJson(Template<T> template, JsonObject jsonObject) {

    }

    public static class Template<T> implements ArgumentTypeInfo.Template<ListArgument<T>> {
        ArgumentType<T> argumentType;

        public Template(ArgumentType<T> argumentType) {
            this.argumentType = argumentType;
        }

        @Override
        public ListArgument<T> instantiate(CommandBuildContext commandBuildContext) {
            return null;
        }

        @Override
        public ArgumentTypeInfo<ListArgument<T>, ?> type() {
            return null;
        }
    }
}
