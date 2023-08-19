package dev.xpple.betterconfig.impl;

import java.util.ServiceLoader;

public interface Platform {
    Class<?> getCommandSourceClass();

    Platform current = ServiceLoader.load(Platform.class, Platform.class.getClassLoader()).iterator().next();
}
