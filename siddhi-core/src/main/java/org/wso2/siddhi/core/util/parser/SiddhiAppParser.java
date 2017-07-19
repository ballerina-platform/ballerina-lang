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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.partition.PartitionRuntime;
import org.wso2.siddhi.core.query.QueryRuntime;
import org.wso2.siddhi.core.util.ElementIdGenerator;
import org.wso2.siddhi.core.util.SiddhiAppRuntimeBuilder;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.ThreadBarrier;
import org.wso2.siddhi.core.util.persistence.PersistenceService;
import org.wso2.siddhi.core.util.snapshot.SnapshotService;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;
import org.wso2.siddhi.core.util.timestamp.EventTimeBasedMillisTimestampGenerator;
import org.wso2.siddhi.core.util.timestamp.SystemCurrentTimeMillisTimestampGenerator;
import org.wso2.siddhi.core.window.Window;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;
import org.wso2.siddhi.query.api.definition.FunctionDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.definition.TriggerDefinition;
import org.wso2.siddhi.query.api.definition.WindowDefinition;
import org.wso2.siddhi.query.api.exception.DuplicateAnnotationException;
import org.wso2.siddhi.query.api.exception.DuplicateDefinitionException;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;
import org.wso2.siddhi.query.api.execution.ExecutionElement;
import org.wso2.siddhi.query.api.execution.partition.Partition;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.util.AnnotationHelper;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * Class to parse {@link SiddhiApp}
 */
public class SiddhiAppParser {
    private static final Logger log = Logger.getLogger(SiddhiAppParser.class);

    /**
     * Parse an SiddhiApp returning SiddhiAppRuntime
     *
     * @param siddhiApp     plan to be parsed
     * @param siddhiContext SiddhiContext
     * @return SiddhiAppRuntime
     */
    public static SiddhiAppRuntimeBuilder parse(SiddhiApp siddhiApp, SiddhiContext siddhiContext) {

        SiddhiAppContext siddhiAppContext = new SiddhiAppContext();
        siddhiAppContext.setSiddhiContext(siddhiContext);

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
                siddhiAppContext.setAsync(true);
                String bufferSizeString = annotation.getElement(SiddhiConstants.ANNOTATION_ELEMENT_BUFFER_SIZE);
                if (bufferSizeString != null) {
                    int bufferSize = Integer.parseInt(bufferSizeString);
                    siddhiAppContext.setBufferSize(bufferSize);
                } else {
                    siddhiAppContext.setBufferSize(SiddhiConstants.DEFAULT_EVENT_BUFFER_SIZE);
                }
            }

            annotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_STATISTICS,
                    siddhiApp.getAnnotations());

            Element statElement = AnnotationHelper.getAnnotationElement(SiddhiConstants.ANNOTATION_STATISTICS, null,
                    siddhiApp.getAnnotations());

            // Both annotation and statElement should be checked since siddhi uses
            // @app:statistics(reporter = 'console', interval = '5' )
            // where cep uses @app:statistics('true').
            if (annotation != null && (statElement == null || Boolean.valueOf(statElement.getValue()))) {
                if (siddhiContext.getStatisticsConfiguration() != null) {
                    siddhiAppContext.setStatsEnabled(true);
                    siddhiAppContext.setStatisticsManager(siddhiContext
                            .getStatisticsConfiguration()
                            .getFactory()
                            .createStatisticsManager(annotation.getElements()));
                }
            }

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
            throw new DuplicateAnnotationException(e.getMessage() + " for the same Siddhi app " +
                    siddhiApp.toString());
        }

        SiddhiAppRuntimeBuilder siddhiAppRuntimeBuilder = new SiddhiAppRuntimeBuilder(siddhiAppContext);

        defineStreamDefinitions(siddhiAppRuntimeBuilder, siddhiApp.getStreamDefinitionMap());
        defineTableDefinitions(siddhiAppRuntimeBuilder, siddhiApp.getTableDefinitionMap());
        defineWindowDefinitions(siddhiAppRuntimeBuilder, siddhiApp.getWindowDefinitionMap());
        defineFunctionDefinitions(siddhiAppRuntimeBuilder, siddhiApp.getFunctionDefinitionMap());
        defineAggregationDefinitions(siddhiAppRuntimeBuilder, siddhiApp.getAggregationDefinitionMap());
        for (Window window : siddhiAppRuntimeBuilder.getWindowMap().values()) {
            String metricName =
                    siddhiAppContext.getSiddhiContext().getStatisticsConfiguration().getMatricPrefix() +
                            SiddhiConstants.METRIC_DELIMITER + SiddhiConstants.METRIC_INFIX_EXECUTION_PLANS +
                            SiddhiConstants.METRIC_DELIMITER + siddhiAppContext.getName() +
                            SiddhiConstants.METRIC_DELIMITER + SiddhiConstants.METRIC_INFIX_SIDDHI +
                            SiddhiConstants.METRIC_DELIMITER + SiddhiConstants.METRIC_INFIX_WINDOWS +
                            SiddhiConstants.METRIC_DELIMITER + window.getWindowDefinition().getId();
            LatencyTracker latencyTracker = null;
            if (siddhiAppContext.isStatsEnabled() && siddhiAppContext.getStatisticsManager() != null) {
                latencyTracker = siddhiAppContext.getSiddhiContext()
                        .getStatisticsConfiguration()
                        .getFactory()
                        .createLatencyTracker(metricName, siddhiAppContext.getStatisticsManager());
            }
            window.init(siddhiAppRuntimeBuilder.getTableMap(), siddhiAppRuntimeBuilder
                    .getWindowMap(), latencyTracker, window.getWindowDefinition().getId());
        }
        try {
            int queryIndex = 1;
            for (ExecutionElement executionElement : siddhiApp.getExecutionElementList()) {
                if (executionElement instanceof Query) {
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
                } else {
                    PartitionRuntime partitionRuntime = PartitionParser.parse(siddhiAppRuntimeBuilder,
                            (Partition) executionElement, siddhiAppContext,
                            siddhiAppRuntimeBuilder.getStreamDefinitionMap(), queryIndex);
                    siddhiAppRuntimeBuilder.addPartition(partitionRuntime);
                    queryIndex += ((Partition) executionElement).getQueryList().size();
                }

            }
        } catch (SiddhiAppCreationException e) {
            throw new SiddhiAppValidationException(e.getMessage() + " in siddhi app \"" +
                    siddhiAppContext.getName() + "\"", e);
        } catch (DuplicateDefinitionException e) {
            throw new DuplicateDefinitionException(e.getMessage() + " in siddhi app \"" +
                    siddhiAppContext.getName() + "\"", e);
        }

        //Done last as they have to be started last
        defineTriggerDefinitions(siddhiAppRuntimeBuilder, siddhiApp.getTriggerDefinitionMap());
        return siddhiAppRuntimeBuilder;
    }

    private static void defineTriggerDefinitions(SiddhiAppRuntimeBuilder siddhiAppRuntimeBuilder,
                                                 Map<String, TriggerDefinition> triggerDefinitionMap) {
        for (TriggerDefinition definition : triggerDefinitionMap.values()) {
            siddhiAppRuntimeBuilder.defineTrigger(definition);
        }
    }

    private static void defineFunctionDefinitions(SiddhiAppRuntimeBuilder siddhiAppRuntimeBuilder,
                                                  Map<String, FunctionDefinition> functionDefinitionMap) {
        for (FunctionDefinition definition : functionDefinitionMap.values()) {
            siddhiAppRuntimeBuilder.defineFunction(definition);
        }
    }

    private static void defineStreamDefinitions(SiddhiAppRuntimeBuilder siddhiAppRuntimeBuilder,
                                                Map<String, StreamDefinition> streamDefinitionMap) {
        for (StreamDefinition definition : streamDefinitionMap.values()) {
            siddhiAppRuntimeBuilder.defineStream(definition);
        }
    }

    private static void defineTableDefinitions(SiddhiAppRuntimeBuilder siddhiAppRuntimeBuilder,
                                               Map<String, TableDefinition> tableDefinitionMap) {
        for (TableDefinition definition : tableDefinitionMap.values()) {
            siddhiAppRuntimeBuilder.defineTable(definition);
        }
    }

    private static void defineWindowDefinitions(SiddhiAppRuntimeBuilder siddhiAppRuntimeBuilder,
                                                Map<String, WindowDefinition> windowDefinitionMap) {
        for (WindowDefinition definition : windowDefinitionMap.values()) {
            siddhiAppRuntimeBuilder.defineWindow(definition);
        }
    }

    private static void defineAggregationDefinitions(SiddhiAppRuntimeBuilder siddhiAppRuntimeBuilder,
                                                     Map<String, AggregationDefinition> aggregationDefinitionMap) {
        for (AggregationDefinition definition : aggregationDefinitionMap.values()) {
            siddhiAppRuntimeBuilder.defineAggregation(definition);
        }

    }
}
