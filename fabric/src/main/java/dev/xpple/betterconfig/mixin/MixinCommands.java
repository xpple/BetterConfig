package dev.xpple.betterconfig.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import dev.xpple.betterconfig.util.WrappedArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(Commands.class)
public abstract class MixinCommands {
    @Inject(method = "fillUsableCommands", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;getSuggestionsProvider()Lcom/mojang/brigadier/suggestion/SuggestionProvider;", ordinal = 0, remap = false))
    private <S> void replace(CommandNode<CommandSourceStack> rootCommandSource, CommandNode<SharedSuggestionProvider> rootSuggestion, CommandSourceStack source, Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> commandNodeToSuggestionNode, CallbackInfo ci, @Local RequiredArgumentBuilder<S, ?> requiredArgumentBuilder) {
        if (requiredArgumentBuilder.getType() instanceof WrappedArgumentType<?, ?> wrappedArgumentType) {
            ((RequiredArgumentBuilderAccessor) requiredArgumentBuilder).setSuggestionsProvider(wrappedArgumentType::listSuggestions);
            ((RequiredArgumentBuilderAccessor) requiredArgumentBuilder).setType(wrappedArgumentType.getNativeType());
        }
    }
}
