package dev.xpple.betterconfig.api;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.nio.file.Path;

public interface ModConfig {
    /**
     * Get the identifier of the mod of this configuration.
     * @return the mod's identifier
     */
    String getModId();

    /**
     * Get the class where all the configurations for this mod are defined.
     * @return the class with all configurations
     */
    Class<?> getConfigsClass();

    /**
     * Get the path where the configurations for this mod are stored.
     * @return the path
     */
    Path getConfigsPath();

    /**
     * Get a config value based on the key.
     * @param config the config's key
     * @return the config value
     */
    Object get(String config);

    /**
     * Get the string representation for this config key.
     * @param config the config's key
     * @return the string representation
     */
    String asString(String config);

    /**
     * Set the value for the config associated with this config key.
     * @param config the config's key
     * @param value the new value
     * @throws IllegalArgumentException when there is no setter for this config
     * @throws CommandSyntaxException when a custom setter has failed to execute
     */
    void set(String config, Object value) throws CommandSyntaxException;

    /**
     * Add a value to the config associated with this config key.
     * @param config the config's key
     * @param value the value
     * @throws IllegalArgumentException when there is no adder for this config
     * @throws CommandSyntaxException when a custom adder has failed to execute
     */
    void add(String config, Object value) throws CommandSyntaxException;

    /**
     * Put a new mapping to the config associated with this config key.
     * @param config the config's key
     * @param key the mapping's key
     * @param value the mapping's value
     * @throws IllegalArgumentException when there is no putter for this config
     * @throws CommandSyntaxException when a custom putter has failed to execute
     */
    void put(String config, Object key, Object value) throws CommandSyntaxException;

    /**
     * Remove a value from the config associated with this config key.
     * @param config the config's key
     * @param value the value
     * @throws IllegalArgumentException when there is no remover for this config
     * @throws CommandSyntaxException when a custom remover has failed to execute
     */
    void remove(String config, Object value) throws CommandSyntaxException;

    /**
     * Save this configuration.
     * @return {@code true} if the configuration was successfully saved, {@code false} otherwise
     */
    boolean save();
}
