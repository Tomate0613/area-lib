package dev.doublekekse.area_lib.util;

import org.apache.commons.lang3.NotImplementedException;

import java.util.Map;

public record Pair<T, V>(T key, V value) implements Map.Entry<T, V> {
    @Override
    public T getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(Object value) {
        throw new NotImplementedException();
    }
}
