/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
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
 *
 */

package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.types.TypeId;
import io.ballerina.runtime.api.types.TypeIdSet;
import io.ballerina.runtime.api.types.semtype.Env;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

final class DistinctIdSupplier implements Supplier<Collection<Integer>> {

    private List<Integer> ids = null;
    private static final Map<TypeIdWrapper, Integer> allocatedIds = new ConcurrentHashMap<>();
    private final Env env;
    private final TypeIdSet typeIdSet;

    DistinctIdSupplier(Env env, TypeIdSet typeIdSet) {
        this.env = env;
        this.typeIdSet = typeIdSet;
    }

    public synchronized Collection<Integer> get() {
        if (ids != null) {
            return ids;
        }
        if (typeIdSet == null) {
            return List.of();
        }
        ids = typeIdSet.getIds().stream().map(TypeIdWrapper::new).map(typeId -> allocatedIds.computeIfAbsent(typeId,
                        ignored -> env.distinctAtomCountGetAndIncrement()))
                .toList();
        return ids;
    }

    // This is to avoid whether id is primary or not affecting the hashcode.
    private record TypeIdWrapper(TypeId typeId) {

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TypeIdWrapper other) {
                return typeId.getName().equals(other.typeId().getName()) &&
                        typeId.getPkg().equals(other.typeId().getPkg());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(typeId.getPkg(), typeId.getName());
        }
    }
}
