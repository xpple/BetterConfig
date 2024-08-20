package dev.xpple.betterconfig.impl;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.xpple.betterconfig.BetterConfig;
import dev.xpple.betterconfig.command.suggestion.SuggestionProviderHelper;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;

import java.nio.file.Path;
import java.util.Arrays;

public class PaperPlatform implements Platform {
    private static final DynamicCommandExceptionType INVALID_ENUM_EXCEPTION = new DynamicCommandExceptionType(value -> MessageComponentSerializer.message().serialize(Component.translatable("argument.enum.invalid").arguments(Component.text(String.valueOf(value)))));

    @Override
    public Path getConfigsPath(String modId) {
        return BetterConfig.PLUGIN_PATH.resolve(modId).resolve("config.json");
    }

    @Override
    public Class<?> getCommandSourceClass() {
        return CommandSourceStack.class;
    }

    @Override
    public DynamicCommandExceptionType invalidEnumException() {
        return INVALID_ENUM_EXCEPTION;
    }

    @Override
    public <S, T extends Enum<T>> SuggestionProvider<S> enumSuggestionProvider(Class<T> type) {
        return (context, builder) -> SuggestionProviderHelper.suggestMatching(Arrays.stream(type.getEnumConstants()).map(Enum::name), builder);
    }
}
