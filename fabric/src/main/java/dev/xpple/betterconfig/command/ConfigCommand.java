package dev.xpple.betterconfig.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.xpple.betterconfig.command.suggestion.EnumSuggestionProvider;
import dev.xpple.betterconfig.impl.AbstractConfigImpl;
import dev.xpple.betterconfig.impl.BetterConfigImpl;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class ConfigCommand extends AbstractConfigCommand<CommandSourceStack, CommandBuildContext> {

    @Override
    protected <T extends Enum<T>> SuggestionProvider<CommandSourceStack> enumSuggestionProvider(Class<T> type) {
        return new EnumSuggestionProvider<>(type);
    }

    @Override
    protected DynamicCommandExceptionType invalidEnumException() {
        return new DynamicCommandExceptionType(value -> Component.translatable("argument.enum.invalid", value));
    }

    private ConfigCommand() {
        super("config");
    }

    @SuppressWarnings("unchecked")
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(new ConfigCommand().create(BetterConfigImpl.getModConfigs().values().stream().map(modConfig -> (AbstractConfigImpl<CommandSourceStack, CommandBuildContext>) modConfig).toList(), buildContext).requires(source -> source.hasPermission(4)));
    }

    @Override
    protected int comment(CommandSourceStack source, String config, String comment) {
        source.sendSuccess(() -> Component.translatableWithFallback("betterconfig.commands.config.comment", "Comment for %s:", config), false);
        source.sendSuccess(() -> Component.literal(comment), false);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int get(CommandSourceStack source, AbstractConfigImpl<CommandSourceStack, CommandBuildContext> abstractConfig, String config) {
        source.sendSuccess(() -> Component.translatableWithFallback("betterconfig.commands.config.get", "%s is currently set to %s.", config, abstractConfig.asString(config)), false);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int reset(CommandSourceStack source, AbstractConfigImpl<CommandSourceStack, CommandBuildContext> abstractConfig, String config) {
        abstractConfig.reset(config);
        source.sendSuccess(() -> Component.translatableWithFallback("betterconfig.commands.config.reset", "%s has been reset to %s.", config, abstractConfig.asString(config)), true);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int set(CommandSourceStack source, AbstractConfigImpl<CommandSourceStack, CommandBuildContext> abstractConfig, String config, Object value) throws CommandSyntaxException {
        abstractConfig.set(config, value);
        source.sendSuccess(() -> Component.translatableWithFallback("betterconfig.commands.config.set", "%s has been set to %s.", config, abstractConfig.asString(config)), true);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int add(CommandSourceStack source, AbstractConfigImpl<CommandSourceStack, CommandBuildContext> abstractConfig, String config, Object value) throws CommandSyntaxException {
        abstractConfig.add(config, value);
        source.sendSuccess(() -> Component.translatableWithFallback("betterconfig.commands.config.add", "%s has been added to %s.", abstractConfig.asString(value), config), true);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int put(CommandSourceStack source, AbstractConfigImpl<CommandSourceStack, CommandBuildContext> abstractConfig, String config, Object key, Object value) throws CommandSyntaxException {
        abstractConfig.put(config, key, value);
        source.sendSuccess(() -> Component.translatableWithFallback("betterconfig.commands.config.put", "The mapping %s=%s has been added to %s.", abstractConfig.asString(key), abstractConfig.asString(value), config), true);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int remove(CommandSourceStack source, AbstractConfigImpl<CommandSourceStack, CommandBuildContext> abstractConfig, String config, Object value) throws CommandSyntaxException {
        abstractConfig.remove(config, value);
        source.sendSuccess(() -> Component.translatableWithFallback("betterconfig.commands.config.remove", "%s has been removed from %s.", abstractConfig.asString(value), config), true);
        return Command.SINGLE_SUCCESS;
    }
}
