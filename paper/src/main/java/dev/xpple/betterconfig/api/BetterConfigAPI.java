package dev.xpple.betterconfig.api;

import dev.xpple.betterconfig.impl.BetterConfigImpl;
import net.kyori.adventure.text.Component;

public interface BetterConfigAPI extends AbstractBetterConfigAPI<Component> {
    /**
     * Get the API instance.
     * @return the API instance.
     */
    static BetterConfigAPI getInstance() {
        return (BetterConfigAPI) BetterConfigImpl.INSTANCE;
    }

    @Override
    ModConfig<Component> getModConfig(String modId);
}
