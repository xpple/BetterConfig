package dev.xpple.betterconfig.impl;

import dev.xpple.betterconfig.api.AbstractBetterConfigAPI;
import dev.xpple.betterconfig.api.ModConfig;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractBetterConfigImpl<P> implements AbstractBetterConfigAPI<P> {

    private static final Map<String, ModConfigImpl<?, ?, ?>> modConfigs = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public ModConfig<P> getModConfig(String modId) {
        ModConfig<P> modConfig = (ModConfig<P>) modConfigs.get(modId);
        if (modConfig == null) {
            throw new IllegalArgumentException(modId);
        }
        return modConfig;
    }

    public static Map<String, ModConfigImpl<?, ?, ?>> getModConfigs() {
        return modConfigs;
    }
}
