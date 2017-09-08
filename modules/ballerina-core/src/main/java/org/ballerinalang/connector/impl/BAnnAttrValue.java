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
package org.ballerinalang.connector.impl;

import org.ballerinalang.connector.api.AnnAttrValue;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.AnnotationValueType;

/**
 * {@code BAnnAttrValue} This is the implementation for the {@code AnnAttrValue} API.
 *
 * @since 0.94
 */
public class BAnnAttrValue implements AnnAttrValue {
    private AnnotationValueType type;

    private long intValue;
    private double floatValue;
    private String stringValue;
    private boolean booleanValue;
    private Annotation annotation;
    private AnnAttrValue[] annotationValueArray;

    public BAnnAttrValue(AnnotationValueType type) {
        this.type = type;
    }

    @Override
    public AnnotationValueType getType() {
        return type;
    }

    @Override
    public long getIntValue() {
        return intValue;
    }

    @Override
    public double getFloatValue() {
        return floatValue;
    }

    @Override
    public String getStringValue() {
        return stringValue;
    }

    @Override
    public boolean getBooleanValue() {
        return booleanValue;
    }

    @Override
    public Annotation getAnnotation() {
        return annotation;
    }

    @Override
    public AnnAttrValue[] getAnnAttrValueArray() {
        return annotationValueArray;
    }

    public void setIntValue(long intValue) {
        this.intValue = intValue;
    }

    public void setFloatValue(double floatValue) {
        this.floatValue = floatValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public void setAnnotationValueArray(AnnAttrValue[] annotationValueArray) {
        this.annotationValueArray = annotationValueArray;
    }
}
