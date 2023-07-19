package dev.xpple.betterconfig.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.xpple.betterconfig.api.Config;
import dev.xpple.betterconfig.command.suggestion.EnumSuggestionProvider;
import dev.xpple.betterconfig.impl.BetterConfigImpl;
import dev.xpple.betterconfig.impl.ModConfigImpl;
import dev.xpple.betterconfig.util.CheckedFunction;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.StringArgumentType.*;

public abstract class ConfigCommandHelper<S extends CommandSource>  {

    private static final DynamicCommandExceptionType INVALID_ENUM_EXCEPTION = new DynamicCommandExceptionType(value -> Text.translatable("argument.enum.invalid", value));

    protected LiteralArgumentBuilder<S> create(String rootLiteral) {
        LiteralArgumentBuilder<S> root = LiteralArgumentBuilder.literal(rootLiteral);
        for (ModConfigImpl modConfig : BetterConfigImpl.getModConfigs().values()) {
            Map<String, LiteralArgumentBuilder<S>> literals = new HashMap<>();
            for (String config : modConfig.getConfigs().keySet()) {
                @SuppressWarnings("unchecked")
                Predicate<S> condition = (Predicate<S>) modConfig.getConditions().get(config);
                LiteralArgumentBuilder<S> configLiteral = LiteralArgumentBuilder.<S>literal(config).requires(condition);
                literals.put(config, configLiteral);

                configLiteral.then(LiteralArgumentBuilder.<S>literal("get").executes(ctx -> get(ctx.getSource(), modConfig, config)));
                configLiteral.then(LiteralArgumentBuilder.<S>literal("reset").executes(ctx -> reset(ctx.getSource(), modConfig, config)));
            }

            modConfig.getComments().forEach((config, comment) -> literals.get(config).then(LiteralArgumentBuilder.<S>literal("comment").executes(ctx -> comment(ctx.getSource(), config, comment))));
            modConfig.getSetters().keySet().forEach(config -> {
                Config annotation = modConfig.getAnnotations().get(config);
                Config.Setter setter = annotation.setter();
                Class<?> type = setter.type() == Config.EMPTY.class ? modConfig.getType(config) : setter.type();
                var argumentPair = modConfig.getArgument(type);
                var suggestorPair = modConfig.getSuggestor(type);
                if (type.isEnum()) {
                    //noinspection rawtypes, unchecked
                    RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", string()).suggests(new EnumSuggestionProvider<>((Class) type));
                    subCommand.executes(ctx -> {
                        String value = getString(ctx, "value");
                        return set(ctx.getSource(), modConfig, config, Arrays.stream(type.getEnumConstants()).filter(c -> c.toString().equals(value)).findAny().orElseThrow(() -> INVALID_ENUM_EXCEPTION.create(value)));
                    });
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("set").then(subCommand));
                } else if (argumentPair != null) {
                    RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", argumentPair.getLeft().get());
                    subCommand.executes(ctx -> set(ctx.getSource(), modConfig, config, argumentPair.getRight().apply(ctx, "value")));
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("set").then(subCommand));
                } else if (suggestorPair != null) {
                    RequiredArgumentBuilder<S, String> subCommand = RequiredArgumentBuilder.argument("value", greedyString());
                    //noinspection unchecked
                    subCommand.suggests((SuggestionProvider<S>) suggestorPair.getLeft().get()).executes(ctx -> set(ctx.getSource(), modConfig, config, suggestorPair.getRight().apply(ctx, "value")));
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("set").then(subCommand));
                }
            });
            modConfig.getAdders().keySet().forEach(config -> {
                Config annotation = modConfig.getAnnotations().get(config);
                Config.Adder adder = annotation.adder();
                Class<?> type = adder.type() == Config.EMPTY.class ? (Class<?>) modConfig.getParameterTypes(config)[0] : adder.type();
                var argumentPair = modConfig.getArgument(type);
                var suggestorPair = modConfig.getSuggestor(type);
                if (type.isEnum()) {
                    //noinspection rawtypes, unchecked
                    RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", string()).suggests(new EnumSuggestionProvider<>((Class) type));
                    subCommand.executes(ctx -> {
                        String value = getString(ctx, "value");
                        return add(ctx.getSource(), modConfig, config, Arrays.stream(type.getEnumConstants()).filter(c -> c.toString().equals(value)).findAny().orElseThrow(() -> INVALID_ENUM_EXCEPTION.create(value)));
                    });
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("add").then(subCommand));
                } else if (argumentPair != null) {
                    RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", argumentPair.getLeft().get());
                    subCommand.executes(ctx -> add(ctx.getSource(), modConfig, config, argumentPair.getRight().apply(ctx, "value")));
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("add").then(subCommand));
                } else if (suggestorPair != null) {
                    RequiredArgumentBuilder<S, String> subCommand = RequiredArgumentBuilder.argument("value", greedyString());
                    //noinspection unchecked
                    subCommand.suggests((SuggestionProvider<S>) suggestorPair.getLeft().get()).executes(ctx -> add(ctx.getSource(), modConfig, config, suggestorPair.getRight().apply(ctx, "value")));
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("add").then(subCommand));
                }
            });
            modConfig.getPutters().keySet().forEach(config -> {
                Config annotation = modConfig.getAnnotations().get(config);
                Config.Putter putter = annotation.putter();
                Type[] types = modConfig.getParameterTypes(config);
                Class<?> keyType = putter.keyType() == Config.EMPTY.class ? (Class<?>) types[0] : putter.keyType();
                RequiredArgumentBuilder<S, ?> subCommand;
                CheckedFunction<CommandContext<S>, ?, CommandSyntaxException> getKey;
                var argumentKeyPair = modConfig.getArgument(keyType);
                var suggestorKeyPair = modConfig.getSuggestor(keyType);
                if (keyType.isEnum()) {
                    //noinspection rawtypes, unchecked
                    subCommand = RequiredArgumentBuilder.argument("key", string()).suggests(new EnumSuggestionProvider<>((Class) keyType));
                    getKey = ctx -> {
                        String value = getString(ctx, "key");
                        return Arrays.stream(keyType.getEnumConstants()).filter(c -> c.toString().equals(value)).findAny().orElseThrow(() -> INVALID_ENUM_EXCEPTION.create(value));
                    };
                } else if (argumentKeyPair != null) {
                    subCommand = RequiredArgumentBuilder.argument("key", argumentKeyPair.getLeft().get());
                    getKey = ctx -> argumentKeyPair.getRight().apply(ctx, "key");
                } else if (suggestorKeyPair != null) {
                    subCommand = RequiredArgumentBuilder.argument("key", string());
                    //noinspection unchecked
                    subCommand.suggests((SuggestionProvider<S>) suggestorKeyPair.getLeft().get());
                    getKey = ctx -> suggestorKeyPair.getRight().apply(ctx, "key");
                } else {
                    return;
                }
                Class<?> valueType = putter.valueType() == Config.EMPTY.class ? (Class<?>) types[1] : putter.valueType();
                var argumentValuePair = modConfig.getArgument(valueType);
                var suggestorValuePair = modConfig.getSuggestor(valueType);
                if (valueType.isEnum()) {
                    //noinspection rawtypes, unchecked
                    RequiredArgumentBuilder<S, ?> subSubCommand = RequiredArgumentBuilder.argument("value", string()).suggests(new EnumSuggestionProvider<>((Class) valueType));
                    subCommand.executes(ctx -> {
                        String value = getString(ctx, "value");
                        return put(ctx.getSource(), modConfig, config, getKey.apply(ctx), Arrays.stream(valueType.getEnumConstants()).filter(c -> c.toString().equals(value)).findAny().orElseThrow(() -> INVALID_ENUM_EXCEPTION.create(value)));
                    });
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("put").then(subCommand.then(subSubCommand)));
                } else if (argumentValuePair != null) {
                    RequiredArgumentBuilder<S, ?> subSubCommand = RequiredArgumentBuilder.argument("value", argumentValuePair.getLeft().get());
                    subSubCommand.executes(ctx -> put(ctx.getSource(), modConfig, config, getKey.apply(ctx), argumentValuePair.getRight().apply(ctx, "value")));
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("put").then(subCommand.then(subSubCommand)));
                } else if (suggestorValuePair != null) {
                    RequiredArgumentBuilder<S, ?> subSubCommand = RequiredArgumentBuilder.argument("value", greedyString());
                    //noinspection unchecked
                    subSubCommand.suggests((SuggestionProvider<S>) suggestorValuePair.getLeft().get()).executes(ctx -> put(ctx.getSource(), modConfig, config, getKey.apply(ctx), suggestorValuePair.getRight().apply(ctx, "value")));
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("put").then(subCommand.then(subSubCommand)));
                }
            });
            modConfig.getRemovers().keySet().forEach(config -> {
                Config annotation = modConfig.getAnnotations().get(config);
                Config.Remover remover = annotation.remover();
                Class<?> type = remover.type() == Config.EMPTY.class ? (Class<?>) modConfig.getParameterTypes(config)[0] : remover.type();
                var argumentPair = modConfig.getArgument(type);
                var suggestorPair = modConfig.getSuggestor(type);
                if (type.isEnum()) {
                    //noinspection rawtypes, unchecked
                    RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", string()).suggests(new EnumSuggestionProvider<>((Class) type));
                    subCommand.executes(ctx -> {
                        String value = getString(ctx, "value");
                        return remove(ctx.getSource(), modConfig, config, Arrays.stream(type.getEnumConstants()).filter(c -> c.toString().equals(value)).findAny().orElseThrow(() -> INVALID_ENUM_EXCEPTION.create(value)));
                    });
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("remove").then(subCommand));
                } else if (argumentPair != null) {
                    RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", argumentPair.getLeft().get());
                    subCommand.executes(ctx -> remove(ctx.getSource(), modConfig, config, argumentPair.getRight().apply(ctx, "value")));
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("remove").then(subCommand));
                } else if (suggestorPair != null) {
                    RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", greedyString());
                    //noinspection unchecked
                    subCommand.suggests((SuggestionProvider<S>) suggestorPair.getLeft().get()).executes(ctx -> remove(ctx.getSource(), modConfig, config, suggestorPair.getRight().apply(ctx, "value")));
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("remove").then(subCommand));
                }
            });
            literals.values().forEach(literal -> root.then(LiteralArgumentBuilder.<S>literal(modConfig.getModId()).then(literal)));
        }
        return root;
    }

    protected abstract int comment(S source, String config, String comment);

    protected abstract int get(S source, ModConfigImpl modConfig, String config);

    protected abstract int reset(S source, ModConfigImpl modConfig, String config);

    protected abstract int set(S source, ModConfigImpl modConfig, String config, Object value) throws CommandSyntaxException;

    protected abstract int add(S source, ModConfigImpl modConfig, String config, Object value) throws CommandSyntaxException;

    protected abstract int put(S source, ModConfigImpl modConfig, String config, Object key, Object value) throws CommandSyntaxException;

    protected abstract int remove(S source, ModConfigImpl modConfig, String config, Object value) throws CommandSyntaxException;
}
