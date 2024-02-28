package dev.xpple.betterconfig.util;

import java.util.Objects;

@FunctionalInterface
public interface CheckedConsumer<T, E extends Exception> {
    void accept(T t) throws E;

    default CheckedConsumer<T, E> andThen(CheckedConsumer<? super T, E> after) {
        Objects.requireNonNull(after);
        return (T t) -> {
            this.accept(t);
            after.accept(t);
        };
    }
}
