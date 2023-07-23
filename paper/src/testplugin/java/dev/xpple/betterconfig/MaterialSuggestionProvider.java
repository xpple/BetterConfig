package dev.xpple.betterconfig;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.xpple.betterconfig.command.suggestion.SuggestionProviderHelper;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

class MaterialSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        Stream<NamespacedKey> keyStream = StreamSupport.stream(Registry.MATERIAL.spliterator(), false).map(Material::getKey);
        return SuggestionProviderHelper.suggestNamespacedKeys(keyStream, builder);
    }
}
