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

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Ballerina function to cache with java.util.concurrent.ConcurrentHashMap.
 *
 * @since 2.0.0
 */
public class Cache {

    public static final String CACHE_MAP = "CACHE_MAP";

    public static void externInit(BObject cache, int capacity) {
        ConcurrentHashMap<BString, BMap<BString, Object>> map = new ConcurrentHashMap<>(capacity);
        cache.addNativeData(CACHE_MAP, map);
    }

    public static void externPut(BObject cache, BString key, BMap<BString, Object> value) {
        ConcurrentHashMap<BString, BMap<BString, Object>> map =
                (ConcurrentHashMap<BString, BMap<BString, Object>>) cache.getNativeData(CACHE_MAP);
        map.put(key, value);
    }

    public static BMap<BString, Object> externGet(BObject cache, BString key) {
        ConcurrentHashMap<BString, BMap<BString, Object>> map =
                (ConcurrentHashMap<BString, BMap<BString, Object>>) cache.getNativeData(CACHE_MAP);
        return map.get(key);
    }

    public static void externRemove(BObject cache, BString key) {
        ConcurrentHashMap<BString, BMap<BString, Object>> map =
                (ConcurrentHashMap<BString, BMap<BString, Object>>) cache.getNativeData(CACHE_MAP);
        map.remove(key);
    }

    public static void externRemoveAll(BObject cache) {
        ConcurrentHashMap<BString, BMap<BString, Object>> map =
                (ConcurrentHashMap<BString, BMap<BString, Object>>) cache.getNativeData(CACHE_MAP);
        map.clear();
    }

    public static boolean externHasKey(BObject cache, BString key) {
        ConcurrentHashMap<BString, BMap<BString, Object>> map =
                (ConcurrentHashMap<BString, BMap<BString, Object>>) cache.getNativeData(CACHE_MAP);
        return map.containsKey(key);
    }

    public static BArray externKeys(BObject cache) {
        ConcurrentHashMap<BString, BMap<BString, Object>> map =
                (ConcurrentHashMap<BString, BMap<BString, Object>>) cache.getNativeData(CACHE_MAP);
        return ValueCreator.createArrayValue(map.keySet().toArray(new BString[0]));
    }

    public static int externSize(BObject cache) {
        ConcurrentHashMap<BString, BMap<BString, Object>> map =
                (ConcurrentHashMap<BString, BMap<BString, Object>>) cache.getNativeData(CACHE_MAP);
        return map.size();
    }
}
