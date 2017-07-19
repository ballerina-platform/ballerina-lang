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
package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.input.ProcessStreamReceiver;
import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.core.query.input.stream.state.CountPostStateProcessor;
import org.wso2.siddhi.core.query.input.stream.state.CountPreStateProcessor;
import org.wso2.siddhi.core.query.input.stream.state.LogicalPostStateProcessor;
import org.wso2.siddhi.core.query.input.stream.state.LogicalPreStateProcessor;
import org.wso2.siddhi.core.query.input.stream.state.StateStreamRuntime;
import org.wso2.siddhi.core.query.input.stream.state.StreamPostStateProcessor;
import org.wso2.siddhi.core.query.input.stream.state.StreamPreStateProcessor;
import org.wso2.siddhi.core.query.input.stream.state.receiver.PatternMultiProcessStreamReceiver;
import org.wso2.siddhi.core.query.input.stream.state.receiver.PatternSingleProcessStreamReceiver;
import org.wso2.siddhi.core.query.input.stream.state.receiver.SequenceMultiProcessStreamReceiver;
import org.wso2.siddhi.core.query.input.stream.state.receiver.SequenceSingleProcessStreamReceiver;
import org.wso2.siddhi.core.query.input.stream.state.runtime.CountInnerStateRuntime;
import org.wso2.siddhi.core.query.input.stream.state.runtime.EveryInnerStateRuntime;
import org.wso2.siddhi.core.query.input.stream.state.runtime.InnerStateRuntime;
import org.wso2.siddhi.core.query.input.stream.state.runtime.LogicalInnerStateRuntime;
import org.wso2.siddhi.core.query.input.stream.state.runtime.NextInnerStateRuntime;
import org.wso2.siddhi.core.query.input.stream.state.runtime.StreamInnerStateRuntime;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.execution.query.input.state.CountStateElement;
import org.wso2.siddhi.query.api.execution.query.input.state.EveryStateElement;
import org.wso2.siddhi.query.api.execution.query.input.state.LogicalStateElement;
import org.wso2.siddhi.query.api.execution.query.input.state.NextStateElement;
import org.wso2.siddhi.query.api.execution.query.input.state.StateElement;
import org.wso2.siddhi.query.api.execution.query.input.state.StreamStateElement;
import org.wso2.siddhi.query.api.execution.query.input.stream.BasicSingleInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.StateInputStream;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to parse {@link StateStreamRuntime}
 */
public class StateInputStreamParser {


    public static StateStreamRuntime parseInputStream(StateInputStream stateInputStream,
                                                      SiddhiAppContext siddhiAppContext,
                                                      MetaStateEvent metaStateEvent,
                                                      Map<String, AbstractDefinition> streamDefinitionMap,
                                                      Map<String, AbstractDefinition> tableDefinitionMap,
                                                      Map<String, AbstractDefinition> windowDefinitionMap,
                                                      Map<String, AbstractDefinition> aggregationDefinitionMap,
                                                      Map<String, Table> tableMap,
                                                      List<VariableExpressionExecutor> variableExpressionExecutors,
                                                      LatencyTracker latencyTracker, String queryName) {

        Map<String, ProcessStreamReceiver> processStreamReceiverMap = new HashMap<String, ProcessStreamReceiver>();

        StateStreamRuntime stateStreamRuntime = new StateStreamRuntime(siddhiAppContext, metaStateEvent);

        String defaultLockKey = "";

        for (String streamId : stateInputStream.getAllStreamIds()) {
            int streamCount = stateInputStream.getStreamCount(streamId);
            if (streamCount == 1) {
                if (stateInputStream.getStateType() == StateInputStream.Type.SEQUENCE) {
                    processStreamReceiverMap.put(streamId, new SequenceSingleProcessStreamReceiver(streamId,
                            stateStreamRuntime, defaultLockKey, latencyTracker, queryName));
                } else {
                    processStreamReceiverMap.put(streamId, new PatternSingleProcessStreamReceiver(streamId,
                            defaultLockKey, latencyTracker, queryName));
                }
            } else {
                if (stateInputStream.getStateType() == StateInputStream.Type.SEQUENCE) {
                    processStreamReceiverMap.put(streamId, new SequenceMultiProcessStreamReceiver(streamId,
                            streamCount, stateStreamRuntime, latencyTracker, queryName));
                } else {
                    processStreamReceiverMap.put(streamId, new PatternMultiProcessStreamReceiver(streamId,
                            streamCount, latencyTracker, queryName));
                }
            }
        }

        StateElement stateElement = stateInputStream.getStateElement();

        InnerStateRuntime innerStateRuntime = parse(stateElement, streamDefinitionMap, tableDefinitionMap,
                windowDefinitionMap, aggregationDefinitionMap, tableMap, metaStateEvent,
                siddhiAppContext, variableExpressionExecutors, processStreamReceiverMap, null, null,
                stateInputStream.getStateType(),
                new ArrayList<Map.Entry<Long, Set<Integer>>>(), latencyTracker, queryName);

        stateStreamRuntime.setInnerStateRuntime(innerStateRuntime);

        ((StreamPreStateProcessor) innerStateRuntime.getFirstProcessor()).setThisLastProcessor(
                (StreamPostStateProcessor) innerStateRuntime.getLastProcessor());

        return stateStreamRuntime;
    }

    private static InnerStateRuntime parse(StateElement stateElement,
                                           Map<String, AbstractDefinition> streamDefinitionMap,
                                           Map<String, AbstractDefinition> tableDefinitionMap,
                                           Map<String, AbstractDefinition> windowDefinitionMap,
                                           Map<String, AbstractDefinition> aggregationDefinitionMap,
                                           Map<String, Table> tableMap,
                                           MetaStateEvent metaStateEvent, SiddhiAppContext siddhiAppContext,
                                           List<VariableExpressionExecutor> variableExpressionExecutors,
                                           Map<String, ProcessStreamReceiver> processStreamReceiverMap,
                                           StreamPreStateProcessor streamPreStateProcessor,
                                           StreamPostStateProcessor streamPostStateProcessor,
                                           StateInputStream.Type stateType,
                                           ArrayList<Map.Entry<Long, Set<Integer>>> withinStates,
                                           LatencyTracker latencyTracker, String queryName) {


        if (stateElement instanceof StreamStateElement) {

            BasicSingleInputStream basicSingleInputStream = ((StreamStateElement) stateElement)
                    .getBasicSingleInputStream();
            SingleStreamRuntime singleStreamRuntime = SingleInputStreamParser.parseInputStream(
                    basicSingleInputStream, siddhiAppContext, variableExpressionExecutors, streamDefinitionMap,
                    tableDefinitionMap, windowDefinitionMap, aggregationDefinitionMap, tableMap, metaStateEvent,
                    processStreamReceiverMap.get(basicSingleInputStream.getUniqueStreamIds().get(0)),
                    false, false, queryName);

            int stateIndex = metaStateEvent.getStreamEventCount() - 1;
            if (streamPreStateProcessor == null) {

                if (stateElement.getWithin() != null) {
                    Set<Integer> withinStateset = new HashSet<Integer>();
                    withinStateset.add(SiddhiConstants.ANY);
                    withinStates.add(0, new AbstractMap.SimpleEntry<Long, Set<Integer>>(stateElement.getWithin()
                            .getValue(), withinStateset));
                }

                streamPreStateProcessor = new StreamPreStateProcessor(stateType, clonewithinStates(withinStates));
                streamPreStateProcessor.init(siddhiAppContext, queryName);

                if (stateElement.getWithin() != null) {
                    withinStates.remove(0);
                }
            }
            streamPreStateProcessor.setStateId(stateIndex);
            streamPreStateProcessor.setNextProcessor(singleStreamRuntime.getProcessorChain());
            singleStreamRuntime.setProcessorChain(streamPreStateProcessor);
            if (streamPostStateProcessor == null) {
                streamPostStateProcessor = new StreamPostStateProcessor();
            }
            streamPostStateProcessor.setStateId(stateIndex);
            singleStreamRuntime.getProcessorChain().setToLast(streamPostStateProcessor);
            streamPostStateProcessor.setThisStatePreProcessor(streamPreStateProcessor);
            streamPreStateProcessor.setThisStatePostProcessor(streamPostStateProcessor);
            streamPreStateProcessor.setThisLastProcessor(streamPostStateProcessor);

            StreamInnerStateRuntime innerStateRuntime = new StreamInnerStateRuntime(stateType);

            innerStateRuntime.setFirstProcessor(streamPreStateProcessor);
            innerStateRuntime.setLastProcessor(streamPostStateProcessor);
            innerStateRuntime.addStreamRuntime(singleStreamRuntime);

            return innerStateRuntime;

        } else if (stateElement instanceof NextStateElement) {

            StateElement currentElement = ((NextStateElement) stateElement).getStateElement();
            InnerStateRuntime currentInnerStateRuntime = parse(currentElement, streamDefinitionMap,
                    tableDefinitionMap, windowDefinitionMap, aggregationDefinitionMap, tableMap, metaStateEvent,
                    siddhiAppContext, variableExpressionExecutors,
                    processStreamReceiverMap,
                    streamPreStateProcessor, streamPostStateProcessor,
                    stateType, withinStates, latencyTracker, queryName);

            if (stateElement.getWithin() != null) {
                Set<Integer> withinStateSet = new HashSet<Integer>();
                withinStateSet.add(currentInnerStateRuntime.getFirstProcessor().getStateId());
                withinStateSet.add(currentInnerStateRuntime.getLastProcessor().getStateId());
                withinStates.add(0, new AbstractMap.SimpleEntry<Long, Set<Integer>>(stateElement.getWithin().getValue
                        (), withinStateSet));
            }

            StateElement nextElement = ((NextStateElement) stateElement).getNextStateElement();
            InnerStateRuntime nextInnerStateRuntime = parse(nextElement, streamDefinitionMap, tableDefinitionMap,
                    windowDefinitionMap, aggregationDefinitionMap, tableMap, metaStateEvent,
                    siddhiAppContext, variableExpressionExecutors, processStreamReceiverMap,
                    streamPreStateProcessor,
                    streamPostStateProcessor, stateType, withinStates, latencyTracker, queryName);

            if (stateElement.getWithin() != null) {
                withinStates.remove(0);
            }
//            currentInnerStateRuntime.getFirstProcessor().getStateId()
            currentInnerStateRuntime.getLastProcessor().setNextStatePreProcessor(nextInnerStateRuntime
                    .getFirstProcessor());

            NextInnerStateRuntime nextStateRuntime = new NextInnerStateRuntime(currentInnerStateRuntime,
                    nextInnerStateRuntime, stateType);
            nextStateRuntime.setFirstProcessor(currentInnerStateRuntime.getFirstProcessor());
            nextStateRuntime.setLastProcessor(nextInnerStateRuntime.getLastProcessor());

            for (SingleStreamRuntime singleStreamRuntime : currentInnerStateRuntime.getSingleStreamRuntimeList()) {
                nextStateRuntime.addStreamRuntime(singleStreamRuntime);
            }
            for (SingleStreamRuntime singleStreamRuntime : nextInnerStateRuntime.getSingleStreamRuntimeList()) {
                nextStateRuntime.addStreamRuntime(singleStreamRuntime);
            }

            return nextStateRuntime;

        } else if (stateElement instanceof EveryStateElement) {

            StateElement currentElement = ((EveryStateElement) stateElement).getStateElement();
            InnerStateRuntime innerStateRuntime = parse(currentElement, streamDefinitionMap, tableDefinitionMap,
                    windowDefinitionMap, aggregationDefinitionMap, tableMap, metaStateEvent,
                    siddhiAppContext, variableExpressionExecutors, processStreamReceiverMap,
                    streamPreStateProcessor,
                    streamPostStateProcessor, stateType, withinStates, latencyTracker, queryName);

            EveryInnerStateRuntime everyInnerStateRuntime = new EveryInnerStateRuntime(innerStateRuntime, stateType);

            everyInnerStateRuntime.setFirstProcessor(innerStateRuntime.getFirstProcessor());
            everyInnerStateRuntime.setLastProcessor(innerStateRuntime.getLastProcessor());

            for (SingleStreamRuntime singleStreamRuntime : innerStateRuntime.getSingleStreamRuntimeList()) {
                everyInnerStateRuntime.addStreamRuntime(singleStreamRuntime);
            }
            everyInnerStateRuntime.getLastProcessor().setNextEveryStatePerProcessor(everyInnerStateRuntime
                    .getFirstProcessor());
            return everyInnerStateRuntime;

        } else if (stateElement instanceof LogicalStateElement) {

            LogicalStateElement.Type type = ((LogicalStateElement) stateElement).getType();

            if (stateElement.getWithin() != null) {
                Set<Integer> withinStateset = new HashSet<Integer>();
                withinStateset.add(SiddhiConstants.ANY);
                withinStates.add(0, new AbstractMap.SimpleEntry<Long, Set<Integer>>(stateElement.getWithin().getValue
                        (), withinStateset));
            }

            LogicalPreStateProcessor logicalPreStateProcessor1 = new LogicalPreStateProcessor(type, stateType,
                    clonewithinStates(withinStates));
            logicalPreStateProcessor1.init(siddhiAppContext, queryName);
            LogicalPostStateProcessor logicalPostStateProcessor1 = new LogicalPostStateProcessor(type);

            LogicalPreStateProcessor logicalPreStateProcessor2 = new LogicalPreStateProcessor(type, stateType,
                    clonewithinStates(withinStates));
            logicalPreStateProcessor2.init(siddhiAppContext, queryName);
            LogicalPostStateProcessor logicalPostStateProcessor2 = new LogicalPostStateProcessor(type);

            if (stateElement.getWithin() != null) {
                withinStates.remove(0);
            }

            logicalPostStateProcessor1.setPartnerPreStateProcessor(logicalPreStateProcessor2);
            logicalPostStateProcessor2.setPartnerPreStateProcessor(logicalPreStateProcessor1);

            logicalPostStateProcessor1.setPartnerPostStateProcessor(logicalPostStateProcessor2);
            logicalPostStateProcessor2.setPartnerPostStateProcessor(logicalPostStateProcessor1);

            logicalPreStateProcessor1.setPartnerStatePreProcessor(logicalPreStateProcessor2);
            logicalPreStateProcessor2.setPartnerStatePreProcessor(logicalPreStateProcessor1);

            StateElement stateElement2 = ((LogicalStateElement) stateElement).getStreamStateElement2();
            InnerStateRuntime innerStateRuntime2 = parse(stateElement2, streamDefinitionMap, tableDefinitionMap,
                    windowDefinitionMap, aggregationDefinitionMap, tableMap, metaStateEvent,
                    siddhiAppContext, variableExpressionExecutors, processStreamReceiverMap,
                    logicalPreStateProcessor2, logicalPostStateProcessor2,
                    stateType, withinStates, latencyTracker,
                    queryName);

            StateElement stateElement1 = ((LogicalStateElement) stateElement).getStreamStateElement1();
            InnerStateRuntime innerStateRuntime1 = parse(stateElement1, streamDefinitionMap, tableDefinitionMap,
                    windowDefinitionMap, aggregationDefinitionMap, tableMap, metaStateEvent,
                    siddhiAppContext, variableExpressionExecutors, processStreamReceiverMap,
                    logicalPreStateProcessor1, logicalPostStateProcessor1, stateType, withinStates, latencyTracker,
                    queryName);


            LogicalInnerStateRuntime logicalInnerStateRuntime = new LogicalInnerStateRuntime(
                    innerStateRuntime1, innerStateRuntime2, stateType);

            logicalInnerStateRuntime.setFirstProcessor(innerStateRuntime1.getFirstProcessor());
            logicalInnerStateRuntime.setLastProcessor(innerStateRuntime2.getLastProcessor());

            for (SingleStreamRuntime singleStreamRuntime : innerStateRuntime2.getSingleStreamRuntimeList()) {
                logicalInnerStateRuntime.addStreamRuntime(singleStreamRuntime);
            }

            for (SingleStreamRuntime singleStreamRuntime : innerStateRuntime1.getSingleStreamRuntimeList()) {
                logicalInnerStateRuntime.addStreamRuntime(singleStreamRuntime);
            }

            return logicalInnerStateRuntime;

        } else if (stateElement instanceof CountStateElement) {

            int minCount = ((CountStateElement) stateElement).getMinCount();
            int maxCount = ((CountStateElement) stateElement).getMaxCount();
            if (minCount == SiddhiConstants.ANY) {
                minCount = 0;
            }
            if (maxCount == SiddhiConstants.ANY) {
                maxCount = Integer.MAX_VALUE;
            }

            if (stateElement.getWithin() != null) {
                Set<Integer> withinStateset = new HashSet<Integer>();
                withinStateset.add(SiddhiConstants.ANY);
                withinStates.add(0, new AbstractMap.SimpleEntry<Long, Set<Integer>>(stateElement.getWithin().getValue
                        (), withinStateset));
            }

            CountPreStateProcessor countPreStateProcessor = new CountPreStateProcessor(minCount, maxCount, stateType,
                    withinStates);
            countPreStateProcessor.init(siddhiAppContext, queryName);
            CountPostStateProcessor countPostStateProcessor = new CountPostStateProcessor(minCount, maxCount);

            if (stateElement.getWithin() != null) {
                withinStates.remove(0);
            }

            countPreStateProcessor.setCountPostStateProcessor(countPostStateProcessor);
            StateElement currentElement = ((CountStateElement) stateElement).getStreamStateElement();
            InnerStateRuntime innerStateRuntime = parse(currentElement, streamDefinitionMap, tableDefinitionMap,
                    windowDefinitionMap, aggregationDefinitionMap, tableMap, metaStateEvent,
                    siddhiAppContext, variableExpressionExecutors, processStreamReceiverMap,
                    countPreStateProcessor, countPostStateProcessor, stateType, withinStates, latencyTracker,
                    queryName);

            return new CountInnerStateRuntime((StreamInnerStateRuntime) innerStateRuntime);

        } else {
            throw new OperationNotSupportedException();
            //todo support not
        }

    }

    private static List<Map.Entry<Long, Set<Integer>>> clonewithinStates(List<Map.Entry<Long, Set<Integer>>>
                                                                                 withinStates) {
        List<Map.Entry<Long, Set<Integer>>> clonedwithinStates = new ArrayList<Map.Entry<Long, Set<Integer>>>
                (withinStates.size());
        for (Map.Entry<Long, Set<Integer>> entry : withinStates) {
            clonedwithinStates.add(new AbstractMap.SimpleEntry<Long, Set<Integer>>(entry.getKey(), new
                    HashSet<Integer>(entry.getValue())));
        }
        return clonedwithinStates;
    }

}
