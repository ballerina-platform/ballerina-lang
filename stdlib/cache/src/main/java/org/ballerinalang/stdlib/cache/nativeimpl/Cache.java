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

import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.HandleValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BArray;
import org.ballerinalang.jvm.values.api.BMap;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.stdlib.cache.nativeimpl.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import java.util.Map;

/**
 * Ballerina function to cache with java.util.concurrent.ConcurrentHashMap.
 *
 * @since 2.0.0
 */
public class Cache {

    private static ConcurrentLinkedHashMap<String, BMap<String, Object>> cacheMap;
    private static final String MAX_CAPACITY = "capacity";
    private static final String EVICTION_FACTOR = "evictionFactor";
    private static final String EXPIRE_TIME = "expTime";
    private static final String CACHE = "CACHE";
    private static final String ID = "ID";

    private Cache() {}

    public static void externInit(ObjectValue cache) {
        int capacity = (int) cache.getIntValue(MAX_CAPACITY);
        cacheMap = new ConcurrentLinkedHashMap<>(capacity);
        cache.addNativeData(CACHE, cacheMap);
    }

    @SuppressWarnings("unchecked")
    public static void externPut(ObjectValue cache, String key, BMap<String, Object> value) {
        int capacity = (int) cache.getIntValue(MAX_CAPACITY);
        float evictionFactor = (float) cache.getFloatValue(EVICTION_FACTOR);
        cacheMap = (ConcurrentLinkedHashMap<String, BMap<String, Object>>) cache.getNativeData(CACHE);
        if (cacheMap.size() >= capacity) {
            int evictionKeysCount = (int) Math.ceil(capacity * evictionFactor);
            cacheMap.setCapacity((capacity - evictionKeysCount));
            cacheMap.setCapacity(capacity);
        }
        cacheMap.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static Object externGet(ObjectValue cache, String key, Long currentTime) {
        cacheMap = (ConcurrentLinkedHashMap<String, BMap<String, Object>>) cache.getNativeData(CACHE);
        BMap<String, Object> value = cacheMap.get(key);
        Long time = (Long) value.get(EXPIRE_TIME);
        if (time != -1 && time <= currentTime) {
            cacheMap.remove(key);
            return null;
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public static void externRemove(ObjectValue cache, String key) {
        cacheMap = (ConcurrentLinkedHashMap<String, BMap<String, Object>>) cache.getNativeData(CACHE);
        cacheMap.remove(key);
    }

    @SuppressWarnings("unchecked")
    public static void externRemoveAll(ObjectValue cache) {
        cacheMap = (ConcurrentLinkedHashMap<String, BMap<String, Object>>) cache.getNativeData(CACHE);
        cacheMap.clear();
    }

    @SuppressWarnings("unchecked")
    public static boolean externHasKey(ObjectValue cache, String key) {
        cacheMap = (ConcurrentLinkedHashMap<String, BMap<String, Object>>) cache.getNativeData(CACHE);
        return cacheMap.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public static BArray externKeys(ObjectValue cache) {
        cacheMap = (ConcurrentLinkedHashMap<String, BMap<String, Object>>) cache.getNativeData(CACHE);
        String[] keySets = cacheMap.keySet().toArray(new String[0]);
        HandleValue[] handleValues = new HandleValue[keySets.length];
        for (int i = 0; i < keySets.length; i++) {
            handleValues[i] = new HandleValue(keySets[i]);
        }
        return BValueCreator.createArrayValue(handleValues, new BArrayType(BTypes.typeHandle));
    }

    @SuppressWarnings("unchecked")
    public static int externSize(ObjectValue cache) {
        cacheMap = (ConcurrentLinkedHashMap<String, BMap<String, Object>>) cache.getNativeData(CACHE);
        return cacheMap.size();
    }

    @SuppressWarnings("unchecked")
    public static void externCleanUp(ObjectValue cache, Long currentTime) {
        cacheMap = (ConcurrentLinkedHashMap<String, BMap<String, Object>>) cache.getNativeData(CACHE);
        for (Map.Entry<String, BMap<String, Object>> entry : cacheMap.entrySet()) {
            BMap<String, Object> value = entry.getValue();
            Long time = (Long) value.get(EXPIRE_TIME);
            if (time != -1 && time <= currentTime) {
                cacheMap.remove(entry.getKey());
            }
        }
    }
}
