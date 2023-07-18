package dev.xpple.betterconfig.api;

public interface PluginConfig extends AbstractConfig {
    /**
     * Get the name of the plugin of this configuration.
     * @return the plugin's name
     */
    String getPluginName();
}
