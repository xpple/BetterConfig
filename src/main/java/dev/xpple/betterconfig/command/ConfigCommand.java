package dev.xpple.betterconfig.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.xpple.betterconfig.api.Config;
import dev.xpple.betterconfig.impl.BetterConfigImpl;
import dev.xpple.betterconfig.impl.ModConfigImpl;
import dev.xpple.betterconfig.util.CheckedFunction;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ConfigCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> rootLiteral = literal("config").requires(source -> source.hasPermissionLevel(4));
        for (ModConfigImpl modConfig : BetterConfigImpl.getModConfigs().values()) {
            Map<String, LiteralArgumentBuilder<ServerCommandSource>> literals = new HashMap<>();
            for (String config : modConfig.getConfigs().keySet()) {
                LiteralArgumentBuilder<ServerCommandSource> configLiteral = literal(config);
                literals.put(config, configLiteral);

                configLiteral.then(literal("get").executes(ctx -> get(ctx.getSource(), modConfig, config)));
            }

            modConfig.getSetters().keySet().forEach(config -> {
                Config annotation = modConfig.getAnnotations().get(config);
                Config.Setter setter = annotation.setter();
                Class<?> type = setter.type() == Config.EMPTY.class ? modConfig.getType(config) : setter.type();
                var argumentPair = modConfig.getArgument(type);
                var suggestorPair = modConfig.getSuggestor(type);
                if (argumentPair != null) {
                    RequiredArgumentBuilder<ServerCommandSource, ?> subCommand = argument("value", argumentPair.getLeft().get()).executes(ctx -> set(ctx.getSource(), modConfig, config, argumentPair.getRight().apply(ctx, "value")));
                    literals.get(config).then(literal("set").then(subCommand));
                } else if (suggestorPair != null) {
                    //noinspection unchecked
                    RequiredArgumentBuilder<ServerCommandSource, String> subCommand = argument("value", string()).suggests((SuggestionProvider<ServerCommandSource>) suggestorPair.getLeft().get()).executes(ctx -> set(ctx.getSource(), modConfig, config, suggestorPair.getRight().apply(ctx, "value")));
                    literals.get(config).then(literal("set").then(subCommand));
                }
            });
            modConfig.getAdders().keySet().forEach(config -> {
                Config annotation = modConfig.getAnnotations().get(config);
                Config.Adder adder = annotation.adder();
                Class<?> type = adder.type() == Config.EMPTY.class ? (Class<?>) modConfig.getParameterTypes(config)[0] : adder.type();
                var argumentPair = modConfig.getArgument(type);
                var suggestorPair = modConfig.getSuggestor(type);
                if (argumentPair != null) {
                    RequiredArgumentBuilder<ServerCommandSource, ?> subCommand = argument("value", argumentPair.getLeft().get()).executes(ctx -> add(ctx.getSource(), modConfig, config, argumentPair.getRight().apply(ctx, "value")));
                    literals.get(config).then(literal("add").then(subCommand));
                } else if (suggestorPair != null) {
                    //noinspection unchecked
                    RequiredArgumentBuilder<ServerCommandSource, String> subCommand = argument("value", string()).suggests((SuggestionProvider<ServerCommandSource>) suggestorPair.getLeft().get()).executes(ctx -> add(ctx.getSource(), modConfig, config, suggestorPair.getRight().apply(ctx, "value")));
                    literals.get(config).then(literal("add").then(subCommand));
                }
            });
            modConfig.getPutters().keySet().forEach(config -> {
                Config annotation = modConfig.getAnnotations().get(config);
                Config.Putter putter = annotation.putter();
                Type[] types = modConfig.getParameterTypes(config);
                Class<?> keyType = putter.keyType() == Config.EMPTY.class ? (Class<?>) types[0] : putter.keyType();
                RequiredArgumentBuilder<ServerCommandSource, ?> subCommand;
                CheckedFunction<CommandContext<ServerCommandSource>, ?, CommandSyntaxException> getKey;
                var argumentKeyPair = modConfig.getArgument(keyType);
                var suggestorKeyPair = modConfig.getSuggestor(keyType);
                if (argumentKeyPair != null) {
                    subCommand = argument("key", argumentKeyPair.getLeft().get());
                    getKey = ctx -> argumentKeyPair.getRight().apply(ctx, "key");
                } else if (suggestorKeyPair != null) {
                    //noinspection unchecked
                    subCommand = argument("key", string()).suggests((SuggestionProvider<ServerCommandSource>) suggestorKeyPair.getLeft().get());
                    getKey = ctx -> suggestorKeyPair.getRight().apply(ctx, "key");
                } else {
                    return;
                }
                Class<?> valueType = putter.valueType() == Config.EMPTY.class ? (Class<?>) types[1] : putter.valueType();
                var argumentValuePair = modConfig.getArgument(valueType);
                var suggestorValuePair = modConfig.getSuggestor(valueType);
                if (argumentValuePair != null) {
                    RequiredArgumentBuilder<ServerCommandSource, ?> subSubCommand = argument("value", argumentValuePair.getLeft().get()).executes(ctx -> put(ctx.getSource(), modConfig, config, getKey.apply(ctx), argumentValuePair.getRight().apply(ctx, "value")));
                    literals.get(config).then(literal("put").then(subCommand.then(subSubCommand)));
                } else if (suggestorValuePair != null) {
                    //noinspection unchecked
                    RequiredArgumentBuilder<ServerCommandSource, ?> subSubCommand = argument("value", string()).suggests((SuggestionProvider<ServerCommandSource>) suggestorValuePair.getLeft().get()).executes(ctx -> put(ctx.getSource(), modConfig, config, getKey.apply(ctx), suggestorValuePair.getRight().apply(ctx, "value")));
                    literals.get(config).then(literal("put").then(subCommand.then(subSubCommand)));
                }
            });
            modConfig.getRemovers().keySet().forEach(config -> {
                Config annotation = modConfig.getAnnotations().get(config);
                Config.Remover remover = annotation.remover();
                Class<?> type = remover.type() == Config.EMPTY.class ? (Class<?>) modConfig.getParameterTypes(config)[0] : remover.type();
                var argumentPair = modConfig.getArgument(type);
                var suggestorPair = modConfig.getSuggestor(type);
                if (argumentPair != null) {
                    RequiredArgumentBuilder<ServerCommandSource, ?> subCommand = argument("value", argumentPair.getLeft().get()).executes(ctx -> remove(ctx.getSource(), modConfig, config, argumentPair.getRight().apply(ctx, "value")));
                    literals.get(config).then(literal("remove").then(subCommand));
                } else if (suggestorPair != null) {
                    //noinspection unchecked
                    RequiredArgumentBuilder<ServerCommandSource, ?> subCommand = argument("value", string()).suggests((SuggestionProvider<ServerCommandSource>) suggestorPair.getLeft().get()).executes(ctx -> remove(ctx.getSource(), modConfig, config, suggestorPair.getRight().apply(ctx, "value")));
                    literals.get(config).then(literal("remove").then(subCommand));
                }
            });
            literals.values().forEach(literal -> rootLiteral.then(literal(modConfig.getModId()).then(literal)));
            dispatcher.register(rootLiteral);
        }
    }

    private static int get(ServerCommandSource source, ModConfigImpl modConfig, String config) {
        source.sendFeedback(Text.translatable("betterconfig.commands.config.get", config, modConfig.asString(config)), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int set(ServerCommandSource source, ModConfigImpl modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.set(config, value);
        source.sendFeedback(Text.translatable("betterconfig.commands.config.set", config, modConfig.asString(config)), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int add(ServerCommandSource source, ModConfigImpl modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.add(config, value);
        source.sendFeedback(Text.translatable("betterconfig.commands.config.add", modConfig.asString(value), config), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int put(ServerCommandSource source, ModConfigImpl modConfig, String config, Object key, Object value) throws CommandSyntaxException {
        modConfig.put(config, key, value);
        source.sendFeedback(Text.translatable("betterconfig.commands.config.put", key, modConfig.asString(value), config), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int remove(ServerCommandSource source, ModConfigImpl modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.remove(config, value);
        source.sendFeedback(Text.translatable("betterconfig.commands.config.remove", modConfig.asString(value), config), true);
        return Command.SINGLE_SUCCESS;
    }
}
