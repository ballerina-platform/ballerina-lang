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


import org.ballerinalang.model.types.BFiniteType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo.Kind;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code PackageInfo} Represent serializable unit of finite type Definition item.
 *
 * @since 0.0971.0
 */
public class FiniteTypeInfo implements TypeInfo {
    private BFiniteType finiteType;


    private Map<Kind, AttributeInfo> attributeInfoMap = new HashMap<>();

    public FiniteTypeInfo() {
    }

    public BType getType() {
        return finiteType;
    }

    public void setType(BFiniteType finiteType) {
        this.finiteType = finiteType;
    }

    @Override
    public AttributeInfo getAttributeInfo(AttributeInfo.Kind attributeKind) {
        return attributeInfoMap.get(attributeKind);
    }

    @Override
    public void addAttributeInfo(AttributeInfo.Kind attributeKind, AttributeInfo attributeInfo) {
        attributeInfoMap.put(attributeKind, attributeInfo);
    }

    @Override
    public AttributeInfo[] getAttributeInfoEntries() {
        return attributeInfoMap.values().toArray(new AttributeInfo[0]);
    }
}
