package dev.xpple.betterconfig.impl;

import dev.xpple.betterconfig.api.BetterConfigAPI;
import dev.xpple.betterconfig.api.PluginConfig;

import java.util.HashMap;
import java.util.Map;

public class BetterConfigImpl implements BetterConfigAPI {

    private static final Map<String, PluginConfigImpl> pluginConfigs = new HashMap<>();

    @Override
    public PluginConfig getPluginConfig(String pluginName) {
        PluginConfig pluginConfig = pluginConfigs.get(pluginName);
        if (pluginConfig == null) {
            throw new IllegalArgumentException(pluginName);
        }
        return pluginConfig;
    }

    public static Map<String, PluginConfigImpl> getPluginConfigs() {
        return pluginConfigs;
    }

    public static final BetterConfigImpl INSTANCE = new BetterConfigImpl();
}
