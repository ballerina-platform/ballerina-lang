package io.ballerina.runtime.internal.types.semtype;

import com.github.benmanes.caffeine.cache.Cache;
import io.ballerina.runtime.api.types.semtype.CacheableTypeDescriptor;
import io.ballerina.runtime.api.types.semtype.TypeCheckCache;

public class TypeCheckCacheImpl implements TypeCheckCache {

    private final Cache<Integer, Boolean> cache = CacheFactory.createTypeCheckCache();

    public TypeCheckCacheImpl() {
    }

    @Override
    public Boolean cachedTypeCheckResult(CacheableTypeDescriptor other) {
        return cache.getIfPresent(other.typeId());
    }

    @Override
    public void cacheTypeCheckResult(CacheableTypeDescriptor other, boolean result) {
        cache.put(other.typeId(), result);
    }

}
