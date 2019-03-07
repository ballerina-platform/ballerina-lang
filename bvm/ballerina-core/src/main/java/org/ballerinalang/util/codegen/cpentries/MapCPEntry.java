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
package org.ballerinalang.util.codegen.cpentries;

import org.ballerinalang.bre.bvm.BVM;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.wso2.ballerinalang.programfile.ConstantValue;
import org.wso2.ballerinalang.programfile.KeyInfo;

import java.util.Map;

/**
 * {@code MapCPEntry} represents a constant record literal in the constant pool.
 *
 * @since 0.990.4
 */
public class MapCPEntry implements ConstantPoolEntry {

    private Map<KeyInfo, ConstantValue> constantValueMap;
    private BMap<String, BRefType> bMap;

    public MapCPEntry(Map<KeyInfo, ConstantValue> constantValueMap, BMap<String, BRefType> bMap) {
        this.constantValueMap = constantValueMap;
        this.bMap = bMap;

        // Freeze the BMap.
        this.bMap.attemptFreeze(new BVM.FreezeStatus(BVM.FreezeStatus.State.FROZEN));
    }

    public Map<KeyInfo, ConstantValue> getConstantValueMap() {
        return constantValueMap;
    }

    public BMap<String, BRefType> getBMap() {
        return bMap;
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

        if (constantValueMap.size() != mapCPEntry.constantValueMap.size()) {
            return false;
        }

        for (Map.Entry<KeyInfo, ConstantValue> entry : mapCPEntry.constantValueMap.entrySet()) {
            KeyInfo key = entry.getKey();
            if (!this.constantValueMap.containsKey(key)) {
                return false;
            }

            ConstantValue value1 = entry.getValue();
            ConstantValue value2 = this.constantValueMap.get(key);
            if (!value1.equals(value2)) {
                return false;
            }
        }

        for (Map.Entry<KeyInfo, ConstantValue> entry : this.constantValueMap.entrySet()) {
            KeyInfo key = entry.getKey();
            if (!mapCPEntry.constantValueMap.containsKey(key)) {
                return false;
            }

            ConstantValue value1 = entry.getValue();
            ConstantValue value2 = mapCPEntry.constantValueMap.get(key);
            if (!value1.equals(value2)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return constantValueMap.hashCode();
    }
}
