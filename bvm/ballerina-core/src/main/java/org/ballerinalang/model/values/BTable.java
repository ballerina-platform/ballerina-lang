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
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;

import java.util.List;

/**
 * The {@code BTable} represents a data set in Ballerina.
 *
 * @since 0.8.0
 */
public class BTable implements BRefType<Object>, BCollection {

    private DataIterator iterator;
    private boolean hasNextVal;
    private boolean nextPrefetched;

    public BTable(DataIterator dataIterator) {
        this.iterator = dataIterator;
        this.nextPrefetched = false;
        this.hasNextVal = false;
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
        return BTypes.typeTable;
    }

    public boolean hasNext(boolean isInTransaction) {
        if (!nextPrefetched) {
            hasNextVal = iterator.next();
            nextPrefetched = true;
        }
        if (!hasNextVal) {
            close(isInTransaction);
        }
        return hasNextVal;
    }

    public void next() {
        if (!nextPrefetched) {
            iterator.next();
        } else {
            nextPrefetched = false;
        }
    }

    public void close(boolean isInTransaction) {
        iterator.close(isInTransaction);
    }

    public BStruct getNext() {
        next();
        return iterator.generateNext();
    }

    public String getString(int columnIndex) {
        return iterator.getString(columnIndex);
    }

    public long getInt(int columnIndex) {
        return iterator.getInt(columnIndex);
    }

    public double getFloat(int columnIndex) {
        return iterator.getFloat(columnIndex);
    }

    public boolean getBoolean(int columnIndex) {
        return iterator.getBoolean(columnIndex);
    }

    public String getBlob(int columnIndex) {
        return iterator.getBlob(columnIndex);
    }

    public Object[] getStruct(int columnIndex) {
        return iterator.getStruct(columnIndex);
    }

    public Object[] getArray(int columnIndex) {
        return iterator.getArray(columnIndex);
    }

    public List<ColumnDefinition> getColumnDefs() {
        return iterator.getColumnDefinitions();
    }

    public BStructType getStructType() {
        return iterator.getStructType();
    }

    @Override
    public BValue copy() {
        return null;
    }

    @Override
    public BIterator newIterator() {
        return new BTable.BTableIterator(this);
    }

    /**
     * Provides iterator implementation for table values.
     *
     * @since 0.961.0
     */
    private static class BTableIterator<K, V extends BValue> implements BIterator {

        private BTable table;
        private int cursor = 0;

        BTableIterator(BTable value) {
            table = value;
        }

        @Override
        public BValue[] getNext(int arity) {
            if (arity == 1) {
                return new BValue[] {table.getNext()};
            }
            int cursor = this.cursor++;
            return new BValue[] {new BInteger(cursor), table.getNext()};
        }

        @Override
        public boolean hasNext() {
            return table.hasNext(false);
        }
    }
}
