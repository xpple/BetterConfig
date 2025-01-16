package dev.xpple.betterconfig.api;

import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 *     An annotation that specifies that this field should be treated as a configuration. The class in which this field is defined
 *     must be declared as {@code public}.
 * </p>
 *
 * <p>
 *     The name of the field will be used as the name of the config as well. The initial value given to the field will be used as
 *     default (fallback) value. This value will be used in the {@code reset} subcommand of the config command. Note that the
 *     field should not be {@code final}! Immutable in-code behaviour can be achieved by making the field {@code private} and
 *     adding a getter method. In the case of {@link java.util.Collection}s or {@link java.util.Map}s, one can return an immutable
 *     view. To make a config immutable in-game, see {@link Config#readOnly()}.
 * </p>
 *
 * <p>
 *     In general, all elements of the config class should be {@code static}. That means both fields and methods should be {@code static}.
 * </p>
 *
 * <p>
 *     BetterConfig distinguishes three different types of configs:
 * </p>
 *
 * <ol>
 *     <li>{@link java.util.Collection}s</li>
 *     <li>{@link java.util.Map}s</li>
 *     <li>All other types</li>
 * </ol>
 *
 * <p>
 *     The type of the config is determined in the above order using the {@link Class#isAssignableFrom(Class)} method. The
 *     different types of configs can be customised differently. Below is a table showing which properties are applicable to which
 *     config types.
 * </p>
 *
 * <table>
 *     <tr>
 *         <th>Property</th>
 *         <th>Type 1</th>
 *         <th>Type 2</th>
 *         <th>Type 3</th>
 *     </tr>
 *     <tr>
 *         <td>{@link Setter}</td>
 *         <td>✗</td>
 *         <td>✗</td>
 *         <td>✓</td>
 *     </tr>
 *     <tr>
 *         <td>{@link Adder}</td>
 *         <td>✓</td>
 *         <td>✗</td>
 *         <td>✗</td>
 *     </tr>
 *     <tr>
 *         <td>{@link Putter}</td>
 *         <td>✗</td>
 *         <td>✓</td>
 *         <td>✗</td>
 *     </tr>
 *     <tr>
 *         <td>{@link Remover}</td>
 *         <td>✓</td>
 *         <td>✓</td>
 *         <td>✗</td>
 *     </tr>
 * </table>
 *
 * <p>
 *     Attempting to use a property on an unsupported type will have no effect.
 * </p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Config {
    /**
     * An explanatory comment about the config. This value will be used in the {@code comment} subcommand of the config command.
     * @return the comment
     */
    String comment() default "";

    /**
     * A method name that will be used to represent the config value in the chat. The method should have no parameters and return
     * a chat {@code Component}, which is {@link net.minecraft.network.chat.Component} on Fabric and {@link net.kyori.adventure.text.Component}
     * on Paper. Below is an example for Fabric:
     * <pre>
     * {@code
     * @Config(chatRepresentation = "chatRepresentation")
     * public static ChatFormatting formatting = ChatFormatting.YELLOW;
     * public static Component chatRepresentation() {
     *     return Component.literal(formatting.getName()).withStyle(formatting);
     * }
     * }
     * </pre>
     * @return the method name
     */
    String chatRepresentation() default "";

    /**
     * A {@link Setter} that customises the {@code set} subcommand of the config command.
     * @return the setter
     */
    Setter setter() default @Setter;
    /**
     * An {@link Adder} that customises the {@code add} subcommand of the config command.
     * @return the setter
     */
    Adder adder() default @Adder;
    /**
     * A {@link Putter} that customises the {@code put} subcommand of the config command.
     * @return the setter
     */
    Putter putter() default @Putter;
    /**
     * A {@link Remover} that customises the {@code remove} subcommand of the config command.
     * @return the setter
     */
    Remover remover() default @Remover;

    /**
     * The name of a method that will be called whenever the config value is altered. The method should have two parameters; one
     * for the old value and one for the new value. For example:
     * <pre>
     * {@code
     * @Config(onChange = "exampleOnChange")
     * public static String exampleString = "defaultString";
     * public static void exampleOnChange(String oldValue, String newValue) {
     *     LOGGER.info("exampleOnChange was updated | old: {}, new: {}", oldValue, newValue);
     * }
     * }
     * </pre>
     * Both values are deep copies of the config that was changed, so they can be modified freely without care for the original
     * object.
     * @return the method name
     */
    String onChange() default "";

    /**
     * Whether the config value is read-only. If set to true, only the {@code get} subcommand of the config command will be
     * available.
     * @return {@code true} if the config is read-only, {@code false} otherwise
     */
    boolean readOnly() default false;
    /**
     * Whether the config value is temporary. If set to true, the config will not be saved to the config file and will not persist
     * between sessions.
     * @return {@code true} if the config is temporary, {@code false} otherwise
     */
    boolean temporary() default false;
    /**
     * Specify a condition for the visibility of the config in the config command. The value should be a method name of a method
     * that returns a boolean and optionally has the {@code CommandSource} as parameter. On Fabric, this is {@link net.minecraft.commands.SharedSuggestionProvider}
     * and on Paper this is {@link io.papermc.paper.command.brigadier.CommandSourceStack}. For example:
     * <pre>
     * {@code
     * @Config(condition = "myCondition")
     * public static String myConfig = "";
     * public static boolean myCondition(CommandSource source) {
     *     return Boolean.getBoolean("enableMyConfig");
     * }
     * }
     * </pre>
     * @return the method name
     */
    String condition() default "";

    /**
     * The setter customises the {@code set} subcommand of the config command. This annotation can be used to transform or verify
     * the user's input to the subcommand.
     */
    @Target({})
    @interface Setter {
        /**
         * The method name of the setter. The method should not return anything and have a single parameter whose type matches the
         * type of the config itself, unless the {@link Setter#type()} is specified. The method can be {@code private}.
         * Optionally, the method may throw a {@link com.mojang.brigadier.exceptions.CommandSyntaxException} to indicate failure.
         * Below is an example of a config along with a custom setter.
         * <pre>
         * {@code
         * @Config(setter = @Config.Setter("exampleSetter"))
         * public static String exampleString = "defaultString";
         * public static void exampleSetter(String string) {
         *     exampleString = string.toLowerCase(Locale.ROOT);
         * }
         * }
         * </pre>
         * If this value is set to the empty string (the default), the field will be set using reflection. Additionally if the
         * value is set to {@code none}, the {@code set} subcommand will not be available.
         * @return the method name
         */
        String value() default "";
        /**
         * An optional property that allows for an alternate type of the method parameter.
         * @return the alternate type
         */
        Class<?> type() default EMPTY.class;
    }

    /**
     * The adder customises the {@code add} subcommand of the config command. This annotation can be used to transform or verify
     * the user's input to the subcommand.
     */
    @Target({})
    @interface Adder {
        /**
         * The method name of the adder. The method should not return anything and have a single parameter whose type matches the
         * type of the config itself, unless the {@link Adder#type()} is specified. The method can be {@code private}.
         * Optionally, the method may throw a {@link com.mojang.brigadier.exceptions.CommandSyntaxException} to indicate failure.
         * Below is an example of a config along with a custom adder.
         * <pre>
         * {@code
         * @Config(adder = @Config.Adder("exampleAdder"))
         * public static List<String> exampleList = new ArrayList<>();
         * public static void exampleAdder(String string) {
         *     exampleList.add(string.toLowerCase(Locale.ROOT));
         * }
         * }
         * </pre>
         * If this value is set to the empty string (the default), the {@link java.util.Collection#add(Object)} method will be
         * used. Additionally if the value is set to {@code none}, the {@code add} subcommand will not be available.
         * @return the method name
         */
        String value() default "";
        /**
         * An optional property that allows for an alternate type of the method parameter.
         * @return the alternate type
         */
        Class<?> type() default EMPTY.class;
    }

    /**
     * The putter customises the {@code put} subcommand of the config command. This annotation can be used to transform or verify
     * the user's input to the subcommand.
     */
    @Target({})
    @interface Putter {
        /**
         * The method name of the putter. The method should not return anything and have two parameter whose types match the types
         * of the key and value types of the map, unless {@link Putter#keyType()} or {@link Putter#valueType()} is specified. The
         * method can be {@code private}. Optionally, the method may throw a {@link com.mojang.brigadier.exceptions.CommandSyntaxException}
         * to indicate failure. Below is an example of a config along with a custom adder.
         * <pre>
         * {@code
         * @Config(putter = @Config.Putter("examplePutter"))
         * public static Map<String, String> exampleMap = new HashMap<>();
         * public static void examplePutter(String string) {
         *     exampleMap.put(string.toLowerCase(Locale.ROOT), string.toUpperCase(Locale.ROOT));
         * }
         * }
         * </pre>
         * If this value is set to the empty string (the default), the {@link java.util.Map#put(Object, Object)} method will be
         * used. Additionally if the value is set to {@code none}, the {@code put} subcommand will not be available.
         * @return the method name
         */
        String value() default "";
        /**
         * An optional property that allows for an alternate type of the method parameter for the key.
         * @return the alternate type
         */
        Class<?> keyType() default EMPTY.class;
        /**
         * An optional property that allows for an alternate type of the method parameter for the value.
         * @return the alternate type
         */
        Class<?> valueType() default EMPTY.class;
    }

    /**
     * The remover customises the {@code remove} subcommand of the config command. This annotation can be used to transform or
     * verify the user's input to the subcommand.
     */
    @Target({})
    @interface Remover {
        /**
         * The method name of the remover. The method should not return anything and have a single parameter whose type matches
         * the type of the config itself, unless the {@link Remover#type()} is specified. The method can be {@code private}.
         * Optionally, the method may throw a {@link com.mojang.brigadier.exceptions.CommandSyntaxException} to indicate failure.
         * Below is an example of a config along with a custom adder.
         * <pre>
         * {@code
         * @Config(remover = @Config.Remover("exampleRemover"))
         * public static List<String> exampleList = new ArrayList<>();
         * public static void exampleRemover(String string) {
         *     exampleList.remove(string.toLowerCase(Locale.ROOT));
         * }
         * }
         * </pre>
         * If this value is set to the empty string (the default), the {@link java.util.Collection#remove(Object)} method will be
         * used. Additionally if the value is set to {@code none}, the {@code remove} subcommand will not be available.
         * @return the method name
         */
        String value() default "";
        /**
         * An optional property that allows for an alternate type of the method parameter.
         * @return the alternate type
         */
        Class<?> type() default EMPTY.class;
    }

    @ApiStatus.Internal
    final class EMPTY {
        private EMPTY() {
        }
    }
}
