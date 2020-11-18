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
package org.ballerinalang.core.model;

import org.ballerinalang.core.model.expressions.variablerefs.SimpleVarRefExpr;
import org.ballerinalang.core.model.types.BType;
import org.ballerinalang.core.model.types.SimpleTypeName;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;

import java.util.Arrays;

/**
 * Holds the value of a ballerina annotation attribute.
 * 
 * @since 0.85
 */
public class AnnotationAttributeValue  implements Node {
    BValue bValue;
    SimpleVarRefExpr varRefExpr;
    AnnotationAttachment annotationValue;
    AnnotationAttributeValue[] valueArray;
    NodeLocation location;
    WhiteSpaceDescriptor whiteSpaceDescriptor;
    SimpleTypeName typeName;
    BType type;
    
    public AnnotationAttributeValue(BValue bValue, SimpleTypeName valueType, NodeLocation location,
                                    WhiteSpaceDescriptor whiteSpaceDescriptor) {
        this.bValue = bValue;
        this.typeName = valueType;
        this.location = location;
        this.whiteSpaceDescriptor = whiteSpaceDescriptor;
    }

    public AnnotationAttributeValue(SimpleVarRefExpr varRefExpr, NodeLocation location,
                                    WhiteSpaceDescriptor whiteSpaceDescriptor) {
        this.varRefExpr = varRefExpr;
        this.location = location;
        this.whiteSpaceDescriptor = whiteSpaceDescriptor;
    }
    
    public AnnotationAttributeValue(AnnotationAttachment annotationValue, SimpleTypeName valueType,
            NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor) {
        this.annotationValue = annotationValue;
        this.typeName = valueType;
        this.location = location;
        this.whiteSpaceDescriptor = whiteSpaceDescriptor;
    }
    
    public AnnotationAttributeValue(AnnotationAttributeValue[] valueArray, SimpleTypeName valueType, 
            NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor) {
        this.valueArray = valueArray;
        this.typeName = valueType;
        this.location = location;
        this.whiteSpaceDescriptor = whiteSpaceDescriptor;
    }
    
    public AnnotationAttachment getAnnotationValue() {
        return annotationValue;
    }
    
    public BValue getLiteralValue() {
        return bValue;
    }

    public SimpleVarRefExpr getVarRefExpr() {
        return varRefExpr;
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

    public void setWhiteSpaceDescriptor(WhiteSpaceDescriptor whiteSpaceDescriptor) {
        this.whiteSpaceDescriptor = whiteSpaceDescriptor;
    }

    @Override
    public WhiteSpaceDescriptor getWhiteSpaceDescriptor() {
        return whiteSpaceDescriptor;
    }

    public SimpleTypeName getTypeName() {
        return typeName;
    }

    public BType getType() {
        return type;
    }

    public void setType(BType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        if (bValue != null) {
            if (bValue instanceof BString) {
                return "\"" + bValue.stringValue() + "\"";
            }
            return bValue.stringValue();
        }
        
        if (annotationValue != null) {
            return annotationValue.toString();
        }
        
        if (valueArray != null) {
            return Arrays.toString(valueArray);
        }
        
        return null;
    }
}
