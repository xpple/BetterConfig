package dev.xpple.betterconfig.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class EnumSuggestionProvider<T extends Enum<T>> implements SuggestionProvider<CommandSource> {

    private final Class<T> enumClass;

    public EnumSuggestionProvider(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(Arrays.stream(this.enumClass.getEnumConstants()).map(Enum::name), builder);
    }
}
