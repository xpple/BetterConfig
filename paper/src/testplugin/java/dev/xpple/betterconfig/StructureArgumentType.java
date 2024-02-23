package dev.xpple.betterconfig;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.xpple.betterconfig.command.suggestion.SuggestionProviderHelper;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.generator.structure.Structure;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

class StructureArgumentType extends CustomArgumentType.Converted<Structure, NamespacedKey> {

    private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(Component.translatable("argument.id.invalid")));

    private static final Set<NamespacedKey> STRUCTURES = StreamSupport.stream(Registry.STRUCTURE.spliterator(), false)
        .map(Keyed::getKey)
        .collect(Collectors.toUnmodifiableSet());

    private StructureArgumentType() {
        super(ArgumentTypes.namespacedKey());
    }

    public static StructureArgumentType structure() {
        return new StructureArgumentType();
    }

    @Override
    public @NotNull Structure convert(@NotNull NamespacedKey key) throws CommandSyntaxException {
        Structure structure = Registry.STRUCTURE.get(key);
        if (structure == null) {
            throw ERROR_INVALID.create();
        }
        return structure;
    }

    @Override
    public @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext context, @NotNull SuggestionsBuilder builder) {
        return SuggestionProviderHelper.suggestNamespacedKeys(STRUCTURES, builder);
    }
}
