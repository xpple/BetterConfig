package dev.xpple.betterconfig.command.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.impl.BetterConfigImpl;
import dev.xpple.betterconfig.impl.ModConfigImpl;
import dev.xpple.betterconfig.util.CheckedFunction;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ConfigCommandClient {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        for (ModConfigImpl modConfig : BetterConfigImpl.getModConfigs().values()) {
            Map<String, LiteralArgumentBuilder<FabricClientCommandSource>> literals = new HashMap<>();
            for (String config : modConfig.getConfigs().keySet()) {
                LiteralArgumentBuilder<FabricClientCommandSource> configLiteral = literal(config);
                literals.put(config, configLiteral);

                configLiteral.then(literal("get").executes(ctx -> get(ctx.getSource(), modConfig, config)));
            }

            modConfig.getSetters().keySet().forEach(config -> {
                Class<?> type = modConfig.getType(config);
                var pair = modConfig.getArgument(type);
                if (pair == null) {
                    return;
                }
                RequiredArgumentBuilder<FabricClientCommandSource, ?> subCommand = argument("value", pair.getLeft().get()).executes(ctx -> set(ctx.getSource(), modConfig, config, pair.getRight().apply(ctx, "value")));
                literals.get(config).then(literal("set").then(subCommand));
            });
            modConfig.getAdders().keySet().forEach(config -> {
                Type[] types = modConfig.getParameterTypes(config);
                Type type;
                if (types.length == 1) {
                    type = types[0];
                } else if (types.length == 2) {
                    type = types[0];
                } else {
                    return;
                }
                var pair = modConfig.getArgument((Class<?>) type);
                if (pair == null) {
                    return;
                }
                RequiredArgumentBuilder<FabricClientCommandSource, ?> subCommand = argument("value", pair.getLeft().get()).executes(ctx -> add(ctx.getSource(), modConfig, config, pair.getRight().apply(ctx, "value")));
                literals.get(config).then(literal("add").then(subCommand));
            });
            modConfig.getPutters().keySet().forEach(config -> {
                Type[] types = modConfig.getParameterTypes(config);
                if (types.length != 2) {
                    return;
                }
                Type keyType = types[0];
                var keyPair = modConfig.getArgument((Class<?>) keyType);
                if (keyPair == null) {
                    return;
                }
                RequiredArgumentBuilder<FabricClientCommandSource, ?> subCommand = argument("key", keyPair.getLeft().get());
                CheckedFunction<CommandContext<FabricClientCommandSource>, ?, CommandSyntaxException> getKey = ctx -> keyPair.getRight().apply(ctx, "key");
                Type valueType = types[1];
                var valuePair = modConfig.getArgument((Class<?>) valueType);
                if (valuePair == null) {
                    return;
                }
                RequiredArgumentBuilder<FabricClientCommandSource, ?> subSubCommand = argument("value", valuePair.getLeft().get()).executes(ctx -> put(ctx.getSource(), modConfig, config, getKey.apply(ctx), valuePair.getRight().apply(ctx, "value")));
                literals.get(config).then(literal("put").then(subCommand.then(subSubCommand)));
            });
            modConfig.getRemovers().keySet().forEach(config -> {
                Type[] types = modConfig.getParameterTypes(config);
                Type type;
                if (types.length == 1) {
                    type = types[0];
                } else if (types.length == 2) {
                    type = types[0];
                } else {
                    return;
                }
                var pair = modConfig.getArgument((Class<?>) type);
                if (pair == null) {
                    return;
                }
                RequiredArgumentBuilder<FabricClientCommandSource, ?> subCommand = argument("value", pair.getLeft().get()).executes(ctx -> remove(ctx.getSource(), modConfig, config, pair.getRight().apply(ctx, "value")));
                literals.get(config).then(literal("remove").then(subCommand));
            });
            LiteralArgumentBuilder<FabricClientCommandSource> rootLiteral = literal("cconfig");
            literals.values().forEach(literal -> rootLiteral.then(literal(modConfig.getModId()).then(literal)));
            dispatcher.register(rootLiteral);
        }
    }

    private static int get(FabricClientCommandSource source, ModConfigImpl modConfig, String config) {
        source.sendFeedback(Text.translatable("betterconfig.commands.config.get", config, modConfig.asString(config)));
        return Command.SINGLE_SUCCESS;
    }

    private static int set(FabricClientCommandSource source, ModConfigImpl modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.set(config, value);
        source.sendFeedback(Text.translatable("betterconfig.commands.config.set", config, modConfig.asString(config)));
        return Command.SINGLE_SUCCESS;
    }

    private static int add(FabricClientCommandSource source, ModConfigImpl modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.add(config, value);
        source.sendFeedback(Text.translatable("betterconfig.commands.config.add", modConfig.asString(value), config));
        return Command.SINGLE_SUCCESS;
    }

    private static int put(FabricClientCommandSource source, ModConfigImpl modConfig, String config, Object key, Object value) throws CommandSyntaxException {
        modConfig.put(config, key, value);
        source.sendFeedback(Text.translatable("betterconfig.commands.config.put", key, modConfig.asString(value), config));
        return Command.SINGLE_SUCCESS;
    }

    private static int remove(FabricClientCommandSource source, ModConfigImpl modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.remove(config, value);
        source.sendFeedback(Text.translatable("betterconfig.commands.config.remove", modConfig.asString(value), config));
        return Command.SINGLE_SUCCESS;
    }
}
