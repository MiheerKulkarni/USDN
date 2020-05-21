package com.github.usdn.motes.core;

import java.util.Objects;

public final class Pair <K,V>{

    private final K key;

    private final V value;

    public Pair(final K k, final V v) {
        key = k;
        value = v;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Pair) {
            Pair pair = (Pair) o;
            if (!Objects.equals(key, pair.key)) {
                return false;
            }
            return !(!Objects.equals(value, pair.value));
        }
        return false;
    }

    public K getKey() {
        return key;
    }
    public V getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return key.hashCode() * 13 + (value == null ? 0 : value.hashCode());
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }
}
