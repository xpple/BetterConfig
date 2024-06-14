package dev.xpple.betterconfig.api;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.mojang.brigadier.arguments.ArgumentType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractConfigBuilder<S, C> {

    final Class<?> configsClass;

    final GsonBuilder builder = new GsonBuilder().serializeNulls().enableComplexMapKeySerialization();
    final Map<Class<?>, Function<C, ? extends ArgumentType<?>>> arguments = new HashMap<>();

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
     * @implNote On servers, this requires that the argument type is known to the client. This holds
     * true for all argument types that natively exist in the game. Any custom argument types must be
     * converted, however. For this, use {@link dev.xpple.betterconfig.util.WrappedArgumentType} on
     * Fabric or {@link io.papermc.paper.command.brigadier.argument.CustomArgumentType} on Paper.
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
     * @implNote On servers, this requires that the argument type is known to the client. This holds
     * true for all argument types that natively exist in the game. Any custom argument types must be
     * converted, however. For this, use {@link dev.xpple.betterconfig.util.WrappedArgumentType} on
     * Fabric or {@link io.papermc.paper.command.brigadier.argument.CustomArgumentType} on Paper.
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
     * @implNote On servers, this requires that the argument type is known to the client. This holds
     * true for all argument types that natively exist in the game. Any custom argument types must be
     * converted, however. For this, use {@link dev.xpple.betterconfig.util.WrappedArgumentType} on
     * Fabric or {@link io.papermc.paper.command.brigadier.argument.CustomArgumentType} on Paper.
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
     * @implNote On servers, this requires that the argument type is known to the client. This holds
     * true for all argument types that natively exist in the game. Any custom argument types must be
     * converted, however. For this, use {@link dev.xpple.betterconfig.util.WrappedArgumentType} on
     * Fabric or {@link io.papermc.paper.command.brigadier.argument.CustomArgumentType} on Paper.
     * @see AbstractConfigBuilder#registerType(Class, TypeAdapter, Function)
     */
    public <T> AbstractConfigBuilder<S, C> registerTypeHierarchy(Class<T> type, TypeAdapter<T> adapter, Function<C, ArgumentType<T>> argumentTypeFunction) {
        this.builder.registerTypeHierarchyAdapter(type, adapter);
        this.arguments.put(type, argumentTypeFunction);
        return this;
    }

    /**
     * Finalise the registration process.
     * @throws IllegalArgumentException when a configuration already exists for this mod
     */
    public abstract void build();
}
