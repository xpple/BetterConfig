package dev.xpple.betterconfig.impl;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.util.CheckedBiConsumer;

public interface CommandContextBiConsumer<T, U> extends CheckedBiConsumer<T, U, CommandSyntaxException> {
}
