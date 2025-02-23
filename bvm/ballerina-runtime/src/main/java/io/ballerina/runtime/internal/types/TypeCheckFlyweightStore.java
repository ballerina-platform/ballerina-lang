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

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.semtype.CacheableTypeDescriptor;
import io.ballerina.runtime.api.types.semtype.Definition;
import io.ballerina.runtime.api.types.semtype.TypeCheckCache;
import io.ballerina.runtime.api.types.semtype.TypeCheckCacheFactory;
import io.ballerina.runtime.internal.types.semtype.DefinitionContainer;
import io.ballerina.runtime.internal.types.semtype.FlyweightLookupTable;
import io.ballerina.runtime.internal.types.semtype.TypeCheckCacheFlyweight;

class TypeCheckFlyweightStore<E extends Definition> {

    private final FlyweightLookupTable<E> cacheRO = new FlyweightLookupTable<>(this::create);

    private final FlyweightLookupTable<E> cacheRW = new FlyweightLookupTable<>(this::create);

    private TypeCheckCacheFlyweight<E> create(int constraintId) {
        int typeId = switch (TypeIdSupplier.kind(constraintId)) {
            case RESERVED -> TypeIdSupplier.getReservedId();
            case NAMED -> TypeIdSupplier.getNamedId();
            case UNNAMED -> TypeIdSupplier.getAnonId();
        };
        TypeCheckCache typeCheckCache = TypeCheckCacheFactory.create();
        DefinitionContainer<E> defn = new DefinitionContainer<>();
        DefinitionContainer<E> acceptedTypeDefn = new DefinitionContainer<>();
        return new TypeCheckCacheFlyweight<>(typeId, typeCheckCache, defn, acceptedTypeDefn);
    }

    private TypeCheckCacheFlyweight<E> create() {
        int typeId = TypeIdSupplier.getAnonId();
        TypeCheckCache typeCheckCache = TypeCheckCacheFactory.create();
        DefinitionContainer<E> defn = new DefinitionContainer<>();
        DefinitionContainer<E> acceptedTypeDefn = new DefinitionContainer<>();
        return new TypeCheckCacheFlyweight<>(typeId, typeCheckCache, defn, acceptedTypeDefn);
    }

    public TypeCheckCacheFlyweight<E> getRO(Type constraint) {
        return get(cacheRO, constraint);
    }

    public TypeCheckCacheFlyweight<E> getRW(Type constraint) {
        return get(cacheRW, constraint);
    }

    private TypeCheckCacheFlyweight<E> get(
            FlyweightLookupTable<E> lookupTable, Type constraint) {
        if (constraint instanceof CacheableTypeDescriptor cacheableTypeDescriptor) {
            return lookupTable.get(cacheableTypeDescriptor.typeId());
        }
        return create();
    }

}
