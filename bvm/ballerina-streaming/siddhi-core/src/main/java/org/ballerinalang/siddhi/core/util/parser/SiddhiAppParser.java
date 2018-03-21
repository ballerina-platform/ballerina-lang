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
package org.ballerinalang.siddhi.core.util.parser;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.config.SiddhiContext;
import org.ballerinalang.siddhi.core.exception.SiddhiAppCreationException;
import org.ballerinalang.siddhi.core.partition.PartitionRuntime;
import org.ballerinalang.siddhi.core.query.QueryRuntime;
import org.ballerinalang.siddhi.core.util.ElementIdGenerator;
import org.ballerinalang.siddhi.core.util.ExceptionUtil;
import org.ballerinalang.siddhi.core.util.SiddhiAppRuntimeBuilder;
import org.ballerinalang.siddhi.core.util.SiddhiConstants;
import org.ballerinalang.siddhi.core.util.ThreadBarrier;
import org.ballerinalang.siddhi.core.util.persistence.PersistenceService;
import org.ballerinalang.siddhi.core.util.snapshot.SnapshotService;
import org.ballerinalang.siddhi.core.util.timestamp.EventTimeBasedMillisTimestampGenerator;
import org.ballerinalang.siddhi.core.util.timestamp.SystemCurrentTimeMillisTimestampGenerator;
import org.ballerinalang.siddhi.core.window.Window;
import org.ballerinalang.siddhi.query.api.SiddhiApp;
import org.ballerinalang.siddhi.query.api.annotation.Annotation;
import org.ballerinalang.siddhi.query.api.annotation.Element;
import org.ballerinalang.siddhi.query.api.definition.AggregationDefinition;
import org.ballerinalang.siddhi.query.api.definition.FunctionDefinition;
import org.ballerinalang.siddhi.query.api.definition.StreamDefinition;
import org.ballerinalang.siddhi.query.api.definition.TableDefinition;
import org.ballerinalang.siddhi.query.api.definition.TriggerDefinition;
import org.ballerinalang.siddhi.query.api.definition.WindowDefinition;
import org.ballerinalang.siddhi.query.api.exception.DuplicateAnnotationException;
import org.ballerinalang.siddhi.query.api.exception.SiddhiAppValidationException;
import org.ballerinalang.siddhi.query.api.execution.ExecutionElement;
import org.ballerinalang.siddhi.query.api.execution.partition.Partition;
import org.ballerinalang.siddhi.query.api.execution.query.Query;
import org.ballerinalang.siddhi.query.api.util.AnnotationHelper;
import org.ballerinalang.siddhi.query.compiler.SiddhiCompiler;
import org.ballerinalang.siddhi.query.compiler.exception.SiddhiParserException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

import static org.ballerinalang.siddhi.core.util.parser.helper.AnnotationHelper.generateIncludedMetrics;

/**
 * Class to parse {@link SiddhiApp}.
 */
public class SiddhiAppParser {

    /**
     * Parse an SiddhiApp returning SiddhiAppRuntime.
     *
     * @param siddhiApp       plan to be parsed
     * @param siddhiAppString content of Siddhi application as string
     * @param siddhiContext   SiddhiContext  @return SiddhiAppRuntime
     * @return SiddhiAppRuntimeBuilder
     */
    public static SiddhiAppRuntimeBuilder parse(SiddhiApp siddhiApp, String siddhiAppString,
                                                SiddhiContext siddhiContext) {

        SiddhiAppContext siddhiAppContext = new SiddhiAppContext();
        siddhiAppContext.setSiddhiContext(siddhiContext);
        siddhiAppContext.setSiddhiAppString(siddhiAppString);

        try {
            Element element = AnnotationHelper.getAnnotationElement(SiddhiConstants.ANNOTATION_NAME, null,
                    siddhiApp.getAnnotations());
            if (element != null) {
                siddhiAppContext.setName(element.getValue());
            } else {
                siddhiAppContext.setName(UUID.randomUUID().toString());
            }

            Annotation annotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_ENFORCE_ORDER,
                    siddhiApp.getAnnotations());
            if (annotation != null) {
                siddhiAppContext.setEnforceOrder(true);
            }

            annotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_ASYNC,
                    siddhiApp.getAnnotations());
            if (annotation != null) {
                throw new SiddhiAppCreationException("@Async not supported in SiddhiApp level, " +
                        "instead use @Async with streams",
                        annotation.getQueryContextStartIndex(), annotation.getQueryContextEndIndex());
            }

            annotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_STATISTICS,
                    siddhiApp.getAnnotations());

            List<Element> statisticsElements = new ArrayList<>();
            if (annotation != null) {
                statisticsElements = annotation.getElements();
            }
            if (siddhiContext.getStatisticsConfiguration() != null) {
                siddhiAppContext.setStatisticsManager(siddhiContext
                        .getStatisticsConfiguration()
                        .getFactory()
                        .createStatisticsManager(
                                siddhiContext.getStatisticsConfiguration().getMetricPrefix(),
                                siddhiAppContext.getName(),
                                statisticsElements));
            }

            Element statStateEnableElement = AnnotationHelper.getAnnotationElement(
                    SiddhiConstants.ANNOTATION_STATISTICS,
                    SiddhiConstants.ANNOTATION_ELEMENT_ENABLE, siddhiApp.getAnnotations());

            if (statStateEnableElement != null && Boolean.valueOf(statStateEnableElement.getValue())) {
                siddhiAppContext.setStatsEnabled(true);
            } else {
                Element statStateElement = AnnotationHelper.getAnnotationElement(
                        SiddhiConstants.ANNOTATION_STATISTICS, null, siddhiApp.getAnnotations());
                // Both annotation and statElement should be checked since siddhi uses
                // @app:statistics(reporter = 'console', interval = '5' )
                // where sp uses @app:statistics('true').
                if (annotation != null && (statStateElement == null || Boolean.valueOf(statStateElement.getValue()))) {
                    siddhiAppContext.setStatsEnabled(true);
                }
            }
            Element statStateIncludElement = AnnotationHelper.getAnnotationElement(
                    SiddhiConstants.ANNOTATION_STATISTICS,
                    SiddhiConstants.ANNOTATION_ELEMENT_INCLUDE, siddhiApp.getAnnotations());
            siddhiAppContext.setIncludedMetrics(generateIncludedMetrics(statStateIncludElement));


            siddhiAppContext.setThreadBarrier(new ThreadBarrier());

            siddhiAppContext.setExecutorService(Executors.newCachedThreadPool(
                    new ThreadFactoryBuilder().setNameFormat("Siddhi-" + siddhiAppContext.getName() +
                            "-executor-thread-%d").build()));

            siddhiAppContext.setScheduledExecutorService(Executors.newScheduledThreadPool(5,
                    new ThreadFactoryBuilder().setNameFormat("Siddhi-" +
                            siddhiAppContext.getName() + "-scheduler-thread-%d").build()));

            // Select the TimestampGenerator based on playback mode on/off
            annotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_PLAYBACK,
                    siddhiApp.getAnnotations());
            if (annotation != null) {
                String idleTime = null;
                String increment = null;
                EventTimeBasedMillisTimestampGenerator timestampGenerator = new
                        EventTimeBasedMillisTimestampGenerator(siddhiAppContext.getScheduledExecutorService());
                // Get the optional elements of playback annotation
                for (Element e : annotation.getElements()) {
                    if (SiddhiConstants.ANNOTATION_ELEMENT_IDLE_TIME.equalsIgnoreCase(e.getKey())) {
                        idleTime = e.getValue();
                    } else if (SiddhiConstants.ANNOTATION_ELEMENT_INCREMENT.equalsIgnoreCase(e.getKey())) {
                        increment = e.getValue();
                    } else {
                        throw new SiddhiAppValidationException("Playback annotation accepts only idle.time and " +
                                "increment but found " + e.getKey());
                    }
                }

                // idleTime and increment are optional but if one presents, the other also should be given
                if (idleTime != null && increment == null) {
                    throw new SiddhiAppValidationException("Playback annotation requires both idle.time and " +
                            "increment but increment not found");
                } else if (idleTime == null && increment != null) {
                    throw new SiddhiAppValidationException("Playback annotation requires both idle.time and " +
                            "increment but idle.time does not found");
                } else if (idleTime != null) {
                    // The fourth case idleTime == null && increment == null are ignored because it means no heartbeat.
                    try {
                        timestampGenerator.setIdleTime(SiddhiCompiler.parseTimeConstantDefinition(idleTime).value());
                    } catch (SiddhiParserException ex) {
                        throw new SiddhiParserException("Invalid idle.time constant '" + idleTime + "' in playback " +
                                "annotation", ex);
                    }
                    try {
                        timestampGenerator.setIncrementInMilliseconds(SiddhiCompiler.parseTimeConstantDefinition
                                (increment).value());
                    } catch (SiddhiParserException ex) {
                        throw new SiddhiParserException("Invalid increment constant '" + increment + "' in playback " +
                                "annotation", ex);
                    }
                }

                siddhiAppContext.setTimestampGenerator(timestampGenerator);
                siddhiAppContext.setPlayback(true);
            } else {
                siddhiAppContext.setTimestampGenerator(new SystemCurrentTimeMillisTimestampGenerator());
            }
            siddhiAppContext.setSnapshotService(new SnapshotService(siddhiAppContext));
            siddhiAppContext.setPersistenceService(new PersistenceService(siddhiAppContext));
            siddhiAppContext.setElementIdGenerator(new ElementIdGenerator(siddhiAppContext.getName()));

        } catch (DuplicateAnnotationException e) {
            throw new DuplicateAnnotationException(e.getMessageWithOutContext() + " for the same Siddhi app " +
                    siddhiApp.toString(), e, e.getQueryContextStartIndex(), e.getQueryContextEndIndex(),
                    siddhiAppContext.getName(), siddhiAppContext.getSiddhiAppString());
        }

        SiddhiAppRuntimeBuilder siddhiAppRuntimeBuilder = new SiddhiAppRuntimeBuilder(siddhiAppContext);

        defineStreamDefinitions(siddhiAppRuntimeBuilder, siddhiApp.getStreamDefinitionMap(), siddhiAppContext);
        defineTableDefinitions(siddhiAppRuntimeBuilder, siddhiApp.getTableDefinitionMap(), siddhiAppContext);
        defineWindowDefinitions(siddhiAppRuntimeBuilder, siddhiApp.getWindowDefinitionMap(), siddhiAppContext);
        defineFunctionDefinitions(siddhiAppRuntimeBuilder, siddhiApp.getFunctionDefinitionMap(), siddhiAppContext);
        defineAggregationDefinitions(siddhiAppRuntimeBuilder, siddhiApp.getAggregationDefinitionMap(),
                siddhiAppContext);
        for (Window window : siddhiAppRuntimeBuilder.getWindowMap().values()) {
            try {
                window.init(siddhiAppRuntimeBuilder.getTableMap(), siddhiAppRuntimeBuilder
                        .getWindowMap(), window.getWindowDefinition().getId());
            } catch (Throwable t) {
                ExceptionUtil.populateQueryContext(t, window.getWindowDefinition(), siddhiAppContext);
                throw t;
            }
        }
        int queryIndex = 1;
        for (ExecutionElement executionElement : siddhiApp.getExecutionElementList()) {
            if (executionElement instanceof Query) {
                try {
                    QueryRuntime queryRuntime = QueryParser.parse((Query) executionElement, siddhiAppContext,
                            siddhiAppRuntimeBuilder.getStreamDefinitionMap(),
                            siddhiAppRuntimeBuilder.getTableDefinitionMap(),
                            siddhiAppRuntimeBuilder.getWindowDefinitionMap(),
                            siddhiAppRuntimeBuilder.getAggregationDefinitionMap(),
                            siddhiAppRuntimeBuilder.getTableMap(),
                            siddhiAppRuntimeBuilder.getAggregationMap(),
                            siddhiAppRuntimeBuilder.getWindowMap(),
                            siddhiAppRuntimeBuilder.getLockSynchronizer(),
                            String.valueOf(queryIndex));
                    siddhiAppRuntimeBuilder.addQuery(queryRuntime);
                    queryIndex++;
                } catch (Throwable t) {
                    ExceptionUtil.populateQueryContext(t, (Query) executionElement, siddhiAppContext);
                    throw t;
                }
            } else {
                try {
                    PartitionRuntime partitionRuntime = PartitionParser.parse(siddhiAppRuntimeBuilder,
                            (Partition) executionElement, siddhiAppContext,
                            siddhiAppRuntimeBuilder.getStreamDefinitionMap(), queryIndex);
                    siddhiAppRuntimeBuilder.addPartition(partitionRuntime);
                    siddhiAppContext.getSnapshotService().addSnapshotable("partition", partitionRuntime);
                    queryIndex += ((Partition) executionElement).getQueryList().size();
                } catch (Throwable t) {
                    ExceptionUtil.populateQueryContext(t, (Partition) executionElement, siddhiAppContext);
                    throw t;
                }
            }
        }
        //Done last as they have to be started last
        defineTriggerDefinitions(siddhiAppRuntimeBuilder, siddhiApp.getTriggerDefinitionMap(), siddhiAppContext);
        return siddhiAppRuntimeBuilder;
    }

    private static void defineTriggerDefinitions(SiddhiAppRuntimeBuilder siddhiAppRuntimeBuilder,
                                                 Map<String, TriggerDefinition> triggerDefinitionMap,
                                                 SiddhiAppContext siddhiAppContext) {
        for (TriggerDefinition definition : triggerDefinitionMap.values()) {
            try {
                siddhiAppRuntimeBuilder.defineTrigger(definition);
            } catch (Throwable t) {
                ExceptionUtil.populateQueryContext(t, definition, siddhiAppContext);
                throw t;
            }
        }
    }

    private static void defineFunctionDefinitions(SiddhiAppRuntimeBuilder siddhiAppRuntimeBuilder,
                                                  Map<String, FunctionDefinition> functionDefinitionMap,
                                                  SiddhiAppContext siddhiAppContext) {
        for (FunctionDefinition definition : functionDefinitionMap.values()) {
            try {
                siddhiAppRuntimeBuilder.defineFunction(definition);
            } catch (Throwable t) {
                ExceptionUtil.populateQueryContext(t, definition, siddhiAppContext);
                throw t;
            }
        }
    }

    private static void defineStreamDefinitions(SiddhiAppRuntimeBuilder siddhiAppRuntimeBuilder,
                                                Map<String, StreamDefinition> streamDefinitionMap,
                                                SiddhiAppContext siddhiAppContext) {
        for (StreamDefinition definition : streamDefinitionMap.values()) {
            try {
                siddhiAppRuntimeBuilder.defineStream(definition);
            } catch (Throwable t) {
                ExceptionUtil.populateQueryContext(t, definition, siddhiAppContext);
                throw t;
            }
        }
    }

    private static void defineTableDefinitions(SiddhiAppRuntimeBuilder siddhiAppRuntimeBuilder,
                                               Map<String, TableDefinition> tableDefinitionMap,
                                               SiddhiAppContext siddhiAppContext) {
        for (TableDefinition definition : tableDefinitionMap.values()) {
            try {
                siddhiAppRuntimeBuilder.defineTable(definition);
            } catch (Throwable t) {
                ExceptionUtil.populateQueryContext(t, definition, siddhiAppContext);
                throw t;
            }
        }
    }

    private static void defineWindowDefinitions(SiddhiAppRuntimeBuilder siddhiAppRuntimeBuilder,
                                                Map<String, WindowDefinition> windowDefinitionMap,
                                                SiddhiAppContext siddhiAppContext) {
        for (WindowDefinition definition : windowDefinitionMap.values()) {
            try {
                siddhiAppRuntimeBuilder.defineWindow(definition);
            } catch (Throwable t) {
                ExceptionUtil.populateQueryContext(t, definition, siddhiAppContext);
                throw t;
            }
        }
    }

    private static void defineAggregationDefinitions(SiddhiAppRuntimeBuilder siddhiAppRuntimeBuilder,
                                                     Map<String, AggregationDefinition> aggregationDefinitionMap,
                                                     SiddhiAppContext siddhiAppContext) {
        for (AggregationDefinition definition : aggregationDefinitionMap.values()) {
            try {
                siddhiAppRuntimeBuilder.defineAggregation(definition);
            } catch (Throwable t) {
                ExceptionUtil.populateQueryContext(t, definition, siddhiAppContext);
                throw t;
            }
        }

    }
}
