/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semver.checker.diff;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * A concrete extractor implementation which can be used separate additions, removals and common items between the two
 * given named maps with item type 'T'.
 *
 * @param <T> item type
 * @since 2201.2.0
 */
public class DiffExtractor<T> {

    private final Map<String, T> newValues;
    private final Map<String, T> oldValues;

    private Map<String, T> additions;
    private Map<String, T> removals;
    private Map<String, Map.Entry<T, T>> commons;

    public DiffExtractor(Map<String, T> newVals, Map<String, T> oldVals) {
        this.newValues = newVals;
        this.oldValues = oldVals;
    }

    public Map<String, T> getAdditions() {
        extractChanges();
        return additions;
    }

    public Map<String, T> getRemovals() {
        extractChanges();
        return removals;
    }

    public Map<String, Map.Entry<T, T>> getCommons() {
        extractChanges();
        return commons;
    }

    private void extractChanges() {
        if (additions != null && removals != null && commons != null) {
            return;
        }

        additions = new HashMap<>();
        removals = new HashMap<>();
        commons = new HashMap<>();
        for (Map.Entry<String, T> entry : newValues.entrySet()) {
            if (!oldValues.containsKey(entry.getKey())) {
                additions.put(entry.getKey(), entry.getValue());
            } else {
                commons.put(entry.getKey(), new AbstractMap.SimpleEntry<>(entry.getValue(),
                        oldValues.remove(entry.getKey())));
            }
        }

        for (Map.Entry<String, T> entry : oldValues.entrySet()) {
            removals.put(entry.getKey(), entry.getValue());
        }
    }
}
