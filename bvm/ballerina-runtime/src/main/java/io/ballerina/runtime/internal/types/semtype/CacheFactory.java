package io.ballerina.runtime.internal.types.semtype;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class CacheFactory {

    private CacheFactory() {
    }

    private static final int LIGHT_CACHE_SIZE = 100;
    private static final int DEFAULT_CACHE_SIZE = 100_000;

    private static final Caffeine<Object, Object> TYPE_CHECK_CACHE_BUILDER =
            Caffeine.newBuilder().maximumSize(LIGHT_CACHE_SIZE);

    public static Cache<Integer, Boolean> createTypeCheckCache() {
        return TYPE_CHECK_CACHE_BUILDER.build();
    }

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
