package dev.xpple.betterconfig.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.xpple.betterconfig.command.suggestion.EnumSuggestionProvider;
import dev.xpple.betterconfig.impl.AbstractConfigImpl;
import dev.xpple.betterconfig.impl.BetterConfigImpl;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ConfigCommand extends ConfigCommandHelper<ServerCommandSource> {

    @Override
    protected <T extends Enum<T>> SuggestionProvider<ServerCommandSource> enumSuggestionProvider(Class<T> type) {
        return new EnumSuggestionProvider<>(type);
    }

    @Override
    protected DynamicCommandExceptionType invalidEnumException() {
        return new DynamicCommandExceptionType(value -> Text.translatable("argument.enum.invalid", value));
    }

    @SuppressWarnings("unchecked")
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(this.create("config", BetterConfigImpl.getModConfigs().values().stream().map(modConfig -> (AbstractConfigImpl<ServerCommandSource>) modConfig).toList()).requires(source -> source.hasPermissionLevel(4)));
    }

    @Override
    protected int comment(ServerCommandSource source, String config, String comment) {
        source.sendFeedback(() -> Text.translatableWithFallback("betterconfig.commands.config.comment", "Comment for %s:", config), false);
        source.sendFeedback(() -> Text.of(comment), false);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int get(ServerCommandSource source, AbstractConfigImpl<ServerCommandSource> abstractConfig, String config) {
        source.sendFeedback(() -> Text.translatableWithFallback("betterconfig.commands.config.get", "%s is currently set to %s.", config, abstractConfig.asString(config)), false);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int reset(ServerCommandSource source, AbstractConfigImpl<ServerCommandSource> abstractConfig, String config) {
        abstractConfig.reset(config);
        source.sendFeedback(() -> Text.translatableWithFallback("betterconfig.commands.config.reset", "%s has been reset to %s.", config, abstractConfig.asString(config)), true);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int set(ServerCommandSource source, AbstractConfigImpl<ServerCommandSource> abstractConfig, String config, Object value) throws CommandSyntaxException {
        abstractConfig.set(config, value);
        source.sendFeedback(() -> Text.translatableWithFallback("betterconfig.commands.config.set", "%s has been set to %s.", config, abstractConfig.asString(config)), true);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int add(ServerCommandSource source, AbstractConfigImpl<ServerCommandSource> abstractConfig, String config, Object value) throws CommandSyntaxException {
        abstractConfig.add(config, value);
        source.sendFeedback(() -> Text.translatableWithFallback("betterconfig.commands.config.add", "%s has been added to %s.", abstractConfig.asString(value), config), true);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int put(ServerCommandSource source, AbstractConfigImpl<ServerCommandSource> abstractConfig, String config, Object key, Object value) throws CommandSyntaxException {
        abstractConfig.put(config, key, value);
        source.sendFeedback(() -> Text.translatableWithFallback("betterconfig.commands.config.put", "The mapping %s=%s has been added to %s.", abstractConfig.asString(key), abstractConfig.asString(value), config), true);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int remove(ServerCommandSource source, AbstractConfigImpl<ServerCommandSource> abstractConfig, String config, Object value) throws CommandSyntaxException {
        abstractConfig.remove(config, value);
        source.sendFeedback(() -> Text.translatableWithFallback("betterconfig.commands.config.remove", "%s has been removed from %s.", abstractConfig.asString(value), config), true);
        return Command.SINGLE_SUCCESS;
    }
}
