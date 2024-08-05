package dev.xpple.betterconfig.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.BetterConfigCommon;
import dev.xpple.betterconfig.api.Config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class BetterConfigInternals {

    public static void init(ModConfigImpl<?, ?> modConfig) {
        JsonObject root = null;
        try (BufferedReader reader = Files.newBufferedReader(modConfig.getConfigsPath())) {
            root = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException ignored) {
        } catch (Exception e) {
            BetterConfigCommon.LOGGER.warn("Could not read config file, default values will be used.\nThe old config file will be renamed.", e);
            try {
                Files.move(modConfig.getConfigsPath(), modConfig.getConfigsPath().resolveSibling("config_old.json"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
            }
        } finally {
            root = Objects.requireNonNullElse(root, new JsonObject());
        }

        for (Field field : modConfig.getConfigsClass().getDeclaredFields()) {
            Config annotation = field.getAnnotation(Config.class);
            if (annotation == null) {
                continue;
            }

            field.setAccessible(true);

            String fieldName = field.getName();
            modConfig.getConfigs().put(fieldName, field);
            try {
                modConfig.getDefaults().put(fieldName, modConfig.getGson().fromJson(modConfig.getGson().toJsonTree(field.get(null)), field.getGenericType()));
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
            modConfig.getAnnotations().put(fieldName, annotation);

            if (!annotation.comment().isEmpty()) {
                modConfig.getComments().put(fieldName, annotation.comment());
            }

            if (!annotation.temporary()) {
                try {
                    if (root.has(fieldName)) {
                        Object value = modConfig.getGson().fromJson(root.get(fieldName), field.getGenericType());
                        if (Modifier.isFinal(field.getModifiers())) {
                            throw new AssertionError("Config field '" + fieldName + "' should not be final");
                        }
                        field.set(null, value);
                    } else {
                        root.add(fieldName, modConfig.getGson().toJsonTree(field.get(null)));
                    }
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }

            if (annotation.condition().isEmpty()) {
                modConfig.getConditions().put(fieldName, source -> true);
            } else {
                Method predicateMethod;
                boolean hasParameter = false;
                try {
                    predicateMethod = modConfig.getConfigsClass().getDeclaredMethod(annotation.condition());
                } catch (ReflectiveOperationException e) {
                    hasParameter = true;
                    try {
                        Class<?> commandSourceClass = Platform.current.getCommandSourceClass();
                        predicateMethod = modConfig.getConfigsClass().getDeclaredMethod(annotation.condition(), commandSourceClass);
                    } catch (ReflectiveOperationException e1) {
                        throw new AssertionError(e1);
                    }
                }
                if (predicateMethod.getReturnType() != boolean.class) {
                    throw new AssertionError("Condition method '" + annotation.condition() + "' does not return boolean");
                }
                if (!Modifier.isStatic(predicateMethod.getModifiers())) {
                    throw new AssertionError("Condition method '" + annotation.condition() + "' is not static");
                }
                predicateMethod.setAccessible(true);

                Method predicateMethod_f = predicateMethod;

                if (hasParameter) {
                    modConfig.getConditions().put(fieldName, source -> {
                        try {
                            return (Boolean) predicateMethod_f.invoke(null, source);
                        } catch (ReflectiveOperationException e) {
                            throw new AssertionError(e);
                        }
                    });
                } else {
                    modConfig.getConditions().put(fieldName, source -> {
                        try {
                            return (Boolean) predicateMethod_f.invoke(null);
                        } catch (ReflectiveOperationException e) {
                            throw new AssertionError(e);
                        }
                    });
                }
            }

            if (annotation.readOnly()) {
                continue;
            }
            Class<?> type = field.getType();
            if (Collection.class.isAssignableFrom(type)) {
                initCollection(modConfig, field, annotation);
            } else if (Map.class.isAssignableFrom(type)) {
                initMap(modConfig, field, annotation);
            } else {
                initObject(modConfig, field, annotation);
            }
        }

        //noinspection ResultOfMethodCallIgnored
        modConfig.getConfigsPath().getParent().toFile().mkdirs();
        try (BufferedWriter writer = Files.newBufferedWriter(modConfig.getConfigsPath())) {
            writer.write(modConfig.getGson().toJson(root));
        } catch (IOException e) {
            BetterConfigCommon.LOGGER.error("Could not save config file.", e);
        }
    }

    private static void initCollection(ModConfigImpl<?, ?> modConfig, Field field, Config annotation) {
        String fieldName = field.getName();
        Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
        Config.Adder adder = annotation.adder();
        String adderMethodName = adder.value();
        //noinspection StatementWithEmptyBody
        if (adderMethodName.equals("none")) {
        } else if (adderMethodName.isEmpty()) {
            Method add;
            try {
                add = Collection.class.getDeclaredMethod("add", Object.class);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
            modConfig.getAdders().put(fieldName, value -> {
                try {
                    add.invoke(field.get(null), value);
                } catch (ReflectiveOperationException e) {
                    throw new AssertionError(e);
                }
            });
        } else {
            Class<?> type = adder.type() == Config.EMPTY.class ? (Class<?>) types[0] : adder.type();
            Method adderMethod;
            try {
                adderMethod = modConfig.getConfigsClass().getDeclaredMethod(adderMethodName, type);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
            adderMethod.setAccessible(true);
            modConfig.getAdders().put(fieldName, value -> {
                try {
                    adderMethod.invoke(null, value);
                } catch (ReflectiveOperationException e) {
                    if (e.getCause() instanceof CommandSyntaxException commandSyntaxException) {
                        throw commandSyntaxException;
                    }
                    throw new AssertionError(e);
                }
            });
        }
        Config.Remover remover = annotation.remover();
        String removerMethodName = remover.value();
        //noinspection StatementWithEmptyBody
        if (removerMethodName.equals("none")) {
        } else if (removerMethodName.isEmpty()) {
            Method remove;
            try {
                remove = Collection.class.getDeclaredMethod("remove", Object.class);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
            modConfig.getRemovers().put(fieldName, value -> {
                try {
                    remove.invoke(field.get(null), value);
                } catch (ReflectiveOperationException e) {
                    throw new AssertionError(e);
                }
            });
        } else {
            Class<?> type = remover.type() == Config.EMPTY.class ? (Class<?>) types[0] : remover.type();
            Method removerMethod;
            try {
                removerMethod = modConfig.getConfigsClass().getDeclaredMethod(removerMethodName, type);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
            removerMethod.setAccessible(true);
            modConfig.getRemovers().put(fieldName, value -> {
                try {
                    removerMethod.invoke(null, value);
                } catch (ReflectiveOperationException e) {
                    if (e.getCause() instanceof CommandSyntaxException commandSyntaxException) {
                        throw commandSyntaxException;
                    }
                    throw new AssertionError(e);
                }
            });
        }
    }

    private static void initMap(ModConfigImpl<?, ?> modConfig, Field field, Config annotation) {
        String fieldName = field.getName();
        Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
        Config.Adder adder = annotation.adder();
        String adderMethodName = adder.value();
        //noinspection StatementWithEmptyBody
        if (adderMethodName.equals("none")) {
        } else if (!adderMethodName.isEmpty()) {
            Class<?> type = adder.type() == Config.EMPTY.class ? (Class<?>) types[0] : adder.type();
            Method adderMethod;
            try {
                adderMethod = modConfig.getConfigsClass().getDeclaredMethod(adderMethodName, type);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
            adderMethod.setAccessible(true);
            modConfig.getAdders().put(fieldName, key -> {
                try {
                    adderMethod.invoke(null, key);
                } catch (ReflectiveOperationException e) {
                    if (e.getCause() instanceof CommandSyntaxException commandSyntaxException) {
                        throw commandSyntaxException;
                    }
                    throw new AssertionError(e);
                }
            });
        }
        Config.Putter putter = annotation.putter();
        String putterMethodName = putter.value();
        //noinspection StatementWithEmptyBody
        if (putterMethodName.equals("none")) {
        } else if (putterMethodName.isEmpty()) {
            Method put;
            try {
                put = Map.class.getDeclaredMethod("put", Object.class, Object.class);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
            modConfig.getPutters().put(fieldName, (key, value) -> {
                try {
                    put.invoke(field.get(null), key, value);
                } catch (ReflectiveOperationException e) {
                    throw new AssertionError(e);
                }
            });
        } else {
            Class<?> keyType = putter.keyType() == Config.EMPTY.class ? (Class<?>) types[0] : putter.keyType();
            Class<?> valueType = putter.valueType() == Config.EMPTY.class ? (Class<?>) types[1] : putter.valueType();
            Method putterMethod;
            try {
                putterMethod = modConfig.getConfigsClass().getDeclaredMethod(putterMethodName, keyType, valueType);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
            putterMethod.setAccessible(true);
            modConfig.getPutters().put(fieldName, (key, value) -> {
                try {
                    putterMethod.invoke(null, key, value);
                } catch (ReflectiveOperationException e) {
                    if (e.getCause() instanceof CommandSyntaxException commandSyntaxException) {
                        throw commandSyntaxException;
                    }
                    throw new AssertionError(e);
                }
            });
        }
        Config.Remover remover = annotation.remover();
        String removerMethodName = remover.value();
        //noinspection StatementWithEmptyBody
        if (removerMethodName.equals("none")) {
        } else if (removerMethodName.isEmpty()) {
            Method remove;
            try {
                remove = Map.class.getDeclaredMethod("remove", Object.class);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
            modConfig.getRemovers().put(fieldName, key -> {
                try {
                    remove.invoke(field.get(null), key);
                } catch (ReflectiveOperationException e) {
                    throw new AssertionError(e);
                }
            });
        } else {
            Class<?> type = remover.type() == Config.EMPTY.class ? (Class<?>) types[0] : remover.type();
            Method removerMethod;
            try {
                removerMethod = modConfig.getConfigsClass().getDeclaredMethod(removerMethodName, type);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
            removerMethod.setAccessible(true);
            modConfig.getRemovers().put(fieldName, key -> {
                try {
                    removerMethod.invoke(null, key);
                } catch (ReflectiveOperationException e) {
                    if (e.getCause() instanceof CommandSyntaxException commandSyntaxException) {
                        throw commandSyntaxException;
                    }
                    throw new AssertionError(e);
                }
            });
        }
    }

    private static void initObject(ModConfigImpl<?, ?> modConfig, Field field, Config annotation) {
        String fieldName = field.getName();
        Config.Setter setter = annotation.setter();
        String setterMethodName = setter.value();
        //noinspection StatementWithEmptyBody
        if (setterMethodName.equals("none")) {
        } else if (setterMethodName.isEmpty()) {
            modConfig.getSetters().put(fieldName, value -> {
                try {
                    field.set(null, value);
                } catch (ReflectiveOperationException e) {
                    throw new AssertionError(e);
                }
            });
        } else {
            Class<?> type = setter.type() == Config.EMPTY.class ? field.getType() : setter.type();
            Method setterMethod;
            try {
                setterMethod = modConfig.getConfigsClass().getDeclaredMethod(setterMethodName, type);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
            setterMethod.setAccessible(true);
            modConfig.getSetters().put(fieldName, value -> {
                try {
                    setterMethod.invoke(null, value);
                } catch (ReflectiveOperationException e) {
                    if (e.getCause() instanceof CommandSyntaxException commandSyntaxException) {
                        throw commandSyntaxException;
                    }
                    throw new AssertionError(e);
                }
            });
        }
    }
}
