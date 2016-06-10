/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.input.MultiProcessStreamReceiver;
import org.wso2.siddhi.core.query.input.ProcessStreamReceiver;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.input.stream.join.JoinProcessor;
import org.wso2.siddhi.core.query.input.stream.join.JoinStreamRuntime;
import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.window.FindableProcessor;
import org.wso2.siddhi.core.query.processor.stream.window.LengthWindowProcessor;
import org.wso2.siddhi.core.query.processor.stream.window.TableWindowProcessor;
import org.wso2.siddhi.core.query.processor.stream.window.WindowProcessor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.execution.query.input.stream.JoinInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.SingleInputStream;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.constant.TimeConstant;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JoinInputStreamParser {


    public static StreamRuntime parseInputStream(JoinInputStream joinInputStream,
                                                 ExecutionPlanContext executionPlanContext,
                                                 Map<String, AbstractDefinition> streamDefinitionMap,
                                                 Map<String, AbstractDefinition> tableDefinitionMap,
                                                 Map<String, EventTable> eventTableMap,
                                                 List<VariableExpressionExecutor> executors,
                                                 LatencyTracker latencyTracker, boolean outputExpectsExpiredEvents) {

        ProcessStreamReceiver leftProcessStreamReceiver;
        ProcessStreamReceiver rightProcessStreamReceiver;

        MetaStreamEvent leftMetaStreamEvent = new MetaStreamEvent();
        MetaStreamEvent rightMetaStreamEvent = new MetaStreamEvent();

        String leftInputStreamId = ((SingleInputStream) joinInputStream.getLeftInputStream()).getStreamId();
        String rightInputStreamId = ((SingleInputStream) joinInputStream.getRightInputStream()).getStreamId();

        boolean leftOuterJoinProcessor = false;
        boolean rightOuterJoinProcessor = false;

        if (joinInputStream.getAllStreamIds().size() == 2) {
            if (!streamDefinitionMap.containsKey(leftInputStreamId)) {
                leftMetaStreamEvent.setTableEvent(true);
            }
            if (!streamDefinitionMap.containsKey(rightInputStreamId)) {
                rightMetaStreamEvent.setTableEvent(true);
            }
            leftProcessStreamReceiver = new ProcessStreamReceiver(leftInputStreamId, latencyTracker);
            rightProcessStreamReceiver = new ProcessStreamReceiver(rightInputStreamId, latencyTracker);
            if (leftMetaStreamEvent.isTableEvent() && rightMetaStreamEvent.isTableEvent()) {
                throw new ExecutionPlanCreationException("Both inputs of join are from static sources " + leftInputStreamId + " and " + rightInputStreamId);
            }
        } else {
            if (streamDefinitionMap.containsKey(joinInputStream.getAllStreamIds().get(0))) {
                rightProcessStreamReceiver = new MultiProcessStreamReceiver(joinInputStream.getAllStreamIds().get(0), 2, latencyTracker);
                leftProcessStreamReceiver = rightProcessStreamReceiver;
            } else {
                throw new ExecutionPlanCreationException("Input of join is from static source " + leftInputStreamId + " and " + rightInputStreamId);
            }
        }

        SingleStreamRuntime leftStreamRuntime = SingleInputStreamParser.parseInputStream(
                (SingleInputStream) joinInputStream.getLeftInputStream(), executionPlanContext, executors, streamDefinitionMap,
                !leftMetaStreamEvent.isTableEvent() ? null : tableDefinitionMap, eventTableMap, leftMetaStreamEvent, leftProcessStreamReceiver, true, latencyTracker, outputExpectsExpiredEvents);

        for (VariableExpressionExecutor variableExpressionExecutor : executors) {
            variableExpressionExecutor.getPosition()[SiddhiConstants.STREAM_EVENT_CHAIN_INDEX] = 0;
        }
        int size = executors.size();

        SingleStreamRuntime rightStreamRuntime = SingleInputStreamParser.parseInputStream(
                (SingleInputStream) joinInputStream.getRightInputStream(), executionPlanContext, executors, streamDefinitionMap,
                !rightMetaStreamEvent.isTableEvent() ? null : tableDefinitionMap, eventTableMap, rightMetaStreamEvent, rightProcessStreamReceiver, true, latencyTracker, outputExpectsExpiredEvents);

        for (int i = size; i < executors.size(); i++) {
            VariableExpressionExecutor variableExpressionExecutor = executors.get(i);
            variableExpressionExecutor.getPosition()[SiddhiConstants.STREAM_EVENT_CHAIN_INDEX] = 1;
        }

        if (leftMetaStreamEvent.isTableEvent()) {
            TableWindowProcessor tableWindowProcessor = new TableWindowProcessor(eventTableMap.get(leftInputStreamId));
            tableWindowProcessor.initProcessor(leftMetaStreamEvent.getLastInputDefinition(), new ExpressionExecutor[0], executionPlanContext, outputExpectsExpiredEvents);
            leftStreamRuntime.setProcessorChain(tableWindowProcessor);
        }
        if (rightMetaStreamEvent.isTableEvent()) {
            TableWindowProcessor tableWindowProcessor = new TableWindowProcessor(eventTableMap.get(rightInputStreamId));
            tableWindowProcessor.initProcessor(rightMetaStreamEvent.getLastInputDefinition(), new ExpressionExecutor[0], executionPlanContext, outputExpectsExpiredEvents);
            rightStreamRuntime.setProcessorChain(tableWindowProcessor);
        }

        MetaStateEvent metaStateEvent = new MetaStateEvent(2);
        metaStateEvent.addEvent(leftMetaStreamEvent);
        metaStateEvent.addEvent(rightMetaStreamEvent);

        switch (joinInputStream.getType()) {
            case FULL_OUTER_JOIN:
                leftOuterJoinProcessor = true;
            case RIGHT_OUTER_JOIN:
                rightOuterJoinProcessor = true;
                break;
            case LEFT_OUTER_JOIN:
                leftOuterJoinProcessor = true;
                break;
        }

        Lock joinLock = new ReentrantLock();
        JoinProcessor leftPreJoinProcessor = new JoinProcessor(true, true, leftOuterJoinProcessor);
        JoinProcessor leftPostJoinProcessor = new JoinProcessor(true, false, leftOuterJoinProcessor);

        FindableProcessor leftFindableProcessor = insertJoinProcessorsAndGetFindable(leftPreJoinProcessor, leftPostJoinProcessor, leftStreamRuntime, executionPlanContext, outputExpectsExpiredEvents);

        JoinProcessor rightPreJoinProcessor = new JoinProcessor(false, true, rightOuterJoinProcessor);
        JoinProcessor rightPostJoinProcessor = new JoinProcessor(false, false, rightOuterJoinProcessor);

        FindableProcessor rightFindableProcessor = insertJoinProcessorsAndGetFindable(rightPreJoinProcessor, rightPostJoinProcessor, rightStreamRuntime, executionPlanContext, outputExpectsExpiredEvents);

        leftPreJoinProcessor.setFindableProcessor(rightFindableProcessor);
        leftPreJoinProcessor.setJoinLock(joinLock);
        leftPostJoinProcessor.setFindableProcessor(rightFindableProcessor);
        leftPostJoinProcessor.setJoinLock(joinLock);

        rightPreJoinProcessor.setFindableProcessor(leftFindableProcessor);
        rightPreJoinProcessor.setJoinLock(joinLock);
        rightPostJoinProcessor.setFindableProcessor(leftFindableProcessor);
        rightPostJoinProcessor.setJoinLock(joinLock);

        Expression compareCondition = joinInputStream.getOnCompare();
        if (compareCondition == null) {
            compareCondition = Expression.value(true);
        }

        long withinTime = SiddhiConstants.ANY;
        if (joinInputStream.getWithin() != null) {
            withinTime = ((TimeConstant) joinInputStream.getWithin()).getValue();
        }

        Finder leftFinder = rightFindableProcessor.constructFinder(compareCondition, metaStateEvent, executionPlanContext, executors, eventTableMap, 0, withinTime);
        Finder rightFinder = leftFindableProcessor.constructFinder(compareCondition, metaStateEvent, executionPlanContext, executors, eventTableMap, 1, withinTime);

        if (joinInputStream.getTrigger() != JoinInputStream.EventTrigger.LEFT) {
            rightPreJoinProcessor.setTrigger(true);
            rightPreJoinProcessor.setFinder(rightFinder);
            rightPostJoinProcessor.setTrigger(true);
            rightPostJoinProcessor.setFinder(rightFinder);
        }
        if (joinInputStream.getTrigger() != JoinInputStream.EventTrigger.RIGHT) {
            leftPreJoinProcessor.setTrigger(true);
            leftPreJoinProcessor.setFinder(leftFinder);
            leftPostJoinProcessor.setTrigger(true);
            leftPostJoinProcessor.setFinder(leftFinder);
        }

        JoinStreamRuntime joinStreamRuntime = new JoinStreamRuntime(executionPlanContext, metaStateEvent);
        joinStreamRuntime.addRuntime(leftStreamRuntime);
        joinStreamRuntime.addRuntime(rightStreamRuntime);
        return joinStreamRuntime;
    }

    private static FindableProcessor insertJoinProcessorsAndGetFindable(JoinProcessor preJoinProcessor,
                                                                        JoinProcessor postJoinProcessor,
                                                                        SingleStreamRuntime streamRuntime, ExecutionPlanContext executionPlanContext, boolean outputExpectsExpiredEvents) {

        Processor lastProcessor = streamRuntime.getProcessorChain();
        Processor prevLastProcessor = null;
        if (lastProcessor != null) {
            while (lastProcessor.getNextProcessor() != null) {
                prevLastProcessor = lastProcessor;
                lastProcessor = lastProcessor.getNextProcessor();
            }
        }

        if (lastProcessor == null) {
            WindowProcessor windowProcessor = new LengthWindowProcessor();
            ExpressionExecutor[] expressionExecutors = new ExpressionExecutor[1];
            expressionExecutors[0] = new ConstantExpressionExecutor(0, Attribute.Type.INT);
            windowProcessor.initProcessor(((MetaStreamEvent) streamRuntime.getMetaComplexEvent()).getLastInputDefinition(),
                    expressionExecutors, executionPlanContext, outputExpectsExpiredEvents);
            lastProcessor = windowProcessor;
        }
        if (lastProcessor instanceof FindableProcessor) {
            if (prevLastProcessor != null) {
                prevLastProcessor.setNextProcessor(preJoinProcessor);
            } else {
                streamRuntime.setProcessorChain(preJoinProcessor);
            }
            preJoinProcessor.setNextProcessor(lastProcessor);
            lastProcessor.setNextProcessor(postJoinProcessor);
            return (FindableProcessor) lastProcessor;
        } else {
            throw new OperationNotSupportedException("Stream " + ((MetaStreamEvent) streamRuntime.getMetaComplexEvent()).getLastInputDefinition().getId() +
                    "'s last processor " + lastProcessor.getClass().getCanonicalName() + " is not an instance of " +
                    FindableProcessor.class.getCanonicalName() + " hence join cannot be proceed");
        }

    }
}
