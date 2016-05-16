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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.partition.PartitionRuntime;
import org.wso2.siddhi.core.query.QueryRuntime;
import org.wso2.siddhi.core.util.ElementIdGenerator;
import org.wso2.siddhi.core.util.ExecutionPlanRuntimeBuilder;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.ThreadBarrier;
import org.wso2.siddhi.core.util.persistence.PersistenceService;
import org.wso2.siddhi.core.util.snapshot.SnapshotService;
import org.wso2.siddhi.core.util.timestamp.SystemCurrentTimeMillisTimestampGenerator;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.FunctionDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.definition.TriggerDefinition;
import org.wso2.siddhi.query.api.exception.DuplicateAnnotationException;
import org.wso2.siddhi.query.api.exception.DuplicateDefinitionException;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.execution.ExecutionElement;
import org.wso2.siddhi.query.api.execution.partition.Partition;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class ExecutionPlanParser {
    private static final Logger log = Logger.getLogger(ExecutionPlanRuntimeBuilder.class);

    /**
     * Parse an ExecutionPlan returning ExecutionPlanRuntime
     *
     * @param executionPlan plan to be parsed
     * @param siddhiContext SiddhiContext
     * @return ExecutionPlanRuntime
     */
    public static ExecutionPlanRuntimeBuilder parse(ExecutionPlan executionPlan, SiddhiContext siddhiContext) {

        ExecutionPlanContext executionPlanContext = new ExecutionPlanContext();
        executionPlanContext.setSiddhiContext(siddhiContext);

        try {
            Element element = AnnotationHelper.getAnnotationElement(SiddhiConstants.ANNOTATION_NAME, null,
                    executionPlan.getAnnotations());
            if (element != null) {
                executionPlanContext.setName(element.getValue());
            } else {
                executionPlanContext.setName(UUID.randomUUID().toString());
            }

            Annotation annotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_PLAYBACK,
                    executionPlan.getAnnotations());
            if (annotation != null) {
                executionPlanContext.setPlayback(true);
            }

            annotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_ENFORCE_ORDER,
                    executionPlan.getAnnotations());
            if (annotation != null) {
                executionPlanContext.setEnforceOrder(true);
            }

            annotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_ASYNC,
                    executionPlan.getAnnotations());
            if (annotation != null) {
                executionPlanContext.setAsync(true);
                String bufferSizeString = annotation.getElement(SiddhiConstants.ANNOTATION_BUFFER_SIZE);
                if (bufferSizeString != null) {
                    int bufferSize = Integer.parseInt(bufferSizeString);
                    executionPlanContext.setBufferSize(bufferSize);
                } else {
                    executionPlanContext.setBufferSize(SiddhiConstants.DEFAULT_EVENT_BUFFER_SIZE);
                }
            }

            annotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_STATISTICS,
                    executionPlan.getAnnotations());

            Element statElement = AnnotationHelper.getAnnotationElement(SiddhiConstants.ANNOTATION_STATISTICS, null,
                    executionPlan.getAnnotations());

            // Both annotation and statElement should be checked since siddhi uses
            // @plan:statistics(reporter = 'console', interval = '5' )
            // where cep uses @plan:statistics('true').
            if (annotation != null && (statElement == null || Boolean.valueOf(statElement.getValue()))) {
                if (siddhiContext.getStatisticsConfiguration() != null) {
                    executionPlanContext.setStatsEnabled(true);
                    executionPlanContext.setStatisticsManager(siddhiContext
                            .getStatisticsConfiguration()
                            .getFactory()
                            .createStatisticsManager(annotation.getElements()));
                }
            }

            executionPlanContext.setThreadBarrier(new ThreadBarrier());

            executionPlanContext.setExecutorService(Executors.newCachedThreadPool(
                    new ThreadFactoryBuilder().setNameFormat("Siddhi-" + executionPlanContext.getName() +
                            "-executor-thread-%d").build()));

            executionPlanContext.setScheduledExecutorService(Executors.newScheduledThreadPool(5,
                    new ThreadFactoryBuilder().setNameFormat("Siddhi-" +
                            executionPlanContext.getName() + "-scheduler-thread-%d").build()));
            executionPlanContext.setTimestampGenerator(new SystemCurrentTimeMillisTimestampGenerator());
            executionPlanContext.setSnapshotService(new SnapshotService(executionPlanContext));
            executionPlanContext.setPersistenceService(new PersistenceService(executionPlanContext));
            executionPlanContext.setElementIdGenerator(new ElementIdGenerator(executionPlanContext.getName()));

        } catch (DuplicateAnnotationException e) {
            throw new DuplicateAnnotationException(e.getMessage() + " for the same Execution Plan " +
                    executionPlan.toString());
        }

        ExecutionPlanRuntimeBuilder executionPlanRuntimeBuilder = new ExecutionPlanRuntimeBuilder(executionPlanContext);

        defineStreamDefinitions(executionPlanRuntimeBuilder, executionPlan.getStreamDefinitionMap());
        defineTableDefinitions(executionPlanRuntimeBuilder, executionPlan.getTableDefinitionMap());
        defineFunctionDefinitions(executionPlanRuntimeBuilder, executionPlan.getFunctionDefinitionMap());
        try {
            for (ExecutionElement executionElement : executionPlan.getExecutionElementList()) {
                if (executionElement instanceof Query) {
                    QueryRuntime queryRuntime = QueryParser.parse((Query) executionElement, executionPlanContext,
                            executionPlanRuntimeBuilder.getStreamDefinitionMap(),
                            executionPlanRuntimeBuilder.getTableDefinitionMap(),
                            executionPlanRuntimeBuilder.getEventTableMap());
                    executionPlanRuntimeBuilder.addQuery(queryRuntime);
                } else {
                    PartitionRuntime partitionRuntime = PartitionParser.parse(executionPlanRuntimeBuilder,
                            (Partition) executionElement, executionPlanContext,
                            executionPlanRuntimeBuilder.getStreamDefinitionMap());
                    executionPlanRuntimeBuilder.addPartition(partitionRuntime);
                }
            }
        } catch (ExecutionPlanCreationException e) {
            throw new ExecutionPlanValidationException(e.getMessage() + " in execution plan \"" +
                    executionPlanContext.getName() + "\"", e);
        } catch (DuplicateDefinitionException e) {
            throw new DuplicateDefinitionException(e.getMessage() + " in execution plan \"" +
                    executionPlanContext.getName() + "\"", e);
        }

        //Done last as they have to be started last
        defineTriggerDefinitions(executionPlanRuntimeBuilder, executionPlan.getTriggerDefinitionMap());
        return executionPlanRuntimeBuilder;
    }

    private static void defineTriggerDefinitions(ExecutionPlanRuntimeBuilder executionPlanRuntimeBuilder,
                                                 Map<String, TriggerDefinition> triggerDefinitionMap) {
        for (TriggerDefinition definition : triggerDefinitionMap.values()) {
            executionPlanRuntimeBuilder.defineTrigger(definition);
        }
    }

    private static void defineFunctionDefinitions(ExecutionPlanRuntimeBuilder executionPlanRuntimeBuilder,
                                                  Map<String, FunctionDefinition> functionDefinitionMap) {
        for (FunctionDefinition definition : functionDefinitionMap.values()) {
            executionPlanRuntimeBuilder.defineFunction(definition);
        }
    }

    private static void defineStreamDefinitions(ExecutionPlanRuntimeBuilder executionPlanRuntimeBuilder,
                                                Map<String, StreamDefinition> streamDefinitionMap) {
        for (StreamDefinition definition : streamDefinitionMap.values()) {
            executionPlanRuntimeBuilder.defineStream(definition);
        }
    }

    private static void defineTableDefinitions(ExecutionPlanRuntimeBuilder executionPlanRuntimeBuilder,
                                               Map<String, TableDefinition> tableDefinitionMap) {
        for (TableDefinition definition : tableDefinitionMap.values()) {
            executionPlanRuntimeBuilder.defineTable(definition);
        }
    }
}
