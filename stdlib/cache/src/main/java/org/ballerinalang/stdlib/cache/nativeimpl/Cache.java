/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.cache.nativeimpl;

import java.util.concurrent.ConcurrentHashMap;

import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;

/**
 * Ballerina function to cache with java.util.concurrent.ConcurrentHashMap.
 *
 * @since 2.0.0
 */
public class Cache {

    public static final String CACHE_MAP = "CACHE_MAP";

    public static void externInit(ObjectValue cache, int capacity) {
        ConcurrentHashMap<BString, MapValue<BString, Object>> map = new ConcurrentHashMap<>(capacity);
        cache.addNativeData(CACHE_MAP, map);
    }

    public static void externPut(ObjectValue cache, BString key, MapValue<BString, Object> value) {
        ConcurrentHashMap<BString, MapValue<BString, Object>> map =
                (ConcurrentHashMap<BString, MapValue<BString, Object>>) cache.getNativeData(CACHE_MAP);
        map.put(key, value);
    }

    public static MapValue<BString, Object> externGet(ObjectValue cache, BString key) {
        ConcurrentHashMap<BString, MapValue<BString, Object>> map =
                (ConcurrentHashMap<BString, MapValue<BString, Object>>) cache.getNativeData(CACHE_MAP);
        return map.get(key);
    }

    public static MapValue<BString, Object> externRemove(ObjectValue cache, BString key) {
        ConcurrentHashMap<BString, MapValue<BString, Object>> map =
                (ConcurrentHashMap<BString, MapValue<BString, Object>>) cache.getNativeData(CACHE_MAP);
        return map.remove(key);
    }

    public static void externRemoveAll(ObjectValue cache) {
        ConcurrentHashMap<BString, MapValue<BString, Object>> map =
                (ConcurrentHashMap<BString, MapValue<BString, Object>>) cache.getNativeData(CACHE_MAP);
        map.clear();
    }

    public static boolean externHasKey(ObjectValue cache, BString key) {
        ConcurrentHashMap<BString, MapValue<BString, Object>> map =
                (ConcurrentHashMap<BString, MapValue<BString, Object>>) cache.getNativeData(CACHE_MAP);
        return map.containsKey(key);
    }

    public static ArrayValueImpl externKeys(ObjectValue cache) {
        ConcurrentHashMap<BString, MapValue<BString, Object>> map =
                (ConcurrentHashMap<BString, MapValue<BString, Object>>) cache.getNativeData(CACHE_MAP);
        return new ArrayValueImpl(map.keySet().toArray(new BString[0]));
    }

    public static int externSize(ObjectValue cache) {
        ConcurrentHashMap<BString, MapValue<BString, Object>> map =
                (ConcurrentHashMap<BString, MapValue<BString, Object>>) cache.getNativeData(CACHE_MAP);
        return map.size();
    }
}
