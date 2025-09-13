/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.types.TypeIdentifier;
import io.ballerina.runtime.internal.types.semtype.CacheFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Use to get a unique id for a type. Multiple instances of the same type (Based on the {@code TypeIdentifier}) will get
 * the same id. Type Ids should be preferred over comparing instances (and {@code TypeIdentifier}s) directly, for caches
 * and equals.
 *
 * @since 2201.12.0
 */
public final class TypeIdSupplier {

    // Anydata         : 2
    // JSON            : 2
    // XML             : 9
    // Immutable types : 10
    // Reserved Base   = 23 (total above)
    // Total Reserved  = 23 * (1 + 2 + 2) = 115 (2 for map and arrays per each)
    public static final int MAX_RESERVED_ID = 128;
    private static final Map<TypeIdentifier, Integer> cache = CacheFactory.createCachingHashMap();
    private static final AtomicInteger nextNamedId = new AtomicInteger(MAX_RESERVED_ID);
    private static final AtomicInteger nextReservedId = new AtomicInteger(0);
    private static final AtomicInteger nextAnonId = new AtomicInteger(-2);
    private static final AtomicBoolean reservedIdsExhausted = new AtomicBoolean(false);

    private TypeIdSupplier() {

    }

    public static int namedId(TypeIdentifier id) {
        if (id.avoidCaching()) {
            return getAnonId();
        }
        var cachedId = cache.get(id);
        if (cachedId != null) {
            return cachedId;
        }
        var newId = getNamedId();
        cache.put(id, newId);
        return newId;
    }

    public static int getReservedId() {
        // This can happen if there are a lot of maps and arrays with reserved ids (T[][][][]). In that case we will
        // gracefully overflow to named ids.
        if (reservedIdsExhausted.get()) {
            return getNamedId();
        }
        int id = nextReservedId.getAndIncrement();
        if (id >= MAX_RESERVED_ID) {
            reservedIdsExhausted.set(true);
            return getNamedId();
        }
        return id;
    }

    public static int getNamedId() {
        assert nextNamedId.get() < Integer.MAX_VALUE - 1;
        return nextNamedId.getAndIncrement();
    }

    public static int getAnonId() {
        assert nextAnonId.get() > Integer.MIN_VALUE + 1;
        return nextAnonId.getAndDecrement();
    }

    public static IdKind kind(int id) {
        if (id < 0) {
            return IdKind.UNNAMED;
        }
        if (id < MAX_RESERVED_ID) {
            return IdKind.RESERVED;
        }
        return IdKind.NAMED;
    }

    public enum IdKind {
        RESERVED,
        NAMED,
        UNNAMED
    }

    public static boolean isAnon(int id) {
        return id < 0;
    }
}
