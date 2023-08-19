package dev.xpple.betterconfig.impl;

import net.minecraft.command.CommandSource;

public class FabricPlatform implements Platform {
    @Override
    public Class<?> getCommandSourceClass() {
        return CommandSource.class;
    }
}
