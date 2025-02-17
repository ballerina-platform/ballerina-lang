package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.types.TypeIdentifier;
import io.ballerina.runtime.internal.types.semtype.CacheFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class TypeIdSupplier {

    private static final Map<TypeIdentifier, Integer> cache = CacheFactory.createCachingHashMap();
    private static final AtomicInteger nextNamedId = new AtomicInteger(0);
    private static final AtomicInteger nextAnonId = new AtomicInteger(-2);

    private TypeIdSupplier() {

    }

    public static int namedId(TypeIdentifier id) {
        var cachedId = cache.get(id);
        if (cachedId != null) {
            return cachedId;
        }
        var newId = getNamedId();
        cache.put(id, newId);
        return newId;
    }

    public static int getNamedId() {
        assert nextNamedId.get() < Integer.MAX_VALUE - 1;
        return nextNamedId.getAndIncrement();
    }

    public static int getAnonId() {
        assert nextAnonId.get() > Integer.MIN_VALUE + 1;
        return nextAnonId.getAndDecrement();
    }
}
