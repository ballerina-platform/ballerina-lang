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

package org.wso2.siddhi.extension.table;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.apache.log4j.Logger;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.ZeroStreamEventConverter;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.table.holder.EventHolder;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.collection.AddingStreamEventExtractor;
import org.wso2.siddhi.core.util.collection.UpdateAttributeMapper;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.extension.table.hazelcast.HazelcastCollectionEventHolder;
import org.wso2.siddhi.extension.table.hazelcast.HazelcastOperatorParser;
import org.wso2.siddhi.extension.table.hazelcast.HazelcastPrimaryKeyEventHolder;
import org.wso2.siddhi.extension.table.hazelcast.HazelcastTableConstants;
import org.wso2.siddhi.extension.table.hazelcast.internal.ds.HazelcastTableServiceValueHolder;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.util.List;
import java.util.Map;

/**
 * Hazelcast event table implementation of SiddhiQL.
 */
@Extension(
        name = "hazelcast",
        namespace = "store",
        description = "",
        parameters = {
                @Parameter(name = "cluster.name",
                        description = "Hazelcast cluster name [Optional]  (i.e cluster.name='cluster_a').",
                        type = {DataType.STRING}),
                @Parameter(name = "cluster.password",
                        description = "Hazelcast cluster/group password [Optional] " +
                                "(i.e cluster.password='pass@cluster_a').",
                        type = {DataType.STRING}),
                @Parameter(name = "cluster.addresses",
                        description = "Hazelcast cluster addresses (ip:port) as a comma separated string [Optional, " +
                                "client mode only] (i.e cluster.addresses='192.168.1.1:5700,192.168.1.2:5700').",
                        type = {DataType.STRING}),
                @Parameter(name = "well.known.addresses",
                        description = "Hazelcast WKAs (ip) as a comma separated string [Optional, server mode only] " +
                                "(i.e well.known.addresses='192.168.1.1,192.168.1.2').",
                        type = {DataType.STRING}),
                @Parameter(name = "collection.name",
                        description = "Hazelcast collection object name [Optional,  can be used to share single " +
                                "table between multiple EPs] (i.e collection.name='stockTable').",
                        type = {DataType.STRING})
        }
)
public class HazelcastTable implements Table {
    private static final Logger logger = Logger.getLogger(HazelcastTable.class);
    private final ZeroStreamEventConverter eventConverter = new ZeroStreamEventConverter();
    private TableDefinition tableDefinition;
    private SiddhiAppContext siddhiAppContext;
    private StreamEventCloner tableStreamEventCloner;
    private String elementId;
    private EventHolder eventHolder = null;

    /**
     * Event Table initialization method, it checks the annotation and do necessary pre configuration tasks.
     *
     * @param tableDefinition      Definition of event table.
     * @param storeEventPool
     * @param storeEventCloner
     * @param configReader
     * @param siddhiAppContext SiddhiApp related meta information.
     */
    @Override
    public void init(TableDefinition tableDefinition,
                     StreamEventPool storeEventPool, StreamEventCloner storeEventCloner,
                     ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        this.tableDefinition = tableDefinition;
        this.tableStreamEventCloner = storeEventCloner;
        this.siddhiAppContext = siddhiAppContext;
        String clusterName;
        String clusterPassword;
        String hosts;
        String collectionName;
        boolean serverMode;

        Annotation fromAnnotation = AnnotationHelper.getAnnotation(
                SiddhiConstants.ANNOTATION_STORE, tableDefinition.getAnnotations());
        //// TODO: 12/6/16 This must be deprecated

        clusterName = fromAnnotation.getElement(
                HazelcastTableConstants.ANNOTATION_ELEMENT_HAZELCAST_CLUSTER_NAME);
        clusterPassword = fromAnnotation.getElement(
                HazelcastTableConstants.ANNOTATION_ELEMENT_HAZELCAST_CLUSTER_PASSWORD);
        hosts = fromAnnotation.getElement(
                HazelcastTableConstants.ANNOTATION_ELEMENT_HAZELCAST_CLUSTER_ADDRESSES);
        collectionName = fromAnnotation.getElement(
                HazelcastTableConstants.ANNOTATION_ELEMENT_HAZELCAST_CLUSTER_COLLECTION);

        serverMode = (hosts == null || hosts.isEmpty());
        if (serverMode) {
            hosts = fromAnnotation.getElement(
                    HazelcastTableConstants.ANNOTATION_ELEMENT_HAZELCAST_WELL_KNOWN_ADDRESSES);
        }

        Annotation annotation = AnnotationHelper.getAnnotation(
                SiddhiConstants.ANNOTATION_INDEX_BY, tableDefinition.getAnnotations());
        if (collectionName == null || collectionName.isEmpty()) {
            collectionName = HazelcastTableConstants.HAZELCAST_COLLECTION_PREFIX +
                    siddhiAppContext.getName() + '.' + tableDefinition.getId();
        }
        MetaStreamEvent metaStreamEvent = new MetaStreamEvent();
        metaStreamEvent.addInputDefinition(tableDefinition);
        for (Attribute attribute : tableDefinition.getAttributeList()) {
            metaStreamEvent.addOutputData(attribute);
        }
        HazelcastInstance hzInstance = getHazelcastInstance(serverMode, clusterName, clusterPassword, hosts);

        if (annotation != null) {
            if (annotation.getElements().size() != 1) {
                throw new OperationNotSupportedException(SiddhiConstants.ANNOTATION_INDEX_BY + " annotation of table " +
                        tableDefinition.getId() + " contains " + annotation.getElements().size() +
                        " elements, Siddhi Hazelcast event table only supports indexing based on a single attribute");
            }
            String indexAttribute = annotation.getElements().get(0).getValue();
            int indexPosition = tableDefinition.getAttributePosition(indexAttribute);
            eventHolder = new HazelcastPrimaryKeyEventHolder(hzInstance.getMap(collectionName), storeEventPool,
                    eventConverter, indexPosition, indexAttribute);
        } else {
            eventHolder = new HazelcastCollectionEventHolder(hzInstance.getList(collectionName), storeEventPool,
                    eventConverter);
        }
        if (elementId == null) {
            elementId = siddhiAppContext.getElementIdGenerator().createNewId();
        }
    }

    /**
     * Called to get the most suitable Hazelcast Instance for the given set of parameters.
     *
     * @param groupName     Hazelcast cluster name.
     * @param groupPassword Hazelcast cluster password.
     * @param addresses     Hazelcast node addresses ("ip1,ip2,..").
     * @return Hazelcast Instance
     */
    protected HazelcastInstance getHazelcastInstance(boolean serverMode, String groupName,
                                                     String groupPassword, String addresses) {
        if (HazelcastTableServiceValueHolder.getHazelcastInstance() != null && addresses == null) {
            return HazelcastTableServiceValueHolder.getHazelcastInstance();
        } else {
            if (serverMode) {
                Config config = new Config();
                config.setProperty("hazelcast.logging.type", "log4j");
                config.setInstanceName(HazelcastTableConstants.HAZELCAST_INSTANCE_PREFIX +
                        siddhiAppContext.getName());
                if (groupName != null && !groupName.isEmpty()) {
                    config.getGroupConfig().setName(groupName);
                }
                if (groupPassword != null && !groupPassword.isEmpty()) {
                    config.getGroupConfig().setPassword(groupPassword);
                }
                if (addresses != null && !addresses.isEmpty()) {
                    JoinConfig joinConfig = config.getNetworkConfig().getJoin();
                    joinConfig.getMulticastConfig().setEnabled(false);
                    joinConfig.getTcpIpConfig().setEnabled(true);
                    for (String ip : addresses.split(",")) {
                        joinConfig.getTcpIpConfig().addMember(ip);
                    }
                }
                return Hazelcast.getOrCreateHazelcastInstance(config);
            } else {
                ClientConfig clientConfig = new ClientConfig();
                clientConfig.setProperty("hazelcast.logging.type", "log4j");
                if (groupName != null && !groupName.isEmpty()) {
                    clientConfig.getGroupConfig().setName(groupName);
                }
                if (groupPassword != null && !groupPassword.isEmpty()) {
                    clientConfig.getGroupConfig().setPassword(groupPassword);
                }
                clientConfig.setNetworkConfig(clientConfig.getNetworkConfig().addAddress(addresses.split(",")));
                return HazelcastClient.newHazelcastClient(clientConfig);
            }
        }
    }

    @Override
    public TableDefinition getTableDefinition() {
        return tableDefinition;
    }

    @Override
    public synchronized void add(ComplexEventChunk<StreamEvent> addingEventChunk) {
        eventHolder.add(addingEventChunk);
    }

    @Override
    public synchronized void delete(ComplexEventChunk<StateEvent> deletingEventChunk, CompiledCondition
            compiledCondition) {
        ((Operator) compiledCondition).delete(deletingEventChunk, eventHolder);
    }

    @Override
    public synchronized void update(ComplexEventChunk<StateEvent> updatingEventChunk, CompiledCondition
            compiledCondition,
                                    UpdateAttributeMapper[] updateAttributeMappers) {
        ((Operator) compiledCondition).update(updatingEventChunk, eventHolder, updateAttributeMappers);

    }

    @Override
    public synchronized void updateOrAdd(ComplexEventChunk<StateEvent> updateOrAddingEventChunk,
                                         CompiledCondition compiledCondition, UpdateAttributeMapper[]
                                                     updateAttributeMappers,
                                         AddingStreamEventExtractor addingStreamEventExtractor) {
        ComplexEventChunk<StreamEvent> failedEvents = ((Operator) compiledCondition).tryUpdate(updateOrAddingEventChunk,
                eventHolder, updateAttributeMappers, addingStreamEventExtractor);
        eventHolder.add(failedEvents);

    }

    @Override
    public synchronized boolean contains(StateEvent matchingEvent, CompiledCondition compiledCondition) {
        return ((Operator) compiledCondition).contains(matchingEvent, eventHolder);
    }

    @Override
    public synchronized StreamEvent find(StateEvent matchingEvent, CompiledCondition compiledCondition) {
        return ((Operator) compiledCondition).find(matchingEvent, eventHolder, tableStreamEventCloner);
    }

    @Override
    public CompiledCondition compileCondition(Expression expression, MatchingMetaInfoHolder matchingMetaInfoHolder,
                                              SiddhiAppContext siddhiAppContext,
                                              List<VariableExpressionExecutor> variableExpressionExecutors,
                                              Map<String, Table> tableMap, String queryName) {
        return HazelcastOperatorParser.constructOperator(eventHolder, expression, matchingMetaInfoHolder,
                siddhiAppContext, variableExpressionExecutors, tableMap, tableDefinition.getId());
    }

}
