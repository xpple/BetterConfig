package dev.xpple.betterconfig.impl;

import com.google.gson.Gson;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.xpple.betterconfig.BetterConfig;
import dev.xpple.betterconfig.api.PluginConfig;
import dev.xpple.betterconfig.util.CheckedBiFunction;
import dev.xpple.betterconfig.util.Pair;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

public class PluginConfigImpl extends AbstractConfigImpl<CommandSourceStack, Void> implements PluginConfig {

    private final String pluginName;

    public PluginConfigImpl(String pluginName, Class<?> configsClass, Gson gson, Map<Class<?>, Function<Void, ? extends ArgumentType<?>>> arguments, Map<Class<?>, Pair<SuggestionProvider<? extends CommandSourceStack>, CheckedBiFunction<CommandContext<? extends CommandSourceStack>, String, ?, CommandSyntaxException>>> suggestors) {
        super(configsClass, gson, arguments, suggestors);
        this.pluginName = pluginName;
    }

    @Override
    public String getPluginName() {
        return this.pluginName;
    }

    @Override
    public Path getConfigsPath() {
        return BetterConfig.PLUGIN_PATH.resolve(this.pluginName).resolve("config.json");
    }

    @Override
    public String getIdentifier() {
        return this.getPluginName();
    }
}
