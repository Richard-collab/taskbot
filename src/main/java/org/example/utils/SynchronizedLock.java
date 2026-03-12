package org.example.utils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SynchronizedLock {

    private final ReentrantReadWriteLock lock;

    public SynchronizedLock(boolean fair) {
        this.lock = new ReentrantReadWriteLock(fair);
    }

    public void read(Runnable runnable) {
        lock.readLock().lock();
        try {
            runnable.run();
        } finally {
            lock.readLock().unlock();
        }
    }

    public <T> T read(Supplier<T> supplier) {
        T result = null;
        lock.readLock().lock();
        try {
            result = supplier.get();
        } finally {
            lock.readLock().unlock();
        }
        return result;
    }

    public void write(Runnable runnable) {
        lock.writeLock().lock();
        try {
            runnable.run();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public <T> T write(Supplier<T> supplier) {
        T result = null;
        lock.writeLock().lock();
        try {
            result = supplier.get();
        } finally {
            lock.writeLock().unlock();
        }
        return result;
    }
}
