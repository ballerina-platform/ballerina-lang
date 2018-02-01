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

import org.ballerinalang.bre.bvm.VarLock;
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

    private HashMap<String, Object> nativeData = new HashMap<>();

    private long[] longFields;
    private VarLock[] longLocks;
    private double[] doubleFields;
    private VarLock[] doubleLocks;
    private String[] stringFields;
    private VarLock[] stringLocks;
    private int[] intFields;
    private VarLock[] intLocks;
    private byte[][] byteFields;
    private VarLock[] byteLocks;
    private BRefType[] refFields;
    private VarLock[] refLocks;

    private BStructType structType;

    /**
     * Creates a struct with a single memory block.
     *
     * @param structType type of the struct
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
    public void lockIntField(int index) {
        if (longLocks == null || longLocks[index] == null) {
            initIntLocks(index);
        }
        longLocks[index].lock();
    }

    @Override
    public void unlockIntField(int index) {
        longLocks[index].unlock();
    }

    @Override
    public void lockFloatField(int index) {
        if (doubleLocks == null || doubleLocks[index] == null) {
            initFloatLocks(index);
        }
        doubleLocks[index].lock();
    }

    @Override
    public void unlockFloatField(int index) {
        doubleLocks[index].unlock();
    }

    @Override
    public void lockStringField(int index) {
        if (stringLocks == null || stringLocks[index] == null) {
            initStringLocks(index);
        }
        stringLocks[index].lock();
    }

    @Override
    public void unlockStringField(int index) {
        stringLocks[index].unlock();
    }

    @Override
    public void lockBooleanField(int index) {
        if (intLocks == null || intLocks[index] == null) {
            initBooleanLocks(index);
        }
        intLocks[index].lock();
    }

    @Override
    public void unlockBooleanField(int index) {
        intLocks[index].unlock();
    }

    @Override
    public void lockBlobField(int index) {
        if (byteLocks == null || byteLocks[index] == null) {
            initBlobLocks(index);
        }
        byteLocks[index].lock();
    }

    @Override
    public void unlockBlobField(int index) {
        byteLocks[index].unlock();
    }

    @Override
    public void lockRefField(int index) {
        if (refLocks == null || refLocks[index] == null) {
            initRefLocks(index);
        }
        refLocks[index].lock();
    }

    @Override
    public void unlockRefField(int index) {
        refLocks[index].unlock();
    }

    private void initIntLocks(int index) {
        synchronized (this.longFields) {
            if (this.longLocks == null) {
                this.longLocks = new VarLock[this.longFields.length];
            }
            if (this.longLocks[index] == null) {
                this.longLocks[index] = new VarLock();
            }
        }
    }

    private void initFloatLocks(int index) {
        synchronized (this.doubleFields) {
            if (this.doubleLocks == null) {
                this.doubleLocks = new VarLock[this.doubleFields.length];
            }
            if (this.doubleLocks[index] == null) {
                this.doubleLocks[index] = new VarLock();
            }
        }
    }

    private void initStringLocks(int index) {
        synchronized (this.stringFields) {
            if (this.stringLocks == null) {
                this.stringLocks = new VarLock[this.stringFields.length];
            }
            if (this.stringLocks[index] == null) {
                this.stringLocks[index] = new VarLock();
            }
        }
    }

    private void initBooleanLocks(int index) {
        synchronized (this.intFields) {
            if (this.intLocks == null) {
                this.intLocks = new VarLock[this.intFields.length];
            }
            if (this.intLocks[index] == null) {
                this.intLocks[index] = new VarLock();
            }
        }
    }

    private void initBlobLocks(int index) {
        synchronized (this.byteFields) {
            if (this.byteLocks == null) {
                this.byteLocks = new VarLock[this.byteFields.length];
            }
            if (this.byteLocks[index] == null) {
                this.byteLocks[index] = new VarLock();
            }
        }
    }

    private void initRefLocks(int index) {
        synchronized (this.refFields) {
            if (this.refLocks == null) {
                this.refLocks = new VarLock[this.refFields.length];
            }
            if (this.refLocks[index] == null) {
                this.refLocks[index] = new VarLock();
            }
        }
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
