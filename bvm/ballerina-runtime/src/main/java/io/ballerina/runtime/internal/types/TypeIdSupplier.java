package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.types.TypeIdentifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class TypeIdSupplier {

    public static int TOMBSTONE = -1;
    private static final Map<TypeIdentifier, Integer> cache = new ConcurrentHashMap<>();
    private static final AtomicInteger nextNamedId = new AtomicInteger(0);
    private static final AtomicInteger nextAnonId = new AtomicInteger(-2);

    private TypeIdSupplier() {

    }

    public static int namedId(TypeIdentifier id) {
        Integer cached = cache.get(id);
        if (cached != null) {
            return cached;
        }
        int typeId = nextNamedId.getAndIncrement();
        cache.put(id, typeId);
        return typeId;
    }

    public static int getAnonId() {
        return nextAnonId.getAndDecrement();
    }
}
