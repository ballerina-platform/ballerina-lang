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
package org.wso2.siddhi.core;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.DifferentDefinitionAlreadyExistException;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.exception.QueryNotExistException;
import org.wso2.siddhi.core.extension.EternalReferencedHolder;
import org.wso2.siddhi.core.persistence.PersistenceService;
import org.wso2.siddhi.core.snapshot.SnapshotService;
import org.wso2.siddhi.core.persistence.PersistenceStore;
import org.wso2.siddhi.core.snapshot.ThreadBarrier;
import org.wso2.siddhi.core.query.QueryManager;
import org.wso2.siddhi.core.query.output.callback.InsertIntoStreamCallback;
import org.wso2.siddhi.core.query.output.callback.OutputCallback;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.table.InMemoryEventTable;
import org.wso2.siddhi.core.table.RDBMSEventTable;
import org.wso2.siddhi.core.tracer.EventMonitor;
import org.wso2.siddhi.core.tracer.EventMonitorService;
import org.wso2.siddhi.core.util.ExecutionPlanReference;
import org.wso2.siddhi.core.util.SiddhiThreadFactory;
import org.wso2.siddhi.core.util.generator.GlobalIndexGenerator;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.definition.partition.PartitionDefinition;
import org.wso2.siddhi.query.api.query.Query;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SiddhiManager {

    static final Logger log = Logger.getLogger(SiddhiManager.class);

    private SiddhiContext siddhiContext;
    private ConcurrentMap<String, StreamJunction> streamJunctionMap = new ConcurrentHashMap<String, StreamJunction>(); //contains definition
    private ConcurrentMap<String, AbstractDefinition> streamTableDefinitionMap = new ConcurrentHashMap<String, AbstractDefinition>(); //contains stream & table definition
    private ConcurrentMap<String, QueryManager> queryProcessorMap = new ConcurrentHashMap<String, QueryManager>();
    private ConcurrentMap<String, InputHandler> inputHandlerMap = new ConcurrentHashMap<String, InputHandler>();
    private ConcurrentMap<String, EventTable> eventTableMap = new ConcurrentHashMap<String, EventTable>(); //contains event tables
    private ConcurrentMap<String, PartitionDefinition> partitionDefinitionMap = new ConcurrentHashMap<String, PartitionDefinition>();

//    LinkedBlockingQueue<StateEvent> inputQueue = new LinkedBlockingQueue<StateEvent>();

    public SiddhiManager() {
        this(new SiddhiConfiguration());
    }

    public SiddhiManager(SiddhiConfiguration siddhiConfiguration) {

        if (siddhiConfiguration.isDistributedProcessing()) {
            HazelcastInstance hazelcastInstance = Hazelcast.getHazelcastInstanceByName(siddhiConfiguration.getInstanceIdentifier());
            if (hazelcastInstance == null) {
                this.siddhiContext = new SiddhiContext(siddhiConfiguration.getQueryPlanIdentifier(), SiddhiContext.ProcessingState.ENABLE_INTERNAL);
                Config hazelcastConf = new Config();
                hazelcastConf.setProperty("hazelcast.logging.type", "log4j");
                hazelcastConf.getGroupConfig().setName(siddhiConfiguration.getQueryPlanIdentifier());
                hazelcastConf.setInstanceName(siddhiConfiguration.getInstanceIdentifier());
                hazelcastInstance = Hazelcast.newHazelcastInstance(hazelcastConf);
            } else {
                this.siddhiContext = new SiddhiContext(siddhiConfiguration.getQueryPlanIdentifier(), SiddhiContext.ProcessingState.ENABLE_EXTERNAL);
            }
            siddhiContext.setHazelcastInstance(hazelcastInstance);
            siddhiContext.setGlobalIndexGenerator(new GlobalIndexGenerator(siddhiContext));
        } else {
            this.siddhiContext = new SiddhiContext(siddhiConfiguration.getQueryPlanIdentifier(), SiddhiContext.ProcessingState.DISABLED);
        }

        this.siddhiContext.setEventBatchSize(siddhiConfiguration.getEventBatchSize());
        this.siddhiContext.setAsyncProcessing(siddhiConfiguration.isAsyncProcessing());
        this.siddhiContext.setSiddhiExtensions(siddhiConfiguration.getSiddhiExtensions());
        this.siddhiContext.setThreadBarrier(new ThreadBarrier());
        this.siddhiContext.setThreadPoolExecutor(new ThreadPoolExecutor(siddhiConfiguration.getThreadExecutorCorePoolSize(),
                siddhiConfiguration.getThreadExecutorMaxPoolSize(),
                50,
                TimeUnit.MICROSECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new SiddhiThreadFactory("Executor")));
        this.siddhiContext.setScheduledExecutorService(Executors.newScheduledThreadPool(siddhiConfiguration.getThreadSchedulerCorePoolSize(), new SiddhiThreadFactory("Scheduler")));
        this.siddhiContext.setSnapshotService(new SnapshotService(siddhiContext));
        this.siddhiContext.setPersistenceService(new PersistenceService(siddhiContext));
        this.siddhiContext.setEventMonitorService(new EventMonitorService(siddhiContext));


    }


    public InputHandler defineStream(StreamDefinition streamDefinition) {
        if (!checkEventStreamExist(streamDefinition)) {
            streamTableDefinitionMap.put(streamDefinition.getStreamId(), streamDefinition);
            StreamJunction streamJunction = streamJunctionMap.get(streamDefinition.getStreamId());
            if (streamJunction == null) {
                streamJunction = new StreamJunction(streamDefinition.getStreamId(), siddhiContext.getEventMonitorService());
                streamJunctionMap.put(streamDefinition.getStreamId(), streamJunction);
            }
            InputHandler inputHandler = new InputHandler(streamDefinition.getStreamId(), streamJunction, siddhiContext);
            inputHandlerMap.put(streamDefinition.getStreamId(), inputHandler);
            return inputHandler;
        } else {
            return inputHandlerMap.get(streamDefinition.getStreamId());
        }

    }

    public InputHandler defineStream(String streamDefinition) throws SiddhiParserException {
        return defineStream(SiddhiCompiler.parseStreamDefinition(streamDefinition));

    }

    public void removeStream(String streamId) {
        AbstractDefinition abstractDefinition = streamTableDefinitionMap.get(streamId);
        if (abstractDefinition != null && abstractDefinition instanceof StreamDefinition) {
            streamTableDefinitionMap.remove(streamId);
            streamJunctionMap.remove(streamId);
            inputHandlerMap.remove(streamId);
        }
    }

//    public InputHandler updateStream(StreamDefinition streamDefinition) {
//        try {
//            if (checkEventStreamExist(streamDefinition)) {
//                return inputHandlerMap.get(streamDefinition.getSourceId());
//            }
//        } catch (EventStreamWithDifferentDefinitionAlreadyExistException e) {
//            StreamJunction streamJunction = streamJunctionMap.get(streamDefinition.getSourceId());
//            streamTableDefinitionMap.replace(streamDefinition.getSourceId(), streamDefinition);
//            streamJunction.setStreamDefinition(streamDefinition);
//            return inputHandlerMap.get(streamDefinition.getSourceId());
//        }
//        throw new EventStreamNotExistException("Stream with name " + streamDefinition.getSourceId() + " does not exist! Hence update aborted!");
//    }
//
//    public void dropStream(String streamId) {
//        StreamDefinition streamDefinition = streamTableDefinitionMap.remove(streamId);
//        if (streamDefinition != null) {
//            streamJunctionMap.remove(streamId);
//            InputHandler inputHandler = inputHandlerMap.remove(streamId);
//            if (inputHandler != null) {
//                inputHandler.setStreamJunction(null);
//            }
//            for (QueryManager analyser : queryProcessorMap.values()) {
//                if (analyser.getQuerySelector().getOutputStreamDefinition().getSourceId().equals(streamId)) {
//                    analyser.getQuerySelector().setStreamJunction(null);
//                }
//            }
//        }
//    }

    private boolean checkEventStreamExist(StreamDefinition newStreamDefinition) {
        AbstractDefinition definition = streamTableDefinitionMap.get(newStreamDefinition.getStreamId());
        if (definition != null) {
            if (definition instanceof TableDefinition) {
                throw new DifferentDefinitionAlreadyExistException("Table " + newStreamDefinition.getStreamId() + " is already defined as " + definition + ", hence cannot define " + newStreamDefinition);
            } else if (!definition.getAttributeList().equals(newStreamDefinition.getAttributeList())) {
                throw new DifferentDefinitionAlreadyExistException("Stream " + newStreamDefinition.getStreamId() + " is already defined as " + definition + ", hence cannot define " + newStreamDefinition);
            } else {
                return true;
            }
        }
        return false;
    }


    public void defineTable(TableDefinition tableDefinition) {
        if (!checkEventTableExist(tableDefinition)) {
            streamTableDefinitionMap.put(tableDefinition.getTableId(), tableDefinition);
            EventTable eventTable = eventTableMap.get(tableDefinition.getTableId());
            if (eventTable == null) {
                if (tableDefinition.getExternalTable() == null) {
                    eventTable = new InMemoryEventTable(tableDefinition, siddhiContext);
                    siddhiContext.getSnapshotService().addSnapshotable((InMemoryEventTable) eventTable);
                } else {
                    eventTable = new RDBMSEventTable(tableDefinition, siddhiContext);
                    // load params for table.
                }
                eventTableMap.put(tableDefinition.getTableId(), eventTable);
            }
        }
    }


    public void defineTable(String tableDefinition) throws SiddhiParserException {
        defineTable(SiddhiCompiler.parseTableDefinition(tableDefinition));

    }

    public void removeTable(String tableId) {
        AbstractDefinition abstractDefinition = streamTableDefinitionMap.get(tableId);
        if (abstractDefinition != null && abstractDefinition instanceof TableDefinition) {
            streamTableDefinitionMap.remove(tableId);
            eventTableMap.remove(tableId);
        }
    }

    private boolean checkEventTableExist(TableDefinition newTableDefinition) {
        AbstractDefinition definition = streamTableDefinitionMap.get(newTableDefinition.getTableId());
        if (definition != null) {
            if (definition instanceof StreamDefinition) {
                throw new DifferentDefinitionAlreadyExistException("Stream " + newTableDefinition.getTableId() + " is already defined as " + definition);
            } else if (!definition.getAttributeList().equals(newTableDefinition.getAttributeList())) {
                throw new DifferentDefinitionAlreadyExistException("Table " + newTableDefinition.getTableId() + " is already defined as " + definition);
            } else {
                return true;
            }
        }
        return false;
    }

    public void definePartition(PartitionDefinition partitionDefinition) {
        if (!checkEventPartitionExist(partitionDefinition)) {
            partitionDefinitionMap.put(partitionDefinition.getPartitionId(), partitionDefinition);
        }
    }

    public void definePartition(String partitionDefinition) throws SiddhiParserException {
        definePartition(SiddhiCompiler.parsePartitionDefinition(partitionDefinition));
    }

    public void removePartition(String partitionId) {
        PartitionDefinition partitionDefinition = partitionDefinitionMap.get(partitionId);
        if (partitionDefinition != null) {
            partitionDefinitionMap.remove(partitionId);
        }
    }

    private boolean checkEventPartitionExist(PartitionDefinition partitionDefinition) {
        PartitionDefinition definition = partitionDefinitionMap.get(partitionDefinition.getPartitionId());
        if (definition != null) {
            if (!definition.getPartitionTypeList().equals(partitionDefinition.getPartitionTypeList())) {
                throw new DifferentDefinitionAlreadyExistException("Partition " + partitionDefinition.getPartitionId() + " is already defined as " + definition);
            } else {
                return true;
            }
        }
        return false;
    }

    public String addQuery(String query) throws SiddhiParserException {
        return addQuery(SiddhiCompiler.parseQuery(query));
    }

//    public void addExecutionPlan(String addExecutionPlan) throws SiddhiParserException {
//        for (IQuery executionPlan : SiddhiCompiler.parse(addExecutionPlan)) {
//            if (executionPlan instanceof StreamDefinition) {
//                defineStream((StreamDefinition) executionPlan);
//            } else {
//                addQuery((Query) executionPlan);
//            }
//        }
//    }

    public String addQuery(Query query) {
        QueryManager queryManager = new QueryManager(query, streamTableDefinitionMap, streamJunctionMap, eventTableMap, partitionDefinitionMap, siddhiContext);
        OutputCallback outputCallback = queryManager.getOutputCallback();
        if (outputCallback != null && outputCallback instanceof InsertIntoStreamCallback) {
            defineStream(((InsertIntoStreamCallback) outputCallback).getOutputStreamDefinition());
        }
        queryProcessorMap.put(queryManager.getQueryId(), queryManager);
        return queryManager.getQueryId();

    }

    public ExecutionPlanReference addExecutionPlan(String executionPlan) throws SiddhiParserException {
        List<ExecutionPlan> executionPlanList = SiddhiCompiler.parse(executionPlan);
        ExecutionPlanReference executionPlanReference = new ExecutionPlanReference();
        for (ExecutionPlan plan : executionPlanList) {
            if (plan instanceof Query) {
                executionPlanReference.addQueryReference(addQuery((Query) plan));
            } else if (plan instanceof StreamDefinition) {
                executionPlanReference.addInputHandler(defineStream((StreamDefinition) plan));
            } else if (plan instanceof TableDefinition) {
                defineTable((TableDefinition) plan);
            } else if (plan instanceof PartitionDefinition) {
                definePartition((PartitionDefinition) plan);
            } else {
                throw new OperationNotSupportedException(plan.getClass().getName() + " is not supported as an execution plan element ");
            }
        }

        return executionPlanReference;
    }

    public void removeQuery(String queryId) {
        QueryManager queryManager = queryProcessorMap.remove(queryId);
        if (queryManager != null) {
            queryManager.removeQuery(streamJunctionMap, streamTableDefinitionMap);
        }
    }


//    private Map<String, StreamDefinition> getQueryStreamDefinitionMap(List<String> streamIds) {
//        Map<String, StreamDefinition> map = new HashMap<String, StreamDefinition>();
//        for (String streamId : streamIds) {
//            map.put(streamId, streamTableDefinitionMap.get(streamId));
//        }
//        return map;
//    }


//    private void checkQuery(Query query) {
//        List<String> streamIds = query.getInputStream().getStreamIds();
//        System.out.println(streamIds);
//    }

    public Query getQuery(String queryReference) {
        return queryProcessorMap.get(queryReference).getQuery();
    }

    public InputHandler getInputHandler(String streamId) {
        return inputHandlerMap.get(streamId);
    }

    public void addCallback(String streamId, StreamCallback streamCallback) {

        streamCallback.setStreamId(streamId);
        streamCallback.setSiddhiContext(siddhiContext);
        StreamJunction streamJunction = streamJunctionMap.get(streamId);
        if (streamJunction == null) {
            streamJunction = new StreamJunction(streamId, siddhiContext.getEventMonitorService());
            streamJunctionMap.put(streamId, streamJunction);
        }
        streamJunction.addEventFlow(streamCallback);
    }

    public void addCallback(String queryReference, QueryCallback callback) {
        QueryManager queryManager = queryProcessorMap.get(queryReference);
        if (queryManager == null) {
            throw new QueryNotExistException("No query fund for " + queryReference);
        }
        callback.setStreamDefinition(queryManager.getOutputStreamDefinition());
        callback.setSiddhiContext(siddhiContext);
        queryManager.addCallback(callback);
    }


    public void shutdown() {
        siddhiContext.getThreadPoolExecutor().shutdown();
        siddhiContext.getScheduledExecutorService().shutdownNow();
        for (EternalReferencedHolder eternalReferencedHolder : siddhiContext.getEternalReferencedHolders()) {
            eternalReferencedHolder.destroy();
        }
        if (siddhiContext.getDistributedProcessingState() == SiddhiContext.ProcessingState.ENABLE_INTERNAL) {
            try {
                siddhiContext.getHazelcastInstance().getLifecycleService().shutdown();
            } catch (IllegalStateException ignore) {
                //occurs when Hazelcast Instance is not active!
            }
        }
//        for (RunnableWindowHandler handler : siddhiContext.getRunnableHandlerList()) {
//            handler.shutdown();
//        }
    }

    public StreamDefinition getStreamDefinition(String streamId) {
        AbstractDefinition abstractDefinition = streamTableDefinitionMap.get(streamId);
        if (abstractDefinition instanceof StreamDefinition) {
            return (StreamDefinition) abstractDefinition;
        } else {
            return null;
        }
    }

    public List<StreamDefinition> getStreamDefinitions() {
        List<StreamDefinition> streamDefinitions = new ArrayList<StreamDefinition>(streamTableDefinitionMap.size());
        for (AbstractDefinition abstractDefinition : streamTableDefinitionMap.values()) {
            if (abstractDefinition instanceof StreamDefinition) {
                streamDefinitions.add((StreamDefinition) abstractDefinition);
            }
        }
        return streamDefinitions;
    }

    public void setPersistStore(PersistenceStore persistStore) {
        siddhiContext.getPersistenceService().setPersistenceStore(persistStore);
    }

    public void setEventMonitor(EventMonitor eventMonitor) {
        siddhiContext.getEventMonitorService().setEventMonitor(eventMonitor);
    }

    public void enableStats(boolean enableStats) {
        siddhiContext.getEventMonitorService().setEnableStats(enableStats);
    }

    public void enableTrace(boolean enableTrace) {
        siddhiContext.getEventMonitorService().setEnableTrace(enableTrace);
    }


    public String persist() {
        return siddhiContext.getPersistenceService().persist();
    }

    public void restoreRevision(String revision) {
        siddhiContext.getPersistenceService().restoreRevision(revision);
    }

    public void restoreLastRevision() {
        siddhiContext.getPersistenceService().restoreLastRevision();
    }

    public byte[] snapshot() {
        return siddhiContext.getSnapshotService().snapshot();
    }

    public void restore(byte[] snapshot) {
        siddhiContext.getSnapshotService().restore(snapshot);
    }

    public SiddhiContext getSiddhiContext() {
        return siddhiContext;
    }
}
