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
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.exception.DefinitionNotExistException;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.query.output.callback.*;
import org.wso2.siddhi.core.query.output.ratelimit.OutputRateLimiter;
import org.wso2.siddhi.core.query.output.ratelimit.PassThroughOutputRateLimiter;
import org.wso2.siddhi.core.query.output.ratelimit.event.*;
import org.wso2.siddhi.core.query.output.ratelimit.snapshot.WrappedSnapshotOutputRateLimiter;
import org.wso2.siddhi.core.query.output.ratelimit.time.*;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.core.util.parser.helper.DefinitionParserHelper;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.execution.query.output.ratelimit.EventOutputRate;
import org.wso2.siddhi.query.api.execution.query.output.ratelimit.OutputRate;
import org.wso2.siddhi.query.api.execution.query.output.ratelimit.SnapshotOutputRate;
import org.wso2.siddhi.query.api.execution.query.output.ratelimit.TimeOutputRate;
import org.wso2.siddhi.query.api.execution.query.output.stream.*;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;

public class OutputParser {


    public static OutputCallback constructOutputCallback(OutputStream outStream, StreamDefinition outputStreamDefinition,
                                                         Map<String, EventTable> eventTableMap, ExecutionPlanContext executionPlanContext) {
        String id = outStream.getId();

        //Construct CallBack
        if (outStream instanceof InsertIntoStream) {
            EventTable eventTable = eventTableMap.get(id);
            if (eventTable != null) {
                DefinitionParserHelper.validateOutputStream(outputStreamDefinition, eventTable.getTableDefinition());
                return new InsertIntoTableCallback(eventTable, outputStreamDefinition);
            } else {
                return new InsertIntoStreamCallback(outputStreamDefinition);
            }
        } else if (outStream instanceof DeleteStream || outStream instanceof UpdateStream || outStream instanceof InsertOverwriteStream) {
            EventTable eventTable = eventTableMap.get(id);
            if (eventTable != null) {

                if (outStream instanceof UpdateStream || outStream instanceof InsertOverwriteStream) {
                    TableDefinition eventTableDefinition = eventTable.getTableDefinition();
                    for (Attribute attribute : outputStreamDefinition.getAttributeList()) {
                        if (!eventTableDefinition.getAttributeList().contains(attribute)) {
                            throw new ExecutionPlanCreationException("Attribute " + attribute + " does not exist on Event Table " + eventTableDefinition);
                        }
                    }
                }

                MetaStreamEvent matchingMetaStreamEvent = new MetaStreamEvent();
                matchingMetaStreamEvent.setTableEvent(true);
                TableDefinition matchingTableDefinition = TableDefinition.id("");
                for (Attribute attribute : outputStreamDefinition.getAttributeList()) {
                    matchingMetaStreamEvent.addOutputData(attribute);
                    matchingTableDefinition.attribute(attribute.getName(), attribute.getType());
                }
                matchingMetaStreamEvent.addInputDefinition(matchingTableDefinition);
                if (outStream instanceof DeleteStream) {
                    try {
                        Operator operator = eventTable.constructOperator(((DeleteStream) outStream).getOnDeleteExpression(), matchingMetaStreamEvent, executionPlanContext, null, eventTableMap, 0, SiddhiConstants.ANY);
                        return new DeleteTableCallback(eventTable, operator);
                    } catch (ExecutionPlanValidationException e) {
                        throw new ExecutionPlanCreationException("Cannot create delete for table '" + outStream.getId() + "', " + e.getMessage(), e);
                    }
                } else if (outStream instanceof UpdateStream) {
                    try {
                        Operator operator = eventTable.constructOperator(((UpdateStream) outStream).getOnUpdateExpression(), matchingMetaStreamEvent, executionPlanContext, null, eventTableMap, 0, SiddhiConstants.ANY);
                        return new UpdateTableCallback(eventTable, operator, matchingTableDefinition);
                    } catch (ExecutionPlanValidationException e) {
                        throw new ExecutionPlanCreationException("Cannot create update for table '" + outStream.getId() + "', " + e.getMessage(), e);
                    }
                } else  {
                    DefinitionParserHelper.validateOutputStream(outputStreamDefinition, eventTable.getTableDefinition());
                    try {
                        Operator operator = eventTable.constructOperator(((InsertOverwriteStream) outStream).getOnOverwriteExpression(), matchingMetaStreamEvent, executionPlanContext, null, eventTableMap, 0, SiddhiConstants.ANY);
                        return new InsertOverwriteTableCallback(eventTable, operator, matchingTableDefinition);
                    } catch (ExecutionPlanValidationException e) {
                        throw new ExecutionPlanCreationException("Cannot create insert overwrite for table '" + outStream.getId() + "', " + e.getMessage(), e);
                    }
                }
            } else {
                throw new DefinitionNotExistException("Event table with id :" + id + " does not exist");
            }
        } else {
            throw new ExecutionPlanCreationException(outStream.getClass().getName() + " not supported");
        }

    }

    private static MetaStateEvent createMetaStateEvent(StreamDefinition outputStreamDefinition, EventTable eventTable) {
        MetaStateEvent metaStateEvent = new MetaStateEvent(2);

        MetaStreamEvent matchingMetaStreamEvent = new MetaStreamEvent();
        matchingMetaStreamEvent.addInputDefinition(outputStreamDefinition);
        for (Attribute attribute : outputStreamDefinition.getAttributeList()) {
            matchingMetaStreamEvent.addOutputData(attribute);
        }
        metaStateEvent.addEvent(matchingMetaStreamEvent);

        MetaStreamEvent candidateMetaStreamEvent = new MetaStreamEvent();
        candidateMetaStreamEvent.addInputDefinition(eventTable.getTableDefinition());
        for (Attribute attribute : eventTable.getTableDefinition().getAttributeList()) {
            candidateMetaStreamEvent.addOutputData(attribute);
        }
        metaStateEvent.addEvent(candidateMetaStreamEvent);
        return metaStateEvent;
    }

    public static OutputCallback constructOutputCallback(OutputStream outStream, String key,
                                                         ConcurrentMap<String, StreamJunction> streamJunctionMap,
                                                         StreamDefinition outputStreamDefinition,
                                                         ExecutionPlanContext executionPlanContext) {
        String id = outStream.getId();
        //Construct CallBack
        if (outStream instanceof InsertIntoStream) {
            StreamJunction outputStreamJunction = streamJunctionMap.get(id + key);
            if (outputStreamJunction == null) {
                outputStreamJunction = new StreamJunction(outputStreamDefinition,
                        executionPlanContext.getExecutorService(),
                        executionPlanContext.getBufferSize(), executionPlanContext);
                streamJunctionMap.putIfAbsent(id + key, outputStreamJunction);
            }
            InsertIntoStreamCallback insertIntoStreamCallback = new InsertIntoStreamCallback(outputStreamDefinition);
            insertIntoStreamCallback.init(streamJunctionMap.get(id + key));
            return insertIntoStreamCallback;

        } else {
            throw new ExecutionPlanCreationException(outStream.getClass().getName() + " not supported");
        }
    }

    public static OutputRateLimiter constructOutputRateLimiter(String id, OutputRate outputRate, boolean isGroupBy, boolean isWindow, ScheduledExecutorService scheduledExecutorService) {
        if (outputRate == null) {
            return new PassThroughOutputRateLimiter(id);
        } else if (outputRate instanceof EventOutputRate) {
            switch (((EventOutputRate) outputRate).getType()) {
                case ALL:
                    return new AllPerEventOutputRateLimiter(id, ((EventOutputRate) outputRate).getValue());
                case FIRST:
                    if (isGroupBy) {
                        return new FirstGroupByPerEventOutputRateLimiter(id, ((EventOutputRate) outputRate).getValue());
                    } else {
                        return new FirstPerEventOutputRateLimiter(id, ((EventOutputRate) outputRate).getValue());
                    }
                case LAST:
                    if (isGroupBy) {
                        return new LastGroupByPerEventOutputRateLimiter(id, ((EventOutputRate) outputRate).getValue());
                    } else {
                        return new LastPerEventOutputRateLimiter(id, ((EventOutputRate) outputRate).getValue());
                    }
            }
            //never happens
            return null;
        } else if (outputRate instanceof TimeOutputRate) {
            switch (((TimeOutputRate) outputRate).getType()) {
                case ALL:
                    return new AllPerTimeOutputRateLimiter(id, ((TimeOutputRate) outputRate).getValue(), scheduledExecutorService);
                case FIRST:
                    if (isGroupBy) {
                        return new FirstGroupByPerTimeOutputRateLimiter(id, ((TimeOutputRate) outputRate).getValue(), scheduledExecutorService);
                    } else {
                        return new FirstPerTimeOutputRateLimiter(id, ((TimeOutputRate) outputRate).getValue(), scheduledExecutorService);
                    }
                case LAST:
                    if (isGroupBy) {
                        return new LastGroupByPerTimeOutputRateLimiter(id, ((TimeOutputRate) outputRate).getValue(), scheduledExecutorService);
                    } else {
                        return new LastPerTimeOutputRateLimiter(id, ((TimeOutputRate) outputRate).getValue(), scheduledExecutorService);
                    }
            }
            //never happens
            return null;
        } else {
            return new WrappedSnapshotOutputRateLimiter(id, ((SnapshotOutputRate) outputRate).getValue(), scheduledExecutorService, isGroupBy, isWindow);
        }

    }


}
