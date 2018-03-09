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

import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.event.state.StateEventPool;
import org.ballerinalang.siddhi.core.event.stream.MetaStreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEventPool;
import org.ballerinalang.siddhi.core.event.stream.converter.StreamEventConverter;
import org.ballerinalang.siddhi.core.event.stream.converter.ZeroStreamEventConverter;
import org.ballerinalang.siddhi.core.exception.OperationNotSupportedException;
import org.ballerinalang.siddhi.core.exception.SiddhiAppCreationException;
import org.ballerinalang.siddhi.core.query.output.callback.DeleteTableCallback;
import org.ballerinalang.siddhi.core.query.output.callback.InsertIntoStreamCallback;
import org.ballerinalang.siddhi.core.query.output.callback.InsertIntoTableCallback;
import org.ballerinalang.siddhi.core.query.output.callback.InsertIntoWindowCallback;
import org.ballerinalang.siddhi.core.query.output.callback.OutputCallback;
import org.ballerinalang.siddhi.core.query.output.callback.UpdateOrInsertTableCallback;
import org.ballerinalang.siddhi.core.query.output.callback.UpdateTableCallback;
import org.ballerinalang.siddhi.core.query.output.ratelimit.OutputRateLimiter;
import org.ballerinalang.siddhi.core.query.output.ratelimit.PassThroughOutputRateLimiter;
import org.ballerinalang.siddhi.core.query.output.ratelimit.event.AllPerEventOutputRateLimiter;
import org.ballerinalang.siddhi.core.query.output.ratelimit.event.FirstGroupByPerEventOutputRateLimiter;
import org.ballerinalang.siddhi.core.query.output.ratelimit.event.FirstPerEventOutputRateLimiter;
import org.ballerinalang.siddhi.core.query.output.ratelimit.event.LastGroupByPerEventOutputRateLimiter;
import org.ballerinalang.siddhi.core.query.output.ratelimit.event.LastPerEventOutputRateLimiter;
import org.ballerinalang.siddhi.core.query.output.ratelimit.snapshot.WrappedSnapshotOutputRateLimiter;
import org.ballerinalang.siddhi.core.query.output.ratelimit.time.AllPerTimeOutputRateLimiter;
import org.ballerinalang.siddhi.core.query.output.ratelimit.time.FirstGroupByPerTimeOutputRateLimiter;
import org.ballerinalang.siddhi.core.query.output.ratelimit.time.FirstPerTimeOutputRateLimiter;
import org.ballerinalang.siddhi.core.query.output.ratelimit.time.LastGroupByPerTimeOutputRateLimiter;
import org.ballerinalang.siddhi.core.query.output.ratelimit.time.LastPerTimeOutputRateLimiter;
import org.ballerinalang.siddhi.core.stream.StreamJunction;
import org.ballerinalang.siddhi.core.table.CompiledUpdateSet;
import org.ballerinalang.siddhi.core.table.Table;
import org.ballerinalang.siddhi.core.util.collection.operator.CompiledCondition;
import org.ballerinalang.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.ballerinalang.siddhi.core.util.parser.helper.DefinitionParserHelper;
import org.ballerinalang.siddhi.core.window.Window;
import org.ballerinalang.siddhi.query.api.definition.Attribute;
import org.ballerinalang.siddhi.query.api.definition.StreamDefinition;
import org.ballerinalang.siddhi.query.api.definition.TableDefinition;
import org.ballerinalang.siddhi.query.api.exception.SiddhiAppValidationException;
import org.ballerinalang.siddhi.query.api.execution.query.output.ratelimit.EventOutputRate;
import org.ballerinalang.siddhi.query.api.execution.query.output.ratelimit.OutputRate;
import org.ballerinalang.siddhi.query.api.execution.query.output.ratelimit.SnapshotOutputRate;
import org.ballerinalang.siddhi.query.api.execution.query.output.ratelimit.TimeOutputRate;
import org.ballerinalang.siddhi.query.api.execution.query.output.stream.DeleteStream;
import org.ballerinalang.siddhi.query.api.execution.query.output.stream.InsertIntoStream;
import org.ballerinalang.siddhi.query.api.execution.query.output.stream.OutputStream;
import org.ballerinalang.siddhi.query.api.execution.query.output.stream.UpdateOrInsertStream;
import org.ballerinalang.siddhi.query.api.execution.query.output.stream.UpdateSet;
import org.ballerinalang.siddhi.query.api.execution.query.output.stream.UpdateStream;
import org.ballerinalang.siddhi.query.api.expression.Variable;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Class to parse {@link OutputCallback}.
 */
public class OutputParser {


    public static OutputCallback constructOutputCallback(final OutputStream outStream,
                                                         StreamDefinition outputStreamDefinition,
                                                         Map<String, Table> tableMap,
                                                         Map<String, Window> eventWindowMap,
                                                         SiddhiAppContext siddhiAppContext,
                                                         boolean convertToStreamEvent, String queryName) {
        String id = outStream.getId();
        Table table = null;
        Window window = null;
        if (id != null) {
            table = tableMap.get(id);
            window = eventWindowMap.get(id);
        }
        StreamEventPool streamEventPool = null;
        StreamEventConverter streamEventConverter = null;
        MetaStreamEvent tableMetaStreamEvent = null;
        if (table != null) {

            tableMetaStreamEvent = new MetaStreamEvent();
            tableMetaStreamEvent.setEventType(MetaStreamEvent.EventType.TABLE);
            TableDefinition matchingTableDefinition = TableDefinition.id("");
            for (Attribute attribute : outputStreamDefinition.getAttributeList()) {
                tableMetaStreamEvent.addOutputData(attribute);
                matchingTableDefinition.attribute(attribute.getName(), attribute.getType());
            }
            matchingTableDefinition.setQueryContextStartIndex(outStream.getQueryContextStartIndex());
            matchingTableDefinition.setQueryContextEndIndex(outStream.getQueryContextEndIndex());
            tableMetaStreamEvent.addInputDefinition(matchingTableDefinition);

            streamEventPool = new StreamEventPool(tableMetaStreamEvent, 10);
            streamEventConverter = new ZeroStreamEventConverter();

        }

        //Construct CallBack
        if (outStream instanceof InsertIntoStream) {
            if (window != null) {
                return new InsertIntoWindowCallback(window, outputStreamDefinition, queryName);
            } else if (table != null) {
                DefinitionParserHelper.validateOutputStream(outputStreamDefinition, table.getTableDefinition());
                return new InsertIntoTableCallback(table, outputStreamDefinition, convertToStreamEvent,
                        streamEventPool, streamEventConverter, queryName);
            } else {
                return new InsertIntoStreamCallback(outputStreamDefinition, queryName);
            }
        } else if (outStream instanceof DeleteStream || outStream instanceof UpdateStream || outStream instanceof
                UpdateOrInsertStream) {
            if (table != null) {

                if (outStream instanceof UpdateStream) {
                    if (((UpdateStream) outStream).getUpdateSet() == null) {
                        TableDefinition tableDefinition = table.getTableDefinition();
                        for (Attribute attribute : outputStreamDefinition.getAttributeList()) {
                            if (!tableDefinition.getAttributeList().contains(attribute)) {
                                throw new SiddhiAppCreationException("Attribute " + attribute + " does not exist on " +
                                        "Event Table " + tableDefinition, outStream.getQueryContextStartIndex(),
                                        outStream.getQueryContextEndIndex());
                            }
                        }
                    }
                }

                if (outStream instanceof UpdateOrInsertStream) {
                    TableDefinition tableDefinition = table.getTableDefinition();
                    for (Attribute attribute : outputStreamDefinition.getAttributeList()) {
                        if (!tableDefinition.getAttributeList().contains(attribute)) {
                            throw new SiddhiAppCreationException("Attribute " + attribute + " does not exist on " +
                                    "Event Table " + tableDefinition, outStream.getQueryContextStartIndex(),
                                    outStream.getQueryContextEndIndex());
                        }
                    }
                }

                if (outStream instanceof DeleteStream) {
                    try {
                        MatchingMetaInfoHolder matchingMetaInfoHolder =
                                MatcherParser.constructMatchingMetaStateHolder(tableMetaStreamEvent,
                                        0, table.getTableDefinition(), 0);
                        CompiledCondition compiledCondition = table.compileCondition((((DeleteStream) outStream).
                                        getOnDeleteExpression()), matchingMetaInfoHolder, siddhiAppContext,
                                null, tableMap, queryName);
                        StateEventPool stateEventPool = new StateEventPool(matchingMetaInfoHolder.getMetaStateEvent(),
                                10);
                        return new DeleteTableCallback(table, compiledCondition, matchingMetaInfoHolder.
                                getMatchingStreamEventIndex(), convertToStreamEvent, stateEventPool, streamEventPool,
                                streamEventConverter, queryName);
                    } catch (SiddhiAppValidationException e) {
                        throw new SiddhiAppCreationException("Cannot create delete for table '" + outStream.getId() +
                                "', " + e.getMessageWithOutContext(), e, e.getQueryContextStartIndex(),
                                e.getQueryContextEndIndex(), siddhiAppContext.getName(),
                                siddhiAppContext.getSiddhiAppString());
                    }
                } else if (outStream instanceof UpdateStream) {
                    try {
                        MatchingMetaInfoHolder matchingMetaInfoHolder =
                                MatcherParser.constructMatchingMetaStateHolder(tableMetaStreamEvent,
                                        0, table.getTableDefinition(), 0);
                        CompiledCondition compiledCondition = table.compileCondition((((UpdateStream) outStream).
                                        getOnUpdateExpression()), matchingMetaInfoHolder, siddhiAppContext,
                                null, tableMap, queryName);
                        UpdateSet updateSet = ((UpdateStream) outStream).getUpdateSet();
                        if (updateSet == null) {
                            updateSet = new UpdateSet();
                            for (Attribute attribute : matchingMetaInfoHolder.getMatchingStreamDefinition().
                                    getAttributeList()) {
                                updateSet.set(new Variable(attribute.getName()), new Variable(attribute.getName()));
                            }
                        }
                        CompiledUpdateSet compiledUpdateSet = table.compileUpdateSet(updateSet, matchingMetaInfoHolder,
                                siddhiAppContext, null, tableMap, queryName);
                        StateEventPool stateEventPool = new StateEventPool(matchingMetaInfoHolder.getMetaStateEvent(),
                                10);
                        return new UpdateTableCallback(table, compiledCondition, compiledUpdateSet,
                                matchingMetaInfoHolder.getMatchingStreamEventIndex(), convertToStreamEvent,
                                stateEventPool, streamEventPool, streamEventConverter, queryName);
                    } catch (SiddhiAppValidationException e) {
                        throw new SiddhiAppCreationException("Cannot create update for table '" + outStream.getId() +
                                "', " + e.getMessageWithOutContext(), e, e.getQueryContextStartIndex(),
                                e.getQueryContextEndIndex(), siddhiAppContext);
                    }
                } else {
                    DefinitionParserHelper.validateOutputStream(outputStreamDefinition, table.getTableDefinition
                            ());
                    try {
                        MatchingMetaInfoHolder matchingMetaInfoHolder =
                                MatcherParser.constructMatchingMetaStateHolder(tableMetaStreamEvent, 0,
                                        table.getTableDefinition(), 0);
                        CompiledCondition compiledCondition = table.
                                compileCondition((((UpdateOrInsertStream) outStream).getOnUpdateExpression()),
                                        matchingMetaInfoHolder, siddhiAppContext, null,
                                        tableMap, queryName);
                        UpdateSet updateSet = ((UpdateOrInsertStream) outStream).getUpdateSet();
                        if (updateSet == null) {
                            updateSet = new UpdateSet();
                            for (Attribute attribute : matchingMetaInfoHolder.getMatchingStreamDefinition().
                                    getAttributeList()) {
                                updateSet.set(new Variable(attribute.getName()), new Variable(attribute.getName()));
                            }
                        }
                        CompiledUpdateSet compiledUpdateSet = table.compileUpdateSet(updateSet, matchingMetaInfoHolder,
                                siddhiAppContext, null, tableMap, queryName);
                        StateEventPool stateEventPool = new StateEventPool(matchingMetaInfoHolder.getMetaStateEvent(),
                                10);
                        return new UpdateOrInsertTableCallback(table, compiledCondition, compiledUpdateSet,
                                matchingMetaInfoHolder.getMatchingStreamEventIndex(), convertToStreamEvent,
                                stateEventPool, streamEventPool, streamEventConverter, queryName);

                    } catch (SiddhiAppValidationException e) {
                        throw new SiddhiAppCreationException("Cannot create update or insert into for table '" +
                                outStream.getId() + "', " + e.getMessageWithOutContext(), e,
                                e.getQueryContextStartIndex(), e.getQueryContextEndIndex(), siddhiAppContext);
                    }
                }
            } else {
                throw new SiddhiAppCreationException("Event table with id :" + id + " does not exist",
                        outStream.getQueryContextStartIndex(),
                        outStream.getQueryContextEndIndex());
            }
        } else {
            throw new SiddhiAppCreationException(outStream.getClass().getName() + " not supported",
                    outStream.getQueryContextStartIndex(),
                    outStream.getQueryContextEndIndex());
        }

    }

    public static OutputCallback constructOutputCallback(OutputStream outStream, String key,
                                                         ConcurrentMap<String, StreamJunction> streamJunctionMap,
                                                         StreamDefinition outputStreamDefinition,
                                                         SiddhiAppContext siddhiAppContext, String queryName) {
        String id = outStream.getId();
        //Construct CallBack
        if (outStream instanceof InsertIntoStream) {
            StreamJunction outputStreamJunction = streamJunctionMap.get(id + key);
            if (outputStreamJunction == null) {
                outputStreamJunction = new StreamJunction(outputStreamDefinition,
                        siddhiAppContext.getExecutorService(),
                        siddhiAppContext.getBufferSize(), siddhiAppContext);
                streamJunctionMap.putIfAbsent(id + key, outputStreamJunction);
            }
            InsertIntoStreamCallback insertIntoStreamCallback = new InsertIntoStreamCallback(outputStreamDefinition,
                    queryName);
            insertIntoStreamCallback.init(streamJunctionMap.get(id + key));
            return insertIntoStreamCallback;

        } else {
            throw new SiddhiAppCreationException(outStream.getClass().getName() + " not supported",
                    outStream.getQueryContextStartIndex(), outStream.getQueryContextEndIndex());
        }
    }

    public static OutputRateLimiter constructOutputRateLimiter(String id, OutputRate outputRate, boolean isGroupBy,
                                                               boolean isWindow, ScheduledExecutorService
                                                                       scheduledExecutorService, SiddhiAppContext
                                                                       siddhiAppContext, String queryName) {
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
                    scheduledExecutorService, isGroupBy, isWindow, siddhiAppContext, queryName);
        }

    }


}
