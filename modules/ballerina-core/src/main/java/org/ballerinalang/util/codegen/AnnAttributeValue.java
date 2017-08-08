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
public class AnnAttributeValue {
    private int typeDescCPIndex;
    private String typeDesc;

    private int valueCPIndex = -1;
    private long intValue;
    private double floatValue;
    private String stringValue;
    private boolean booleanValue;
    private boolean runTimeValue = false;
    private int memoryOffset = -1;

    private AnnAttachmentInfo annotationAttachmentValue;

    private AnnAttributeValue[] attributeValueArray;

    public AnnAttributeValue(int typeDescCPIndex, String typeDesc) {
        this.typeDescCPIndex = typeDescCPIndex;
        this.typeDesc = typeDesc;
    }

    public AnnAttributeValue(int typeDescCPIndex, String typeDesc, AnnAttachmentInfo annotationAttachmentValue) {
        this.typeDescCPIndex = typeDescCPIndex;
        this.typeDesc = typeDesc;
        this.annotationAttachmentValue = annotationAttachmentValue;
    }

    public AnnAttributeValue(int typeDescCPIndex, String typeDesc, AnnAttributeValue[] attributeValueArray) {
        this.typeDescCPIndex = typeDescCPIndex;
        this.typeDesc = typeDesc;
        this.attributeValueArray = attributeValueArray;
    }

    public int getTypeDescCPIndex() {
        return typeDescCPIndex;
    }

    public String getTypeDesc() {
        return typeDesc;
    }

    public int getValueCPIndex() {
        return valueCPIndex;
    }

    public void setValueCPIndex(int valueCPIndex) {
        this.valueCPIndex = valueCPIndex;
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

    public boolean getBooleanValue() {
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

    public AnnAttachmentInfo getAnnotationAttachmentValue() {
        return annotationAttachmentValue;
    }

    public AnnAttributeValue[] getAttributeValueArray() {
        return attributeValueArray;
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
