/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.debugger;

import org.wso2.siddhi.core.util.snapshot.Snapshotable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A POJO class that represents the current state of the query. This contains all the events currently available in the
 * query which includes the windows and other {@link org.wso2.siddhi.core.util.snapshot.Snapshotable} entities of the
 * query.
 */
public class QueryState {
    /**
     * Event data with anonymous names.
     * Technically it contains the unnamed data returned by the {@link Snapshotable#currentState()} method.
     */
    private HashMap<String, Object[]> unknownFields = new HashMap<String, Object[]>();

    /**
     * Event data with names.
     * Technically it contains the named data returned by the {@link Snapshotable#currentState()} method.
     */
    private HashMap<String, Map<String, Object>> knownFields = new HashMap<String, Map<String, Object>>();

    public HashMap<String, Map<String, Object>> getKnownFields() {
        return knownFields;
    }

    public HashMap<String, Object[]> getUnknownFields() {
        return unknownFields;
    }

    /**
     * Add the given known fields to the existing map.
     *
     * @param currentField fields with name
     */
    public void addKnownFields(Map.Entry<String, Map<String, Object>> currentField) {
        this.knownFields.put(currentField.getKey(), currentField.getValue());
    }

    /**
     * Add the given unknown fields to the existing map.
     *
     * @param currentField fields without name
     */
    public void addUnknownFields(Map.Entry<String, Object[]> currentField) {
        this.unknownFields.put(currentField.getKey(), currentField.getValue());
    }

    public String toString() {
        StringBuilder stateBuilder = new StringBuilder();
        if (unknownFields.size() != 0) {
            for (Map.Entry<String, Object[]> entry : unknownFields.entrySet()) {
                stateBuilder.append(entry.getKey())
                        .append('\n')
                        .append("Unknown Field: ")
                        .append(Arrays.deepToString(entry.getValue()))
                        .append('\n');
            }
        }
        if (knownFields.size() != 0) {
            for (Map.Entry<String, Map<String, Object>> entry : knownFields.entrySet()) {
                Map<String, Object> subMap = entry.getValue();
                stateBuilder.append(entry.getKey()).append('\n');
                for (Map.Entry<String, Object> subEntry : subMap.entrySet()) {
                    stateBuilder.append(subEntry.getKey() + ": " + subEntry.getValue()).append('\n');
                }
            }
        }
        return stateBuilder.toString().trim();
    }
}
