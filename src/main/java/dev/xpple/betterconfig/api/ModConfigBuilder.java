package dev.xpple.betterconfig.api;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.xpple.betterconfig.impl.BetterConfigImpl;
import dev.xpple.betterconfig.impl.BetterConfigInternals;
import dev.xpple.betterconfig.impl.ModConfigImpl;
import dev.xpple.betterconfig.util.CheckedBiFunction;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModConfigBuilder {

    final String modId;
    final Class<?> configsClass;

    final GsonBuilder builder = new GsonBuilder().serializeNulls().enableComplexMapKeySerialization();
    final Map<Class<?>, Pair<?, ?>> arguments = new HashMap<>();
    final Map<Class<?>, Pair<?, ?>> suggestors = new HashMap<>();

    public ModConfigBuilder(String modId, Class<?> configsClass) {
        this.modId = modId;
        this.configsClass = configsClass;
    }

    /**
     * Register a new type adapter and argument type for the specified type.
     * @param type the type's class
     * @param adapter the type adapter
     * @param argument a brigadier argument pair
     * @param <T> the type
     * @return the current builder instance
     * @implNote On servers, consider using {@link ModConfigBuilder#registerTypeHierarchyWithSuggestor}
     * instead. To use this method on servers, operators need to register the brigadier argument type
     * as well.
     * @see ModConfigBuilder#registerTypeHierarchyWithArgument
     */
    public <T> ModConfigBuilder registerTypeWithArgument(Class<T> type, TypeAdapter<T> adapter, Pair<Supplier<ArgumentType<T>>, CheckedBiFunction<CommandContext<? extends CommandSource>, String, T, CommandSyntaxException>> argument) {
        this.builder.registerTypeAdapter(type, adapter);
        this.arguments.put(type, argument);
        return this;
    }

    /**
     * Register a new type adapter and argument type for the specified type and all subclasses.
     * @param type the type's class
     * @param adapter the type adapter
     * @param argument a pair of a brigadier argument and parser
     * @param <T> the type
     * @return the current builder instance
     * @implNote On servers, consider using {@link ModConfigBuilder#registerTypeHierarchyWithSuggestor}
     * instead. To use this method on servers, operators need to register the brigadier argument type
     * as well.
     */
    public <T> ModConfigBuilder registerTypeHierarchyWithArgument(Class<T> type, TypeAdapter<T> adapter, Pair<Supplier<ArgumentType<T>>, CheckedBiFunction<CommandContext<? extends CommandSource>, String, T, CommandSyntaxException>> argument) {
        this.builder.registerTypeHierarchyAdapter(type, adapter);
        this.arguments.put(type, argument);
        return this;
    }

    /**
     * Register a new type adapter and suggestor for the specified type.
     * @param type the type's class
     * @param adapter the type adapter
     * @param suggestor a pair of a custom suggestions provider and parser
     * @param <T> the type
     * @return the current builder instance
     * @implNote On clients, use {@link ModConfigBuilder#registerTypeWithArgument} instead.
     * @see ModConfigBuilder#registerTypeHierarchyWithSuggestor
     */
    public <T> ModConfigBuilder registerTypeWithSuggestor(Class<T> type, TypeAdapter<T> adapter, Pair<Supplier<SuggestionProvider<? extends CommandSource>>, CheckedBiFunction<CommandContext<? extends CommandSource>, String, T, CommandSyntaxException>> suggestor) {
        this.builder.registerTypeAdapter(type, adapter);
        this.suggestors.put(type, suggestor);
        return this;
    }

    /**
     * Register a new type adapter and suggestor for the specified type and all subclasses.
     * @param type the type's class
     * @param adapter the type adapter
     * @param suggestor a pair of a custom suggestions provider and parser
     * @param <T> the type
     * @return the current builder instance
     * @implNote On clients, use {@link ModConfigBuilder#registerTypeHierarchyWithArgument} instead.
     */
    public <T> ModConfigBuilder registerTypeHierarchyWithSuggestor(Class<T> type, TypeAdapter<T> adapter, Pair<Supplier<SuggestionProvider<? extends CommandSource>>, CheckedBiFunction<CommandContext<? extends CommandSource>, String, T, CommandSyntaxException>> suggestor) {
        this.builder.registerTypeHierarchyAdapter(type, adapter);
        this.suggestors.put(type, suggestor);
        return this;
    }

    /**
     * Finalise the registration process.
     * @throws IllegalArgumentException when a configuration already exists for this mod
     */
    public void build() {
        ModConfigImpl modConfig = new ModConfigImpl(this.modId, this.configsClass, this.builder.create(), this.arguments, this.suggestors);
        if (BetterConfigImpl.getModConfigs().putIfAbsent(this.modId, modConfig) == null) {
            BetterConfigInternals.init(modConfig);
            return;
        }
        throw new IllegalArgumentException(this.modId);
    }
}
