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
package org.ballerinalang.util.codegen;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code AnnotationAttributeInfo} contains metadata of Ballerina annotation attachments.
 *
 * @since 0.87
 */
public class AnnotationAttributeInfo implements AttributeInfo {

    private List<AnnotationAttachmentInfo> attachmentList = new ArrayList<>();

    public void addAnnotationAttachmentInfo(AnnotationAttachmentInfo attachmentInfo) {
        attachmentList.add(attachmentInfo);
    }

    public List<AnnotationAttachmentInfo> getAnnotationAttachmentInfo() {
        return attachmentList;
    }
}
