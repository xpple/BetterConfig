package dev.xpple.betterconfig.command.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.xpple.betterconfig.command.AbstractConfigCommand;
import dev.xpple.betterconfig.command.suggestion.EnumSuggestionProvider;
import dev.xpple.betterconfig.impl.AbstractConfigImpl;
import dev.xpple.betterconfig.impl.BetterConfigImpl;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

public class ConfigCommandClient extends AbstractConfigCommand<FabricClientCommandSource, CommandBuildContext> {

    @Override
    protected <T extends Enum<T>> SuggestionProvider<FabricClientCommandSource> enumSuggestionProvider(Class<T> type) {
        return new EnumSuggestionProvider<>(type);
    }

    @Override
    protected DynamicCommandExceptionType invalidEnumException() {
        return new DynamicCommandExceptionType(value -> Component.translatable("argument.enum.invalid", value));
    }

    private ConfigCommandClient() {
        super("cconfig");
    }

    @SuppressWarnings("unchecked")
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(new ConfigCommandClient().create(BetterConfigImpl.getModConfigs().values().stream().map(modConfig -> (AbstractConfigImpl<FabricClientCommandSource, CommandBuildContext>) modConfig).toList(), buildContext));
    }

    @Override
    protected int comment(FabricClientCommandSource source, String config, String comment) {
        source.sendFeedback(Component.translatable("betterconfig.commands.config.comment", config));
        source.sendFeedback(Component.literal(comment));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int get(FabricClientCommandSource source, AbstractConfigImpl<FabricClientCommandSource, CommandBuildContext> abstractConfig, String config) {
        source.sendFeedback(Component.translatable("betterconfig.commands.config.get", config, abstractConfig.asString(config)));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int reset(FabricClientCommandSource source, AbstractConfigImpl<FabricClientCommandSource, CommandBuildContext> abstractConfig, String config) {
        abstractConfig.reset(config);
        source.sendFeedback(Component.translatable("betterconfig.commands.config.reset", config, abstractConfig.asString(config)));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int set(FabricClientCommandSource source, AbstractConfigImpl<FabricClientCommandSource, CommandBuildContext> abstractConfig, String config, Object value) throws CommandSyntaxException {
        abstractConfig.set(config, value);
        source.sendFeedback(Component.translatable("betterconfig.commands.config.set", config, abstractConfig.asString(config)));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int add(FabricClientCommandSource source, AbstractConfigImpl<FabricClientCommandSource, CommandBuildContext> abstractConfig, String config, Object value) throws CommandSyntaxException {
        abstractConfig.add(config, value);
        source.sendFeedback(Component.translatable("betterconfig.commands.config.add", abstractConfig.asString(value), config));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int put(FabricClientCommandSource source, AbstractConfigImpl<FabricClientCommandSource, CommandBuildContext> abstractConfig, String config, Object key, Object value) throws CommandSyntaxException {
        abstractConfig.put(config, key, value);
        source.sendFeedback(Component.translatable("betterconfig.commands.config.put", abstractConfig.asString(key), abstractConfig.asString(value), config));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int remove(FabricClientCommandSource source, AbstractConfigImpl<FabricClientCommandSource, CommandBuildContext> abstractConfig, String config, Object value) throws CommandSyntaxException {
        abstractConfig.remove(config, value);
        source.sendFeedback(Component.translatable("betterconfig.commands.config.remove", abstractConfig.asString(value), config));
        return Command.SINGLE_SUCCESS;
    }
}
