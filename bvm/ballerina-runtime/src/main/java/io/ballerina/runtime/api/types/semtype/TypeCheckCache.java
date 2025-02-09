package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.api.types.TypeIdentifier;
import io.ballerina.runtime.internal.types.TypeIdSupplier;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
    private final AtomicReference<CachedResult>[] cachedResults;
    private final AtomicInteger[] hitCounts;

    private TypeCheckCache() {
        cachedResults = new AtomicReference[SIZE];
        Arrays.fill(cachedResults, new AtomicReference<>(CachedResult.INIT));
        hitCounts = new AtomicInteger[SIZE];
        Arrays.fill(hitCounts, new AtomicInteger(0));
    }

    public Result cachedTypeCheckResult(CacheableTypeDescriptor other) {
        int replacementCandidateId = 0;
        CachedResult replacement = null;
        int minHitCount = Integer.MAX_VALUE;
        int targetTypeId = other.typeId();
        for (int i = 0; i < SIZE; i++) {
            var each = cachedResults[i].get();
            if (each.typeId == targetTypeId) {
                hitCounts[i].incrementAndGet();
                return new Result(true, each.result, null);
            }
            int hitCount = hitCounts[i].get();
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
        AtomicReference<CachedResult> candidate = cachedResults[index];
        CachedResult newValue = new CachedResult(other.typeId(), result);
        if (candidate.compareAndSet(replacementData.candidate, newValue)) {
            hitCounts[index].set(0);
        }
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
