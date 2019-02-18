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
package org.wso2.ballerinalang.programfile;

import org.wso2.ballerinalang.programfile.attributes.AttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.AttributeInfoPool;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 0.990.4
 */
public class ConstantInfo_new implements AttributeInfoPool {

    public String name;
    public int nameCPIndex;

    public boolean isSimpleLiteral;

    public int globalMemIndex = -1;

    public ConstantValue constantValue;
    public Map<String, ConstantValue> constantValueMap = new HashMap<>();

    private Map<AttributeInfo.Kind, AttributeInfo> attributeInfoMap = new HashMap<>();

    public ConstantInfo_new(String name, int nameCPIndex) {
        this.name = name;
        this.nameCPIndex = nameCPIndex;
    }

    //    public void addConstantValue(String key, ConstantValue constantValue) {
    //        constantValueMap.put(key, constantValue);
    //    }

    //    public ConstantValue getConstantValue(String key) {
    //        return constantValueMap.get(key);
    //    }

    //    public String getName() {
    //        return name;
    //    }

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
