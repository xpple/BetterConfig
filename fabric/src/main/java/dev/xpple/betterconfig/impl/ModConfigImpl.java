package dev.xpple.betterconfig.impl;

import com.google.gson.Gson;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.xpple.betterconfig.api.ModConfig;
import dev.xpple.betterconfig.util.CheckedBiFunction;
import dev.xpple.betterconfig.util.Pair;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

import static dev.xpple.betterconfig.BetterConfig.MOD_PATH;

public class ModConfigImpl<S extends CommandSource> extends AbstractConfigImpl<S, CommandRegistryAccess> implements ModConfig {

    private final String modId;

    public ModConfigImpl(String modId, Class<?> configsClass, Gson gson, Map<Class<?>, Function<CommandRegistryAccess, ? extends ArgumentType<?>>> arguments, Map<Class<?>, Pair<SuggestionProvider<? extends S>, CheckedBiFunction<CommandContext<? extends S>, String, ?, CommandSyntaxException>>> suggestors) {
        super(configsClass, gson, arguments, suggestors);
        this.modId = modId;
    }

    @Override
    public String getModId() {
        return this.modId;
    }

    @Override
    public Path getConfigsPath() {
        return MOD_PATH.resolve(this.modId).resolve("config.json");
    }

    @Override
    public String getIdentifier() {
        return this.getModId();
    }
}
