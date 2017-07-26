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

import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.aggregation.AggregationRuntime;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.partition.PartitionRuntime;
import org.wso2.siddhi.core.query.QueryRuntime;
import org.wso2.siddhi.core.query.input.ProcessStreamReceiver;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.core.query.output.callback.InsertIntoStreamCallback;
import org.wso2.siddhi.core.query.output.callback.InsertIntoWindowCallback;
import org.wso2.siddhi.core.query.output.callback.OutputCallback;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.stream.input.InputManager;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.trigger.EventTrigger;
import org.wso2.siddhi.core.util.lock.LockSynchronizer;
import org.wso2.siddhi.core.util.parser.AggregationParser;
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
 * For building SiddhiAppRuntime
 */
public class SiddhiAppRuntimeBuilder {
    private ConcurrentMap<String, AbstractDefinition> streamDefinitionMap =
            new ConcurrentHashMap<String, AbstractDefinition>(); //contains stream definition
    private ConcurrentMap<String, AbstractDefinition> tableDefinitionMap =
            new ConcurrentHashMap<String, AbstractDefinition>(); //contains table definition
    private ConcurrentMap<String, AbstractDefinition> windowDefinitionMap =
            new ConcurrentHashMap<String, AbstractDefinition>(); //contains window definition
    private ConcurrentMap<String, AbstractDefinition> aggregationDefinitionMap =
            new ConcurrentHashMap<String, AbstractDefinition>(); //contains aggregation definition
    private ConcurrentMap<String, TriggerDefinition> triggerDefinitionMap =
            new ConcurrentHashMap<String, TriggerDefinition>(); //contains trigger definition
    private Map<String, QueryRuntime> queryProcessorMap =
            Collections.synchronizedMap(new LinkedHashMap<String, QueryRuntime>());  //contains query processors
    private ConcurrentMap<String, StreamJunction> streamJunctionMap =
            new ConcurrentHashMap<String, StreamJunction>(); //contains stream junctions
    private ConcurrentMap<String, List<Source>> sourceMap =
            new ConcurrentHashMap<String, List<Source>>(); //contains sources
    private ConcurrentMap<String, List<Sink>> sinkMap =
            new ConcurrentHashMap<String, List<Sink>>(); //contains sinks
    private ConcurrentMap<String, Table> tableMap =
            new ConcurrentHashMap<String, Table>(); //contains tables
    private ConcurrentMap<String, Window> windowMap =
            new ConcurrentHashMap<String, Window>(); //contains windows
    private ConcurrentMap<String, AggregationRuntime> aggregationMap =
            new ConcurrentHashMap<String, AggregationRuntime>(); //contains aggregation runtime
    private ConcurrentMap<String, EventTrigger> triggerMap =
            new ConcurrentHashMap<String, EventTrigger>(); //contains triggers
    private ConcurrentMap<String, PartitionRuntime> partitionMap =
            new ConcurrentHashMap<String, PartitionRuntime>(); //contains partitions
    private ConcurrentMap<String, SiddhiAppRuntime> siddhiAppRuntimeMap = null;
    private SiddhiAppContext siddhiAppContext;
    private InputManager inputManager;
    private LockSynchronizer lockSynchronizer = new LockSynchronizer();

    public SiddhiAppRuntimeBuilder(SiddhiAppContext siddhiAppContext) {
        this.siddhiAppContext = siddhiAppContext;
        this.inputManager = new InputManager(this.siddhiAppContext, streamDefinitionMap, streamJunctionMap);
    }

    public void defineStream(StreamDefinition streamDefinition) {
        DefinitionParserHelper.validateDefinition(streamDefinition, streamDefinitionMap, tableDefinitionMap,
                windowDefinitionMap, aggregationDefinitionMap);
        AbstractDefinition currentDefinition = streamDefinitionMap
                .putIfAbsent(streamDefinition.getId(), streamDefinition);
        if (currentDefinition != null) {
            streamDefinition = (StreamDefinition) currentDefinition;
        }
        DefinitionParserHelper.addStreamJunction(streamDefinition, streamJunctionMap, siddhiAppContext);
        DefinitionParserHelper.addEventSource(streamDefinition, sourceMap, siddhiAppContext);
        DefinitionParserHelper.addEventSink(streamDefinition, sinkMap, siddhiAppContext);
    }

    public void defineTable(TableDefinition tableDefinition) {
        DefinitionParserHelper.validateDefinition(tableDefinition, streamDefinitionMap, tableDefinitionMap,
                windowDefinitionMap, aggregationDefinitionMap);
        AbstractDefinition currentDefinition = tableDefinitionMap.putIfAbsent(tableDefinition.getId(), tableDefinition);
        if (currentDefinition != null) {
            tableDefinition = (TableDefinition) currentDefinition;
        }
        DefinitionParserHelper.addTable(tableDefinition, tableMap, siddhiAppContext);
    }

    public void defineWindow(WindowDefinition windowDefinition) {
        DefinitionParserHelper.validateDefinition(windowDefinition, streamDefinitionMap, tableDefinitionMap,
                windowDefinitionMap, aggregationDefinitionMap);
        DefinitionParserHelper.addStreamJunction(windowDefinition, streamJunctionMap, siddhiAppContext);
        AbstractDefinition currentDefinition = windowDefinitionMap
                .putIfAbsent(windowDefinition.getId(), windowDefinition);
        if (currentDefinition != null) {
            windowDefinition = (WindowDefinition) currentDefinition;
        }
        DefinitionParserHelper.addWindow(windowDefinition, windowMap, siddhiAppContext);
        // defineStream(windowDefinition);
        // DefinitionParserHelper.addStreamJunction(windowDefinition, streamJunctionMap, siddhiAppContext);
    }

    public void defineTrigger(TriggerDefinition triggerDefinition) {
        DefinitionParserHelper.validateDefinition(triggerDefinition);
        TriggerDefinition currentDefinition = triggerDefinitionMap.putIfAbsent(triggerDefinition.getId(),
                triggerDefinition);
        if (currentDefinition != null) {
            triggerDefinition = currentDefinition;
        }
        DefinitionParserHelper.addEventTrigger(triggerDefinition, triggerMap, streamJunctionMap,
                siddhiAppContext);
    }

    public void defineAggregation(AggregationDefinition aggregationDefinition) {
        AggregationRuntime aggregationRuntime = AggregationParser.parse(aggregationDefinition, siddhiAppContext,
                streamDefinitionMap, tableDefinitionMap, windowDefinitionMap, aggregationDefinitionMap, tableMap,
                windowMap, aggregationMap, this);
        DefinitionParserHelper.validateDefinition(aggregationDefinition, streamDefinitionMap, tableDefinitionMap,
                windowDefinitionMap, aggregationDefinitionMap);
        aggregationDefinitionMap.putIfAbsent(aggregationDefinition.getId(), aggregationDefinition);
        ProcessStreamReceiver processStreamReceiver = aggregationRuntime.getSingleStreamRuntime().
                getProcessStreamReceiver();
        streamJunctionMap.get(processStreamReceiver.getStreamId()).subscribe(processStreamReceiver);
        aggregationMap.putIfAbsent(aggregationDefinition.getId(), aggregationRuntime);
    }

    public void addPartition(PartitionRuntime partitionRuntime) {
        partitionMap.put(partitionRuntime.getPartitionId(), partitionRuntime);
    }

    public String addQuery(QueryRuntime queryRuntime) {
        queryProcessorMap.put(queryRuntime.getQueryId(), queryRuntime);
        StreamRuntime streamRuntime = queryRuntime.getStreamRuntime();

        for (SingleStreamRuntime singleStreamRuntime : streamRuntime.getSingleStreamRuntimes()) {
            ProcessStreamReceiver processStreamReceiver = singleStreamRuntime.getProcessStreamReceiver();
            if (processStreamReceiver.toStream()) {
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
                        siddhiAppContext.getExecutorService(),
                        siddhiAppContext.getBufferSize(), siddhiAppContext);
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
                        siddhiAppContext.getExecutorService(),
                        siddhiAppContext.getBufferSize(), siddhiAppContext);
                streamJunctionMap.putIfAbsent(streamDefinition.getId(), outputStreamJunction);
            }
            insertIntoWindowCallback.getWindow().setPublisher(streamJunctionMap.get(insertIntoWindowCallback
                    .getOutputStreamDefinition().getId()).constructPublisher());
        }

        return queryRuntime.getQueryId();
    }

    public void defineFunction(FunctionDefinition functionDefinition) {
        DefinitionParserHelper.addFunction(siddhiAppContext, functionDefinition);
    }

    public void setSiddhiAppRuntimeMap(ConcurrentMap<String, SiddhiAppRuntime> siddhiAppRuntimeMap) {
        this.siddhiAppRuntimeMap = siddhiAppRuntimeMap;
    }

    public ConcurrentMap<String, StreamJunction> getStreamJunctions() {
        return streamJunctionMap;
    }

    public ConcurrentMap<String, Table> getTableMap() {
        return tableMap;
    }

    public ConcurrentMap<String, Window> getWindowMap() {
        return windowMap;
    }

    public ConcurrentMap<String, AggregationRuntime> getAggregationMap() {
        return aggregationMap;
    }

    public ConcurrentMap<String, AbstractDefinition> getStreamDefinitionMap() {
        return streamDefinitionMap;
    }

    public ConcurrentMap<String, AbstractDefinition> getTableDefinitionMap() {
        return tableDefinitionMap;
    }

    public ConcurrentMap<String, List<Source>> getSourceMap() {
        return sourceMap;
    }

    public ConcurrentMap<String, List<Sink>> getSinkMap() {
        return sinkMap;
    }

    public ConcurrentMap<String, AbstractDefinition> getWindowDefinitionMap() {
        return windowDefinitionMap;
    }

    public ConcurrentMap<String, AbstractDefinition> getAggregationDefinitionMap() {
        return aggregationDefinitionMap;
    }

    public LockSynchronizer getLockSynchronizer() {
        return lockSynchronizer;
    }

    public SiddhiAppRuntime build() {
        return new SiddhiAppRuntime(streamDefinitionMap, tableDefinitionMap, inputManager, queryProcessorMap,
                streamJunctionMap, tableMap, windowMap, aggregationMap, sourceMap, sinkMap, partitionMap,
                siddhiAppContext, siddhiAppRuntimeMap);
    }

}
