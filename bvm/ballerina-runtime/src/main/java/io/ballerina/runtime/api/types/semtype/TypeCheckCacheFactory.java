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
        return cache.computeIfAbsent(identifier, TypeCheckCacheFactory::create);
    }

    private static TypeCheckCache create(TypeIdentifier identifier) {
        return new TypeCheckCacheImpl();
    }

    public static TypeCheckCache create() {
        return new TypeCheckCacheImpl();
    }

    public static void reset() {
        cache.clear();
    }
}
