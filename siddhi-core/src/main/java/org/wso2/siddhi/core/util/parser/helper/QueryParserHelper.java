/*
 *
 *  * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 *  * All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.util.parser.helper;

import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.state.MetaStateEventAttribute;
import org.wso2.siddhi.core.event.state.StateEventCloner;
import org.wso2.siddhi.core.event.state.StateEventPool;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.input.ProcessStreamReceiver;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.input.stream.join.JoinProcessor;
import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.core.query.input.stream.state.PreStateProcessor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.SchedulingProcessor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.List;

import static org.wso2.siddhi.core.util.SiddhiConstants.*;

/**
 * Utility class for queryParser to help with QueryRuntime
 * generation.
 */
public class QueryParserHelper {

    public static void reduceMetaStateEvent(MetaStateEvent metaStateEvent) {
        for (MetaStateEventAttribute attribute : metaStateEvent.getOutputDataAttributes()) {
            metaStateEvent.getMetaStreamEvent(attribute.getPosition()[STREAM_EVENT_CHAIN_INDEX]).
                    addOutputData(attribute.getAttribute());
        }
        for (MetaStreamEvent metaStreamEvent : metaStateEvent.getMetaStreamEvents()) {
            reduceStreamAttributes(metaStreamEvent);
        }
    }

//    private static void unifySameMetaStreamEvents(MetaStateEvent metaStateEvent) {
//        MetaStreamEvent[] metaStreamEvents = metaStateEvent.getMetaStreamEvents();
//        Map<String, MetaStreamEvent> commonMetaStreamEventMap = new HashMap<String, MetaStreamEvent>();
//        for (MetaStreamEvent metaStreamEvent : metaStreamEvents) {
//            MetaStreamEvent commonMetaStreamEvent;
//            if (!commonMetaStreamEventMap.containsKey(metaStreamEvent.getInputDefinition().getId())) {
//                commonMetaStreamEvent = new MetaStreamEvent();
//                commonMetaStreamEvent.setInputDefinition(metaStreamEvent.getInputDefinition());
//                commonMetaStreamEvent.initializeAfterWindowData();
//                commonMetaStreamEventMap.put(metaStreamEvent.getInputDefinition().getId(), commonMetaStreamEvent);
//            } else {
//                commonMetaStreamEvent = commonMetaStreamEventMap.get(metaStreamEvent.getInputDefinition().getId());
//            }
//
//            for (Attribute attribute : metaStreamEvent.getBeforeWindowData()) {
//                if (!commonMetaStreamEvent.getBeforeWindowData().contains(attribute)) {
//                    commonMetaStreamEvent.getBeforeWindowData().add(attribute);
//                }
//            }
//            for (Attribute attribute : metaStreamEvent.getOnAfterWindowData()) {
//                if (!commonMetaStreamEvent.getOnAfterWindowData().contains(attribute)) {
//                    commonMetaStreamEvent.getOnAfterWindowData().add(attribute);
//                }
//            }
//            for (Attribute attribute : metaStreamEvent.getOutputData()) {
//                if (attribute == null) {
//                    commonMetaStreamEvent.addOutputData(null);
//                } else if (!commonMetaStreamEvent.getOutputData().contains(attribute)) {
//                    commonMetaStreamEvent.addOutputData(attribute);
//                }
//            }
//        }
//
//        for (MetaStreamEvent metaStreamEvent : commonMetaStreamEventMap.values()) {
//            reduceStreamAttributes(metaStreamEvent);
//        }
//
//        for (MetaStreamEvent metaStreamEvent : metaStreamEvents) {
//            MetaStreamEvent commonMetaStreamEvent = commonMetaStreamEventMap.get(metaStreamEvent.getInputDefinition().getId());
//            metaStreamEvent.getBeforeWindowData().clear();
//            metaStreamEvent.getBeforeWindowData().addAll(commonMetaStreamEvent.getBeforeWindowData());
//            metaStreamEvent.getOnAfterWindowData().clear();
//            metaStreamEvent.getOnAfterWindowData().addAll(commonMetaStreamEvent.getOnAfterWindowData());
//            metaStreamEvent.getOutputData().clear();
//            metaStreamEvent.getOutputData().addAll(commonMetaStreamEvent.getOutputData());
//        }
//    }

    /**
     * Helper method to clean/refactor MetaStreamEvent
     *
     * @param metaStreamEvent
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

    public static void updateVariablePosition(MetaStateEvent metaStateEvent, List<VariableExpressionExecutor> variableExpressionExecutorList) {
        for (VariableExpressionExecutor variableExpressionExecutor : variableExpressionExecutorList) {
            int streamEventChainIndex = variableExpressionExecutor.getPosition()[STREAM_EVENT_CHAIN_INDEX];
            if (streamEventChainIndex == HAVING_STATE) {
                if (metaStateEvent.getStreamEventCount() == 1) {
                    variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_TYPE_INDEX] = OUTPUT_DATA_INDEX;
                } else {
                    variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_TYPE_INDEX] = STATE_OUTPUT_DATA_INDEX;
                }
                variableExpressionExecutor.getPosition()[STREAM_EVENT_CHAIN_INDEX] = UNKNOWN_STATE;
                variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_INDEX] = metaStateEvent.
                        getOutputStreamDefinition().getAttributeList().indexOf(variableExpressionExecutor.getAttribute());

                continue;
            }

            MetaStreamEvent metaStreamEvent;
            if (streamEventChainIndex == UNKNOWN_STATE) {
                metaStreamEvent = metaStateEvent.getMetaStreamEvent(0);
            } else {
                metaStreamEvent = metaStateEvent.getMetaStreamEvent(streamEventChainIndex);
            }

            if (metaStreamEvent.getOutputData().contains(variableExpressionExecutor.getAttribute())) {
                variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_TYPE_INDEX] =
                        OUTPUT_DATA_INDEX;
                variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_INDEX] =
                        metaStreamEvent.getOutputData().indexOf(variableExpressionExecutor.getAttribute());
            } else if (metaStreamEvent.getOnAfterWindowData().contains(variableExpressionExecutor.getAttribute())) {
                variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_TYPE_INDEX] =
                        ON_AFTER_WINDOW_DATA_INDEX;
                variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_INDEX] =
                        metaStreamEvent.getOnAfterWindowData().indexOf(variableExpressionExecutor.getAttribute());
            } else if (metaStreamEvent.getBeforeWindowData().contains(variableExpressionExecutor.getAttribute())) {
                variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_TYPE_INDEX] =
                        BEFORE_WINDOW_DATA_INDEX;
                variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_INDEX] =
                        metaStreamEvent.getBeforeWindowData().indexOf(variableExpressionExecutor.getAttribute());
            }
        }
    }

    public static void initStreamRuntime(StreamRuntime runtime, MetaStateEvent metaStateEvent) {

        if (runtime instanceof SingleStreamRuntime) {
            MetaStreamEvent metaStreamEvent = metaStateEvent.getMetaStreamEvent(0);
            initSingleStreamRuntime((SingleStreamRuntime) runtime, 0, metaStateEvent, null);
        } else {
            StateEventPool stateEventPool = new StateEventPool(metaStateEvent, 5);
            MetaStreamEvent[] metaStreamEvents = metaStateEvent.getMetaStreamEvents();
            for (int i = 0, metaStreamEventsLength = metaStreamEvents.length; i < metaStreamEventsLength; i++) {
                initSingleStreamRuntime(runtime.getSingleStreamRuntimes().get(i),
                        i, metaStateEvent, stateEventPool);
            }
        }
    }

    private static void initSingleStreamRuntime(SingleStreamRuntime singleStreamRuntime, int streamEventChainIndex, MetaStateEvent metaStateEvent, StateEventPool stateEventPool) {
        MetaStreamEvent metaStreamEvent = metaStateEvent.getMetaStreamEvent(streamEventChainIndex);
        StreamEventPool streamEventPool = new StreamEventPool(metaStreamEvent, 5);
        ProcessStreamReceiver processStreamReceiver = singleStreamRuntime.getProcessStreamReceiver();
        processStreamReceiver.setMetaStreamEvent(metaStreamEvent);
        processStreamReceiver.setStreamEventPool(streamEventPool);
        processStreamReceiver.init();
        Processor processor = singleStreamRuntime.getProcessorChain();
        while (processor != null) {
            if (processor instanceof SchedulingProcessor) {
                ((SchedulingProcessor) processor).getScheduler().setStreamEventPool(streamEventPool);
            }
            if (processor instanceof StreamProcessor) {
                ((StreamProcessor) processor).setStreamEventCloner(new StreamEventCloner(metaStreamEvent,
                        streamEventPool));
                ((StreamProcessor) processor).constructStreamEventPopulater(metaStreamEvent, streamEventChainIndex);
            }
            if (stateEventPool != null && processor instanceof JoinProcessor) {
                ((JoinProcessor) processor).setStateEventPool(stateEventPool);
            }
            if (stateEventPool != null && processor instanceof PreStateProcessor) {
                ((PreStateProcessor) processor).setStateEventPool(stateEventPool);
                ((PreStateProcessor) processor).setStreamEventPool(streamEventPool);
                ((PreStateProcessor) processor).setStreamEventCloner(new StreamEventCloner(metaStreamEvent, streamEventPool));
                ((PreStateProcessor) processor).setStateEventCloner(new StateEventCloner(metaStateEvent, stateEventPool));
            }

            processor = processor.getNextProcessor();
        }
    }

}
