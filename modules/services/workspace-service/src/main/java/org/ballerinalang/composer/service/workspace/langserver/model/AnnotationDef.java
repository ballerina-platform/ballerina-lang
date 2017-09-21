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

package org.ballerinalang.composer.service.workspace.langserver.model;

import org.ballerinalang.model.AnnotationAttachmentPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an Annotation definition that is used for package API.
 */

public class AnnotationDef {
    private String packagePath = null;

    private String name = null;

    private List<String> attachmentPoints = new ArrayList<String>();

    private List<AnnotationAttributeDef> annotationAttributeDefs = new ArrayList<>();

    private List<AnnotationAttachment> annotationAttachments = new ArrayList<>();

    /**
     * Converts a {@link org.ballerinalang.model.AnnotationDef} to {@link AnnotationDef}.
     * @param annotationDef The model to be converted.
     * @return Converted model.
     */
    public static AnnotationDef convertToPackageModel(org.ballerinalang.model.AnnotationDef annotationDef) {
        AnnotationDef tempAnnotation = new AnnotationDef();
        tempAnnotation.setPackagePath(annotationDef.getPkgPath());
        tempAnnotation.setName(annotationDef.getName());
        ArrayList<String> attachmentPoints = new ArrayList<String>();
        AnnotationAttachmentPoint[] attachmentPointsList = annotationDef.getAttachmentPoints();
        for (AnnotationAttachmentPoint annotationAttachmentPoint : attachmentPointsList) {
            attachmentPoints.add(annotationAttachmentPoint.getAttachmentPoint().getValue());
        }
        tempAnnotation.setAttachmentPoints(attachmentPoints);
        //Annotation Definitions
        for (org.ballerinalang.model.AnnotationAttributeDef annotationAttributeDef : annotationDef.getAttributeDefs()) {
            AnnotationAttributeDef tempAnnotationAttributeDef = AnnotationAttributeDef.convertToPackageModel(
                                                                                                annotationAttributeDef);
            tempAnnotation.getAnnotationAttributeDefs().add(tempAnnotationAttributeDef);
        }
        //Annotation Attachments
        for (org.ballerinalang.model.AnnotationAttachment annotationAttachment
                : annotationDef.getAnnotations()) {
            AnnotationAttachment tempAnnotationAttachment = AnnotationAttachment.convertToPackageModel(
                    annotationAttachment);
            tempAnnotation.getAnnotationAttachments().add(tempAnnotationAttachment);
        }
        return tempAnnotation;
    }
    
    public String getPackagePath() {
        return packagePath;
    }
    
    public void setPackagePath(String packagePath) {
        this.packagePath = packagePath;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<String> getAttachmentPoints() {
        return attachmentPoints;
    }
    
    public void setAttachmentPoints(List<String> attachmentPoints) {
        this.attachmentPoints = attachmentPoints;
    }
    
    public List<AnnotationAttributeDef> getAnnotationAttributeDefs() {
        return annotationAttributeDefs;
    }
    
    public void setAnnotationAttributeDefs(List<AnnotationAttributeDef> annotationAttributeDefs) {
        this.annotationAttributeDefs = annotationAttributeDefs;
    }

    public List<AnnotationAttachment> getAnnotationAttachments() {
        return annotationAttachments;
    }

    public void setAnnotationAttachments(List<AnnotationAttachment> annotationAttachments) {
        this.annotationAttachments = annotationAttachments;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Annotation{" + "packagePath='" + packagePath + '\'' + ", name='" + name + '\'' + ", " +
               "attachmentPoints=" + attachmentPoints + ", annotationAttributeDefs=" + annotationAttributeDefs + '}';
    }
}

