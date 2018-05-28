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

/**
 * {@code AnnotationAttributeValue} contains the default value of a Ballerina struct field.
 *
 * @since 0.92
 */
public class DefaultValue {
    private int typeDescCPIndex;
    private String typeDesc;

    private int valueCPIndex = -1;
    private long intValue;
    private double floatValue;
    private String stringValue;
    private boolean booleanValue;
    private byte[] blobValue;

    public DefaultValue(int typeDescCPIndex, String typeDesc) {
        this.typeDescCPIndex = typeDescCPIndex;
        this.typeDesc = typeDesc;
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

    public byte[] getBlobValue() {
        return blobValue;
    }

    public void setBlobValue(byte[] blobValue) {
        this.blobValue = blobValue;
    }
}
