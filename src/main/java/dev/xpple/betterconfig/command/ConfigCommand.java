package dev.xpple.betterconfig.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.impl.ModConfigImpl;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ConfigCommand extends ConfigCommandHelper<ServerCommandSource> {
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(this.create("config").requires(source -> source.hasPermissionLevel(4)));
    }

    @Override
    protected int get(ServerCommandSource source, ModConfigImpl modConfig, String config) {
        source.sendFeedback(() -> Text.translatable("betterconfig.commands.config.get", config, modConfig.asString(config)), true);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int reset(ServerCommandSource source, ModConfigImpl modConfig, String config) {
        modConfig.reset(config);
        source.sendFeedback(() -> Text.translatable("betterconfig.commands.config.reset", config, modConfig.asString(config)), true);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int set(ServerCommandSource source, ModConfigImpl modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.set(config, value);
        source.sendFeedback(() -> Text.translatable("betterconfig.commands.config.set", config, modConfig.asString(config)), true);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int add(ServerCommandSource source, ModConfigImpl modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.add(config, value);
        source.sendFeedback(() -> Text.translatable("betterconfig.commands.config.add", modConfig.asString(value), config), true);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int put(ServerCommandSource source, ModConfigImpl modConfig, String config, Object key, Object value) throws CommandSyntaxException {
        modConfig.put(config, key, value);
        source.sendFeedback(() -> Text.translatable("betterconfig.commands.config.put", key, modConfig.asString(value), config), true);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int remove(ServerCommandSource source, ModConfigImpl modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.remove(config, value);
        source.sendFeedback(() -> Text.translatable("betterconfig.commands.config.remove", modConfig.asString(value), config), true);
        return Command.SINGLE_SUCCESS;
    }
}
