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

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.CacheableTypeDescriptor;
import io.ballerina.runtime.api.types.semtype.TypeCheckCache;

import java.util.HashMap;
import java.util.Map;

/**
 * Hashmap based implementation of {@code TypeCheckCache}. Will allow types to be garbage collected and is fully
 * non-blocking. However make no concurrency guarantees.
 *
 * @since 2201.12.0
 */
public class TypeCheckCacheImpl implements TypeCheckCache {

    private final Map<Integer, Boolean> cache = new HashMap<>();

    public TypeCheckCacheImpl() {
    }

    @Override
    public Boolean cachedTypeCheckResult(CacheableTypeDescriptor other) {
        int targetTypeId = other.typeId();
        return cache.get(targetTypeId);
    }

    public void cacheTypeCheckResult(CacheableTypeDescriptor other, boolean result) {
        int targetTypeId = other.typeId();
        cache.put(targetTypeId, result);
    }
}
