package dev.xpple.betterconfig;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

class BlockArgumentType implements ArgumentType<Block> {

    private BlockArgumentType() {
    }

    private static final Collection<String> EXAMPLES = Arrays.asList("stone", "command_block", "minecraft:emerald_ore");

    public static BlockArgumentType block() {
        return new BlockArgumentType();
    }

    public static Block getBlock(CommandContext<? extends SharedSuggestionProvider> context, String name) {
        return context.getArgument(name, Block.class);
    }

    @Override
    public Block parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        ResourceLocation key = ResourceLocation.read(reader);
        if (!BuiltInRegistries.BLOCK.containsKey(key)) {
            reader.setCursor(cursor);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
        }
        return BuiltInRegistries.BLOCK.get(key);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(BuiltInRegistries.BLOCK.keySet(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
