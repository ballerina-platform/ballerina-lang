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

import com.github.benmanes.caffeine.cache.LoadingCache;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.semtype.CacheableTypeDescriptor;
import io.ballerina.runtime.api.types.semtype.TypeCheckCache;
import io.ballerina.runtime.api.types.semtype.TypeCheckCacheFactory;
import io.ballerina.runtime.internal.types.semtype.CacheFactory;

import java.util.Map;

class SimpleTypeCheckFlyweightStore {

    private final LoadingCache<Integer, TypeCheckFlyweight> unnamedTypeCache =
            CacheFactory.createCache(TypeCheckFlyweight::new);

    private final TypeCheckFlyweight[] reservedLAT =
            new TypeCheckFlyweight[TypeIdSupplier.MAX_RESERVED_ID];

    private final Map<Integer, TypeCheckFlyweight> namedTypeCache = CacheFactory.createCachingHashMap();

    public TypeCheckFlyweight get(Type constraint) {
        if (constraint instanceof CacheableTypeDescriptor cacheableTypeDescriptor) {
            return getInner(cacheableTypeDescriptor.typeId());
        }
        return new TypeCheckFlyweight(TypeIdSupplier.getAnonId(), TypeCheckCacheFactory.create());
    }

    private TypeCheckFlyweight getInner(int typeId) {
        return switch (TypeIdSupplier.kind(typeId)) {
            case RESERVED -> getReserved(typeId);
            case NAMED -> getNamed(typeId);
            case UNNAMED -> getUnnamed(typeId);
        };
    }

    private TypeCheckFlyweight getUnnamed(int typeId) {
        return unnamedTypeCache.get(typeId);
    }

    private TypeCheckFlyweight getNamed(int typeId) {
        var cached = namedTypeCache.get(typeId);
        if (cached != null) {
            return cached;
        }
        cached = new TypeCheckFlyweight(typeId);
        namedTypeCache.put(typeId, cached);
        return cached;
    }

    private TypeCheckFlyweight getReserved(int typeId) {
        TypeCheckFlyweight o = reservedLAT[typeId];
        if (o == null) {
            o = new TypeCheckFlyweight(typeId);
            reservedLAT[typeId] = o;
        }
        return o;
    }

    public record TypeCheckFlyweight(int typeId, TypeCheckCache typeCheckCache) {

        public TypeCheckFlyweight(Integer constraintId) {
            this(TypeIdSupplier.getAnonId(), TypeCheckCacheFactory.create());
        }
    }
}