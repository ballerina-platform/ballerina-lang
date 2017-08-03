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
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
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
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.Scheduler;
import org.wso2.siddhi.core.util.SiddhiClassLoader;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.extension.holder.StreamFunctionProcessorExtensionHolder;
import org.wso2.siddhi.core.util.extension.holder.StreamProcessorExtensionHolder;
import org.wso2.siddhi.core.util.extension.holder.WindowProcessorExtensionHolder;
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
     * @param siddhiAppContext            query to be parsed
     * @param variableExpressionExecutors List to hold VariableExpressionExecutors to update after query parsing
     * @param streamDefinitionMap         Stream Definition Map
     * @param tableDefinitionMap          Table Definition Map
     * @param windowDefinitionMap         window definition map
     * @param aggregationDefinitionMap    aggregation definition map
     * @param tableMap                    Table Map
     * @param metaComplexEvent            MetaComplexEvent
     * @param processStreamReceiver       ProcessStreamReceiver
     * @param supportsBatchProcessing     supports batch processing
     * @param outputExpectsExpiredEvents  is output expects ExpiredEvents
     * @param queryName                   query name of single input stream belongs to.       @return SingleStreamRuntime
     */
    public static SingleStreamRuntime parseInputStream(SingleInputStream inputStream,
                                                       SiddhiAppContext siddhiAppContext,
                                                       List<VariableExpressionExecutor> variableExpressionExecutors,
                                                       Map<String, AbstractDefinition> streamDefinitionMap,
                                                       Map<String, AbstractDefinition> tableDefinitionMap,
                                                       Map<String, AbstractDefinition> windowDefinitionMap,
                                                       Map<String, AbstractDefinition> aggregationDefinitionMap,
                                                       Map<String, Table> tableMap,
                                                       MetaComplexEvent metaComplexEvent,
                                                       ProcessStreamReceiver processStreamReceiver,
                                                       boolean supportsBatchProcessing,
                                                       boolean outputExpectsExpiredEvents, String queryName) {
        Processor processor = null;
        EntryValveProcessor entryValveProcessor = null;
        boolean first = true;
        MetaStreamEvent metaStreamEvent;
        if (metaComplexEvent instanceof MetaStateEvent) {
            metaStreamEvent = new MetaStreamEvent();
            ((MetaStateEvent) metaComplexEvent).addEvent(metaStreamEvent);
            initMetaStreamEvent(inputStream, streamDefinitionMap, tableDefinitionMap, windowDefinitionMap,
                    aggregationDefinitionMap, metaStreamEvent);
        } else {
            metaStreamEvent = (MetaStreamEvent) metaComplexEvent;
            initMetaStreamEvent(inputStream, streamDefinitionMap, tableDefinitionMap, windowDefinitionMap,
                    aggregationDefinitionMap, metaStreamEvent);
        }

        // A window cannot be defined for a window stream
        if (!inputStream.getStreamHandlers().isEmpty() && windowDefinitionMap != null && windowDefinitionMap
                .containsKey(inputStream.getStreamId())) {
            for (StreamHandler handler : inputStream.getStreamHandlers()) {
                if (handler instanceof Window) {
                    throw new OperationNotSupportedException("Cannot create " + ((Window) handler).getName() + " " +
                            "window for the window stream " + inputStream.getStreamId());
                }
            }
        }

        if (!inputStream.getStreamHandlers().isEmpty()) {
            for (StreamHandler handler : inputStream.getStreamHandlers()) {
                Processor currentProcessor = generateProcessor(handler, metaComplexEvent,
                        variableExpressionExecutors, siddhiAppContext, tableMap, supportsBatchProcessing,
                        outputExpectsExpiredEvents, queryName);
                if (currentProcessor instanceof SchedulingProcessor) {
                    if (entryValveProcessor == null) {

                        entryValveProcessor = new EntryValveProcessor(siddhiAppContext);
                        if (first) {
                            processor = entryValveProcessor;
                            first = false;
                        } else {
                            processor.setToLast(entryValveProcessor);
                        }
                    }
                    Scheduler scheduler = SchedulerParser.parse(siddhiAppContext.getScheduledExecutorService(),
                            entryValveProcessor, siddhiAppContext);
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


    public static Processor generateProcessor(StreamHandler streamHandler, MetaComplexEvent metaEvent,
                                              List<VariableExpressionExecutor> variableExpressionExecutors,
                                              SiddhiAppContext siddhiAppContext, Map<String, Table> tableMap,
                                              boolean supportsBatchProcessing, boolean outputExpectsExpiredEvents,
                                              String queryName) {
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
                    attributeExpressionExecutors[i] = ExpressionParser.parseExpression(parameters[i], metaEvent,
                            stateIndex, tableMap, variableExpressionExecutors,
                            siddhiAppContext, false, SiddhiConstants.CURRENT, queryName);
                }
            } else {
                List<Attribute> attributeList = metaStreamEvent.getLastInputDefinition().getAttributeList();
                int parameterSize = attributeList.size();
                attributeExpressionExecutors = new ExpressionExecutor[parameterSize];
                for (int i = 0; i < parameterSize; i++) {
                    attributeExpressionExecutors[i] = ExpressionParser.parseExpression(new Variable(attributeList.get
                                    (i).getName()), metaEvent, stateIndex, tableMap, variableExpressionExecutors,
                            siddhiAppContext, false, SiddhiConstants.CURRENT, queryName);
                }
            }
        } else {
            attributeExpressionExecutors = new ExpressionExecutor[0];
        }

        ConfigReader configReader;
        if (streamHandler instanceof Filter) {
            return new FilterProcessor(attributeExpressionExecutors[0]);

        } else if (streamHandler instanceof Window) {
            WindowProcessor windowProcessor = (WindowProcessor) SiddhiClassLoader.loadExtensionImplementation(
                    (Extension) streamHandler,
                    WindowProcessorExtensionHolder.getInstance(siddhiAppContext));
            configReader = siddhiAppContext.getSiddhiContext().getConfigManager().
                    generateConfigReader(((Window) streamHandler).getNamespace(),
                            ((Window) streamHandler).getName());
            windowProcessor.initProcessor(metaStreamEvent.getLastInputDefinition(), attributeExpressionExecutors,
                    configReader, siddhiAppContext, outputExpectsExpiredEvents, queryName);
            return windowProcessor;

        } else if (streamHandler instanceof StreamFunction) {
            AbstractStreamProcessor abstractStreamProcessor;
            configReader = siddhiAppContext.getSiddhiContext().getConfigManager().
                    generateConfigReader(((StreamFunction) streamHandler).getNamespace(),
                            ((StreamFunction) streamHandler).getName());
            if (supportsBatchProcessing) {
                try {
                    abstractStreamProcessor = (StreamProcessor) SiddhiClassLoader.loadExtensionImplementation(
                            (Extension) streamHandler,
                            StreamProcessorExtensionHolder.getInstance(siddhiAppContext));
                    metaStreamEvent.addInputDefinition(abstractStreamProcessor.initProcessor(metaStreamEvent
                                    .getLastInputDefinition(),
                            attributeExpressionExecutors, configReader, siddhiAppContext,
                            outputExpectsExpiredEvents, queryName));
                    return abstractStreamProcessor;
                } catch (SiddhiAppCreationException e) {
                    if (!e.isClassLoadingIssue()) {
                        throw e;
                    }
                }
            }
            abstractStreamProcessor = (StreamFunctionProcessor) SiddhiClassLoader.loadExtensionImplementation(
                    (Extension) streamHandler,
                    StreamFunctionProcessorExtensionHolder.getInstance(siddhiAppContext));
            metaStreamEvent.addInputDefinition(abstractStreamProcessor.initProcessor(metaStreamEvent
                            .getLastInputDefinition(),
                    attributeExpressionExecutors, configReader, siddhiAppContext, outputExpectsExpiredEvents,
                    queryName));
            return abstractStreamProcessor;
        } else {
            throw new IllegalStateException(streamHandler.getClass().getName() + " is not supported");
        }
    }

    /**
     * Method to generate MetaStreamEvent reagent to the given input stream. Empty definition will be created and
     * definition and reference is will be set accordingly in this method.
     *
     * @param inputStream              InputStream
     * @param streamDefinitionMap      StreamDefinition Map
     * @param tableDefinitionMap       TableDefinition Map
     * @param aggregationDefinitionMap AggregationDefinition Map
     * @param metaStreamEvent          MetaStreamEvent
     */
    private static void initMetaStreamEvent(SingleInputStream inputStream,
                                            Map<String, AbstractDefinition> streamDefinitionMap,
                                            Map<String, AbstractDefinition> tableDefinitionMap,
                                            Map<String, AbstractDefinition> windowDefinitionMap,
                                            Map<String, AbstractDefinition> aggregationDefinitionMap,
                                            MetaStreamEvent metaStreamEvent) {
        String streamId = inputStream.getStreamId();

        if (!inputStream.isInnerStream() && windowDefinitionMap != null && windowDefinitionMap.containsKey(streamId)) {
            AbstractDefinition inputDefinition = windowDefinitionMap.get(streamId);
            if (!metaStreamEvent.getInputDefinitions().contains(inputDefinition)) {
                metaStreamEvent.addInputDefinition(inputDefinition);
            }
        } else if (streamDefinitionMap != null && streamDefinitionMap.containsKey(streamId)) {
            AbstractDefinition inputDefinition = streamDefinitionMap.get(streamId);
            metaStreamEvent.addInputDefinition(inputDefinition);
        } else if (!inputStream.isInnerStream() && tableDefinitionMap != null &&
                tableDefinitionMap.containsKey(streamId)) {
            AbstractDefinition inputDefinition = tableDefinitionMap.get(streamId);
            metaStreamEvent.addInputDefinition(inputDefinition);
        } else if (!inputStream.isInnerStream() && aggregationDefinitionMap != null &&
                aggregationDefinitionMap.containsKey(streamId)) {
            AbstractDefinition inputDefinition = aggregationDefinitionMap.get(streamId);
            metaStreamEvent.addInputDefinition(inputDefinition);
        } else {
            throw new SiddhiAppCreationException("Stream/table/window/aggregation definition with ID '" +
                    inputStream.getStreamId() + "' has not been defined");
        }

        if ((inputStream.getStreamReferenceId() != null) &&
                !(inputStream.getStreamId()).equals(inputStream.getStreamReferenceId())) { //if ref id is provided
            metaStreamEvent.setInputReferenceId(inputStream.getStreamReferenceId());
        }
    }


}
