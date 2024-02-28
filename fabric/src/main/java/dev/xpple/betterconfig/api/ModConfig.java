package dev.xpple.betterconfig.api;

public interface ModConfig extends AbstractConfig {
    /**
     * Get the identifier of the mod of this configuration.
     * @return the mod's identifier
     */
    String getModId();
}
