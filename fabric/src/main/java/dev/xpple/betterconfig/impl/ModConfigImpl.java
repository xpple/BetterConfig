package dev.xpple.betterconfig.impl;

import com.google.gson.Gson;
import dev.xpple.betterconfig.api.ModConfig;
import net.minecraft.command.CommandSource;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Path;
import java.util.Map;

import static dev.xpple.betterconfig.BetterConfig.MOD_PATH;

public class ModConfigImpl<S extends CommandSource> extends AbstractConfigImpl<S> implements ModConfig {

    private final String modId;

    public ModConfigImpl(String modId, Class<?> configsClass, Gson gson, Map<Class<?>, Pair<?, ?>> arguments, Map<Class<?>, Pair<?, ?>> suggestors) {
        super(configsClass, gson, arguments, suggestors);
        this.modId = modId;
    }

    @Override
    public String getModId() {
        return this.modId;
    }

    @Override
    public Path getConfigsPath() {
        return MOD_PATH.resolve(this.modId).resolve("config.json");
    }

    @Override
    public String getIdentifier() {
        return this.getModId();
    }
}
