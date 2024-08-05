package dev.xpple.betterconfig.mixin;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = RequiredArgumentBuilder.class, remap = false)
public interface RequiredArgumentBuilderAccessor {
    @Mutable
    @Accessor
    void setType(ArgumentType<?> type);

    @Accessor
    void setSuggestionsProvider(SuggestionProvider<?> suggestionsProvider);
}
