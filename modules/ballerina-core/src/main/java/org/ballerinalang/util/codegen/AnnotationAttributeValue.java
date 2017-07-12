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

import org.ballerinalang.model.values.StructureType;

/**
 * {@code AnnotationAttributeValue} contains the value of a Ballerina annotation attribute.
 *
 * @since 0.87
 */
public class AnnotationAttributeValue {
    //    private BType attributeType;
    private int typeTag;

    private long intValue;
    private double floatValue;
    private String stringValue;
    private boolean booleanValue;
    private boolean runTimeValue = false;
    private int memoryOffset = -1;

    private AnnotationAttachmentInfo annotationAttachmentValue;

    private AnnotationAttributeValue[] attributeValueArray;

    public int getTypeTag() {
        return typeTag;
    }

    public void setTypeTag(int typeTag) {
        this.typeTag = typeTag;
    }

    public long getIntValue() {
        return intValue;
    }

    public void setIntValue(long intValue) {
        this.intValue = intValue;
    }

    public double getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(double floatValue) {
        this.floatValue = floatValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public boolean isBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public boolean isRunTimeValue() {
        return runTimeValue;
    }

    public void setRunTimeValue(boolean runTimeValue) {
        this.runTimeValue = runTimeValue;
    }

    public int getMemoryOffset() {
        return memoryOffset;
    }

    public void setMemoryOffset(int memoryOffset) {
        this.memoryOffset = memoryOffset;
    }

    public AnnotationAttachmentInfo getAnnotationAttachmentValue() {
        return annotationAttachmentValue;
    }

    public void setAnnotationAttachmentValue(AnnotationAttachmentInfo annotationAttachmentValue) {
        this.annotationAttachmentValue = annotationAttachmentValue;
    }

    public AnnotationAttributeValue[] getAttributeValueArray() {
        return attributeValueArray;
    }

    public void setAttributeValueArray(AnnotationAttributeValue[] valueArray) {
        this.attributeValueArray = valueArray;
    }

    public void loadDynamicAttributeValues(StructureType globalMemoryBlock) {
        if (runTimeValue) {
            switch (typeTag) {
                case 1: //INT_TAG
                    intValue = globalMemoryBlock.getIntField(memoryOffset);
                    break;
                case 2: //FLOAT_TAG
                    floatValue = globalMemoryBlock.getFloatField(memoryOffset);
                    break;
                case 3: //STRING_TAG
                    stringValue = globalMemoryBlock.getStringField(memoryOffset);
                    break;
                case 4: //BOOLEAN_TAG
                    booleanValue = globalMemoryBlock.getBooleanField(memoryOffset) == 1 ? true : false;
                    break;
            }
            runTimeValue = false;
        } else if (annotationAttachmentValue != null) {
            annotationAttachmentValue.loadDynamicAttributes(globalMemoryBlock);
        } else if (attributeValueArray != null) {
            for (AnnotationAttributeValue attributeValue : attributeValueArray) {
                attributeValue.loadDynamicAttributeValues(globalMemoryBlock);
            }
        }

    }
}
