package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.CacheableTypeDescriptor;
import io.ballerina.runtime.api.types.semtype.TypeCheckCache;

import java.util.HashMap;
import java.util.Map;

public class TypeCheckCacheImpl implements TypeCheckCache {

    private final Map<Integer, Boolean> cache = new HashMap<>();

    public TypeCheckCacheImpl() {
    }

    @Override
    public Boolean cachedTypeCheckResult(CacheableTypeDescriptor other) {
        int targetTypeId = other.typeId();
        return cache.get(targetTypeId);
    }

    public void cacheTypeCheckResult(CacheableTypeDescriptor other, boolean result) {
        int targetTypeId = other.typeId();
        cache.put(targetTypeId, result);
    }
}
