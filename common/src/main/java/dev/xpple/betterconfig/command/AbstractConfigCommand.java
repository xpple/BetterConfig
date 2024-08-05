package dev.xpple.betterconfig.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.api.Config;
import dev.xpple.betterconfig.impl.ModConfigImpl;
import dev.xpple.betterconfig.impl.Platform;
import dev.xpple.betterconfig.util.CheckedFunction;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.StringArgumentType.*;

public abstract class AbstractConfigCommand<S, C>  {

    private final String rootLiteral;

    protected AbstractConfigCommand(String rootLiteral) {
        this.rootLiteral = rootLiteral;
    }

    protected final LiteralArgumentBuilder<S> create(Collection<? extends ModConfigImpl<S, C>> modConfigs, C buildContext) {
        LiteralArgumentBuilder<S> root = LiteralArgumentBuilder.literal(this.rootLiteral);
        for (ModConfigImpl<S, C> modConfig : modConfigs) {
            LiteralArgumentBuilder<S> identifierLiteral = LiteralArgumentBuilder.literal(modConfig.getModId());
            for (String config : modConfig.getConfigs().keySet()) {
                Predicate<S> condition = modConfig.getConditions().get(config);
                LiteralArgumentBuilder<S> configLiteral = LiteralArgumentBuilder.<S>literal(config).requires(condition);

                configLiteral.then(LiteralArgumentBuilder.<S>literal("get").executes(ctx -> get(ctx.getSource(), modConfig, config)));
                configLiteral.then(LiteralArgumentBuilder.<S>literal("reset").executes(ctx -> reset(ctx.getSource(), modConfig, config)));

                String comment = modConfig.getComments().get(config);
                if (comment != null) {
                    configLiteral.then(LiteralArgumentBuilder.<S>literal("comment").executes(ctx -> comment(ctx.getSource(), config, comment)));
                }

                if (modConfig.getSetters().containsKey(config)) {
                    Config annotation = modConfig.getAnnotations().get(config);
                    Config.Setter setter = annotation.setter();
                    Class<?> type = setter.type() == Config.EMPTY.class ? modConfig.getType(config) : setter.type();
                    var argumentFunction = modConfig.getArgument(type);
                    if (argumentFunction != null) {
                        RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", argumentFunction.apply(buildContext));
                        subCommand.executes(ctx -> set(ctx.getSource(), modConfig, config, ctx.getArgument("value", type)));
                        configLiteral.then(LiteralArgumentBuilder.<S>literal("set").then(subCommand));
                    } else if (type.isEnum()) {
                        //noinspection rawtypes, unchecked
                        RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", string()).suggests(Platform.current.enumSuggestionProvider((Class) type));
                        subCommand.executes(ctx -> {
                            String value = getString(ctx, "value");
                            return set(ctx.getSource(), modConfig, config, Arrays.stream(type.getEnumConstants()).filter(c -> ((Enum<?>) c).name().equals(value)).findAny().orElseThrow(() -> Platform.current.invalidEnumException().create(value)));
                        });
                        configLiteral.then(LiteralArgumentBuilder.<S>literal("set").then(subCommand));
                    }
                }

                if (modConfig.getAdders().containsKey(config)) {
                    Config annotation = modConfig.getAnnotations().get(config);
                    Config.Adder adder = annotation.adder();
                    Class<?> type = adder.type() == Config.EMPTY.class ? (Class<?>) modConfig.getParameterTypes(config)[0] : adder.type();
                    var argumentFunction = modConfig.getArgument(type);
                    if (argumentFunction != null) {
                        RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", argumentFunction.apply(buildContext));
                        subCommand.executes(ctx -> add(ctx.getSource(), modConfig, config, ctx.getArgument("value", type)));
                        configLiteral.then(LiteralArgumentBuilder.<S>literal("add").then(subCommand));
                    } else if (type.isEnum()) {
                        // noinspection rawtypes, unchecked
                        RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", string()).suggests(Platform.current.enumSuggestionProvider((Class) type));
                        subCommand.executes(ctx -> {
                            String value = getString(ctx, "value");
                            return add(ctx.getSource(), modConfig, config, Arrays.stream(type.getEnumConstants()).filter(c -> ((Enum<?>) c).name().equals(value)).findAny().orElseThrow(() -> Platform.current.invalidEnumException().create(value)));
                        });
                        configLiteral.then(LiteralArgumentBuilder.<S>literal("add").then(subCommand));
                    }
                }

                if (modConfig.getPutters().containsKey(config)) {
                    Config annotation = modConfig.getAnnotations().get(config);
                    Config.Putter putter = annotation.putter();
                    Type[] types = modConfig.getParameterTypes(config);
                    Class<?> keyType = putter.keyType() == Config.EMPTY.class ? (Class<?>) types[0] : putter.keyType();
                    RequiredArgumentBuilder<S, ?> subCommand;
                    CheckedFunction<CommandContext<S>, ?, CommandSyntaxException> getKey;
                    var keyArgumentFunction = modConfig.getArgument(keyType);
                    if (keyArgumentFunction != null) {
                        subCommand = RequiredArgumentBuilder.argument("key", keyArgumentFunction.apply(buildContext));
                        getKey = ctx -> ctx.getArgument("key", keyType);
                    } else if (keyType.isEnum()) {
                        //noinspection rawtypes, unchecked
                        subCommand = RequiredArgumentBuilder.argument("key", string()).suggests(Platform.current.enumSuggestionProvider((Class) keyType));
                        getKey = ctx -> {
                            String value = getString(ctx, "key");
                            return Arrays.stream(keyType.getEnumConstants()).filter(c -> ((Enum<?>) c).name().equals(value)).findAny().orElseThrow(() -> Platform.current.invalidEnumException().create(value));
                        };
                    } else {
                        subCommand = null;
                        getKey = null;
                    }
                    if (subCommand != null) {
                        Class<?> valueType = putter.valueType() == Config.EMPTY.class ? (Class<?>) types[1] : putter.valueType();
                        var valueArgumentFunction = modConfig.getArgument(valueType);
                        if (valueArgumentFunction != null) {
                            RequiredArgumentBuilder<S, ?> subSubCommand = RequiredArgumentBuilder.argument("value", valueArgumentFunction.apply(buildContext));
                            subSubCommand.executes(ctx -> put(ctx.getSource(), modConfig, config, getKey.apply(ctx), ctx.getArgument("value", valueType)));
                            configLiteral.then(LiteralArgumentBuilder.<S>literal("put").then(subCommand.then(subSubCommand)));
                        } else if (valueType.isEnum()) {
                            //noinspection rawtypes, unchecked
                            RequiredArgumentBuilder<S, ?> subSubCommand = RequiredArgumentBuilder.argument("value", string()).suggests(Platform.current.enumSuggestionProvider((Class) valueType));
                            subCommand.executes(ctx -> {
                                String value = getString(ctx, "value");
                                return put(ctx.getSource(), modConfig, config, getKey.apply(ctx), Arrays.stream(valueType.getEnumConstants()).filter(c -> ((Enum<?>) c).name().equals(value)).findAny().orElseThrow(() -> Platform.current.invalidEnumException().create(value)));
                            });
                            configLiteral.then(LiteralArgumentBuilder.<S>literal("put").then(subCommand.then(subSubCommand)));
                        }
                    }
                }

                if (modConfig.getRemovers().containsKey(config)) {
                    Config annotation = modConfig.getAnnotations().get(config);
                    Config.Remover remover = annotation.remover();
                    Class<?> type = remover.type() == Config.EMPTY.class ? (Class<?>) modConfig.getParameterTypes(config)[0] : remover.type();
                    var argumentFunction = modConfig.getArgument(type);
                    if (argumentFunction != null) {
                        RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", argumentFunction.apply(buildContext));
                        subCommand.executes(ctx -> remove(ctx.getSource(), modConfig, config, ctx.getArgument("value", type)));
                        configLiteral.then(LiteralArgumentBuilder.<S>literal("remove").then(subCommand));
                    } else if (type.isEnum()) {
                        //noinspection rawtypes, unchecked
                        RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", string()).suggests(Platform.current.enumSuggestionProvider((Class) type));
                        subCommand.executes(ctx -> {
                            String value = getString(ctx, "value");
                            return remove(ctx.getSource(), modConfig, config, Arrays.stream(type.getEnumConstants()).filter(c -> ((Enum<?>) c).name().equals(value)).findAny().orElseThrow(() -> Platform.current.invalidEnumException().create(value)));
                        });
                        configLiteral.then(LiteralArgumentBuilder.<S>literal("remove").then(subCommand));
                    }
                }

                identifierLiteral.then(configLiteral);
            }

            root.then(identifierLiteral);
        }
        return root;
    }

    protected abstract int comment(S source, String config, String comment);

    protected abstract int get(S source, ModConfigImpl<S, C> modConfig, String config);

    protected abstract int reset(S source, ModConfigImpl<S, C> modConfig, String config);

    protected abstract int set(S source, ModConfigImpl<S, C> modConfig, String config, Object value) throws CommandSyntaxException;

    protected abstract int add(S source, ModConfigImpl<S, C> modConfig, String config, Object value) throws CommandSyntaxException;

    protected abstract int put(S source, ModConfigImpl<S, C> modConfig, String config, Object key, Object value) throws CommandSyntaxException;

    protected abstract int remove(S source, ModConfigImpl<S, C> modConfig, String config, Object value) throws CommandSyntaxException;
}
