package dev.xpple.betterconfig.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.api.Config;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import sun.misc.Unsafe;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

import static dev.xpple.betterconfig.BetterConfig.LOGGER;

public class BetterConfigInternals {

    private static final Unsafe unsafe = UnsafeAccess.UNSAFE;

    public static void init(ModConfigImpl modConfig) {
        JsonObject root;
        try (BufferedReader reader = Files.newBufferedReader(modConfig.getConfigsPath())) {
            root = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            root = null;
            LOGGER.warn("Could not read config file, default values will be used.");
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
                modConfig.getDefaults().put(fieldName, field.get(null));
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
            modConfig.getAnnotations().put(fieldName, annotation);

            if (!annotation.temporary() && root != null && root.has(fieldName)) {
                try {
                    Object value = modConfig.getGson().fromJson(root.get(fieldName), field.getGenericType());
                    if (Modifier.isFinal(field.getModifiers())) {
                        unsafe.putObject(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), value);
                    } else {
                        field.set(null, value);
                    }
                } catch (Exception e) {
                    throw new AssertionError(e);
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
    }

    private static void initCollection(ModConfigImpl modConfig, Field field, Config annotation) {
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

    private static void initMap(ModConfigImpl modConfig, Field field, Config annotation) {
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

    private static void initObject(ModConfigImpl modConfig, Field field, Config annotation) {
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
