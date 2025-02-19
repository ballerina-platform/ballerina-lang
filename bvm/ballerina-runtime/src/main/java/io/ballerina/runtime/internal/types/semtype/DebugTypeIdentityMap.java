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

import io.ballerina.runtime.api.types.TypeIdentifier;

import java.util.HashMap;
import java.util.function.Function;

public class DebugTypeIdentityMap<E> implements TypeIdentityMap<E> {

    private final TypeIdentityMapImpl<E> typeIdentityMap;
    private final HashMap<TypeIdentifier, E> debugMap;

    public DebugTypeIdentityMap(int initialCapacity) {
        debugMap = new HashMap<>();
        typeIdentityMap = new TypeIdentityMapImpl<>(initialCapacity);
    }

    @Override
    public E get(TypeIdentifier identifier) {
        E value = debugMap.get(identifier);
        assert value.equals(typeIdentityMap.get(identifier));
        return value;
    }

    @Override
    public void put(TypeIdentifier identifier, E value) {
        debugMap.put(identifier, value);
        typeIdentityMap.put(identifier, value);
    }

    @Override
    public E computeIfAbsent(TypeIdentifier identifier, Function<? super TypeIdentifier, ? extends E> mappingFunction) {
        E value = debugMap.get(identifier);
        if (value == null) {
            assert typeIdentityMap.get(identifier) == null;
            value = mappingFunction.apply(identifier);
            debugMap.put(identifier, value);
            typeIdentityMap.put(identifier, value);
        } else {
            assert value.equals(typeIdentityMap.get(identifier));
        }
        return value;

    }

    @Override
    public void clear() {

    }
}
