package org.example.utils;

import java.util.HashSet;
import java.util.Set;

public class SynchronizedSet<E> {

    private final SynchronizedLock lock;
    private final Set<E> set;
    private final String pathJson;

    public SynchronizedSet(Set<E> set, boolean fair) {
        this.set = new HashSet<>(set);
        this.lock = new SynchronizedLock(fair);
        this.pathJson = null;
    }

    public SynchronizedSet(Set<E> set, boolean fair, String pathJson) {
        this.set = new HashSet<>(set);
        this.lock = new SynchronizedLock(fair);
        this.pathJson = pathJson;
    }

    public Set<E> getHashSet() {
        return lock.read(() -> new HashSet<>(set));
    }

    public boolean add(E e) {
        return lock.write(() -> {
            boolean result = set.add(e);

            writeToFile();
            return result;
        });
    }

    public boolean remove(E e) {
        return lock.write(() -> {
            boolean result = set.remove(e);

            writeToFile();
            return result;
        });
    }

    public void clear() {
        lock.write(() -> {
            this.set.clear();

            writeToFile();
        });
    }

    private void writeToFile() {
        if (!StringUtils.isEmpty(pathJson)) {
            FileUtils.write(JsonUtils.toJson(set, false), pathJson, false);
        }
    }
}
