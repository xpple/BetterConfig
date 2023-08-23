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
import dev.xpple.betterconfig.util.Pair;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModConfigBuilder {

    final String modId;
    final Class<?> configsClass;

    final GsonBuilder builder = new GsonBuilder().serializeNulls().enableComplexMapKeySerialization();
    final Map<Class<?>, Function<CommandRegistryAccess, ? extends ArgumentType<?>>> arguments = new HashMap<>();
    final Map<Class<?>, Pair<SuggestionProvider<? extends CommandSource>, CheckedBiFunction<CommandContext<? extends CommandSource>, String, ?, CommandSyntaxException>>> suggestors = new HashMap<>();

    public ModConfigBuilder(String modId, Class<?> configsClass) {
        this.modId = modId;
        this.configsClass = configsClass;
    }

    /**
     * Register a new type adapter and argument type for the specified type.
     * @param type the type's class
     * @param adapter the type adapter
     * @param argumentTypeSupplier a supplier for the argument type
     * @param <T> the type
     * @return the current builder instance
     * @implNote On servers, consider using {@link ModConfigBuilder#registerType(Class, TypeAdapter, SuggestionProvider, CheckedBiFunction)}
     * instead. To use this method on servers, operators need to register the brigadier argument type
     * as well.
     * @see ModConfigBuilder#registerTypeHierarchy(Class, TypeAdapter, Supplier)
     */
    public <T> ModConfigBuilder registerType(Class<T> type, TypeAdapter<T> adapter, Supplier<ArgumentType<T>> argumentTypeSupplier) {
        return this.registerType(type, adapter, registryAccess -> argumentTypeSupplier.get());
    }

    /**
     * Register a new type adapter and argument type for the specified type.
     * @param type the type's class
     * @param adapter the type adapter
     * @param argumentTypeFunction a function for the argument type needing registry access
     * @param <T> the type
     * @return the current builder instance
     * @implNote On servers, consider using {@link ModConfigBuilder#registerType(Class, TypeAdapter, SuggestionProvider, CheckedBiFunction)}
     * instead. To use this method on servers, operators need to register the brigadier argument type
     * as well.
     * @see ModConfigBuilder#registerTypeHierarchy(Class, TypeAdapter, Function)
     */
    public <T> ModConfigBuilder registerType(Class<T> type, TypeAdapter<T> adapter, Function<CommandRegistryAccess, ArgumentType<T>> argumentTypeFunction) {
        this.builder.registerTypeAdapter(type, adapter);
        this.arguments.put(type, argumentTypeFunction);
        return this;
    }

    /**
     * Register a new type adapter and argument type for the specified type and all subclasses.
     * @param type the type's class
     * @param adapter the type adapter
     * @param argumentTypeSupplier a supplier for the argument type
     * @param <T> the type
     * @return the current builder instance
     * @implNote On servers, consider using {@link ModConfigBuilder#registerTypeHierarchy(Class, TypeAdapter, SuggestionProvider, CheckedBiFunction)}
     * instead. To use this method on servers, operators need to register the brigadier argument type
     * as well.
     * @see ModConfigBuilder#registerType(Class, TypeAdapter, Supplier)
     */
    public <T> ModConfigBuilder registerTypeHierarchy(Class<T> type, TypeAdapter<T> adapter, Supplier<ArgumentType<T>> argumentTypeSupplier) {
        return this.registerTypeHierarchy(type, adapter, registryAccess -> argumentTypeSupplier.get());
    }

    /**
     * Register a new type adapter and argument type for the specified type and all subclasses.
     * @param type the type's class
     * @param adapter the type adapter
     * @param argumentTypeFunction a function for the argument type needing registry access
     * @param <T> the type
     * @return the current builder instance
     * @implNote On servers, consider using {@link ModConfigBuilder#registerTypeHierarchy(Class, TypeAdapter, SuggestionProvider, CheckedBiFunction)}
     * instead. To use this method on servers, operators need to register the brigadier argument type
     * as well.
     * @see ModConfigBuilder#registerType(Class, TypeAdapter, Function)
     */
    public <T> ModConfigBuilder registerTypeHierarchy(Class<T> type, TypeAdapter<T> adapter, Function<CommandRegistryAccess, ArgumentType<T>> argumentTypeFunction) {
        this.builder.registerTypeHierarchyAdapter(type, adapter);
        this.arguments.put(type, argumentTypeFunction);
        return this;
    }

    /**
     * Register a new type adapter and suggestor for the specified type.
     * @param type the type's class
     * @param adapter the type adapter
     * @param suggestionProvider a suggestion provider for the type
     * @param argumentParser a parser for the argument
     * @param <T> the type
     * @return the current builder instance
     * @implNote On clients, consider using {@link ModConfigBuilder#registerType(Class, TypeAdapter, Supplier)} instead.
     * @see ModConfigBuilder#registerTypeHierarchy(Class, TypeAdapter, SuggestionProvider, CheckedBiFunction)
     */
    public <T> ModConfigBuilder registerType(Class<T> type, TypeAdapter<T> adapter, SuggestionProvider<? extends CommandSource> suggestionProvider, CheckedBiFunction<CommandContext<? extends CommandSource>, String, T, CommandSyntaxException> argumentParser) {
        this.builder.registerTypeAdapter(type, adapter);
        this.suggestors.put(type, new Pair<>(suggestionProvider, argumentParser));
        return this;
    }

    /**
     * Register a new type adapter and suggestor for the specified type and all subclasses.
     * @param type the type's class
     * @param adapter the type adapter
     * @param suggestionProvider a suggestion provider for the type
     * @param argumentParser a parser for the argument
     * @param <T> the type
     * @return the current builder instance
     * @implNote On clients, consider using {@link ModConfigBuilder#registerTypeHierarchy(Class, TypeAdapter, Supplier)} instead.
     * @see ModConfigBuilder#registerType(Class, TypeAdapter, SuggestionProvider, CheckedBiFunction)
     */
    public <T> ModConfigBuilder registerTypeHierarchy(Class<T> type, TypeAdapter<T> adapter, SuggestionProvider<? extends CommandSource> suggestionProvider, CheckedBiFunction<CommandContext<? extends CommandSource>, String, T, CommandSyntaxException> argumentParser) {
        this.builder.registerTypeHierarchyAdapter(type, adapter);
        this.suggestors.put(type, new Pair<>(suggestionProvider, argumentParser));
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
