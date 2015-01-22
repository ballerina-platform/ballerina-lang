/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.exception.DefinitionNotExistException;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.finder.Finder;
import org.wso2.siddhi.core.query.output.callback.*;
import org.wso2.siddhi.core.query.output.rateLimit.OutputRateLimiter;
import org.wso2.siddhi.core.query.output.rateLimit.PassThroughOutputRateLimiter;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.parser.helper.DefinitionParserHelper;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.execution.query.output.ratelimit.OutputRate;
import org.wso2.siddhi.query.api.execution.query.output.stream.DeleteStream;
import org.wso2.siddhi.query.api.execution.query.output.stream.InsertIntoStream;
import org.wso2.siddhi.query.api.execution.query.output.stream.OutputStream;
import org.wso2.siddhi.query.api.execution.query.output.stream.UpdateStream;

import java.util.concurrent.ConcurrentMap;

public class OutputParser {


    public static OutputCallback constructOutputCallback(OutputStream outStream, StreamDefinition outputStreamDefinition,
                                                         ExecutionPlanContext executionPlanContext) {
        String id = outStream.getId();

        //Construct CallBack
        if (outStream instanceof InsertIntoStream) {
            EventTable eventTable = executionPlanContext.getEventTableMap().get(id);
            if (eventTable != null) {
                DefinitionParserHelper.validateOutputStream(outputStreamDefinition, eventTable.getTableDefinition());
                return new InsertIntoTableCallback(eventTable, outputStreamDefinition);
            } else {
                return new InsertIntoStreamCallback(outputStreamDefinition);
            }
        } else if (outStream instanceof DeleteStream) {
            EventTable eventTable = executionPlanContext.getEventTableMap().get(id);
            if (eventTable != null) {
                DefinitionParserHelper.validateOutputStream(outputStreamDefinition, eventTable.getTableDefinition());
                MetaStreamEvent matchingMetaStreamEvent = new MetaStreamEvent();
                matchingMetaStreamEvent.setTableEvent(true);
                TableDefinition tableDefinition = TableDefinition.id("");
                for (Attribute attribute : outputStreamDefinition.getAttributeList()) {
                    matchingMetaStreamEvent.addOutputData(attribute);
                    tableDefinition.attribute(attribute.getName(), attribute.getType());
                }
                matchingMetaStreamEvent.setInputDefinition(tableDefinition);
                Finder finder = eventTable.constructFinder(((DeleteStream) outStream).getOnDeleteExpression(), matchingMetaStreamEvent, executionPlanContext, null, 0);
                return new DeleteTableCallback(eventTable, finder);
            } else {
                throw new DefinitionNotExistException("Event table with id :" + id + " does not exist");
            }
        } else if (outStream instanceof UpdateStream) {
            EventTable eventTable = executionPlanContext.getEventTableMap().get(id);
            if (eventTable != null) {
                DefinitionParserHelper.validateOutputStream(outputStreamDefinition, eventTable.getTableDefinition());
                MetaStateEvent metaStateEvent = createMetaStateEvent(outputStreamDefinition, eventTable);
                Finder finder = eventTable.constructFinder(((UpdateStream) outStream).getOnUpdateExpression(), metaStateEvent, executionPlanContext, null, 0);
                return new UpdateTableCallback(eventTable, finder, outputStreamDefinition);
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
        matchingMetaStreamEvent.setInputDefinition(outputStreamDefinition);
        for (Attribute attribute : outputStreamDefinition.getAttributeList()) {
            matchingMetaStreamEvent.addOutputData(attribute);
        }
        metaStateEvent.addEvent(matchingMetaStreamEvent);

        MetaStreamEvent candidateMetaStreamEvent = new MetaStreamEvent();
        candidateMetaStreamEvent.setInputDefinition(eventTable.getTableDefinition());
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
        String id = "#" + outStream.getId();
        //Construct CallBack
        if (outStream instanceof InsertIntoStream) {
            StreamJunction outputStreamJunction = streamJunctionMap.get(id + key);
            if (outputStreamJunction == null) {
                outputStreamJunction = new StreamJunction(outputStreamDefinition,
                        executionPlanContext.getExecutorService(),
                        executionPlanContext.getSiddhiContext().getEventBufferSize(), executionPlanContext);
                streamJunctionMap.putIfAbsent(id + key, outputStreamJunction);
            }
            InsertIntoStreamCallback insertIntoStreamCallback = new InsertIntoStreamCallback(outputStreamDefinition);
            insertIntoStreamCallback.init(streamJunctionMap.get(id + key));
            return insertIntoStreamCallback;

        } else {
            throw new ExecutionPlanCreationException(outStream.getClass().getName() + " not supported");
        }
    }

    public static OutputRateLimiter constructOutputRateLimiter(String id, OutputRate outputRate) {
        if (outputRate == null) {
            return new PassThroughOutputRateLimiter(id);
        }
        //TODO: else
        return null;
    }
}
