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
    public long getLong(String columnName) {
        this.checkCurrentRow();
        return this.current.getLong(columnName);
    }

    @Override
    public int getInt(String columnName) {
        this.checkCurrentRow();
        return this.current.getInt(columnName);
    }

    @Override
    public float getFloat(String columnName) {
        this.checkCurrentRow();
        return this.current.getFloat(columnName);
    }

    @Override
    public double getDouble(String columnName) {
        this.checkCurrentRow();
        return this.current.getDouble(columnName);
    }

    @Override
    public boolean getBoolean(String columnName) {
        this.checkCurrentRow();
        return this.current.getBool(columnName);
    }

}
