/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.util.parser.helper;

import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.state.MetaStateEventAttribute;
import org.wso2.siddhi.core.event.state.StateEventCloner;
import org.wso2.siddhi.core.event.state.StateEventPool;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.input.stream.join.JoinProcessor;
import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.core.query.input.stream.state.StreamPreStateProcessor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.SchedulingProcessor;
import org.wso2.siddhi.core.query.processor.stream.AbstractStreamProcessor;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.ExecuteStreamReceiver;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.Executor;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.IncrementalExecutor;
import org.wso2.siddhi.core.util.lock.LockWrapper;
import org.wso2.siddhi.core.util.parser.AggregationRuntime;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.List;

import static org.wso2.siddhi.core.util.SiddhiConstants.*;

/**
 * Utility class for aggregateParser to help with AggregationRuntime
 * generation.
 */

// TODO: 5/16/17  copy of QueryParserHelper. Change later

public class AggregationDefinitionParserHelper {

    public static void reduceMetaComplexEvent(MetaComplexEvent metaComplexEvent) {
        if (metaComplexEvent instanceof MetaStateEvent) {
            MetaStateEvent metaStateEvent = (MetaStateEvent) metaComplexEvent;
            for (MetaStateEventAttribute attribute : metaStateEvent.getOutputDataAttributes()) {
                if (attribute != null) {
                    metaStateEvent.getMetaStreamEvent(attribute.getPosition()[STREAM_EVENT_CHAIN_INDEX]).
                            addOutputData(attribute.getAttribute());
                }
            }
            for (MetaStreamEvent metaStreamEvent : metaStateEvent.getMetaStreamEvents()) {
                reduceStreamAttributes(metaStreamEvent);
            }
        } else {
            reduceStreamAttributes((MetaStreamEvent) metaComplexEvent);
        }
    }

    /**
     * Helper method to clean/refactor MetaStreamEvent
     *
     * @param metaStreamEvent MetaStreamEvent
     */
    private static synchronized void reduceStreamAttributes(MetaStreamEvent metaStreamEvent) {
        for (Attribute attribute : metaStreamEvent.getOutputData()) {
            if (metaStreamEvent.getBeforeWindowData().contains(attribute)) {
                metaStreamEvent.getBeforeWindowData().remove(attribute);
            }
            if (metaStreamEvent.getOnAfterWindowData().contains(attribute)) {
                metaStreamEvent.getOnAfterWindowData().remove(attribute);
            }
        }
        for (Attribute attribute : metaStreamEvent.getOnAfterWindowData()) {
            if (metaStreamEvent.getBeforeWindowData().contains(attribute)) {
                metaStreamEvent.getBeforeWindowData().remove(attribute);
            }
        }
    }

    public static void updateVariablePosition(MetaComplexEvent metaComplexEvent, List<VariableExpressionExecutor> variableExpressionExecutorList) {

        for (VariableExpressionExecutor variableExpressionExecutor : variableExpressionExecutorList) {
            int streamEventChainIndex = variableExpressionExecutor.getPosition()[STREAM_EVENT_CHAIN_INDEX];
            if (streamEventChainIndex == HAVING_STATE) {
                if (metaComplexEvent instanceof MetaStreamEvent) {
                    variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_TYPE_INDEX] = OUTPUT_DATA_INDEX;
                } else {
                    variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_TYPE_INDEX] = STATE_OUTPUT_DATA_INDEX;
                }
                variableExpressionExecutor.getPosition()[STREAM_EVENT_CHAIN_INDEX] = UNKNOWN_STATE;
                variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_INDEX_IN_TYPE] = metaComplexEvent.
                        getOutputStreamDefinition().getAttributeList().indexOf(variableExpressionExecutor.getAttribute());
                continue;
            } else if (metaComplexEvent instanceof MetaStreamEvent && streamEventChainIndex >= 1) { //for VariableExpressionExecutor on Event table
                continue;
            } else if (metaComplexEvent instanceof MetaStateEvent && streamEventChainIndex >= ((MetaStateEvent) metaComplexEvent).getMetaStreamEvents().length) { //for VariableExpressionExecutor on Event table
                continue;
            }

            MetaStreamEvent metaStreamEvent;
            if (metaComplexEvent instanceof MetaStreamEvent) {
                metaStreamEvent = (MetaStreamEvent) metaComplexEvent;
            } else {
                metaStreamEvent = ((MetaStateEvent) metaComplexEvent).getMetaStreamEvent(streamEventChainIndex);
            }

            if (metaStreamEvent.getOutputData().contains(variableExpressionExecutor.getAttribute())) {
                variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_TYPE_INDEX] =
                        OUTPUT_DATA_INDEX;
                variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_INDEX_IN_TYPE] =
                        metaStreamEvent.getOutputData().indexOf(variableExpressionExecutor.getAttribute());
            } else if (metaStreamEvent.getOnAfterWindowData().contains(variableExpressionExecutor.getAttribute())) {
                variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_TYPE_INDEX] =
                        ON_AFTER_WINDOW_DATA_INDEX;
                variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_INDEX_IN_TYPE] =
                        metaStreamEvent.getOnAfterWindowData().indexOf(variableExpressionExecutor.getAttribute());
            } else if (metaStreamEvent.getBeforeWindowData().contains(variableExpressionExecutor.getAttribute())) {
                variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_TYPE_INDEX] =
                        BEFORE_WINDOW_DATA_INDEX;
                variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_INDEX_IN_TYPE] =
                        metaStreamEvent.getBeforeWindowData().indexOf(variableExpressionExecutor.getAttribute());
            }
        }
    }

    public static void initStreamRuntime(StreamRuntime runtime, MetaComplexEvent metaComplexEvent, LockWrapper lockWrapper, String queryName, AggregationRuntime aggregationRuntime) {

        if (runtime instanceof SingleStreamRuntime) {
            initSingleStreamRuntime((SingleStreamRuntime) runtime, 0, metaComplexEvent, null, lockWrapper, queryName, aggregationRuntime);
        } else {
            MetaStateEvent metaStateEvent = (MetaStateEvent) metaComplexEvent;
            StateEventPool stateEventPool = new StateEventPool(metaStateEvent, 5);
            MetaStreamEvent[] metaStreamEvents = metaStateEvent.getMetaStreamEvents();
            for (int i = 0, metaStreamEventsLength = metaStreamEvents.length; i < metaStreamEventsLength; i++) {
                initSingleStreamRuntime(runtime.getSingleStreamRuntimes().get(i),
                        i, metaStateEvent, stateEventPool, lockWrapper, queryName, aggregationRuntime);
            }
        }
    }

    private static void initSingleStreamRuntime(SingleStreamRuntime singleStreamRuntime, int streamEventChainIndex,
                                                MetaComplexEvent metaComplexEvent, StateEventPool stateEventPool, LockWrapper lockWrapper, String queryName, AggregationRuntime aggregationRuntime) {
        MetaStreamEvent metaStreamEvent;

        if (metaComplexEvent instanceof MetaStateEvent) {
            metaStreamEvent = ((MetaStateEvent) metaComplexEvent).getMetaStreamEvent(streamEventChainIndex);
        } else {
            metaStreamEvent = (MetaStreamEvent) metaComplexEvent;
        }
        StreamEventPool streamEventPool = new StreamEventPool(metaStreamEvent, 5);
        ExecuteStreamReceiver executeStreamReceiver = aggregationRuntime.getExecuteStreamReceiver();
        executeStreamReceiver.setMetaStreamEvent(metaStreamEvent);
        executeStreamReceiver.setOriginalMetaStreamEvent((MetaStreamEvent) singleStreamRuntime.getMetaComplexEvent());
        executeStreamReceiver.setStreamEventPool(streamEventPool);
        executeStreamReceiver.setLockWrapper(lockWrapper);
        executeStreamReceiver.init();
        // TODO: 5/17/17 should we chain executors as in QueryParserHelper? If so following must be corrected

        /*Executor executor = aggregationRuntime.getIncrementalExecutor();
        executor.setToLast(((IncrementalExecutor)executor).child);

        aggregationRuntime.getIncrementalExecutor().setNextExecutor(executor);*/

        executeStreamReceiver.setNext(aggregationRuntime.getIncrementalExecutor());

    }

}
