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
import org.wso2.siddhi.core.aggregation.AggregationRuntime;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.debugger.SiddhiDebugger;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.DefinitionNotExistException;
import org.wso2.siddhi.core.exception.QueryNotExistException;
import org.wso2.siddhi.core.partition.PartitionRuntime;
import org.wso2.siddhi.core.query.QueryRuntime;
import org.wso2.siddhi.core.query.StoreQueryRuntime;
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
import org.wso2.siddhi.core.util.parser.StoreQueryParser;
import org.wso2.siddhi.core.util.snapshot.AsyncSnapshotPersistor;
import org.wso2.siddhi.core.util.snapshot.PersistenceReference;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;
import org.wso2.siddhi.core.util.statistics.MemoryUsageTracker;
import org.wso2.siddhi.core.window.Window;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.execution.query.StoreQuery;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Keep streamDefinitions, partitionRuntimes, queryRuntimes of an SiddhiApp
 * and streamJunctions and inputHandlers used
 */
public class SiddhiAppRuntime {
    private static final Logger log = Logger.getLogger(SiddhiAppRuntime.class);
    private final Map<String, Window> windowMap;
    private final Map<String, List<Source>> sourceMap;
    private final Map<String, List<Sink>> sinkMap;
    private ConcurrentMap<String, AggregationRuntime> aggregationMap;
    private Map<String, AbstractDefinition> streamDefinitionMap =
            new ConcurrentHashMap<String,
                    AbstractDefinition>(); // Contains stream definition.
    private Map<String, AbstractDefinition> tableDefinitionMap =
            new ConcurrentHashMap<String,
                    AbstractDefinition>(); // Contains table definition.
    private InputManager inputManager;
    private Map<String, QueryRuntime> queryProcessorMap =
            Collections.synchronizedMap(new LinkedHashMap<String, QueryRuntime>());
    private Map<String, StreamJunction> streamJunctionMap =
            new ConcurrentHashMap<String, StreamJunction>(); // Contains stream junctions.
    private Map<String, Table> tableMap = new ConcurrentHashMap<String, Table>(); // Contains event tables.
    private Map<String, PartitionRuntime> partitionMap =
            new ConcurrentHashMap<String, PartitionRuntime>(); // Contains partitions.
    private Map<StoreQuery, StoreQueryRuntime> storeQueryRuntimeMap =
            new ConcurrentHashMap<>(); // Contains partitions.
    private SiddhiAppContext siddhiAppContext;
    private Map<String, SiddhiAppRuntime> siddhiAppRuntimeMap;
    private MemoryUsageTracker memoryUsageTracker;
    private LatencyTracker storeQueryLatencyTracker;
    private SiddhiDebugger siddhiDebugger;

    public SiddhiAppRuntime(Map<String, AbstractDefinition> streamDefinitionMap,
                            Map<String, AbstractDefinition> tableDefinitionMap,
                            InputManager inputManager,
                            Map<String, QueryRuntime> queryProcessorMap,
                            Map<String, StreamJunction> streamJunctionMap,
                            Map<String, Table> tableMap,
                            Map<String, Window> windowMap,
                            ConcurrentMap<String, AggregationRuntime> aggregationMap,
                            Map<String, List<Source>> sourceMap,
                            Map<String, List<Sink>> sinkMap,
                            Map<String, PartitionRuntime> partitionMap,
                            SiddhiAppContext siddhiAppContext,
                            Map<String, SiddhiAppRuntime> siddhiAppRuntimeMap) {
        this.streamDefinitionMap = streamDefinitionMap;
        this.tableDefinitionMap = tableDefinitionMap;
        this.inputManager = inputManager;
        this.queryProcessorMap = queryProcessorMap;
        this.streamJunctionMap = streamJunctionMap;
        this.tableMap = tableMap;
        this.windowMap = windowMap;
        this.aggregationMap = aggregationMap;
        this.sourceMap = sourceMap;
        this.sinkMap = sinkMap;
        this.partitionMap = partitionMap;
        this.siddhiAppContext = siddhiAppContext;
        this.siddhiAppRuntimeMap = siddhiAppRuntimeMap;
        if (siddhiAppContext.isStatsEnabled() && siddhiAppContext.getStatisticsManager() != null) {
            memoryUsageTracker = siddhiAppContext
                    .getSiddhiContext()
                    .getStatisticsConfiguration()
                    .getFactory()
                    .createMemoryUsageTracker(siddhiAppContext.getStatisticsManager());
            monitorQueryMemoryUsage();
            storeQueryLatencyTracker = siddhiAppContext.getSiddhiContext().getStatisticsConfiguration()
                    .getFactory().createLatencyTracker("store_query", siddhiAppContext.getStatisticsManager());
        }

        for (Map.Entry<String, List<Sink>> sinkEntries : sinkMap.entrySet()) {
            addCallback(sinkEntries.getKey(),
                    new SinkCallback(sinkEntries.getValue(), streamDefinitionMap.get(sinkEntries.getKey())));
        }
        for (Map.Entry<String, List<Source>> sourceEntries : sourceMap.entrySet()) {
            InputHandler inputHandler = getInputHandler(sourceEntries.getKey());
            for (Source source : sourceEntries.getValue()) {
                source.getMapper().setInputHandler(inputHandler);
            }
        }
    }

    public String getName() {
        return siddhiAppContext.getName();
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
     * @return string set of query names.
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
        streamCallback.setContext(siddhiAppContext);
        streamJunction.subscribe(streamCallback);
    }

    public void addCallback(String queryName, QueryCallback callback) {
        callback.setContext(siddhiAppContext);
        QueryRuntime queryRuntime = queryProcessorMap.get(queryName);
        if (queryRuntime == null) {
            throw new QueryNotExistException("No query found with name: " + queryName);
        }
        callback.setQuery(queryRuntime.getQuery());
        queryRuntime.addCallback(callback);
    }

    public Event[] query(StoreQuery storeQuery) {
        try {
            if (storeQueryLatencyTracker != null) {
                storeQueryLatencyTracker.markIn();
            }
            StoreQueryRuntime storeQueryRuntime = storeQueryRuntimeMap.get(storeQuery);
            if (storeQueryRuntime == null) {
                storeQueryRuntime = StoreQueryParser.parse(storeQuery, siddhiAppContext, tableMap, windowMap,
                        aggregationMap);
                storeQueryRuntimeMap.put(storeQuery, storeQueryRuntime);
            }

            return storeQueryRuntime.execute();
        } finally {
            if (storeQueryLatencyTracker != null) {
                storeQueryLatencyTracker.markOut();
            }
        }
    }

    public Event[] query(String storeQuery) {
        return query(SiddhiCompiler.parseStoreQuery(storeQuery));
    }

    public InputHandler getInputHandler(String streamId) {
        return inputManager.getInputHandler(streamId);
    }

    public Collection<List<Source>> getSources() {
        return sourceMap.values();
    }

    public synchronized void start() {
        if (siddhiAppContext.isStatsEnabled() && siddhiAppContext.getStatisticsManager() != null) {
            siddhiAppContext.getStatisticsManager().startReporting();
        }
        for (EternalReferencedHolder eternalReferencedHolder : siddhiAppContext.getEternalReferencedHolders()) {
            eternalReferencedHolder.start();
        }
        for (List<Sink> sinks : sinkMap.values()) {
            for (Sink sink : sinks) {
                sink.connectWithRetry();
            }
        }

        for (Table table : tableMap.values()) {
            table.connectWithRetry();
        }

        for (StreamJunction streamJunction : streamJunctionMap.values()) {
            streamJunction.startProcessing();
        }
        for (List<Source> sources : sourceMap.values()) {
            for (Source source : sources) {
                source.connectWithRetry();
            }
        }
    }

    public synchronized void shutdown() {
        for (List<Source> sources : sourceMap.values()) {
            for (Source source : sources) {
                try {
                    source.shutdown();
                } catch (Throwable t) {
                    log.error("Error in shutting down source '" + source.getType() + "' at '" +
                            source.getStreamDefinition().getId() + "' on Siddhi App '" + siddhiAppContext.getName() +
                            "', " + t.getMessage(), t);
                }

            }
        }

        for (Table table : tableMap.values()) {
            try {
                table.shutdown();
            } catch (Throwable t) {
                log.error("Error in shutting down table '" +
                        table.getTableDefinition().getId() + "' on Siddhi App '" + siddhiAppContext.getName() +
                        "', " + t.getMessage(), t);
            }
        }

        for (List<Sink> sinks : sinkMap.values()) {
            for (Sink sink : sinks) {
                try {
                    sink.shutdown();
                } catch (Throwable t) {
                    log.error("Error in shutting down sink '" + sink.getType() + "' at '" +
                            sink.getStreamDefinition().getId() + "' on Siddhi App '" + siddhiAppContext.getName() +
                            "', " + t.getMessage(), t);
                }
            }
        }

        for (Table table : tableMap.values()) {
            table.shutdown();
        }

        for (EternalReferencedHolder eternalReferencedHolder : siddhiAppContext.getEternalReferencedHolders()) {
            try {
                eternalReferencedHolder.stop();
            } catch (Throwable t) {
                log.error("Error while stopping EternalReferencedHolder '" + eternalReferencedHolder +
                        "' down Siddhi app '" + siddhiAppContext.getName() + "', " + t.getMessage(), t);
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
                siddhiAppContext.getScheduledExecutorService().shutdownNow();
                siddhiAppContext.getExecutorService().shutdownNow();

            }
        }, "Siddhi-SiddhiApp-" + siddhiAppContext.getName() + "-Shutdown-Cleaner");
        thread.start();

        if (siddhiAppRuntimeMap != null) {
            siddhiAppRuntimeMap.remove(siddhiAppContext.getName());
        }
        if (siddhiAppContext.isStatsEnabled() && siddhiAppContext.getStatisticsManager() != null) {
            siddhiAppContext.getStatisticsManager().stopReporting();
            siddhiAppContext.getStatisticsManager().cleanup();
        }
    }

    public synchronized SiddhiDebugger debug() {
        siddhiDebugger = new SiddhiDebugger(siddhiAppContext);
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

    public PersistenceReference persist() {
        try {
            // first, pause all the event sources
            sourceMap.values().forEach(list -> list.forEach(Source::pause));
            // take snapshots of execution units
            byte[] snapshots = siddhiAppContext.getSnapshotService().snapshot();
            // start the snapshot persisting task asynchronously
            AsyncSnapshotPersistor asyncSnapshotPersistor = new AsyncSnapshotPersistor(snapshots,
                    siddhiAppContext.getSiddhiContext().getPersistenceStore(), siddhiAppContext.getName());
            String revision = asyncSnapshotPersistor.getRevision();
            Future future = siddhiAppContext.getExecutorService().submit(asyncSnapshotPersistor);
            return new PersistenceReference(future, revision);
        } finally {
            // at the end, resume the event sources
            sourceMap.values().forEach(list -> list.forEach(Source::resume));
        }
    }

    public byte[] snapshot() {
        try {
            // first, pause all the event sources
            sourceMap.values().forEach(list -> list.forEach(Source::pause));
            // take snapshots of execution units
            return siddhiAppContext.getSnapshotService().snapshot();
        } finally {
            // at the end, resume the event sources
            sourceMap.values().forEach(list -> list.forEach(Source::resume));
        }
    }

    public void restore(byte[] snapshot) {
        try {
            // first, pause all the event sources
            sourceMap.values().forEach(list -> list.forEach(Source::pause));
            // start the restoring process
            siddhiAppContext.getPersistenceService().restore(snapshot);
        } finally {
            // at the end, resume the event sources
            sourceMap.values().forEach(list -> list.forEach(Source::resume));
        }
    }


    public void restoreRevision(String revision) {
        try {
            // first, pause all the event sources
            sourceMap.values().forEach(list -> list.forEach(Source::pause));
            // start the restoring process
            siddhiAppContext.getPersistenceService().restoreRevision(revision);
        } finally {
            // at the end, resume the event sources
            sourceMap.values().forEach(list -> list.forEach(Source::resume));
        }
    }

    public void restoreLastRevision() {
        try {
            // first, pause all the event sources
            sourceMap.values().forEach(list -> list.forEach(Source::pause));
            // start the restoring process
            siddhiAppContext.getPersistenceService().restoreLastRevision();
        } finally {
            // at the end, resume the event sources
            sourceMap.values().forEach(list -> list.forEach(Source::resume));
        }
    }

    private void monitorQueryMemoryUsage() {
        for (Map.Entry entry : queryProcessorMap.entrySet()) {
            memoryUsageTracker.registerObject(entry.getValue(),
                    siddhiAppContext.getSiddhiContext().getStatisticsConfiguration().getMatricPrefix() +
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
                        siddhiAppContext.getSiddhiContext().getStatisticsConfiguration().getMatricPrefix() +
                                SiddhiConstants.METRIC_DELIMITER + SiddhiConstants.METRIC_INFIX_EXECUTION_PLANS +
                                SiddhiConstants.METRIC_DELIMITER + getName() + SiddhiConstants.METRIC_DELIMITER +
                                SiddhiConstants.METRIC_INFIX_SIDDHI + SiddhiConstants.METRIC_DELIMITER +
                                SiddhiConstants.METRIC_INFIX_QUERIES + SiddhiConstants.METRIC_DELIMITER +
                                query.getKey());
            }
        }
    }

    public void handleExceptionWith(ExceptionHandler<Object> exceptionHandler) {
        siddhiAppContext.setDisruptorExceptionHandler(exceptionHandler);
    }
}
