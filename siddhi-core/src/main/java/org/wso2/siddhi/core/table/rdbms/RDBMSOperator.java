/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.siddhi.core.table.rdbms;


import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.collection.operator.Operator;

import java.util.List;

public class RDBMSOperator implements Operator {

    private List<ExpressionExecutor> expressionExecutorList;
    private DBConfiguration dbConfiguration;

    public RDBMSOperator(List<ExpressionExecutor> expressionExecutorList, DBConfiguration dbConfiguration) {
        this.expressionExecutorList = expressionExecutorList;
        this.dbConfiguration = dbConfiguration;
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

            dbConfiguration.deleteEvent(obj);
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
            dbConfiguration.updateEvent(obj);
        }
    }

    @Override
    public Finder cloneFinder() {
        return new RDBMSOperator(expressionExecutorList, dbConfiguration);
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
        return dbConfiguration.selectEvent(obj);
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
                count++;
            }
        } else {
            obj = new Object[]{};
        }
        return dbConfiguration.checkExistence(obj);
    }
}
