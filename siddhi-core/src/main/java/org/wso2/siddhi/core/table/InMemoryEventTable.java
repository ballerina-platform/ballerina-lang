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
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.ZeroStreamEventConverter;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.holder.EventHolder;
import org.wso2.siddhi.core.table.holder.ListEventHolder;
import org.wso2.siddhi.core.table.holder.MapEventHolder;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.collection.OverwritingStreamEventExtractor;
import org.wso2.siddhi.core.util.collection.UpdateAttributeMapper;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaStateHolder;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.core.util.parser.OperatorParser;
import org.wso2.siddhi.core.util.snapshot.Snapshotable;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * In-memory event table implementation of SiddhiQL.
 */
public class InMemoryEventTable implements EventTable, Snapshotable {

    private TableDefinition tableDefinition;
    private StreamEventPool tableStreamEventPool;
    private StreamEventCloner tableStreamEventCloner;
    private final ZeroStreamEventConverter eventConverter = new ZeroStreamEventConverter();
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private EventHolder eventHolder;
    private String elementId;
    private int indexPosition;
    private boolean indexed = false;


    @Override
    public void init(TableDefinition tableDefinition, MetaStreamEvent tableMetaStreamEvent,
                     StreamEventPool tableStreamEventPool, StreamEventCloner tableStreamEventCloner,
                     ExecutionPlanContext executionPlanContext) {
        this.tableDefinition = tableDefinition;
        this.tableStreamEventPool = tableStreamEventPool;
        this.tableStreamEventCloner = tableStreamEventCloner;

        // indexes.
        Annotation indexByAnnotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_INDEX_BY,
                tableDefinition.getAnnotations());
        if (indexByAnnotation != null) {
            if (indexByAnnotation.getElements().size() > 1) {
                throw new OperationNotSupportedException(SiddhiConstants.ANNOTATION_INDEX_BY + " annotation contains " +
                        indexByAnnotation.getElements().size() +
                        " elements, Siddhi in-memory table only supports indexing based on a single attribute");
            }
            if (indexByAnnotation.getElements().size() == 0) {
                throw new ExecutionPlanValidationException(SiddhiConstants.ANNOTATION_INDEX_BY + " annotation contains "
                        + indexByAnnotation.getElements().size() + " element");
            }
            String indexAttribute = indexByAnnotation.getElements().get(0).getValue();
            indexPosition = tableDefinition.getAttributePosition(indexAttribute);
            eventHolder = new MapEventHolder();
            indexed = true;
        } else {
            eventHolder = new ListEventHolder();
        }

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
    public void add(ComplexEventChunk<StreamEvent> addingEventChunk) {
        try {
            readWriteLock.writeLock().lock();
            addingEventChunk.reset();
            while (addingEventChunk.hasNext()) {
                ComplexEvent complexEvent = addingEventChunk.next();
                StreamEvent streamEvent = tableStreamEventPool.borrowEvent();
                eventConverter.convertComplexEvent(complexEvent, streamEvent);
                if (indexed) {
                    ((MapEventHolder) eventHolder).getEventMap().put(streamEvent.getOutputData()[indexPosition], streamEvent);
                } else {
                    ((ListEventHolder) eventHolder).getEventList().add(streamEvent);
                }
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }

    }

    @Override
    public void delete(ComplexEventChunk<StateEvent> deletingEventChunk, Operator operator) {
        try {
            readWriteLock.writeLock().lock();
            operator.delete(deletingEventChunk, eventHolder.getEventCollection());
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void update(ComplexEventChunk<StateEvent> updatingEventChunk, Operator operator,
                       UpdateAttributeMapper[] updateAttributeMappers) {
        try {
            readWriteLock.writeLock().lock();
            operator.update(updatingEventChunk, eventHolder.getEventCollection(), updateAttributeMappers);
        } finally {
            readWriteLock.writeLock().unlock();
        }

    }

    @Override
    public void overwriteOrAdd(ComplexEventChunk<StateEvent> overwritingOrAddingEventChunk, Operator operator,
                               UpdateAttributeMapper[] updateAttributeMappers,
                               OverwritingStreamEventExtractor overwritingStreamEventExtractor) {
        try {
            readWriteLock.writeLock().lock();
            ComplexEventChunk<StreamEvent> failedEvents = operator.overwriteOrAdd(overwritingOrAddingEventChunk,
                    eventHolder.getEventCollection(), updateAttributeMappers, overwritingStreamEventExtractor);
            while (failedEvents.hasNext()) {
                ComplexEvent complexEvent = failedEvents.next();
                StreamEvent streamEvent = tableStreamEventPool.borrowEvent();
                eventConverter.convertComplexEvent(complexEvent, streamEvent);
                if (indexed) {
                    ((MapEventHolder) eventHolder).getEventMap().put(streamEvent.getOutputData()[indexPosition], streamEvent);
                } else {
                    ((ListEventHolder) eventHolder).getEventList().add(streamEvent);
                }
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }

    }

    @Override
    public boolean contains(StateEvent matchingEvent, Finder finder) {
        try {
            readWriteLock.readLock().lock();
            return finder.contains(matchingEvent, eventHolder.getEventCollection());
        } finally {
            readWriteLock.readLock().unlock();
        }

    }

    @Override
    public StreamEvent find(StateEvent matchingEvent, Finder finder) {
        try {
            readWriteLock.readLock().lock();
            return finder.find(matchingEvent, eventHolder.getEventCollection(), tableStreamEventCloner);
        } finally {
            readWriteLock.readLock().unlock();
        }

    }

    @Override
    public Finder constructFinder(Expression expression, MatchingMetaStateHolder matchingMetaStateHolder,
                                  ExecutionPlanContext executionPlanContext,
                                  List<VariableExpressionExecutor> variableExpressionExecutors,
                                  Map<String, EventTable> eventTableMap) {
        return OperatorParser.constructOperator(eventHolder.getEventCollection(), expression, matchingMetaStateHolder,
                executionPlanContext, variableExpressionExecutors, eventTableMap);
    }


    @Override
    public Operator constructOperator(Expression expression, MatchingMetaStateHolder matchingMetaStateHolder,
                                      ExecutionPlanContext executionPlanContext,
                                      List<VariableExpressionExecutor> variableExpressionExecutors,
                                      Map<String, EventTable> eventTableMap) {
        return OperatorParser.constructOperator(eventHolder.getEventCollection(), expression, matchingMetaStateHolder,
                executionPlanContext, variableExpressionExecutors, eventTableMap);
    }


    @Override
    public Object[] currentState() {
        return new Object[]{eventHolder};
    }

    @Override
    public void restoreState(Object[] state) {
        eventHolder = (EventHolder) state[0];
    }

    @Override
    public String getElementId() {
        return elementId;
    }
}
