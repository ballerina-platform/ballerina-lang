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
package org.ballerinalang.langserver.common.utils.completion;

import org.ballerinalang.langserver.commons.completion.AnnotationNodeKind;

import java.util.Queue;

/**
 * Meta information holder for annotation attachment.
 * 
 * Used when the annotations are suggested within the top level
 */
public class AnnotationAttachmentMetaInfo {

    private String attachmentName;
    
    private Queue<String> fieldQueue;
    
    private String packageAlias;
    
    private AnnotationNodeKind attachmentPoint;

    public AnnotationAttachmentMetaInfo(String attachmentName, Queue<String> fieldQueue, String packageAlias,
                                        AnnotationNodeKind attachmentPoint) {
        this.attachmentName = attachmentName;
        this.fieldQueue = fieldQueue;
        this.packageAlias = packageAlias;
        this.attachmentPoint = attachmentPoint;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public Queue<String> getFieldQueue() {
        return fieldQueue;
    }

    public String getPackageAlias() {
        return packageAlias;
    }

    public AnnotationNodeKind getAttachmentPoint() {
        return attachmentPoint;
    }
}
