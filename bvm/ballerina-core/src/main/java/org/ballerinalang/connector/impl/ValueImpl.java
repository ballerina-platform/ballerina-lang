/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.connector.impl;

import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueType;

/**
 * Implementation of {@link org.ballerinalang.connector.api.Value}.
 *
 * @since 0.965.0
 */
public class ValueImpl implements Value {

    private Type type;
    private long intValue;
    private double floatValue;
    private String stringValue;
    private boolean booleanValue;
    private StructImpl structValue;
    private BValue value;

    private ValueImpl(Type type, BValue value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public Type getType() {
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
    public Struct getStructValue() {
        return structValue;
    }

    public BValue getVMValue() {
        return this.value;
    }

    public static ValueImpl createValue(BValue value) {
        if (value == null) {
            return null;
        }
        ValueImpl val = new ValueImpl(Type.getType(value), value);
        switch (val.type) {
            case INT:
                val.intValue = ((BValueType) value).intValue();
                break;
            case FLOAT:
                val.floatValue = ((BValueType) value).floatValue();
                break;
            case BOOLEAN:
                val.booleanValue = ((BValueType) value).booleanValue();
                break;
            case STRUCT:
                val.structValue = new StructImpl((BStruct) value);
                break;
            case NULL:
                break;
            default:
                val.stringValue = value.stringValue();
        }
        return val;
    }
}
