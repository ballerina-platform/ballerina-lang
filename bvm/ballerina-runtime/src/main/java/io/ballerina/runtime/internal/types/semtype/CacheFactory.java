package io.ballerina.runtime.internal.types.semtype;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.util.concurrent.TimeUnit;

public class CacheFactory {

    private CacheFactory() {
    }

    private static final int DEFAULT_EXPIRE_TIME = 10;
    private static final Caffeine<Object, Object> identityCacheBuilder =
            Caffeine.newBuilder().weakKeys().expireAfterAccess(DEFAULT_EXPIRE_TIME, TimeUnit.MINUTES);
    private static final Caffeine<Object, Object> cacheBuilder =
            Caffeine.newBuilder().expireAfterAccess(DEFAULT_EXPIRE_TIME, TimeUnit.MINUTES);

    public static <K, V> LoadingCache<K, V> createCache(CacheLoader<K, V> loader) {
        return cacheBuilder.build(loader);
    }

    public static <K, V> Cache<K, V> createCache() {
        return cacheBuilder.build();
    }

    public static <K, V> LoadingCache<K, V> createIdentityCache(CacheLoader<K, V> loader) {
        return identityCacheBuilder.build(loader);
    }
}
