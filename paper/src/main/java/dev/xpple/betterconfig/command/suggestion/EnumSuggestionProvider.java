package dev.xpple.betterconfig.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class EnumSuggestionProvider<S, T extends Enum<T>> extends AbstractEnumSuggestionProvider<S, T> {

    public EnumSuggestionProvider(Class<T> enumClass) {
        super(enumClass);
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SuggestionProviderHelper.suggestMatching(Arrays.stream(this.enumClass.getEnumConstants()).map(Enum::name), builder);
    }
}
