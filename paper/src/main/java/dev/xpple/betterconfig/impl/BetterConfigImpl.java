package dev.xpple.betterconfig.impl;

import dev.xpple.betterconfig.api.BetterConfigAPI;
import net.kyori.adventure.text.Component;

public class BetterConfigImpl extends AbstractBetterConfigImpl<Component> implements BetterConfigAPI {
    public static final AbstractBetterConfigImpl<?> INSTANCE = new BetterConfigImpl();
}
