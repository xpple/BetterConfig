package dev.xpple.betterconfig.api;

import org.jetbrains.annotations.ApiStatus;

/**
 * An event object that is created whenever any config value is updated.
 * @param config the name of the config that was changed
 * @param oldValue the config's old value
 * @param newValue the config's new value
 */
public record GlobalChangeEvent(String config, Object oldValue, Object newValue) {
    @ApiStatus.Internal
    public GlobalChangeEvent {
    }
}
