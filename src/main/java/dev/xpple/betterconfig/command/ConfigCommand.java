package dev.xpple.betterconfig.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.impl.ModConfigImpl;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ConfigCommand extends AbstractConfigCommand<ServerCommandSource> {
    private ConfigCommand() {
        super("config");
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(new ConfigCommand().create(registryAccess).requires(source -> source.hasPermissionLevel(4)));
    }

    @Override
    protected int comment(ServerCommandSource source, String config, String comment) {
        source.sendFeedback(() -> Text.translatableWithFallback("betterconfig.commands.config.comment", "Comment for %s:", config), false);
        source.sendFeedback(() -> Text.of(comment), false);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int get(ServerCommandSource source, ModConfigImpl modConfig, String config) {
        source.sendFeedback(() -> Text.translatableWithFallback("betterconfig.commands.config.get", "%s is currently set to %s.", config, modConfig.asString(config)), false);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int reset(ServerCommandSource source, ModConfigImpl modConfig, String config) {
        modConfig.reset(config);
        source.sendFeedback(() -> Text.translatableWithFallback("betterconfig.commands.config.reset", "%s has been reset to %s.", config, modConfig.asString(config)), true);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int set(ServerCommandSource source, ModConfigImpl modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.set(config, value);
        source.sendFeedback(() -> Text.translatableWithFallback("betterconfig.commands.config.set", "%s has been set to %s.", config, modConfig.asString(config)), true);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int add(ServerCommandSource source, ModConfigImpl modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.add(config, value);
        source.sendFeedback(() -> Text.translatableWithFallback("betterconfig.commands.config.add", "%s has been added to %s.", modConfig.asString(value), config), true);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int put(ServerCommandSource source, ModConfigImpl modConfig, String config, Object key, Object value) throws CommandSyntaxException {
        modConfig.put(config, key, value);
        source.sendFeedback(() -> Text.translatableWithFallback("betterconfig.commands.config.put", "The mapping %s=%s has been added to %s.", modConfig.asString(key), modConfig.asString(value), config), true);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int remove(ServerCommandSource source, ModConfigImpl modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.remove(config, value);
        source.sendFeedback(() -> Text.translatableWithFallback("betterconfig.commands.config.remove", "%s has been removed from %s.", modConfig.asString(value), config), true);
        return Command.SINGLE_SUCCESS;
    }
}
