package dev.xpple.betterconfig.impl;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.xpple.betterconfig.api.ModConfig;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Pair;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static dev.xpple.betterconfig.BetterConfig.LOGGER;
import static dev.xpple.betterconfig.BetterConfig.MOD_PATH;

public class ModConfigImpl implements ModConfig {

    private static final Map<Class<?>, Pair<?, ?>> defaultArguments = ImmutableMap.<Class<?>, Pair<?, ?>>builder()
        .put(boolean.class, new Pair<>((Supplier<ArgumentType<Boolean>>) BoolArgumentType::bool, (CommandContextBiFunction<Boolean>) BoolArgumentType::getBool))
        .put(Boolean.class, new Pair<>((Supplier<ArgumentType<Boolean>>) BoolArgumentType::bool, (CommandContextBiFunction<Boolean>) BoolArgumentType::getBool))
        .put(double.class, new Pair<>((Supplier<ArgumentType<Double>>) DoubleArgumentType::doubleArg, (CommandContextBiFunction<Double>) DoubleArgumentType::getDouble))
        .put(Double.class, new Pair<>((Supplier<ArgumentType<Double>>) DoubleArgumentType::doubleArg, (CommandContextBiFunction<Double>) DoubleArgumentType::getDouble))
        .put(float.class, new Pair<>((Supplier<ArgumentType<Float>>) FloatArgumentType::floatArg, (CommandContextBiFunction<Float>) FloatArgumentType::getFloat))
        .put(Float.class, new Pair<>((Supplier<ArgumentType<Float>>) FloatArgumentType::floatArg, (CommandContextBiFunction<Float>) FloatArgumentType::getFloat))
        .put(int.class, new Pair<>((Supplier<ArgumentType<Integer>>) IntegerArgumentType::integer, (CommandContextBiFunction<Integer>) IntegerArgumentType::getInteger))
        .put(Integer.class, new Pair<>((Supplier<ArgumentType<Integer>>) IntegerArgumentType::integer, (CommandContextBiFunction<Integer>) IntegerArgumentType::getInteger))
        .put(long.class, new Pair<>((Supplier<ArgumentType<Long>>) LongArgumentType::longArg, (CommandContextBiFunction<Long>) LongArgumentType::getLong))
        .put(Long.class, new Pair<>((Supplier<ArgumentType<Long>>) LongArgumentType::longArg, (CommandContextBiFunction<Long>) LongArgumentType::getLong))
        .put(String.class, new Pair<>((Supplier<ArgumentType<String>>) StringArgumentType::string, (CommandContextBiFunction<String>) StringArgumentType::getString))
        .build();

    private final String modId;
    private final Class<?> configsClass;

    private final Gson gson;
    private final Gson inlineGson;
    private final Map<Class<?>, Pair<?, ?>> arguments;
    private final Map<Class<?>, Pair<?, ?>> suggestors;

    public ModConfigImpl(String modId, Class<?> configsClass, Gson gson, Map<Class<?>, Pair<?, ?>> arguments, Map<Class<?>, Pair<?, ?>> suggestors) {
        this.modId = modId;
        this.configsClass = configsClass;
        this.gson = gson.newBuilder().setPrettyPrinting().create();
        this.inlineGson = gson;
        this.arguments = arguments;
        this.suggestors = suggestors;
    }

    @Override
    public String getModId() {
        return this.modId;
    }

    @Override
    public Class<?> getConfigsClass() {
        return this.configsClass;
    }

    public Gson getGson() {
        return this.gson;
    }

    @SuppressWarnings("unchecked")
    public <T> Pair<Supplier<? extends ArgumentType<T>>, CommandContextBiFunction<T>> getArgument(Class<T> type) {
        return (Pair<Supplier<? extends ArgumentType<T>>, CommandContextBiFunction<T>>) this.arguments.getOrDefault(type, defaultArguments.get(type));
    }

    @SuppressWarnings("unchecked")
    public <T> Pair<Supplier<SuggestionProvider<? extends CommandSource>>, CommandContextBiFunction<T>> getSuggestor(Class<T> type) {
        return (Pair<Supplier<SuggestionProvider<? extends CommandSource>>, CommandContextBiFunction<T>>) this.suggestors.get(type);
    }

    @Override
    public Path getConfigsPath() {
        return MOD_PATH.resolve(this.modId).resolve("config.json");
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
    public void set(String config, Object value) throws CommandSyntaxException {
        CommandContextConsumer<Object> setter = this.setters.get(config);
        if (setter == null) {
            throw new IllegalArgumentException();
        }
        setter.accept(value);
        this.save();
    }

    @Override
    public void add(String config, Object value) throws CommandSyntaxException {
        CommandContextConsumer<Object> adder = this.adders.get(config);
        if (adder == null) {
            throw new IllegalArgumentException();
        }
        adder.accept(value);
        this.save();
    }

    @Override
    public void put(String config, Object key, Object value) throws CommandSyntaxException {
        CommandContextBiConsumer<Object, Object> putter = this.putters.get(config);
        if (putter == null) {
            throw new IllegalArgumentException();
        }
        putter.accept(key, value);
        this.save();
    }

    @Override
    public void remove(String config, Object value) throws CommandSyntaxException {
        CommandContextConsumer<Object> remover = this.removers.get(config);
        if (remover == null) {
            throw new IllegalArgumentException();
        }
        remover.accept(value);
        this.save();
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

    private void save() {
        //noinspection ResultOfMethodCallIgnored
        this.getConfigsPath().getParent().toFile().mkdirs();
        try (BufferedWriter writer = Files.newBufferedWriter(this.getConfigsPath())) {
            JsonObject root = new JsonObject();
            this.getConfigs().keySet().forEach(config -> {
                Object value = this.get(config);
                root.add(config, gson.toJsonTree(value));
            });
            writer.write(gson.toJson(root));
        } catch (IOException e) {
            LOGGER.error("Could not save config file.");
            e.printStackTrace();
        }
    }

    public Map<String, Field> getConfigs() {
        return this.configs;
    }

    public Map<String, CommandContextConsumer<Object>> getSetters() {
        return this.setters;
    }

    public Map<String, CommandContextConsumer<Object>> getAdders() {
        return this.adders;
    }

    public Map<String, CommandContextBiConsumer<Object, Object>> getPutters() {
        return this.putters;
    }

    public Map<String, CommandContextConsumer<Object>> getRemovers() {
        return this.removers;
    }

    private final Map<String, Field> configs = new HashMap<>();
    private final Map<String, CommandContextConsumer<Object>> setters = new HashMap<>();
    private final Map<String, CommandContextConsumer<Object>> adders = new HashMap<>();
    private final Map<String, CommandContextBiConsumer<Object, Object>> putters = new HashMap<>();
    private final Map<String, CommandContextConsumer<Object>> removers = new HashMap<>();
}
