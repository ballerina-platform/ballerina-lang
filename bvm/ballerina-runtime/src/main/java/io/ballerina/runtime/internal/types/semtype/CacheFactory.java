package io.ballerina.runtime.internal.types.semtype;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CacheFactory {

    private CacheFactory() {
    }

    private static final int DEFAULT_EXPIRE_TIME = 10;
    private static final int INITIAL_CAPACITY = 10_000;
    // Week keys are compared using ==, and they are eligible for garbage collection, so need to have an expiration
    private static final Caffeine<Object, Object> identityCacheBuilder =
            Caffeine.newBuilder().weakKeys().initialCapacity(INITIAL_CAPACITY);
    private static final Caffeine<Object, Object> cacheBuilder =
            Caffeine.newBuilder().expireAfterAccess(DEFAULT_EXPIRE_TIME, TimeUnit.MINUTES)
                    .initialCapacity(INITIAL_CAPACITY);

    public static <K, V> LoadingCache<K, V> createCache(CacheLoader<K, V> loader) {
        return cacheBuilder.build(loader);
    }

    public static <K, V> Cache<K, V> createCache() {
        return cacheBuilder.build();
    }

    public static <K, V> LoadingCache<K, V> createIdentityCache(CacheLoader<K, V> loader) {
        return identityCacheBuilder.build(loader);
    }

    public static <K, V> Map<K, V> createCachingHashMap() {
        return new HashMap<>(INITIAL_CAPACITY);
    }
}
