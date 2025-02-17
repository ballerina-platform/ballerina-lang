package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.CacheableTypeDescriptor;
import io.ballerina.runtime.api.types.semtype.TypeCheckCache;

public class TypeCheckCacheImpl implements TypeCheckCache {

    private static final int SIZE = 10;
    private final CachedResult[] cachedResults = new CachedResult[SIZE];
    private final int[] hitCounts = new int[SIZE];

    public TypeCheckCacheImpl() {
    }

    @Override
    public Boolean cachedTypeCheckResult(CacheableTypeDescriptor other) {
        int targetTypeId = other.typeId();
        for (int i = 0; i < SIZE; i++) {
            var each = cachedResults[i];
            if (each != null && each.typeId == targetTypeId) {
                hitCounts[i]++;
                return each.result;
            }
        }
        return null;
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

    private record CachedResult(int typeId, boolean result) {

    }
}
