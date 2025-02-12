package io.ballerina.runtime.api.types.semtype;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.ballerina.runtime.api.types.TypeIdentifier;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Generalized implementation of type check result cache. It is okay to access
 * this from multiple threads but makes no
 * guarantee about the consistency of the cache under parallel access. Given
 * result don't change due to race conditions
 * this should eventually become consistent.
 *
 * @since 2201.11.0
 */
public class TypeCheckCache {

    private static final int SIZE = 100;
    private final Cache<Integer, Boolean> cache = Caffeine.newBuilder()
            .maximumSize(SIZE)
            .build();
    private static final AtomicLong nextId = new AtomicLong(0);
    private final long id = nextId.getAndIncrement();

    private TypeCheckCache() {
    }

    public Result cachedTypeCheckResult(CacheableTypeDescriptor other) {
        int targetTypeId = other.typeId();
        Boolean cachedResult = cache.getIfPresent(targetTypeId);
        if (cachedResult != null) {
            return cachedResult ? Result.TRUE : Result.FALSE;
        }
        return Result.MISS;
    }

    public void cacheTypeCheckResult(CacheableTypeDescriptor other, boolean result) {
        cache.put(other.typeId(), result);
    }

    public enum Result {
        TRUE,
        FALSE,
        MISS
    }

    public static class TypeCheckCacheFactory {

        private static final Cache<TypeIdentifier, TypeCheckCache> cache = Caffeine.newBuilder()
                .maximumSize(10_000_000)
                .build();

        private TypeCheckCacheFactory() {
        }

        public static TypeCheckCache get(TypeIdentifier identifier) {
            return cache.get(identifier, TypeCheckCacheFactory::create);
        }

        private static TypeCheckCache create(TypeIdentifier identifier) {
            return new TypeCheckCache();
        }

        public static TypeCheckCache create() {
            return new TypeCheckCache();
        }

        public static void reset() {
            cache.invalidateAll();
        }
    }
}
