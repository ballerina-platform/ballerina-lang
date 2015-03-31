/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.table.rdbms;


import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.collection.operator.Operator;

public class RDBMSOperator implements Operator {

    private ExpressionExecutor expressionExecutor;
    private DBConfiguration dbConfiguration;

    public RDBMSOperator(ExpressionExecutor expressionExecutor, DBConfiguration dbConfiguration) {
        this.expressionExecutor = expressionExecutor;
        this.dbConfiguration = dbConfiguration;
    }

    @Override
    public void delete(ComplexEventChunk<StreamEvent> deletingEventChunk, Object candidateEvents) {
        deletingEventChunk.reset();
        while (deletingEventChunk.hasNext()) {
            StreamEvent deletingEvent = deletingEventChunk.next();
            Object value = expressionExecutor.execute(deletingEvent);
            Object[] obj = new Object[]{value};
            dbConfiguration.deleteEvent(obj);
        }
    }

    @Override
    public void update(ComplexEventChunk<StreamEvent> updatingEventChunk, Object candidateEvents, int[] mappingPosition) {
        updatingEventChunk.reset();
        while (updatingEventChunk.hasNext()) {
            StreamEvent updatingEvent = updatingEventChunk.next();
            Object[] incomingEvent = updatingEvent.getOutputData();
            Object[] obj = new Object[incomingEvent.length + 1];
            Object value = expressionExecutor.execute(updatingEvent);
            System.arraycopy(incomingEvent, 0, obj, 0, incomingEvent.length);
            obj[incomingEvent.length] = value;
            dbConfiguration.updateEvent(obj);
        }
    }

    @Override
    public Finder cloneFinder() {
        return new RDBMSOperator(expressionExecutor, dbConfiguration);
    }

    @Override
    public StreamEvent find(ComplexEvent matchingEvent, Object candidateEvents, StreamEventCloner streamEventCloner) {

        Object[] obj;
        if (expressionExecutor != null) {
            Object value = expressionExecutor.execute(matchingEvent);
            obj = new Object[]{value};
        } else {
            obj = new Object[]{};
        }
        return dbConfiguration.selectEvent(obj);
    }

    @Override
    public boolean contains(ComplexEvent matchingEvent, Object candidateEvents) {
        Object[] obj;
        if (expressionExecutor != null) {
            Object value = expressionExecutor.execute(matchingEvent);
            obj = new Object[]{value};
        } else {
            obj = new Object[]{};
        }
        return dbConfiguration.checkExistence(obj);
    }
}
