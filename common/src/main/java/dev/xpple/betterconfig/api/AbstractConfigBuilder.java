package dev.xpple.betterconfig.api;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.xpple.betterconfig.util.CheckedBiFunction;
import dev.xpple.betterconfig.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractConfigBuilder<S, C> {

    final Class<?> configsClass;

    final GsonBuilder builder = new GsonBuilder().serializeNulls().enableComplexMapKeySerialization();
    final Map<Class<?>, Function<C, ? extends ArgumentType<?>>> arguments = new HashMap<>();
    final Map<Class<?>, Pair<SuggestionProvider<? extends S>, CheckedBiFunction<CommandContext<? extends S>, String, ?, CommandSyntaxException>>> suggestors = new HashMap<>();

    public AbstractConfigBuilder(Class<?> configsClass) {
        this.configsClass = configsClass;
    }

    /**
     * Register a new type adapter and argument type for the specified type.
     * @param type the type's class
     * @param adapter the type adapter
     * @param argumentTypeSupplier a supplier for the argument type
     * @param <T> the type
     * @return the current builder instance
     * @implNote On servers, consider using {@link AbstractConfigBuilder#registerType(Class, TypeAdapter, SuggestionProvider, CheckedBiFunction)}
     * instead. To use this method on servers, operators need to register the brigadier argument type
     * as well.
     * @see AbstractConfigBuilder#registerTypeHierarchy(Class, TypeAdapter, Supplier)
     */
    public <T> AbstractConfigBuilder<S, C> registerType(Class<T> type, TypeAdapter<T> adapter, Supplier<ArgumentType<T>> argumentTypeSupplier) {
        return this.registerType(type, adapter, buildContext -> argumentTypeSupplier.get());
    }

    /**
     * Register a new type adapter and argument type for the specified type.
     * @param type the type's class
     * @param adapter the type adapter
     * @param argumentTypeFunction a function for the argument type needing build context
     * @param <T> the type
     * @return the current builder instance
     * @implNote On servers, consider using {@link AbstractConfigBuilder#registerType(Class, TypeAdapter, SuggestionProvider, CheckedBiFunction)}
     * instead. To use this method on servers, operators need to register the brigadier argument type
     * as well.
     * @see AbstractConfigBuilder#registerTypeHierarchy(Class, TypeAdapter, Function)
     */
    public <T> AbstractConfigBuilder<S, C> registerType(Class<T> type, TypeAdapter<T> adapter, Function<C, ArgumentType<T>> argumentTypeFunction) {
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
     * @implNote On servers, consider using {@link AbstractConfigBuilder#registerTypeHierarchy(Class, TypeAdapter, SuggestionProvider, CheckedBiFunction)}
     * instead. To use this method on servers, operators need to register the brigadier argument type
     * as well.
     * @see AbstractConfigBuilder#registerType(Class, TypeAdapter, Supplier)
     */
    public <T> AbstractConfigBuilder<S, C> registerTypeHierarchy(Class<T> type, TypeAdapter<T> adapter, Supplier<ArgumentType<T>> argumentTypeSupplier) {
        return this.registerTypeHierarchy(type, adapter, buildContext -> argumentTypeSupplier.get());
    }

    /**
     * Register a new type adapter and argument type for the specified type and all subclasses.
     * @param type the type's class
     * @param adapter the type adapter
     * @param argumentTypeFunction a function for the argument type needing build context
     * @param <T> the type
     * @return the current builder instance
     * @implNote On servers, consider using {@link AbstractConfigBuilder#registerTypeHierarchy(Class, TypeAdapter, SuggestionProvider, CheckedBiFunction)}
     * instead. To use this method on servers, operators need to register the brigadier argument type
     * as well.
     * @see AbstractConfigBuilder#registerType(Class, TypeAdapter, Function)
     */
    public <T> AbstractConfigBuilder<S, C> registerTypeHierarchy(Class<T> type, TypeAdapter<T> adapter, Function<C, ArgumentType<T>> argumentTypeFunction) {
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
     * @implNote On clients, consider using {@link AbstractConfigBuilder#registerType(Class, TypeAdapter, Supplier)} instead.
     * @see AbstractConfigBuilder#registerTypeHierarchy(Class, TypeAdapter, SuggestionProvider, CheckedBiFunction)
     */
    public <T> AbstractConfigBuilder<S, C> registerType(Class<T> type, TypeAdapter<T> adapter, SuggestionProvider<? extends S> suggestionProvider, CheckedBiFunction<CommandContext<? extends S>, String, T, CommandSyntaxException> argumentParser) {
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
     * @implNote On clients, consider using {@link AbstractConfigBuilder#registerTypeHierarchy(Class, TypeAdapter, Supplier)} instead.
     * @see AbstractConfigBuilder#registerType(Class, TypeAdapter, SuggestionProvider, CheckedBiFunction)
     */
    public <T> AbstractConfigBuilder<S, C> registerTypeHierarchy(Class<T> type, TypeAdapter<T> adapter, SuggestionProvider<? extends S> suggestionProvider, CheckedBiFunction<CommandContext<? extends S>, String, T, CommandSyntaxException> argumentParser) {
        this.builder.registerTypeHierarchyAdapter(type, adapter);
        this.suggestors.put(type, new Pair<>(suggestionProvider, argumentParser));
        return this;
    }

    /**
     * Finalise the registration process.
     * @throws IllegalArgumentException when a configuration already exists for this mod
     */
    public abstract void build();
}
