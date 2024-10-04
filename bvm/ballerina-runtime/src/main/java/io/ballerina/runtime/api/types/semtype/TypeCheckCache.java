package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.api.types.Type;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public class TypeCheckCache<T extends Type> {

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
