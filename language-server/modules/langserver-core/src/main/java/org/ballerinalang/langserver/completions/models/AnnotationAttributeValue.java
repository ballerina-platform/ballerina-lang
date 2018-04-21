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


import java.util.ArrayList;
import java.util.List;

/**
 * Represents the value of a annotation's attribute pair.
 */
public class AnnotationAttributeValue {

    private String bValue;

    private String fileName = null;

    private AnnotationAttachment annotationValue;

    private List<AnnotationAttributeValue> valueArray;

    public String getBValue() {
        return bValue;
    }

    public void setBValue(String bValue) {
        this.bValue = bValue;
    }

    public AnnotationAttachment getAnnotationValue() {
        return annotationValue;
    }

    public void setAnnotationValue(AnnotationAttachment annotationValue) {
        this.annotationValue = annotationValue;
    }

    public List<AnnotationAttributeValue> getValueArray() {
        return valueArray;
    }

    public void setValueArray(List<AnnotationAttributeValue> valueArray) {
        this.valueArray = valueArray;
    }

    /**
     * Adds a value to array type value.
     * @param annotationAttributeValue The attribute to be added.
     */
    public void addToValueArray(AnnotationAttributeValue annotationAttributeValue) {
        if (null == this.valueArray) {
            this.valueArray = new ArrayList<>();
        }

        this.valueArray.add(annotationAttributeValue);
    }

    /**
     * Converts a {@link org.ballerinalang.model.AnnotationAttributeValue} to {@link AnnotationAttributeValue}.
     * @param annotationAttributeValue The model to be converted.
     * @return Converted model.
     */
    public static AnnotationAttributeValue convertToPackageModel(
                                            org.ballerinalang.model.AnnotationAttributeValue annotationAttributeValue) {
        if (null != annotationAttributeValue) {
            AnnotationAttributeValue tempAnnotationAttributeValue = new AnnotationAttributeValue();
            return tempAnnotationAttributeValue;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "AnnotationAttributeValue{" + "bValue='" + bValue + '\''
                + ", annotationValue=" + annotationValue + "," + " valueArray=" + valueArray + '}';
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
