package io.ballerina.runtime.internal.types.semtype;

import com.github.benmanes.caffeine.cache.Cache;
import io.ballerina.runtime.api.types.semtype.CacheableTypeDescriptor;
import io.ballerina.runtime.api.types.semtype.TypeCheckCache;
import io.ballerina.runtime.api.types.semtype.TypeCheckCacheResult;

public class TypeCheckCacheImpl implements TypeCheckCache {

    private final Cache<Integer, Boolean> cache = CacheFactory.createPerInstanceCache();

    public TypeCheckCacheImpl() {
    }

    @Override
    public TypeCheckCacheResult cachedTypeCheckResult(CacheableTypeDescriptor other) {
        int targetTypeId = other.typeId();
        Boolean cachedResult = cache.getIfPresent(targetTypeId);
        if (cachedResult != null) {
            return cachedResult ? TypeCheckCacheResult.TRUE : TypeCheckCacheResult.FALSE;
        }
        return TypeCheckCacheResult.MISS;
    }

    @Override
    public void cacheTypeCheckResult(CacheableTypeDescriptor other, boolean result) {
        cache.put(other.typeId(), result);
    }

}
