package dev.xpple.betterconfig.impl;

import dev.xpple.betterconfig.api.BetterConfigAPI;
import net.minecraft.network.chat.Component;

public class BetterConfigImpl extends AbstractBetterConfigImpl<Component> implements BetterConfigAPI {
    public static final BetterConfigImpl INSTANCE = new BetterConfigImpl();
}
