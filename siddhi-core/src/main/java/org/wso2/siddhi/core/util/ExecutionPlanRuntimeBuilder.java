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

package org.wso2.siddhi.core.util;

import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.partition.PartitionRuntime;
import org.wso2.siddhi.core.query.QueryRuntime;
import org.wso2.siddhi.core.query.input.ProcessStreamReceiver;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.core.query.output.callback.InsertIntoStreamCallback;
import org.wso2.siddhi.core.query.output.callback.OutputCallback;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.stream.input.InputManager;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.trigger.EventTrigger;
import org.wso2.siddhi.core.util.parser.helper.DefinitionParserHelper;
import org.wso2.siddhi.query.api.definition.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * For building ExecutionPlanRuntime
 */
public class ExecutionPlanRuntimeBuilder {
    private ConcurrentMap<String, AbstractDefinition> streamDefinitionMap = new ConcurrentHashMap<String, AbstractDefinition>(); //contains stream definition
    private ConcurrentMap<String, AbstractDefinition> tableDefinitionMap = new ConcurrentHashMap<String, AbstractDefinition>(); //contains table definition
    private ConcurrentMap<String, TriggerDefinition> triggerDefinitionMap = new ConcurrentHashMap<String, TriggerDefinition>(); //contains trigger definition
    private ConcurrentMap<String, QueryRuntime> queryProcessorMap = new ConcurrentHashMap<String, QueryRuntime>();
    private ConcurrentMap<String, StreamJunction> streamJunctionMap = new ConcurrentHashMap<String, StreamJunction>(); //contains stream junctions
    private ConcurrentMap<String, EventTable> eventTableMap = new ConcurrentHashMap<String, EventTable>(); //contains event tables
    private ConcurrentMap<String, EventTrigger> eventTriggerMap = new ConcurrentHashMap<String, EventTrigger>(); //contains event tables
    private ConcurrentMap<String, PartitionRuntime> partitionMap = new ConcurrentHashMap<String, PartitionRuntime>(); //contains partitions
    private ConcurrentMap<String, ExecutionPlanRuntime> executionPlanRuntimeMap = null;
    private ExecutionPlanContext executionPlanContext;
    private InputManager inputManager;

    public ExecutionPlanRuntimeBuilder(ExecutionPlanContext executionPlanContext) {
        this.executionPlanContext = executionPlanContext;
        this.inputManager = new InputManager(this.executionPlanContext, streamDefinitionMap, streamJunctionMap);
    }

    public void defineStream(StreamDefinition streamDefinition) {
        DefinitionParserHelper.validateDefinition(streamDefinition, streamDefinitionMap, tableDefinitionMap);
        if (!streamDefinitionMap.containsKey(streamDefinition.getId())) {
            streamDefinitionMap.putIfAbsent(streamDefinition.getId(), streamDefinition);
        }
        DefinitionParserHelper.addStreamJunction(streamDefinition, streamJunctionMap, executionPlanContext);
    }

    public void defineTable(TableDefinition tableDefinition) {
        DefinitionParserHelper.validateDefinition(tableDefinition, streamDefinitionMap, tableDefinitionMap);
        if (!tableDefinitionMap.containsKey(tableDefinition.getId())) {
            tableDefinitionMap.putIfAbsent(tableDefinition.getId(), tableDefinition);
        }
        DefinitionParserHelper.addEventTable(tableDefinition, eventTableMap, executionPlanContext);
    }

    public void defineTrigger(TriggerDefinition triggerDefinition) {
        DefinitionParserHelper.validateDefinition(triggerDefinition);
        triggerDefinitionMap.putIfAbsent(triggerDefinition.getId(), triggerDefinition);
        DefinitionParserHelper.addEventTrigger(triggerDefinition, eventTriggerMap, streamJunctionMap, executionPlanContext);
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
            DefinitionParserHelper.validateOutputStream(streamDefinition, streamDefinitionMap.get(streamDefinition.getId()));
            StreamJunction outputStreamJunction = streamJunctionMap.get(streamDefinition.getId());

            if (outputStreamJunction == null) {
                outputStreamJunction = new StreamJunction(streamDefinition,
                        executionPlanContext.getExecutorService(),
                        executionPlanContext.getBufferSize(), executionPlanContext);
                streamJunctionMap.putIfAbsent(streamDefinition.getId(), outputStreamJunction);
            }
            insertIntoStreamCallback.init(streamJunctionMap.get(insertIntoStreamCallback.getOutputStreamDefinition().getId()));
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

    public ConcurrentMap<String, EventTable> getEventTableMap() {
        return eventTableMap;
    }

    public ConcurrentMap<String, AbstractDefinition> getStreamDefinitionMap() {
        return streamDefinitionMap;
    }

    public ConcurrentMap<String, AbstractDefinition> getTableDefinitionMap() {
        return tableDefinitionMap;
    }


    public ExecutionPlanRuntime build() {
        return new ExecutionPlanRuntime(streamDefinitionMap, tableDefinitionMap, inputManager, queryProcessorMap, streamJunctionMap, eventTableMap, partitionMap, executionPlanContext, executionPlanRuntimeMap);
    }
}
