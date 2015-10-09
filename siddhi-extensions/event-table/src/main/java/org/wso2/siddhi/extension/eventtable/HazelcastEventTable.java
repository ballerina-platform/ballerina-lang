/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.extension.eventtable;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IdGenerator;
import org.apache.log4j.Logger;
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
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.extension.eventtable.hazelcast.HazelcastEventTableConstants;
import org.wso2.siddhi.extension.eventtable.hazelcast.HazelcastOperatorParser;
import org.wso2.siddhi.extension.eventtable.hazelcast.internal.ds.HazelcastEventTableServiceValueHolder;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class HazelcastEventTable implements EventTable {
    private static final Logger logger = Logger.getLogger(HazelcastEventTable.class);
    private final ZeroStreamEventConverter eventConverter = new ZeroStreamEventConverter();
    private TableDefinition tableDefinition;
    private ExecutionPlanContext executionPlanContext;
    private StreamEventCloner streamEventCloner;
    private StreamEventPool streamEventPool;
    private HazelcastInstance hcInstance;
//    private ConcurrentMap<Object, StreamEvent> eventMap = null;
    private ConcurrentMap<Object, StreamEvent> eventMap = null;
    private String indexAttribute = null;
    private IdGenerator idGenerator = null;
    private int indexPosition;
    private String elementId;

    /**
     * Event Table initialization method, it checks the annotation and do necessary pre configuration tasks.
     *
     * @param tableDefinition      Definition of event table
     * @param executionPlanContext ExecutionPlan related meta information
     */
    @Override
    public void init(TableDefinition tableDefinition, ExecutionPlanContext executionPlanContext) {
        this.tableDefinition = tableDefinition;
        this.executionPlanContext = executionPlanContext;

        Annotation fromAnnotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_FROM, tableDefinition.getAnnotations());
        String clusterName = fromAnnotation.getElement(HazelcastEventTableConstants.ANNOTATION_ELEMENT_CLUSTER_NAME);
        String clusterPassword = fromAnnotation.getElement(HazelcastEventTableConstants.ANNOTATION_ELEMENT_CLUSTER_PASSWORD);
        String clusterAddresses = fromAnnotation.getElement(HazelcastEventTableConstants.ANNOTATION_ELEMENT_CLUSTER_ADDRESSES);
        String instanceName = HazelcastEventTableConstants.HAZELCAST_INSTANCE_PREFIX + this.executionPlanContext.getName();

        hcInstance = getHazelcastInstance(clusterName, clusterPassword, clusterAddresses, instanceName);
        idGenerator = hcInstance.getIdGenerator(HazelcastEventTableConstants.HAZELCAST_ID_GENERATOR_PREFIX + tableDefinition.getId());
        eventMap = hcInstance.getMap(HazelcastEventTableConstants.HAZELCAST_COLLECTION_INSTANCE_PREFIX +
                executionPlanContext.getName() + '_' + tableDefinition.getId());

        MetaStreamEvent metaStreamEvent = new MetaStreamEvent();
        metaStreamEvent.addInputDefinition(tableDefinition);
        for (Attribute attribute : tableDefinition.getAttributeList()) {
            metaStreamEvent.addOutputData(attribute);
        }

        Annotation annotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_INDEX_BY, tableDefinition.getAnnotations());
        if (annotation != null) {
            if (annotation.getElements().size() != 1) {
                throw new OperationNotSupportedException(SiddhiConstants.ANNOTATION_INDEX_BY + " annotation of table " +
                        tableDefinition.getId() + " contains " + annotation.getElements().size() +
                        " elements, Siddhi Hazelcast event table only supports indexing based on a single attribute");
            }
            indexAttribute = annotation.getElements().get(0).getValue();
            indexPosition = tableDefinition.getAttributePosition(indexAttribute);
        }

        streamEventPool = new StreamEventPool(metaStreamEvent, HazelcastEventTableConstants.STREAM_EVENT_POOL_SIZE);
        streamEventCloner = new StreamEventCloner(metaStreamEvent, streamEventPool);
        if (elementId == null) {
            elementId = executionPlanContext.getElementIdGenerator().createNewId();
        }
    }

    /**
     * Called to get the most suitable Hazelcast Instance for the given set of parameters
     *
     * @param clusterName      Hazelcast cluster name
     * @param clusterPassword  Hazelcast cluster password
     * @param clusterAddresses Hazelcast node addresses (ip:port)
     * @param instanceName     Hazelcast instance name
     * @return Hazelcast Instance
     */
    protected HazelcastInstance getHazelcastInstance(String clusterName, String clusterPassword, String clusterAddresses, String instanceName) {
        HazelcastInstance hazelcastInstance;

        if (clusterAddresses == null) {
            if (HazelcastEventTableServiceValueHolder.getHazelcastInstance() != null) {
                // take instance from osgi
                hazelcastInstance = HazelcastEventTableServiceValueHolder.getHazelcastInstance();
                logger.info("shared hazelcast server instance retrieved : " + hazelcastInstance.getName());
            } else {
                // create a new server with default cluster name
                Config config = new Config();
                config.setInstanceName(instanceName);
                config.setProperty("hazelcast.logging.type", "log4j");

                if (clusterName != null && !clusterName.isEmpty()) {
                    config.getGroupConfig().setName(clusterName);
                }

                if (clusterPassword != null && !clusterPassword.isEmpty()) {
                    config.getGroupConfig().setPassword(clusterPassword);
                }

                hazelcastInstance = Hazelcast.getOrCreateHazelcastInstance(config);
                logger.info("hazelcast server instance started: " + instanceName);
            }
        } else {
            // client
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.setProperty("hazelcast.logging.type", "log4j");

            if (clusterName != null && !clusterName.isEmpty()) {
                clientConfig.getGroupConfig().setName(clusterName);
            }

            if (clusterPassword != null && !clusterPassword.isEmpty()) {
                clientConfig.getGroupConfig().setPassword(clusterPassword);
            }

            clientConfig.setNetworkConfig(clientConfig.getNetworkConfig().addAddress(clusterAddresses.split(",")));
            hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);
        }
        return hazelcastInstance;
    }

    @Override
    public TableDefinition getTableDefinition() {
        return tableDefinition;
    }

    /**
     * Called when adding an event to the event table
     *
     * @param addingEventChunk input event list
     */
    @Override
    public synchronized void add(ComplexEventChunk addingEventChunk) {
        addingEventChunk.reset();
        while (addingEventChunk.hasNext()) {
            ComplexEvent complexEvent = addingEventChunk.next();
            StreamEvent streamEvent = streamEventPool.borrowEvent();
            eventConverter.convertStreamEvent(complexEvent, streamEvent);
            if (indexAttribute != null) {
                eventMap.put(streamEvent.getOutputData()[indexPosition], streamEvent);
            } else {
                eventMap.put(idGenerator.newId(), streamEvent);
            }
        }
    }

    /**
     * Called when deleting an event chunk from event table
     *
     * @param deletingEventChunk Event list for deletion
     * @param operator           Operator that perform Hazelcast related operations
     */
    @Override
    public synchronized void delete(ComplexEventChunk deletingEventChunk, Operator operator) {
        operator.delete(deletingEventChunk, eventMap);
    }

    /**
     * Called when updating the event table entries
     *
     * @param updatingEventChunk Event list that needs to be updated
     * @param operator           Operator that perform Hazelcast related operations
     */
    @Override
    public synchronized void update(ComplexEventChunk updatingEventChunk, Operator operator, int[] mappingPosition) {
        operator.update(updatingEventChunk, eventMap, mappingPosition);
    }

    /**
     * Called when having "in" condition, to check the existence of the event
     *
     * @param matchingEvent Event that need to be check for existence
     * @param finder        Operator that perform Hazelcast related search
     * @return boolean      whether event exists or not
     */
    @Override
    public synchronized boolean contains(ComplexEvent matchingEvent, Finder finder) {
        return finder.contains(matchingEvent, eventMap);
    }

    /**
     * Called to find a event from event table
     *
     * @param matchingEvent the event to be matched with the events at the processor
     * @param finder        the execution element responsible for finding the corresponding events that matches
     *                      the matchingEvent based on pool of events at Processor
     * @return StreamEvent  event found
     */
    @Override
    public synchronized StreamEvent find(ComplexEvent matchingEvent, Finder finder) {
        return finder.find(matchingEvent, eventMap, streamEventCloner);
    }

    /**
     * Called to construct a operator to perform search operations
     *
     * @param expression                  the matching expression
     * @param metaComplexEvent            the meta structure of the incoming matchingEvent
     * @param executionPlanContext        current execution plan context
     * @param variableExpressionExecutors the list of variable ExpressionExecutors already created
     * @param eventTableMap               map of event tables
     * @param matchingStreamIndex         the stream index of the incoming matchingEvent
     * @param withinTime                  the maximum time gap between the events to be matched
     * @return
     */
    @Override
    public Finder constructFinder(Expression expression, MetaComplexEvent metaComplexEvent, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap, int matchingStreamIndex, long withinTime) {
        return HazelcastOperatorParser.parse(expression, metaComplexEvent, executionPlanContext, variableExpressionExecutors, eventTableMap, matchingStreamIndex, tableDefinition, withinTime, indexAttribute);
    }

    /**
     * Called to construct a operator to perform delete and update operations
     *
     * @param expression                  the matching expression
     * @param metaComplexEvent            the meta structure of the incoming matchingEvent
     * @param executionPlanContext        current execution plan context
     * @param variableExpressionExecutors the list of variable ExpressionExecutors already created
     * @param eventTableMap               map of event tables
     * @param matchingStreamIndex         the stream index of the incoming matchingEvent
     * @param withinTime                  the maximum time gap between the events to be matched
     * @return
     */
    @Override
    public Operator constructOperator(Expression expression, MetaComplexEvent metaComplexEvent, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap, int matchingStreamIndex, long withinTime) {
        return HazelcastOperatorParser.parse(expression, metaComplexEvent, executionPlanContext, variableExpressionExecutors, eventTableMap, matchingStreamIndex, tableDefinition, withinTime, indexAttribute);
    }
}
