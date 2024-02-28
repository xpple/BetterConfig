package dev.xpple.betterconfig.impl;

import com.google.gson.Gson;
import com.mojang.brigadier.arguments.ArgumentType;
import dev.xpple.betterconfig.BetterConfig;
import dev.xpple.betterconfig.api.PluginConfig;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

public class PluginConfigImpl extends AbstractConfigImpl<CommandSourceStack, Void> implements PluginConfig {

    private final String pluginName;

    public PluginConfigImpl(String pluginName, Class<?> configsClass, Gson gson, Map<Class<?>, Function<Void, ? extends ArgumentType<?>>> arguments) {
        super(configsClass, gson, arguments);
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
