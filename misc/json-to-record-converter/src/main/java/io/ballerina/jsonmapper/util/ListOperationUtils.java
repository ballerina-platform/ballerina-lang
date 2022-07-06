/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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
 */

package io.ballerina.jsonmapper.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Util methods for JSON to record direct converter.
 *
 * @since 2201.2.0
 */
public final class ListOperationUtils {

    private ListOperationUtils() {}

    /**
     * This method returns the intersection of Key, Value pairs based on the keys of the pairs.
     *
     * @param mapOne First set of Key, Value pairs to be compared with other
     * @param mapTwo Second set of Key, Value pairs to be compared with other
     * @return {@link Map} Intersection of first and second set of Key Value pairs
     */
    public static <K, V> Map<K, V> intersection(Map<K, V> mapOne, Map<K, V> mapTwo) {
        Map<K, V> intersection = new LinkedHashMap<>();
        for (Map.Entry<K, V> key: mapOne.entrySet()) {
            if (mapTwo.containsKey(key.getKey())) {
                intersection.put(key.getKey(), mapOne.get(key.getKey()));
            }
        }
        return intersection;
    }

    /**
     * This method returns the union of Key, Value pairs based on the keys of the pairs.
     *
     * @param mapOne First set of Key, Value pairs to be compared with other
     * @param mapTwo Second set of Key, Value pairs to be compared with other
     * @return {@link Map} Union of first and second set of Key Value pairs
     */
    public static <K, V> Map<K, V> union(Map<K, V> mapOne, Map<K, V> mapTwo) {
        Map<K, V> union = new LinkedHashMap<>(mapOne);
        for (Map.Entry<K, V> key: mapTwo.entrySet()) {
            if (!mapOne.containsKey(key.getKey())) {
                union.put(key.getKey(), mapTwo.get(key.getKey()));
            }
        }
        return union;
    }

    /**
     * This method returns the difference of Key, Value pairs based on the keys of the pairs.
     *
     * @param mapOne First set of Key, Value pairs to be compared with other
     * @param mapTwo Second set of Key, Value pairs to be compared with other
     * @return {@link Map} Difference of first and second set of Key Value pairs
     */
    public static <K, V> Map<K, V> difference(Map<K, V> mapOne, Map<K, V> mapTwo) {
        Map<K, V> unionMap = union(mapOne, mapTwo);
        Map<K, V> intersectionMap = intersection(mapOne, mapTwo);
        for (Map.Entry<K, V> key: intersectionMap.entrySet()) {
            unionMap.remove(key.getKey());
        }
        return unionMap;
    }
}
