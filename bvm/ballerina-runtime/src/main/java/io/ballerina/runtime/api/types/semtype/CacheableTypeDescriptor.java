package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.api.types.Type;

/**
 * Represent TypeDescriptors whose type check results can be cached.
 *
 * @since 2201.11.0
 */
public interface CacheableTypeDescriptor extends Type {

    /**
     * Check whether the type check result of this type descriptor is cached for the given type descriptor.
     *
     * @param cx    Context in which the type check is performed
     * @param other Type descriptor to check the cached result for
     * @return Result of the type check if it is cached, {@code MISS}  otherwise
     */
    TypeCheckCacheResult cachedTypeCheckResult(Context cx, CacheableTypeDescriptor other);

    /**
     * Cache the type check result of this type descriptor for the given type descriptor. Note that implementations of
     * this method could choose to not cache the result.
     *
     * @param other  Type descriptor to cache the result for
     * @param result Result of the type check
     */
    void cacheTypeCheckResult(CacheableTypeDescriptor other, boolean result);

    int typeId();
}
