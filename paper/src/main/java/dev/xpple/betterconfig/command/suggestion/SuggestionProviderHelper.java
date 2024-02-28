package dev.xpple.betterconfig.command.suggestion;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.bukkit.NamespacedKey;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class SuggestionProviderHelper {
    public static CompletableFuture<Suggestions> suggestMatching(Stream<String> candidates, SuggestionsBuilder builder) {
        String string = builder.getRemaining().toLowerCase(Locale.ROOT);
        Stream<String> suggestions = candidates.filter(candidate -> shouldSuggest(string, candidate.toLowerCase(Locale.ROOT)));
        suggestions.forEach(builder::suggest);
        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggestNamespacedKeys(Stream<NamespacedKey> candidates, SuggestionsBuilder builder) {
        return suggestNamespacedKeys(candidates::iterator, builder);
    }

    public static CompletableFuture<Suggestions> suggestNamespacedKeys(Iterable<NamespacedKey> candidates, SuggestionsBuilder builder) {
        String string = builder.getRemaining().toLowerCase(Locale.ROOT);
        forEachMatching(candidates, string, key -> key, key -> builder.suggest(key.toString()));
        return builder.buildFuture();
    }

    private static <T> void forEachMatching(Iterable<T> candidates, String remaining, Function<T, NamespacedKey> namespacedKeyFunction, Consumer<T> action) {
        boolean hasNamespace = remaining.indexOf(':') > -1;

        for (T candidate : candidates) {
            NamespacedKey namespacedKey = namespacedKeyFunction.apply(candidate);
            if (hasNamespace) {
                String string = namespacedKey.toString();
                if (shouldSuggest(remaining, string)) {
                    action.accept(candidate);
                }
            } else if (shouldSuggest(remaining, namespacedKey.getNamespace()) || namespacedKey.getNamespace().equals("minecraft") && shouldSuggest(remaining, namespacedKey.getKey())) {
                action.accept(candidate);
            }
        }
    }

    private static boolean shouldSuggest(String remaining, String candidate) {
        for (int i = 0; !candidate.startsWith(remaining, i); ++i) {
            i = candidate.indexOf('_', i);
            if (i < 0) {
                return false;
            }
        }
        return true;
    }
}
