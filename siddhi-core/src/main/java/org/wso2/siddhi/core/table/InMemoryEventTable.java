/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.table;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.ZeroStreamEventConverter;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.core.util.parser.CollectionOperatorParser;
import org.wso2.siddhi.core.util.snapshot.Snapshotable;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * In-memory event table implementation of SiddhiQL.
 */
public class InMemoryEventTable implements EventTable, Snapshotable {

    private final TableDefinition tableDefinition;
    private final ExecutionPlanContext executionPlanContext;
    private final StreamEventCloner streamEventCloner;
    private final StreamEventPool streamEventPool;
    private final ZeroStreamEventConverter eventConverter = new ZeroStreamEventConverter();
    private Queue<StreamEvent> eventsList;
    private String elementId;

    // For indexed table.
    private String indexAttribute = null;
    private int indexPosition;
    private SortedMap<Object, StreamEvent> eventsMap;
    private ReadWriteLock readWriteLock=new ReentrantReadWriteLock();


    public InMemoryEventTable(TableDefinition tableDefinition, ExecutionPlanContext executionPlanContext) {
        this.tableDefinition = tableDefinition;
        this.executionPlanContext = executionPlanContext;
        MetaStreamEvent metaStreamEvent = new MetaStreamEvent();
        metaStreamEvent.addInputDefinition(tableDefinition);
        for (Attribute attribute : tableDefinition.getAttributeList()) {
            metaStreamEvent.addOutputData(attribute);
        }
        // Adding indexes.
        Annotation annotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_INDEX_BY,
                tableDefinition.getAnnotations());
        if (annotation != null) {
            if (annotation.getElements().size() > 1) {
                throw new OperationNotSupportedException(SiddhiConstants.ANNOTATION_INDEX_BY + " annotation contains " +
                        annotation.getElements().size() +
                        " elements, Siddhi in-memory table only supports indexing based on a single attribute");
            }
            if (annotation.getElements().size() == 0) {
                throw new ExecutionPlanValidationException(SiddhiConstants.ANNOTATION_INDEX_BY + " annotation contains "
                        + annotation.getElements().size() + " element");
            }
            indexAttribute = annotation.getElements().get(0).getValue();
            indexPosition = tableDefinition.getAttributePosition(indexAttribute);
            eventsMap = new ConcurrentSkipListMap<Object, StreamEvent>();
        } else {
            eventsList = new ConcurrentLinkedQueue<StreamEvent>();
        }
        streamEventPool = new StreamEventPool(metaStreamEvent, 10);
        streamEventCloner = new StreamEventCloner(metaStreamEvent, streamEventPool);
    }

    @Override
    public void init(TableDefinition tableDefinition, ExecutionPlanContext executionPlanContext) {
        if (elementId == null) {
            elementId = executionPlanContext.getElementIdGenerator().createNewId();
        }
        executionPlanContext.getSnapshotService().addSnapshotable(this);
    }

    @Override
    public TableDefinition getTableDefinition() {
        return tableDefinition;
    }

    @Override
    public void add(ComplexEventChunk addingEventChunk) {
        try{
            readWriteLock.writeLock().lock();
            addingEventChunk.reset();
            while (addingEventChunk.hasNext()) {
                ComplexEvent complexEvent = addingEventChunk.next();
                StreamEvent streamEvent = streamEventPool.borrowEvent();
                eventConverter.convertStreamEvent(complexEvent, streamEvent);
                if (indexAttribute != null) {
                    eventsMap.put(streamEvent.getOutputData()[indexPosition], streamEvent);
                } else {
                    eventsList.add(streamEvent);
                }
            }
        }finally {
            readWriteLock.writeLock().unlock();
        }

    }

    @Override
    public void delete(ComplexEventChunk deletingEventChunk, Operator operator) {
        try{
            readWriteLock.writeLock().lock();
            if (indexAttribute != null) {
                operator.delete(deletingEventChunk, eventsMap);
            } else {
                operator.delete(deletingEventChunk, eventsList);
            }
        }finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void update(ComplexEventChunk updatingEventChunk, Operator operator,
                                    int[] mappingPosition) {
        try{
            readWriteLock.writeLock().lock();
            if (indexAttribute != null) {
                operator.update(updatingEventChunk, eventsMap, mappingPosition);
            } else {
                operator.update(updatingEventChunk, eventsList, mappingPosition);
            }
        }finally {
            readWriteLock.writeLock().unlock();
        }

    }

    @Override
    public void overwriteOrAdd(ComplexEventChunk overwritingOrAddingEventChunk, Operator operator,
                               int[] mappingPosition) {
        if (indexAttribute != null) {
            operator.overwriteOrAdd(overwritingOrAddingEventChunk, eventsMap, mappingPosition);
        } else {
            operator.overwriteOrAdd(overwritingOrAddingEventChunk, eventsList, mappingPosition);
        }
    }

    @Override
    public boolean contains(ComplexEvent matchingEvent, Finder finder) {
        try{
            readWriteLock.readLock().lock();
            if (indexAttribute != null) {
                return finder.contains(matchingEvent, eventsMap);
            } else {
                return finder.contains(matchingEvent, eventsList);
            }
        }finally {
            readWriteLock.readLock().unlock();
        }

    }

    @Override
    public StreamEvent find(ComplexEvent matchingEvent, Finder finder) {
        try{
            readWriteLock.readLock().lock();
            if (indexAttribute != null) {
                return finder.find(matchingEvent, eventsMap, streamEventCloner);
            } else {
                return finder.find(matchingEvent, eventsList, streamEventCloner);
            }
        }finally {
            readWriteLock.readLock().unlock();
        }

    }

    @Override
    public Finder constructFinder(Expression expression, MetaComplexEvent matchingMetaComplexEvent,
                                  ExecutionPlanContext executionPlanContext,
                                  List<VariableExpressionExecutor> variableExpressionExecutors,
                                  Map<String, EventTable> eventTableMap, int matchingStreamIndex, long withinTime) {
        return CollectionOperatorParser.parse(expression, matchingMetaComplexEvent, executionPlanContext,
                variableExpressionExecutors, eventTableMap, matchingStreamIndex, tableDefinition, withinTime,
                indexAttribute, indexPosition);
    }

    @Override
    public Operator constructOperator(Expression expression, MetaComplexEvent metaComplexEvent,
                                      ExecutionPlanContext executionPlanContext,
                                      List<VariableExpressionExecutor> variableExpressionExecutors,
                                      Map<String, EventTable> eventTableMap, int matchingStreamIndex, long withinTime) {
        return CollectionOperatorParser.parse(expression, metaComplexEvent, executionPlanContext,
                variableExpressionExecutors, eventTableMap, matchingStreamIndex, tableDefinition, withinTime,
                indexAttribute, indexPosition);
    }

    @Override
    public Object[] currentState() {
        return new Object[]{eventsList, eventsMap};
    }

    @Override
    public void restoreState(Object[] state) {
        eventsList = (ConcurrentLinkedQueue<StreamEvent>) state[0];
        eventsMap = (ConcurrentSkipListMap<Object, StreamEvent>) state[1];
    }

    @Override
    public String getElementId() {
        return elementId;
    }
}
