package dev.xpple.betterconfig.impl;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.xpple.betterconfig.api.AbstractConfig;
import dev.xpple.betterconfig.api.Config;
import dev.xpple.betterconfig.util.CheckedBiConsumer;
import dev.xpple.betterconfig.util.CheckedBiFunction;
import dev.xpple.betterconfig.util.CheckedConsumer;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static dev.xpple.betterconfig.BetterConfigCommon.LOGGER;

public abstract class AbstractConfigImpl<S> implements AbstractConfig {

    private final Map<Class<?>, Pair<?, ?>> defaultArguments = ImmutableMap.<Class<?>, Pair<?, ?>>builder()
        .put(boolean.class, Pair.of((Supplier<ArgumentType<Boolean>>) BoolArgumentType::bool, (CheckedBiFunction<CommandContext<? extends S>, String, Boolean, CommandSyntaxException>) BoolArgumentType::getBool))
        .put(Boolean.class, Pair.of((Supplier<ArgumentType<Boolean>>) BoolArgumentType::bool, (CheckedBiFunction<CommandContext<? extends S>, String, Boolean, CommandSyntaxException>) BoolArgumentType::getBool))
        .put(double.class, Pair.of((Supplier<ArgumentType<Double>>) DoubleArgumentType::doubleArg, (CheckedBiFunction<CommandContext<? extends S>, String, Double, CommandSyntaxException>) DoubleArgumentType::getDouble))
        .put(Double.class, Pair.of((Supplier<ArgumentType<Double>>) DoubleArgumentType::doubleArg, (CheckedBiFunction<CommandContext<? extends S>, String, Double, CommandSyntaxException>) DoubleArgumentType::getDouble))
        .put(float.class, Pair.of((Supplier<ArgumentType<Float>>) FloatArgumentType::floatArg, (CheckedBiFunction<CommandContext<? extends S>, String, Float, CommandSyntaxException>) FloatArgumentType::getFloat))
        .put(Float.class, Pair.of((Supplier<ArgumentType<Float>>) FloatArgumentType::floatArg, (CheckedBiFunction<CommandContext<? extends S>, String, Float, CommandSyntaxException>) FloatArgumentType::getFloat))
        .put(int.class, Pair.of((Supplier<ArgumentType<Integer>>) IntegerArgumentType::integer, (CheckedBiFunction<CommandContext<? extends S>, String, Integer, CommandSyntaxException>) IntegerArgumentType::getInteger))
        .put(Integer.class, Pair.of((Supplier<ArgumentType<Integer>>) IntegerArgumentType::integer, (CheckedBiFunction<CommandContext<? extends S>, String, Integer, CommandSyntaxException>) IntegerArgumentType::getInteger))
        .put(long.class, Pair.of((Supplier<ArgumentType<Long>>) LongArgumentType::longArg, (CheckedBiFunction<CommandContext<? extends S>, String, Long, CommandSyntaxException>) LongArgumentType::getLong))
        .put(Long.class, Pair.of((Supplier<ArgumentType<Long>>) LongArgumentType::longArg, (CheckedBiFunction<CommandContext<? extends S>, String, Long, CommandSyntaxException>) LongArgumentType::getLong))
        .put(String.class, Pair.of((Supplier<ArgumentType<String>>) StringArgumentType::string, (CheckedBiFunction<CommandContext<? extends S>, String, String, CommandSyntaxException>) StringArgumentType::getString))
        .build();

    private final Class<?> configsClass;

    private final Gson gson;
    private final Gson inlineGson;
    private final Map<Class<?>, Pair<?, ?>> arguments;
    private final Map<Class<?>, Pair<?, ?>> suggestors;

    protected AbstractConfigImpl(Class<?> configsClass, Gson gson, Map<Class<?>, Pair<?, ?>> arguments, Map<Class<?>, Pair<?, ?>> suggestors) {
        this.configsClass = configsClass;
        this.gson = gson.newBuilder().setPrettyPrinting().create();
        this.inlineGson = gson;
        this.arguments = arguments;
        this.suggestors = suggestors;
    }

    public abstract String getIdentifier();

    @Override
    public Class<?> getConfigsClass() {
        return this.configsClass;
    }

    public Gson getGson() {
        return this.gson;
    }

    @SuppressWarnings("unchecked")
    public <T> Pair<Supplier<ArgumentType<T>>, CheckedBiFunction<CommandContext<?>, String, T, CommandSyntaxException>> getArgument(Class<T> type) {
        return (Pair<Supplier<ArgumentType<T>>, CheckedBiFunction<CommandContext<?>, String, T, CommandSyntaxException>>) this.arguments.getOrDefault(type, defaultArguments.get(type));
    }

    @SuppressWarnings("unchecked")
    public <T> Pair<Supplier<SuggestionProvider<?>>, CheckedBiFunction<CommandContext<?>, String, T, CommandSyntaxException>> getSuggestor(Class<T> type) {
        return (Pair<Supplier<SuggestionProvider<?>>, CheckedBiFunction<CommandContext<?>, String, T, CommandSyntaxException>>) this.suggestors.get(type);
    }

    @Override
    public Object get(String config) {
        Field field = this.configs.get(config);
        if (field == null) {
            throw new IllegalArgumentException();
        }
        try {
            return field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public String asString(String config) {
        Object value = this.get(config);
        return this.asString(value);
    }

    public String asString(Object value) {
        return this.inlineGson.toJson(value);
    }

    @Override
    public void reset(String config) {
        Field field = this.configs.get(config);
        if (field == null) {
            throw new IllegalArgumentException();
        }
        try {
            field.set(null, this.defaults.get(config));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
        this.save();
    }

    @Override
    public void set(String config, Object value) throws CommandSyntaxException {
        CheckedConsumer<Object, CommandSyntaxException> setter = this.setters.get(config);
        if (setter == null) {
            throw new IllegalArgumentException();
        }
        setter.accept(value);
        this.save();
    }

    @Override
    public void add(String config, Object value) throws CommandSyntaxException {
        CheckedConsumer<Object, CommandSyntaxException> adder = this.adders.get(config);
        if (adder == null) {
            throw new IllegalArgumentException();
        }
        adder.accept(value);
        this.save();
    }

    @Override
    public void put(String config, Object key, Object value) throws CommandSyntaxException {
        CheckedBiConsumer<Object, Object, CommandSyntaxException> putter = this.putters.get(config);
        if (putter == null) {
            throw new IllegalArgumentException();
        }
        putter.accept(key, value);
        this.save();
    }

    @Override
    public void remove(String config, Object value) throws CommandSyntaxException {
        CheckedConsumer<Object, CommandSyntaxException> remover = this.removers.get(config);
        if (remover == null) {
            throw new IllegalArgumentException();
        }
        remover.accept(value);
        this.save();
    }

    @Override
    public void resetTemporaryConfigs() {
        for (String config : this.configs.keySet()) {
            if (this.annotations.get(config).temporary()) {
                this.reset(config);
            }
        }
    }

    public Class<?> getType(String config) {
        Field field = this.configs.get(config);
        if (field == null) {
            throw new IllegalArgumentException();
        }
        return field.getType();
    }

    public Type[] getParameterTypes(String config) {
        Field field = this.configs.get(config);
        if (field == null) {
            throw new IllegalArgumentException();
        }
        return ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
    }

    @Override
    public boolean save() {
        try (BufferedWriter writer = Files.newBufferedWriter(this.getConfigsPath())) {
            JsonObject root = new JsonObject();
            this.getConfigs().keySet().forEach(config -> {
                if (this.getAnnotations().get(config).temporary()) {
                    return;
                }
                Object value = this.get(config);
                root.add(config, this.gson.toJsonTree(value));
            });
            writer.write(this.gson.toJson(root));
        } catch (IOException e) {
            LOGGER.error("Could not save config file.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Map<String, Field> getConfigs() {
        return this.configs;
    }

    public Map<String, Object> getDefaults() {
        return this.defaults;
    }

    public Map<String, String> getComments() {
        return this.comments;
    }

    public Map<String, CheckedConsumer<Object, CommandSyntaxException>> getSetters() {
        return this.setters;
    }

    public Map<String, CheckedConsumer<Object, CommandSyntaxException>> getAdders() {
        return this.adders;
    }

    public Map<String, CheckedBiConsumer<Object, Object, CommandSyntaxException>> getPutters() {
        return this.putters;
    }

    public Map<String, CheckedConsumer<Object, CommandSyntaxException>> getRemovers() {
        return this.removers;
    }

    public Map<String, Predicate<S>> getConditions() {
        return this.conditions;
    }

    public Map<String, Config> getAnnotations() {
        return this.annotations;
    }

    private final Map<String, Field> configs = new HashMap<>();
    private final Map<String, Object> defaults = new HashMap<>();
    private final Map<String, String> comments = new HashMap<>();
    private final Map<String, CheckedConsumer<Object, CommandSyntaxException>> setters = new HashMap<>();
    private final Map<String, CheckedConsumer<Object, CommandSyntaxException>> adders = new HashMap<>();
    private final Map<String, CheckedBiConsumer<Object, Object, CommandSyntaxException>> putters = new HashMap<>();
    private final Map<String, CheckedConsumer<Object, CommandSyntaxException>> removers = new HashMap<>();
    private final Map<String, Predicate<S>> conditions = new HashMap<>();
    private final Map<String, Config> annotations = new HashMap<>();
}
