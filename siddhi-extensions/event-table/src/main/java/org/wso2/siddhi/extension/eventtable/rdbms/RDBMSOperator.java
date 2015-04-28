/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.wso2.siddhi.extension.eventtable.rdbms;


import org.apache.hadoop.util.bloom.Key;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.List;

/**
 * Operator which is related to RDBMS operations
 */
public class RDBMSOperator implements Operator {

    private Operator inMemoryEventTableOperator;
    private List<ExpressionExecutor> expressionExecutorList;
    private DBHandler dbHandler;
    private final boolean isBloomEnabled;
    private final int[] attributeIndexArray;
    private ExecutionInfo executionInfo;

    public RDBMSOperator(ExecutionInfo executionInfo, List<ExpressionExecutor> expressionExecutorList, DBHandler dbHandler, Operator inMemoryEventTableOperator) {
        this.expressionExecutorList = expressionExecutorList;
        this.dbHandler = dbHandler;
        this.inMemoryEventTableOperator = inMemoryEventTableOperator;
        this.isBloomEnabled = dbHandler.isBloomFilterEnabled();
        this.executionInfo = executionInfo;
        List<Attribute> conditionList = executionInfo.getConditionQueryColumnOrder();
        attributeIndexArray = new int[conditionList.size()];

        int i = 0;
        for (Attribute attribute : conditionList) {
            attributeIndexArray[i++] = getAttributeIndex(dbHandler, attribute.getName());
        }
    }

    @Override
    public void delete(ComplexEventChunk<StreamEvent> deletingEventChunk, Object candidateEvents) {
        deletingEventChunk.reset();
        while (deletingEventChunk.hasNext()) {
            Object[] obj;
            StreamEvent deletingEvent = deletingEventChunk.next();
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

            dbHandler.deleteEvent(obj, deletingEvent,executionInfo);
        }
    }

    @Override
    public void update(ComplexEventChunk<StreamEvent> updatingEventChunk, Object candidateEvents, int[] mappingPosition) {
        updatingEventChunk.reset();
        while (updatingEventChunk.hasNext()) {
            StreamEvent updatingEvent = updatingEventChunk.next();
            Object[] incomingEvent = updatingEvent.getOutputData();
            Object[] obj = new Object[incomingEvent.length + expressionExecutorList.size()];
            System.arraycopy(incomingEvent, 0, obj, 0, incomingEvent.length);
            int count = incomingEvent.length;
            for (ExpressionExecutor expressionExecutor : expressionExecutorList) {
                Object value = expressionExecutor.execute(updatingEvent);
                obj[count] = value;
                count++;
            }
            dbHandler.updateEvent(obj,executionInfo);
        }
    }

    @Override
    public Finder cloneFinder() {
        return new RDBMSOperator(executionInfo,expressionExecutorList, dbHandler, inMemoryEventTableOperator);
    }

    @Override
    public StreamEvent find(ComplexEvent matchingEvent, Object candidateEvents, StreamEventCloner streamEventCloner) {

        Object[] obj;
        if (expressionExecutorList != null) {
            obj = new Object[expressionExecutorList.size()];
            int count = 0;
            for (ExpressionExecutor expressionExecutor : expressionExecutorList) {
                Object value = expressionExecutor.execute(matchingEvent);
                obj[count] = value;
                count++;
            }
        } else {
            obj = new Object[]{};
        }
        return dbHandler.selectEvent(obj,executionInfo);
    }

    @Override
    public boolean contains(ComplexEvent matchingEvent, Object candidateEvents) {

        Object[] obj;
        if (expressionExecutorList != null) {
            obj = new Object[expressionExecutorList.size()];
            int count = 0;
            for (ExpressionExecutor expressionExecutor : expressionExecutorList) {
                Object value = expressionExecutor.execute(matchingEvent);
                obj[count] = value;
                if (isBloomEnabled) {
                    boolean mightContain = dbHandler.getBloomFilters()[attributeIndexArray[count]].membershipTest(new Key(value.toString().getBytes()));
                    if (!mightContain) {
                        return false;
                    }
                }
                count++;
            }
        } else {
            obj = new Object[]{};
        }
        return dbHandler.checkExistence(obj,executionInfo);
    }

    public Operator getInMemoryEventTableOperator() {
        return inMemoryEventTableOperator;
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
}
