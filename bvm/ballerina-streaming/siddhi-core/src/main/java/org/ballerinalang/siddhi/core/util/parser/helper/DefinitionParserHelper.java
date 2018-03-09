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
import org.ballerinalang.siddhi.core.event.stream.MetaStreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEventCloner;
import org.ballerinalang.siddhi.core.event.stream.StreamEventPool;
import org.ballerinalang.siddhi.core.exception.SiddhiAppCreationException;
import org.ballerinalang.siddhi.core.function.Script;
import org.ballerinalang.siddhi.core.stream.StreamJunction;
import org.ballerinalang.siddhi.core.stream.input.source.AttributeMapping;
import org.ballerinalang.siddhi.core.stream.input.source.Source;
import org.ballerinalang.siddhi.core.stream.input.source.SourceHandler;
import org.ballerinalang.siddhi.core.stream.input.source.SourceHandlerManager;
import org.ballerinalang.siddhi.core.stream.input.source.SourceMapper;
import org.ballerinalang.siddhi.core.stream.output.sink.DynamicOptionGroupDeterminer;
import org.ballerinalang.siddhi.core.stream.output.sink.OutputGroupDeterminer;
import org.ballerinalang.siddhi.core.stream.output.sink.PartitionedGroupDeterminer;
import org.ballerinalang.siddhi.core.stream.output.sink.Sink;
import org.ballerinalang.siddhi.core.stream.output.sink.SinkHandler;
import org.ballerinalang.siddhi.core.stream.output.sink.SinkHandlerManager;
import org.ballerinalang.siddhi.core.stream.output.sink.SinkMapper;
import org.ballerinalang.siddhi.core.stream.output.sink.distributed.DistributedTransport;
import org.ballerinalang.siddhi.core.stream.output.sink.distributed.DistributionStrategy;
import org.ballerinalang.siddhi.core.table.InMemoryTable;
import org.ballerinalang.siddhi.core.table.Table;
import org.ballerinalang.siddhi.core.table.record.RecordTableHandler;
import org.ballerinalang.siddhi.core.table.record.RecordTableHandlerManager;
import org.ballerinalang.siddhi.core.trigger.CronTrigger;
import org.ballerinalang.siddhi.core.trigger.PeriodicTrigger;
import org.ballerinalang.siddhi.core.trigger.StartTrigger;
import org.ballerinalang.siddhi.core.trigger.Trigger;
import org.ballerinalang.siddhi.core.util.ExceptionUtil;
import org.ballerinalang.siddhi.core.util.SiddhiClassLoader;
import org.ballerinalang.siddhi.core.util.SiddhiConstants;
import org.ballerinalang.siddhi.core.util.config.ConfigReader;
import org.ballerinalang.siddhi.core.util.extension.holder.DistributionStrategyExtensionHolder;
import org.ballerinalang.siddhi.core.util.extension.holder.ScriptExtensionHolder;
import org.ballerinalang.siddhi.core.util.extension.holder.SinkExecutorExtensionHolder;
import org.ballerinalang.siddhi.core.util.extension.holder.SinkMapperExecutorExtensionHolder;
import org.ballerinalang.siddhi.core.util.extension.holder.SourceExecutorExtensionHolder;
import org.ballerinalang.siddhi.core.util.extension.holder.SourceMapperExecutorExtensionHolder;
import org.ballerinalang.siddhi.core.util.extension.holder.TableExtensionHolder;
import org.ballerinalang.siddhi.core.util.transport.MultiClientDistributedSink;
import org.ballerinalang.siddhi.core.util.transport.Option;
import org.ballerinalang.siddhi.core.util.transport.OptionHolder;
import org.ballerinalang.siddhi.core.util.transport.SingleClientDistributedSink;
import org.ballerinalang.siddhi.core.window.Window;
import org.ballerinalang.siddhi.query.api.annotation.Annotation;
import org.ballerinalang.siddhi.query.api.annotation.Element;
import org.ballerinalang.siddhi.query.api.definition.AbstractDefinition;
import org.ballerinalang.siddhi.query.api.definition.AggregationDefinition;
import org.ballerinalang.siddhi.query.api.definition.Attribute;
import org.ballerinalang.siddhi.query.api.definition.FunctionDefinition;
import org.ballerinalang.siddhi.query.api.definition.StreamDefinition;
import org.ballerinalang.siddhi.query.api.definition.TableDefinition;
import org.ballerinalang.siddhi.query.api.definition.TriggerDefinition;
import org.ballerinalang.siddhi.query.api.definition.WindowDefinition;
import org.ballerinalang.siddhi.query.api.exception.DuplicateDefinitionException;
import org.ballerinalang.siddhi.query.api.exception.SiddhiAppValidationException;
import org.ballerinalang.siddhi.query.api.extension.Extension;
import org.ballerinalang.siddhi.query.api.util.AnnotationHelper;

import java.util.ArrayList;
import java.util.Arrays;
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


    public static void validateDefinition(AbstractDefinition definition,
                                          ConcurrentMap<String, AbstractDefinition> streamDefinitionMap,
                                          ConcurrentMap<String, AbstractDefinition> tableDefinitionMap,
                                          ConcurrentMap<String, AbstractDefinition> windowDefinitionMap,
                                          ConcurrentMap<String, AbstractDefinition> aggregationDefinitionMap) {
        AbstractDefinition existingTableDefinition = tableDefinitionMap.get(definition.getId());
        if (existingTableDefinition != null && (!existingTableDefinition.equals(definition) || definition instanceof
                StreamDefinition)) {
            throw new DuplicateDefinitionException("Table Definition with same Stream Id '" +
                    definition.getId() + "' already exist : " + existingTableDefinition +
                    ", hence cannot add " + definition, definition.getQueryContextStartIndex(),
                    definition.getQueryContextEndIndex());
        }
        AbstractDefinition existingStreamDefinition = streamDefinitionMap.get(definition.getId());
        if (existingStreamDefinition != null && (!existingStreamDefinition.equals(definition) || definition
                instanceof TableDefinition)) {
            throw new DuplicateDefinitionException("Stream Definition with same Stream Id '" +
                    definition.getId() + "' already exist : " + existingStreamDefinition +
                    ", hence cannot add " + definition, definition.getQueryContextStartIndex(),
                    definition.getQueryContextEndIndex());
        }
        AbstractDefinition existingWindowDefinition = windowDefinitionMap.get(definition.getId());
        if (existingWindowDefinition != null && (!existingWindowDefinition.equals(definition) || definition
                instanceof WindowDefinition)) {
            throw new DuplicateDefinitionException("Window Definition with same Window Id '" +
                    definition.getId() + "' already exist : " + existingWindowDefinition +
                    ", hence cannot add " + definition, definition.getQueryContextStartIndex(),
                    definition.getQueryContextEndIndex());
        }
        AbstractDefinition existingAggregationDefinition = aggregationDefinitionMap.get(definition.getId());
        if (existingAggregationDefinition != null
                && (!existingAggregationDefinition.equals(definition)
                || definition instanceof AggregationDefinition)) {
            throw new DuplicateDefinitionException(
                    "Aggregation Definition with same Aggregation Id '" + definition.getId() + "' already exist : "
                            + existingWindowDefinition + ", hence cannot add " + definition,
                    definition.getQueryContextStartIndex(), definition.getQueryContextEndIndex());
        }
    }

    public static void addStreamJunction(StreamDefinition streamDefinition,
                                         ConcurrentMap<String, StreamJunction> streamJunctionMap,
                                         SiddhiAppContext siddhiAppContext) {
        if (!streamJunctionMap.containsKey(streamDefinition.getId())) {
            StreamJunction streamJunction = new StreamJunction(streamDefinition,
                    siddhiAppContext.getExecutorService(),
                    siddhiAppContext.getBufferSize(), siddhiAppContext);
            streamJunctionMap.putIfAbsent(streamDefinition.getId(), streamJunction);
        }
    }

    public static void validateOutputStream(StreamDefinition outputStreamDefinition,
                                            AbstractDefinition existingStream) {
        if (!existingStream.equalsIgnoreAnnotations(outputStreamDefinition)) {
            throw new DuplicateDefinitionException("Different definition same as output stream definition '" +
                    outputStreamDefinition + "' already exist as '" + existingStream + "'",
                    outputStreamDefinition.getQueryContextStartIndex(),
                    outputStreamDefinition.getQueryContextEndIndex());
        }
    }

    public static void addTable(TableDefinition tableDefinition, ConcurrentMap<String, Table> tableMap,
                                SiddhiAppContext siddhiAppContext) {

        if (!tableMap.containsKey(tableDefinition.getId())) {

            MetaStreamEvent tableMetaStreamEvent = new MetaStreamEvent();
            tableMetaStreamEvent.addInputDefinition(tableDefinition);
            for (Attribute attribute : tableDefinition.getAttributeList()) {
                tableMetaStreamEvent.addOutputData(attribute);
            }

            StreamEventPool tableStreamEventPool = new StreamEventPool(tableMetaStreamEvent, 10);
            StreamEventCloner tableStreamEventCloner = new StreamEventCloner(tableMetaStreamEvent,
                    tableStreamEventPool);

            Annotation annotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_STORE,
                    tableDefinition.getAnnotations());

            Table table;
            ConfigReader configReader = null;
            RecordTableHandlerManager recordTableHandlerManager = null;
            RecordTableHandler recordTableHandler = null;
            if (annotation != null) {
                annotation = updateAnnotationRef(annotation, SiddhiConstants.NAMESPACE_STORE, siddhiAppContext);
                String tableType = annotation.getElement(SiddhiConstants.ANNOTATION_ELEMENT_TYPE);
                Extension extension = new Extension() {
                    @Override
                    public String getNamespace() {
                        return SiddhiConstants.NAMESPACE_STORE;
                    }

                    @Override
                    public String getName() {
                        return tableType;
                    }
                };
                recordTableHandlerManager = siddhiAppContext.getSiddhiContext().getRecordTableHandlerManager();
                if (recordTableHandlerManager != null) {
                    recordTableHandler = recordTableHandlerManager.generateRecordTableHandler();
                }
                table = (Table) SiddhiClassLoader.loadExtensionImplementation(extension,
                        TableExtensionHolder.getInstance(siddhiAppContext));
                configReader = siddhiAppContext.getSiddhiContext().getConfigManager()
                        .generateConfigReader(extension.getNamespace(), extension.getName());
            } else {
                table = new InMemoryTable();
            }
            table.initTable(tableDefinition, tableStreamEventPool, tableStreamEventCloner, configReader,
                    siddhiAppContext, recordTableHandler);
            if (recordTableHandler != null) {
                recordTableHandlerManager.registerRecordTableHandler(recordTableHandler.getElementId(),
                        recordTableHandler);
            }
            tableMap.putIfAbsent(tableDefinition.getId(), table);
        }
    }

    public static void addWindow(WindowDefinition windowDefinition,
                                 ConcurrentMap<String, Window> eventWindowMap,
                                 SiddhiAppContext siddhiAppContext) {
        if (!eventWindowMap.containsKey(windowDefinition.getId())) {
            Window table = new Window(windowDefinition, siddhiAppContext);
            eventWindowMap.putIfAbsent(windowDefinition.getId(), table);
        }
    }

    public static void addFunction(SiddhiAppContext siddhiAppContext,
                                   final FunctionDefinition functionDefinition) {

        Extension extension = new Extension() {
            @Override
            public String getNamespace() {
                return "script";
            }

            @Override
            public String getName() {
                return functionDefinition.getLanguage().toLowerCase();
            }
        };
        try {
            Script script = (Script) SiddhiClassLoader.loadExtensionImplementation(extension,
                    ScriptExtensionHolder.getInstance(siddhiAppContext));
            ConfigReader configReader = siddhiAppContext.getSiddhiContext().getConfigManager()
                    .generateConfigReader(extension.getNamespace(), extension.getName());
            script.setReturnType(functionDefinition.getReturnType());
            script.init(functionDefinition.getId(), functionDefinition.getBody(), configReader);
            siddhiAppContext.getScriptFunctionMap().put(functionDefinition.getId(), script);
        } catch (Throwable t) {
            ExceptionUtil.populateQueryContext(t, functionDefinition, siddhiAppContext);
            throw t;
        }
    }

    public static void validateDefinition(TriggerDefinition triggerDefinition) {
        if (triggerDefinition.getId() != null) {
            if (triggerDefinition.getAtEvery() == null) {
                String expression = triggerDefinition.getAt();
                if (expression == null) {
                    throw new SiddhiAppValidationException("Trigger Definition '" + triggerDefinition.getId() +
                            "' must have trigger time defined");
                } else {
                    if (!expression.trim().equalsIgnoreCase(SiddhiConstants.TRIGGER_START)) {
                        try {
                            org.quartz.CronExpression.isValidExpression(expression);
                        } catch (Throwable t) {
                            throw new SiddhiAppValidationException("Trigger Definition '" + triggerDefinition
                                    .getId()
                                    + "' have invalid trigger time defined, expected 'start' " +
                                    "or valid cron but found '"
                                    + expression + "'");
                        }
                    }
                }
            } else if (triggerDefinition.getAt() != null) {
                throw new SiddhiAppValidationException("Trigger Definition '" + triggerDefinition.getId() + "' "
                        + "must either have trigger time in cron or 'start' or time interval defined, and it cannot "
                        + "have more than one defined as '" + triggerDefinition + "'");
            }
        } else {
            throw new SiddhiAppValidationException("Trigger Definition id cannot be null");
        }
    }

    public static void addEventTrigger(TriggerDefinition triggerDefinition,
                                       ConcurrentMap<String, Trigger> eventTriggerMap,
                                       ConcurrentMap<String, StreamJunction> streamJunctionMap,
                                       SiddhiAppContext siddhiAppContext) {
        if (!eventTriggerMap.containsKey(triggerDefinition.getId())) {
            Trigger trigger;
            if (triggerDefinition.getAtEvery() != null) {
                trigger = new PeriodicTrigger();
            } else if (triggerDefinition.getAt().trim().equalsIgnoreCase(SiddhiConstants.TRIGGER_START)) {
                trigger = new StartTrigger();
            } else {
                trigger = new CronTrigger();
            }
            StreamJunction streamJunction = streamJunctionMap.get(triggerDefinition.getId());
            trigger.init(triggerDefinition, siddhiAppContext, streamJunction);
            siddhiAppContext.addEternalReferencedHolder(trigger);
            eventTriggerMap.putIfAbsent(trigger.getId(), trigger);
        }
    }

    public static void addEventSource(StreamDefinition streamDefinition,
                                      ConcurrentMap<String, List<Source>> eventSourceMap,
                                      SiddhiAppContext siddhiAppContext) {
        for (Annotation sourceAnnotation : streamDefinition.getAnnotations()) {
            if (SiddhiConstants.ANNOTATION_SOURCE.equalsIgnoreCase(sourceAnnotation.getName())) {
                sourceAnnotation = updateAnnotationRef(sourceAnnotation, SiddhiConstants.NAMESPACE_SOURCE,
                        siddhiAppContext);
                Annotation mapAnnotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_MAP,
                        sourceAnnotation.getAnnotations());
                if (mapAnnotation == null) {
                    mapAnnotation = Annotation.annotation(SiddhiConstants.ANNOTATION_MAP).element(SiddhiConstants
                            .ANNOTATION_ELEMENT_TYPE, "passThrough");
                }
                final String sourceType = sourceAnnotation.getElement(SiddhiConstants.ANNOTATION_ELEMENT_TYPE);
                final String mapType = mapAnnotation.getElement(SiddhiConstants.ANNOTATION_ELEMENT_TYPE);
                if (sourceType != null && mapType != null) {
                    SourceHandlerManager sourceHandlerManager = siddhiAppContext.getSiddhiContext().
                            getSourceHandlerManager();
                    SourceHandler sourceHandler = null;
                    if (sourceHandlerManager != null) {
                        sourceHandler = sourceHandlerManager.generateSourceHandler();
                    }
                    // load input transport extension
                    Extension sourceExtension = constructExtension(streamDefinition, SiddhiConstants.ANNOTATION_SOURCE,
                            sourceType, sourceAnnotation, SiddhiConstants.NAMESPACE_SOURCE);
                    Source source = (Source) SiddhiClassLoader.loadExtensionImplementation(sourceExtension,
                            SourceExecutorExtensionHolder.getInstance(siddhiAppContext));
                    ConfigReader configReader = siddhiAppContext.getSiddhiContext().getConfigManager()
                            .generateConfigReader(sourceExtension.getNamespace(), sourceExtension.getName());

                    // load input mapper extension
                    Extension mapperExtension = constructExtension(streamDefinition, SiddhiConstants.ANNOTATION_MAP,
                            mapType, sourceAnnotation, SiddhiConstants.NAMESPACE_SOURCE_MAPPER);
                    SourceMapper sourceMapper = (SourceMapper) SiddhiClassLoader.loadExtensionImplementation(
                            mapperExtension, SourceMapperExecutorExtensionHolder.getInstance(siddhiAppContext));
                    ConfigReader mapperConfigReader = siddhiAppContext.getSiddhiContext().getConfigManager()
                            .generateConfigReader(mapperExtension.getNamespace(), mapperExtension.getName());
                    validateSourceMapperCompatibility(streamDefinition, sourceType, mapType, source, sourceMapper,
                            sourceAnnotation);

                    OptionHolder sourceOptionHolder = constructOptionHolder(streamDefinition, sourceAnnotation,
                            source.getClass().getAnnotation(org.ballerinalang.siddhi.annotation.Extension.class),
                            null);
                    OptionHolder mapOptionHolder = constructOptionHolder(streamDefinition, mapAnnotation,
                            sourceMapper.getClass().getAnnotation(org.ballerinalang.siddhi.annotation.Extension.class),
                            null);

                    AttributesHolder attributesHolder = getAttributeMappings(mapAnnotation, mapType, streamDefinition);
                    String[] transportPropertyNames = getTransportPropertyNames(attributesHolder);
                    try {
                        source.init(sourceType, sourceOptionHolder, sourceMapper, transportPropertyNames,
                                configReader, mapType, mapOptionHolder, attributesHolder.payloadMappings,
                                attributesHolder.transportMappings, mapperConfigReader, sourceHandler, streamDefinition,
                                siddhiAppContext);
                    } catch (Throwable t) {
                        ExceptionUtil.populateQueryContext(t, sourceAnnotation, siddhiAppContext);
                        throw t;
                    }
                    siddhiAppContext.getSnapshotService().addSnapshotable(source.getStreamDefinition().getId(), source);
                    if (sourceHandlerManager != null) {
                        sourceHandlerManager.registerSourceHandler(sourceHandler.getElementId(), sourceHandler);
                        siddhiAppContext.getSnapshotService().addSnapshotable(streamDefinition.getId(), sourceHandler);
                    }
                    List<Source> eventSources = eventSourceMap.get(streamDefinition.getId());
                    if (eventSources == null) {
                        eventSources = new ArrayList<>();
                        eventSources.add(source);
                        eventSourceMap.put(streamDefinition.getId(), eventSources);
                    } else {
                        eventSources.add(source);
                    }
                } else {
                    throw new SiddhiAppCreationException("Both @Sink(type=) and @map(type=) are required.",
                            sourceAnnotation.getQueryContextStartIndex(), sourceAnnotation.getQueryContextEndIndex());
                }
            }
        }
    }

    private static void validateSourceMapperCompatibility(StreamDefinition streamDefinition, String sourceType,
                                                          String mapType, Source source, SourceMapper sourceMapper,
                                                          Annotation sourceAnnotation) {
        Class[] inputEventClasses = sourceMapper.getSupportedInputEventClasses();
        Class[] outputEventClasses = source.getOutputEventClasses();

        //skipping validation for unknown output types
        if (outputEventClasses == null || outputEventClasses.length == 0) {
            return;
        }

        boolean matchingSinkAndMapperClasses = false;
        for (Class inputEventClass : inputEventClasses) {
            for (Class outputEventClass : outputEventClasses) {
                if (inputEventClass.isAssignableFrom(outputEventClass)) {
                    matchingSinkAndMapperClasses = true;
                    break;
                }
            }
            if (matchingSinkAndMapperClasses) {
                break;
            }
        }
        if (!matchingSinkAndMapperClasses) {
            throw new SiddhiAppCreationException("At stream '" + streamDefinition.getId() + "', source '" + sourceType
                    + "' produces incompatible '" + Arrays.deepToString(outputEventClasses) +
                    "' classes, while it's source mapper '" + mapType + "' can only consume '" +
                    Arrays.deepToString(inputEventClasses) + "' classes.",
                    sourceAnnotation.getQueryContextStartIndex(), sourceAnnotation.getQueryContextEndIndex());
        }
    }

    private static String[] getTransportPropertyNames(AttributesHolder attributesHolder) {
        List<String> attributeNames = new ArrayList<>();
        for (AttributeMapping attributeMapping : attributesHolder.transportMappings
                ) {
            attributeNames.add(attributeMapping.getMapping());
        }
        return attributeNames.toArray(new String[0]);
    }

    public static void addEventSink(StreamDefinition streamDefinition,
                                    ConcurrentMap<String, List<Sink>> eventSinkMap,
                                    SiddhiAppContext siddhiAppContext) {
        for (Annotation sinkAnnotation : streamDefinition.getAnnotations()) {
            if (SiddhiConstants.ANNOTATION_SINK.equalsIgnoreCase(sinkAnnotation.getName())) {
                sinkAnnotation = updateAnnotationRef(sinkAnnotation, SiddhiConstants.NAMESPACE_SINK,
                        siddhiAppContext);
                Annotation mapAnnotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_MAP,
                        sinkAnnotation.getAnnotations());
                if (mapAnnotation == null) {
                    mapAnnotation = Annotation.annotation(SiddhiConstants.ANNOTATION_MAP).element(SiddhiConstants
                            .ANNOTATION_ELEMENT_TYPE, "passThrough");
                }
                Annotation distributionAnnotation =
                        AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_DISTRIBUTION,
                                sinkAnnotation.getAnnotations());

                if (mapAnnotation != null) {

                    String[] supportedDynamicOptions = null;
                    List<OptionHolder> destinationOptHolders = new ArrayList<>();
                    String sinkType = sinkAnnotation.getElement(SiddhiConstants.ANNOTATION_ELEMENT_TYPE);
                    Extension sinkExtension = constructExtension(streamDefinition, SiddhiConstants.ANNOTATION_SINK,
                            sinkType, sinkAnnotation, SiddhiConstants.NAMESPACE_SINK);
                    ConfigReader sinkConfigReader = siddhiAppContext.getSiddhiContext().getConfigManager()
                            .generateConfigReader(sinkExtension.getNamespace(), sinkExtension.getName());
                    final boolean isDistributedTransport = (distributionAnnotation != null);
                    boolean isMultiClient = false;
                    if (isDistributedTransport) {
                        Sink sink = createSink(sinkExtension, siddhiAppContext);
                        isMultiClient = isMultiClientDistributedTransport(sink, streamDefinition,
                                distributionAnnotation, siddhiAppContext);
                        supportedDynamicOptions = sink.getSupportedDynamicOptions();
                        destinationOptHolders = createDestinationOptionHolders(distributionAnnotation,
                                streamDefinition, sink, siddhiAppContext);
                    }

                    final String mapType = mapAnnotation.getElement(SiddhiConstants.ANNOTATION_ELEMENT_TYPE);
                    if (mapType != null) {
                        Sink sink;
                        if (isDistributedTransport) {
                            sink = (isMultiClient) ? new MultiClientDistributedSink() :
                                    new SingleClientDistributedSink();
                        } else {
                            sink = createSink(sinkExtension, siddhiAppContext);
                        }
                        if (supportedDynamicOptions == null) {
                            supportedDynamicOptions = sink.getSupportedDynamicOptions();
                        }

                        //load output mapper extension
                        Extension mapperExtension = constructExtension(streamDefinition, SiddhiConstants.ANNOTATION_MAP,
                                mapType, sinkAnnotation, SiddhiConstants.NAMESPACE_SINK_MAPPER);
                        ConfigReader mapperConfigReader = siddhiAppContext.getSiddhiContext().getConfigManager()
                                .generateConfigReader(sinkExtension.getNamespace(), sinkExtension.getName());

                        SinkMapper sinkMapper = (SinkMapper) SiddhiClassLoader.loadExtensionImplementation(
                                mapperExtension, SinkMapperExecutorExtensionHolder.getInstance(siddhiAppContext));

                        org.ballerinalang.siddhi.annotation.Extension sinkExt
                                = sink.getClass().getAnnotation(org.ballerinalang.siddhi.annotation.Extension.class);

                        OptionHolder transportOptionHolder = constructOptionHolder(streamDefinition, sinkAnnotation,
                                sinkExt, supportedDynamicOptions);
                        OptionHolder mapOptionHolder = constructOptionHolder(streamDefinition, mapAnnotation,
                                sinkMapper.getClass().
                                        getAnnotation(org.ballerinalang.siddhi.annotation.Extension.class),
                                sinkMapper.getSupportedDynamicOptions());
                        List<Element> payloadElementList = getPayload(mapAnnotation);

                        OptionHolder distributionOptHolder = null;
                        SinkHandlerManager sinkHandlerManager = siddhiAppContext.getSiddhiContext().
                                getSinkHandlerManager();
                        SinkHandler sinkHandler = null;
                        if (sinkHandlerManager != null) {
                            sinkHandler = sinkHandlerManager.generateSinkHandler();
                        }

                        if (isDistributedTransport) {
                            distributionOptHolder = constructOptionHolder(streamDefinition, distributionAnnotation,
                                    sinkExt, supportedDynamicOptions);
                            String strategyType = distributionOptHolder
                                    .validateAndGetStaticValue(SiddhiConstants.DISTRIBUTION_STRATEGY_KEY);
                            Extension strategyExtension = constructExtension(streamDefinition,
                                    SiddhiConstants.ANNOTATION_SINK, strategyType, sinkAnnotation,
                                    SiddhiConstants.NAMESPACE_DISTRIBUTION_STRATEGY);
                            ConfigReader configReader = siddhiAppContext.getSiddhiContext().getConfigManager()
                                    .generateConfigReader(strategyExtension.getNamespace(),
                                            strategyExtension.getName());
                            DistributionStrategy distributionStrategy = (DistributionStrategy) SiddhiClassLoader
                                    .loadExtensionImplementation(
                                            strategyExtension, DistributionStrategyExtensionHolder.getInstance
                                                    (siddhiAppContext));
                            try {
                                distributionStrategy.init(streamDefinition, transportOptionHolder,
                                        distributionOptHolder, destinationOptHolders, configReader);

                                ((DistributedTransport) sink).init(streamDefinition, sinkType,
                                        transportOptionHolder, sinkConfigReader, sinkMapper,
                                        mapType, mapOptionHolder, sinkHandler,
                                        payloadElementList, mapperConfigReader, siddhiAppContext,
                                        destinationOptHolders,
                                        sinkAnnotation, distributionStrategy,
                                        supportedDynamicOptions);
                            } catch (Throwable t) {
                                ExceptionUtil.populateQueryContext(t, sinkAnnotation, siddhiAppContext);
                                throw t;
                            }
                        } else {
                            try {
                                sink.init(streamDefinition, sinkType, transportOptionHolder, sinkConfigReader,
                                        sinkMapper, mapType, mapOptionHolder, sinkHandler, payloadElementList,
                                        mapperConfigReader, siddhiAppContext);
                            } catch (Throwable t) {
                                ExceptionUtil.populateQueryContext(t, sinkAnnotation, siddhiAppContext);
                                throw t;
                            }
                        }

                        if (sinkHandlerManager != null) {
                            sinkHandlerManager.registerSinkHandler(sinkHandler.getElementId(), sinkHandler);
                            siddhiAppContext.getSnapshotService().addSnapshotable(streamDefinition.getId(),
                                    sinkHandler);
                        }

                        validateSinkMapperCompatibility(streamDefinition, sinkType, mapType, sink, sinkMapper,
                                sinkAnnotation);

                        // Setting the output group determiner
                        OutputGroupDeterminer groupDeterminer = constructOutputGroupDeterminer(transportOptionHolder,
                                distributionOptHolder, streamDefinition, destinationOptHolders.size());
                        if (groupDeterminer != null) {
                            sink.getMapper().setGroupDeterminer(groupDeterminer);
                        }

                        siddhiAppContext.getSnapshotService().addSnapshotable(sink.getStreamDefinition().getId(), sink);

                        List<Sink> eventSinks = eventSinkMap.get(streamDefinition.getId());
                        if (eventSinks == null) {
                            eventSinks = new ArrayList<>();
                            eventSinks.add(sink);
                            eventSinkMap.put(streamDefinition.getId(), eventSinks);
                        } else {
                            eventSinks.add(sink);
                        }
                    }
                } else {
                    throw new SiddhiAppCreationException("Both @sink(type=) and @map(type=) are required.",
                            sinkAnnotation.getQueryContextStartIndex(), sinkAnnotation.getQueryContextEndIndex());
                }
            }
        }
    }

    private static void validateSinkMapperCompatibility(StreamDefinition streamDefinition, String sinkType,
                                                        String mapType, Sink sink, SinkMapper sinkMapper,
                                                        Annotation sinkAnnotation) {
        Class[] inputEventClasses = sink.getSupportedInputEventClasses();
        Class[] outputEventClasses = sinkMapper.getOutputEventClasses();

        //skipping validation for unknown output types
        if (outputEventClasses == null || outputEventClasses.length == 0) {
            return;
        }

        boolean matchingSinkAndMapperClasses = false;
        for (Class inputEventClass : inputEventClasses) {
            for (Class outputEventClass : outputEventClasses) {
                if (inputEventClass.isAssignableFrom(outputEventClass)) {
                    matchingSinkAndMapperClasses = true;
                    break;
                }
            }
            if (matchingSinkAndMapperClasses) {
                break;
            }
        }
        if (!matchingSinkAndMapperClasses) {
            throw new SiddhiAppCreationException("At stream '" + streamDefinition.getId() + "', " +
                    "sink mapper '" + mapType + "' processes '" + Arrays.deepToString(outputEventClasses) +
                    "' classes but it's sink '" + sinkType + "' cannot not consume any of those class, where " +
                    "sink can only consume '" + Arrays.deepToString(inputEventClasses) + "' classes.",
                    sinkAnnotation.getQueryContextStartIndex(), sinkAnnotation.getQueryContextEndIndex());
        }
    }

    private static OutputGroupDeterminer constructOutputGroupDeterminer(OptionHolder transportOptHolder,
                                                                        OptionHolder distributedOptHolder,
                                                                        StreamDefinition streamDef,
                                                                        int destinationCount) {

        OutputGroupDeterminer groupDeterminer = null;
        if (distributedOptHolder != null) {
            String strategy = distributedOptHolder.validateAndGetStaticValue(SiddhiConstants.DISTRIBUTION_STRATEGY_KEY);
            if (strategy.equalsIgnoreCase(SiddhiConstants.DISTRIBUTION_STRATEGY_PARTITIONED)) {
                String partitionKeyField = distributedOptHolder.validateAndGetStaticValue(SiddhiConstants
                        .PARTITION_KEY_FIELD_KEY);
                int partitioningFieldIndex = streamDef.getAttributePosition(partitionKeyField);
                groupDeterminer = new PartitionedGroupDeterminer(partitioningFieldIndex, destinationCount);
            }
        }

        if (groupDeterminer == null) {
            List<Option> dynamicTransportOptions = new ArrayList<Option>(transportOptHolder.getDynamicOptionsKeys()
                    .size());
            for (String option : transportOptHolder.getDynamicOptionsKeys()) {
                dynamicTransportOptions.add(transportOptHolder.validateAndGetOption(option));
            }

            if (dynamicTransportOptions.size() > 0) {
                groupDeterminer = new DynamicOptionGroupDeterminer(dynamicTransportOptions);
            }
        }

        return groupDeterminer;
    }

    public static Extension constructExtension(StreamDefinition streamDefinition, String typeName, String typeValue,
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
            throw new SiddhiAppCreationException("Malformed '" + typeName + "' annotation type '" + typeValue + "' "
                    + "provided, for annotation '" + annotation + "' on stream '" + streamDefinition.getId() + "', "
                    + "it should be either '<namespace>:<name>' or '<name>'",
                    annotation.getQueryContextStartIndex(), annotation.getQueryContextEndIndex());
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

    private static AttributesHolder getAttributeMappings(Annotation mapAnnotation, String mapType,
                                                         StreamDefinition streamDefinition) {
        List<Annotation> attributeAnnotations = mapAnnotation.getAnnotations(SiddhiConstants.ANNOTATION_ATTRIBUTES);
        DefinitionParserHelper.AttributesHolder attributesHolder = new DefinitionParserHelper.AttributesHolder();
        if (attributeAnnotations.size() > 0) {
            Map<String, String> elementMap = new HashMap<>();
            List<String> elementList = new ArrayList<>();
            Boolean attributesNameDefined = null;
            for (Element element : attributeAnnotations.get(0).getElements()) {
                if (element.getKey() == null) {
                    if (attributesNameDefined != null && attributesNameDefined) {
                        throw new SiddhiAppCreationException("Error at '" + mapType + "' defined at stream'" +
                                streamDefinition.getId() + "', some attributes are defined and some are not defined.",
                                element.getQueryContextStartIndex(), element.getQueryContextEndIndex());
                    }
                    attributesNameDefined = false;
                    elementList.add(element.getValue());
                } else {
                    if (attributesNameDefined != null && !attributesNameDefined) {
                        throw new SiddhiAppCreationException("Error at '" + mapType + "' defined at stream '" +
                                streamDefinition.getId() + "', some attributes are defined and some are not defined.",
                                element.getQueryContextStartIndex(), element.getQueryContextEndIndex());
                    }
                    attributesNameDefined = true;
                    elementMap.put(element.getKey(), element.getValue());
                }
            }
            if (elementMap.size() > 0) {
                List<Attribute> attributeList = streamDefinition.getAttributeList();
                for (int i = 0, attributeListSize = attributeList.size(); i < attributeListSize; i++) {
                    Attribute attribute = attributeList.get(i);
                    String value = elementMap.get(attribute.getName());
                    if (value == null) {
                        throw new SiddhiAppCreationException("Error at '" + mapType + "' defined at stream '" +
                                streamDefinition.getId() + "', attribute '" + attribute.getName() + "' is not mapped.",
                                mapAnnotation.getQueryContextStartIndex(), mapAnnotation.getQueryContextEndIndex());
                    }
                    assignMapping(attributesHolder, elementMap, i, attribute);
                }
            } else {
                List<Attribute> attributeList = streamDefinition.getAttributeList();
                if (elementList.size() != attributeList.size()) {
                    throw new SiddhiAppCreationException("Error at '" + mapType + "' defined at stream '" +
                            streamDefinition.getId() + "', '" + elementList.size() + "' mapping attributes are " +
                            "provided but expected attributes are '" + attributeList.size() + "'.",
                            mapAnnotation.getQueryContextStartIndex(), mapAnnotation.getQueryContextEndIndex());
                }
                for (int i = 0; i < attributeList.size(); i++) {
                    Attribute attribute = attributeList.get(i);
                    assignMapping(attributesHolder, elementMap, i, attribute);
                }
            }
        }
        return attributesHolder;
    }

    private static void assignMapping(AttributesHolder attributesHolder, Map<String, String> elementMap, int i,
                                      Attribute attribute) {
        String mapping = elementMap.get(attribute.getName()).trim();
        if (mapping.startsWith("trp:")) {
            attributesHolder.transportMappings.add(new AttributeMapping(attribute.getName(), i, mapping
                    .substring(4)));
        } else {
            attributesHolder.payloadMappings.add(new AttributeMapping(attribute.getName(), i, mapping));
        }
    }

    private static List<Element> getPayload(Annotation mapAnnotation) {
        List<Annotation> attributeAnnotations = mapAnnotation.getAnnotations(SiddhiConstants.ANNOTATION_PAYLOAD);
        if (attributeAnnotations.size() == 1) {
            List<Element> elements = attributeAnnotations.get(0).getElements();
            return elements;
        } else if (attributeAnnotations.size() > 1) {
            throw new SiddhiAppCreationException("@map() annotation should only contain single @payload() " +
                    "annotation.", mapAnnotation.getQueryContextStartIndex(), mapAnnotation.getQueryContextEndIndex());
        } else {
            return null;
        }
    }

    private static OptionHolder constructOptionHolder(StreamDefinition streamDefinition,
                                                      Annotation annotation,
                                                      org.ballerinalang.siddhi.annotation.Extension extension,
                                                      String[] supportedDynamicOptions) {
        List<String> supportedDynamicOptionList = new ArrayList<>();
        if (supportedDynamicOptions != null) {
            supportedDynamicOptionList = Arrays.asList(supportedDynamicOptions);
        }

        Map<String, String> options = new HashMap<String, String>();
        Map<String, String> dynamicOptions = new HashMap<String, String>();
        for (Element element : annotation.getElements()) {
            if (Pattern.matches("(.*?)\\{\\{.*?\\}\\}(.*?)", element.getValue())) {
                if (supportedDynamicOptionList.contains(element.getKey())) {
                    dynamicOptions.put(element.getKey(), element.getValue());
                } else {
                    throw new SiddhiAppCreationException("'" + element.getKey() + "' is not a supported " +
                            "DynamicOption " + "for the Extension '" + extension.namespace() + ":" + extension.name() +
                            "', it only supports following as its DynamicOptions: " + supportedDynamicOptionList,
                            annotation.getQueryContextStartIndex(), annotation.getQueryContextEndIndex());
                }
            } else {
                options.put(element.getKey(), element.getValue());
            }
        }
        return new OptionHolder(streamDefinition, options, dynamicOptions, extension);
    }

    private static boolean isMultiClientDistributedTransport(Sink clientTransport, StreamDefinition
            streamDefinition, Annotation distributionAnnotation, SiddhiAppContext siddhiAppContext) {

        // fetch the @distribution annotations from the @sink annotation
        List<OptionHolder> destinationOptHolders = createDestinationOptionHolders(distributionAnnotation,
                streamDefinition, clientTransport, siddhiAppContext);

        List<String> dynamicOptions = Arrays.asList(clientTransport.getSupportedDynamicOptions());
        // If at least one of the @destination contains a static option then multi client transport should be used
        for (OptionHolder optionHolder : destinationOptHolders) {
            for (String key : optionHolder.getDynamicOptionsKeys()) {
                if (!dynamicOptions.contains(key)) {
                    return true;
                }
            }
            for (String key : optionHolder.getStaticOptionsKeys()) {
                if (!dynamicOptions.contains(key)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static Sink createSink(Extension sinkExtension, SiddhiAppContext siddhiAppContext) {
        // Create a temp instance of the underlying transport to get supported dynamic options
        return (Sink) SiddhiClassLoader.loadExtensionImplementation(sinkExtension,
                SinkExecutorExtensionHolder.getInstance(siddhiAppContext));
    }

    private static List<OptionHolder> createDestinationOptionHolders(Annotation distributionAnnotation,
                                                                     StreamDefinition streamDefinition,
                                                                     Sink clientTransport,
                                                                     SiddhiAppContext siddhiAppContext) {
        org.ballerinalang.siddhi.annotation.Extension sinkExt
                = clientTransport.getClass().getAnnotation(org.ballerinalang.siddhi.annotation.Extension.class);

        List<OptionHolder> destinationOptHolders = new ArrayList<>();
        distributionAnnotation.getAnnotations().stream()
                .filter(annotation -> annotation.getName().equalsIgnoreCase(SiddhiConstants.ANNOTATION_DESTINATION))
                .forEach(destinationAnnotation -> destinationOptHolders.add(constructOptionHolder(streamDefinition,
                        updateAnnotationRef(destinationAnnotation, SiddhiConstants.ANNOTATION_DESTINATION,
                                siddhiAppContext), sinkExt, clientTransport.getSupportedDynamicOptions())));
        return destinationOptHolders;
    }

    private static Annotation updateAnnotationRef(Annotation annotation, String type,
                                                  SiddhiAppContext siddhiAppContext) {
        String ref = annotation.getElement(SiddhiConstants.ANNOTATION_ELEMENT_REF);
        if (ref != null) {
            Map<String, String> systemConfigs = siddhiAppContext.getSiddhiContext().getConfigManager()
                    .extractSystemConfigs(ref);

            if (systemConfigs.size() == 0) {
                throw new SiddhiAppCreationException("The " + type + " element of the name '" + ref +
                        "' is not defined in the configurations file.",
                        annotation.getQueryContextStartIndex(),
                        annotation.getQueryContextEndIndex());
            } else {
                HashMap<String, String> newSystemConfig = new HashMap<>(systemConfigs);

                Map<String, String> collection = annotation.getElements().stream()
                        .collect(Collectors.toMap(Element::getKey, Element::getValue));
                collection.remove(SiddhiConstants.ANNOTATION_ELEMENT_REF);
                newSystemConfig.putAll(collection);

                List<Element> annotationElements = newSystemConfig.entrySet().stream()
                        .map((property) -> new Element(
                                property.getKey(),
                                property.getValue()))
                        .collect(Collectors.toList());

                annotation.setElements(annotationElements);
            }
        }
        return annotation;
    }

    /**
     * Holder to collect attributes mapping.
     */
    static class AttributesHolder {
        List<AttributeMapping> transportMappings = new ArrayList<>();
        List<AttributeMapping> payloadMappings = new ArrayList<>();

    }

}
