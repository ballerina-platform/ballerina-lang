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

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.ColumnDefinition;
import org.ballerinalang.model.DataIterator;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BTableType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.util.TableProvider;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;

import java.util.List;
import java.util.StringJoiner;

/**
 * The {@code BTable} represents a data set in Ballerina.
 *
 * @since 0.8.0
 */
public class BTable implements BRefType<Object>, BCollection {

    protected DataIterator iterator;
    private boolean hasNextVal;
    private boolean nextPrefetched;
    private TableProvider tableProvider;
    private String tableName;
    protected BStructType constraintType;
    private boolean isInMemoryTable;

    public BTable() {
        this.iterator = null;
        this.tableProvider = null;
        this.nextPrefetched = false;
        this.hasNextVal = false;
        this.tableName = null;
        this.constraintType = null;
        this.isInMemoryTable = false;
    }

    public BTable(DataIterator dataIterator) {
        this.iterator = dataIterator;
        this.nextPrefetched = false;
        this.hasNextVal = false;
        this.tableProvider = null;
        this.tableName = null;
        this.constraintType = null;
        this.isInMemoryTable = false;
    }

    public BTable(String tableName, BStructType constraintType) {
        this.nextPrefetched = false;
        this.hasNextVal = false;
        this.tableProvider = null;
        this.tableName = tableName;
        this.isInMemoryTable = false;
        this.constraintType = constraintType;
    }

    public BTable(String query, BTable fromTable, BTable joinTable, BStructType constraintType, BRefValueArray params) {
        this.tableProvider = TableProvider.getInstance();
        if (joinTable != null) {
            this.tableName = tableProvider.createTable(fromTable.tableName, joinTable.tableName, query,
                    constraintType, params);
        } else {
            this.tableName = tableProvider.createTable(fromTable.tableName, query, constraintType, params);
        }
        this.constraintType = constraintType;
        this.isInMemoryTable = true;
    }

    public BTable(BType type, BStruct configStruct) {
        BStringArray primaryKeys = null;
        BStringArray indexColumns = null;
        BRefValueArray data = null;
        if (configStruct != null) {
            primaryKeys = (BStringArray) configStruct.getRefField(0);
            indexColumns = (BStringArray) configStruct.getRefField(1);
            data = (BRefValueArray) configStruct.getRefField(2);
        }
        //Create table with given contraints.
        this.tableProvider = TableProvider.getInstance();
        this.tableName = tableProvider.createTable(((BTableType) type).getConstrainedType(), primaryKeys, indexColumns);
        this.constraintType = (BStructType) ((BTableType) type).getConstrainedType();
        this.isInMemoryTable = true;
        //Insert initial data
        if (data != null) {
            insertInitialData(data);
        }
    }

    @Override
    public Object value() {
        return null;
    }

    @Override
    public String stringValue() {
        if (!this.isInMemoryTable) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{data: ");
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        while (hasNext(false)) {
            BStruct struct = getNext();
            sj.add(struct.stringValue());
        }
        sb.append(sj.toString());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public BType getType() {
        return BTypes.typeTable;
    }


    public boolean hasNext(boolean isInTransaction) {
        if (isIteratorGenerationConditionMet()) {
            generateIterator();
        }
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
        if (isIteratorGenerationConditionMet()) {
            generateIterator();
        }
        if (!nextPrefetched) {
            iterator.next();
        } else {
            nextPrefetched = false;
        }
    }

    public void close(boolean isInTransaction) {
        if (iterator != null) {
            iterator.close(isInTransaction);
            if (iteratorResetRequired()) {
                resetIterator();
            }
        }
    }

    public BStruct getNext() {
        // Make next row the current row
        next();
        // Create BStruct from current row
        return iterator.generateNext();
    }

    /**
     * Performs addition of a record to the database.
     *
     * @param data The record to be inserted
     * @param context The context which represents the runtime state of the program that called "table.add"
     */
    public void performAddOperation(BStruct data, Context context) {
        this.addData(data, context);
        context.setReturnValues();
    }

    public void addData(BStruct data, Context context) {
        if (!this.isInMemoryTable) {
            throw new BallerinaException("data cannot be added to a table returned from a database");
        }
        if (data.getType() != this.constraintType) {
            throw new BallerinaException("incompatible types: struct of type:" + data.getType().getName()
                    + " cannot be added to a table with type:" + this.constraintType.getName());
        }
        tableProvider.insertData(tableName, data);
        resetIterator();
    }

    public void addData(BStruct data) {
        addData(data, null);
    }

    /**
     * Performs Removal of records matching the condition defined by the provided lambda function.
     *
     * @param context The context which represents the runtime state of the program that called "table.remove"
     * @param lambdaFunction The function that decides the condition of data removal
     */
    public void performRemoveOperation(Context context, BFunctionPointer lambdaFunction) {
        int deletedCount = 0;
        while (this.hasNext(false)) {
            BStruct data = this.getNext();
            BValue[] args = { data };
            BValue[] returns = BLangFunctions
                    .invokeCallable(lambdaFunction.value().getFunctionInfo(), args);
            if (((BBoolean) returns[0]).booleanValue()) {
                ++deletedCount;
                this.removeData(data);
            }
        }
        context.setReturnValues(new BInteger(deletedCount));
    }

    private void removeData(BStruct data) {
        if (!this.isInMemoryTable) {
            throw new BallerinaException("data cannot be deleted from a table returned from a database");
        }
        tableProvider.deleteData(tableName, data);
        resetIterator();
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

    protected void generateIterator() {
        this.iterator = tableProvider.createIterator(tableName, this.constraintType);
        resetIterationHelperAttributes();
    }

    protected boolean isIteratorGenerationConditionMet() {
        return this.isInMemoryTable && this.iterator == null;
    }

    protected void resetIterationHelperAttributes() {
        this.nextPrefetched = false;
        this.hasNextVal = false;
    }

    @Override
    protected void finalize() {
        if (this.isInMemoryTable) {
            if (this.iterator != null) {
                this.iterator.close(false);
            }
            tableProvider.dropTable(this.tableName);
        }
    }

    private void insertInitialData(BRefValueArray data) {
        int count = (int) data.size();
        for (int i = 0; i < count; i++) {
            if (!(data.get(i) instanceof BStruct)) {
                throw new BallerinaException("initial data should be in struct type");
            }
            addData((BStruct) data.get(i));
        }
    }

    protected void resetIterator() {
        if (iterator != null) {
            iterator.close(false);
            this.iterator = null;
        }
        resetIterationHelperAttributes();
    }

    protected boolean iteratorResetRequired() {
        return isInMemoryTable;
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
