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
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.function.EvalScript;
import org.wso2.siddhi.core.stream.StreamJunction;
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
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.*;
import org.wso2.siddhi.query.api.exception.DuplicateDefinitionException;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.expression.function.AttributeFunctionExtension;
import org.wso2.siddhi.query.api.extension.Extension;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.util.concurrent.ConcurrentMap;

/**
 * Utility class for queryParser to help with QueryRuntime
 * generation.
 */
public class DefinitionParserHelper {


    public static void validateDefinition(AbstractDefinition definition, ConcurrentMap<String, AbstractDefinition> streamDefinitionMap, ConcurrentMap<String, AbstractDefinition> tableDefinitionMap) {
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
            EventTable eventTable;
            Annotation annotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_FROM,
                    tableDefinition.getAnnotations());
            if (annotation != null) {
                String evenTableType = annotation.getElement("eventtable");
                Extension extension = new AttributeFunctionExtension("eventtable", evenTableType);
                eventTable = (EventTable) SiddhiClassLoader.loadExtensionImplementation(extension, EventTableExtensionHolder.getInstance(executionPlanContext));
            } else {
                eventTable = new InMemoryEventTable(tableDefinition, executionPlanContext);
            }
            eventTable.init(tableDefinition, executionPlanContext);
            eventTableMap.putIfAbsent(tableDefinition.getId(), eventTable);
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
                    public String getFunction() {
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
            executionPlanContext.getEternalReferencedHolders().add(eventTrigger);
            eventTriggerMap.putIfAbsent(eventTrigger.getId(), eventTrigger);
        }
    }
}
