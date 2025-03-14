package dev.xpple.betterconfig.impl;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ServiceLoader;

public interface Platform {
    Path getConfigsPath(String modId);

    Class<?> getCommandSourceClass();

    DynamicCommandExceptionType invalidEnumException();

    <S, T extends Enum<T>> SuggestionProvider<S> enumSuggestionProvider(Class<T> type);

    Class<?> getComponentClass();

    <P> P stringToComponent(@Nullable String string);

    Platform current = ServiceLoader.load(Platform.class, Platform.class.getClassLoader()).iterator().next();
}
