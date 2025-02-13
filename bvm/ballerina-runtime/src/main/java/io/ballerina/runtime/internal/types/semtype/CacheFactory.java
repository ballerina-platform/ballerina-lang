package io.ballerina.runtime.internal.types.semtype;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class CacheFactory {

    private CacheFactory() {
    }

    private final static int LIGHT_CACHE_SIZE = 100;
    private final static int DEFAULT_CACHE_SIZE = 100_000;

    public static <K, V> Cache<K, V> createPerInstanceCache() {
        return Caffeine.newBuilder().maximumSize(LIGHT_CACHE_SIZE).build();
    }

    public static <K, V> Cache<K, V> createCache() {
        return Caffeine.newBuilder().maximumSize(DEFAULT_CACHE_SIZE).build();
    }

    public static <K, V> Cache<K, V> createIdentityCache() {
        return Caffeine.newBuilder().weakKeys().maximumSize(DEFAULT_CACHE_SIZE).build();
    }
}
