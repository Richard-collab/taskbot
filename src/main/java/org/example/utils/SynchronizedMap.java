package org.example.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class SynchronizedMap<K,V> {

    private final SynchronizedLock lock;
    private final Map<K, V> map;
    private final String pathJson;

    public SynchronizedMap(Map<K, V> map, boolean fair) {
        this.map = new HashMap<>(map);
        this.lock = new SynchronizedLock(fair);
        this.pathJson = null;
    }

    public SynchronizedMap(Map<K, V> map, boolean fair, String pathJson) {
        this.map = new HashMap<>(map);
        this.lock = new SynchronizedLock(fair);
        this.pathJson = pathJson;
    }

    public <T> T apply(Function<Map<K, V>, T> function) {
        return lock.write(() -> function.apply(map));
    }

    public Set<K> keySet() {
        return lock.read(() -> new HashSet<>(map.keySet()));
    }

    public Set<V> values() {
        return lock.read(() -> new HashSet<>(map.values()));
    }

    public V get(K k) {
        return lock.read(() -> map.get(k));
    }

    public <T> T get(K k, Function<V, T> function) {
        return lock.read(() -> {
            V v = map.get(k);
            T t = function.apply(v);
            return t;
        });
    }

    public V putIfAbsent(K key, V value) {
        return lock.write(() -> {
            V result = map.putIfAbsent(key, value);

            writeToFile();
            return result;
        });
    }

    public V put(K k, V v) {
        return lock.write(() -> {
            V result = map.put(k, v);

            writeToFile();
            return result;
        });
    }

    public V put(K k, Function<V, V> function) {
        return lock.write(() -> {
            V v = map.get(k);
            v = function.apply(v);
            V result;
            if (v != null) {
                result = map.put(k, v);
            } else {
                result = map.remove(k);
            }

            writeToFile();
            return result;
        });
    }

    public V remove(K k) {
        return lock.write(() -> {
            V result = map.remove(k);

            writeToFile();
            return result;
        });
    }

    public void clear() {
        lock.write(() -> {
            this.map.clear();

            writeToFile();
        });
    }

    public void writeToFile() {
        if (!StringUtils.isEmpty(pathJson)) {
            lock.write(() -> FileUtils.write(JsonUtils.toJson(map, false), pathJson, false));
        }
    }
}
