/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.util.codegen;

import org.ballerinalang.model.types.BType;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfoPool;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code ConstantInfo} represents a constant in a compiled package.
 *
 * @since 0.985.0
 */
public class ConstantInfo implements AttributeInfoPool {

    private int nameCPIndex;
    private String name;
    private int finiteTypeCPIndex;
    private BType finiteType;
    private int valueTypeCPIndex;
    private BType valueType;

    private Map<AttributeInfo.Kind, AttributeInfo> attributeInfoMap = new HashMap<>();

    public ConstantInfo(int nameCPIndex, String name, int finiteTypeCPIndex, BType finiteType, int valueTypeCPIndex,
                        BType valueType) {
        this.nameCPIndex = nameCPIndex;
        this.name = name;
        this.finiteTypeCPIndex = finiteTypeCPIndex;
        this.finiteType = finiteType;
        this.valueTypeCPIndex = valueTypeCPIndex;
        this.valueType = valueType;
    }

    public int getNameCPIndex() {
        return nameCPIndex;
    }

    public int getFiniteTypeCPIndex() {
        return finiteTypeCPIndex;
    }

    public BType getFIniteType() {
        return finiteType;
    }

    public int getValueTypeCPIndex() {
        return valueTypeCPIndex;
    }

    public void setValueTypeCPIndex(int valueTypeCPIndex) {
        this.valueTypeCPIndex = valueTypeCPIndex;
    }

    public BType getValueType() {
        return valueType;
    }

    public void setValueType(BType valueType) {
        this.valueType = valueType;
    }

    public String getName() {
        return name;
    }

    public AttributeInfo getAttributeInfo(AttributeInfo.Kind attributeKind) {
        return attributeInfoMap.get(attributeKind);
    }

    public void addAttributeInfo(AttributeInfo.Kind attributeKind, AttributeInfo attributeInfo) {
        attributeInfoMap.put(attributeKind, attributeInfo);
    }

    public AttributeInfo[] getAttributeInfoEntries() {
        return attributeInfoMap.values().toArray(new AttributeInfo[0]);
    }
}
