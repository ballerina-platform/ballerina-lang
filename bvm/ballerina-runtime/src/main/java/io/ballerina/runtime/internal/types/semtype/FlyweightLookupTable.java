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

import com.github.benmanes.caffeine.cache.LoadingCache;
import io.ballerina.runtime.api.types.semtype.Definition;
import io.ballerina.runtime.internal.types.TypeIdSupplier;

import java.util.Map;
import java.util.function.IntFunction;

@SuppressWarnings("rawtypes")
public class FlyweightLookupTable<E extends Definition> {

    private final IntFunction<TypeCheckCacheFlyweight<E>> createFn;
    private final LoadingCache<Integer, TypeCheckCacheFlyweight<E>> unnamedTypeCache;
    private final Map<Integer, TypeCheckCacheFlyweight<E>> namedTypeCache = CacheFactory.createCachingHashMap();
    private final TypeCheckCacheFlyweight[] reservedLAT;

    public FlyweightLookupTable(IntFunction<TypeCheckCacheFlyweight<E>> createFn) {
        this.createFn = createFn;
        this.unnamedTypeCache = CacheFactory.createCache(createFn::apply);
        this.reservedLAT = new TypeCheckCacheFlyweight[TypeIdSupplier.MAX_RESERVED_ID];
    }

    public TypeCheckCacheFlyweight<E> get(int typeId) {
        return switch (TypeIdSupplier.kind(typeId)) {
            case RESERVED -> getReserved(typeId);
            case NAMED -> getNamed(typeId);
            case UNNAMED -> getUnnamed(typeId);
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private TypeCheckCacheFlyweight<E> getReserved(int typeId) {
        TypeCheckCacheFlyweight o = reservedLAT[typeId];
        if (o == null) {
            o = createFn.apply(typeId);
            reservedLAT[typeId] = o;
        }
        return (TypeCheckCacheFlyweight<E>) o;
    }

    private TypeCheckCacheFlyweight<E> getUnnamed(int typeId) {
        return unnamedTypeCache.get(typeId);
    }

    private TypeCheckCacheFlyweight<E> getNamed(int typeId) {
        return namedTypeCache.computeIfAbsent(typeId, createFn::apply);
    }
}
