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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Use to get a unique id for a type. Multiple instances of the same type (Based on the {@code TypeIdentifier}) will get
 * the same id. Type Ids should be preferred over comparing instances (and {@code TypeIdentifier}s) directly, for caches
 * and equals.
 *
 * @since 2201.12.0
 */
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

    public static int reserveNamedId() {
        return getNamedId();
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
