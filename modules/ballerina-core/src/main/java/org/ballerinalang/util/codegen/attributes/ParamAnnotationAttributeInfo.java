/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.util.codegen.ParamAnnAttachmentInfo;

/**
 * {@code ParamAnnotationAttributeInfo} contains metadata of Ballerina annotations attached to
 * function/resource/action/connector nodes.
 *
 * @since 0.87
 */
public class ParamAnnotationAttributeInfo implements AttributeInfo {

    // Index to a UTF8CPEntry
    private int attributeNameIndex;

    private ParamAnnAttachmentInfo[] attachmentInfoArray;

    public ParamAnnotationAttributeInfo(int attributeNameIndex, int paramCount) {
        this.attributeNameIndex = attributeNameIndex;
        this.attachmentInfoArray = new ParamAnnAttachmentInfo[paramCount];
    }

    public void addParamAttachmentInfo(int paramIndex, ParamAnnAttachmentInfo attachmentInfo) {
        attachmentInfoArray[paramIndex] = attachmentInfo;
    }

    @Override
    public Kind getKind() {
        return Kind.PARAMETER_ANNOTATIONS_ATTRIBUTE;
    }

    @Override
    public int getAttributeNameIndex() {
        return attributeNameIndex;
    }
}
