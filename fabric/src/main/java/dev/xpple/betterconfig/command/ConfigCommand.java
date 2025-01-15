package dev.xpple.betterconfig.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.impl.AbstractBetterConfigImpl;
import dev.xpple.betterconfig.impl.ModConfigImpl;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class ConfigCommand extends AbstractConfigCommand<CommandSourceStack, CommandBuildContext, Component> {

    private ConfigCommand() {
        super("config");
    }

    @SuppressWarnings("unchecked")
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(new ConfigCommand().create(AbstractBetterConfigImpl.getModConfigs().values().stream().map(modConfig -> (ModConfigImpl<CommandSourceStack, CommandBuildContext, Component>) modConfig).toList(), buildContext).requires(source -> source.hasPermission(4)));
    }

    @Override
    protected int comment(CommandSourceStack source, String config, String comment) {
        source.sendSuccess(() -> Component.translatableWithFallback("betterconfig.commands.config.comment", "Comment for %s:", config), false);
        source.sendSuccess(() -> Component.literal(comment), false);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int get(CommandSourceStack source, ModConfigImpl<CommandSourceStack, CommandBuildContext, Component> modConfig, String config) {
        source.sendSuccess(() -> Component.translatableWithFallback("betterconfig.commands.config.get", "%s is currently set to %s.", config, modConfig.asComponent(config)), false);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int reset(CommandSourceStack source, ModConfigImpl<CommandSourceStack, CommandBuildContext, Component> modConfig, String config) {
        modConfig.reset(config);
        source.sendSuccess(() -> Component.translatableWithFallback("betterconfig.commands.config.reset", "%s has been reset to %s.", config, modConfig.asComponent(config)), true);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int set(CommandSourceStack source, ModConfigImpl<CommandSourceStack, CommandBuildContext, Component> modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.set(config, value);
        source.sendSuccess(() -> Component.translatableWithFallback("betterconfig.commands.config.set", "%s has been set to %s.", config, modConfig.asComponent(config)), true);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int add(CommandSourceStack source, ModConfigImpl<CommandSourceStack, CommandBuildContext, Component> modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.add(config, value);
        source.sendSuccess(() -> Component.translatableWithFallback("betterconfig.commands.config.add", "%s has been added to %s.", modConfig.asString(value), config), true);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int put(CommandSourceStack source, ModConfigImpl<CommandSourceStack, CommandBuildContext, Component> modConfig, String config, Object key, Object value) throws CommandSyntaxException {
        modConfig.put(config, key, value);
        source.sendSuccess(() -> Component.translatableWithFallback("betterconfig.commands.config.put", "The mapping %s=%s has been added to %s.", modConfig.asString(key), modConfig.asString(value), config), true);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int remove(CommandSourceStack source, ModConfigImpl<CommandSourceStack, CommandBuildContext, Component> modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.remove(config, value);
        source.sendSuccess(() -> Component.translatableWithFallback("betterconfig.commands.config.remove", "%s has been removed from %s.", modConfig.asString(value), config), true);
        return Command.SINGLE_SUCCESS;
    }
}
