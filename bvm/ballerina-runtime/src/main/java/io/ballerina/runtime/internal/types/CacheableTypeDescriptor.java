package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.types.Type;

import java.util.Optional;

/**
 * Represent TypeDescriptors whose type check results can be cached.
 *
 * @since 2201.11.0
 */
public interface CacheableTypeDescriptor extends Type {

    boolean shouldCache();

    Optional<Boolean> cachedTypeCheckResult(CacheableTypeDescriptor other);

    void cacheTypeCheckResult(CacheableTypeDescriptor other, boolean result);
}
