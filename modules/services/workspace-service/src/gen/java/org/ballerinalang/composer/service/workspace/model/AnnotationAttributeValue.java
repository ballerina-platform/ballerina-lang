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

import java.util.ArrayList;
import java.util.List;

/**
 * Annotation attribute value.
 */
public class AnnotationAttributeValue {

    AnnotationAttachment annotationValue;
    private List<AnnotationAttributeValue> valueArray = new ArrayList<AnnotationAttributeValue>();

    public List<AnnotationAttributeValue> getValueArray() {
        return valueArray;
    }

    public void setValueArray(List<AnnotationAttributeValue> valueArray) {
        this.valueArray = valueArray;
    }

    public AnnotationAttachment getAnnotationValue() {
        return annotationValue;
    }

    public void setAnnotationValue(AnnotationAttachment annotationValue) {
        this.annotationValue = annotationValue;
    }

}
