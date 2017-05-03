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

package org.wso2.siddhi.core;

import com.lmax.disruptor.ExceptionHandler;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.debugger.SiddhiDebugger;
import org.wso2.siddhi.core.exception.DefinitionNotExistException;
import org.wso2.siddhi.core.exception.QueryNotExistException;
import org.wso2.siddhi.core.partition.PartitionRuntime;
import org.wso2.siddhi.core.query.QueryRuntime;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.core.query.output.callback.InsertIntoStreamCallback;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.input.InputManager;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.core.stream.output.sink.SinkCallback;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.extension.holder.EternalReferencedHolder;
import org.wso2.siddhi.core.util.snapshot.AsyncSnapshotPersistor;
import org.wso2.siddhi.core.util.statistics.MemoryUsageTracker;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Keep streamDefinitions, partitionRuntimes, queryRuntimes of an executionPlan
 * and streamJunctions and inputHandlers used
 */
public class ExecutionPlanRuntime {
    private static final Logger log = Logger.getLogger(ExecutionPlanRuntime.class);
    // Contains event tables.
    private final ConcurrentMap<String, List<InputTransport>> eventSourceMap;
    private final ConcurrentMap<String, List<OutputTransport>> eventSinkMap;
    private ConcurrentMap<String, AbstractDefinition> streamDefinitionMap = new ConcurrentHashMap<String,
            AbstractDefinition>(); // Contains stream definition.
    private ConcurrentMap<String, AbstractDefinition> tableDefinitionMap = new ConcurrentHashMap<String,
            AbstractDefinition>(); // Contains table definition.
    private InputManager inputManager;
    private ConcurrentMap<String, QueryRuntime> queryProcessorMap = new ConcurrentHashMap<String, QueryRuntime>();
    private ConcurrentMap<String, StreamJunction> streamJunctionMap = new ConcurrentHashMap<String, StreamJunction>()
            ; // Contains stream junctions.
    private ConcurrentMap<String, Table> tableMap = new ConcurrentHashMap<String, Table>(); //
    // Contains event tables.
    private final ConcurrentMap<String, List<Source>> eventSourceMap;
    private final ConcurrentMap<String, List<Sink>> eventSinkMap;
    private ConcurrentMap<String, PartitionRuntime> partitionMap = new ConcurrentHashMap<String, PartitionRuntime>();
    // Contains partitions.
    private ExecutionPlanContext executionPlanContext;
    private ConcurrentMap<String, ExecutionPlanRuntime> executionPlanRuntimeMap;
    private MemoryUsageTracker memoryUsageTracker;
    private SiddhiDebugger siddhiDebugger;

    public ExecutionPlanRuntime(ConcurrentMap<String, AbstractDefinition> streamDefinitionMap,
                                ConcurrentMap<String, AbstractDefinition> tableDefinitionMap, InputManager inputManager,
                                ConcurrentMap<String, QueryRuntime> queryProcessorMap,
                                ConcurrentMap<String, StreamJunction> streamJunctionMap,
                                ConcurrentMap<String, Table> tableMap,
                                ConcurrentMap<String, List<Source>> eventSourceMap,
                                ConcurrentMap<String, List<Sink>> eventSinkMap,
                                ConcurrentMap<String, PartitionRuntime> partitionMap,
                                ExecutionPlanContext executionPlanContext,
                                ConcurrentMap<String, ExecutionPlanRuntime> executionPlanRuntimeMap) {
        this.streamDefinitionMap = streamDefinitionMap;
        this.tableDefinitionMap = tableDefinitionMap;
        this.inputManager = inputManager;
        this.queryProcessorMap = queryProcessorMap;
        this.streamJunctionMap = streamJunctionMap;
        this.tableMap = tableMap;
        this.eventSourceMap = eventSourceMap;
        this.eventSinkMap = eventSinkMap;
        this.partitionMap = partitionMap;
        this.executionPlanContext = executionPlanContext;
        this.executionPlanRuntimeMap = executionPlanRuntimeMap;
        if (executionPlanContext.isStatsEnabled() && executionPlanContext.getStatisticsManager() != null) {
            memoryUsageTracker = executionPlanContext
                    .getSiddhiContext()
                    .getStatisticsConfiguration()
                    .getFactory()
                    .createMemoryUsageTracker(executionPlanContext.getStatisticsManager());
            monitorQueryMemoryUsage();
        }

        for (Map.Entry<String, List<Sink>> sinkEntries : eventSinkMap.entrySet()) {
            addCallback(sinkEntries.getKey(),
                    new SinkCallback(sinkEntries.getValue(),
                            streamDefinitionMap.get(sinkEntries.getKey())));
        }
        for (Map.Entry<String, List<Source>> sourceEntries : eventSourceMap.entrySet()) {
            InputHandler inputHandler = getInputHandler(sourceEntries.getKey());
            for (Source source : sourceEntries.getValue()) {
                source.getMapper().setInputHandler(inputHandler);
            }
        }
    }

    public String getName() {
        return executionPlanContext.getName();
    }

    /**
     * Get the stream definition map.
     *
     * @return Map of {@link StreamDefinition}s.
     */
    public Map<String, StreamDefinition> getStreamDefinitionMap() {
        return streamDefinitionMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> (StreamDefinition) e.getValue()));
    }

    /**
     * Get the table definition map.
     *
     * @return Map of {@link TableDefinition}s.
     */
    public Map<String, TableDefinition> getTableDefinitionMap() {
        return tableDefinitionMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> (TableDefinition) e.getValue()));
    }

    /**
     * Get the names of the available queries.
     *
     * @return {@link Set<String>} of query names.
     */
    public Set<String> getQueryNames() {
        return queryProcessorMap.keySet();
    }

    public Map<String, Map<String, AbstractDefinition>> getPartitionedInnerStreamDefinitionMap() {
        Map<String, Map<String, AbstractDefinition>> innerStreams = new HashMap<>();
        for (PartitionRuntime partition : partitionMap.values()) {
            innerStreams.put(partition.getElementId(), partition.getLocalStreamDefinitionMap());
        }
        return innerStreams;
    }

    public void addCallback(String streamId, StreamCallback streamCallback) {
        streamCallback.setStreamId(streamId);
        StreamJunction streamJunction = streamJunctionMap.get(streamId);
        if (streamJunction == null) {
            throw new DefinitionNotExistException("No stream found with name: " + streamId);
        }
        streamCallback.setStreamDefinition(streamDefinitionMap.get(streamId));
        streamCallback.setContext(executionPlanContext);
        streamJunction.subscribe(streamCallback);
    }

    public void addCallback(String queryName, QueryCallback callback) {
        callback.setContext(executionPlanContext);
        QueryRuntime queryRuntime = queryProcessorMap.get(queryName);
        if (queryRuntime == null) {
            throw new QueryNotExistException("No query found with name: " + queryName);
        }
        callback.setQuery(queryRuntime.getQuery());
        queryRuntime.addCallback(callback);
    }

    public InputHandler getInputHandler(String streamId) {
        return inputManager.getInputHandler(streamId);
    }

    public Collection<List<Source>> getSources() {
        return eventSourceMap.values();
    }

    public synchronized void shutdown() {
        for (List<Source> sources : eventSourceMap.values()) {
            for (Source source : sources) {
                source.shutdown();
            }
        }

        for (List<Sink> sinks : eventSinkMap.values()) {
            for (Sink sink : sinks) {
                sink.shutdown();
            }
        }

        for (EternalReferencedHolder eternalReferencedHolder : executionPlanContext.getEternalReferencedHolders()) {
            try {
                eternalReferencedHolder.stop();
            } catch (Throwable t) {
                log.error("Error in shutting down Execution Plan '" + executionPlanContext.getName() +
                        "', " + t.getMessage(), t);
            }
        }
        inputManager.disconnect();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }
                for (StreamJunction streamJunction : streamJunctionMap.values()) {
                    streamJunction.stopProcessing();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }
                executionPlanContext.getScheduledExecutorService().shutdownNow();
                executionPlanContext.getExecutorService().shutdownNow();

            }
        }, "Siddhi-ExecutionPlan-" + executionPlanContext.getName() + "-Shutdown-Cleaner");
        thread.start();

        if (executionPlanRuntimeMap != null) {
            executionPlanRuntimeMap.remove(executionPlanContext.getName());
        }
        if (executionPlanContext.isStatsEnabled() && executionPlanContext.getStatisticsManager() != null) {
            executionPlanContext.getStatisticsManager().stopReporting();
            executionPlanContext.getStatisticsManager().cleanup();
        }
    }

    public synchronized void start() {
        if (executionPlanContext.isStatsEnabled() && executionPlanContext.getStatisticsManager() != null) {
            executionPlanContext.getStatisticsManager().startReporting();
        }
        for (EternalReferencedHolder eternalReferencedHolder : executionPlanContext.getEternalReferencedHolders()) {
            eternalReferencedHolder.start();
        }
        for (List<Sink> sinks : eventSinkMap.values()) {
            for (Sink sink : sinks) {
                sink.connectWithRetry(executionPlanContext.getExecutorService());
            }
        }
        for (StreamJunction streamJunction : streamJunctionMap.values()) {
            streamJunction.startProcessing();
        }
        for (List<Source> sources : eventSourceMap.values()) {
            for (Source source : sources) {
                source.connectWithRetry(executionPlanContext.getExecutorService());
            }
        }
    }

    public synchronized SiddhiDebugger debug() {
        siddhiDebugger = new SiddhiDebugger(executionPlanContext);
        List<StreamRuntime> streamRuntime = new ArrayList<StreamRuntime>();
        List<InsertIntoStreamCallback> streamCallbacks = new ArrayList<InsertIntoStreamCallback>();
        for (QueryRuntime queryRuntime : queryProcessorMap.values()) {
            streamRuntime.add(queryRuntime.getStreamRuntime());
            streamCallbacks.add((InsertIntoStreamCallback) queryRuntime.getOutputCallback());
        }
        for (StreamRuntime streamRuntime1 : streamRuntime) {
            for (SingleStreamRuntime singleStreamRuntime : streamRuntime1.getSingleStreamRuntimes()) {
                singleStreamRuntime.getProcessStreamReceiver().setSiddhiDebugger(siddhiDebugger);
            }
        }
        for (InsertIntoStreamCallback insertedCallbacks : streamCallbacks) {
            insertedCallbacks.setSiddhiDebugger(siddhiDebugger);
        }
        start();

        return siddhiDebugger;
    }

    public Future persist() {
        try {
            // first, pause all the event sources
            eventSourceMap.values().forEach(list -> list.forEach(Source::pause));
            // take snapshots of execution units
            byte[] snapshots = executionPlanContext.getSnapshotService().snapshot();
            // start the snapshot persisting task asynchronously
            return executionPlanContext.getExecutorService().submit(new AsyncSnapshotPersistor(snapshots,
                    executionPlanContext.getSiddhiContext().getPersistenceStore(), executionPlanContext.getName()));
        } finally {
            // at the end, resume the event sources
            eventSourceMap.values().forEach(list -> list.forEach(Source::resume));
        }
    }

    public byte[] snapshot() {
        try {
            // first, pause all the event sources
            eventSourceMap.values().forEach(list -> list.forEach(Source::pause));
            // take snapshots of execution units
            return executionPlanContext.getSnapshotService().snapshot();
        } finally {
            // at the end, resume the event sources
            eventSourceMap.values().forEach(list -> list.forEach(Source::resume));
        }
    }

    public void restoreRevision(String revision) {
        try {
            // first, pause all the event sources
            eventSourceMap.values().forEach(list -> list.forEach(Source::pause));
            // start the restoring process
            executionPlanContext.getPersistenceService().restoreRevision(revision);
        } finally {
            // at the end, resume the event sources
            eventSourceMap.values().forEach(list -> list.forEach(Source::resume));
        }
    }

    public void restoreLastRevision() {
        try {
            // first, pause all the event sources
            eventSourceMap.values().forEach(list -> list.forEach(Source::pause));
            // start the restoring process
            executionPlanContext.getPersistenceService().restoreLastRevision();
        } finally {
            // at the end, resume the event sources
            eventSourceMap.values().forEach(list -> list.forEach(Source::resume));
        }
    }

    private void monitorQueryMemoryUsage() {
        for (Map.Entry entry : queryProcessorMap.entrySet()) {
            memoryUsageTracker.registerObject(entry.getValue(),
                    executionPlanContext.getSiddhiContext().getStatisticsConfiguration().getMatricPrefix() +
                            SiddhiConstants.METRIC_DELIMITER + SiddhiConstants.METRIC_INFIX_EXECUTION_PLANS +
                            SiddhiConstants.METRIC_DELIMITER + getName() + SiddhiConstants.METRIC_DELIMITER +
                            SiddhiConstants.METRIC_INFIX_SIDDHI + SiddhiConstants.METRIC_DELIMITER +
                            SiddhiConstants.METRIC_INFIX_QUERIES + SiddhiConstants.METRIC_DELIMITER +
                            entry.getKey());
        }
        for (Map.Entry entry : partitionMap.entrySet()) {
            ConcurrentMap<String, QueryRuntime> queryRuntime = ((PartitionRuntime) entry.getValue())
                    .getMetaQueryRuntimeMap();
            for (Map.Entry query : queryRuntime.entrySet()) {
                memoryUsageTracker.registerObject(entry.getValue(),
                        executionPlanContext.getSiddhiContext().getStatisticsConfiguration().getMatricPrefix() +
                                SiddhiConstants.METRIC_DELIMITER + SiddhiConstants.METRIC_INFIX_EXECUTION_PLANS +
                                SiddhiConstants.METRIC_DELIMITER + getName() + SiddhiConstants.METRIC_DELIMITER +
                                SiddhiConstants.METRIC_INFIX_SIDDHI + SiddhiConstants.METRIC_DELIMITER +
                                SiddhiConstants.METRIC_INFIX_QUERIES + SiddhiConstants.METRIC_DELIMITER +
                                query.getKey());
            }
        }
    }

    public void handleExceptionWith(ExceptionHandler<Object> exceptionHandler) {
        executionPlanContext.setDisruptorExceptionHandler(exceptionHandler);
    }
}