package dev.xpple.betterconfig.impl;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.BetterConfigCommon;
import dev.xpple.betterconfig.api.AbstractConfig;
import dev.xpple.betterconfig.api.Config;
import dev.xpple.betterconfig.util.CheckedBiConsumer;
import dev.xpple.betterconfig.util.CheckedConsumer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractConfigImpl<S, C> implements AbstractConfig {

    private static final Map<Class<?>, Function<?, ArgumentType<?>>> defaultArguments = ImmutableMap.<Class<?>, Function<?, ArgumentType<?>>>builder()
        .put(boolean.class, buildContext -> BoolArgumentType.bool())
        .put(Boolean.class, buildContext -> BoolArgumentType.bool())
        .put(double.class, buildContext -> DoubleArgumentType.doubleArg())
        .put(Double.class, buildContext -> DoubleArgumentType.doubleArg())
        .put(float.class, buildContext -> FloatArgumentType.floatArg())
        .put(Float.class, buildContext -> FloatArgumentType.floatArg())
        .put(int.class, buildContext -> IntegerArgumentType.integer())
        .put(Integer.class, buildContext -> IntegerArgumentType.integer())
        .put(long.class, buildContext -> LongArgumentType.longArg())
        .put(Long.class, buildContext -> LongArgumentType.longArg())
        .put(String.class, buildContext -> StringArgumentType.string())
        .build();

    private final Class<?> configsClass;

    private final Gson gson;
    private final Gson inlineGson;
    private final Map<Class<?>, Function<C, ? extends ArgumentType<?>>> arguments;

    protected AbstractConfigImpl(Class<?> configsClass, Gson gson, Map<Class<?>, Function<C, ? extends ArgumentType<?>>> arguments) {
        this.configsClass = configsClass;
        this.gson = gson.newBuilder().setPrettyPrinting().create();
        this.inlineGson = gson;
        this.arguments = arguments;
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
    public Function<C, ? extends ArgumentType<?>> getArgument(Class<?> type) {
        return this.arguments.getOrDefault(type, (Function<C, ? extends ArgumentType<?>>) defaultArguments.get(type));
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
            field.set(null, this.gson.fromJson(this.gson.toJsonTree(this.defaults.get(config)), field.getGenericType()));
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
            BetterConfigCommon.LOGGER.error("Could not save config file.", e);
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
