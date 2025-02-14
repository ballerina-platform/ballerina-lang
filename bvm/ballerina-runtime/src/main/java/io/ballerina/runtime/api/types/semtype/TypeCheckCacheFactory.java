package io.ballerina.runtime.api.types.semtype;

import com.github.benmanes.caffeine.cache.LoadingCache;
import io.ballerina.runtime.api.types.TypeIdentifier;
import io.ballerina.runtime.internal.types.semtype.CacheFactory;
import io.ballerina.runtime.internal.types.semtype.TypeCheckCacheImpl;

/**
 * Factory for creating {@link TypeCheckCache} instances.
 *
 * @since 2201.11.0
 */
public class TypeCheckCacheFactory {

    private static final LoadingCache<TypeIdentifier, TypeCheckCache> cache =
            CacheFactory.createCache(TypeCheckCacheFactory::create);

    private TypeCheckCacheFactory() {
    }

    public static TypeCheckCache get(TypeIdentifier identifier) {
        return cache.get(identifier);
    }

    private static TypeCheckCache create(TypeIdentifier identifier) {
        return new TypeCheckCacheImpl();
    }

    public static TypeCheckCache create() {
        return new TypeCheckCacheImpl();
    }

    public static void reset() {
        cache.invalidateAll();
    }
}
