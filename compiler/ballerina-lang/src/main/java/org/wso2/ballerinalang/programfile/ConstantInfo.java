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
package org.wso2.ballerinalang.programfile;

import org.wso2.ballerinalang.programfile.attributes.AttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.AttributeInfoPool;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents constant information.
 *
 * @since 0.985.0
 */
@Deprecated
public class ConstantInfo implements AttributeInfoPool {

    // Contains the details of the constant name.
    public KeyInfo key;

    // Contains the flags of the constant.
    public int flags = -1;

    // Contains the value type signature CP index of the constant.
    public int valueTypeSigCPIndex = -1;

    // Flag to indicate that the constant info contains a simple literal.
    public boolean isSimpleLiteral;

    // Contains the details of the constants.
    public ConstantValue constantValue;

    private Map<AttributeInfo.Kind, AttributeInfo> attributeInfoMap = new HashMap<>();

    public ConstantInfo(String name, int nameCPIndex) {
        this.key = new KeyInfo(name, nameCPIndex);
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
