/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.model.values;

import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BStructType.StructField;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import java.util.HashMap;
import java.util.StringJoiner;

/**
 * The {@code BStruct} represents the value of a user defined struct in Ballerina.
 *
 * @since 1.0.0
 */
public final class BStruct implements BRefType, StructureType {

    private BStruct stackTrace;
    private HashMap<String, Object> nativeData = new HashMap<>();

    private long[] longFields;
    private double[] doubleFields;
    private String[] stringFields;
    private int[] intFields;
    private byte[][] byteFields;
    private BRefType[] refFields;

    private BStructType structType;

    /**
     * Creates a struct with a single memory block.
     */
    public BStruct(BStructType structType) {
        this.structType = structType;

        int[] fieldCount = this.structType.getFieldTypeCount();
        longFields = new long[fieldCount[0]];
        doubleFields = new double[fieldCount[1]];
        stringFields = new String[fieldCount[2]];
        Arrays.fill(stringFields, "");
        intFields = new int[fieldCount[3]];
        byteFields = new byte[fieldCount[4]][];
        refFields = new BRefType[fieldCount[5]];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BStruct value() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String stringValue() {
        int stringIndex = 0,
                intIndex = 0,
                longIndex = 0,
                doubleIndex = 0,
                byteIndex = 0,
                refValIndex = 0;

        StringJoiner sj = new StringJoiner(", ", "{", "}");
        for (StructField field : structType.getStructFields()) {
            String fieldName = field.getFieldName();
            Object fieldVal;
            BType fieldType = field.getFieldType();
            if (fieldType == BTypes.typeString) {
                fieldVal = "\"" + stringFields[stringIndex++] + "\"";
            } else if (fieldType == BTypes.typeInt) {
                fieldVal = longFields[longIndex++];
            } else if (fieldType == BTypes.typeFloat) {
                fieldVal = doubleFields[doubleIndex++];
            } else if (fieldType == BTypes.typeBoolean) {
                fieldVal = intFields[intIndex++] == 1;
            } else if (fieldType == BTypes.typeBlob) {
                byte[] blob = byteFields[byteIndex++];
                fieldVal = blob == null ? null : new String(blob, StandardCharsets.UTF_8);
            } else {
                BValue val = refFields[refValIndex++];
                fieldVal = val == null ? null : val.stringValue();
            }
            sj.add(fieldName + ":" + fieldVal);
        }
        return sj.toString();
    }

    @Override
    public BStructType getType() {
        return structType;
    }

    public BStruct getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(BStruct stackTrace) {
        this.stackTrace = stackTrace;
    }

    @Override
    public long getIntField(int index) {
        return longFields[index];
    }

    @Override
    public void setIntField(int index, long value) {
        longFields[index] = value;
    }

    @Override
    public double getFloatField(int index) {
        return doubleFields[index];
    }

    @Override
    public void setFloatField(int index, double value) {
        doubleFields[index] = value;
    }

    @Override
    public String getStringField(int index) {
        return stringFields[index];
    }

    @Override
    public void setStringField(int index, String value) {
        stringFields[index] = value;
    }

    @Override
    public int getBooleanField(int index) {
        return intFields[index];
    }

    @Override
    public byte[] getBlobField(int index) {
        return byteFields[index];
    }

    @Override
    public void setBlobField(int index, byte[] value) {
        byteFields[index] = value;
    }

    @Override
    public void setBooleanField(int index, int value) {
        intFields[index] = value;
    }

    @Override
    public BRefType getRefField(int index) {
        return refFields[index];
    }

    @Override
    public void setRefField(int index, BRefType value) {
        refFields[index] = value;
    }

    @Override
    public BValue copy() {
        BStruct bStruct = new BStruct(structType);
        bStruct.longFields = Arrays.copyOf(longFields, longFields.length);
        bStruct.doubleFields = Arrays.copyOf(doubleFields, doubleFields.length);
        bStruct.stringFields = Arrays.copyOf(stringFields, stringFields.length);
        bStruct.intFields = Arrays.copyOf(intFields, intFields.length);
        bStruct.byteFields = Arrays.copyOf(byteFields, byteFields.length);
        bStruct.refFields = Arrays.copyOf(refFields, refFields.length);
        return bStruct;
    }

    /**
     * Add natively accessible data to a struct.
     *
     * @param key  key to store data with
     * @param data data to be stored
     */
    public void addNativeData(String key, Object data) {
        nativeData.put(key, data);
    }

    /**
     * Get natively accessible data from struct.
     *
     * @param key key by which data was stored
     * @return data which was stored with given key or null if no value corresponding to key
     */
    public Object getNativeData(String key) {
        return nativeData.get(key);
    }
}
