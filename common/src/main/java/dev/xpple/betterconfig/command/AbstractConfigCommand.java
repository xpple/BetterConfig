package dev.xpple.betterconfig.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.xpple.betterconfig.api.Config;
import dev.xpple.betterconfig.impl.AbstractConfigImpl;
import dev.xpple.betterconfig.util.CheckedFunction;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.StringArgumentType.*;

public abstract class AbstractConfigCommand<S, C>  {

    protected abstract <T extends Enum<T>> SuggestionProvider<S> enumSuggestionProvider(Class<T> type);
    protected abstract DynamicCommandExceptionType invalidEnumException();

    private final String rootLiteral;

    protected AbstractConfigCommand(String rootLiteral) {
        this.rootLiteral = rootLiteral;
    }

    protected final LiteralArgumentBuilder<S> create(Collection<? extends AbstractConfigImpl<S, C>> abstractConfigs, C buildContext) {
        LiteralArgumentBuilder<S> root = LiteralArgumentBuilder.literal(this.rootLiteral);
        for (AbstractConfigImpl<S, C> abstractConfig : abstractConfigs) {
            Map<String, LiteralArgumentBuilder<S>> literals = new HashMap<>();
            for (String config : abstractConfig.getConfigs().keySet()) {
                Predicate<S> condition = abstractConfig.getConditions().get(config);
                LiteralArgumentBuilder<S> configLiteral = LiteralArgumentBuilder.<S>literal(config).requires(condition);
                literals.put(config, configLiteral);

                configLiteral.then(LiteralArgumentBuilder.<S>literal("get").executes(ctx -> get(ctx.getSource(), abstractConfig, config)));
                configLiteral.then(LiteralArgumentBuilder.<S>literal("reset").executes(ctx -> reset(ctx.getSource(), abstractConfig, config)));
            }

            abstractConfig.getComments().forEach((config, comment) -> literals.get(config).then(LiteralArgumentBuilder.<S>literal("comment").executes(ctx -> comment(ctx.getSource(), config, comment))));
            abstractConfig.getSetters().keySet().forEach(config -> {
                Config annotation = abstractConfig.getAnnotations().get(config);
                Config.Setter setter = annotation.setter();
                Class<?> type = setter.type() == Config.EMPTY.class ? abstractConfig.getType(config) : setter.type();
                var argumentFunction = abstractConfig.getArgument(type);
                if (argumentFunction != null) {
                    RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", argumentFunction.apply(buildContext));
                    subCommand.executes(ctx -> set(ctx.getSource(), abstractConfig, config, ctx.getArgument("value", type)));
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("set").then(subCommand));
                } else if (type.isEnum()) {
                    //noinspection rawtypes, unchecked
                    RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", string()).suggests(this.enumSuggestionProvider((Class) type));
                    subCommand.executes(ctx -> {
                        String value = getString(ctx, "value");
                        return set(ctx.getSource(), abstractConfig, config, Arrays.stream(type.getEnumConstants()).filter(c -> ((Enum<?>) c).name().equals(value)).findAny().orElseThrow(() -> this.invalidEnumException().create(value)));
                    });
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("set").then(subCommand));
                }
            });
            abstractConfig.getAdders().keySet().forEach(config -> {
                Config annotation = abstractConfig.getAnnotations().get(config);
                Config.Adder adder = annotation.adder();
                Class<?> type = adder.type() == Config.EMPTY.class ? (Class<?>) abstractConfig.getParameterTypes(config)[0] : adder.type();
                var argumentFunction = abstractConfig.getArgument(type);
                if (argumentFunction != null) {
                    RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", argumentFunction.apply(buildContext));
                    subCommand.executes(ctx -> add(ctx.getSource(), abstractConfig, config, ctx.getArgument("value", type)));
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("add").then(subCommand));
                } else if (type.isEnum()) {
                    //noinspection rawtypes, unchecked
                    RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", string()).suggests(this.enumSuggestionProvider((Class) type));
                    subCommand.executes(ctx -> {
                        String value = getString(ctx, "value");
                        return add(ctx.getSource(), abstractConfig, config, Arrays.stream(type.getEnumConstants()).filter(c -> ((Enum<?>) c).name().equals(value)).findAny().orElseThrow(() -> this.invalidEnumException().create(value)));
                    });
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("add").then(subCommand));
                }
            });
            abstractConfig.getPutters().keySet().forEach(config -> {
                Config annotation = abstractConfig.getAnnotations().get(config);
                Config.Putter putter = annotation.putter();
                Type[] types = abstractConfig.getParameterTypes(config);
                Class<?> keyType = putter.keyType() == Config.EMPTY.class ? (Class<?>) types[0] : putter.keyType();
                RequiredArgumentBuilder<S, ?> subCommand;
                CheckedFunction<CommandContext<S>, ?, CommandSyntaxException> getKey;
                var keyArgumentFunction = abstractConfig.getArgument(keyType);
                if (keyArgumentFunction != null) {
                    subCommand = RequiredArgumentBuilder.argument("key", keyArgumentFunction.apply(buildContext));
                    getKey = ctx -> ctx.getArgument("key", keyType);
                } else if (keyType.isEnum()) {
                    //noinspection rawtypes, unchecked
                    subCommand = RequiredArgumentBuilder.argument("key", string()).suggests(this.enumSuggestionProvider((Class) keyType));
                    getKey = ctx -> {
                        String value = getString(ctx, "key");
                        return Arrays.stream(keyType.getEnumConstants()).filter(c -> ((Enum<?>) c).name().equals(value)).findAny().orElseThrow(() -> this.invalidEnumException().create(value));
                    };
                } else {
                    return;
                }
                Class<?> valueType = putter.valueType() == Config.EMPTY.class ? (Class<?>) types[1] : putter.valueType();
                var valueArgumentFunction = abstractConfig.getArgument(valueType);
                if (valueArgumentFunction != null) {
                    RequiredArgumentBuilder<S, ?> subSubCommand = RequiredArgumentBuilder.argument("value", valueArgumentFunction.apply(buildContext));
                    subSubCommand.executes(ctx -> put(ctx.getSource(), abstractConfig, config, getKey.apply(ctx), ctx.getArgument("value", valueType)));
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("put").then(subCommand.then(subSubCommand)));
                } else if (valueType.isEnum()) {
                    //noinspection rawtypes, unchecked
                    RequiredArgumentBuilder<S, ?> subSubCommand = RequiredArgumentBuilder.argument("value", string()).suggests(this.enumSuggestionProvider((Class) valueType));
                    subCommand.executes(ctx -> {
                        String value = getString(ctx, "value");
                        return put(ctx.getSource(), abstractConfig, config, getKey.apply(ctx), Arrays.stream(valueType.getEnumConstants()).filter(c -> ((Enum<?>) c).name().equals(value)).findAny().orElseThrow(() -> this.invalidEnumException().create(value)));
                    });
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("put").then(subCommand.then(subSubCommand)));
                }
            });
            abstractConfig.getRemovers().keySet().forEach(config -> {
                Config annotation = abstractConfig.getAnnotations().get(config);
                Config.Remover remover = annotation.remover();
                Class<?> type = remover.type() == Config.EMPTY.class ? (Class<?>) abstractConfig.getParameterTypes(config)[0] : remover.type();
                var argumentFunction = abstractConfig.getArgument(type);
                if (argumentFunction != null) {
                    RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", argumentFunction.apply(buildContext));
                    subCommand.executes(ctx -> remove(ctx.getSource(), abstractConfig, config, ctx.getArgument("value", type)));
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("remove").then(subCommand));
                } else if (type.isEnum()) {
                    //noinspection rawtypes, unchecked
                    RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", string()).suggests(this.enumSuggestionProvider((Class) type));
                    subCommand.executes(ctx -> {
                        String value = getString(ctx, "value");
                        return remove(ctx.getSource(), abstractConfig, config, Arrays.stream(type.getEnumConstants()).filter(c -> ((Enum<?>) c).name().equals(value)).findAny().orElseThrow(() -> this.invalidEnumException().create(value)));
                    });
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("remove").then(subCommand));
                }
            });
            literals.values().forEach(literal -> root.then(LiteralArgumentBuilder.<S>literal(abstractConfig.getIdentifier()).then(literal)));
        }
        return root;
    }

    protected abstract int comment(S source, String config, String comment);

    protected abstract int get(S source, AbstractConfigImpl<S, C> abstractConfig, String config);

    protected abstract int reset(S source, AbstractConfigImpl<S, C> abstractConfig, String config);

    protected abstract int set(S source, AbstractConfigImpl<S, C> abstractConfig, String config, Object value) throws CommandSyntaxException;

    protected abstract int add(S source, AbstractConfigImpl<S, C> abstractConfig, String config, Object value) throws CommandSyntaxException;

    protected abstract int put(S source, AbstractConfigImpl<S, C> abstractConfig, String config, Object key, Object value) throws CommandSyntaxException;

    protected abstract int remove(S source, AbstractConfigImpl<S, C> abstractConfig, String config, Object value) throws CommandSyntaxException;
}
