package dev.xpple.betterconfig.impl;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.util.CheckedConsumer;

public interface CommandContextConsumer<T> extends CheckedConsumer<T, CommandSyntaxException> {
}
