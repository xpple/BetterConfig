package dev.xpple.betterconfig;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.xpple.betterconfig.util.WrappedArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class BlockWrappedArgumentType extends WrappedArgumentType<Block, BlockPredicateArgument.Result> {

    private static final DynamicCommandExceptionType INVALID_BLOCK_ID_EXCEPTION = new DynamicCommandExceptionType(id -> Component.translatable("argument.block.id.invalid", id));

    private BlockWrappedArgumentType(CommandBuildContext buildContext) {
        super(BlockPredicateArgument.blockPredicate(buildContext));
    }

    public static BlockWrappedArgumentType block(CommandBuildContext buildContext) {
        return new BlockWrappedArgumentType(buildContext);
    }

    @Override
    public Block parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        ResourceLocation key = ResourceLocation.read(reader);
        CommandSyntaxException blockNotFound = INVALID_BLOCK_ID_EXCEPTION.create(Component.translationArg(key));
        if (!BuiltInRegistries.BLOCK.containsKey(key)) {
            reader.setCursor(cursor);
            throw blockNotFound;
        }
        return BuiltInRegistries.BLOCK.getOptional(key).orElseThrow(() -> blockNotFound);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(BuiltInRegistries.BLOCK.keySet(), builder);
    }
}
