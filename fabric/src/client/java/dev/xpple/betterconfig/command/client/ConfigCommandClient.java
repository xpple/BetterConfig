package dev.xpple.betterconfig.command.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.xpple.betterconfig.command.AbstractConfigCommand;
import dev.xpple.betterconfig.impl.AbstractBetterConfigImpl;
import dev.xpple.betterconfig.impl.ModConfigImpl;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.Set;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.*;

public class ConfigCommandClient extends AbstractConfigCommand<FabricClientCommandSource, CommandBuildContext, Component> {

    private ConfigCommandClient() {
        super("cconfig");
    }

    @SuppressWarnings("unchecked")
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext buildContext) {
        Map<String, ModConfigImpl<?, ?, ?>> modConfigs = AbstractBetterConfigImpl.getModConfigs();
        LiteralCommandNode<FabricClientCommandSource> command = dispatcher.register(new ConfigCommandClient().create(modConfigs.values().stream().map(modConfig -> (ModConfigImpl<FabricClientCommandSource, CommandBuildContext, Component>) modConfig).toList(), buildContext));
        for (ModConfigImpl<?, ?, ?> modConfig : modConfigs.values()) {
            Set<String> commandAliases = modConfig.getCommandAliases();
            CommandNode<FabricClientCommandSource> modCommand = command.getChild(modConfig.getModId());
            if (modCommand == null) {
                continue;
            }
            for (String alias : commandAliases) {
                dispatcher.register(literal(alias).redirect(modCommand));
            }
        }
    }

    @Override
    protected int comment(FabricClientCommandSource source, String config, Component comment) {
        source.sendFeedback(Component.translatable("betterconfig.commands.config.comment", config));
        source.sendFeedback(comment);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int get(FabricClientCommandSource source, ModConfigImpl<FabricClientCommandSource, CommandBuildContext, Component> modConfig, String config) {
        source.sendFeedback(Component.translatable("betterconfig.commands.config.get", config, modConfig.asComponent(config)));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int reset(FabricClientCommandSource source, ModConfigImpl<FabricClientCommandSource, CommandBuildContext, Component> modConfig, String config) {
        modConfig.reset(config);
        source.sendFeedback(Component.translatable("betterconfig.commands.config.reset", config, modConfig.asComponent(config)));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int set(FabricClientCommandSource source, ModConfigImpl<FabricClientCommandSource, CommandBuildContext, Component> modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.set(config, value);
        source.sendFeedback(Component.translatable("betterconfig.commands.config.set", config, modConfig.asComponent(config)));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int add(FabricClientCommandSource source, ModConfigImpl<FabricClientCommandSource, CommandBuildContext, Component> modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.add(config, value);
        source.sendFeedback(Component.translatable("betterconfig.commands.config.add", modConfig.asString(value), config));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int put(FabricClientCommandSource source, ModConfigImpl<FabricClientCommandSource, CommandBuildContext, Component> modConfig, String config, Object key, Object value) throws CommandSyntaxException {
        modConfig.put(config, key, value);
        source.sendFeedback(Component.translatable("betterconfig.commands.config.put", modConfig.asString(key), modConfig.asString(value), config));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int remove(FabricClientCommandSource source, ModConfigImpl<FabricClientCommandSource, CommandBuildContext, Component> modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.remove(config, value);
        source.sendFeedback(Component.translatable("betterconfig.commands.config.remove", modConfig.asString(value), config));
        return Command.SINGLE_SUCCESS;
    }
}
