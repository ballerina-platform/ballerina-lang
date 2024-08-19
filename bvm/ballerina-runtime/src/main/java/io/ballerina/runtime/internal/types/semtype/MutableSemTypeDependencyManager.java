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
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class MutableSemTypeDependencyManager {

    private static final MutableSemTypeDependencyManager INSTANCE = new MutableSemTypeDependencyManager();
    private final Map<MutableSemType, List<Reference<MutableSemType>>> dependencies = new WeakHashMap<>();

    public static MutableSemTypeDependencyManager getInstance() {
        return INSTANCE;
    }

    private MutableSemTypeDependencyManager() {
    }

    public synchronized void notifyDependenciesToReset(MutableSemType semType) {
        List<Reference<MutableSemType>> mutableSemTypes = dependencies.get(semType);
        if (mutableSemTypes != null) {
            dependencies.remove(semType);
            for (var dependent : mutableSemTypes) {
                MutableSemType dependentSemType = dependent.get();
                if (dependentSemType != null) {
                    dependentSemType.resetSemType();
                }
            }
        }
    }

    public synchronized SemType getSemType(Type target, MutableSemType self) {
        assert target != null;
        if (target instanceof MutableSemType mutableTarget) {
            List<Reference<MutableSemType>> dependencies =
                    this.dependencies.computeIfAbsent(mutableTarget, (ignored) -> new ArrayList<>());
            dependencies.add(new WeakReference<>(self));
        }
        return target;
    }
}
