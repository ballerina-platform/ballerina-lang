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
package org.ballerinalang.util.codegen;

import org.ballerinalang.model.types.BErrorType;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code ErrorTypeInfo} contains metadata of a Ballerina error entry in the program file.
 *
 * @since 0.991.0
 */
public class ErrorTypeInfo implements TypeInfo {

    private BErrorType errorType;

    private String reasonFieldTypeSignature;
    private String detailFieldTypeSignature;

    private Map<AttributeInfo.Kind, AttributeInfo> attributeInfoMap = new HashMap<>();

    public BErrorType getType() {
        return errorType;
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

    public void setType(BErrorType structType) {
        this.errorType = structType;
    }

    public String getReasonFieldTypeSignature() {
        return reasonFieldTypeSignature;
    }

    public void setReasonFieldTypeSignature(String reasonFieldTypeSignature) {
        this.reasonFieldTypeSignature = reasonFieldTypeSignature;
    }

    public String getDetailFieldTypeSignature() {
        return detailFieldTypeSignature;
    }

    public void setDetailFieldTypeSignature(String detailFieldTypeSignature) {
        this.detailFieldTypeSignature = detailFieldTypeSignature;
    }
}
