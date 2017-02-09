/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerina.core.model;

import org.wso2.ballerina.core.model.values.BValue;

/**
 * Empty iterator for default usage. This will represent empty datatable object in ballerina
 */
public class EmptyDataIterator implements DataIterator {
    @Override
    public void close() {
        // Do nothing.
    }

    @Override
    public boolean next() {
        return false;
    }

    @Override
    public String getString(int index) {
        return null;
    }

    @Override
    public String getString(String columnName) {
        return null;
    }

    @Override
    public String[] getStringArray(int columnIndex) {
        return new String[0];
    }

    @Override
    public String[] getStringArray(String columnName) {
        return new String[0];
    }

    @Override
    public long getLong(int index) {
        return 0;
    }

    @Override
    public long getLong(String columnName) {
        return 0;
    }

    @Override
    public long[] getLongArray(int columnIndex) {
        return new long[0];
    }

    @Override
    public long[] getLongArray(String columnName) {
        return new long[0];
    }

    @Override
    public int getInt(int index) {
        return 0;
    }

    @Override
    public int getInt(String columnName) {
        return 0;
    }

    @Override
    public int[] getIntArray(int columnIndex) {
        return new int[0];
    }

    @Override
    public int[] getIntArray(String columnName) {
        return new int[0];
    }

    @Override
    public float getFloat(int index) {
        return 0;
    }

    @Override
    public float getFloat(String columnName) {
        return 0;
    }

    @Override
    public float[] getFloatArray(int columnIndex) {
        return new float[0];
    }

    @Override
    public float[] getFloatArray(String columnName) {
        return new float[0];
    }

    @Override
    public double getDouble(int index) {
        return 0;
    }

    @Override
    public double getDouble(String columnName) {
        return 0;
    }

    @Override
    public double[] getDoubleArray(int columnIndex) {
        return new double[0];
    }

    @Override
    public double[] getDoubleArray(String columnName) {
        return new double[0];
    }

    @Override
    public boolean getBoolean(int index) {
        return false;
    }

    @Override
    public boolean getBoolean(String columnName) {
        return false;
    }

    @Override
    public boolean[] getBooleanArray(int columnIndex) {
        return new boolean[0];
    }

    @Override
    public boolean[] getBooleanArray(String columnName) {
        return new boolean[0];
    }

    @Override
    public String getObjectAsString(int columnIndex) {
        return null;
    }

    @Override
    public String getObjectAsString(String columnName) {
        return null;
    }

    @Override
    public BValue get(int columnIndex, String type) {
        return null;
    }

    @Override
    public BValue get(String columnName, String type) {
        return null;
    }
}
