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

    public void lockIntField(int index, String workerName) {
        if (longLocks == null || longLocks[index] == null) {
            initIntLocks(index);
        }
        logBeforeLock(index, workerName, "int");
        longLocks[index].lock();
        logAfterLock(index, workerName, "int");
    }

    public void unlockIntField(int index, String workerName) {
        longLocks[index].unlock();
        logLockReleased(index, workerName, "int");
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

    public void lockFloatField(int index, String workerName) {
        if (doubleLocks == null || doubleLocks[index] == null) {
            initFloatLocks(index);
        }
        logBeforeLock(index, workerName, "float");
        doubleLocks[index].lock();
        logAfterLock(index, workerName, "float");
    }

    public void unlockFloatField(int index, String workerName) {
        doubleLocks[index].unlock();
        logLockReleased(index, workerName, "float");
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

    public void lockStringField(int index, String workerName) {
        if (stringLocks == null || stringLocks[index] == null) {
            initStringLocks(index);
        }
        logBeforeLock(index, workerName, "string");
        stringLocks[index].lock();
        logAfterLock(index, workerName, "string");
    }

    public void unlockStringField(int index, String workerName) {
        stringLocks[index].unlock();
        logLockReleased(index, workerName, "string");
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

    public void lockBooleanField(int index, String workerName) {
        if (intLocks == null || intLocks[index] == null) {
            initBooleanLocks(index);
        }
        logBeforeLock(index, workerName, "boolean");
        intLocks[index].lock();
        logAfterLock(index, workerName, "string");
    }

    public void unlockBooleanField(int index, String workerName) {
        intLocks[index].unlock();
        logLockReleased(index, workerName, "boolean");
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

    public void lockBlobField(int index, String workerName) {
        if (byteLocks == null || byteLocks[index] == null) {
            initBlobLocks(index);
        }
        logBeforeLock(index, workerName, "blob");
        byteLocks[index].lock();
        logAfterLock(index, workerName, "blob");
    }

    public void unlockBlobField(int index, String workerName) {
        byteLocks[index].unlock();
        logLockReleased(index, workerName, "blob");
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

    public void lockRefField(int index, String workerName) {
        if (refLocks == null || refLocks[index] == null) {
            initRefLocks(index);
        }
        logBeforeLock(index, workerName, "ref");
        refLocks[index].lock();
        logAfterLock(index, workerName, "ref");
    }

    public void unlockRefField(int index, String workerName) {
        refLocks[index].unlock();
        logLockReleased(index, workerName, "ref");
    }

    private void logBeforeLock(int index, String workerName, String varType) {
        String threadId = Thread.currentThread().getName() + ":" + Thread.currentThread().getId();
        System.out.println(System.nanoTime() + ", ******** trying to lock index of, '" + varType + "', " + index
                + ", worker, " + workerName + ", thread id, " + threadId);
    }

    private void logAfterLock(int index, String workerName, String varType) {
        String threadId = Thread.currentThread().getName() + ":" + Thread.currentThread().getId();
        System.out.println(System.nanoTime() + ", ******** lock acquired index of, '" + varType + "', " + index
                + ", worker, " + workerName + ", thread id, " + threadId);
    }

    private void logLockReleased(int index, String workerName, String varType) {
        String threadId = Thread.currentThread().getName() + ":" + Thread.currentThread().getId();
        System.out.println(System.nanoTime() + ", ******** lock released index of, '" + varType + "', " + index
                + ", worker, " + workerName + ", thread id, " + threadId);
    }

    private synchronized void initIntLocks(int index) {
        if (longLocks == null) {
            longLocks = new VarLock[longFields.length];
        }
        if (longLocks[index] == null) {
            longLocks[index] = new VarLock();
        }
    }

    private synchronized void initFloatLocks(int index) {
        if (doubleLocks == null) {
            doubleLocks = new VarLock[doubleFields.length];
        }
        if (doubleLocks[index] == null) {
            doubleLocks[index] = new VarLock();
        }
    }

    private synchronized void initStringLocks(int index) {
        if (stringLocks == null) {
            stringLocks = new VarLock[stringFields.length];
        }
        if (stringLocks[index] == null) {
            stringLocks[index] = new VarLock();
        }
    }

    private synchronized void initBooleanLocks(int index) {
        if (intLocks == null) {
            intLocks = new VarLock[intFields.length];
        }
        if (intLocks[index] == null) {
            intLocks[index] = new VarLock();
        }
    }

    private synchronized void initBlobLocks(int index) {
        if (byteLocks == null) {
            byteLocks = new VarLock[byteFields.length];
        }
        if (byteLocks[index] == null) {
            byteLocks[index] = new VarLock();
        }
    }

    private synchronized void initRefLocks(int index) {
        if (refLocks == null) {
            refLocks = new VarLock[refFields.length];
        }
        if (refLocks[index] == null) {
            refLocks[index] = new VarLock();
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
