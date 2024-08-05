package dev.xpple.betterconfig.util;

import java.util.Objects;

@FunctionalInterface
public interface CheckedBiConsumer<T, U, E extends Exception> {
    void accept(T t, U u) throws E;

    default CheckedBiConsumer<T, U, E> andThen(CheckedBiConsumer<? super T, ? super U, E> after) {
        Objects.requireNonNull(after);

        return (l, r) -> {
            this.accept(l, r);
            after.accept(l, r);
        };
    }
}
