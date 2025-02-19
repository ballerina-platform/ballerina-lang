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

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.types.TypeIdentifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class TypeIdentityMapImpl<E> implements TypeIdentityMap<E> {

    private final int INITIAL_CAPACITY;
    private final Map<Module, Map<String, E>> cache;

    public TypeIdentityMapImpl(int initialCapacity) {
        INITIAL_CAPACITY = initialCapacity;
        cache = new HashMap<>();
    }

    @Override
    public E get(TypeIdentifier identifier) {
        Map<String, E> moduleCache = cache.get(identifier.pkg());
        if (moduleCache == null) {
            return null;
        }
        return moduleCache.get(identifier.typeName());
    }

    @Override
    public void put(TypeIdentifier identifier, E value) {
        Map<String, E> moduleCache =
                cache.computeIfAbsent(identifier.pkg(), k -> new HashMap<>(INITIAL_CAPACITY));
        moduleCache.put(identifier.typeName(), value);
    }

    @Override
    public E computeIfAbsent(TypeIdentifier identifier, Function<? super TypeIdentifier, ? extends E> mappingFunction) {
        E result = get(identifier);
        if (result == null) {
            result = mappingFunction.apply(identifier);
            put(identifier, result);
        }
        return result;
    }

    @Override
    public void clear() {
        cache.forEach((module, moduleCache) -> moduleCache.clear());
        cache.clear();
    }
}
