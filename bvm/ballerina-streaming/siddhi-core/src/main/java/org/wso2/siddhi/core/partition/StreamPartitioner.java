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
package org.wso2.siddhi.core.partition;

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.executor.condition.ConditionExpressionExecutor;
import org.wso2.siddhi.core.partition.executor.PartitionExecutor;
import org.wso2.siddhi.core.partition.executor.RangePartitionExecutor;
import org.wso2.siddhi.core.partition.executor.ValuePartitionExecutor;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.parser.ExpressionParser;
import org.wso2.siddhi.query.api.execution.partition.Partition;
import org.wso2.siddhi.query.api.execution.partition.PartitionType;
import org.wso2.siddhi.query.api.execution.partition.RangePartitionType;
import org.wso2.siddhi.query.api.execution.partition.ValuePartitionType;
import org.wso2.siddhi.query.api.execution.query.input.state.CountStateElement;
import org.wso2.siddhi.query.api.execution.query.input.state.EveryStateElement;
import org.wso2.siddhi.query.api.execution.query.input.state.LogicalStateElement;
import org.wso2.siddhi.query.api.execution.query.input.state.NextStateElement;
import org.wso2.siddhi.query.api.execution.query.input.state.StateElement;
import org.wso2.siddhi.query.api.execution.query.input.state.StreamStateElement;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.JoinInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.SingleInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.StateInputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * create PartitionExecutors to be used to get partitioning key
 */
public class StreamPartitioner {

    private List<List<PartitionExecutor>> partitionExecutorLists = new ArrayList<List<PartitionExecutor>>();

    public StreamPartitioner(InputStream inputStream, Partition partition, MetaStateEvent metaEvent,
                             List<VariableExpressionExecutor> executors, SiddhiAppContext siddhiAppContext,
                             String queryName) {
        if (partition != null) {
            createExecutors(inputStream, partition, metaEvent, executors, siddhiAppContext, queryName);
        }
    }

    private void createExecutors(InputStream inputStream, Partition partition, MetaComplexEvent metaEvent,
                                 List<VariableExpressionExecutor> executors, SiddhiAppContext
                                         siddhiAppContext, String queryName) {
        if (inputStream instanceof SingleInputStream) {
            if (metaEvent instanceof MetaStateEvent) {
                createSingleInputStreamExecutors((SingleInputStream) inputStream, partition, ((MetaStateEvent)
                        metaEvent).getMetaStreamEvent(0), executors, null, siddhiAppContext, queryName);
            } else {
                createSingleInputStreamExecutors((SingleInputStream) inputStream, partition, (MetaStreamEvent)
                        metaEvent, executors, null, siddhiAppContext, queryName);
            }
        } else if (inputStream instanceof JoinInputStream) {
            createJoinInputStreamExecutors((JoinInputStream) inputStream, partition, (MetaStateEvent) metaEvent,
                    executors, siddhiAppContext, queryName);
        } else if (inputStream instanceof StateInputStream) {
            createStateInputStreamExecutors(((StateInputStream) inputStream).getStateElement(), partition,
                    (MetaStateEvent) metaEvent, executors, siddhiAppContext, 0, queryName);
        }
    }

    private int createStateInputStreamExecutors(StateElement stateElement, Partition partition, MetaStateEvent
            metaEvent, List<VariableExpressionExecutor> executors, SiddhiAppContext siddhiAppContext, int
            executorIndex, String queryName) {

        if (stateElement instanceof EveryStateElement) {
            return createStateInputStreamExecutors(((EveryStateElement) stateElement).getStateElement(), partition,
                    metaEvent, executors, siddhiAppContext, executorIndex, queryName);
        } else if (stateElement instanceof NextStateElement) {
            executorIndex = createStateInputStreamExecutors(((NextStateElement) stateElement).getStateElement(),
                    partition, metaEvent, executors, siddhiAppContext, executorIndex, queryName);
            return createStateInputStreamExecutors(((NextStateElement) stateElement).getNextStateElement(),
                    partition, metaEvent, executors, siddhiAppContext, executorIndex, queryName);
        } else if (stateElement instanceof LogicalStateElement) {
            executorIndex = createStateInputStreamExecutors(((LogicalStateElement) stateElement)
                    .getStreamStateElement1(), partition, metaEvent, executors, siddhiAppContext, executorIndex,
                    queryName);
            return createStateInputStreamExecutors(((LogicalStateElement) stateElement).getStreamStateElement2(),
                    partition, metaEvent, executors, siddhiAppContext, executorIndex, queryName);
        } else if (stateElement instanceof CountStateElement) {
            return createStateInputStreamExecutors(((CountStateElement) stateElement).getStreamStateElement(),
                    partition, metaEvent, executors, siddhiAppContext, executorIndex, queryName);
        } else {  //when stateElement is an instanceof StreamStateElement
            int size = executors.size();
            createExecutors(((StreamStateElement) stateElement).getBasicSingleInputStream(), partition, metaEvent
                    .getMetaStreamEvent(executorIndex), executors, siddhiAppContext, queryName);
            for (int j = size; j < executors.size(); j++) {
                executors.get(j).getPosition()[SiddhiConstants.STREAM_EVENT_CHAIN_INDEX] = executorIndex;
            }
            return ++executorIndex;
        }
    }

    private void createJoinInputStreamExecutors(JoinInputStream inputStream, Partition partition, MetaStateEvent
            metaEvent, List<VariableExpressionExecutor> executors, SiddhiAppContext siddhiAppContext, String
            queryName) {
        createExecutors(inputStream.getLeftInputStream(), partition, metaEvent.getMetaStreamEvent(0), executors,
                siddhiAppContext, queryName);
        int size = executors.size();
        for (VariableExpressionExecutor variableExpressionExecutor : executors) {
            variableExpressionExecutor.getPosition()[SiddhiConstants.STREAM_EVENT_CHAIN_INDEX] = 0;
        }
        createExecutors(inputStream.getRightInputStream(), partition, metaEvent.getMetaStreamEvent(1), executors,
                siddhiAppContext, queryName);
        for (int i = size; i < executors.size(); i++) {
            executors.get(i).getPosition()[SiddhiConstants.STREAM_EVENT_CHAIN_INDEX] = 1;
        }

    }

    private void createSingleInputStreamExecutors(SingleInputStream inputStream, Partition partition, MetaStreamEvent
            metaEvent, List<VariableExpressionExecutor> executors, Map<String, Table> tableMap,
                                                  SiddhiAppContext siddhiAppContext, String queryName) {
        List<PartitionExecutor> executorList = new ArrayList<PartitionExecutor>();
        partitionExecutorLists.add(executorList);
        if (!inputStream.isInnerStream()) {
            for (PartitionType partitionType : partition.getPartitionTypeMap().values()) {
                if (partitionType instanceof ValuePartitionType) {
                    if (partitionType.getStreamId().equals(inputStream.getStreamId())) {
                        executorList.add(new ValuePartitionExecutor(ExpressionParser.parseExpression((
                                (ValuePartitionType) partitionType).getExpression(),
                                metaEvent, SiddhiConstants.UNKNOWN_STATE, tableMap, executors,
                                siddhiAppContext, false, 0, queryName)));
                    }
                } else {
                    for (RangePartitionType.RangePartitionProperty rangePartitionProperty : ((RangePartitionType)
                            partitionType).getRangePartitionProperties()) {
                        if (partitionType.getStreamId().equals(inputStream.getStreamId())) {
                            executorList.add(new RangePartitionExecutor((ConditionExpressionExecutor)
                                    ExpressionParser.parseExpression(rangePartitionProperty.getCondition(), metaEvent,
                                            SiddhiConstants.UNKNOWN_STATE, tableMap, executors,
                                            siddhiAppContext, false, 0, queryName),
                                    rangePartitionProperty.getPartitionKey()));

                        }
                    }
                }
            }
        }
    }

    public List<List<PartitionExecutor>> getPartitionExecutorLists() {
        return partitionExecutorLists;
    }


}
