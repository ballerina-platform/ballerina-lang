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

package org.wso2.ballerina.core.model.values;

import org.wso2.ballerina.core.model.DataIterator;

import java.util.Map;

/**
 * The {@code BDataframe} represents a data set in Ballerina
 *
 * @since 0.8.0
 */
public class BDataframe implements BRefType {

    private DataIterator iterator;
    private Map<String, Object> properties;

    public BDataframe(DataIterator dataIterator, Map<String, Object> properties) {
        this.iterator = dataIterator;
        this.properties = properties;
    }

    @Override
    public Object value() {
        return null;
    }

    @Override
    public String stringValue() {
        return null;
    }

    public boolean next() {
        return iterator.next();
    }

    public void close() {
        iterator.close();
    }

    public String getString(int index) {
        return iterator.getString(index);
    }

    public String getString(String columnName) {
        return iterator.getString(columnName);
    }

    public long getLong(int index) {
        return iterator.getLong(index);
    }

    public long getLong(String columnName) {
        return iterator.getLong(columnName);
    }

    public int getInt(int index) {
        return iterator.getInt(index);
    }

    public int getInt(String columnName) {
        return iterator.getInt(columnName);
    }

    public float getFloat(int index) {
        return iterator.getFloat(index);
    }

    public float getFloat(String columnName) {
        return iterator.getFloat(columnName);
    }

    public double getDouble(int index) {
        return iterator.getDouble(index);
    }

    public double getDouble(String columnName) {
        return iterator.getDouble(columnName);
    }

    public boolean getBoolean(int index) {
        return iterator.getBoolean(index);
    }

    public boolean getBoolean(String columnName) {
        return iterator.getBoolean(columnName);
    }

    public String[] getAvailableProprtyNames() {
        return properties.keySet().toArray(new String[properties.keySet().size()]);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }
}
