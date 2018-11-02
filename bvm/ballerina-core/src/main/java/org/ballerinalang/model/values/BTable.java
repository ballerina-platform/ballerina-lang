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
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BTableType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.util.TableProvider;
import org.ballerinalang.util.TableUtils;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;

import java.util.List;
import java.util.StringJoiner;

/**
 * The {@code BTable} represents a two dimensional data set in Ballerina.
 *
 * @since 0.8.0
 */
public class BTable implements BRefType<Object>, BCollection {

    protected DataIterator iterator;
    private boolean hasNextVal;
    private boolean nextPrefetched;
    private TableProvider tableProvider;
    private String tableName;
    protected BStructureType constraintType;
    private BStringArray primaryKeys;
    private BStringArray indices;
    private boolean tableClosed;

    public BTable() {
        this.iterator = null;
        this.tableProvider = null;
        this.nextPrefetched = false;
        this.hasNextVal = false;
        this.tableName = null;
        this.constraintType = null;
    }

    public BTable(String tableName, BStructureType constraintType) {
        this.nextPrefetched = false;
        this.hasNextVal = false;
        this.tableProvider = null;
        this.tableName = tableName;
        this.constraintType = constraintType;
    }

    public BTable(String query, BTable fromTable, BTable joinTable,
                  BStructureType constraintType, BRefValueArray params) {
        this.tableProvider = TableProvider.getInstance();
        if (joinTable != null) {
            this.tableName = tableProvider.createTable(fromTable.tableName, joinTable.tableName, query,
                    constraintType, params);
        } else {
            this.tableName = tableProvider.createTable(fromTable.tableName, query, constraintType, params);
        }
        this.constraintType = constraintType;
    }

    public BTable(BType type, BStringArray indexColumns, BStringArray keyColumns, BRefValueArray dataRows) {
        //Create table with given constraints.
        BType constrainedType = ((BTableType) type).getConstrainedType();
        this.tableProvider = TableProvider.getInstance();
        if (constrainedType != null) {
            this.tableName = tableProvider.createTable(constrainedType, keyColumns, indexColumns);
            this.constraintType = (BStructureType) constrainedType;
        }
        this.primaryKeys = keyColumns;
        this.indices = indexColumns;
        //Insert initial data
        if (dataRows != null) {
            insertInitialData(dataRows);
        }
    }

    @Override
    public Object value() {
        return null;
    }

    @Override
    public String stringValue() {
        String constraint = constraintType != null ? "<" + constraintType.toString() + ">" : "";
        StringBuilder tableWrapper = new StringBuilder("table" + constraint + " ");
        StringJoiner tableContent = new StringJoiner(", ", "{", "}");
        tableContent.add(createStringValueEntry("index", indices));
        tableContent.add(createStringValueEntry("primaryKey", primaryKeys));
        tableContent.add(createStringValueDataEntry());
        tableWrapper.append(tableContent.toString());

        return tableWrapper.toString();
    }

    private String createStringValueEntry(String key, BStringArray contents) {
        String stringValue = "[]";
        if (contents != null) {
            stringValue = contents.stringValue();
        }
        return key + ": " + stringValue;
    }

    private String createStringValueDataEntry() {
        StringBuilder sb = new StringBuilder();
        sb.append("data: ");
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        while (hasNext()) {
            BMap<?, ?> struct = getNext();
            sj.add(struct.stringValue());
        }
        sb.append(sj.toString());
        return sb.toString();
    }

    @Override
    public BType getType() {
        return BTypes.typeTable;
    }


    public boolean hasNext() {
        if (tableClosed) {
            throw new BallerinaException("Trying to perform hasNext operation over a closed table");
        }
        if (isIteratorGenerationConditionMet()) {
            generateIterator();
        }
        if (!nextPrefetched && iterator != null) {
            hasNextVal = iterator.next();
            nextPrefetched = true;
        }
        if (!hasNextVal) {
           reset();
        }
        return hasNextVal;
    }

    public void moveToNext() {
        if (tableClosed) {
            throw new BallerinaException("Trying to perform an operation over a closed table");
        }
        if (isIteratorGenerationConditionMet()) {
            generateIterator();
        }
        if (!nextPrefetched && iterator != null) {
            iterator.next();
        } else {
            nextPrefetched = false;
        }
    }

    public void close() {
        if (iterator != null) {
            iterator.close();
        }
        tableClosed = true;
    }

    public void reset() {
        if (iterator != null) {
            iterator.reset();
            iterator = null;
        }
        resetIterationHelperAttributes();
    }

    public BMap<String, BValue> getNext() {
        // Make next row the current row
        moveToNext();
        // Create BStruct from current row
        if (iterator != null) {
            return (BMap<String, BValue>) iterator.generateNext();
        }
        return new BMap<>(BTypes.typeAny);
    }

    /**
     * Performs addition of a record to the database.
     *
     * @param data The record to be inserted
     * @param context The context which represents the runtime state of the program that called "table.add"
     */
    public void performAddOperation(BMap<String, BValue> data, Context context) {
        try {
            this.addData(data, context);
            context.setReturnValues();
        } catch (Throwable e) {
            context.setReturnValues(TableUtils.createTableOperationError(context, e));
        }
    }

    public void addData(BMap<String, BValue> data, Context context) {
        if (this.constraintType == null) {
            throw new BallerinaException("incompatible types: record of type:" + data.getType().getName()
                    + " cannot be added to a table with no type");
        }
        if (data.getType() != this.constraintType) {
            throw new BallerinaException("incompatible types: record of type:" + data.getType().getName()
                    + " cannot be added to a table with type:" + this.constraintType.getName());
        }
        tableProvider.insertData(tableName, data);
        reset();
    }

    public void addData(BMap<String, BValue> data) {
        addData(data, null);
    }

    /**
     * Performs Removal of records matching the condition defined by the provided lambda function.
     *
     * @param context The context which represents the runtime state of the program that called "table.remove"
     * @param lambdaFunction The function that decides the condition of data removal
     */
    public void performRemoveOperation(Context context, BFunctionPointer lambdaFunction) {
        try {
            BType functionInputType = lambdaFunction.value().getParamTypes()[0];
            if (this.constraintType == null) {
                throw new BallerinaException("incompatible types: function with record type:"
                        + functionInputType.getName() + " cannot be used to remove records from a table with no type");
            }
            if (functionInputType != this.constraintType) {
                throw new BallerinaException("incompatible types: function with record type:"
                        + functionInputType.getName() + " cannot be used to remove records from a table with type:"
                        + this.constraintType.getName());
            }
            int deletedCount = 0;
            while (this.hasNext()) {
                BMap<String, BValue> data = this.getNext();
                BValue[] args = { data };
                BValue[] returns = BLangFunctions.invokeCallable(lambdaFunction.value(), args);
                if (((BBoolean) returns[0]).booleanValue()) {
                    ++deletedCount;
                    tableProvider.deleteData(tableName, data);
                }
            }
            context.setReturnValues(new BInteger(deletedCount));
            reset();
        } catch (Throwable e) {
            context.setReturnValues(TableUtils.createTableOperationError(context, e));
        }
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

    public BStructureType getStructType() {
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
        return this.iterator == null && this.constraintType != null;
    }

    protected void resetIterationHelperAttributes() {
        this.nextPrefetched = false;
        this.hasNextVal = false;
    }

    @Override
    protected void finalize() {
        if (this.iterator != null) {
            this.iterator.close();
        }
        tableProvider.dropTable(this.tableName);
    }

    private void insertInitialData(BRefValueArray data) {
        int count = (int) data.size();
        for (int i = 0; i < count; i++) {
            addData((BMap<String, BValue>) data.get(i));
        }
    }

    /**
     * Returns a flag indicating whether this table is an in-memory one.
     * TODO: This is a hack to get the table to JSON conversion works
     * with in-memory tables. Fix this ASAP. Issue: #10615
     *
     * @return Flag indicating whether this table is an in-memory one.
     */
    public boolean isInMemoryTable() {
        return true;
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
            return table.hasNext();
        }
    }
}
