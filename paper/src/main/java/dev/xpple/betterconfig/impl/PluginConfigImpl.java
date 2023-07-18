package dev.xpple.betterconfig.impl;

import com.google.gson.Gson;
import dev.xpple.betterconfig.api.PluginConfig;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Path;
import java.util.Map;

import static dev.xpple.betterconfig.BetterConfig.PLUGIN_PATH;

public class PluginConfigImpl extends AbstractConfigImpl<CommandSourceStack> implements PluginConfig {

    private final String pluginName;

    public PluginConfigImpl(String pluginName, Class<?> configsClass, Gson gson, Map<Class<?>, Pair<?, ?>> arguments, Map<Class<?>, Pair<?, ?>> suggestors) {
        super(configsClass, gson, arguments, suggestors);
        this.pluginName = pluginName;
    }

    @Override
    public String getPluginName() {
        return this.pluginName;
    }

    @Override
    public Path getConfigsPath() {
        return PLUGIN_PATH.resolve(this.pluginName).resolve("config.json");
    }

    @Override
    public String getIdentifier() {
        return this.getPluginName();
    }
}
