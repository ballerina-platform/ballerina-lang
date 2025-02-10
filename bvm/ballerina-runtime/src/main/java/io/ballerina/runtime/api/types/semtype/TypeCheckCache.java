package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.api.types.TypeIdentifier;
import io.ballerina.runtime.internal.types.TypeIdSupplier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final static AtomicInteger nextId = new AtomicInteger(0);
    private final int id = nextId.getAndIncrement();

    private TypeCheckCache() {
    }

    public Result cachedTypeCheckResult(CacheableTypeDescriptor other) {
        int replacementCandidateId = 0;
        CachedResult replacement = null;
        int minHitCount = Integer.MAX_VALUE;
        int targetTypeId = other.typeId();
        for (int i = 0; i < SIZE; i++) {
            var each = cachedResults[i];
            if (each != null) {
                if (each.typeId == targetTypeId) {
                    hitCounts[i]++;
                    return new Result(true, each.result, null);
                }
            }
            int hitCount = hitCounts[i];
            if (minHitCount > hitCount) {
                minHitCount = hitCount;
                replacementCandidateId = i;
                replacement = each;
            }
        }
        return new Result(false, false, new ReplacementData(replacementCandidateId, replacement));
    }

    public void cacheTypeCheckResult(CacheableTypeDescriptor other, boolean result, ReplacementData replacementData) {
        int index = replacementData.index;
        CachedResult newValue = new CachedResult(other.typeId(), result);
        cachedResults[index] = newValue;
        hitCounts[index] = 0;
    }

    public record Result(boolean hit, boolean result, ReplacementData replacementData) {

    }

    public record ReplacementData(int index, CachedResult candidate) {

    }

    private record CachedResult(int typeId, boolean result) {

        public static CachedResult INIT = new CachedResult(TypeIdSupplier.TOMBSTONE, false);

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
