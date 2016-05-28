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
package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.input.ProcessStreamReceiver;
import org.wso2.siddhi.core.query.input.stream.single.EntryValveProcessor;
import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.SchedulingProcessor;
import org.wso2.siddhi.core.query.processor.filter.FilterProcessor;
import org.wso2.siddhi.core.query.processor.stream.AbstractStreamProcessor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.core.query.processor.stream.function.StreamFunctionProcessor;
import org.wso2.siddhi.core.query.processor.stream.window.WindowProcessor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.Scheduler;
import org.wso2.siddhi.core.util.SiddhiClassLoader;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.extension.holder.StreamFunctionProcessorExtensionHolder;
import org.wso2.siddhi.core.util.extension.holder.StreamProcessorExtensionHolder;
import org.wso2.siddhi.core.util.extension.holder.WindowProcessorExtensionHolder;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.execution.query.input.handler.Filter;
import org.wso2.siddhi.query.api.execution.query.input.handler.StreamFunction;
import org.wso2.siddhi.query.api.execution.query.input.handler.StreamHandler;
import org.wso2.siddhi.query.api.execution.query.input.handler.Window;
import org.wso2.siddhi.query.api.execution.query.input.stream.SingleInputStream;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.extension.Extension;

import java.util.List;
import java.util.Map;

public class SingleInputStreamParser {

    /**
     * Parse single InputStream and return SingleStreamRuntime
     *
     * @param inputStream                 single input stream to be parsed
     * @param executionPlanContext        query to be parsed
     * @param variableExpressionExecutors List to hold VariableExpressionExecutors to update after query parsing
     * @param streamDefinitionMap         Stream Definition Map
     * @param tableDefinitionMap          Table Definition Map
     * @param eventTableMap               EventTable Map
     * @param metaComplexEvent            MetaComplexEvent
     * @param processStreamReceiver       ProcessStreamReceiver
     * @param latencyTracker              latency tracker
     * @param outputExpectsExpiredEvents
     * @return SingleStreamRuntime
     */
    public static SingleStreamRuntime parseInputStream(SingleInputStream inputStream, ExecutionPlanContext executionPlanContext,
                                                       List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, AbstractDefinition> streamDefinitionMap,
                                                       Map<String, AbstractDefinition> tableDefinitionMap, Map<String, EventTable> eventTableMap, MetaComplexEvent metaComplexEvent,
                                                       ProcessStreamReceiver processStreamReceiver, boolean supportsBatchProcessing, LatencyTracker latencyTracker, boolean outputExpectsExpiredEvents) {
        Processor processor = null;
        EntryValveProcessor entryValveProcessor = null;
        boolean first = true;
        MetaStreamEvent metaStreamEvent;
        if (metaComplexEvent instanceof MetaStateEvent) {
            metaStreamEvent = new MetaStreamEvent();
            ((MetaStateEvent) metaComplexEvent).addEvent(metaStreamEvent);
            initMetaStreamEvent(inputStream, streamDefinitionMap, tableDefinitionMap, metaStreamEvent);
        } else {
            metaStreamEvent = (MetaStreamEvent) metaComplexEvent;
            initMetaStreamEvent(inputStream, streamDefinitionMap, tableDefinitionMap, metaStreamEvent);
        }
        if (!inputStream.getStreamHandlers().isEmpty()) {
            for (StreamHandler handler : inputStream.getStreamHandlers()) {
                Processor currentProcessor = generateProcessor(handler, metaComplexEvent, variableExpressionExecutors, executionPlanContext, eventTableMap, supportsBatchProcessing, outputExpectsExpiredEvents);
                if (currentProcessor instanceof SchedulingProcessor) {
                    if (entryValveProcessor == null) {

                        entryValveProcessor = new EntryValveProcessor(executionPlanContext);
                        if (first) {
                            processor = entryValveProcessor;
                            first = false;
                        } else {
                            processor.setToLast(entryValveProcessor);
                        }
                    }
                    Scheduler scheduler = new Scheduler(executionPlanContext.getScheduledExecutorService(), entryValveProcessor);
                    scheduler.init(executionPlanContext, latencyTracker);
                    ((SchedulingProcessor) currentProcessor).setScheduler(scheduler);
                }
                if (first) {
                    processor = currentProcessor;
                    first = false;
                } else {
                    processor.setToLast(currentProcessor);
                }
            }
        }

        metaStreamEvent.initializeAfterWindowData();
        return new SingleStreamRuntime(processStreamReceiver, processor, metaComplexEvent);

    }


    private static Processor generateProcessor(StreamHandler streamHandler, MetaComplexEvent metaEvent, List<VariableExpressionExecutor> variableExpressionExecutors, ExecutionPlanContext executionPlanContext, Map<String, EventTable> eventTableMap, boolean supportsBatchProcessing, boolean outputExpectsExpiredEvents) {
        Expression[] parameters = streamHandler.getParameters();
        MetaStreamEvent metaStreamEvent;
        int stateIndex = SiddhiConstants.UNKNOWN_STATE;
        if (metaEvent instanceof MetaStateEvent) {
            stateIndex = ((MetaStateEvent) metaEvent).getStreamEventCount() - 1;
            metaStreamEvent = ((MetaStateEvent) metaEvent).getMetaStreamEvent(stateIndex);
        } else {
            metaStreamEvent = (MetaStreamEvent) metaEvent;
        }

        ExpressionExecutor[] attributeExpressionExecutors;
        if (parameters != null) {
            if (parameters.length > 0) {
                attributeExpressionExecutors = new ExpressionExecutor[parameters.length];
                for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
                    attributeExpressionExecutors[i] = ExpressionParser.parseExpression(parameters[i], metaEvent, stateIndex, eventTableMap, variableExpressionExecutors,
                            executionPlanContext, false, SiddhiConstants.CURRENT);
                }
            } else {
                List<Attribute> attributeList = metaStreamEvent.getLastInputDefinition().getAttributeList();
                int parameterSize = attributeList.size();
                attributeExpressionExecutors = new ExpressionExecutor[parameterSize];
                for (int i = 0; i < parameterSize; i++) {
                    attributeExpressionExecutors[i] = ExpressionParser.parseExpression(new Variable(attributeList.get(i).getName()), metaEvent, stateIndex, eventTableMap, variableExpressionExecutors,
                            executionPlanContext, false, SiddhiConstants.CURRENT);
                }
            }
        } else {
            attributeExpressionExecutors = new ExpressionExecutor[0];
        }

        if (streamHandler instanceof Filter) {
            return new FilterProcessor(attributeExpressionExecutors[0]);

        } else if (streamHandler instanceof Window) {
            WindowProcessor windowProcessor;
            if (streamHandler instanceof Extension) {
                windowProcessor = (WindowProcessor) SiddhiClassLoader.loadExtensionImplementation((Extension) streamHandler,
                        WindowProcessorExtensionHolder.getInstance(executionPlanContext));
            } else {
                windowProcessor = (WindowProcessor) SiddhiClassLoader.loadSiddhiImplementation(((Window) streamHandler).getFunction(),
                        WindowProcessor.class);
            }
            windowProcessor.initProcessor(metaStreamEvent.getLastInputDefinition(), attributeExpressionExecutors, executionPlanContext, outputExpectsExpiredEvents);
            return windowProcessor;

        } else if (streamHandler instanceof StreamFunction) {
            AbstractStreamProcessor abstractStreamProcessor;
            if (supportsBatchProcessing) {
                try {
                    if (streamHandler instanceof Extension) {
                        abstractStreamProcessor = (StreamProcessor) SiddhiClassLoader.loadExtensionImplementation((Extension) streamHandler,
                                StreamProcessorExtensionHolder.getInstance(executionPlanContext));
                    } else {
                        abstractStreamProcessor = (StreamProcessor) SiddhiClassLoader.loadSiddhiImplementation(
                                ((StreamFunction) streamHandler).getFunction(), StreamProcessor.class);
                    }
                    metaStreamEvent.addInputDefinition(abstractStreamProcessor.initProcessor(metaStreamEvent.getLastInputDefinition(),
                            attributeExpressionExecutors, executionPlanContext, outputExpectsExpiredEvents));
                    return abstractStreamProcessor;
                } catch (ExecutionPlanCreationException e) {
                    if (!e.isClassLoadingIssue()) {
                        throw e;
                    }
                }
            }
            if (streamHandler instanceof Extension) {
                abstractStreamProcessor = (StreamFunctionProcessor) SiddhiClassLoader.loadExtensionImplementation((Extension) streamHandler,
                        StreamFunctionProcessorExtensionHolder.getInstance(executionPlanContext));
            } else {
                abstractStreamProcessor = (StreamFunctionProcessor) SiddhiClassLoader.loadSiddhiImplementation(
                        ((StreamFunction) streamHandler).getFunction(), StreamFunctionProcessor.class);
            }
            metaStreamEvent.addInputDefinition(abstractStreamProcessor.initProcessor(metaStreamEvent.getLastInputDefinition(),
                    attributeExpressionExecutors, executionPlanContext, outputExpectsExpiredEvents));
            return abstractStreamProcessor;
        } else {
            throw new IllegalStateException(streamHandler.getClass().getName() + " is not supported");
        }
    }

    /**
     * Method to generate MetaStreamEvent reagent to the given input stream. Empty definition will be created and
     * definition and reference is will be set accordingly in this method.
     *
     * @param inputStream         InputStream
     * @param streamDefinitionMap StreamDefinition Map
     * @param tableDefinitionMap  TableDefinition Map
     * @param metaStreamEvent     MetaStreamEvent
     */
    private static void initMetaStreamEvent(SingleInputStream inputStream, Map<String,
            AbstractDefinition> streamDefinitionMap, Map<String, AbstractDefinition> tableDefinitionMap, MetaStreamEvent metaStreamEvent) {

        String streamId = inputStream.getStreamId();

        if (streamDefinitionMap != null && streamDefinitionMap.containsKey(streamId)) {
            AbstractDefinition inputDefinition = streamDefinitionMap.get(streamId);
            metaStreamEvent.addInputDefinition(inputDefinition);
        } else if (!inputStream.isInnerStream() && tableDefinitionMap != null && tableDefinitionMap.containsKey(streamId)) {
            AbstractDefinition inputDefinition = tableDefinitionMap.get(streamId);
            metaStreamEvent.addInputDefinition(inputDefinition);
        } else {
            throw new ExecutionPlanCreationException("Stream/table definition with ID '" + inputStream.getStreamId() + "' has not been defined");
        }

        if ((inputStream.getStreamReferenceId() != null) &&
                !(inputStream.getStreamId()).equals(inputStream.getStreamReferenceId())) { //if ref id is provided
            metaStreamEvent.setInputReferenceId(inputStream.getStreamReferenceId());
        }
    }


}
