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

package org.wso2.siddhi.core.util;

import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.partition.PartitionRuntime;
import org.wso2.siddhi.core.query.QueryRuntime;
import org.wso2.siddhi.core.query.input.ProcessStreamReceiver;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.core.query.output.callback.InsertIntoStreamCallback;
import org.wso2.siddhi.core.query.output.callback.InsertIntoWindowCallback;
import org.wso2.siddhi.core.query.output.callback.OutputCallback;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.IncrementalExecuteStreamReceiver;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.stream.input.InputManager;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.trigger.EventTrigger;
import org.wso2.siddhi.core.util.lock.LockSynchronizer;
import org.wso2.siddhi.core.util.parser.AggregationParser;
import org.wso2.siddhi.core.util.parser.AggregationRuntime;
import org.wso2.siddhi.core.util.parser.helper.DefinitionParserHelper;
import org.wso2.siddhi.core.window.Window;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;
import org.wso2.siddhi.query.api.definition.FunctionDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.definition.TriggerDefinition;
import org.wso2.siddhi.query.api.definition.WindowDefinition;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * For building ExecutionPlanRuntime
 */
public class ExecutionPlanRuntimeBuilder {
    private ConcurrentMap<String, AbstractDefinition> streamDefinitionMap =
            new ConcurrentHashMap<String, AbstractDefinition>(); //contains stream definition
    private ConcurrentMap<String, AbstractDefinition> tableDefinitionMap =
            new ConcurrentHashMap<String, AbstractDefinition>(); //contains table definition
    private ConcurrentMap<String, AbstractDefinition> windowDefinitionMap =
            new ConcurrentHashMap<String, AbstractDefinition>(); //contains window definition
    private ConcurrentMap<String, AbstractDefinition> aggregationDefinitionConcurrentMap =
            new ConcurrentHashMap<String, AbstractDefinition>(); //contains window definition
    private ConcurrentMap<String, TriggerDefinition> triggerDefinitionMap =
            new ConcurrentHashMap<String, TriggerDefinition>(); //contains trigger definition
    private Map<String, QueryRuntime> queryProcessorMap =
            Collections.synchronizedMap(new LinkedHashMap<String, QueryRuntime>());
    private ConcurrentMap<String, StreamJunction> streamJunctionMap =
            new ConcurrentHashMap<String, StreamJunction>(); //contains stream junctions
    private ConcurrentMap<String, List<Source>> eventSourceMap =
            new ConcurrentHashMap<String, List<Source>>(); //contains event sources
    private ConcurrentMap<String, List<Sink>> eventSinkMap =
            new ConcurrentHashMap<String, List<Sink>>(); //contains event sinks
    private ConcurrentMap<String, Table> tableMap = new ConcurrentHashMap<String, Table>(); //contains event tables
    private ConcurrentMap<String, Window> eventWindowMap =
            new ConcurrentHashMap<String, Window>(); //contains event tables
    private ConcurrentMap<String, EventTrigger> eventTriggerMap =
            new ConcurrentHashMap<String, EventTrigger>(); //contains event tables
    private ConcurrentMap<String, PartitionRuntime> partitionMap =
            new ConcurrentHashMap<String, PartitionRuntime>(); //contains partitions
    private ConcurrentMap<String, ExecutionPlanRuntime> executionPlanRuntimeMap = null;
    private ExecutionPlanContext executionPlanContext;
    private InputManager inputManager;
    private LockSynchronizer lockSynchronizer = new LockSynchronizer();

    public ExecutionPlanRuntimeBuilder(ExecutionPlanContext executionPlanContext) {
        this.executionPlanContext = executionPlanContext;
        this.inputManager = new InputManager(this.executionPlanContext, streamDefinitionMap, streamJunctionMap);
    }

    public void defineStream(StreamDefinition streamDefinition) {
        DefinitionParserHelper.validateDefinition(streamDefinition, streamDefinitionMap, tableDefinitionMap,
                windowDefinitionMap, aggregationDefinitionConcurrentMap);
        AbstractDefinition currentDefinition = streamDefinitionMap
                .putIfAbsent(streamDefinition.getId(), streamDefinition);
        if (currentDefinition != null) {
            streamDefinition = (StreamDefinition) currentDefinition;
        }
        DefinitionParserHelper.addStreamJunction(streamDefinition, streamJunctionMap, executionPlanContext);
        DefinitionParserHelper.addEventSource(streamDefinition, eventSourceMap, executionPlanContext);
        DefinitionParserHelper.addEventSink(streamDefinition, eventSinkMap, executionPlanContext);
    }

    public void defineTable(TableDefinition tableDefinition) {
        DefinitionParserHelper.validateDefinition(tableDefinition, streamDefinitionMap, tableDefinitionMap,
                windowDefinitionMap, aggregationDefinitionConcurrentMap);
        AbstractDefinition currentDefinition = tableDefinitionMap.putIfAbsent(tableDefinition.getId(), tableDefinition);
        if (currentDefinition != null) {
            tableDefinition = (TableDefinition) currentDefinition;
        }
        DefinitionParserHelper.addTable(tableDefinition, tableMap, executionPlanContext);
    }

    public void defineWindow(WindowDefinition windowDefinition) {
        DefinitionParserHelper.validateDefinition(windowDefinition, streamDefinitionMap, tableDefinitionMap,
                windowDefinitionMap, aggregationDefinitionConcurrentMap);
        DefinitionParserHelper.addStreamJunction(windowDefinition, streamJunctionMap, executionPlanContext);
        AbstractDefinition currentDefinition = windowDefinitionMap
                .putIfAbsent(windowDefinition.getId(), windowDefinition);
        if (currentDefinition != null) {
            windowDefinition = (WindowDefinition) currentDefinition;
        }
        DefinitionParserHelper.addWindow(windowDefinition, eventWindowMap, executionPlanContext);
        // defineStream(windowDefinition);
        // DefinitionParserHelper.addStreamJunction(windowDefinition, streamJunctionMap, executionPlanContext);
    }

    public void defineTrigger(TriggerDefinition triggerDefinition) {
        DefinitionParserHelper.validateDefinition(triggerDefinition);
        TriggerDefinition currentDefinition = triggerDefinitionMap.putIfAbsent(triggerDefinition.getId(),
                                                                               triggerDefinition);
        if (currentDefinition != null) {
            triggerDefinition = currentDefinition;
        }
        DefinitionParserHelper.addEventTrigger(triggerDefinition, eventTriggerMap, streamJunctionMap,
                executionPlanContext);
    }

    public void defineAggregation(AggregationDefinition aggregationDefinition,
                                  ExecutionPlanContext executionPlanContext) {
        DefinitionParserHelper.validateDefinition(aggregationDefinition, streamDefinitionMap, tableDefinitionMap,
                windowDefinitionMap, aggregationDefinitionConcurrentMap);
        aggregationDefinitionConcurrentMap.putIfAbsent(aggregationDefinition.getId(), aggregationDefinition);
        // TODO: 3/21/17 : review this and are we missing something
        AggregationRuntime aggregationRuntime = AggregationParser.parse(aggregationDefinition, executionPlanContext,
                getStreamDefinitionMap(),
                getTableDefinitionMap(),
                getWindowDefinitionMap(),
                getTableMap(),
                getEventWindowMap(),
                getEventSourceMap(),
                getEventSinkMap(),
                getLockSynchronizer());

        IncrementalExecuteStreamReceiver incrementalExecuteStreamReceiver =
                aggregationRuntime.getIncrementalExecuteStreamReceiver();
        streamJunctionMap.get(incrementalExecuteStreamReceiver.getStreamId()).
                subscribe(incrementalExecuteStreamReceiver);
    }

    public void addPartition(PartitionRuntime partitionRuntime) {
        partitionMap.put(partitionRuntime.getPartitionId(), partitionRuntime);
    }

    public String addQuery(QueryRuntime queryRuntime) {
        queryProcessorMap.put(queryRuntime.getQueryId(), queryRuntime);
        StreamRuntime streamRuntime = queryRuntime.getStreamRuntime();

        for (SingleStreamRuntime singleStreamRuntime : streamRuntime.getSingleStreamRuntimes()) {
            ProcessStreamReceiver processStreamReceiver = singleStreamRuntime.getProcessStreamReceiver();
            if (!processStreamReceiver.toTable()) {
                streamJunctionMap.get(processStreamReceiver.getStreamId()).subscribe(processStreamReceiver);
            }
        }

        OutputCallback outputCallback = queryRuntime.getOutputCallback();

        if (outputCallback != null && outputCallback instanceof InsertIntoStreamCallback) {
            InsertIntoStreamCallback insertIntoStreamCallback = (InsertIntoStreamCallback) outputCallback;
            StreamDefinition streamDefinition = insertIntoStreamCallback.getOutputStreamDefinition();
            streamDefinitionMap.putIfAbsent(streamDefinition.getId(), streamDefinition);
            DefinitionParserHelper.validateOutputStream(streamDefinition, streamDefinitionMap.get(streamDefinition
                    .getId()));
            StreamJunction outputStreamJunction = streamJunctionMap.get(streamDefinition.getId());

            if (outputStreamJunction == null) {
                outputStreamJunction = new StreamJunction(streamDefinition,
                        executionPlanContext.getExecutorService(),
                        executionPlanContext.getBufferSize(), executionPlanContext);
                streamJunctionMap.putIfAbsent(streamDefinition.getId(), outputStreamJunction);
            }
            insertIntoStreamCallback.init(streamJunctionMap.get(insertIntoStreamCallback.getOutputStreamDefinition()
                    .getId()));
        } else if (outputCallback != null && outputCallback instanceof InsertIntoWindowCallback) {
            InsertIntoWindowCallback insertIntoWindowCallback = (InsertIntoWindowCallback) outputCallback;
            StreamDefinition streamDefinition = insertIntoWindowCallback.getOutputStreamDefinition();
            windowDefinitionMap.putIfAbsent(streamDefinition.getId(), streamDefinition);
            DefinitionParserHelper.validateOutputStream(streamDefinition, windowDefinitionMap.get(streamDefinition
                    .getId()));
            StreamJunction outputStreamJunction = streamJunctionMap.get(streamDefinition.getId());

            if (outputStreamJunction == null) {
                outputStreamJunction = new StreamJunction(streamDefinition,
                        executionPlanContext.getExecutorService(),
                        executionPlanContext.getBufferSize(), executionPlanContext);
                streamJunctionMap.putIfAbsent(streamDefinition.getId(), outputStreamJunction);
            }
            insertIntoWindowCallback.getWindow().setPublisher(streamJunctionMap.get(insertIntoWindowCallback
                    .getOutputStreamDefinition().getId()).constructPublisher());
        }

        return queryRuntime.getQueryId();
    }

    public void defineFunction(FunctionDefinition functionDefinition) {
        DefinitionParserHelper.addFunction(executionPlanContext, functionDefinition);
    }

    public void setExecutionPlanRuntimeMap(ConcurrentMap<String, ExecutionPlanRuntime> executionPlanRuntimeMap) {
        this.executionPlanRuntimeMap = executionPlanRuntimeMap;
    }

    public ConcurrentMap<String, StreamJunction> getStreamJunctions() {
        return streamJunctionMap;
    }

    public ConcurrentMap<String, Table> getTableMap() {
        return tableMap;
    }

    public ConcurrentMap<String, Window> getEventWindowMap() {
        return eventWindowMap;
    }

    public ConcurrentMap<String, AbstractDefinition> getStreamDefinitionMap() {
        return streamDefinitionMap;
    }

    public ConcurrentMap<String, AbstractDefinition> getTableDefinitionMap() {
        return tableDefinitionMap;
    }

    public ConcurrentMap<String, List<Source>> getEventSourceMap() {
        return eventSourceMap;
    }

    public ConcurrentMap<String, List<Sink>> getEventSinkMap() {
        return eventSinkMap;
    }

    public ConcurrentMap<String, AbstractDefinition> getWindowDefinitionMap() {
        return windowDefinitionMap;
    }

    public LockSynchronizer getLockSynchronizer() {
        return lockSynchronizer;
    }

    public ExecutionPlanRuntime build() {
        return new ExecutionPlanRuntime(streamDefinitionMap, tableDefinitionMap, inputManager, queryProcessorMap,
                streamJunctionMap, tableMap, eventSourceMap, eventSinkMap, partitionMap, executionPlanContext,
                executionPlanRuntimeMap);
    }

}
