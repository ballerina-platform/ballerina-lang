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
import java.util.TreeMap;

public class InMemoryEventTable implements EventTable, Snapshotable {

    private final TableDefinition tableDefinition;
    private final ExecutionPlanContext executionPlanContext;
    private LinkedList<StreamEvent> list = new LinkedList<StreamEvent>();
    private TreeMap<Object, StreamEvent> treeMap = new TreeMap<Object, StreamEvent>();
    private String indexAttribute = null;
    private int indexPosition;
    private final StreamEventCloner streamEventCloner;
    private final StreamEventPool streamEventPool;
    private final ZeroStreamEventConverter eventConverter = new ZeroStreamEventConverter();
    private String elementId;


    public InMemoryEventTable(TableDefinition tableDefinition, ExecutionPlanContext executionPlanContext) {

        this.tableDefinition = tableDefinition;
        this.executionPlanContext = executionPlanContext;
        MetaStreamEvent metaStreamEvent = new MetaStreamEvent();
        metaStreamEvent.addInputDefinition(tableDefinition);
        for (Attribute attribute : tableDefinition.getAttributeList()) {
            metaStreamEvent.addOutputData(attribute);
        }

        //Adding indexes
        Annotation annotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_INDEX_BY,
                tableDefinition.getAnnotations());
        if (annotation != null) {
            if (annotation.getElements().size() > 1) {
                throw new OperationNotSupportedException(SiddhiConstants.ANNOTATION_INDEX_BY + " annotation contains " + annotation.getElements().size() + " elements, Siddhi in-memory table only supports indexing based on a single attribute");
            }
            if (annotation.getElements().size() == 0) {
                throw new ExecutionPlanValidationException(SiddhiConstants.ANNOTATION_INDEX_BY + " annotation contains " + annotation.getElements().size() + " element");
            }
            indexAttribute = annotation.getElements().get(0).getValue();
            indexPosition = tableDefinition.getAttributePosition(indexAttribute);
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

    public synchronized void add(ComplexEventChunk addingEventChunk) {
        addingEventChunk.reset();
        while (addingEventChunk.hasNext()) {
            ComplexEvent complexEvent = addingEventChunk.next();
            StreamEvent streamEvent = streamEventPool.borrowEvent();
            eventConverter.convertStreamEvent(complexEvent, streamEvent);
            if (indexAttribute != null) {
                treeMap.put(streamEvent.getOutputData()[indexPosition], streamEvent);
            } else {
                list.add(streamEvent);
            }
        }
    }

    public synchronized void delete(ComplexEventChunk deletingEventChunk, Operator operator) {
        if (indexAttribute != null) {
            operator.delete(deletingEventChunk, treeMap);
        } else {
            operator.delete(deletingEventChunk, list);
        }

    }

    public synchronized void update(ComplexEventChunk updatingEventChunk, Operator operator, int[] mappingPosition) {
        if (indexAttribute != null) {
            operator.update(updatingEventChunk, treeMap, mappingPosition);
        } else {
            operator.update(updatingEventChunk, list, mappingPosition);
        }
    }


    public synchronized boolean contains(ComplexEvent matchingEvent, Finder finder) {
        if (indexAttribute != null) {
            return finder.contains(matchingEvent, treeMap);
        } else {
            return finder.contains(matchingEvent, list);
        }
    }

    public synchronized StreamEvent find(ComplexEvent matchingEvent, Finder finder) {
        if (indexAttribute != null) {
            return finder.find(matchingEvent, treeMap, streamEventCloner);
        } else {
            return finder.find(matchingEvent, list, streamEventCloner);
        }

    }

    public Finder constructFinder(Expression expression, MetaComplexEvent metaComplexEvent, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap, int matchingStreamIndex, long withinTime) {
        return CollectionOperatorParser.parse(expression, metaComplexEvent, executionPlanContext, variableExpressionExecutors, eventTableMap, matchingStreamIndex, tableDefinition, withinTime, indexAttribute);
    }

    @Override
    public Operator constructOperator(Expression expression, MetaComplexEvent metaComplexEvent, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap, int matchingStreamIndex, long withinTime) {
        return CollectionOperatorParser.parse(expression, metaComplexEvent, executionPlanContext, variableExpressionExecutors, eventTableMap, matchingStreamIndex, tableDefinition, withinTime, indexAttribute);
    }

    @Override
    public Object[] currentState() {
        return new Object[]{list, treeMap};
    }

    @Override
    public void restoreState(Object[] state) {
        list = (LinkedList<StreamEvent>) state[0];
        treeMap = (TreeMap<Object, StreamEvent>) state[1];
    }

    @Override
    public String getElementId() {
        return elementId;
    }
}
