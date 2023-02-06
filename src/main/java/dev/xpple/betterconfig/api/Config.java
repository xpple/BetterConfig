package dev.xpple.betterconfig.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 *     An annotation that specifies that this field should be treated as a configuration.
 *     The class in which this field is defined, as well as the field itself, must be
 *     defined as {@code public}. Moreover, fields annotated with this annotation must not
 *     be declared {@code final}.
 * </p>
 *
 * <p>
 *     The name of the field will be used as the name of the config as well. The initial
 *     value given to the field will be used as default (fallback) value.
 * </p>
 *
 * <p>
 *     For each of the attributes of this annotation, its value represents the name of a
 *     method. See below for an example.
 *     <pre>
 *     {@code
 *     @Config(setter = "exampleSetter")
 *     public static String exampleString = "defaultString";
 *     public static void exampleSetter(Object string) {
 *         exampleString = ((String) string).toLowerCase(Locale.ROOT);
 *     }
 *     }
 *     </pre>
 *     This method is allowed to throw a {@link com.mojang.brigadier.exceptions.CommandSyntaxException},
 *     which will be caught and dealt with as usual. <br />
 *     If an attribute equals the empty string (the default value), the Java built-in
 *     method will be used. For instance, the method {@link java.util.Collection#add} for
 *     adders. When the attribute is set to {@code "none"}, the availability of this default
 *     method is removed.
 * </p>
 *
 * <p>
 *     The {@link Config#setter()} attribute is used for objects and primitives. For these
 *     types the other attributes don't make sense, and will therefore be ignored. The
 *     {@link Config#adder()} is used for {@link java.util.Collection}s. You can also
 *     define an adder for a {@link java.util.Map}, in which you can create a key-value
 *     pair based on the single parameter. The {@link Config#putter()} attribute is solely
 *     used for {@code Map}s. Lastly, the {@link Config#remover()} is used for both
 *     {@code Collection}s and {@code Map}s. In the case of a {@code Map}, an entry will be
 *     removed based on its key.
 * </p>
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Config {
    String setter() default "";
    String adder() default "";
    String putter() default "";
    String remover() default "";
}
