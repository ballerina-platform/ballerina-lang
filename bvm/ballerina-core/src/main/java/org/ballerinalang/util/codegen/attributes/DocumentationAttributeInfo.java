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
package org.ballerinalang.util.codegen.attributes;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code DocumentationAttributeInfo} holds documentation attachment data.
 *
 * @since 0.970.0
 */
public class DocumentationAttributeInfo implements AttributeInfo {

    // Index to a UTF8CPEntry
    public final int attributeNameIndex;

    public final int descriptionCPIndex;
    public final String description;

    public final List<DocumentationAttributeInfo.ParameterDocumentInfo> paramDocInfoList;

    public DocumentationAttributeInfo(int attributeNameIndex,
                                      int descriptionCPIndex,
                                      String description) {
        this.attributeNameIndex = attributeNameIndex;
        this.descriptionCPIndex = descriptionCPIndex;
        this.description = description;
        this.paramDocInfoList = new ArrayList<>();
    }

    @Override
    public AttributeInfo.Kind getKind() {
        return AttributeInfo.Kind.DOCUMENT_ATTACHMENT_ATTRIBUTE;
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
        public final int nameCPIndex;
        public final String name;
        public final int typeSigCPIndex;
        public final String typeSig;
        public final int paramKindCPIndex;
        public final String paramKindValue;
        public final int descriptionCPIndex;
        public final String description;

        public ParameterDocumentInfo(int nameCPIndex,
                                     String name,
                                     int typeSigCPIndex,
                                     String typeSig,
                                     int paramKindCPIndex,
                                     String paramKindValue,
                                     int descriptionCPIndex,
                                     String description) {
            this.nameCPIndex = nameCPIndex;
            this.name = name;
            this.typeSigCPIndex = typeSigCPIndex;
            this.typeSig = typeSig;
            this.paramKindCPIndex = paramKindCPIndex;
            this.paramKindValue = paramKindValue;
            this.descriptionCPIndex = descriptionCPIndex;
            this.description = description;
        }
    }
}
