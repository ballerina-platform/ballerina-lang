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
    private static final int GC_THRESHOLD = 100;
    private final Map<MutableSemType, List<Reference<MutableSemType>>> dependencies = new WeakHashMap<>();
    private final Map<MutableSemType, Object> accessLocks = new WeakHashMap<>();

    public static MutableSemTypeDependencyManager getInstance() {
        return INSTANCE;
    }

    private MutableSemTypeDependencyManager() {
    }

    public void notifyDependenciesToReset(MutableSemType semType) {
        Object lock = getLock(semType);
        synchronized (lock) {
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
    }

    public SemType getSemType(Type target, MutableSemType self) {
        assert target != null;
        if (target instanceof MutableSemType mutableTarget) {
            addDependency(self, mutableTarget);
        }
        return target;
    }

    private void addDependency(MutableSemType self, MutableSemType mutableTarget) {
        Object lock = getLock(mutableTarget);
        synchronized (lock) {
            List<Reference<MutableSemType>> dependencies =
                    this.dependencies.computeIfAbsent(mutableTarget, (ignored) -> new ArrayList<>());
            // garbage collect these dependencies since the actual target may never mutate, triggering the cleanup
            // of the list
            if (dependencies.size() > GC_THRESHOLD) {
                dependencies.removeIf((ref) -> ref.get() == null);
            }
            dependencies.add(new WeakReference<>(self));
        }
    }

    private synchronized Object getLock(MutableSemType semType) {
        return accessLocks.computeIfAbsent(semType, (ignored) -> new Object());
    }
}
