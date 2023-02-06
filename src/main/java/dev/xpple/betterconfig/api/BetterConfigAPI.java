package dev.xpple.betterconfig.api;

import dev.xpple.betterconfig.impl.BetterConfigImpl;

public interface BetterConfigAPI {

    static BetterConfigAPI getInstance() {
        return BetterConfigImpl.INSTANCE;
    }

    /**
     * Get the configurations for the specified mod.
     * @param modId the mod's identifier
     * @return the configurations for the specified mod
     */
    ModConfig getModConfig(String modId);
}
