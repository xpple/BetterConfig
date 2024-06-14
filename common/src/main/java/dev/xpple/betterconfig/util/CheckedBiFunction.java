package dev.xpple.betterconfig.util;

import java.util.Objects;

@FunctionalInterface
public interface CheckedBiFunction<T, U, R, E extends Exception> {
    R apply(T t, U u) throws E;

    default <V> CheckedBiFunction<T, U, V, E> andThen(CheckedFunction<? super R, ? extends V, E> after) {
        Objects.requireNonNull(after);
        return (T t, U u) -> after.apply(this.apply(t, u));
    }
}
