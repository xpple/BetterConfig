package dev.xpple.betterconfig.api;

import dev.xpple.betterconfig.impl.BetterConfigImpl;
import dev.xpple.betterconfig.impl.BetterConfigInternals;
import dev.xpple.betterconfig.impl.ModConfigImpl;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;

public class ModConfigBuilder extends AbstractConfigBuilder<SharedSuggestionProvider, CommandBuildContext> {

    private final String modId;

    public ModConfigBuilder(String modId, Class<?> configsClass) {
        super(configsClass);
        this.modId = modId;
    }

    @Override
    public void build() {
        ModConfigImpl<?> modConfig = new ModConfigImpl<>(this.modId, this.configsClass, this.builder.create(), this.arguments, this.suggestors);
        if (BetterConfigImpl.getModConfigs().putIfAbsent(this.modId, modConfig) == null) {
            BetterConfigInternals.init(modConfig);
            return;
        }
        throw new IllegalArgumentException(this.modId);
    }
}
