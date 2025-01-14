package dev.xpple.betterconfig.impl;

import dev.xpple.betterconfig.api.AbstractBetterConfigAPI;
import org.apache.logging.log4j.core.config.builder.api.Component;

public class BetterConfigImpl extends AbstractBetterConfigImpl<Component> implements AbstractBetterConfigAPI<Component> {
    public static final AbstractBetterConfigImpl<?> INSTANCE = new BetterConfigImpl();
}
