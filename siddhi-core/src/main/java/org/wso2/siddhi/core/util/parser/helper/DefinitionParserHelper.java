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

package org.wso2.siddhi.core.util.parser.helper;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.function.EvalScript;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.stream.input.source.OutputMapper;
import org.wso2.siddhi.core.stream.input.source.OutputTransport;
import org.wso2.siddhi.core.stream.output.sink.InputMapper;
import org.wso2.siddhi.core.stream.output.sink.InputTransport;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.table.InMemoryEventTable;
import org.wso2.siddhi.core.trigger.CronEventTrigger;
import org.wso2.siddhi.core.trigger.EventTrigger;
import org.wso2.siddhi.core.trigger.PeriodicEventTrigger;
import org.wso2.siddhi.core.trigger.StartEventTrigger;
import org.wso2.siddhi.core.util.SiddhiClassLoader;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.extension.holder.EvalScriptExtensionHolder;
import org.wso2.siddhi.core.util.extension.holder.EventTableExtensionHolder;
import org.wso2.siddhi.core.util.extension.holder.InputMapperExecutorExtensionHolder;
import org.wso2.siddhi.core.util.extension.holder.InputTransportExecutorExtensionHolder;
import org.wso2.siddhi.core.util.extension.holder.OutputMapperExecutorExtensionHolder;
import org.wso2.siddhi.core.util.extension.holder.OutputTransportExecutorExtensionHolder;
import org.wso2.siddhi.core.window.EventWindow;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.FunctionDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.definition.TriggerDefinition;
import org.wso2.siddhi.query.api.definition.WindowDefinition;
import org.wso2.siddhi.query.api.definition.io.Store;
import org.wso2.siddhi.query.api.exception.DuplicateDefinitionException;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.execution.io.map.AttributeMapping;
import org.wso2.siddhi.query.api.extension.Extension;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class for queryParser to help with QueryRuntime
 * generation.
 */
public class DefinitionParserHelper {


    public static void validateDefinition(AbstractDefinition definition, ConcurrentMap<String, AbstractDefinition> streamDefinitionMap,
                                          ConcurrentMap<String, AbstractDefinition> tableDefinitionMap, ConcurrentMap<String, AbstractDefinition> windowDefinitionMap) {
        AbstractDefinition existingTableDefinition = tableDefinitionMap.get(definition.getId());
        if (existingTableDefinition != null && (!existingTableDefinition.equals(definition) || definition instanceof StreamDefinition)) {
            throw new DuplicateDefinitionException("Table Definition with same Stream Id '" +
                    definition.getId() + "' already exist : " + existingTableDefinition +
                    ", hence cannot add " + definition);
        }
        AbstractDefinition existingStreamDefinition = streamDefinitionMap.get(definition.getId());
        if (existingStreamDefinition != null && (!existingStreamDefinition.equals(definition) || definition instanceof TableDefinition)) {
            throw new DuplicateDefinitionException("Stream Definition with same Stream Id '" +
                    definition.getId() + "' already exist : " + existingStreamDefinition +
                    ", hence cannot add " + definition);
        }
        AbstractDefinition existingWindowDefinition = windowDefinitionMap.get(definition.getId());
        if (existingWindowDefinition != null && (!existingWindowDefinition.equals(definition) || definition instanceof WindowDefinition)) {
            throw new DuplicateDefinitionException("Window Definition with same Window Id '" +
                    definition.getId() + "' already exist : " + existingWindowDefinition +
                    ", hence cannot add " + definition);
        }
        // TODO: 1/29/17 add source / sink both validation here
    }


    public static void addStreamJunction(StreamDefinition streamDefinition, ConcurrentMap<String, StreamJunction> streamJunctionMap, ExecutionPlanContext executionPlanContext) {
        if (!streamJunctionMap.containsKey(streamDefinition.getId())) {
            StreamJunction streamJunction = new StreamJunction(streamDefinition,
                    executionPlanContext.getExecutorService(),
                    executionPlanContext.getBufferSize(), executionPlanContext);
            streamJunctionMap.putIfAbsent(streamDefinition.getId(), streamJunction);

        }
    }

    public static void validateOutputStream(StreamDefinition outputStreamDefinition, AbstractDefinition existingStream) {
        if (!existingStream.equalsIgnoreAnnotations(outputStreamDefinition)) {
            throw new DuplicateDefinitionException("Different definition same as output stream definition :" + outputStreamDefinition + " already exist as:" + existingStream);
        }
    }

    public static void addEventTable(TableDefinition tableDefinition, ConcurrentMap<String, EventTable> eventTableMap, ExecutionPlanContext executionPlanContext) {

        if (!eventTableMap.containsKey(tableDefinition.getId())) {

            MetaStreamEvent tableMetaStreamEvent = new MetaStreamEvent();
            tableMetaStreamEvent.addInputDefinition(tableDefinition);
            for (Attribute attribute : tableDefinition.getAttributeList()) {
                tableMetaStreamEvent.addOutputData(attribute);
            }

            StreamEventPool tableStreamEventPool = new StreamEventPool(tableMetaStreamEvent, 10);
            StreamEventCloner tableStreamEventCloner = new StreamEventCloner(tableMetaStreamEvent, tableStreamEventPool);

            Annotation annotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_FROM,
                    tableDefinition.getAnnotations()); //// TODO: 12/5/16 this must be removed

            Store store = tableDefinition.getStore();

            EventTable eventTable;
            if (annotation != null) {
                final String evenTableType = annotation.getElement(SiddhiConstants.EVENT_TABLE);
                Extension extension = new Extension() {
                    @Override
                    public String getNamespace() {
                        return SiddhiConstants.EVENT_TABLE;
                    }

                    @Override
                    public String getName() {
                        return evenTableType;
                    }
                };
                eventTable = (EventTable) SiddhiClassLoader.loadExtensionImplementation(extension, EventTableExtensionHolder.getInstance(executionPlanContext));
            } else if (store != null) {
                final String evenTableType = store.getType();
                Extension extension = new Extension() { //// TODO: 12/5/16 check this
                    @Override
                    public String getNamespace() {
                        return SiddhiConstants.EVENT_TABLE;
                    }

                    @Override
                    public String getName() {
                        return evenTableType;
                    }
                };
                eventTable = (EventTable) SiddhiClassLoader.loadExtensionImplementation(extension, EventTableExtensionHolder.getInstance(executionPlanContext));
            } else {
                eventTable = new InMemoryEventTable();
            }
            eventTable.init(tableDefinition, tableMetaStreamEvent, tableStreamEventPool, tableStreamEventCloner, executionPlanContext);
            eventTableMap.putIfAbsent(tableDefinition.getId(), eventTable);
        }
    }

    public static void addWindow(WindowDefinition windowDefinition, ConcurrentMap<String, EventWindow> eventWindowMap, ExecutionPlanContext executionPlanContext) {
        if (!eventWindowMap.containsKey(windowDefinition.getId())) {
            EventWindow eventTable = new EventWindow(windowDefinition, executionPlanContext);
            eventWindowMap.putIfAbsent(windowDefinition.getId(), eventTable);
        }
    }

    public static void addFunction(ExecutionPlanContext executionPlanContext, final FunctionDefinition functionDefinition) {
        EvalScript evalScript = (EvalScript) SiddhiClassLoader.loadExtensionImplementation(
                new Extension() {
                    @Override
                    public String getNamespace() {
                        return "evalscript";
                    }

                    @Override
                    public String getName() {
                        return functionDefinition.getLanguage().toLowerCase();
                    }
                }, EvalScriptExtensionHolder.getInstance(executionPlanContext));
        evalScript.setReturnType(functionDefinition.getReturnType());
        evalScript.init(functionDefinition.getId(), functionDefinition.getBody());
        executionPlanContext.getScriptFunctionMap().put(functionDefinition.getId(), evalScript);
    }

    public static void validateDefinition(TriggerDefinition triggerDefinition) {
        if (triggerDefinition.getId() != null) {
            if (triggerDefinition.getAtEvery() == null) {
                String expression = triggerDefinition.getAt();
                if (expression == null) {
                    throw new ExecutionPlanValidationException("Trigger Definition '" + triggerDefinition.getId() + "' must have trigger time defined");
                } else {
                    if (!expression.trim().equalsIgnoreCase(SiddhiConstants.TRIGGER_START)) {
                        try {
                            org.quartz.CronExpression.isValidExpression(expression);
                        } catch (Throwable t) {
                            throw new ExecutionPlanValidationException("Trigger Definition '" + triggerDefinition.getId() +
                                    "' have invalid trigger time defined, expected 'start' or valid cron but found '" + expression + "'");
                        }
                    }
                }
            } else if (triggerDefinition.getAt() != null) {
                throw new ExecutionPlanValidationException("Trigger Definition '" + triggerDefinition.getId() + "' must either have trigger time in cron or 'start' or time interval defined, and it cannot have more than one defined as '" + triggerDefinition + "'");
            }
        } else {
            throw new ExecutionPlanValidationException("Trigger Definition id cannot be null");
        }
    }

    public static void addEventTrigger(TriggerDefinition triggerDefinition, ConcurrentMap<String, EventTrigger> eventTriggerMap, ConcurrentMap<String, StreamJunction> streamJunctionMap, ExecutionPlanContext executionPlanContext) {
        if (!eventTriggerMap.containsKey(triggerDefinition.getId())) {
            EventTrigger eventTrigger;
            if (triggerDefinition.getAtEvery() != null) {
                eventTrigger = new PeriodicEventTrigger();
            } else if (triggerDefinition.getAt().trim().equalsIgnoreCase(SiddhiConstants.TRIGGER_START)) {
                eventTrigger = new StartEventTrigger();
            } else {
                eventTrigger = new CronEventTrigger();
            }
            StreamJunction streamJunction = streamJunctionMap.get(triggerDefinition.getId());
            eventTrigger.init(triggerDefinition, executionPlanContext, streamJunction);
            executionPlanContext.addEternalReferencedHolder(eventTrigger);
            eventTriggerMap.putIfAbsent(eventTrigger.getId(), eventTrigger);
        }
    }

    public static void addEventSource(StreamDefinition streamDefinition,
                                      ConcurrentMap<String, InputTransport> eventSourceMap,
                                      ExecutionPlanContext executionPlanContext) {
        Annotation sourceAnnotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_SOURCE,
                streamDefinition.getAnnotations());
        if (sourceAnnotation != null && !eventSourceMap.containsKey(streamDefinition.getId())) {
            Annotation mapAnnotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_MAP,
                    sourceAnnotation.getAnnotations());
            if (mapAnnotation != null) {
                final String sourceType = sourceAnnotation.getElement(SiddhiConstants.ANNOTATION_ELEMENT_TYPE);
                final String mapType = mapAnnotation.getElement(SiddhiConstants.ANNOTATION_ELEMENT_TYPE);
                if (sourceType != null && mapType != null) {
                    // load input transport extension
                    Extension source = new Extension() {
                        @Override
                        public String getNamespace() {
                            return SiddhiConstants.INPUT_TRANSPORT;
                        }

                        @Override
                        public String getName() {
                            return sourceType.toLowerCase();
                        }
                    };
                    InputTransport inputTransport = (InputTransport) SiddhiClassLoader.loadExtensionImplementation(
                            source, InputTransportExecutorExtensionHolder.getInstance(executionPlanContext));

                    // load input mapper extension
                    Extension mapper = new Extension() {
                        @Override
                        public String getNamespace() {
                            return SiddhiConstants.INPUT_MAPPER;
                        }

                        @Override
                        public String getName() {
                            return mapType.toLowerCase();
                        }
                    };
                    InputMapper inputMapper = (InputMapper) SiddhiClassLoader.loadExtensionImplementation(
                            mapper, InputMapperExecutorExtensionHolder.getInstance(executionPlanContext));

                    MetaStreamEvent metaStreamEvent = new MetaStreamEvent();
                    metaStreamEvent.setOutputDefinition(streamDefinition);
                    streamDefinition.getAttributeList().forEach(metaStreamEvent::addOutputData);

//                    inputMapper.init(streamDefinition, outputCallback, metaStreamEvent,
//                            (Map<String, String>) getOptions(mapAnnotation)[0], getAttributeMappings(mapAnnotation));
                    inputTransport.init((Map<String, String>) getOptions(sourceAnnotation)[0], inputMapper);

                    executionPlanContext.addEternalReferencedHolder(inputTransport);
                    // TODO: 1/31/17 add multiple event source support
                    eventSourceMap.putIfAbsent(streamDefinition.getId(), inputTransport);
                } else {
                    throw new ExecutionPlanCreationException("Both @source(type=) and @map(type=) are required.");
                }
            }
        }
    }

    public static void addEventSink(StreamDefinition streamDefinition,
                                    ConcurrentMap<String, OutputTransport> eventSinkMap,
                                    ExecutionPlanContext executionPlanContext) {
        Annotation sinkAnnotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_SINK,
                streamDefinition.getAnnotations());
        if (sinkAnnotation != null && !eventSinkMap.containsKey(streamDefinition.getId())) {
            Annotation mapAnnotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_MAP,
                    sinkAnnotation.getAnnotations());
            if (mapAnnotation != null) {
                final String sourceType = sinkAnnotation.getElement(SiddhiConstants.ANNOTATION_ELEMENT_TYPE);
                final String mapType = mapAnnotation.getElement(SiddhiConstants.ANNOTATION_ELEMENT_TYPE);

                if (sourceType != null && mapType != null) {
                    // load input transport extension
                    Extension sink = new Extension() {
                        @Override
                        public String getNamespace() {
                            return SiddhiConstants.OUTPUT_TRANSPORT;
                        }

                        @Override
                        public String getName() {
                            return sourceType;
                        }
                    };
                    OutputTransport outputTransport = (OutputTransport) SiddhiClassLoader.loadExtensionImplementation(
                            sink, OutputTransportExecutorExtensionHolder.getInstance(executionPlanContext));

                    // load input mapper extension
                    Extension mapper = new Extension() {
                        @Override
                        public String getNamespace() {
                            return SiddhiConstants.OUTPUT_MAPPER;
                        }

                        @Override
                        public String getName() {
                            return mapType;
                        }
                    };
                    OutputMapper outputMapper = (OutputMapper) SiddhiClassLoader.loadExtensionImplementation(
                            mapper, OutputMapperExecutorExtensionHolder.getInstance(executionPlanContext));

                    Object[] transportOptions = getOptions(sinkAnnotation);
                    Object[] mapperOptions = getOptions(mapAnnotation);
                    String payload = getPayload(mapAnnotation);

                    outputMapper.init(streamDefinition, mapType, payload,
                            (Map<String, String>) mapperOptions[0], (Map<String, String>) mapperOptions[1]);

                    outputTransport.init(executionPlanContext, streamDefinition, sourceType, outputMapper,
                            (Map<String, String>) transportOptions[0], (Map<String, String>) transportOptions[1]);

                    executionPlanContext.addEternalReferencedHolder(outputTransport);
                    // TODO: 1/31/17 add multiple event sink support
                    eventSinkMap.putIfAbsent(streamDefinition.getId(), outputTransport);
                } else {
                    throw new ExecutionPlanCreationException("Both @sink(type=) and @map(type=) are required.");
                }
            }
        }
    }

    private static List<AttributeMapping> getAttributeMappings(Annotation mapAnnotation) {
        List<AttributeMapping> mappings = new ArrayList<>();
        List<Annotation> attribAnnotations = mapAnnotation.getAnnotations(SiddhiConstants.ANNOTATION_ATTRIBUTES);
        if (attribAnnotations.size() > 0) {
            mappings.addAll(
                    attribAnnotations
                            .get(0)
                            .getElements()
                            .stream()
                            .map(element -> new AttributeMapping(element.getKey(), element.getValue()))
                            .collect(Collectors.toList())
            );
        }
        return mappings;
    }

    private static String getPayload(Annotation mapAnnotation) {
        List<Annotation> attribAnnotations = mapAnnotation.getAnnotations(SiddhiConstants.ANNOTATION_PAYLOAD);
        if (attribAnnotations.size() == 1) {
            List<Element> elements = attribAnnotations.get(0).getElements();
            if (elements.size() == 1) {
                return elements.get(0).getValue();
            } else {
                throw new ExecutionPlanCreationException("@payload() annotation should only contain single element.");
            }
        } else if (attribAnnotations.size() > 1){
            throw new ExecutionPlanCreationException("@map() annotation should only contain single @payload() annotation.");
        } else {
            return null;
        }
    }

    private static Object[] getOptions(Annotation annotation) {
        // returns [options, dynamicOptions]
        Object[] optionTuple = new Object[]{new HashMap<String, String>(), new HashMap<String, String>()};
        for (Element element : annotation.getElements()) {
            if (Pattern.matches("\\{\\{.*?}}", element.getValue())) {
                ((Map<String, String>) optionTuple[1]).put(element.getKey(), element.getValue());
            } else {
                ((Map<String, String>) optionTuple[0]).put(element.getKey(), element.getValue());
            }
        }
        return optionTuple;
    }
}
