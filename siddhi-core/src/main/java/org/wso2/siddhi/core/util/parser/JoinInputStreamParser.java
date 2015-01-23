/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.finder.Finder;
import org.wso2.siddhi.core.query.input.MultiProcessStreamReceiver;
import org.wso2.siddhi.core.query.input.ProcessStreamReceiver;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.input.stream.join.JoinProcessor;
import org.wso2.siddhi.core.query.input.stream.join.JoinStreamRuntime;
import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.window.FindableProcessor;
import org.wso2.siddhi.core.query.processor.window.TableWindowProcessor;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.execution.query.input.stream.JoinInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.SingleInputStream;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.List;
import java.util.Map;

public class JoinInputStreamParser {


    public static StreamRuntime parseInputStream(JoinInputStream joinInputStream, ExecutionPlanContext executionPlanContext, Map<String, AbstractDefinition> streamDefinitionMap, Map<String, AbstractDefinition> tableDefinitionMap, List<VariableExpressionExecutor> executors) {

        ProcessStreamReceiver leftProcessStreamReceiver;
        ProcessStreamReceiver rightProcessStreamReceiver;

        MetaStreamEvent leftMetaStreamEvent = new MetaStreamEvent();
        MetaStreamEvent rightMetaStreamEvent = new MetaStreamEvent();

        SingleInputStream leftInputStream = ((SingleInputStream) joinInputStream.getLeftInputStream());
        SingleInputStream rightInputStream = ((SingleInputStream) joinInputStream.getRightInputStream());

        if (joinInputStream.getAllStreamIds().size() == 2) {
            if (!streamDefinitionMap.containsKey(leftInputStream.getStreamId())) {
                leftMetaStreamEvent.setTableEvent(true);
            }
            if (!streamDefinitionMap.containsKey(rightInputStream.getStreamId())) {
                rightMetaStreamEvent.setTableEvent(true);
            }
            leftProcessStreamReceiver = new ProcessStreamReceiver((leftInputStream.isInnerStream() ? SiddhiConstants.INNER_STREAM_FLAG : "") + leftInputStream.getStreamId());
            rightProcessStreamReceiver = new ProcessStreamReceiver((rightInputStream.isInnerStream() ? SiddhiConstants.INNER_STREAM_FLAG : "") + rightInputStream.getStreamId());
            if (leftMetaStreamEvent.isTableEvent() && rightMetaStreamEvent.isTableEvent()) {
                throw new ExecutionPlanCreationException("Both inputs of join are from static sources " + leftInputStream.getStreamId() + " and " + rightInputStream.getStreamId());
            }
        } else {
            if (streamDefinitionMap.containsKey(joinInputStream.getAllStreamIds().get(0))) {
                rightProcessStreamReceiver = new MultiProcessStreamReceiver(joinInputStream.getAllStreamIds().get(0), 2);
                leftProcessStreamReceiver = rightProcessStreamReceiver;
            } else {
                throw new ExecutionPlanCreationException("Input of join is from static source " + leftInputStream.getStreamId() + " and " + rightInputStream.getStreamId());
            }
        }

        SingleStreamRuntime leftStreamRuntime = SingleInputStreamParser.parseInputStream(
                leftInputStream, executionPlanContext, executors, streamDefinitionMap,
                !leftMetaStreamEvent.isTableEvent() ? null : tableDefinitionMap, leftMetaStreamEvent, leftProcessStreamReceiver);

        SingleStreamRuntime rightStreamRuntime = SingleInputStreamParser.parseInputStream(
                rightInputStream, executionPlanContext, executors, streamDefinitionMap,
                !rightMetaStreamEvent.isTableEvent() ? null : tableDefinitionMap, rightMetaStreamEvent, rightProcessStreamReceiver);

        if (leftMetaStreamEvent.isTableEvent()) {
            TableWindowProcessor tableWindowProcessor = new TableWindowProcessor(executionPlanContext.getEventTableMap().get(leftInputStream.getStreamId()));
            tableWindowProcessor.initProcessor(leftMetaStreamEvent.getInputDefinition(), null);
            leftStreamRuntime.setProcessorChain(tableWindowProcessor);
        }
        if (rightMetaStreamEvent.isTableEvent()) {
            TableWindowProcessor tableWindowProcessor = new TableWindowProcessor(executionPlanContext.getEventTableMap().get(rightInputStream.getStreamId()));
            tableWindowProcessor.initProcessor(rightMetaStreamEvent.getInputDefinition(), null);
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

        Finder leftFinder = rightFindableProcessor.constructFinder(compareCondition, metaStateEvent, executionPlanContext, executors, 0);
        Finder rightFinder = leftFindableProcessor.constructFinder(compareCondition, metaStateEvent, executionPlanContext, executors, 1);

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
            throw new OperationNotSupportedException("Stream " + ((MetaStreamEvent) streamRuntime.getMetaComplexEvent()).getInputDefinition().getId() +
                    "'s last processor  " + (lastProcessor != null ? lastProcessor.getClass().getCanonicalName() : null) + " is not an instance of " +
                    FindableProcessor.class.getCanonicalName() + " hence join cannot be proceed");
        }

    }
}
