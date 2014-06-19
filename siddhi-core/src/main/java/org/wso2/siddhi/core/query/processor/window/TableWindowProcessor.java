/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.query.processor.window;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.event.in.InListEvent;
import org.wso2.siddhi.core.executor.conditon.ConditionExecutor;
import org.wso2.siddhi.core.query.QueryPostProcessingElement;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.Iterator;

public class TableWindowProcessor extends WindowProcessor {

    private EventTable eventTable;

    public TableWindowProcessor(EventTable eventTable) {
        this.eventTable = eventTable;
    }

    @Override
    protected void processEvent(InEvent event) {
        // ignore
    }

    @Override
    protected void processEvent(InListEvent listEvent) {
        // ignore
    }

    @Override
    public Iterator<StreamEvent> iterator() {
        return eventTable.iterator();
    }

    @Override
    public Iterator<StreamEvent> iterator(String SQLPredicate) {
      return  eventTable.iterator(SQLPredicate);

    }

    @Override
    public Iterator<StreamEvent> iterator(StreamEvent streamEvent, ConditionExecutor conditionExecutor) {
        return  eventTable.iterator(streamEvent, conditionExecutor);
    }

    @Override
    protected Object[] currentState() {
        return new Object[0];
    }

    @Override
    protected void restoreState(Object[] data) {
        // ignore
    }

    @Override
    protected void init(Expression[] parameters, QueryPostProcessingElement nextProcessor, AbstractDefinition streamDefinition, String elementId, boolean async, SiddhiContext siddhiContext) {
        // ignore
    }

    @Override
    public void destroy(){

    }

}
