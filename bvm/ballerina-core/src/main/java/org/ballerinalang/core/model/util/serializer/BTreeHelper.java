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
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;

import java.util.Set;

/**
 * Helper methods to convert Java objects to BValue tree.
 *
 * @since 0.982.0
 */
class BTreeHelper {

    private BTreeHelper() {
    }

    static BMap<String, BValue> wrapWithTypeMetadata(String type, BValue payload) {
        BMap<String, BValue> map = new BMap<>();
        map.put(JsonSerializerConst.TYPE_TAG, createBString(type));
        map.put(JsonSerializerConst.VALUE_TAG, payload);
        return map;
    }

    static BString createBString(String s) {
        if (s == null) {
            return null;
        }
        return new BString(s);
    }

    /**
     * Walk the object graph and remove the HASH code from nodes that does not repeat.
     *
     * @param jsonObj              BValue tree to be trimmed.
     * @param repeatedReferenceSet Set of hashCodes of repeated references.
     */
    @SuppressWarnings("unchecked")
    static void trimTree(BValue jsonObj, Set<Long> repeatedReferenceSet) {
        if (jsonObj == null) {
            return;
        }
        if (jsonObj instanceof BMap) {
            BMap map = (BMap) jsonObj;
            BInteger objId = (BInteger) map.get(JsonSerializerConst.ID_TAG);
            if (objId != null && !repeatedReferenceSet.contains(objId.intValue())) {
                map.remove(JsonSerializerConst.ID_TAG);
            }
            trimTree(map.get(JsonSerializerConst.VALUE_TAG), repeatedReferenceSet);
        }
        if (jsonObj instanceof BValueArray) {
            BValueArray array = (BValueArray) jsonObj;
            for (int i = 0; i < array.size(); i++) {
                trimTree(array.getRefValue(i), repeatedReferenceSet);
            }
        }
    }
}
