/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.model.Connector;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;

import java.util.Arrays;

/**
 * The {@code BConnector} represents a Connector in Ballerina.
 *
 * @since 0.8.0
 */
public final class BConnector implements BRefType<Connector>, StructureType {

    private Connector connector;
    private BValue[] connectorMemBlock;

    private long[] longFields;
    private double[] doubleFields;
    private String[] stringFields;
    private int[] intFields;
    private byte[][] byteFields;
    private BRefType[] refFields;

    private BType connectorType;

    // TODO Remove this when old executor is removed
    private BType[] fieldTypes;

    public BConnector(BType connectorType) {
        this(null, new BValue[0]);
        this.connectorType = connectorType;
    }

    public BConnector(Connector connector, BValue[] connectorMemBlock) {
        this.connector = connector;
        this.connectorMemBlock = connectorMemBlock;
    }

    public BValue getValue(int offset) {
        return connectorMemBlock[offset];
    }

    public void setValue(int offset, BValue bValue) {
        this.connectorMemBlock[offset] = bValue;
    }

    public BType[] getFieldTypes() {
        return fieldTypes;
    }

    public void setFieldTypes(BType[] fieldTypes) {
        this.fieldTypes = fieldTypes;
    }

    @Override
    public BValue[] getMemoryBlock() {
        return connectorMemBlock;
    }

    @Override
    public void setMemoryBlock(BValue[] connectorMemBlock) {
        this.connectorMemBlock = connectorMemBlock;
    }

    @Override
    public Connector value() {
        return connector;
    }

    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public BType getType() {
        return BTypes.typeConnector;
    }

    @Override
    public void init(int[] fieldIndexes) {
        longFields = new long[fieldIndexes[0]];
        doubleFields = new double[fieldIndexes[1]];
        stringFields = new String[fieldIndexes[2]];
        Arrays.fill(stringFields, "");
        intFields = new int[fieldIndexes[3]];
        byteFields = new byte[fieldIndexes[4]][];
        refFields = new BRefType[fieldIndexes[5]];
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
    public void setBooleanField(int index, int value) {
        intFields[index] = value;
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
    public BRefType getRefField(int index) {
        return refFields[index];
    }

    @Override
    public void setRefField(int index, BRefType value) {
        refFields[index] = value;
    }

    @Override
    public BValue copy() {
        return null;
    }
}
