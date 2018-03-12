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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Record Table Handler Callback is used to send events to the {@link AbstractRecordTable} after being handled by
 * {@link RecordTableHandler}.
 */
public class RecordTableHandlerCallback {

    private static final Logger log = LoggerFactory.getLogger(RecordTableHandlerCallback.class);

    private AbstractRecordTable abstractRecordTable;

    RecordTableHandlerCallback(AbstractRecordTable abstractRecordTable) {
        this.abstractRecordTable = abstractRecordTable;
    }

    public void add(List<Object[]> records) throws ConnectionUnavailableException {
        abstractRecordTable.add(records);
    }

    public RecordIterator<Object[]> find(Map<String, Object> findConditionParameterMap,
                                         CompiledCondition compiledCondition) throws ConnectionUnavailableException {
        return abstractRecordTable.find(findConditionParameterMap, compiledCondition);
    }

    public boolean contains(Map<String, Object> containsConditionParameterMap, CompiledCondition compiledCondition)
            throws ConnectionUnavailableException {
        return abstractRecordTable.contains(containsConditionParameterMap, compiledCondition);
    }

    public void delete(List<Map<String, Object>> deleteConditionParameterMaps, CompiledCondition compiledCondition)
            throws ConnectionUnavailableException {
        abstractRecordTable.delete(deleteConditionParameterMaps, compiledCondition);
    }

    public void update(CompiledCondition updateCondition, List<Map<String, Object>> updateConditionParameterMaps,
                       Map<String, CompiledExpression> updateSetExpressions,
                       List<Map<String, Object>> updateSetParameterMaps) throws ConnectionUnavailableException {
        abstractRecordTable.update(updateCondition, updateConditionParameterMaps, updateSetExpressions,
                updateSetParameterMaps);
    }

    public void updateOrAdd(CompiledCondition updateCondition, List<Map<String, Object>> updateConditionParameterMaps,
                            Map<String, CompiledExpression> updateSetExpressions,
                            List<Map<String, Object>> updateSetParameterMaps, List<Object[]> addingRecords)
            throws ConnectionUnavailableException {
        abstractRecordTable.updateOrAdd(updateCondition, updateConditionParameterMaps, updateSetExpressions,
                updateSetParameterMaps, addingRecords);
    }

    public Iterator<Object[]> query(Map<String, Object> parameterMap, CompiledCondition compiledCondition,
                                    CompiledSelection compiledSelection)
            throws ConnectionUnavailableException {
        if (abstractRecordTable instanceof AbstractQueryableRecordTable) {
            return ((AbstractQueryableRecordTable) abstractRecordTable).query(parameterMap, compiledCondition,
                    compiledSelection);
        } else {
            log.error("Record Table " + this.abstractRecordTable.getTableDefinition().getId() +
                    " used is not a Queryable Record Table.");
            return null;
        }
    }
}
