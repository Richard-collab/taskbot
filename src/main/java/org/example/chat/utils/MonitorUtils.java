package org.example.chat.utils;

import org.example.utils.SynchronizedLock;

public class MonitorUtils {

    private static volatile int value = 0;
    private static final SynchronizedLock lock = new SynchronizedLock(false);

    public static int get() {
        return lock.read(() -> value);
    }

    public static void increase() {
        lock.write(() -> value++);
    }

    public static void decrease() {
        lock.write(() -> value--);
    }

    public static void clear() {
        lock.write(() -> value = 0);
    }

    public static void apply(Runnable runnable) {
        lock.write(() -> {
            if (value == 0) {
                runnable.run();
            }
        });
    }
}
