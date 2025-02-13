package io.ballerina.runtime.api.types.semtype;

/**
 * Generalized implementation of type check result cache. It is okay to access this from multiple threads but makes no
 * guarantee about the consistency of the cache under parallel access. Given result don't change due to race conditions
 * this should eventually become consistent.
 *
 * @since 2201.11.0
 */
public interface TypeCheckCache {

    TypeCheckCacheResult cachedTypeCheckResult(CacheableTypeDescriptor other);

    void cacheTypeCheckResult(CacheableTypeDescriptor other, boolean result);

}
