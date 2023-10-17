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
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.StringArgumentType.*;

public abstract class AbstractConfigCommand<S extends CommandSource>  {

    private static final DynamicCommandExceptionType INVALID_ENUM_EXCEPTION = new DynamicCommandExceptionType(value -> Text.translatable("argument.enum.invalid", value));

    private final String rootLiteral;

    protected AbstractConfigCommand(String rootLiteral) {
        this.rootLiteral = rootLiteral;
    }

    protected LiteralArgumentBuilder<S> create(CommandRegistryAccess registryAccess) {
        LiteralArgumentBuilder<S> root = LiteralArgumentBuilder.literal(this.rootLiteral);
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
                var argumentFunction = modConfig.getArgument(type);
                var suggestorPair = modConfig.getSuggestor(type);
                if (argumentFunction != null) {
                    RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", argumentFunction.apply(registryAccess));
                    subCommand.executes(ctx -> set(ctx.getSource(), modConfig, config, ctx.getArgument("value", type)));
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("set").then(subCommand));
                } else if (suggestorPair != null) {
                    RequiredArgumentBuilder<S, String> subCommand = RequiredArgumentBuilder.argument("value", greedyString());
                    //noinspection unchecked
                    subCommand.suggests((SuggestionProvider<S>) suggestorPair.left()).executes(ctx -> set(ctx.getSource(), modConfig, config, suggestorPair.right().apply(ctx, "value")));
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("set").then(subCommand));
                } else if (type.isEnum()) {
                    //noinspection rawtypes, unchecked
                    RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", string()).suggests(new EnumSuggestionProvider<>((Class) type));
                    subCommand.executes(ctx -> {
                        String value = getString(ctx, "value");
                        return set(ctx.getSource(), modConfig, config, Arrays.stream(type.getEnumConstants()).filter(c -> ((Enum<?>) c).name().equals(value)).findAny().orElseThrow(() -> INVALID_ENUM_EXCEPTION.create(value)));
                    });
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("set").then(subCommand));
                }
            });
            modConfig.getAdders().keySet().forEach(config -> {
                Config annotation = modConfig.getAnnotations().get(config);
                Config.Adder adder = annotation.adder();
                Class<?> type = adder.type() == Config.EMPTY.class ? (Class<?>) modConfig.getParameterTypes(config)[0] : adder.type();
                var argumentFunction = modConfig.getArgument(type);
                var suggestorPair = modConfig.getSuggestor(type);
                if (argumentFunction != null) {
                    RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", argumentFunction.apply(registryAccess));
                    subCommand.executes(ctx -> add(ctx.getSource(), modConfig, config, ctx.getArgument("value", type)));
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("add").then(subCommand));
                } else if (suggestorPair != null) {
                    RequiredArgumentBuilder<S, String> subCommand = RequiredArgumentBuilder.argument("value", greedyString());
                    //noinspection unchecked
                    subCommand.suggests((SuggestionProvider<S>) suggestorPair.left()).executes(ctx -> add(ctx.getSource(), modConfig, config, suggestorPair.right().apply(ctx, "value")));
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("add").then(subCommand));
                } else if (type.isEnum()) {
                    //noinspection rawtypes, unchecked
                    RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", string()).suggests(new EnumSuggestionProvider<>((Class) type));
                    subCommand.executes(ctx -> {
                        String value = getString(ctx, "value");
                        return add(ctx.getSource(), modConfig, config, Arrays.stream(type.getEnumConstants()).filter(c -> ((Enum<?>) c).name().equals(value)).findAny().orElseThrow(() -> INVALID_ENUM_EXCEPTION.create(value)));
                    });
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
                var keyArgumentFunction = modConfig.getArgument(keyType);
                var keySuggestorPair = modConfig.getSuggestor(keyType);
                if (keyArgumentFunction != null) {
                    subCommand = RequiredArgumentBuilder.argument("key", keyArgumentFunction.apply(registryAccess));
                    getKey = ctx -> ctx.getArgument("key", keyType);
                } else if (keySuggestorPair != null) {
                    subCommand = RequiredArgumentBuilder.argument("key", string());
                    //noinspection unchecked
                    subCommand.suggests((SuggestionProvider<S>) keySuggestorPair.left());
                    getKey = ctx -> keySuggestorPair.right().apply(ctx, "key");
                } else if (keyType.isEnum()) {
                    //noinspection rawtypes, unchecked
                    subCommand = RequiredArgumentBuilder.argument("key", string()).suggests(new EnumSuggestionProvider<>((Class) keyType));
                    getKey = ctx -> {
                        String value = getString(ctx, "key");
                        return Arrays.stream(keyType.getEnumConstants()).filter(c -> ((Enum<?>) c).name().equals(value)).findAny().orElseThrow(() -> INVALID_ENUM_EXCEPTION.create(value));
                    };
                } else {
                    return;
                }
                Class<?> valueType = putter.valueType() == Config.EMPTY.class ? (Class<?>) types[1] : putter.valueType();
                var valueArgumentFunction = modConfig.getArgument(valueType);
                var valueSuggestorPair = modConfig.getSuggestor(valueType);
                if (valueArgumentFunction != null) {
                    RequiredArgumentBuilder<S, ?> subSubCommand = RequiredArgumentBuilder.argument("value", valueArgumentFunction.apply(registryAccess));
                    subSubCommand.executes(ctx -> put(ctx.getSource(), modConfig, config, getKey.apply(ctx), ctx.getArgument("value", valueType)));
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("put").then(subCommand.then(subSubCommand)));
                } else if (valueSuggestorPair != null) {
                    RequiredArgumentBuilder<S, ?> subSubCommand = RequiredArgumentBuilder.argument("value", greedyString());
                    //noinspection unchecked
                    subSubCommand.suggests((SuggestionProvider<S>) valueSuggestorPair.left()).executes(ctx -> put(ctx.getSource(), modConfig, config, getKey.apply(ctx), valueSuggestorPair.right().apply(ctx, "value")));
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("put").then(subCommand.then(subSubCommand)));
                } else if (valueType.isEnum()) {
                    //noinspection rawtypes, unchecked
                    RequiredArgumentBuilder<S, ?> subSubCommand = RequiredArgumentBuilder.argument("value", string()).suggests(new EnumSuggestionProvider<>((Class) valueType));
                    subCommand.executes(ctx -> {
                        String value = getString(ctx, "value");
                        return put(ctx.getSource(), modConfig, config, getKey.apply(ctx), Arrays.stream(valueType.getEnumConstants()).filter(c -> ((Enum<?>) c).name().equals(value)).findAny().orElseThrow(() -> INVALID_ENUM_EXCEPTION.create(value)));
                    });
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("put").then(subCommand.then(subSubCommand)));
                }
            });
            modConfig.getRemovers().keySet().forEach(config -> {
                Config annotation = modConfig.getAnnotations().get(config);
                Config.Remover remover = annotation.remover();
                Class<?> type = remover.type() == Config.EMPTY.class ? (Class<?>) modConfig.getParameterTypes(config)[0] : remover.type();
                var argumentFunction = modConfig.getArgument(type);
                var suggestorPair = modConfig.getSuggestor(type);
                if (argumentFunction != null) {
                    RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", argumentFunction.apply(registryAccess));
                    subCommand.executes(ctx -> remove(ctx.getSource(), modConfig, config, ctx.getArgument("value", type)));
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("remove").then(subCommand));
                } else if (suggestorPair != null) {
                    RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", greedyString());
                    //noinspection unchecked
                    subCommand.suggests((SuggestionProvider<S>) suggestorPair.left()).executes(ctx -> remove(ctx.getSource(), modConfig, config, suggestorPair.right().apply(ctx, "value")));
                    literals.get(config).then(LiteralArgumentBuilder.<S>literal("remove").then(subCommand));
                } else if (type.isEnum()) {
                    //noinspection rawtypes, unchecked
                    RequiredArgumentBuilder<S, ?> subCommand = RequiredArgumentBuilder.argument("value", string()).suggests(new EnumSuggestionProvider<>((Class) type));
                    subCommand.executes(ctx -> {
                        String value = getString(ctx, "value");
                        return remove(ctx.getSource(), modConfig, config, Arrays.stream(type.getEnumConstants()).filter(c -> ((Enum<?>) c).name().equals(value)).findAny().orElseThrow(() -> INVALID_ENUM_EXCEPTION.create(value)));
                    });
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
