/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.core.model.util.serializer;

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Keep track of object identities to find reference sharing and self references.
 *
 * @since 0.982.0
 */
class ObjectUID {
    private long counter = 1;
    private final IdentityHashMap<Object, Long> identityMap = new IdentityHashMap<>();
    private final HashSet<Long> repeatedReferenceSet = new HashSet<>();

    ObjectUID() {
    }

    private long inc() {
        return counter++;
    }

    void addUID(Object obj, BMap<String, BValue> map) {
        map.put(JsonSerializerConst.ID_TAG, new BInteger(track(obj)));
    }

    long track(Object obj) {
        return identityMap.computeIfAbsent(obj, o -> inc());
    }

    boolean isTracked(Object obj) {
        return identityMap.containsKey(obj);
    }

    long findUID(Object key) {
        return identityMap.get(key);
    }

    Set<Long> getRepeatedReferences() {
        return repeatedReferenceSet;
    }

    void addRepeatedRef(long objId) {
        repeatedReferenceSet.add(objId);
    }
}
