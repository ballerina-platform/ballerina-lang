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
package org.wso2.ballerinalang.programfile.attributes;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code DocumentationAttributeInfo} holds documentation attachment data.
 *
 * @since 0.970.0
 */
public class DocumentationAttributeInfo implements AttributeInfo {

    // Index to a UTF8CPEntry
    public int attributeNameIndex;
    public int descriptionCPIndex;

    public List<ParameterDocumentInfo> paramDocInfoList;

    public DocumentationAttributeInfo(int attributeNameIndex, int descriptionCPIndex) {
        this.attributeNameIndex = attributeNameIndex;
        this.descriptionCPIndex = descriptionCPIndex;
        this.paramDocInfoList = new ArrayList<>();
    }

    @Override
    public Kind getKind() {
        return Kind.DOCUMENT_ATTACHMENT_ATTRIBUTE;
    }

    @Override
    public int getAttributeNameIndex() {
        return attributeNameIndex;
    }

    /**
     * {@code ParameterDocumentInfo} holds documentation of a single parameter/field.
     *
     * @since 0.970.0
     */
    public static class ParameterDocumentInfo {
        public int nameCPIndex;
        public int typeSigCPIndex;
        public int paramKindCPIndex;
        public int descriptionCPIndex;

        public ParameterDocumentInfo(int nameCPIndex,
                                     int typeSigCPIndex,
                                     int paramKindCPIndex,
                                     int descriptionCPIndex) {
            this.nameCPIndex = nameCPIndex;
            this.typeSigCPIndex = typeSigCPIndex;
            this.paramKindCPIndex = paramKindCPIndex;
            this.descriptionCPIndex = descriptionCPIndex;
        }
    }
}
