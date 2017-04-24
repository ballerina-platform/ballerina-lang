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

package org.wso2.siddhi.core.util.parser.helper;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.function.EvalScript;
import org.wso2.siddhi.core.stream.AttributeMapping;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.stream.input.source.InputMapper;
import org.wso2.siddhi.core.stream.input.source.InputTransport;
import org.wso2.siddhi.core.stream.output.sink.*;
import org.wso2.siddhi.core.stream.output.sink.distributed.DistributedTransport;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.table.InMemoryEventTable;
import org.wso2.siddhi.core.trigger.CronEventTrigger;
import org.wso2.siddhi.core.trigger.EventTrigger;
import org.wso2.siddhi.core.trigger.PeriodicEventTrigger;
import org.wso2.siddhi.core.trigger.StartEventTrigger;
import org.wso2.siddhi.core.util.SiddhiClassLoader;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.extension.holder.*;
import org.wso2.siddhi.core.util.transport.Option;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.core.window.EventWindow;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.*;
import org.wso2.siddhi.query.api.exception.DuplicateDefinitionException;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.extension.Extension;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class for queryParser to help with QueryRuntime
 * generation.
 */
public class DefinitionParserHelper {


    public static void validateDefinition(AbstractDefinition definition, ConcurrentMap<String, AbstractDefinition> streamDefinitionMap,
                                          ConcurrentMap<String, AbstractDefinition> tableDefinitionMap,
                                          ConcurrentMap<String, AbstractDefinition> windowDefinitionMap,
                                          ConcurrentMap<String, AbstractDefinition> aggregationDefinitionMap) {
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
        AbstractDefinition existingAggregationDefinition = aggregationDefinitionMap.get(definition.getId());
        if (existingAggregationDefinition != null && (!existingAggregationDefinition.equals(definition) || definition instanceof AggregationDefinition)){
            throw new DuplicateDefinitionException("Aggregation Definition with same Aggregation Id '" +
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

            EventTable eventTable;
            if (annotation != null) {
                final String evenTableType = annotation.getElement(SiddhiConstants.NAMESPACE_EVENT_TABLE);
                Extension extension = new Extension() {
                    @Override
                    public String getNamespace() {
                        return SiddhiConstants.NAMESPACE_EVENT_TABLE;
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
            eventTable.init(tableDefinition, tableStreamEventPool, tableStreamEventCloner, executionPlanContext);
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
                                      ConcurrentMap<String, List<InputTransport>> eventSourceMap,
                                      ExecutionPlanContext executionPlanContext) {
        for (Annotation sourceAnnotation : streamDefinition.getAnnotations()) {
            if (SiddhiConstants.ANNOTATION_SOURCE.equalsIgnoreCase(sourceAnnotation.getName())) {
                Annotation mapAnnotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_MAP,
                        sourceAnnotation.getAnnotations());
                if (mapAnnotation == null) {
                    mapAnnotation = Annotation.annotation(SiddhiConstants.ANNOTATION_MAP).element(SiddhiConstants.ANNOTATION_ELEMENT_TYPE, "passThrough");
                }
                final String sourceType = sourceAnnotation.getElement(SiddhiConstants.ANNOTATION_ELEMENT_TYPE);
                final String mapType = mapAnnotation.getElement(SiddhiConstants.ANNOTATION_ELEMENT_TYPE);
                if (sourceType != null && mapType != null) {
                    // load input transport extension
                    Extension source = constructExtension(streamDefinition, SiddhiConstants.ANNOTATION_SOURCE,
                            sourceType, sourceAnnotation, SiddhiConstants.NAMESPACE_INPUT_TRANSPORT);
                    InputTransport inputTransport = (InputTransport) SiddhiClassLoader.loadExtensionImplementation(
                            source, InputTransportExecutorExtensionHolder.getInstance(executionPlanContext));

                    // load input mapper extension
                    Extension mapper = constructExtension(streamDefinition, SiddhiConstants.ANNOTATION_MAP,
                            mapType, sourceAnnotation, SiddhiConstants.NAMESPACE_INPUT_MAPPER);
                    InputMapper inputMapper = (InputMapper) SiddhiClassLoader.loadExtensionImplementation(
                            mapper, InputMapperExecutorExtensionHolder.getInstance(executionPlanContext));

                    OptionHolder sourceOptionHolder = constructOptionProcessor(streamDefinition, sourceAnnotation,
                            inputTransport.getClass().getAnnotation(org.wso2.siddhi.annotation.Extension.class), null);
                    OptionHolder mapOptionHolder = constructOptionProcessor(streamDefinition, mapAnnotation,
                            inputMapper.getClass().getAnnotation(org.wso2.siddhi.annotation.Extension.class), null);

                    inputMapper.init(streamDefinition, mapType, mapOptionHolder, getAttributeMappings(mapAnnotation));
                    inputTransport.init(sourceOptionHolder, inputMapper, executionPlanContext);

                    List<InputTransport> eventSources = eventSourceMap.get(streamDefinition.getId());
                    if (eventSources == null) {
                        eventSources = new ArrayList<InputTransport>();
                        eventSources.add(inputTransport);
                        eventSourceMap.put(streamDefinition.getId(), eventSources);
                    } else {
                        eventSources.add(inputTransport);
                    }
                } else {
                    throw new ExecutionPlanCreationException("Both @Sink(type=) and @map(type=) are required.");
                }
            }
        }
    }

    public static void addEventSink(StreamDefinition streamDefinition,
                                    ConcurrentMap<String, List<OutputTransport>> eventSinkMap,
                                    ExecutionPlanContext executionPlanContext) {
        for (Annotation sinkAnnotation : streamDefinition.getAnnotations()) {
            if (SiddhiConstants.ANNOTATION_SINK.equalsIgnoreCase(sinkAnnotation.getName())) {
                Annotation mapAnnotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_MAP,
                        sinkAnnotation.getAnnotations());
                if (mapAnnotation == null) {
                    mapAnnotation = Annotation.annotation(SiddhiConstants.ANNOTATION_MAP).element(SiddhiConstants.ANNOTATION_ELEMENT_TYPE, "passThrough");
                }
                Annotation distributionAnnotation =
                        AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_DISTRIBUTION,
                                sinkAnnotation.getAnnotations());

                boolean isDistributedTransport = (distributionAnnotation != null);
                String sinkType;

                if (isDistributedTransport) {
                    // All distributed transports falls under two generalized extensions. Here we instantiate the generalised extension and
                    // the actual underlying transport is instantiated inside the generalized extensions depending on the 'type' parameter
                    // in Sink options.
                    sinkType = (distributionAnnotation.getElement("channels") == null) ?
                            SiddhiConstants.EXTENSION_MULTI_ENDPOINT_TRANSPORT :
                            SiddhiConstants.EXTENSION_PARTITIONED_TRANSPORT;
                } else {
                    sinkType = sinkAnnotation.getElement(SiddhiConstants.ANNOTATION_ELEMENT_TYPE);
                }

                final String mapType = mapAnnotation.getElement(SiddhiConstants.ANNOTATION_ELEMENT_TYPE);
                if (sinkType != null && mapType != null) {
                    // load output transport extension
                    Extension sink = constructExtension(streamDefinition, SiddhiConstants.ANNOTATION_SINK,
                            sinkType, sinkAnnotation, SiddhiConstants.NAMESPACE_OUTPUT_TRANSPORT);
                    OutputTransport outputTransport = (OutputTransport) SiddhiClassLoader.loadExtensionImplementation(
                            sink, OutputTransportExecutorExtensionHolder.getInstance(executionPlanContext));

                    // load output mapper extension
                    Extension mapper = constructExtension(streamDefinition, SiddhiConstants.ANNOTATION_MAP,
                            mapType, sinkAnnotation, SiddhiConstants.NAMESPACE_OUTPUT_MAPPER);
                    OutputMapper outputMapper = (OutputMapper) SiddhiClassLoader.loadExtensionImplementation(
                            mapper, OutputMapperExecutorExtensionHolder.getInstance(executionPlanContext));

                    OptionHolder transportOptionHolder = constructOptionProcessor(streamDefinition, sinkAnnotation,
                            outputTransport.getClass().getAnnotation(org.wso2.siddhi.annotation.Extension.class),
                            outputTransport.getSupportedDynamicOptions());
                    OptionHolder mapOptionHolder = constructOptionProcessor(streamDefinition, mapAnnotation,
                            outputMapper.getClass().getAnnotation(org.wso2.siddhi.annotation.Extension.class),
                            outputMapper.getSupportedDynamicOptions());
                    String payload = getPayload(mapAnnotation);

                    outputTransport.init(streamDefinition, sinkType, transportOptionHolder, outputMapper, mapType,
                            mapOptionHolder, payload, executionPlanContext);

                    // Initializing output transport with distributed configurations
                    OptionHolder distributionOptionHolder = null;
                    List<OptionHolder> endpointOptionHolders = new ArrayList<>();
                    if (isDistributedTransport) {
                        distributionOptionHolder = constructOptionProcessor(streamDefinition,
                                distributionAnnotation,
                                outputTransport.getClass().
                                        getAnnotation(org.wso2.siddhi.annotation.Extension.class), outputTransport.getSupportedDynamicOptions());

                        distributionAnnotation.getAnnotations().stream()
                                .filter(annotation ->
                                        SiddhiConstants.ANNOTATION_ENDPOINT.equalsIgnoreCase(annotation.getName()))
                                .forEach(annotation ->
                                        endpointOptionHolders
                                                .add(constructOptionProcessor(streamDefinition, annotation,
                                                        outputTransport
                                                                .getClass()
                                                                .getAnnotation(org.wso2.siddhi.annotation.Extension.class), outputTransport.getSupportedDynamicOptions())));

                        ((DistributedTransport) outputTransport).
                                initDistributedTransportOptions(distributionOptionHolder, endpointOptionHolders);
                    }

                    OutputGroupDeterminer groupDeterminer = constructOutputGroupDeterminer(transportOptionHolder,
                            distributionOptionHolder, streamDefinition, endpointOptionHolders.size());
                    if (groupDeterminer != null) {
                        outputTransport.getMapper().setGroupDeterminer(groupDeterminer);
                    }

                    List<OutputTransport> eventSinks = eventSinkMap.get(streamDefinition.getId());
                    if (eventSinks == null) {
                        eventSinks = new ArrayList<OutputTransport>();
                        eventSinks.add(outputTransport);
                        eventSinkMap.put(streamDefinition.getId(), eventSinks);
                    } else {
                        eventSinks.add(outputTransport);
                    }
                } else {
                    throw new ExecutionPlanCreationException("Both @sink(type=) and @map(type=) are required.");
                }
            }
        }
    }

    private static OutputGroupDeterminer constructOutputGroupDeterminer(OptionHolder transportOptionHolder, OptionHolder distributedOptHolder,
                                                                        StreamDefinition streamDefinition, int endpointCount) {

        OutputGroupDeterminer groupDeterminer = null;
        if (distributedOptHolder != null) {
            int partitionCount = endpointCount;
            String strategy = distributedOptHolder
                    .validateAndGetStaticValue(DistributedTransport.DISTRIBUTION_STRATEGY_KEY);

            if (strategy.equalsIgnoreCase(DistributedTransport.DISTRIBUTION_STRATEGY_PARTITIONED)) {
                String partitionKeyField = distributedOptHolder
                        .validateAndGetStaticValue(DistributedTransport.PARTITION_KEY_FIELD_KEY);
                int partitioningFieldIndex = streamDefinition.getAttributePosition(partitionKeyField);

                if (distributedOptHolder.isOptionExists(DistributedTransport.DISTRIBUTION_CHANNELS_KEY)) {
                    partitionCount = Integer.parseInt(distributedOptHolder
                            .validateAndGetStaticValue(DistributedTransport.DISTRIBUTION_CHANNELS_KEY));
                }
                groupDeterminer = new PartitionedGroupDeterminer(partitioningFieldIndex, partitionCount);
            }
        }

        if (groupDeterminer == null) {
            List<Option> dynamicTransportOptions = new ArrayList<Option>(transportOptionHolder
                    .getDynamicOptionsKeys().size());

            transportOptionHolder.getDynamicOptionsKeys().
                    forEach(option -> dynamicTransportOptions.add(transportOptionHolder.validateAndGetOption(option)));

            if (dynamicTransportOptions.size() > 0) {
                groupDeterminer = new DynamicOptionGroupDeterminer(dynamicTransportOptions);
            }
        }

        return groupDeterminer;
    }

    private static Extension constructExtension(StreamDefinition streamDefinition, String typeName, String typeValue,
                                                Annotation annotation, String defaultNamespace) {
        String[] namespaceAndName = typeValue.split(SiddhiConstants.EXTENSION_SEPARATOR);
        String namespace;
        String name;
        if (namespaceAndName.length == 1) {
            namespace = defaultNamespace;
            name = namespaceAndName[0];
        } else if (namespaceAndName.length == 2) {
            namespace = namespaceAndName[0];
            name = namespaceAndName[1];
        } else {
            throw new ExecutionPlanCreationException("Malformed '" + typeName + "' annotation type '" + typeValue + "' " +
                    "provided, for annotation '" + annotation + "' on stream '" + streamDefinition.getId() + "', " +
                    "it should be either '<namespace>:<name>' or '<name>'");
        }
        return new Extension() {
            @Override
            public String getNamespace() {
                return namespace;
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    private static List<AttributeMapping> getAttributeMappings(Annotation mapAnnotation) {
        List<AttributeMapping> mappings = new ArrayList<>();
        List<Annotation> attributeAnnotations = mapAnnotation.getAnnotations(SiddhiConstants.ANNOTATION_ATTRIBUTES);
        if (attributeAnnotations.size() > 0) {
            mappings.addAll(
                    attributeAnnotations
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
        List<Annotation> attributeAnnotations = mapAnnotation.getAnnotations(SiddhiConstants.ANNOTATION_PAYLOAD);
        if (attributeAnnotations.size() == 1) {
            List<Element> elements = attributeAnnotations.get(0).getElements();
            if (elements.size() == 1) {
                return elements.get(0).getValue();
            } else {
                throw new ExecutionPlanCreationException("@payload() annotation should only contain single element.");
            }
        } else if (attributeAnnotations.size() > 1) {
            throw new ExecutionPlanCreationException("@map() annotation should only contain single @payload() annotation.");
        } else {
            return null;
        }
    }

    private static OptionHolder constructOptionProcessor(StreamDefinition streamDefinition, Annotation annotation,
                                                         org.wso2.siddhi.annotation.Extension extension,
                                                         String[] supportedDynamicOptions) {


        List<String> supportedDynamicOptionList = new ArrayList<>();
        if (supportedDynamicOptions != null) {
            supportedDynamicOptionList = Arrays.asList(supportedDynamicOptions);
        }

        Map<String, String> options = new HashMap<String, String>();
        Map<String, String> dynamicOptions = new HashMap<String, String>();
        for (Element element : annotation.getElements()) {
            if (Pattern.matches("\\{\\{.*?}}", element.getValue())) {
                if (supportedDynamicOptionList.contains(element.getKey())) {
                    dynamicOptions.put(element.getKey(), element.getValue());
                } else {
                    throw new ExecutionPlanCreationException("'" + element.getKey() + "' is not a supported DynamicOption " +
                            "for the Extension '" + extension.namespace() + ":" + extension.name() + "', it only supports " +
                            "following as its DynamicOptions: " + supportedDynamicOptionList);
                }
            } else {
                options.put(element.getKey(), element.getValue());
            }
        }
        return new OptionHolder(streamDefinition, options, dynamicOptions, extension);
    }
}
