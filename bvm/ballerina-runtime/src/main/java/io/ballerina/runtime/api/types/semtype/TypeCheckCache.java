package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.api.types.TypeIdentifier;
import io.ballerina.runtime.internal.types.TypeIdSupplier;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

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
    private final AtomicReferenceArray<CachedResult> cachedResults;
    private final AtomicInteger[] hitCounts;
    private final static AtomicInteger nextId = new AtomicInteger(0);
    private final int id = nextId.getAndIncrement();

    private TypeCheckCache() {
        cachedResults = new AtomicReferenceArray<>(SIZE);
        hitCounts = new AtomicInteger[SIZE];
        Arrays.setAll(hitCounts, i -> new AtomicInteger(-1));
    }

    public Result cachedTypeCheckResult(CacheableTypeDescriptor other) {
        int replacementCandidateId = 0;
        CachedResult replacement = null;
        int minHitCount = Integer.MAX_VALUE;
        int targetTypeId = other.typeId();
        for (int i = 0; i < SIZE; i++) {
            int hitCount = hitCounts[i].get();
            if (hitCount != -1) {
                var each = cachedResults.get(i);
                if (each.typeId == targetTypeId) {
                    hitCounts[i].incrementAndGet();
                    return new Result(true, each.result, null);
                } else if (minHitCount > hitCount) {
                    minHitCount = hitCount;
                    replacementCandidateId = i;
                    replacement = each;
                }
            } else {
                replacementCandidateId = i;
                replacement = null;
            }
        }
        return new Result(false, false, new ReplacementData(replacementCandidateId, replacement));
    }

    public void cacheTypeCheckResult(CacheableTypeDescriptor other, boolean result, ReplacementData replacementData) {
        int index = replacementData.index;
        CachedResult newValue = new CachedResult(other.typeId(), result);
        // Probably this has an effect only on ARM not X86
        if (cachedResults.weakCompareAndSetPlain(index, replacementData.candidate, newValue)) {
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
