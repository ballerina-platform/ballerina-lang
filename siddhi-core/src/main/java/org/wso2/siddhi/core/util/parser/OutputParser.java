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

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.state.StateEventPool;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverter;
import org.wso2.siddhi.core.event.stream.converter.ZeroStreamEventConverter;
import org.wso2.siddhi.core.exception.DefinitionNotExistException;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.query.output.callback.DeleteTableCallback;
import org.wso2.siddhi.core.query.output.callback.InsertIntoStreamCallback;
import org.wso2.siddhi.core.query.output.callback.InsertIntoTableCallback;
import org.wso2.siddhi.core.query.output.callback.InsertIntoWindowCallback;
import org.wso2.siddhi.core.query.output.callback.OutputCallback;
import org.wso2.siddhi.core.query.output.callback.UpdateOrInsertTableCallback;
import org.wso2.siddhi.core.query.output.callback.UpdateTableCallback;
import org.wso2.siddhi.core.query.output.ratelimit.OutputRateLimiter;
import org.wso2.siddhi.core.query.output.ratelimit.PassThroughOutputRateLimiter;
import org.wso2.siddhi.core.query.output.ratelimit.event.AllPerEventOutputRateLimiter;
import org.wso2.siddhi.core.query.output.ratelimit.event.FirstGroupByPerEventOutputRateLimiter;
import org.wso2.siddhi.core.query.output.ratelimit.event.FirstPerEventOutputRateLimiter;
import org.wso2.siddhi.core.query.output.ratelimit.event.LastGroupByPerEventOutputRateLimiter;
import org.wso2.siddhi.core.query.output.ratelimit.event.LastPerEventOutputRateLimiter;
import org.wso2.siddhi.core.query.output.ratelimit.snapshot.WrappedSnapshotOutputRateLimiter;
import org.wso2.siddhi.core.query.output.ratelimit.time.AllPerTimeOutputRateLimiter;
import org.wso2.siddhi.core.query.output.ratelimit.time.FirstGroupByPerTimeOutputRateLimiter;
import org.wso2.siddhi.core.query.output.ratelimit.time.FirstPerTimeOutputRateLimiter;
import org.wso2.siddhi.core.query.output.ratelimit.time.LastGroupByPerTimeOutputRateLimiter;
import org.wso2.siddhi.core.query.output.ratelimit.time.LastPerTimeOutputRateLimiter;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.wso2.siddhi.core.util.parser.helper.DefinitionParserHelper;
import org.wso2.siddhi.core.window.EventWindow;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.execution.query.output.ratelimit.EventOutputRate;
import org.wso2.siddhi.query.api.execution.query.output.ratelimit.OutputRate;
import org.wso2.siddhi.query.api.execution.query.output.ratelimit.SnapshotOutputRate;
import org.wso2.siddhi.query.api.execution.query.output.ratelimit.TimeOutputRate;
import org.wso2.siddhi.query.api.execution.query.output.stream.DeleteStream;
import org.wso2.siddhi.query.api.execution.query.output.stream.InsertIntoStream;
import org.wso2.siddhi.query.api.execution.query.output.stream.OutputStream;
import org.wso2.siddhi.query.api.execution.query.output.stream.UpdateOrInsertStream;
import org.wso2.siddhi.query.api.execution.query.output.stream.UpdateStream;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;

public class OutputParser {


    public static OutputCallback constructOutputCallback(final OutputStream outStream,
                                                         StreamDefinition outputStreamDefinition,
                                                         Map<String, EventTable> eventTableMap,
                                                         Map<String, EventWindow> eventWindowMap,
                                                         ExecutionPlanContext executionPlanContext,
                                                         boolean convertToStreamEvent, String queryName) {
        String id = outStream.getId();
        EventTable eventTable = null;
        EventWindow eventWindow = null;
        if (id != null) {
            eventTable = eventTableMap.get(id);
            eventWindow = eventWindowMap.get(id);
        }
        StreamEventPool streamEventPool = null;
        StreamEventConverter streamEventConverter = null;
        MetaStreamEvent tableMetaStreamEvent = null;
        if (eventTable != null) {

            tableMetaStreamEvent = new MetaStreamEvent();
            tableMetaStreamEvent.setTableEvent(true);
            TableDefinition matchingTableDefinition = TableDefinition.id("");
            for (Attribute attribute : outputStreamDefinition.getAttributeList()) {
                tableMetaStreamEvent.addOutputData(attribute);
                matchingTableDefinition.attribute(attribute.getName(), attribute.getType());
            }
            tableMetaStreamEvent.addInputDefinition(matchingTableDefinition);

            streamEventPool = new StreamEventPool(tableMetaStreamEvent, 10);
            streamEventConverter = new ZeroStreamEventConverter();

        }

        //Construct CallBack
        if (outStream instanceof InsertIntoStream) {
            if (eventWindow != null) {
                return new InsertIntoWindowCallback(eventWindow, outputStreamDefinition);
            } else if (eventTable != null) {
                DefinitionParserHelper.validateOutputStream(outputStreamDefinition, eventTable.getTableDefinition());
                return new InsertIntoTableCallback(eventTable, outputStreamDefinition, convertToStreamEvent,
                        streamEventPool, streamEventConverter);
            } else {
                return new InsertIntoStreamCallback(outputStreamDefinition, queryName);
            }
        } else if (outStream instanceof DeleteStream || outStream instanceof UpdateStream || outStream instanceof
                UpdateOrInsertStream) {
            if (eventTable != null) {

                if (outStream instanceof UpdateStream || outStream instanceof UpdateOrInsertStream) {
                    TableDefinition eventTableDefinition = eventTable.getTableDefinition();
                    for (Attribute attribute : outputStreamDefinition.getAttributeList()) {
                        if (!eventTableDefinition.getAttributeList().contains(attribute)) {
                            throw new ExecutionPlanCreationException("Attribute " + attribute + " does not exist on " +
                                    "Event Table " + eventTableDefinition);
                        }
                    }
                }

                if (outStream instanceof DeleteStream) {
                    try {
                        MatchingMetaInfoHolder matchingMetaInfoHolder =
                                MatcherParser.constructMatchingMetaStateHolder(tableMetaStreamEvent, 0, eventTable
                                        .getTableDefinition(), 0);
                        CompiledCondition compiledCondition = eventTable.compileCondition((((DeleteStream) outStream)
                                        .getOnDeleteExpression()),
                                matchingMetaInfoHolder, executionPlanContext, null, eventTableMap, queryName);
                        StateEventPool stateEventPool = new StateEventPool(matchingMetaInfoHolder.getMetaStateEvent()
                                , 10);
                        return new DeleteTableCallback(eventTable, compiledCondition, matchingMetaInfoHolder
                                .getMatchingStreamEventIndex(),
                                convertToStreamEvent, stateEventPool, streamEventPool, streamEventConverter);
                    } catch (ExecutionPlanValidationException e) {
                        throw new ExecutionPlanCreationException("Cannot create delete for table '" + outStream.getId
                                () + "', " + e.getMessage(), e);
                    }
                } else if (outStream instanceof UpdateStream) {
                    try {
                        MatchingMetaInfoHolder matchingMetaInfoHolder =
                                MatcherParser.constructMatchingMetaStateHolder(tableMetaStreamEvent, 0, eventTable
                                        .getTableDefinition(), 0);
                        CompiledCondition compiledCondition = eventTable.compileCondition((((UpdateStream) outStream)
                                        .getOnUpdateExpression()),
                                matchingMetaInfoHolder, executionPlanContext, null, eventTableMap, queryName);
                        StateEventPool stateEventPool = new StateEventPool(matchingMetaInfoHolder.getMetaStateEvent()
                                , 10);
                        return new UpdateTableCallback(eventTable, compiledCondition, outputStreamDefinition,
                                matchingMetaInfoHolder.getMatchingStreamEventIndex(), convertToStreamEvent,
                                stateEventPool,
                                streamEventPool, streamEventConverter);
                    } catch (ExecutionPlanValidationException e) {
                        throw new ExecutionPlanCreationException("Cannot create update for table '" + outStream.getId
                                () + "', " + e.getMessage(), e);
                    }
                } else {
                    DefinitionParserHelper.validateOutputStream(outputStreamDefinition, eventTable.getTableDefinition
                            ());
                    try {
                        MatchingMetaInfoHolder matchingMetaInfoHolder =
                                MatcherParser.constructMatchingMetaStateHolder(tableMetaStreamEvent, 0, eventTable
                                        .getTableDefinition(), 0);
                        CompiledCondition compiledCondition = eventTable.compileCondition((((UpdateOrInsertStream)
                                        outStream).getOnUpdateExpression()),
                                matchingMetaInfoHolder, executionPlanContext, null, eventTableMap, queryName);
                        StateEventPool stateEventPool = new StateEventPool(matchingMetaInfoHolder.getMetaStateEvent()
                                , 10);
                        return new UpdateOrInsertTableCallback(eventTable, compiledCondition, outputStreamDefinition,
                                matchingMetaInfoHolder.getMatchingStreamEventIndex(), convertToStreamEvent,
                                stateEventPool,
                                streamEventPool, streamEventConverter);

                    } catch (ExecutionPlanValidationException e) {
                        throw new ExecutionPlanCreationException("Cannot create update or insert into for table '" +
                                outStream.getId() + "', " + e.getMessage(), e);
                    }
                }
            } else {
                throw new DefinitionNotExistException("Event table with id :" + id + " does not exist");
            }
        } else {
            throw new ExecutionPlanCreationException(outStream.getClass().getName() + " not supported");
        }

    }

    public static OutputCallback constructOutputCallback(OutputStream outStream, String key,
                                                         ConcurrentMap<String, StreamJunction> streamJunctionMap,
                                                         StreamDefinition outputStreamDefinition,
                                                         ExecutionPlanContext executionPlanContext, String queryName) {
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
            InsertIntoStreamCallback insertIntoStreamCallback = new InsertIntoStreamCallback(outputStreamDefinition,
                    queryName);
            insertIntoStreamCallback.init(streamJunctionMap.get(id + key));
            return insertIntoStreamCallback;

        } else {
            throw new ExecutionPlanCreationException(outStream.getClass().getName() + " not supported");
        }
    }

    public static OutputRateLimiter constructOutputRateLimiter(String id, OutputRate outputRate, boolean isGroupBy,
                                                               boolean isWindow, ScheduledExecutorService
                                                                       scheduledExecutorService, ExecutionPlanContext
                                                                       executionPlanContext, String queryName) {
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
            throw new OperationNotSupportedException(((EventOutputRate) outputRate).getType() + " not supported in " +
                    "output rate limiting");
        } else if (outputRate instanceof TimeOutputRate) {
            switch (((TimeOutputRate) outputRate).getType()) {
                case ALL:
                    return new AllPerTimeOutputRateLimiter(id, ((TimeOutputRate) outputRate).getValue(),
                            scheduledExecutorService, queryName);
                case FIRST:
                    if (isGroupBy) {
                        return new FirstGroupByPerTimeOutputRateLimiter(id, ((TimeOutputRate) outputRate).getValue(),
                                scheduledExecutorService, queryName);
                    } else {
                        return new FirstPerTimeOutputRateLimiter(id, ((TimeOutputRate) outputRate).getValue(),
                                scheduledExecutorService, queryName);
                    }
                case LAST:
                    if (isGroupBy) {
                        return new LastGroupByPerTimeOutputRateLimiter(id, ((TimeOutputRate) outputRate).getValue(),
                                scheduledExecutorService, queryName);
                    } else {
                        return new LastPerTimeOutputRateLimiter(id, ((TimeOutputRate) outputRate).getValue(),
                                scheduledExecutorService, queryName);
                    }
            }
            //never happens
            throw new OperationNotSupportedException(((TimeOutputRate) outputRate).getType() + " not supported in " +
                    "output rate limiting");
        } else {
            return new WrappedSnapshotOutputRateLimiter(id, ((SnapshotOutputRate) outputRate).getValue(),
                    scheduledExecutorService, isGroupBy, isWindow, executionPlanContext, queryName);
        }

    }


}
