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

package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.api.types.TypeIdentifier;
import io.ballerina.runtime.internal.types.semtype.CacheFactory;

import java.util.Map;

/**
 * Factory for creating {@link TypeCheckCache} instances.
 *
 * @since 2201.12.0
 */
public class TypeCheckCacheFactory {

    private static final Map<TypeIdentifier, TypeCheckCache> cache =
            CacheFactory.createCachingHashMap();

    private TypeCheckCacheFactory() {
    }

    public static TypeCheckCache get(TypeIdentifier identifier) {
        if (identifier.avoidCaching()) {
            return create();
        }
        var cached = cache.get(identifier);
        if (cached != null) {
            return cached;
        }
        var newCache = create();
        cache.put(identifier, newCache);
        return newCache;
    }

    public static TypeCheckCache create() {
        return new TypeCheckCache();
    }

    public static void reset() {
        cache.clear();
    }
}
