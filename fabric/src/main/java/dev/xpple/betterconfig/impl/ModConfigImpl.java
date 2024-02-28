package dev.xpple.betterconfig.impl;

import com.google.gson.Gson;
import com.mojang.brigadier.arguments.ArgumentType;
import dev.xpple.betterconfig.BetterConfig;
import dev.xpple.betterconfig.api.ModConfig;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

public class ModConfigImpl<S extends SharedSuggestionProvider> extends AbstractConfigImpl<S, CommandBuildContext> implements ModConfig {

    private final String modId;

    public ModConfigImpl(String modId, Class<?> configsClass, Gson gson, Map<Class<?>, Function<CommandBuildContext, ? extends ArgumentType<?>>> arguments) {
        super(configsClass, gson, arguments);
        this.modId = modId;
    }

    @Override
    public String getModId() {
        return this.modId;
    }

    @Override
    public Path getConfigsPath() {
        return BetterConfig.MOD_PATH.resolve(this.modId).resolve("config.json");
    }

    @Override
    public String getIdentifier() {
        return this.getModId();
    }
}
