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

package org.ballerinalang.composer.service.workspace.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ballerinalang.model.AttachmentPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Annotation attachment.
 */
public class AnnotationAttachment {

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("annotations")
    private List<Annotation> annotations = new ArrayList<Annotation>();

    @JsonProperty("attributeNameValPairs")
    private Map<String, AnnotationAttributeValue> attributeNameValPairs = new HashMap<>();

    @JsonProperty("attachedPoint")
    AttachmentPoint attachedPoint = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    public Map<String, AnnotationAttributeValue> getAttributeNameValPairs() {
        return attributeNameValPairs;
    }

    public void setAttributeNameValPairs(Map<String, AnnotationAttributeValue> attributeNameValPairs) {
        this.attributeNameValPairs = attributeNameValPairs;
    }

    public AttachmentPoint getAttachedPoint() {
        return attachedPoint;
    }

    public void setAttachedPoint(AttachmentPoint attachedPoint) {
        this.attachedPoint = attachedPoint;
    }
}
