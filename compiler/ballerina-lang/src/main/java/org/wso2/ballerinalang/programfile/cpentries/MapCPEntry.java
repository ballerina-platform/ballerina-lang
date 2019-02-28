/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.programfile.cpentries;

import org.wso2.ballerinalang.programfile.ConstantValue;
import org.wso2.ballerinalang.programfile.KeyInfo;

import java.util.Collections;
import java.util.Map;

/**
 * {@code MapCPEntry} represents a constant record literal in the constant pool.
 *
 * @since 0.990.4
 */
public class MapCPEntry implements ConstantPoolEntry {

    private Map<KeyInfo, ConstantValue> value;

    public MapCPEntry(Map<KeyInfo, ConstantValue> value) {
        this.value = value;
    }

    public Map<KeyInfo, ConstantValue> getValue() {
        return value;
    }

    public EntryType getEntryType() {
        return EntryType.CP_ENTRY_MAP;
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof MapCPEntry)) {
            return false;
        }

        MapCPEntry mapCPEntry = (MapCPEntry) o;

        if (value.size() != mapCPEntry.value.size()) {
            return false;
        }

        for (Map.Entry<KeyInfo, ConstantValue> entry : mapCPEntry.value.entrySet()) {
            KeyInfo key = entry.getKey();
            if (!this.value.containsKey(key)) {
                return false;
            }

            ConstantValue value1 = entry.getValue();
            ConstantValue value2 = this.value.get(key);
            if (!value1.equals(value2)) {
                return false;
            }
        }

        for (Map.Entry<KeyInfo, ConstantValue> entry : this.value.entrySet()) {
            KeyInfo key = entry.getKey();
            if (!mapCPEntry.value.containsKey(key)) {
                return false;
            }

            ConstantValue value1 = entry.getValue();
            ConstantValue value2 = mapCPEntry.value.get(key);
            if (!value1.equals(value2)) {
                return false;
            }
        }


        return true;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
