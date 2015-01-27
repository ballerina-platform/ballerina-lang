/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.util.finder.Finder;
import org.wso2.siddhi.core.query.input.MultiProcessStreamReceiver;
import org.wso2.siddhi.core.query.input.ProcessStreamReceiver;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.input.stream.join.JoinProcessor;
import org.wso2.siddhi.core.query.input.stream.join.JoinStreamRuntime;
import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.window.FindableProcessor;
import org.wso2.siddhi.core.query.processor.window.TableWindowProcessor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.execution.query.input.stream.JoinInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.SingleInputStream;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.List;
import java.util.Map;

public class JoinInputStreamParser {


    public static StreamRuntime parseInputStream(JoinInputStream joinInputStream, ExecutionPlanContext executionPlanContext, Map<String, AbstractDefinition> streamDefinitionMap, Map<String, AbstractDefinition> tableDefinitionMap, Map<String, EventTable> eventTableMap, List<VariableExpressionExecutor> executors) {

        ProcessStreamReceiver leftProcessStreamReceiver;
        ProcessStreamReceiver rightProcessStreamReceiver;

        MetaStreamEvent leftMetaStreamEvent = new MetaStreamEvent();
        MetaStreamEvent rightMetaStreamEvent = new MetaStreamEvent();

        String leftInputStreamId = ((SingleInputStream) joinInputStream.getLeftInputStream()).getStreamId();
        String rightInputStreamId = ((SingleInputStream) joinInputStream.getRightInputStream()).getStreamId();

        if (joinInputStream.getAllStreamIds().size() == 2) {
            if (!streamDefinitionMap.containsKey(leftInputStreamId)) {
                leftMetaStreamEvent.setTableEvent(true);
            }
            if (!streamDefinitionMap.containsKey(rightInputStreamId)) {
                rightMetaStreamEvent.setTableEvent(true);
            }
            leftProcessStreamReceiver = new ProcessStreamReceiver(leftInputStreamId);
            rightProcessStreamReceiver = new ProcessStreamReceiver(rightInputStreamId);
            if (leftMetaStreamEvent.isTableEvent() && rightMetaStreamEvent.isTableEvent()) {
                throw new ExecutionPlanCreationException("Both inputs of join are from static sources " + leftInputStreamId + " and " + rightInputStreamId);
            }
        } else {
            if (streamDefinitionMap.containsKey(joinInputStream.getAllStreamIds().get(0))) {
                rightProcessStreamReceiver = new MultiProcessStreamReceiver(joinInputStream.getAllStreamIds().get(0), 2);
                leftProcessStreamReceiver = rightProcessStreamReceiver;
            } else {
                throw new ExecutionPlanCreationException("Input of join is from static source " + leftInputStreamId + " and " + rightInputStreamId);
            }
        }

        SingleStreamRuntime leftStreamRuntime = SingleInputStreamParser.parseInputStream(
                (SingleInputStream) joinInputStream.getLeftInputStream(), executionPlanContext, executors, streamDefinitionMap,
                !leftMetaStreamEvent.isTableEvent() ? null : tableDefinitionMap, eventTableMap, leftMetaStreamEvent, leftProcessStreamReceiver);

        SingleStreamRuntime rightStreamRuntime = SingleInputStreamParser.parseInputStream(
                (SingleInputStream) joinInputStream.getRightInputStream(), executionPlanContext, executors, streamDefinitionMap,
                !rightMetaStreamEvent.isTableEvent() ? null : tableDefinitionMap, eventTableMap, rightMetaStreamEvent, rightProcessStreamReceiver);

        if (leftMetaStreamEvent.isTableEvent()) {
            TableWindowProcessor tableWindowProcessor = new TableWindowProcessor(eventTableMap.get(leftInputStreamId));
            tableWindowProcessor.initProcessor(leftMetaStreamEvent.getLastInputDefinition(), new ExpressionExecutor[0], executionPlanContext);
            leftStreamRuntime.setProcessorChain(tableWindowProcessor);
        }
        if (rightMetaStreamEvent.isTableEvent()) {
            TableWindowProcessor tableWindowProcessor = new TableWindowProcessor(eventTableMap.get(rightInputStreamId));
            tableWindowProcessor.initProcessor(rightMetaStreamEvent.getLastInputDefinition(), new ExpressionExecutor[0], executionPlanContext);
            rightStreamRuntime.setProcessorChain(tableWindowProcessor);
        }

        MetaStateEvent metaStateEvent = new MetaStateEvent(2);
        metaStateEvent.addEvent(leftMetaStreamEvent);
        metaStateEvent.addEvent(rightMetaStreamEvent);

        JoinProcessor leftPreJoinProcessor = new JoinProcessor(true, true);
        JoinProcessor leftPostJoinProcessor = new JoinProcessor(true, false);

        FindableProcessor leftFindableProcessor = insertJoinProcessorsAndGetFindable(leftPreJoinProcessor, leftPostJoinProcessor, leftStreamRuntime);

        JoinProcessor rightPreJoinProcessor = new JoinProcessor(false, true);
        JoinProcessor rightPostJoinProcessor = new JoinProcessor(false, false);

        FindableProcessor rightFindableProcessor = insertJoinProcessorsAndGetFindable(rightPreJoinProcessor, rightPostJoinProcessor, rightStreamRuntime);

        leftPreJoinProcessor.setFindableProcessor(rightFindableProcessor);
        leftPostJoinProcessor.setFindableProcessor(rightFindableProcessor);

        rightPreJoinProcessor.setFindableProcessor(leftFindableProcessor);
        rightPostJoinProcessor.setFindableProcessor(leftFindableProcessor);

        Expression compareCondition = joinInputStream.getOnCompare();
        if (compareCondition == null) {
            compareCondition = Expression.value(true);
        }

        Finder leftFinder = rightFindableProcessor.constructFinder(compareCondition, metaStateEvent, executionPlanContext, executors, eventTableMap, 0);
        Finder rightFinder = leftFindableProcessor.constructFinder(compareCondition, metaStateEvent, executionPlanContext, executors, eventTableMap, 1);

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
                                                                        SingleStreamRuntime streamRuntime) {

        Processor lastProcessor = streamRuntime.getProcessorChain();
        Processor prevLastProcessor = null;
        if (lastProcessor != null) {
            while (lastProcessor.getNextProcessor() != null) {
                prevLastProcessor = lastProcessor;
                lastProcessor = lastProcessor.getNextProcessor();
            }
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
                    "'s last processor  " + (lastProcessor != null ? lastProcessor.getClass().getCanonicalName() : null) + " is not an instance of " +
                    FindableProcessor.class.getCanonicalName() + " hence join cannot be proceed");
        }

    }
}
