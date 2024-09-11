package dev.xpple.betterconfig.util;

@FunctionalInterface
public interface CheckedRunnable<E extends Exception> {
    void run() throws E;
}
