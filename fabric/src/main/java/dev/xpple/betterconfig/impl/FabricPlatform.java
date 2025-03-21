package dev.xpple.betterconfig.impl;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.xpple.betterconfig.BetterConfig;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Arrays;

public class FabricPlatform implements Platform {
    private static final DynamicCommandExceptionType INVALID_ENUM_EXCEPTION = new DynamicCommandExceptionType(value -> Component.translatable("argument.enum.invalid", value));

    @Override
    public Path getConfigsPath(String modId) {
        return BetterConfig.MOD_PATH.resolve(modId).resolve("config.json");
    }

    @Override
    public Class<?> getCommandSourceClass() {
        return SharedSuggestionProvider.class;
    }

    @Override
    public DynamicCommandExceptionType invalidEnumException() {
        return INVALID_ENUM_EXCEPTION;
    }

    @Override
    public <S, T extends Enum<T>> SuggestionProvider<S> enumSuggestionProvider(Class<T> type) {
        return (context, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(type.getEnumConstants()).map(Enum::name), builder);
    }

    @Override
    public Class<?> getComponentClass() {
        return Component.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <P> P stringToComponent(@Nullable String string) {
        return (P) Component.nullToEmpty(string);
    }
}
