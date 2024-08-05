package dev.xpple.betterconfig;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.xpple.betterconfig.command.suggestion.SuggestionProviderHelper;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class BlockMaterialArgumentType implements CustomArgumentType.Converted<Material, BlockState> {

    private static final Set<NamespacedKey> BLOCKS = Arrays.stream(Material.values())
        .filter(Material::isBlock)
        .map(Material::getKey)
        .collect(Collectors.toUnmodifiableSet());

    @Override
    public @NotNull ArgumentType<BlockState> getNativeType() {
        return ArgumentTypes.blockState();
    }

    public static BlockMaterialArgumentType block() {
        return new BlockMaterialArgumentType();
    }

    @Override
    public @NotNull Material convert(@NotNull BlockState state) {
        return state.getType();
    }

    @Override
    public @NotNull <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        return SuggestionProviderHelper.suggestNamespacedKeys(BLOCKS, builder);
    }
}
