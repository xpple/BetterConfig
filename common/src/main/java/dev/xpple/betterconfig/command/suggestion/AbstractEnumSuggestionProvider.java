package dev.xpple.betterconfig.command.suggestion;

import com.mojang.brigadier.suggestion.SuggestionProvider;

public abstract class AbstractEnumSuggestionProvider<S, T extends Enum<T>> implements SuggestionProvider<S> {

    final Class<T> enumClass;

    public AbstractEnumSuggestionProvider(Class<T> enumClass) {
        this.enumClass = enumClass;
    }
}
