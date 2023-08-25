package dev.xpple.betterconfig.api;

import dev.xpple.betterconfig.impl.BetterConfigImpl;
import dev.xpple.betterconfig.impl.BetterConfigInternals;
import dev.xpple.betterconfig.impl.PluginConfigImpl;
import io.papermc.paper.command.brigadier.CommandSourceStack;

public class PluginConfigBuilder extends AbstractConfigBuilder<CommandSourceStack, Void> {

    private final String pluginName;

    public PluginConfigBuilder(String pluginName, Class<?> configsClass) {
        super(configsClass);
        this.pluginName = pluginName;
    }

    @Override
    public void build() {
        PluginConfigImpl pluginConfig = new PluginConfigImpl(this.pluginName, this.configsClass, this.builder.create(), this.arguments, this.suggestors);
        if (BetterConfigImpl.getPluginConfigs().putIfAbsent(this.pluginName, pluginConfig) == null) {
            BetterConfigInternals.init(pluginConfig);
            return;
        }
        throw new IllegalArgumentException(this.pluginName);
    }
}
