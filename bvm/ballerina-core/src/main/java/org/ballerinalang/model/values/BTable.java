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
import org.ballerinalang.bre.bvm.BVM;
import org.ballerinalang.bre.bvm.BVMExecutor;
import org.ballerinalang.model.ColumnDefinition;
import org.ballerinalang.model.DataIterator;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BTableType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.util.TableIterator;
import org.ballerinalang.util.TableProvider;
import org.ballerinalang.util.TableUtils;
import org.ballerinalang.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static org.ballerinalang.model.util.FreezeUtils.handleInvalidUpdate;
import static org.ballerinalang.model.util.FreezeUtils.isOpenForFreeze;

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
    private BValueArray primaryKeys;
    private BValueArray indices;
    private boolean tableClosed;
    private volatile BVM.FreezeStatus freezeStatus = new BVM.FreezeStatus(BVM.FreezeStatus.State.UNFROZEN);
    private BType type;

    public BTable() {
        this.iterator = null;
        this.tableProvider = null;
        this.nextPrefetched = false;
        this.hasNextVal = false;
        this.tableName = null;
        this.type = BTypes.typeTable;
    }

    public BTable(String tableName, BStructureType constraintType) {
        this(constraintType);
        this.tableName = tableName;
    }

    public BTable(BStructureType constraintType) {
        this.nextPrefetched = false;
        this.hasNextVal = false;
        this.constraintType = constraintType;
        this.type = new BTableType(constraintType);
    }

    public BTable(String query, BTable fromTable, BTable joinTable,
                  BStructureType constraintType, BValueArray params) {
        this.tableProvider = TableProvider.getInstance();
        if (!fromTable.isInMemoryTable()) {
            throw new BallerinaException(BallerinaErrorReasons.TABLE_OPERATION_ERROR,
                                         "Table query over a cursor table not supported");
        }
        if (joinTable != null) {
            if (!joinTable.isInMemoryTable()) {
                throw new BallerinaException(BallerinaErrorReasons.TABLE_OPERATION_ERROR,
                                             "Table query over a cursor table not supported");
            }
            this.tableName = tableProvider.createTable(fromTable.tableName, joinTable.tableName, query,
                    constraintType, params);
        } else {
            this.tableName = tableProvider.createTable(fromTable.tableName, query, constraintType, params);
        }
        this.constraintType = constraintType;
        this.type = new BTableType(constraintType);
    }

    public BTable(BType type, BValueArray indexColumns, BValueArray keyColumns, BValueArray dataRows) {
        //Create table with given constraints.
        BType constrainedType = ((BTableType) type).getConstrainedType();
        this.tableProvider = TableProvider.getInstance();
        this.tableName = tableProvider.createTable(constrainedType, keyColumns, indexColumns);
        this.constraintType = (BStructureType) constrainedType;
        this.type = new BTableType(constraintType);
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

    private String createStringValueEntry(String key, BValueArray contents) {
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
        return this.type;
    }

    @Override
    public void stamp(BType type, List<BVM.TypeValuePair> unresolvedValues) {

    }

    public boolean hasNext() {
        if (tableClosed) {
            throw new BallerinaException("Trying to perform hasNext operation over a closed table");
        }
        if (isIteratorGenerationConditionMet()) {
            generateIterator();
        }
        if (!nextPrefetched) {
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
            throw new BallerinaException(BallerinaErrorReasons.TABLE_CLOSED_ERROR,
                                         "Trying to perform an operation over a closed table");
        }
        if (isIteratorGenerationConditionMet()) {
            generateIterator();
        }
        if (!nextPrefetched) {
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
        return (BMap<String, BValue>) iterator.generateNext();
    }

    /**
     * Performs addition of a record to the database.
     *
     * @param data    The record to be inserted
     * @param context The context which represents the runtime state of the program that called "table.add"
     */
    public void performAddOperation(BMap<String, BValue> data, Context context) {
        synchronized (this) {
            if (freezeStatus.getState() != BVM.FreezeStatus.State.UNFROZEN) {
                handleInvalidUpdate(freezeStatus.getState());
            }
        }

        try {
            this.addData(data, context);
        } catch (Throwable e) {
            context.setReturnValues(TableUtils.createTableOperationError(context, e));
        }
    }

    public void addData(BMap<String, BValue> data, Context context) {
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
     * @param context        The context which represents the runtime state of the program that called "table.remove"
     * @param lambdaFunction The function that decides the condition of data removal
     */
    public void performRemoveOperation(Context context, BFunctionPointer lambdaFunction) {
        synchronized (this) {
            if (freezeStatus.getState() != BVM.FreezeStatus.State.UNFROZEN) {
                handleInvalidUpdate(freezeStatus.getState());
            }
        }

        try {
            BType functionInputType = lambdaFunction.value().getParamTypes()[0];
            if (functionInputType != this.constraintType) {
                throw new BallerinaException("incompatible types: function with record type:"
                        + functionInputType.getName() + " cannot be used to remove records from a table with type:"
                        + this.constraintType.getName());
            }
            int deletedCount = 0;
            while (this.hasNext()) {

                BMap<String, BValue> data = this.getNext();
                BValue[] args = {data};
                BValue[] returns = BVMExecutor.executeFunction(lambdaFunction.value().getPackageInfo()
                        .getProgramFile(), lambdaFunction.value(), args);
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

    public Long getInt(int columnIndex) {
        return iterator.getInt(columnIndex);
    }

    public Double getFloat(int columnIndex) {
        return iterator.getFloat(columnIndex);
    }

    public Boolean getBoolean(int columnIndex) {
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
    public BValue copy(Map<BValue, BValue> refs) {
        if (tableClosed) {
            throw new BallerinaException("Trying to invoke clone built-in method over a closed table");
        }

        if (isFrozen()) {
            return this;
        }

        if (refs.containsKey(this)) {
            return refs.get(this);
        }

        TableIterator cloneIterator = tableProvider.createIterator(this.tableName, this.constraintType);
        BValueArray data = new BValueArray();
        int cursor = 0;
        try {
            while (cloneIterator.next()) {
                data.add(cursor++, cloneIterator.generateNext());
            }
            BTable table = new BTable(new BTableType(constraintType), this.indices, this.primaryKeys, data);
            refs.put(this, table);
            return table;
        } finally {
            cloneIterator.close();
        }
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
        return this.iterator == null;
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

    private void insertInitialData(BValueArray data) {
        int count = (int) data.size();
        for (int i = 0; i < count; i++) {
            addData((BMap<String, BValue>) data.getRefValue(i));
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
     * Returns the length or the number of rows of the table.
     *
     * @return number of rows of the table
     */
    public int length() {
        if (tableName == null) {
            return 0;
        }
        return tableProvider.getRowCount(tableName);
    }

    /**
     * Provides iterator implementation for table values.
     *
     * @since 0.961.0
     */
    private static class BTableIterator<K, V extends BValue> implements BIterator {

        private BTable table;

        BTableIterator(BTable value) {
            table = value;
        }

        @Override
        public BValue getNext() {
            if (hasNext()) {
                return table.getNext();
            }
            return null;
        }

        @Override
        public boolean hasNext() {
            return table.hasNext();
        }

        @Override
        public void stamp(BType type, List<BVM.TypeValuePair> unresolvedValues) {

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean isFrozen() {
        return this.freezeStatus.isFrozen();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void attemptFreeze(BVM.FreezeStatus freezeStatus) {
        if (isOpenForFreeze(this.freezeStatus, freezeStatus)) {
            this.freezeStatus = freezeStatus;
        }
    }
}
