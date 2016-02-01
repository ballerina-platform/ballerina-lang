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

package org.wso2.siddhi.extension.eventtable;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
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

/**
 * Hazelcast event table implementation of SiddhiQL.
 */
public class HazelcastEventTable implements EventTable {
    private static final Logger logger = Logger.getLogger(HazelcastEventTable.class);
    private final ZeroStreamEventConverter eventConverter = new ZeroStreamEventConverter();
    private TableDefinition tableDefinition;
    private ExecutionPlanContext executionPlanContext;
    private StreamEventCloner streamEventCloner;
    private StreamEventPool streamEventPool;
    private List<StreamEvent> eventsList = null;
    private String elementId;

    // For Indexed table
    private ConcurrentMap<Object, StreamEvent> eventsMap = null;
    private String indexAttribute = null;
    private int indexPosition;

    /**
     * Event Table initialization method, it checks the annotation and do necessary pre configuration tasks.
     *
     * @param tableDefinition      Definition of event table.
     * @param executionPlanContext ExecutionPlan related meta information.
     */
    @Override
    public void init(TableDefinition tableDefinition, ExecutionPlanContext executionPlanContext) {
        this.tableDefinition = tableDefinition;
        this.executionPlanContext = executionPlanContext;

        Annotation fromAnnotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_FROM,
                tableDefinition.getAnnotations());
        String clusterName = fromAnnotation.getElement(HazelcastEventTableConstants.ANNOTATION_ELEMENT_CLUSTER_NAME);
        String clusterPassword = fromAnnotation.getElement(
                HazelcastEventTableConstants.ANNOTATION_ELEMENT_CLUSTER_PASSWORD);
        String clusterAddresses = fromAnnotation.getElement(
                HazelcastEventTableConstants.ANNOTATION_ELEMENT_CLUSTER_ADDRESSES);
        String instanceName = fromAnnotation.getElement(HazelcastEventTableConstants.ANNOTATION_ELEMENT_INSTANCE_NAME);
        HazelcastInstance hcInstance = getHazelcastInstance(clusterName, clusterPassword, clusterAddresses, instanceName);

        MetaStreamEvent metaStreamEvent = new MetaStreamEvent();
        metaStreamEvent.addInputDefinition(tableDefinition);
        for (Attribute attribute : tableDefinition.getAttributeList()) {
            metaStreamEvent.addOutputData(attribute);
        }

        Annotation annotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_INDEX_BY,
                tableDefinition.getAnnotations());
        if (annotation != null) {
            if (annotation.getElements().size() != 1) {
                throw new OperationNotSupportedException(SiddhiConstants.ANNOTATION_INDEX_BY + " annotation of table " +
                        tableDefinition.getId() + " contains " + annotation.getElements().size() +
                        " elements, Siddhi Hazelcast event table only supports indexing based on a single attribute");
            }
            indexAttribute = annotation.getElements().get(0).getValue();
            indexPosition = tableDefinition.getAttributePosition(indexAttribute);
            eventsMap = hcInstance.getMap(HazelcastEventTableConstants.HAZELCAST_MAP_INSTANCE_PREFIX +
                    executionPlanContext.getName() + '_' + tableDefinition.getId());
        } else {
            eventsList = hcInstance.getList(HazelcastEventTableConstants.HAZELCAST_LIST_INSTANCE_PREFIX +
                    executionPlanContext.getName() + '_' + tableDefinition.getId());
        }
        streamEventPool = new StreamEventPool(metaStreamEvent, HazelcastEventTableConstants.STREAM_EVENT_POOL_SIZE);
        streamEventCloner = new StreamEventCloner(metaStreamEvent, streamEventPool);
        if (elementId == null) {
            elementId = executionPlanContext.getElementIdGenerator().createNewId();
        }
    }

    /**
     * Called to get the most suitable Hazelcast Instance for the given set of parameters.
     *
     * @param clusterName      Hazelcast cluster name.
     * @param clusterPassword  Hazelcast cluster password.
     * @param clusterAddresses Hazelcast node addresses (ip:port).
     * @param instanceName     Hazelcast instance name.
     * @return Hazelcast Instance
     */
    protected HazelcastInstance getHazelcastInstance(String clusterName, String clusterPassword, String clusterAddresses,
                                                     String instanceName) {
        HazelcastInstance hazelcastInstance;
        if (instanceName == null) {
            instanceName = HazelcastEventTableConstants.HAZELCAST_INSTANCE_PREFIX + this.executionPlanContext.getName();
        }

        if (clusterAddresses == null) {
            if (HazelcastEventTableServiceValueHolder.getHazelcastInstance() != null) {
                // Take instance from osgi.
                hazelcastInstance = HazelcastEventTableServiceValueHolder.getHazelcastInstance();
                logger.info("Shared hazelcast server instance retrieved : " + hazelcastInstance.getName());
            } else {
                // Create a new server with default cluster name.
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
                logger.info("Hazelcast server instance started: " + instanceName);
            }
        } else {
            // Client mode.
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
     * Called when adding an event to the event table.
     *
     * @param addingEventChunk input event list.
     */
    @Override
    public synchronized void add(ComplexEventChunk addingEventChunk) {
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
    }

    /**
     * Called when deleting an event chunk from event table.
     *
     * @param deletingEventChunk Event list for deletion.
     * @param operator           Operator that perform Hazelcast related operations.
     */
    @Override
    public synchronized void delete(ComplexEventChunk deletingEventChunk, Operator operator) {
        if (indexAttribute != null) {
            operator.delete(deletingEventChunk, eventsMap);
        } else {
            operator.delete(deletingEventChunk, eventsList);
        }
    }

    /**
     * Called when updating the event table entries.
     *
     * @param updatingEventChunk Event list that needs to be updated.
     * @param operator           Operator that perform Hazelcast related operations.
     */
    @Override
    public synchronized void update(ComplexEventChunk updatingEventChunk, Operator operator, int[] mappingPosition) {
        if (indexAttribute != null) {
            operator.update(updatingEventChunk, eventsMap, mappingPosition);
        } else {
            operator.update(updatingEventChunk, eventsList, mappingPosition);
        }
    }

    /**
     * Called when insert or overwriting the event table entries.
     *
     * @param overwritingOrAddingEventChunk Event list that needs to be inserted or updated.
     * @param operator                      Operator that perform Hazelcast related operations.
     */
    @Override
    public void overwriteOrAdd(ComplexEventChunk overwritingOrAddingEventChunk, Operator operator, int[] mappingPosition) {
        if (indexAttribute != null) {
            operator.overwriteOrAdd(overwritingOrAddingEventChunk, eventsMap, mappingPosition);
        } else {
            operator.overwriteOrAdd(overwritingOrAddingEventChunk, eventsList, mappingPosition);
        }
    }

    /**
     * Called when having "in" condition, to check the existence of the event.
     *
     * @param matchingEvent Event that need to be check for existence.
     * @param finder        Operator that perform Hazelcast related search.
     * @return boolean      whether event exists or not.
     */
    @Override
    public synchronized boolean contains(ComplexEvent matchingEvent, Finder finder) {
        if (indexAttribute != null) {
            return finder.contains(matchingEvent, eventsMap);
        } else {
            return finder.contains(matchingEvent, eventsList);
        }
    }

    /**
     * Called to find a event from event table.
     *
     * @param matchingEvent the event to be matched with the events at the processor.
     * @param finder        the execution element responsible for finding the corresponding events that matches.
     *                      the matchingEvent based on pool of events at Processor.
     * @return StreamEvent  event found.
     */
    @Override
    public synchronized StreamEvent find(ComplexEvent matchingEvent, Finder finder) {
        if (indexAttribute != null) {
            return finder.find(matchingEvent, eventsMap, streamEventCloner);
        } else {
            return finder.find(matchingEvent, eventsList, streamEventCloner);
        }
    }

    /**
     * Called to construct a operator to perform search operations.
     *
     * @param expression                  the matching expression.
     * @param matchingMetaComplexEvent    the meta structure of the incoming matchingEvent.
     * @param executionPlanContext        current execution plan context.
     * @param variableExpressionExecutors the list of variable ExpressionExecutors already created.
     * @param eventTableMap               map of event tables.
     * @param matchingStreamIndex         the stream index of the incoming matchingEvent.
     * @param withinTime                  the maximum time gap between the events to be matched.
     * @return HazelcastOperator.
     */
    @Override
    public Finder constructFinder(Expression expression, MetaComplexEvent matchingMetaComplexEvent,
                                  ExecutionPlanContext executionPlanContext,
                                  List<VariableExpressionExecutor> variableExpressionExecutors,
                                  Map<String, EventTable> eventTableMap, int matchingStreamIndex, long withinTime) {
        return HazelcastOperatorParser.parse(expression, matchingMetaComplexEvent, executionPlanContext,
                variableExpressionExecutors, eventTableMap, matchingStreamIndex, tableDefinition, withinTime,
                indexAttribute, indexPosition);
    }

    /**
     * Called to construct a operator to perform delete and update operations.
     *
     * @param expression                  the matching expression.
     * @param metaComplexEvent            the meta structure of the incoming matchingEvent.
     * @param executionPlanContext        current execution plan context.
     * @param variableExpressionExecutors the list of variable ExpressionExecutors already created.
     * @param eventTableMap               map of event tables.
     * @param matchingStreamIndex         the stream index of the incoming matchingEvent.
     * @param withinTime                  the maximum time gap between the events to be matched.
     * @return HazelcastOperator
     */
    @Override
    public Operator constructOperator(Expression expression, MetaComplexEvent metaComplexEvent,
                                      ExecutionPlanContext executionPlanContext,
                                      List<VariableExpressionExecutor> variableExpressionExecutors,
                                      Map<String, EventTable> eventTableMap, int matchingStreamIndex, long withinTime) {
        return HazelcastOperatorParser.parse(expression, metaComplexEvent, executionPlanContext,
                variableExpressionExecutors, eventTableMap, matchingStreamIndex, tableDefinition, withinTime,
                indexAttribute, indexPosition);
    }
}
