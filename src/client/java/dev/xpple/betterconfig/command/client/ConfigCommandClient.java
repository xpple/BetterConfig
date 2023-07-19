package dev.xpple.betterconfig.command.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.command.ConfigCommandHelper;
import dev.xpple.betterconfig.impl.ModConfigImpl;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class ConfigCommandClient extends ConfigCommandHelper<FabricClientCommandSource> {
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(this.create("cconfig"));
    }

    @Override
    protected int comment(FabricClientCommandSource source, String config, String comment) {
        source.sendFeedback(Text.translatable("betterconfig.commands.config.comment", config));
        source.sendFeedback(Text.of(comment));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int get(FabricClientCommandSource source, ModConfigImpl modConfig, String config) {
        source.sendFeedback(Text.translatable("betterconfig.commands.config.get", config, modConfig.asString(config)));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int reset(FabricClientCommandSource source, ModConfigImpl modConfig, String config) {
        modConfig.reset(config);
        source.sendFeedback(Text.translatable("betterconfig.commands.config.reset", config, modConfig.asString(config)));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int set(FabricClientCommandSource source, ModConfigImpl modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.set(config, value);
        source.sendFeedback(Text.translatable("betterconfig.commands.config.set", config, modConfig.asString(config)));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int add(FabricClientCommandSource source, ModConfigImpl modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.add(config, value);
        source.sendFeedback(Text.translatable("betterconfig.commands.config.add", modConfig.asString(value), config));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int put(FabricClientCommandSource source, ModConfigImpl modConfig, String config, Object key, Object value) throws CommandSyntaxException {
        modConfig.put(config, key, value);
        source.sendFeedback(Text.translatable("betterconfig.commands.config.put", modConfig.asString(key), modConfig.asString(value), config));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int remove(FabricClientCommandSource source, ModConfigImpl modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.remove(config, value);
        source.sendFeedback(Text.translatable("betterconfig.commands.config.remove", modConfig.asString(value), config));
        return Command.SINGLE_SUCCESS;
    }
}
