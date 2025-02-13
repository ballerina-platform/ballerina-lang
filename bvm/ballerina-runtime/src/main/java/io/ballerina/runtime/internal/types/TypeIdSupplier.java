package io.ballerina.runtime.internal.types;

import com.github.benmanes.caffeine.cache.Cache;
import io.ballerina.runtime.api.types.TypeIdentifier;
import io.ballerina.runtime.internal.types.semtype.CacheFactory;

import java.util.concurrent.atomic.AtomicInteger;

public final class TypeIdSupplier {

    private static final Cache<TypeIdentifier, Integer> cache = CacheFactory.createCache();
    private static final AtomicInteger nextNamedId = new AtomicInteger(0);
    private static final AtomicInteger nextAnonId = new AtomicInteger(-2);

    private TypeIdSupplier() {

    }

    public static int namedId(TypeIdentifier id) {
        return cache.get(id, TypeIdSupplier::getNamedId);
    }

    public static int getNamedId(TypeIdentifier id) {
        assert nextNamedId.get() < Integer.MAX_VALUE - 1;
        return nextNamedId.getAndIncrement();
    }

    public static int getAnonId() {
        assert nextAnonId.get() > Integer.MIN_VALUE + 1;
        return nextAnonId.getAndDecrement();
    }
}
