package com.example.finance.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class RolesCache {
    private final int capacity;
    private final Map<String, String> cache;

    public RolesCache(int capacity) {
        this.capacity = capacity;
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > RolesCache.this.capacity;
            }
        };
    }

    public void set(String key, String value) {
        cache.put(key, value);
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(cache.get(key));
    }
}
