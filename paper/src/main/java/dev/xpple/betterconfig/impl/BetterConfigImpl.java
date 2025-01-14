package dev.xpple.betterconfig.impl;

import dev.xpple.betterconfig.api.AbstractBetterConfigAPI;
import net.kyori.adventure.text.Component;

public class BetterConfigImpl extends AbstractBetterConfigImpl<Component> implements AbstractBetterConfigAPI<Component> {
    public static final AbstractBetterConfigImpl<?> INSTANCE = new BetterConfigImpl();
}
