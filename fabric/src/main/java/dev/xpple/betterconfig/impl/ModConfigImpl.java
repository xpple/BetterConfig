package dev.xpple.betterconfig.impl;

import com.google.gson.Gson;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.xpple.betterconfig.BetterConfig;
import dev.xpple.betterconfig.api.ModConfig;
import dev.xpple.betterconfig.util.CheckedBiFunction;
import dev.xpple.betterconfig.util.Pair;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

public class ModConfigImpl<S extends SharedSuggestionProvider> extends AbstractConfigImpl<S, CommandBuildContext> implements ModConfig {

    private final String modId;

    public ModConfigImpl(String modId, Class<?> configsClass, Gson gson, Map<Class<?>, Function<CommandBuildContext, ? extends ArgumentType<?>>> arguments, Map<Class<?>, Pair<SuggestionProvider<? extends S>, CheckedBiFunction<CommandContext<? extends S>, String, ?, CommandSyntaxException>>> suggestors) {
        super(configsClass, gson, arguments, suggestors);
        this.modId = modId;
    }

    @Override
    public String getModId() {
        return this.modId;
    }

    @Override
    public Path getConfigsPath() {
        return BetterConfig.MOD_PATH.resolve(this.modId).resolve("config.json");
    }

    @Override
    public String getIdentifier() {
        return this.getModId();
    }
}
