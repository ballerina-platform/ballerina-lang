/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.programfile;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 0.990.4
 */
public class ConstantValue {

    public int finiteTypeSigCPIndex = -1;
    public int valueTypeSigCPIndex = -1;
    public int flags = -1;

    public boolean isSimpleLiteral;

    public int literalValueTypeTag = -1;

    //    // Todo - add other values
    public boolean booleanValue;
    //    private long intValue;
    //    private byte byteValue;
    //    private double floatValue;
    //    private String stringValue;

    public int valueCPEntry = -1;

    public Map<String, ConstantValue> constantValueMap = new HashMap<>();
    public Map<String, Integer> constantValueMapKeyCPIndex = new HashMap<>();

    // Todo - Add array support
    //    private ConstantValue[] constantValueArray;


    // Todo - Add a constructor

    //    public boolean getBooleanValue() {
    //        return booleanValue;
    //    }
    //
    //    public void setBooleanValue(boolean booleanValue) {
    //        this.booleanValue = booleanValue;
    //    }
    //
    //
    //    public byte getByteValue() {
    //        return byteValue;
    //    }
    //
    //    public void setByteValue(byte byteValue) {
    //        this.byteValue = byteValue;
    //    }
    //
    //
    //
    //
    //    public long getIntValue() {
    //        return intValue;
    //    }
    //
    //    public void setIntValue(long intValue) {
    //        this.intValue = intValue;
    //    }
    //
    //
    //
    //    public double getFloatValue() {
    //        return floatValue;
    //    }
    //
    //    public void setFloatValue(double floatValue) {
    //        this.floatValue = floatValue;
    //    }
    //
    //
    //
    //    public String getStringValue() {
    //        return stringValue;
    //    }
    //
    //    public void setStringValue(String stringValue) {
    //        this.stringValue = stringValue;
    //    }


}
