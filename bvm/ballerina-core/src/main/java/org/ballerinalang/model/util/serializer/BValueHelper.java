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
package org.ballerinalang.model.util.serializer;

import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;

import java.util.HashSet;

/**
 * Helper methods to convert Java objects to BValue objects.
 *
 * @since 0.98.1
 */
public class BValueHelper {
    static BMap<String, BValue> wrapObject(String type, BValue payload) {
        BMap<String, BValue> map = new BMap<>();
        map.put(JsonSerializerConst.TYPE_TAG, createBString(type));
        map.put(JsonSerializerConst.PAYLOAD_TAG, payload);
        return map;
    }

    static BString createBString(String s) {
        if (s == null) {
            return null;
        }
        return new BString(s);
    }

    static BString getHashCode(Object obj) {
        return createBString(getHashCode(obj, null, null));
    }

    static String getHashCode(Object obj, String prefix, String sufix) {
        StringBuilder sb = new StringBuilder();
        if (prefix != null) {
            sb.append(prefix);
            sb.append("#");
        }
        sb.append(obj.getClass().getSimpleName());
        sb.append("#");

        // hashCode function may throw an exception probably due to programming error,
        // and it should not interrupt the serialization process.

        int hash;
        try {
            hash = obj.hashCode();
        } catch (RuntimeException e) {
            // -2 is just a magic number, indicating exception
            hash = -2;
        }
        sb.append(hash);
        if (sufix != null) {
            sb.append("#");
            sb.append(sufix);
        }
        return sb.toString();
    }

    static void addHashValue(Object obj, BMap<String, BValue> map) {
        if (map == null) {
            return;
        }
        map.put(JsonSerializerConst.HASH_TAG, getHashCode(obj));
    }


    /**
     * Walk the object graph and remove the HASH code from nodes that does not repeat.
     *
     * @param jsonObj BValue tree to be trimmed.
     * @param repeatedReferenceSet Set of hashCodes of repeated references.
     */
    @SuppressWarnings("unchecked")
    public static void trimTree(BValue jsonObj, HashSet<String> repeatedReferenceSet) {
        if (jsonObj == null) {
            return;
        }
        if (jsonObj instanceof BMap) {
            BMap map = (BMap) jsonObj;
            BString bHashCode = (BString) map.get(JsonSerializerConst.HASH_TAG);
            if (bHashCode != null) {
                String hashCode = bHashCode.stringValue();
                if (!repeatedReferenceSet.contains(hashCode)) {
                    map.remove(JsonSerializerConst.HASH_TAG);
                }
            }
            trimTree(map.get(JsonSerializerConst.PAYLOAD_TAG), repeatedReferenceSet);
        }
        if (jsonObj instanceof BRefValueArray) {
            BRefValueArray array = (BRefValueArray) jsonObj;
            for (int i = 0; i < array.size(); i++) {
                trimTree(array.get(i), repeatedReferenceSet);
            }
        }
    }
}
