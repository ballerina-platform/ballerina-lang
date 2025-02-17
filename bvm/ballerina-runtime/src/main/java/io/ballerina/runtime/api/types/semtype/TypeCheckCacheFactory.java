package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.api.types.TypeIdentifier;
import io.ballerina.runtime.internal.types.semtype.CacheFactory;
import io.ballerina.runtime.internal.types.semtype.TypeCheckCacheImpl;

import java.util.Map;

/**
 * Factory for creating {@link TypeCheckCache} instances.
 *
 * @since 2201.11.0
 */
public class TypeCheckCacheFactory {

    private static final Map<TypeIdentifier, TypeCheckCache> cache =
            CacheFactory.createCachingHashMap();

    private TypeCheckCacheFactory() {
    }

    public static TypeCheckCache get(TypeIdentifier identifier) {
        var cached = cache.get(identifier);
        if (cached != null) {
            return cached;
        }
        var newCache = create();
        cache.put(identifier, newCache);
        return newCache;
    }

    public static TypeCheckCache create() {
        return new TypeCheckCacheImpl();
    }

    public static void reset() {
        cache.clear();
    }
}
