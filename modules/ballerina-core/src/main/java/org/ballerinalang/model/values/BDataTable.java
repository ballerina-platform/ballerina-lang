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
package org.ballerinalang.model.values;

import org.ballerinalang.model.ColumnDefinition;
import org.ballerinalang.model.DataIterator;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;

import java.util.List;
import java.util.Map;

/**
 * The {@code BDataTable} represents a data set in Ballerina.
 *
 * @since 0.8.0
 */
public class BDataTable implements BRefType<Object> {

    private DataIterator iterator;

    public BDataTable(DataIterator dataIterator) {
        this.iterator = dataIterator;
    }

    @Override
    public Object value() {
        return null;
    }

    @Override
    public String stringValue() {
        return "";
    }

    @Override
    public BType getType() {
        return BTypes.typeDatatable;
    }

    public boolean hasNext(boolean isInTransaction) {
        boolean hasNext = iterator.next();
        if (!hasNext) {
            close(isInTransaction);
        }
        return hasNext;
    }

    public void close(boolean isInTransaction) {
        iterator.close(isInTransaction);
    }

    public BStruct getNext() {
        return iterator.generateNext();
    }

    public String getString(String columnName) {
        return iterator.getString(columnName);
    }

    public long getInt(String columnName) {
        return iterator.getInt(columnName);
    }

    public double getFloat(String columnName) {
        return iterator.getFloat(columnName);
    }

    public boolean getBoolean(String columnName) {
        return iterator.getBoolean(columnName);
    }

    public String getObjectAsString(String columnName) {
        return iterator.getObjectAsString(columnName);
    }

    public Map<String, Object> getArray(String columnName) {
        return iterator.getArray(columnName);
    }

    public List<ColumnDefinition> getColumnDefs() {
        return iterator.getColumnDefinitions();
    }

    @Override
    public BValue copy() {
        return null;
    }
}
