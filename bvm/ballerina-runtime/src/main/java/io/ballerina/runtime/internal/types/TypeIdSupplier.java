package io.ballerina.runtime.internal.types;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.ballerina.runtime.api.types.TypeIdentifier;

import java.util.concurrent.atomic.AtomicInteger;

public final class TypeIdSupplier {

    private static final Cache<TypeIdentifier, Integer> cache = Caffeine.newBuilder()
            .maximumSize(10_000_000)
            .build();
    private static final AtomicInteger nextNamedId = new AtomicInteger(0);
    private static final AtomicInteger nextAnonId = new AtomicInteger(-2);

    private TypeIdSupplier() {

    }

    public static int namedId(TypeIdentifier id) {
        return cache.get(id, TypeIdSupplier::getNamedId);
    }

    public static int getNamedId(TypeIdentifier id) {
        return nextNamedId.getAndIncrement();
    }

    public static int getAnonId() {
        return nextAnonId.getAndDecrement();
    }
}
