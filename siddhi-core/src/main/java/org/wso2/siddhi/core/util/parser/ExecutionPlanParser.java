/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.partition.PartitionRuntime;
import org.wso2.siddhi.core.query.QueryRuntime;
import org.wso2.siddhi.core.util.ElementIdGenerator;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.persistence.PersistenceService;
import org.wso2.siddhi.core.util.snapshot.SnapshotService;
import org.wso2.siddhi.core.util.timestamp.SystemCurrentTimeMillisTimestampGenerator;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.exception.DuplicateAnnotationException;
import org.wso2.siddhi.query.api.exception.DuplicateDefinitionException;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.execution.ExecutionElement;
import org.wso2.siddhi.query.api.execution.partition.Partition;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ExecutionPlanParser {

    /**
     * Parse an ExecutionPlan returning ExecutionPlanRuntime
     *
     * @param executionPlan plan to be parsed
     * @return ExecutionPlanRuntime
     */
    public static ExecutionPlanRuntime parse(ExecutionPlan executionPlan, SiddhiContext siddhiContext) {

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

            annotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_PARALLEL,
                    executionPlan.getAnnotations());
            if (annotation != null) {
                executionPlanContext.setParallel(true);
            }

            if (!executionPlanContext.isPlayback() && !executionPlanContext.isEnforceOrder() && !executionPlanContext.isParallel()) {
                executionPlanContext.setSharedLock(new ReentrantLock());
            }

            executionPlanContext.setExecutorService(new ThreadPoolExecutor(5, Integer.MAX_VALUE,
                    60L, TimeUnit.SECONDS,
                    new LinkedBlockingDeque<Runnable>()));

            executionPlanContext.setScheduledExecutorService(Executors.newScheduledThreadPool(5));
            executionPlanContext.setTimestampGenerator(new SystemCurrentTimeMillisTimestampGenerator());
            executionPlanContext.setSnapshotService(new SnapshotService(executionPlanContext));
            executionPlanContext.setPersistenceService(new PersistenceService(executionPlanContext));
            executionPlanContext.setElementIdGenerator(new ElementIdGenerator(executionPlanContext.getName()));

        } catch (DuplicateAnnotationException e) {
            throw new DuplicateAnnotationException(e.getMessage() + " for the same Execution Plan " +
                    executionPlan.toString());
        }

        ExecutionPlanRuntime executionPlanRuntime = new ExecutionPlanRuntime(executionPlanContext);

        defineStreamDefinitions(executionPlanRuntime, executionPlan.getStreamDefinitionMap());
        defineTableDefinitions(executionPlanRuntime, executionPlan.getTableDefinitionMap());
        try {
            for (ExecutionElement executionElement : executionPlan.getExecutionElementList()) {
                if (executionElement instanceof Query) {
                    QueryRuntime queryRuntime = QueryParser.parse((Query) executionElement, executionPlanContext,
                            executionPlanRuntime.getStreamDefinitionMap(), executionPlanRuntime.getTableDefinitionMap(), executionPlanRuntime.getEventTableMap());
                    executionPlanRuntime.addQuery(queryRuntime);
                } else {
                    PartitionRuntime partitionRuntime = PartitionParser.parse(executionPlanRuntime,
                            (Partition) executionElement, executionPlanContext, executionPlanRuntime.getStreamDefinitionMap());
                    executionPlanRuntime.addPartition(partitionRuntime);
                }
            }
        } catch (ExecutionPlanCreationException e) {
            throw new ExecutionPlanValidationException(e.getMessage() + " in execution plan \"" +
                    executionPlanRuntime.getName() + "\"", e);
        } catch (DuplicateDefinitionException e) {
            throw new DuplicateDefinitionException(e.getMessage() + " in execution plan \"" +
                    executionPlanRuntime.getName() + "\"", e);
        }
        return executionPlanRuntime;
    }

    private static void defineStreamDefinitions(ExecutionPlanRuntime executionPlanRuntime, Map<String, StreamDefinition> streamDefinitionMap) {
        for (StreamDefinition definition : streamDefinitionMap.values()) {
            executionPlanRuntime.defineStream(definition);
        }
    }

    private static void defineTableDefinitions(ExecutionPlanRuntime executionPlanRuntime, Map<String, TableDefinition> tableDefinitionMap) {
        for (TableDefinition definition : tableDefinitionMap.values()) {
            executionPlanRuntime.defineTable(definition);
        }
    }

    public void validateDefinition(AbstractDefinition definition) {

    }

}
