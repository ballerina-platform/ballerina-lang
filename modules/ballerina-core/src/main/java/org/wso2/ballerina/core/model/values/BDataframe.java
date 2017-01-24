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

/**
 * The {@code BDataframe} represents a data set in Ballerina
 *
 * @since 0.8.0
 */
public class BDataframe implements BRefType {

    private DataIterator iterator;

    public BDataframe(DataIterator dataIterator) {
        this.iterator = dataIterator;
    }

    @Override
    public Object value() {
        return null;
    }

    @Override
    public String stringValue() {
        return null;
    }

    /**
     * Check whether is there anymore result or not.
     *
     * @return whether ResultSet have more data or not
     */
    public boolean hasNext() {
        return iterator.hasNext();
    }

    public Object getColumnValue(int columnIndex) {
        return iterator.getColumnValue(columnIndex);
    }

    public Object getColumnValue(String columnName) {
        return getColumnValue(columnName);
    }

    public String getString(int index) {
        return iterator.getString(index);
    }

    public long getLong(int index) {
        return iterator.getLong(index);
    }

    public int getInt(int index) {
        return iterator.getInt(index);
    }

    public float getFloat(int index) {
        return iterator.getFloat(index);
    }

    public double getDouble(int index) {
        return iterator.getDouble(index);
    }

    public boolean getBoolean(int index) {
        return iterator.getBoolean(index);
    }

}
