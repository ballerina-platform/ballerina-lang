/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.eventtable.rdbms;


import org.apache.hadoop.util.bloom.Key;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.collection.AddingStreamEventExtractor;
import org.wso2.siddhi.core.util.collection.UpdateAttributeMapper;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.List;

/**
 * Operator which is related to RDBMS operations
 */
public class RDBMSOperator implements Operator {

    private final int[] attributeIndexArray;
    private Operator inMemoryEventTableOperator;
    private List<ExpressionExecutor> expressionExecutorList;
    private DBHandler dbHandler;
    private boolean isBloomEnabled;
    private ExecutionInfo executionInfo;
    private int matchingEventOutputSize;

    public RDBMSOperator(ExecutionInfo executionInfo, List<ExpressionExecutor> expressionExecutorList, DBHandler
            dbHandler, Operator inMemoryEventTableOperator, int matchingEventOutputSize) {
        this.expressionExecutorList = expressionExecutorList;
        this.dbHandler = dbHandler;
        this.inMemoryEventTableOperator = inMemoryEventTableOperator;
        this.executionInfo = executionInfo;
        this.matchingEventOutputSize = matchingEventOutputSize;

        if (dbHandler.isBloomFilterEnabled() && executionInfo.isBloomFilterCompatible()) {
            this.isBloomEnabled = true;
        }

        List<Attribute> conditionList = executionInfo.getConditionQueryColumnOrder();
        attributeIndexArray = new int[conditionList.size()];
        int i = 0;
        for (Attribute attribute : conditionList) {
            attributeIndexArray[i++] = getAttributeIndex(dbHandler, attribute.getName());
        }
    }

    private static int getAttributeIndex(DBHandler dbHandler, String attributeName) {
        int i = 0;
        for (Attribute attribute : dbHandler.getAttributeList()) {
            if (attribute.getName().equals(attributeName)) {
                return i;
            }
            i++;
        }
        //not-possible to happen
        return 0;
    }

    @Override
    public void delete(ComplexEventChunk<StateEvent> deletingEventChunk, Object storeEvents) {
        deletingEventChunk.reset();
        List<Object[]> deletionEventList = new ArrayList<Object[]>();
        while (deletingEventChunk.hasNext()) {
            Object[] obj;
            ComplexEvent deletingEvent = deletingEventChunk.next();
            if (expressionExecutorList != null) {
                obj = new Object[expressionExecutorList.size()];
                int count = 0;
                for (ExpressionExecutor expressionExecutor : expressionExecutorList) {
                    Object value = expressionExecutor.execute(deletingEvent);
                    obj[count] = value;
                    count++;
                }
            } else {
                obj = new Object[]{};
            }

            deletionEventList.add(obj);
        }

        if (deletionEventList.size() > 0) {
            dbHandler.deleteEvent(deletionEventList, executionInfo);
        }
    }

    @Override
    public void update(ComplexEventChunk<StateEvent> updatingEventChunk, Object storeEvents, UpdateAttributeMapper[]
            updateAttributeMappers) {
        updatingEventChunk.reset();
        List<Object[]> updateEventList = new ArrayList<Object[]>();
        while (updatingEventChunk.hasNext()) {
            StateEvent updatingEvent = updatingEventChunk.next();
            Object[] incomingEvent = updatingEvent.getStreamEvent(0).getOutputData();
            Object[] obj = new Object[matchingEventOutputSize + expressionExecutorList.size()];
            System.arraycopy(incomingEvent, 0, obj, 0, matchingEventOutputSize);
            int count = matchingEventOutputSize;
            for (ExpressionExecutor expressionExecutor : expressionExecutorList) {
                Object value = expressionExecutor.execute(updatingEvent);
                obj[count] = value;
                count++;
            }
            updateEventList.add(obj);
        }

        if (updateEventList.size() > 0) {
            dbHandler.updateEvent(updateEventList, executionInfo);
        }
    }

    @Override
    public ComplexEventChunk<StreamEvent> tryUpdate(ComplexEventChunk<StateEvent> updatingOrAddingEventChunk, Object
            storeEvents, UpdateAttributeMapper[] updateAttributeMappers, AddingStreamEventExtractor
            addingStreamEventExtractor) {
        updatingOrAddingEventChunk.reset();
        List<Object[]> updateEventList = new ArrayList<Object[]>();

        while (updatingOrAddingEventChunk.hasNext()) {
            StateEvent overwritingOrAddingEvent = updatingOrAddingEventChunk.next();
            Object[] incomingEvent = overwritingOrAddingEvent.getStreamEvent(0).getOutputData();
            Object[] obj = new Object[matchingEventOutputSize + expressionExecutorList.size()];
            System.arraycopy(incomingEvent, 0, obj, 0, matchingEventOutputSize);
            int count = matchingEventOutputSize;
            for (ExpressionExecutor expressionExecutor : expressionExecutorList) {
                Object value = expressionExecutor.execute(overwritingOrAddingEvent);
                obj[count] = value;
                count++;
            }
            updateEventList.add(obj);
        }
        dbHandler.updateOrAddEvent(updateEventList, executionInfo);
        return null;
    }

    @Override
    public CompiledCondition cloneCompiledCondition(String key) {
        return new RDBMSOperator(executionInfo, expressionExecutorList, dbHandler, inMemoryEventTableOperator,
                matchingEventOutputSize);
    }

    @Override
    public StreamEvent find(StateEvent matchingEvent, Object storeEvents, StreamEventCloner storeEventCloner) {

        Object[] obj;
        if (expressionExecutorList != null) {
            obj = new Object[expressionExecutorList.size()];
            int count = 0;
            for (ExpressionExecutor expressionExecutor : expressionExecutorList) {
                Object value = expressionExecutor.execute(matchingEvent);
                obj[count] = value;
                if (isBloomEnabled) {
                    boolean mightContain = dbHandler.getBloomFilters()[attributeIndexArray[count]].membershipTest(new
                            Key(value.toString().getBytes()));
                    if (!mightContain) {
                        return null;
                    }
                }
                count++;
            }
        } else {
            obj = new Object[]{};
        }
        return dbHandler.selectEvent(obj, executionInfo);
    }

    @Override
    public boolean contains(StateEvent matchingEvent, Object storeEvents) {

        Object[] obj;
        if (expressionExecutorList != null) {
            obj = new Object[expressionExecutorList.size()];
            int count = 0;
            for (ExpressionExecutor expressionExecutor : expressionExecutorList) {
                Object value = expressionExecutor.execute(matchingEvent);
                obj[count] = value;
                if (isBloomEnabled) {
                    boolean mightContain = dbHandler.getBloomFilters()[attributeIndexArray[count]].membershipTest(new
                            Key(value.toString().getBytes()));
                    if (!mightContain) {
                        return false;
                    }
                }
                count++;
            }
        } else {
            obj = new Object[]{};
        }
        return dbHandler.checkExistence(obj, executionInfo);
    }

    public Operator getInMemoryEventTableOperator() {
        return inMemoryEventTableOperator;
    }
}
