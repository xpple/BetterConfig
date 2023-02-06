package dev.xpple.betterconfig.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.api.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

import static dev.xpple.betterconfig.BetterConfig.LOGGER;

public class BetterConfigInternals {

    public static void init(ModConfigImpl modConfig) {
        JsonObject root;
        try (BufferedReader reader = Files.newBufferedReader(modConfig.getConfigsPath())) {
            root = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            root = null;
            LOGGER.warn("Could not read config file, default values will be used.");
        }

        for (Field field : modConfig.getConfigsClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(Config.class)) {
                continue;
            }

            String fieldName = field.getName();
            modConfig.getConfigs().put(fieldName, field);
            if (root != null && root.has(fieldName)) {
                try {
                    Object value = modConfig.getGson().fromJson(root.get(fieldName), field.getGenericType());
                    field.set(null, value);
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }

            Config annotation = field.getAnnotation(Config.class);
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
        String adder = annotation.adder();
        //noinspection StatementWithEmptyBody
        if (adder.equals("none")) {
        } else if (adder.isEmpty()) {
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
            Method adderMethod;
            try {
                adderMethod = modConfig.getConfigsClass().getDeclaredMethod(adder, Object.class);
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
        String remover = annotation.remover();
        //noinspection StatementWithEmptyBody
        if (remover.equals("none")) {
        } else if (remover.isEmpty()) {
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
            Method removerMethod;
            try {
                removerMethod = modConfig.getConfigsClass().getDeclaredMethod(adder, Object.class);
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
        String adder = annotation.adder();
        //noinspection StatementWithEmptyBody
        if (adder.equals("none")) {
        } else if (!adder.isEmpty()) {
            Method adderMethod;
            try {
                adderMethod = modConfig.getConfigsClass().getDeclaredMethod(adder, Object.class);
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
        String putter = annotation.putter();
        //noinspection StatementWithEmptyBody
        if (putter.equals("none")) {
        } else if (putter.isEmpty()) {
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
            Method putterMethod;
            try {
                putterMethod = modConfig.getConfigsClass().getDeclaredMethod(putter, Object.class, Object.class);
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
        String remover = annotation.remover();
        //noinspection StatementWithEmptyBody
        if (remover.equals("none")) {
        } else if (remover.isEmpty()) {
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
            Method removerMethod;
            try {
                removerMethod = modConfig.getConfigsClass().getDeclaredMethod(putter, Object.class);
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
        String setter = annotation.setter();
        //noinspection StatementWithEmptyBody
        if (setter.equals("none")) {
        } else if (setter.isEmpty()) {
            modConfig.getSetters().put(fieldName, value -> {
                try {
                    field.set(null, value);
                } catch (ReflectiveOperationException e) {
                    throw new AssertionError(e);
                }
            });
        } else {
            Method setterMethod;
            try {
                setterMethod = modConfig.getConfigsClass().getDeclaredMethod(setter, Object.class);
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
