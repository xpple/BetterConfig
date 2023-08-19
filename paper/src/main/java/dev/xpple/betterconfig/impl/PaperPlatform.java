package dev.xpple.betterconfig.impl;

import io.papermc.paper.command.brigadier.CommandSourceStack;

public class PaperPlatform implements Platform {
    @Override
    public Class<?> getCommandSourceClass() {
        return CommandSourceStack.class;
    }
}
