package dev.xpple.betterconfig.command.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.api.Config;
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
        LiteralArgumentBuilder<FabricClientCommandSource> rootLiteral = literal("cconfig");
        for (ModConfigImpl modConfig : BetterConfigImpl.getModConfigs().values()) {
            Map<String, LiteralArgumentBuilder<FabricClientCommandSource>> literals = new HashMap<>();
            for (String config : modConfig.getConfigs().keySet()) {
                LiteralArgumentBuilder<FabricClientCommandSource> configLiteral = literal(config);
                literals.put(config, configLiteral);

                configLiteral.then(literal("get").executes(ctx -> get(ctx.getSource(), modConfig, config)));
            }

            modConfig.getSetters().keySet().forEach(config -> {
                Config annotation = modConfig.getAnnotations().get(config);
                Config.Setter setter = annotation.setter();
                Class<?> type = setter.type() == Config.EMPTY.class ? modConfig.getType(config) : setter.type();
                var pair = modConfig.getArgument(type);
                if (pair == null) {
                    return;
                }
                RequiredArgumentBuilder<FabricClientCommandSource, ?> subCommand = argument("value", pair.getLeft().get()).executes(ctx -> set(ctx.getSource(), modConfig, config, pair.getRight().apply(ctx, "value")));
                literals.get(config).then(literal("set").then(subCommand));
            });
            modConfig.getAdders().keySet().forEach(config -> {
                Config annotation = modConfig.getAnnotations().get(config);
                Config.Adder adder = annotation.adder();
                Class<?> type = adder.type() == Config.EMPTY.class ? (Class<?>) modConfig.getParameterTypes(config)[0] : adder.type();
                var pair = modConfig.getArgument(type);
                if (pair == null) {
                    return;
                }
                RequiredArgumentBuilder<FabricClientCommandSource, ?> subCommand = argument("value", pair.getLeft().get()).executes(ctx -> add(ctx.getSource(), modConfig, config, pair.getRight().apply(ctx, "value")));
                literals.get(config).then(literal("add").then(subCommand));
            });
            modConfig.getPutters().keySet().forEach(config -> {
                Config annotation = modConfig.getAnnotations().get(config);
                Config.Putter putter = annotation.putter();
                Type[] types = modConfig.getParameterTypes(config);
                Class<?> keyType = putter.keyType() == Config.EMPTY.class ? (Class<?>) types[0] : putter.keyType();
                var keyPair = modConfig.getArgument(keyType);
                if (keyPair == null) {
                    return;
                }
                RequiredArgumentBuilder<FabricClientCommandSource, ?> subCommand = argument("key", keyPair.getLeft().get());
                CheckedFunction<CommandContext<FabricClientCommandSource>, ?, CommandSyntaxException> getKey = ctx -> keyPair.getRight().apply(ctx, "key");
                Class<?> valueType = putter.valueType() == Config.EMPTY.class ? (Class<?>) types[1] : putter.valueType();
                var valuePair = modConfig.getArgument(valueType);
                if (valuePair == null) {
                    return;
                }
                RequiredArgumentBuilder<FabricClientCommandSource, ?> subSubCommand = argument("value", valuePair.getLeft().get()).executes(ctx -> put(ctx.getSource(), modConfig, config, getKey.apply(ctx), valuePair.getRight().apply(ctx, "value")));
                literals.get(config).then(literal("put").then(subCommand.then(subSubCommand)));
            });
            modConfig.getRemovers().keySet().forEach(config -> {
                Config annotation = modConfig.getAnnotations().get(config);
                Config.Remover remover = annotation.remover();
                Class<?> type = remover.type() == Config.EMPTY.class ? (Class<?>) modConfig.getParameterTypes(config)[0] : remover.type();
                var pair = modConfig.getArgument(type);
                if (pair == null) {
                    return;
                }
                RequiredArgumentBuilder<FabricClientCommandSource, ?> subCommand = argument("value", pair.getLeft().get()).executes(ctx -> remove(ctx.getSource(), modConfig, config, pair.getRight().apply(ctx, "value")));
                literals.get(config).then(literal("remove").then(subCommand));
            });
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
