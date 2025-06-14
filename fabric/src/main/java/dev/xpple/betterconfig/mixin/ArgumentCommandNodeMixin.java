package dev.xpple.betterconfig.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import dev.xpple.betterconfig.util.WrappedArgumentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ArgumentCommandNode.class, remap = false)
public abstract class ArgumentCommandNodeMixin {
    @Inject(method = "createBuilder()Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;suggests(Lcom/mojang/brigadier/suggestion/SuggestionProvider;)Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;", remap = false, shift = At.Shift.AFTER))
    private static <S, T> void replace(CallbackInfoReturnable<RequiredArgumentBuilder<S, T>> cir, @Local RequiredArgumentBuilder<S, ?> builder) {
        if (builder.getType() instanceof WrappedArgumentType<?, ?> wrappedArgumentType) {
            ((RequiredArgumentBuilderAccessor) builder).setSuggestionsProvider(wrappedArgumentType::listSuggestions);
            ((RequiredArgumentBuilderAccessor) builder).setType(wrappedArgumentType.getNativeType());
        }
    }
}
