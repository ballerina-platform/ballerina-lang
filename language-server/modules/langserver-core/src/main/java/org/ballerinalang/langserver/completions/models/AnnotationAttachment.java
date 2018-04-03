/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.langserver.completions.models;

import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachmentPoint;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an annotation attached to certain objects({@link Action}, {@link Connector}).
 */
public class AnnotationAttachment {
    private String name;

    private String packageName;

    private String packagePath;

    private String attachedPoint;

    private Map<String, AnnotationAttributeValue> attributeNameValPairs = new HashMap();

    private String fileName = null;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public String getPackagePath() {
        return packagePath;
    }
    
    public void setPackagePath(String packagePath) {
        this.packagePath = packagePath;
    }
    
    public String getAttachedPoint() {
        return attachedPoint;
    }
    
    public void setAttachedPoint(String attachedPoint) {
        this.attachedPoint = attachedPoint;
    }
    
    public Map<String, AnnotationAttributeValue> getAttributeNameValPairs() {
        return attributeNameValPairs;
    }
    
    public void setAttributeNameValPairs(Map<String, AnnotationAttributeValue> attributeNameValPairs) {
        this.attributeNameValPairs = attributeNameValPairs;
    }
    
    /**
     * Converts a {@link org.ballerinalang.model.AnnotationAttachment} to {@link AnnotationAttachment}.
     * @param annotationAttachment The model to be converted.
     * @return Converted model.
     */
    public static AnnotationAttachment convertToPackageModel(BLangAnnotationAttachmentPoint annotationAttachment) {
        if (null != annotationAttachment) {
            AnnotationAttachment tempAnnotationAttachment = new AnnotationAttachment();
//            tempAnnotationAttachment.setName(annotationAttachment.getName());
//            tempAnnotationAttachment.setPackageName(annotationAttachment.getPkgName());
//            tempAnnotationAttachment.setPackagePath(annotationAttachment.getPkgPath());
            BLangAnnotationAttachmentPoint.AttachmentPoint attachmentPoint = annotationAttachment.getAttachmentPoint();
            if (attachmentPoint != null) {
                tempAnnotationAttachment.setAttachedPoint(attachmentPoint.getValue());
            }
//
//            if (null != annotationAttachment.getAttachedPoint()) {
//                AnnotationAttachmentPoint attachmentPoint = annotationAttachment.getAttachedPoint();
//                tempAnnotationAttachment.setAttachedPoint(attachmentPoint.getAttachmentPoint().getValue());
//            }
//
//            for (Map.Entry<String, org.ballerinalang.model.AnnotationAttributeValue> annotationAttributeValueEntry :
//                    annotationAttachment.getAttributeNameValuePairs().entrySet()) {
//                tempAnnotationAttachment.getAttributeNameValPairs().put(annotationAttributeValueEntry.getKey(),
//                            AnnotationAttributeValue.convertToPackageModel(annotationAttributeValueEntry.getValue()));
//            }
        
            return tempAnnotationAttachment;
        } else {
            return null;
        }
    }

    public static AnnotationAttachment convertToPackageModel(
            BLangAnnotationAttachment annotationAttachment) {
        if (null != annotationAttachment) {
            AnnotationAttachment tempAnnotationAttachment = new AnnotationAttachment();
            return tempAnnotationAttachment;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "AnnotationAttachment{" + "name='" + name + '\'' + ", packagePath='" + packagePath + '\'' + ", " +
               "attachedPoint='" + attachedPoint + '\'' + '}';
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
