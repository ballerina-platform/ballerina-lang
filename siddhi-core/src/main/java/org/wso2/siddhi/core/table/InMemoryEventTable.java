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

package org.wso2.siddhi.core.table;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.util.finder.Finder;
import org.wso2.siddhi.core.util.parser.SimpleFinderParser;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created on 1/18/15.
 */
public class InMemoryEventTable implements EventTable {

    private final TableDefinition tableDefinition;
    private final ExecutionPlanContext executionPlanContext;
    private final LinkedList<StreamEvent> list = new LinkedList<StreamEvent>();
    private final StreamEventCloner streamEventCloner;
    private final StreamEventPool streamEventPool;


    public InMemoryEventTable(TableDefinition tableDefinition, ExecutionPlanContext executionPlanContext) {

        this.tableDefinition = tableDefinition;
        this.executionPlanContext = executionPlanContext;
        MetaStreamEvent metaStreamEvent = new MetaStreamEvent();
        metaStreamEvent.addInputDefinition(tableDefinition);
        for (Attribute attribute : tableDefinition.getAttributeList()) {
            metaStreamEvent.addOutputData(attribute);
        }
        streamEventPool = new StreamEventPool(metaStreamEvent, 10);
        streamEventCloner = new StreamEventCloner(metaStreamEvent, streamEventPool);
    }

    @Override
    public TableDefinition getTableDefinition() {
        return tableDefinition;
    }

    public synchronized void add(ComplexEventChunk<StreamEvent> addingEventChunk) {
        addingEventChunk.reset();
        while (addingEventChunk.hasNext()) {
            StreamEvent streamEvent = addingEventChunk.next();
            list.add(streamEventCloner.copyStreamEvent(streamEvent));
        }
    }

    public synchronized void delete(ComplexEventChunk<StreamEvent> deletingEventChunk, Finder finder) {

        deletingEventChunk.reset();
        while (deletingEventChunk.hasNext()) {
            StreamEvent matchStreamEvent = deletingEventChunk.next();
            finder.setMatchingEvent(matchStreamEvent);
            Iterator<StreamEvent> iterator = list.iterator();
            while (iterator.hasNext()) {
                StreamEvent streamEvent = iterator.next();
                if (finder.execute(streamEvent)) {
                    iterator.remove();
                }
            }
            finder.setMatchingEvent(null);
        }


    }

    public synchronized void update(ComplexEventChunk<StreamEvent> updatingEventChunk, Finder finder, int[] mappingPosition) {

        updatingEventChunk.reset();
        while (updatingEventChunk.hasNext()) {
            StreamEvent matchStreamEvent = updatingEventChunk.next();
            finder.setMatchingEvent(matchStreamEvent);
            Iterator<StreamEvent> iterator = list.iterator();
            while (iterator.hasNext()) {
                StreamEvent streamEvent = iterator.next();
                if (finder.execute(streamEvent)) {
                    for (int i = 0, size = mappingPosition.length; i < size; i++) {
                        streamEvent.setOutputData(matchStreamEvent.getOutputData()[i], mappingPosition[i]);
                    }
                }
            }
            finder.setMatchingEvent(null);
        }

    }


    public synchronized boolean contains(ComplexEvent matchingEvent, Finder finder) {
        finder.setMatchingEvent(matchingEvent);
        for (StreamEvent streamEvent : list) {
            if (finder.execute(streamEvent)) {
                return true;
            }
        }
        finder.setMatchingEvent(null);
        return false;
    }

    public synchronized StreamEvent find(ComplexEvent matchingEvent, Finder finder) {    //todo optimize
        finder.setMatchingEvent(matchingEvent);
        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>();
        for (StreamEvent streamEvent : list) {
            if (finder.execute(streamEvent)) {
                returnEventChunk.add(streamEventCloner.copyStreamEvent(streamEvent));
            }
        }
        finder.setMatchingEvent(null);
        return returnEventChunk.getFirst();
    }

    public Finder constructFinder(Expression expression, MetaComplexEvent metaComplexEvent, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap, int matchingStreamIndex) {
        return SimpleFinderParser.parse(expression, metaComplexEvent, executionPlanContext, variableExpressionExecutors, eventTableMap, matchingStreamIndex, tableDefinition);

    }

}
