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

package org.ballerinalang.siddhi.core.util.parser.helper;

import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.event.MetaComplexEvent;
import org.ballerinalang.siddhi.core.event.state.MetaStateEvent;
import org.ballerinalang.siddhi.core.event.state.MetaStateEventAttribute;
import org.ballerinalang.siddhi.core.event.state.StateEventCloner;
import org.ballerinalang.siddhi.core.event.state.StateEventPool;
import org.ballerinalang.siddhi.core.event.stream.MetaStreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEventCloner;
import org.ballerinalang.siddhi.core.event.stream.StreamEventPool;
import org.ballerinalang.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.ballerinalang.siddhi.core.event.stream.populater.StreamEventPopulaterFactory;
import org.ballerinalang.siddhi.core.executor.VariableExpressionExecutor;
import org.ballerinalang.siddhi.core.query.input.ProcessStreamReceiver;
import org.ballerinalang.siddhi.core.query.input.stream.StreamRuntime;
import org.ballerinalang.siddhi.core.query.input.stream.join.JoinProcessor;
import org.ballerinalang.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.ballerinalang.siddhi.core.query.input.stream.state.StreamPreStateProcessor;
import org.ballerinalang.siddhi.core.query.processor.Processor;
import org.ballerinalang.siddhi.core.query.processor.SchedulingProcessor;
import org.ballerinalang.siddhi.core.query.processor.stream.AbstractStreamProcessor;
import org.ballerinalang.siddhi.core.util.SiddhiConstants;
import org.ballerinalang.siddhi.core.util.collection.operator.IncrementalAggregateCompileCondition;
import org.ballerinalang.siddhi.core.util.lock.LockWrapper;
import org.ballerinalang.siddhi.core.util.statistics.LatencyTracker;
import org.ballerinalang.siddhi.core.util.statistics.MemoryUsageTracker;
import org.ballerinalang.siddhi.core.util.statistics.ThroughputTracker;
import org.ballerinalang.siddhi.query.api.definition.Attribute;

import java.util.List;

import static org.ballerinalang.siddhi.core.util.SiddhiConstants.BEFORE_WINDOW_DATA_INDEX;
import static org.ballerinalang.siddhi.core.util.SiddhiConstants.HAVING_STATE;
import static org.ballerinalang.siddhi.core.util.SiddhiConstants.ON_AFTER_WINDOW_DATA_INDEX;
import static org.ballerinalang.siddhi.core.util.SiddhiConstants.OUTPUT_DATA_INDEX;
import static org.ballerinalang.siddhi.core.util.SiddhiConstants.STATE_OUTPUT_DATA_INDEX;
import static org.ballerinalang.siddhi.core.util.SiddhiConstants.STREAM_ATTRIBUTE_INDEX_IN_TYPE;
import static org.ballerinalang.siddhi.core.util.SiddhiConstants.STREAM_ATTRIBUTE_TYPE_INDEX;
import static org.ballerinalang.siddhi.core.util.SiddhiConstants.STREAM_EVENT_CHAIN_INDEX;
import static org.ballerinalang.siddhi.core.util.SiddhiConstants.UNKNOWN_STATE;

/**
 * Utility class for queryParser to help with QueryRuntime
 * generation.
 */
public class QueryParserHelper {

    public static void reduceMetaComplexEvent(MetaComplexEvent metaComplexEvent) {
        if (metaComplexEvent instanceof MetaStateEvent) {
            MetaStateEvent metaStateEvent = (MetaStateEvent) metaComplexEvent;
            for (MetaStateEventAttribute attribute : metaStateEvent.getOutputDataAttributes()) {
                if (attribute != null) {
                    metaStateEvent.getMetaStreamEvent(attribute.getPosition()[STREAM_EVENT_CHAIN_INDEX])
                            .addOutputData(attribute.getAttribute());
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
     * Helper method to clean/refactor MetaStreamEvent.
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

    public static void updateVariablePosition(MetaComplexEvent metaComplexEvent,
                                              List<VariableExpressionExecutor> variableExpressionExecutorList) {

        for (VariableExpressionExecutor variableExpressionExecutor : variableExpressionExecutorList) {
            int streamEventChainIndex = variableExpressionExecutor.getPosition()[STREAM_EVENT_CHAIN_INDEX];
            if (streamEventChainIndex == HAVING_STATE) {
                if (metaComplexEvent instanceof MetaStreamEvent) {
                    variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_TYPE_INDEX] = OUTPUT_DATA_INDEX;
                } else {
                    variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_TYPE_INDEX] = STATE_OUTPUT_DATA_INDEX;
                }
                variableExpressionExecutor.getPosition()[STREAM_EVENT_CHAIN_INDEX] = UNKNOWN_STATE;
                variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_INDEX_IN_TYPE] = metaComplexEvent
                        .getOutputStreamDefinition().getAttributeList()
                        .indexOf(variableExpressionExecutor.getAttribute());
                continue;
            } else if (metaComplexEvent instanceof MetaStreamEvent && streamEventChainIndex >= 1) { // for
                // VariableExpressionExecutor on Event table
                continue;
            } else if (metaComplexEvent instanceof MetaStateEvent
                    && streamEventChainIndex >= ((MetaStateEvent) metaComplexEvent).getMetaStreamEvents().length) {
                // for VariableExpressionExecutor on Event table
                continue;
            }

            MetaStreamEvent metaStreamEvent;
            if (metaComplexEvent instanceof MetaStreamEvent) {
                metaStreamEvent = (MetaStreamEvent) metaComplexEvent;
            } else {
                metaStreamEvent = ((MetaStateEvent) metaComplexEvent).getMetaStreamEvent(streamEventChainIndex);
            }

            if (metaStreamEvent.getOutputData().contains(variableExpressionExecutor.getAttribute())) {
                variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_TYPE_INDEX] = OUTPUT_DATA_INDEX;
                variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_INDEX_IN_TYPE] = metaStreamEvent
                        .getOutputData().indexOf(variableExpressionExecutor.getAttribute());
            } else if (metaStreamEvent.getOnAfterWindowData().contains(variableExpressionExecutor.getAttribute())) {
                variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_TYPE_INDEX] = ON_AFTER_WINDOW_DATA_INDEX;
                variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_INDEX_IN_TYPE] = metaStreamEvent
                        .getOnAfterWindowData().indexOf(variableExpressionExecutor.getAttribute());
            } else if (metaStreamEvent.getBeforeWindowData().contains(variableExpressionExecutor.getAttribute())) {
                variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_TYPE_INDEX] = BEFORE_WINDOW_DATA_INDEX;
                variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_INDEX_IN_TYPE] = metaStreamEvent
                        .getBeforeWindowData().indexOf(variableExpressionExecutor.getAttribute());
            }
        }
    }

    public static void initStreamRuntime(StreamRuntime runtime, MetaComplexEvent metaComplexEvent,
                                         LockWrapper lockWrapper, String queryName) {

        if (runtime instanceof SingleStreamRuntime) {
            initSingleStreamRuntime((SingleStreamRuntime) runtime, 0, metaComplexEvent,
                    null, lockWrapper, queryName);
        } else {
            MetaStateEvent metaStateEvent = (MetaStateEvent) metaComplexEvent;
            StateEventPool stateEventPool = new StateEventPool(metaStateEvent, 5);
            MetaStreamEvent[] metaStreamEvents = metaStateEvent.getMetaStreamEvents();
            for (int i = 0, metaStreamEventsLength = metaStreamEvents.length; i < metaStreamEventsLength; i++) {
                initSingleStreamRuntime(runtime.getSingleStreamRuntimes().get(i), i, metaStateEvent, stateEventPool,
                        lockWrapper, queryName);
            }
        }
    }

    private static void initSingleStreamRuntime(SingleStreamRuntime singleStreamRuntime, int streamEventChainIndex,
                                                MetaComplexEvent metaComplexEvent, StateEventPool stateEventPool,
                                                LockWrapper lockWrapper, String queryName) {
        MetaStreamEvent metaStreamEvent;

        if (metaComplexEvent instanceof MetaStateEvent) {
            metaStreamEvent = ((MetaStateEvent) metaComplexEvent).getMetaStreamEvent(streamEventChainIndex);
        } else {
            metaStreamEvent = (MetaStreamEvent) metaComplexEvent;
        }
        StreamEventPool streamEventPool = new StreamEventPool(metaStreamEvent, 5);
        ProcessStreamReceiver processStreamReceiver = singleStreamRuntime.getProcessStreamReceiver();
        processStreamReceiver.setMetaStreamEvent(metaStreamEvent);
        processStreamReceiver.setStreamEventPool(streamEventPool);
        processStreamReceiver.setLockWrapper(lockWrapper);
        processStreamReceiver.init();
        Processor processor = singleStreamRuntime.getProcessorChain();
        while (processor != null) {
            if (processor instanceof SchedulingProcessor) {
                ((SchedulingProcessor) processor).getScheduler().setStreamEventPool(streamEventPool);
                ((SchedulingProcessor) processor).getScheduler().init(lockWrapper, queryName);
            }
            if (processor instanceof AbstractStreamProcessor) {
                ((AbstractStreamProcessor) processor)
                        .setStreamEventCloner(new StreamEventCloner(metaStreamEvent, streamEventPool));
                ((AbstractStreamProcessor) processor).constructStreamEventPopulater(metaStreamEvent,
                        streamEventChainIndex);
            }
            if (stateEventPool != null && processor instanceof JoinProcessor) {
                if (((JoinProcessor) processor)
                        .getCompiledCondition() instanceof IncrementalAggregateCompileCondition) {
                    IncrementalAggregateCompileCondition compiledCondition = (IncrementalAggregateCompileCondition) (
                            (JoinProcessor) processor).getCompiledCondition();
                    ComplexEventPopulater complexEventPopulater = StreamEventPopulaterFactory
                            .constructEventPopulator(metaStreamEvent, 0, compiledCondition.getAdditionalAttributes());
                    compiledCondition.setComplexEventPopulater(complexEventPopulater);

                }
                ((JoinProcessor) processor).setStateEventPool(stateEventPool);
                ((JoinProcessor) processor).setJoinLock(lockWrapper);
            }
            if (stateEventPool != null && processor instanceof StreamPreStateProcessor) {
                ((StreamPreStateProcessor) processor).setStateEventPool(stateEventPool);
                ((StreamPreStateProcessor) processor).setStreamEventPool(streamEventPool);
                ((StreamPreStateProcessor) processor)
                        .setStreamEventCloner(new StreamEventCloner(metaStreamEvent, streamEventPool));
                if (metaComplexEvent instanceof MetaStateEvent) {
                    ((StreamPreStateProcessor) processor).setStateEventCloner(
                            new StateEventCloner(((MetaStateEvent) metaComplexEvent), stateEventPool));
                }
            }

            processor = processor.getNextProcessor();
        }
    }

    public static LatencyTracker createLatencyTracker(SiddhiAppContext siddhiAppContext, String name, String type,
                                                      String function) {
        LatencyTracker latencyTracker = null;
        if (siddhiAppContext.getStatisticsManager() != null) {
            String metricName =
                    siddhiAppContext.getSiddhiContext().getStatisticsConfiguration().getMetricPrefix() +
                            SiddhiConstants.METRIC_DELIMITER + SiddhiConstants.METRIC_INFIX_SIDDHI_APPS +
                            SiddhiConstants.METRIC_DELIMITER + siddhiAppContext.getName() +
                            SiddhiConstants.METRIC_DELIMITER + SiddhiConstants.METRIC_INFIX_SIDDHI +
                            SiddhiConstants.METRIC_DELIMITER + type +
                            SiddhiConstants.METRIC_DELIMITER + name;
            if (function != null) {
                metricName += SiddhiConstants.METRIC_DELIMITER + function;
            }
            metricName += SiddhiConstants.METRIC_DELIMITER + "latency";
            boolean matchExist = false;
            for (String regex : siddhiAppContext.getIncludedMetrics()) {
                if (metricName.matches(regex)) {
                    matchExist = true;
                    break;
                }
            }
            if (matchExist) {
                latencyTracker = siddhiAppContext.getSiddhiContext()
                        .getStatisticsConfiguration()
                        .getFactory()
                        .createLatencyTracker(metricName, siddhiAppContext.getStatisticsManager());
            }
        }
        return latencyTracker;
    }

    public static ThroughputTracker createThroughputTracker(SiddhiAppContext siddhiAppContext, String name,
                                                            String type, String function) {
        ThroughputTracker throughputTracker = null;
        if (siddhiAppContext.getStatisticsManager() != null) {
            String metricName =
                    siddhiAppContext.getSiddhiContext().getStatisticsConfiguration().getMetricPrefix() +
                            SiddhiConstants.METRIC_DELIMITER + SiddhiConstants.METRIC_INFIX_SIDDHI_APPS +
                            SiddhiConstants.METRIC_DELIMITER + siddhiAppContext.getName() +
                            SiddhiConstants.METRIC_DELIMITER + SiddhiConstants.METRIC_INFIX_SIDDHI +
                            SiddhiConstants.METRIC_DELIMITER + type +
                            SiddhiConstants.METRIC_DELIMITER + name;
            if (function != null) {
                metricName += SiddhiConstants.METRIC_DELIMITER + function;
            }
            metricName += SiddhiConstants.METRIC_DELIMITER + "throughput";
            boolean matchExist = false;
            for (String regex : siddhiAppContext.getIncludedMetrics()) {
                if (metricName.matches(regex)) {
                    matchExist = true;
                    break;
                }
            }
            if (matchExist) {
                throughputTracker = siddhiAppContext
                        .getSiddhiContext()
                        .getStatisticsConfiguration()
                        .getFactory()
                        .createThroughputTracker(metricName, siddhiAppContext.getStatisticsManager());
            }
        }
        return throughputTracker;
    }


    public static void registerMemoryUsageTracking(String name, Object value, String metricInfixQueries,
                                                   SiddhiAppContext siddhiAppContext,
                                                   MemoryUsageTracker memoryUsageTracker) {
        String metricName = siddhiAppContext.getSiddhiContext().getStatisticsConfiguration().getMetricPrefix() +
                SiddhiConstants.METRIC_DELIMITER + SiddhiConstants.METRIC_INFIX_SIDDHI_APPS +
                SiddhiConstants.METRIC_DELIMITER + siddhiAppContext.getName() + SiddhiConstants.METRIC_DELIMITER +
                SiddhiConstants.METRIC_INFIX_SIDDHI + SiddhiConstants.METRIC_DELIMITER +
                metricInfixQueries + SiddhiConstants.METRIC_DELIMITER +
                name + SiddhiConstants.METRIC_DELIMITER + "memory";
        boolean matchExist = false;
        for (String regex : siddhiAppContext.getIncludedMetrics()) {
            if (metricName.matches(regex)) {
                matchExist = true;
                break;
            }
        }
        if (matchExist) {
            memoryUsageTracker.registerObject(value, metricName);
        }
    }

}
