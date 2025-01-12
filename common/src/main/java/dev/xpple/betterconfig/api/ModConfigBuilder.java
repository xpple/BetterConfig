package dev.xpple.betterconfig.api;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.mojang.brigadier.arguments.ArgumentType;
import dev.xpple.betterconfig.impl.BetterConfigImpl;
import dev.xpple.betterconfig.impl.BetterConfigInternals;
import dev.xpple.betterconfig.impl.ModConfigImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @param <S> the command source type: {@link net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource}
 *           on Fabric clients, {@link net.minecraft.commands.CommandSourceStack} on Fabric servers
 *           and {@link io.papermc.paper.command.brigadier.CommandSourceStack} on Paper.
 * @param <C> the command build context: {@link net.minecraft.commands.CommandBuildContext} on Fabric
 *           and unused on Paper.
 */
public final class ModConfigBuilder<S, C> {

    private final String modId;

    private final Class<?> configsClass;

    private final GsonBuilder builder = new GsonBuilder().serializeNulls().enableComplexMapKeySerialization();
    private final Map<Class<?>, Function<C, ? extends ArgumentType<?>>> arguments = new HashMap<>();

    private Consumer<GlobalChangeEvent> globalChangeHook = event -> {};

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
     * @implNote On servers, this requires that the argument type is known to the client. This holds
     * true for all argument types that natively exist in the game. Any custom argument types must be
     * converted, however. For this, use {@link dev.xpple.betterconfig.util.WrappedArgumentType} on
     * Fabric or {@link io.papermc.paper.command.brigadier.argument.CustomArgumentType} on Paper.
     * @see ModConfigBuilder#registerTypeHierarchy(Class, TypeAdapter, Supplier)
     */
    public <T> ModConfigBuilder<S, C> registerType(Class<T> type, TypeAdapter<T> adapter, Supplier<ArgumentType<T>> argumentTypeSupplier) {
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
     * @see ModConfigBuilder#registerTypeHierarchy(Class, TypeAdapter, Function)
     */
    public <T> ModConfigBuilder<S, C> registerType(Class<T> type, TypeAdapter<T> adapter, Function<C, ArgumentType<T>> argumentTypeFunction) {
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
     * @see ModConfigBuilder#registerType(Class, TypeAdapter, Supplier)
     */
    public <T> ModConfigBuilder<S, C> registerTypeHierarchy(Class<T> type, TypeAdapter<T> adapter, Supplier<ArgumentType<T>> argumentTypeSupplier) {
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
     * @see ModConfigBuilder#registerType(Class, TypeAdapter, Function)
     */
    public <T> ModConfigBuilder<S, C> registerTypeHierarchy(Class<T> type, TypeAdapter<T> adapter, Function<C, ArgumentType<T>> argumentTypeFunction) {
        this.builder.registerTypeHierarchyAdapter(type, adapter);
        this.arguments.put(type, argumentTypeFunction);
        return this;
    }

    /**
     * Register a callback that will be called whenever any config value is updated. See
     * {@link GlobalChangeEvent} for the event context that is available in the callback.
     * @param hook the callback
     * @return the current builder instance
     */
    public ModConfigBuilder<S, C> registerGlobalChangeHook(Consumer<GlobalChangeEvent> hook) {
        this.globalChangeHook = hook;
        return this;
    }

    /**
     * Finalise the registration process.
     * @throws IllegalArgumentException when a configuration already exists for this mod
     */
    public void build() {
        ModConfigImpl<?, ?, ?> modConfig = new ModConfigImpl<>(this.modId, this.configsClass, this.builder.create(), this.arguments, this.globalChangeHook);
        if (BetterConfigImpl.getModConfigs().putIfAbsent(this.modId, modConfig) == null) {
            BetterConfigInternals.init(modConfig);
            return;
        }
        throw new IllegalArgumentException(this.modId);
    }
}
