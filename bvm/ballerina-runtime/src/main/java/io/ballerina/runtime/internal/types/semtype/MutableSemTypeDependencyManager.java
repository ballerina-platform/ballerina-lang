/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.semtype.MutableSemType;
import io.ballerina.runtime.api.types.semtype.SemType;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MutableSemTypeDependencyManager {

    private static final MutableSemTypeDependencyManager INSTANCE = new MutableSemTypeDependencyManager();
    private final Map<MutableSemTypeKey, List<Reference<MutableSemType>>> dependencies = new HashMap<>();

    public static MutableSemTypeDependencyManager getInstance() {
        return INSTANCE;
    }

    private MutableSemTypeDependencyManager() {
    }

    public synchronized void notifyDependenciesToReset(MutableSemType semType) {
        MutableSemTypeKey key = MutableSemTypeKey.from(semType);
        List<Reference<MutableSemType>> mutableSemTypes = dependencies.get(key);
        if (mutableSemTypes != null) {
            dependencies.remove(key);
            for (Reference<MutableSemType> mutableSemType : mutableSemTypes) {
                MutableSemType dependent = mutableSemType.get();
                if (dependent != null) {
                    dependent.resetSemType();
                }
            }
        }
    }

    public synchronized SemType getSemType(Type target, MutableSemType self) {
        assert target != null;
        if (target instanceof MutableSemType mutableTarget) {
            MutableSemTypeKey key = MutableSemTypeKey.from(mutableTarget);
            List<Reference<MutableSemType>> dependencies =
                    this.dependencies.computeIfAbsent(key, (ignored) -> new ArrayList<>());
            dependencies.add(new WeakReference<>(self));
        }
        return target;
    }

    private record MutableSemTypeKey(WeakReference<MutableSemType> semTypeRef) {

        private static MutableSemTypeKey from(MutableSemType semType) {
            return new MutableSemTypeKey(new WeakReference<>(semType));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MutableSemTypeKey that) {
                if (semTypeRef.get() == null || that.semTypeRef().get() == null) {
                    return false;
                }
                return semTypeRef.get() == that.semTypeRef.get();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(semTypeRef.get());
        }
    }
}
