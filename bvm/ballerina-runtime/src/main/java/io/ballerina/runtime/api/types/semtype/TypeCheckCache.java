package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.api.types.TypeIdentifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    private static final int SIZE = 10;
    private final CachedResult[] cachedResults = new CachedResult[SIZE];
    private final int[] hitCounts = new int[SIZE];

    private TypeCheckCache() {
    }

    public Result cachedTypeCheckResult(CacheableTypeDescriptor other) {
        int targetTypeId = other.typeId();
        for (int i = 0; i < SIZE; i++) {
            var each = cachedResults[i];
            if (each != null && each.typeId == targetTypeId) {
                hitCounts[i]++;
                return each.result ? Result.TRUE : Result.FALSE;
            }
        }
        return Result.MISS;
    }

    public void cacheTypeCheckResult(CacheableTypeDescriptor other, boolean result) {
        int index = -1;
        int minHitCount = Integer.MAX_VALUE;
        for (int i = 0; i < SIZE; i++) {
            if (cachedResults[i] == null) {
                index = i;
                break;
            }
            if (hitCounts[i] < minHitCount) {
                minHitCount = hitCounts[i];
                index = i;
            }
        }
        CachedResult newValue = new CachedResult(other.typeId(), result);
        cachedResults[index] = newValue;
        hitCounts[index] = 0;
    }

    public enum Result {
        TRUE,
        FALSE,
        MISS
    }

    private record CachedResult(int typeId, boolean result) {

    }

    public static class TypeCheckCacheFactory {

        private static final Map<TypeIdentifier, TypeCheckCache> namedCaches = new ConcurrentHashMap<>();

        private TypeCheckCacheFactory() {
        }

        public static TypeCheckCache get(TypeIdentifier identifier) {
            return namedCaches.computeIfAbsent(identifier, ignored -> new TypeCheckCache());
        }

        public static TypeCheckCache create() {
            return new TypeCheckCache();
        }
    }
}
