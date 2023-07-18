package dev.xpple.betterconfig.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class EnumSuggestionProvider<S, T extends Enum<T>> extends AbstractEnumSuggestionProvider<S, T> {

    public EnumSuggestionProvider(Class<T> enumClass) {
        super(enumClass);
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return suggestMatching(Arrays.stream(this.enumClass.getEnumConstants()).map(Enum::name), builder);
    }

    private static CompletableFuture<Suggestions> suggestMatching(Stream<String> candidates, SuggestionsBuilder builder) {
        String string = builder.getRemaining().toLowerCase(Locale.ROOT);
        Stream<String> suggestions = candidates.filter(candidate -> shouldSuggest(string, candidate.toLowerCase(Locale.ROOT)));
        suggestions.forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static boolean shouldSuggest(String remaining, String candidate) {
        for (int i = 0; !candidate.startsWith(remaining, i); ++i) {
            i = candidate.indexOf(95, i);
            if (i < 0) {
                return false;
            }
        }
        return true;
    }
}
