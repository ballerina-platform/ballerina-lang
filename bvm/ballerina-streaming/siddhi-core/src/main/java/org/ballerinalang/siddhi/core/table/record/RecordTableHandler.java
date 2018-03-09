/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.siddhi.core.table.record;

import org.ballerinalang.siddhi.core.exception.ConnectionUnavailableException;
import org.ballerinalang.siddhi.core.util.collection.operator.CompiledCondition;
import org.ballerinalang.siddhi.core.util.collection.operator.CompiledExpression;
import org.ballerinalang.siddhi.core.util.collection.operator.CompiledSelection;
import org.ballerinalang.siddhi.query.api.definition.TableDefinition;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RecordTableHandler is an optional handler that can be implemented to do processing on output events before sending
 * to the AbstractRecordTable.
 */
public abstract class RecordTableHandler {

    private String elementId;
    private RecordTableHandlerCallback recordTableHandlerCallback;

    public String getElementId() {
        return elementId;
    }

    protected final void init(String elementId, TableDefinition tableDefinition,
                              RecordTableHandlerCallback recordTableHandlerCallback) {
        this.elementId = elementId;
        this.recordTableHandlerCallback = recordTableHandlerCallback;
        init(elementId, tableDefinition);
    }

    /**
     * Initialize the Record Table Handler.
     *
     * @param elementId       is the generated id for the record table handler
     * @param tableDefinition is the definition of the table with annotations if any
     */
    public abstract void init(String elementId, TableDefinition tableDefinition);

    public void add(long timestamp, List<Object[]> records) throws ConnectionUnavailableException {
        add(timestamp, records, recordTableHandlerCallback);
    }

    /**
     * Add record.
     *
     * @param timestamp                  the timestamp of the last event in the event chunk
     * @param records                    records that need to be added to the table, each Object[] represent a
     *                                   record and it will match the attributes of the Table Definition.
     * @param recordTableHandlerCallback call back to do operations on the record table
     * @throws ConnectionUnavailableException
     */
    public abstract void add(long timestamp, List<Object[]> records,
                             RecordTableHandlerCallback recordTableHandlerCallback)
            throws ConnectionUnavailableException;

    public void delete(long timestamp, List<Map<String, Object>> deleteConditionParameterMaps,
                       CompiledCondition compiledCondition) throws ConnectionUnavailableException {
        delete(timestamp, deleteConditionParameterMaps, compiledCondition, recordTableHandlerCallback);
    }

    /**
     * Delete record.
     *
     * @param timestamp                    the timestamp of the last event in the event chunk
     * @param deleteConditionParameterMaps map of matching StreamVariable Ids and their values corresponding to the
     *                                     compiled condition
     * @param compiledCondition            the compiledCondition against which records should be matched for deletion
     * @param recordTableHandlerCallback   call back to do operations on the record table
     * @throws ConnectionUnavailableException
     */
    public abstract void delete(long timestamp, List<Map<String, Object>> deleteConditionParameterMaps,
                                CompiledCondition compiledCondition,
                                RecordTableHandlerCallback recordTableHandlerCallback)
            throws ConnectionUnavailableException;

    public void update(long timestamp, CompiledCondition compiledCondition,
                       List<Map<String, Object>> updateConditionParameterMaps,
                       LinkedHashMap<String, CompiledExpression> updateSetMap,
                       List<Map<String, Object>> updateSetParameterMaps) throws ConnectionUnavailableException {
        update(timestamp, compiledCondition, updateConditionParameterMaps, updateSetMap, updateSetParameterMaps,
                recordTableHandlerCallback);
    }

    /**
     * Update record.
     *
     * @param timestamp                    the timestamp of the last event in the event chunk
     * @param updateCondition              the compiledCondition against which records should be matched for update
     * @param updateConditionParameterMaps map of matching StreamVariable Ids and their values corresponding to the
     *                                     compiled condition based on which the records will be updated
     * @param updateSetExpressions         the set of updates mappings and related complied expressions
     * @param updateSetParameterMaps       map of matching StreamVariable Ids and their values corresponding to the
     * @param recordTableHandlerCallback   call back to do operations on the record table
     * @throws ConnectionUnavailableException
     */
    public abstract void update(long timestamp, CompiledCondition updateCondition,
                                List<Map<String, Object>> updateConditionParameterMaps,
                                LinkedHashMap<String, CompiledExpression> updateSetExpressions,
                                List<Map<String, Object>> updateSetParameterMaps,
                                RecordTableHandlerCallback recordTableHandlerCallback)
            throws ConnectionUnavailableException;

    public void updateOrAdd(long timestamp, CompiledCondition compiledCondition,
                            List<Map<String, Object>> updateConditionParameterMaps,
                            LinkedHashMap<String, CompiledExpression> updateSetMap,
                            List<Map<String, Object>> updateSetParameterMaps, List<Object[]> addingRecords)
            throws ConnectionUnavailableException {
        updateOrAdd(timestamp, compiledCondition, updateConditionParameterMaps, updateSetMap, updateSetParameterMaps,
                addingRecords, recordTableHandlerCallback);
    }

    /**
     * Update or add record.
     *
     * @param timestamp                    the timestamp of the last event in the event chunk
     * @param updateCondition              the compiledCondition against which records should be matched for update
     * @param updateConditionParameterMaps map of matching StreamVariable Ids and their values corresponding to the
     *                                     compiled condition based on which the records will be updated
     * @param updateSetExpressions         the set of updates mappings and related complied expressions
     * @param updateSetParameterMaps       map of matching StreamVariable Ids and their values corresponding to the
     *                                     update set
     * @param addingRecords                the values for adding new records if the update condition did not match
     * @param recordTableHandlerCallback   call back to do operations on the record table
     * @throws ConnectionUnavailableException
     */
    public abstract void updateOrAdd(long timestamp, CompiledCondition updateCondition,
                                     List<Map<String, Object>> updateConditionParameterMaps,
                                     LinkedHashMap<String, CompiledExpression> updateSetExpressions,
                                     List<Map<String, Object>> updateSetParameterMaps, List<Object[]> addingRecords,
                                     RecordTableHandlerCallback recordTableHandlerCallback)
            throws ConnectionUnavailableException;

    public Iterator<Object[]> find(long timestamp, Map<String, Object> findConditionParameterMap,
                                   CompiledCondition compiledCondition) throws
            ConnectionUnavailableException {
        return find(timestamp, findConditionParameterMap, compiledCondition, recordTableHandlerCallback);
    }

    /**
     * Find record.
     *
     * @param timestamp                  the timestamp of the event used to match from record table
     * @param findConditionParameterMap  map of matching StreamVariable Ids and their values
     *                                   corresponding to the compiled condition
     * @param compiledCondition          the compiledCondition against which records should be matched
     * @param recordTableHandlerCallback call back to do operations on the record table
     * @return RecordIterator of matching records
     * @throws ConnectionUnavailableException
     */
    public abstract Iterator<Object[]> find(long timestamp, Map<String, Object> findConditionParameterMap,
                                            CompiledCondition compiledCondition,
                                            RecordTableHandlerCallback recordTableHandlerCallback)
            throws ConnectionUnavailableException;

    public boolean contains(long timestamp, Map<String, Object> containsConditionParameterMap,
                            CompiledCondition compiledCondition) throws ConnectionUnavailableException {
        return contains(timestamp, containsConditionParameterMap, compiledCondition, recordTableHandlerCallback);
    }

    /**
     * Check record existence.
     *
     * @param timestamp                     the timestamp of the event used to match from record table
     * @param containsConditionParameterMap map of matching StreamVariable Ids and their values corresponding to the
     *                                      compiled condition
     * @param compiledCondition             the compiledCondition against which records should be matched
     * @param recordTableHandlerCallback    call back to do operations on the record table
     * @return if matching record found or not
     * @throws ConnectionUnavailableException
     */
    public abstract boolean contains(long timestamp, Map<String, Object> containsConditionParameterMap,
                                     CompiledCondition compiledCondition,
                                     RecordTableHandlerCallback recordTableHandlerCallback)
            throws ConnectionUnavailableException;


    public Iterator<Object[]> query(long timestamp, Map<String, Object> parameterMap,
                                    CompiledCondition compiledCondition, CompiledSelection compiledSelection)
            throws ConnectionUnavailableException {
        return query(timestamp, parameterMap, compiledCondition, compiledSelection, recordTableHandlerCallback);
    }

    /**
     * Query record.
     *
     * @param timestamp                  the timestamp of the event used to match from record table
     * @param parameterMap               map of matching StreamVariable Ids and their values
     *                                   corresponding to the compiled condition and selection
     * @param compiledCondition          the compiledCondition against which records should be matched
     * @param compiledSelection          the compiledSelection which maps the events based on selection
     * @param recordTableHandlerCallback call back to do operations on the record table
     * @return RecordIterator of matching records
     * @throws ConnectionUnavailableException
     */
    public abstract Iterator<Object[]> query(long timestamp, Map<String, Object> parameterMap,
                                             CompiledCondition compiledCondition,
                                             CompiledSelection compiledSelection,
                                             RecordTableHandlerCallback recordTableHandlerCallback)
            throws ConnectionUnavailableException;
}
