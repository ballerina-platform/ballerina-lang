/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.ColumnDefinition;
import org.ballerinalang.jvm.DataIterator;
import org.ballerinalang.jvm.IteratorUtils;
import org.ballerinalang.jvm.TableProvider;
import org.ballerinalang.jvm.TableUtils;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BFunctionType;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.BTableType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.jvm.values.api.BFunctionPointer;
import org.ballerinalang.jvm.values.api.BMap;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.api.BTable;
import org.ballerinalang.jvm.values.freeze.FreezeUtils;
import org.ballerinalang.jvm.values.freeze.State;
import org.ballerinalang.jvm.values.freeze.Status;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static org.ballerinalang.jvm.util.BLangConstants.TABLE_LANG_LIB;

/**
 * <p>
 * The {@code {@link TableValue}} represents a two dimensional data set in Ballerina.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 *  
 * @since 0.995.0
 */
public class TableValue implements RefValue, BTable {

    protected volatile DataIterator iterator;
    private volatile boolean hasNextVal;
    private volatile boolean nextPrefetched;
    private volatile TableProvider tableProvider;
    private String tableName;
    private BStructureType constraintType;
    private ArrayValue primaryKeys;
    private volatile boolean tableClosed;
    private volatile Status freezeStatus = new Status(State.UNFROZEN);
    private BType type;
    private BType iteratorNextReturnType;

    @Deprecated
    public TableValue() {
        this.iterator = null;
        this.tableProvider = null;
        this.nextPrefetched = false;
        this.hasNextVal = false;
        this.tableName = null;
        this.type = BTypes.typeTable;
    }

    @Deprecated
    public TableValue(String tableName, BStructureType constraintType) {
        this(constraintType);
        this.tableName = tableName;
    }

    @Deprecated
    public TableValue(BStructureType constraintType) {
        this.nextPrefetched = false;
        this.hasNextVal = false;
        this.constraintType = constraintType;
        this.type = new BTableType(constraintType);
    }

    @Deprecated
    public TableValue(String query, TableValue fromTable, TableValue joinTable,
                      BStructureType constraintType, ArrayValue params) {
        this.tableProvider = TableProvider.getInstance();
        if (!fromTable.isInMemoryTable()) {
            throw BallerinaErrors.createError(BallerinaErrorReasons.TABLE_OPERATION_ERROR,
                    "Table query over a cursor table not supported");
        }
        if (joinTable != null) {
            if (!joinTable.isInMemoryTable()) {
                throw BallerinaErrors.createError(BallerinaErrorReasons.TABLE_OPERATION_ERROR,
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

    @Deprecated
    public TableValue(BType type, ArrayValue keyColumns, ArrayValue dataRows) {
        //Create table with given constraints.
        BType constrainedType = ((BTableType) type).getConstrainedType();
        this.tableProvider = TableProvider.getInstance();
        this.tableName = tableProvider.createTable(constrainedType, keyColumns);
        this.constraintType = (BStructureType) constrainedType;
        this.type = new BTableType(constraintType);
        this.primaryKeys = keyColumns;
        //Insert initial data
        if (dataRows != null) {
            insertInitialData(dataRows);
        }
    }

    @Override
    public String toString() {
        return stringValue();
    }

    /**
     * Returns string representation of the table.
     * 
     * @return string representation of the table
     */
    public String stringValue() {
        return createStringValueDataEntry();
    }

    @Override
    public BString bStringValue() {
        return null;
    }

    private String createStringValueDataEntry() {
        StringJoiner sj = new StringJoiner(" ");
        while (hasNext()) {
            MapValueImpl<?, ?> struct = getNext();
            sj.add(struct.stringValue());
        }
        return sj.toString();
    }

    @Override
    public BType getType() {
        return this.type;
    }

    /**
     * Returns true if the table iterator has more records.
     * @return true if there is more rows
     */
    public boolean hasNext() {
        if (tableClosed) {
            throw BallerinaErrors.createError(BallerinaErrorReasons.TABLE_OPERATION_ERROR,
                    "Trying to perform hasNext operation over a closed table");
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

    /**
     * Move table cursor to the next row.
     */
    public void moveToNext() {
        if (tableClosed) {
            throw BallerinaErrors.createError(BallerinaErrorReasons.TABLE_CLOSED_ERROR,
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

    /**
     * Close the table dataset.
     */
    public void close() {
        if (iterator != null) {
            iterator.close();
        }
        tableClosed = true;
    }

    /**
     * Close and reset the table iterator cursors.
     */
    public void reset() {
        if (iterator != null) {
            iterator.reset();
            iterator = null;
        }
        resetIterationHelperAttributes();
    }

    /**
     * Get the next record.
     *
     * @return {@code MapValue}
     */
    public MapValueImpl<String, Object> getNext() {
        // Make next row the current row
        moveToNext();
        // Create BStruct from current row
        return (MapValueImpl<String, Object>) iterator.generateNext();
    }

    @Override
    public Object performAddOperation(BMap<String, Object> data) {
        return performAddOperation((MapValueImpl<String, Object>) data);
    }

    /**
     * Performs addition of a record to the database.
     *
     * @param data    The record to be inserted
     * @return error if something goes wrong
     */
    public Object performAddOperation(MapValueImpl<String, Object> data) {
        synchronized (this) {
            if (freezeStatus.getState() != State.UNFROZEN) {
                FreezeUtils.handleInvalidUpdate(freezeStatus.getState(), TABLE_LANG_LIB);
            }
        }

        try {
            this.addData(data);
            return null;
        } catch (ErrorValue e) {
            return e;
        } catch (Throwable e) {
            return TableUtils.createTableOperationError(e);
        }
    }

    /**
     * Performs addition of a record to the database.
     *
     * @param data The record to be inserted
     */
    public void addData(MapValueImpl<String, Object> data) {
        if (data.getType() != this.constraintType) {
            throw BallerinaErrors.createError(BallerinaErrorReasons.TABLE_OPERATION_ERROR,
                    "incompatible types: record of type:" + data.getType().getName()
                            + " cannot be added to a table with type:" + this.constraintType.getName());
        }
        tableProvider.insertData(tableName, data);
        reset();
    }

    public void addData(BMap<String, Object> data) {
        addData((MapValueImpl<String, Object>) data);
    }

    /**
     * Remove records that cause true return from given function pointer.
     * @param strand strand to be used for function execution
     * @param func function pointer
     * @return deleted count or error
     */
    public Object performRemoveOperation(Strand strand, FPValue<Object, Boolean> func) {
        synchronized (this) {
            if (freezeStatus.getState() != State.UNFROZEN) {
                FreezeUtils.handleInvalidUpdate(freezeStatus.getState(), TABLE_LANG_LIB);
            }
        }

        if (((BFunctionType) func.type).paramTypes[0] != this.constraintType) {
            return TableUtils.createTableOperationError(new Exception(
                    "incompatible types: function with record type:" + ((BFunctionType) func.type).paramTypes[0]
                            .getName() + " cannot be used to remove records from a table with type:"
                            + this.constraintType.getName()));
        }
        int deletedCount = 0;
        while (this.hasNext()) {
            MapValueImpl<String, Object> row = this.getNext();
            if (func.call(new Object[] { strand, row, true })) {
                tableProvider.deleteData(this.tableName, row);
                ++deletedCount;
            }
        }
        return deletedCount;
    }

    public Object performRemoveOperation(Strand strand, BFunctionPointer<Object, Boolean> func) {
        return performRemoveOperation(strand, (FPValue<Object, Boolean>) func);
    }

    /**
     * Retrieves the value of the designated string column in the current row.
     * @param columnIndex column index
     * @return column value
     */
    public String getString(int columnIndex) {
        return iterator.getString(columnIndex);
    }

    /**
     * Retrieves the value of the designated int column in the current row.
     * @param columnIndex column index
     * @return column value
     */
    public Long getInt(int columnIndex) {
        return iterator.getInt(columnIndex);
    }

    /**
     * Retrieves the value of the designated float column in the current row.
     * @param columnIndex column index
     * @return column value
     */
    public Double getFloat(int columnIndex) {
        return iterator.getFloat(columnIndex);
    }

    /**
     * Retrieves the value of the designated boolean column in the current row.
     * @param columnIndex column index
     * @return column value
     */
    public Boolean getBoolean(int columnIndex) {
        return iterator.getBoolean(columnIndex);
    }

    /**
     * Retrieves the value of the designated blob column in the current row.
     * @param columnIndex column index
     * @return column value
     */
    public String getBlob(int columnIndex) {
        return iterator.getBlob(columnIndex);
    }

    /**
     * Retrieves the value of the designated struct column in the current row.
     * @param columnIndex column index
     * @return column value
     */
    public Object[] getStruct(int columnIndex) {
        return iterator.getStruct(columnIndex);
    }

    /**
     * Retrieves the value of the designated array column in the current row.
     * @param columnIndex column index
     * @return column value
     */
    public Object[] getArray(int columnIndex) {
        return iterator.getArray(columnIndex);
    }

    /**
     * Retrieves the value of the designated decimal column in the current row.
     * @param columnIndex column index
     * @return column value
     */
    public DecimalValue getDecimal(int columnIndex) {
        return iterator.getDecimal(columnIndex);
    }

    /**
     * Returns the list of column definitions.
     * @return list of {@code ColumnDefinition}
     */
    public List<ColumnDefinition> getColumnDefs() {
        return iterator.getColumnDefinitions();
    }

    /**
     * Returns the record structure of the table.
     * @return structure type
     */
    public BStructureType getStructType() {
        return iterator.getStructType();
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        if (tableClosed) {
            throw BallerinaErrors.createError(BallerinaErrorReasons.TABLE_OPERATION_ERROR,
                    "Trying to invoke clone built-in method over a closed table");
        }

        if (isFrozen()) {
            return this;
        }

        if (refs.containsKey(this)) {
            return refs.get(this);
        }

        TableIterator cloneIterator = tableProvider.createIterator(this.tableName, this.constraintType);
        ArrayValue data = new ArrayValueImpl(new BArrayType(this.constraintType));
        int cursor = 0;
        try {
            while (cloneIterator.next()) {
                data.add(cursor++, cloneIterator.generateNext());
            }
            TableValue table = new TableValue(new BTableType(constraintType), this.primaryKeys, data);
            refs.put(this, table);
            return table;
        } finally {
            cloneIterator.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        TableValue copy = (TableValue) copy(refs);
        if (!copy.isFrozen()) {
            copy.freezeDirect();
        }
        return copy;
    }

    @Override
    public IteratorValue getIterator() {
        return new TableValueIterator(this);
    }

    private void generateIterator() {
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
    public void finalize() {
        if (this.iterator != null) {
            this.iterator.close();
        }
        tableProvider.dropTable(this.tableName);
    }

    private void insertInitialData(ArrayValue data) {
        int count = data.size();
        for (int i = 0; i < count; i++) {
            addData((MapValueImpl<String, Object>) data.getRefValue(i));
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
     * Returns an array of primary keys.
     * @return primary key array
     */
    public ArrayValue getPrimaryKeys() {
        return this.primaryKeys;
    }

    /**
     * Provides iterator implementation for table values.
     *
     * @since 0.961.0
     */
    //TODO : copy and stamp to be implemented
    private static class TableValueIterator implements IteratorValue {

        private TableValue table;

        TableValueIterator(TableValue value) {
            table = value;
        }

        @Override
        public boolean hasNext() {
            return table.hasNext();
        }

        @Override
        public Object next() {
            if (hasNext()) {
                return table.getNext();
            }
            return null;
        }

        @Override
        public StringValue bStringValue() {
            return null;
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
    public synchronized void attemptFreeze(Status freezeStatus) {
        if (FreezeUtils.isOpenForFreeze(this.freezeStatus, freezeStatus)) {
            this.freezeStatus = freezeStatus;
        }
    }

    @Override
    public void freezeDirect() {
        this.freezeStatus.setFrozen();
    }

    public BType getIteratorNextReturnType() {
        if (iteratorNextReturnType == null) {
            iteratorNextReturnType = IteratorUtils.createIteratorNextReturnType(constraintType);
        }

        return iteratorNextReturnType;
    }
}
