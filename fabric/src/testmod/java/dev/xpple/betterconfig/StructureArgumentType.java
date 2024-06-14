package dev.xpple.betterconfig;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.xpple.betterconfig.util.WrappedArgumentType;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.concurrent.CompletableFuture;

public class StructureArgumentType extends WrappedArgumentType.Converted<StructureType<?>, ResourceLocation> {

    private static final DynamicCommandExceptionType INVALID_STRUCTURE_ID_EXCEPTION = new DynamicCommandExceptionType(id -> Component.translatable("structure_block.invalid_structure_name", id));

    private StructureArgumentType() {
        super(ResourceLocationArgument.id());
    }

    public static StructureArgumentType structure() {
        return new StructureArgumentType();
    }

    @Override
    public StructureType<?> convert(ResourceLocation key) throws CommandSyntaxException {
        if (!BuiltInRegistries.STRUCTURE_TYPE.containsKey(key)) {
            throw INVALID_STRUCTURE_ID_EXCEPTION.create(Component.translationArg(key));
        }
        return BuiltInRegistries.STRUCTURE_TYPE.get(key);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(BuiltInRegistries.STRUCTURE_TYPE.keySet(), builder);
    }
}
