/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.nativeimpl.connectors.data.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.DataIterator;
import org.wso2.ballerina.core.model.values.BValue;

import java.util.Iterator;

/**
 * Cassandra {@link DataIterator} implementation.
 */
public class CassandraDataIterator implements DataIterator {

    private Iterator<Row> itr;
    
    private Row current;
    
    public CassandraDataIterator(ResultSet rs) {
        this.itr = rs.iterator();
    }
    
    @Override
    public void close() {
        /* nothing to do */
    }

    @Override
    public boolean next() {
        boolean result = this.itr.hasNext();
        if (result) {
            this.current = this.itr.next();
        }
        return result;
    }
    
    private void checkCurrentRow() {
        if (this.current == null) {
            throw new BallerinaException("Invalid position in the data iterator");
        }
    }

    @Override
    public String getString(int index) {
        this.checkCurrentRow();
        return this.current.getString(index);
    }

    @Override
    public long getLong(int index) {
        this.checkCurrentRow();
        return this.current.getLong(index);
    }

    @Override
    public int getInt(int index) {
        this.checkCurrentRow();
        return this.current.getInt(index);
    }

    @Override
    public float getFloat(int index) {
        this.checkCurrentRow();
        return this.current.getFloat(index);
    }

    @Override
    public double getDouble(int index) {
        this.checkCurrentRow();
        return this.current.getDouble(index);
    }

    @Override
    public boolean getBoolean(int index) {
        this.checkCurrentRow();
        return this.current.getBool(index);
    }

    @Override
    public String getString(String columnName) {
        this.checkCurrentRow();
        return this.current.getString(columnName);
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
    public long getLong(String columnName) {
        this.checkCurrentRow();
        return this.current.getLong(columnName);
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
    public int getInt(String columnName) {
        this.checkCurrentRow();
        return this.current.getInt(columnName);
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
    public float getFloat(String columnName) {
        this.checkCurrentRow();
        return this.current.getFloat(columnName);
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
    public double getDouble(String columnName) {
        this.checkCurrentRow();
        return this.current.getDouble(columnName);
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
    public boolean getBoolean(String columnName) {
        this.checkCurrentRow();
        return this.current.getBool(columnName);
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
        this.checkCurrentRow();
        return this.current.getObject(columnIndex).toString();
    }

    @Override
    public String getObjectAsString(String columnName) {
        this.checkCurrentRow();
        return this.current.getObject(columnName).toString();
    }

    @Override
    public BValue get(int columnIndex, String type) {
        this.checkCurrentRow();
        return null;
    }

    @Override
    public BValue get(String columnName, String type) {
        this.checkCurrentRow();
        return null;
    }
    
}
