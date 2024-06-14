package dev.xpple.betterconfig.api;

import dev.xpple.betterconfig.impl.BetterConfigImpl;

public interface BetterConfigAPI {

    static BetterConfigAPI getInstance() {
        return BetterConfigImpl.INSTANCE;
    }

    /**
     * Get the configurations for the specified plugin.
     * @param pluginName the plugin's name
     * @return the configurations for the specified plugin
     */
    PluginConfig getPluginConfig(String pluginName);
}
