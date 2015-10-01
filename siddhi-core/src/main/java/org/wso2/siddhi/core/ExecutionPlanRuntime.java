/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.siddhi.core;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.DefinitionNotExistException;
import org.wso2.siddhi.core.exception.QueryNotExistException;
import org.wso2.siddhi.core.partition.PartitionRuntime;
import org.wso2.siddhi.core.query.QueryRuntime;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.input.InputManager;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.extension.holder.EternalReferencedHolder;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * keep streamDefinitions, partitionRuntimes, queryRuntimes of an executionPlan
 * and streamJunctions and inputHandlers used
 */
public class ExecutionPlanRuntime {
    private static final Logger log = Logger.getLogger(ExecutionPlanRuntime.class);

    private ConcurrentMap<String, AbstractDefinition> streamDefinitionMap = new ConcurrentHashMap<String, AbstractDefinition>(); //contains stream definition
    private ConcurrentMap<String, AbstractDefinition> tableDefinitionMap = new ConcurrentHashMap<String, AbstractDefinition>(); //contains table definition
    private InputManager inputManager;
    private ConcurrentMap<String, QueryRuntime> queryProcessorMap = new ConcurrentHashMap<String, QueryRuntime>();
    private ConcurrentMap<String, StreamJunction> streamJunctionMap = new ConcurrentHashMap<String, StreamJunction>(); //contains stream junctions
    private ConcurrentMap<String, EventTable> eventTableMap = new ConcurrentHashMap<String, EventTable>(); //contains event tables
    private ConcurrentMap<String, PartitionRuntime> partitionMap = new ConcurrentHashMap<String, PartitionRuntime>(); //contains partitions
    private ExecutionPlanContext executionPlanContext;
    private ConcurrentMap<String, ExecutionPlanRuntime> executionPlanRuntimeMap;

    public ExecutionPlanRuntime(ConcurrentMap<String, AbstractDefinition> streamDefinitionMap, ConcurrentMap<String, AbstractDefinition> tableDefinitionMap, InputManager inputManager, ConcurrentMap<String, QueryRuntime> queryProcessorMap, ConcurrentMap<String, StreamJunction> streamJunctionMap, ConcurrentMap<String, EventTable> eventTableMap, ConcurrentMap<String, PartitionRuntime> partitionMap, ExecutionPlanContext executionPlanContext, ConcurrentMap<String, ExecutionPlanRuntime> executionPlanRuntimeMap) {
        this.streamDefinitionMap = streamDefinitionMap;
        this.tableDefinitionMap = tableDefinitionMap;
        this.inputManager = inputManager;
        this.queryProcessorMap = queryProcessorMap;
        this.streamJunctionMap = streamJunctionMap;
        this.eventTableMap = eventTableMap;
        this.partitionMap = partitionMap;
        this.executionPlanContext = executionPlanContext;
        this.executionPlanRuntimeMap = executionPlanRuntimeMap;
    }

    public String getName() {
        return executionPlanContext.getName();
    }

    public Map<String, AbstractDefinition> getStreamDefinitionMap() {
        return streamDefinitionMap;
    }

    public void addCallback(String streamId, StreamCallback streamCallback) {
        streamCallback.setStreamId(streamId);
        StreamJunction streamJunction = streamJunctionMap.get(streamId);
        if (streamJunction == null) {
            throw new DefinitionNotExistException("No stream fund with name: " + streamId);
        }
        streamCallback.setStreamDefinition(streamDefinitionMap.get(streamId));
        streamCallback.setContext(executionPlanContext);
        streamJunction.subscribe(streamCallback);
    }

    public void addCallback(String queryName, QueryCallback callback) {
        callback.setContext(executionPlanContext);
        QueryRuntime queryRuntime = queryProcessorMap.get(queryName);
        if (queryRuntime == null) {
            throw new QueryNotExistException("No query fund with name: " + queryName);
        }
        callback.setQuery(queryRuntime.getQuery());
        queryRuntime.addCallback(callback);
    }

    public InputHandler getInputHandler(String streamId) {
        return inputManager.getInputHandler(streamId);
    }

    public synchronized void shutdown() {
        for (EternalReferencedHolder eternalReferencedHolder : executionPlanContext.getEternalReferencedHolders()) {
            try {
                eternalReferencedHolder.stop();
            } catch (Throwable t) {
                log.error("Error in shutting down Execution Plan '" + executionPlanContext.getName() + "', " + t.getMessage(), t);
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
                inputManager.stopProcessing();
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
    }

    public synchronized void start() {
        for (EternalReferencedHolder eternalReferencedHolder : executionPlanContext.getEternalReferencedHolders()) {
            eternalReferencedHolder.start();
        }
        inputManager.startProcessing();
        for (StreamJunction streamJunction : streamJunctionMap.values()) {
            streamJunction.startProcessing();
        }
    }

    public String persist() {
        return executionPlanContext.getPersistenceService().persist();
    }

    public void restoreRevision(String revision) {
        executionPlanContext.getPersistenceService().restoreRevision(revision);
    }

    public void restoreLastRevision() {
        executionPlanContext.getPersistenceService().restoreLastRevision();
    }

    public byte[] snapshot() {
        return executionPlanContext.getSnapshotService().snapshot();
    }

    public void restore(byte[] snapshot) {
        executionPlanContext.getSnapshotService().restore(snapshot);
    }

    public void enableStatistics() {
        ExecutionPlanContext.statEnable = true;
    }

}
