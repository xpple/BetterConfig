package dev.xpple.betterconfig.api;

import dev.xpple.betterconfig.impl.BetterConfigImpl;

/**
 * @param <P> the chat component type: {@link net.minecraft.network.chat.Component} on Fabric
 *           and {@link net.kyori.adventure.text.Component} on Paper
 */
public interface BetterConfigAPI<P> {
    /**
     * Get the API instance.
     * @return the API instance.
     * @param <P> the chat component type: {@link net.minecraft.network.chat.Component}
     *           on Fabric and {@link net.kyori.adventure.text.Component} on Paper
     */
    static <P> BetterConfigAPI<P> getInstance() {
        return (BetterConfigAPI<P>) BetterConfigImpl.INSTANCE;
    }

    /**
     * Get the configurations for the specified mod.
     * @param modId the mod's identifier
     * @return the configurations for the specified mod
     */
    ModConfig<P> getModConfig(String modId);
}
