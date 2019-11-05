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
package org.ballerinalang.jvm.values.api;

import org.ballerinalang.jvm.ColumnDefinition;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.values.CollectionValue;

import java.util.List;

/**
 * <p>
 * The {@code {@link BTable }} represents a two dimensional data set in Ballerina.
 * </p>
 *
 * @since 1.1.0
 */
public interface BTable extends BRefValue, CollectionValue {

    /**
     * Returns true if the table iterator has more records.
     *
     * @return true if there is more rows
     */
    boolean hasNext();

    /**
     * Move table cursor to the next row.
     */
    void moveToNext();

    /**
     * Close the table dataset.
     */
    void close();

    /**
     * Close and reset the table iterator cursors.
     */
    void reset();

    /**
     * Get the next record.
     *
     * @return {@code MapValue}
     */
    BMap<String, Object> getNext();

    /**
     * Performs addition of a record to the database.
     *
     * @param data    The record to be inserted
     * @return error if something goes wrong
     */
    Object performAddOperation(BMap<String, Object> data);

    /**
     * Performs addition of a record to the database.
     *
     * @param data The record to be inserted
     */
    void addData(BMap<String, Object> data);

    /**
     * Remove records that cause true return from given function pointer.
     *
     * @param strand strand to be used for function execution
     * @param func function pointer
     * @return deleted count or error
     */
    Object performRemoveOperation(Strand strand, BFunctionPointer<Object, Boolean> func);

    /**
     * Retrieves the value of the designated string column in the current row.
     *
     * @param columnIndex column index
     * @return column value
     */
    String getString(int columnIndex);

    /**
     * Retrieves the value of the designated int column in the current row.
     *
     * @param columnIndex column index
     * @return column value
     */
    Long getInt(int columnIndex);

    /**
     * Retrieves the value of the designated float column in the current row.
     *
     * @param columnIndex column index
     * @return column value
     */
    Double getFloat(int columnIndex);

    /**
     * Retrieves the value of the designated boolean column in the current row.
     *
     * @param columnIndex column index
     * @return column value
     */
    Boolean getBoolean(int columnIndex);

    /**
     * Retrieves the value of the designated blob column in the current row.
     *
     * @param columnIndex column index
     * @return column value
     */
    String getBlob(int columnIndex);

    /**
     * Retrieves the value of the designated struct column in the current row.
     *
     * @param columnIndex column index
     * @return column value
     */
    Object[] getStruct(int columnIndex);

    /**
     * Retrieves the value of the designated array column in the current row.
     *
     * @param columnIndex column index
     * @return column value
     */
    Object[] getArray(int columnIndex);

    /**
     * Retrieves the value of the designated decimal column in the current row.
     *
     * @param columnIndex column index
     * @return column value
     */
    BDecimal getDecimal(int columnIndex);

    /**
     * Returns the list of column definitions.
     *
     * @return list of {@code ColumnDefinition}
     */
    List<ColumnDefinition> getColumnDefs();

    /**
     * Returns the record structure of the table.
     *
     * @return structure type
     */
    BStructureType getStructType();

    /**
     * Returns a flag indicating whether this table is an in-memory one.
     *
     * @return Flag indicating whether this table is an in-memory one.
     */
    boolean isInMemoryTable();

    /**
     * Returns an array of primary keys.
     *
     * @return primary key array
     */
    BArray getPrimaryKeys();
}
