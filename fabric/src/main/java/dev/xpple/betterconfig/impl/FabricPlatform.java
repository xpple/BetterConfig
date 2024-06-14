package dev.xpple.betterconfig.impl;

import net.minecraft.commands.SharedSuggestionProvider;

public class FabricPlatform implements Platform {
    @Override
    public Class<?> getCommandSourceClass() {
        return SharedSuggestionProvider.class;
    }
}
