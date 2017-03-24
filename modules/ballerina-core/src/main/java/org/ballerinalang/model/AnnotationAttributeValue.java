/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.model;

import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.model.values.BValue;

/**
 * Holds the value of a ballerina annotation attribute.
 * 
 * @since 0.85
 */
public class AnnotationAttributeValue  implements Node {
    BValue bValue;
    AnnotationAttachment annotationValue;
    AnnotationAttributeValue[] valueArray;
    NodeLocation location;
    SimpleTypeName type;
    
    public AnnotationAttributeValue(BValue bValue, SimpleTypeName valueType, NodeLocation location) {
        this.bValue = bValue;
        this.type = valueType;
        this.location = location;
    }
    
    public AnnotationAttributeValue(AnnotationAttachment annotationValue, SimpleTypeName valueType,
            NodeLocation location) {
        this.annotationValue = annotationValue;
        this.type = valueType;
        this.location = location;
    }
    
    public AnnotationAttributeValue(AnnotationAttributeValue[] valueArray, SimpleTypeName valueType, 
            NodeLocation location) {
        this.valueArray = valueArray;
        this.type = valueType;
        this.location = location;
    }
    
    public AnnotationAttachment getAnnotationValue() {
        return annotationValue;
    }
    
    public BValue getLiteralValue() {
        return bValue;
    }
    
    public AnnotationAttributeValue[] getValueArray() {
        return valueArray;
    }
    
    public void setNodeLocation(NodeLocation location) {
        this.location = location;
    }
    
    public NodeLocation getNodeLocation() {
        return location;
    }
    
    public SimpleTypeName getType() {
        return type;
    }
    
    @Override
    public String toString() {
        if (bValue != null) {
            return bValue.stringValue();
        }
        
        if (annotationValue != null) {
            return annotationValue.toString();
        }
        
        return null;
    }

    @Override
    public void accept(NodeVisitor visitor) {
    }
}
