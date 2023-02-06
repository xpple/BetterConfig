package dev.xpple.betterconfig.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.util.CheckedBiFunction;
import net.minecraft.command.CommandSource;

@FunctionalInterface
public interface CommandContextBiFunction<T> extends CheckedBiFunction<CommandContext<? extends CommandSource>, String, T, CommandSyntaxException> {
}
