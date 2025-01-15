package dev.xpple.betterconfig.api;

import org.jetbrains.annotations.ApiStatus;

/**
 * @param <P> the chat component type: {@link net.minecraft.network.chat.Component} on Fabric
 *           and {@link net.kyori.adventure.text.Component} on Paper
 */
@ApiStatus.Internal
public interface AbstractBetterConfigAPI<P> {
    /**
     * Get the configurations for the specified mod.
     * @param modId the mod's identifier
     * @return the configurations for the specified mod
     */
    ModConfig<P> getModConfig(String modId);
}
