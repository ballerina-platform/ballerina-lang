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

import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangMapLiteral;
import org.wso2.ballerinalang.programfile.ConstantValue;
import org.wso2.ballerinalang.programfile.KeyInfo;

import java.util.Map;

/**
 * {@code MapCPEntry} represents a constant record literal in the constant pool.
 *
 * @since 0.990.4
 */
@Deprecated
public class MapCPEntry implements ConstantPoolEntry {

    private BConstantSymbol constantSymbol;
    private Map<KeyInfo, ConstantValue> constantValueMap;

    public BLangMapLiteral literalValue;

    public MapCPEntry(BConstantSymbol constantSymbol, Map<KeyInfo, ConstantValue> constantValueMap) {
        this.constantSymbol = constantSymbol;
        this.constantValueMap = constantValueMap;
    }

    public Map<KeyInfo, ConstantValue> getConstantValueMap() {
        return constantValueMap;
    }

    public EntryType getEntryType() {
        return EntryType.CP_ENTRY_MAP;
    }

    public void setConstantSymbol(BConstantSymbol constantSymbol) {
        this.constantSymbol = constantSymbol;
    }

    public BConstantSymbol getConstantSymbol() {
        return constantSymbol;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MapCPEntry)) {
            return false;
        }

        MapCPEntry mapCPEntry = (MapCPEntry) o;

        if (mapCPEntry.constantSymbol != constantSymbol) {
            return false;
        }

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
