package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.api.types.Type;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * Generalized implementation of type check result cache. It is okay to access
 * this from multiple threads but makes no
 * guarantee about the consistency of the cache under parallel access. Given
 * result don't change due to race conditions
 * this should eventually become consistent.
 *
 * @param <T> Type of the type descriptor which owns this cache
 * @since 2201.11.0
 */
public class TypeCheckCache<T extends Type> {

    // Not synchronizing this should be fine since race conditions don't lead to inconsistent results. (i.e. results
    // of doing multiple type checks are agnostic to the order of execution). Data races shouldn't lead to tearing in
    // 64-bit JVMs.
    private final Map<T, Boolean> cachedResults = new WeakHashMap<>();
    private final T owner;

    public TypeCheckCache(T owner) {
        this.owner = owner;
    }

    public Optional<Boolean> cachedTypeCheckResult(T other) {
        if (other.equals(owner)) {
            return Optional.of(true);
        }
        return Optional.ofNullable(cachedResults.get(other));
    }

    public void cacheTypeCheckResult(T other, boolean result) {
        cachedResults.put(other, result);
    }
}
