/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.query.output.callback;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.state.StateEventPool;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverter;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.collection.UpdateAttributeMapper;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.core.util.parser.MatcherParser;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;

public class UpdateTableCallback extends OutputCallback {
    private final int matchingStreamIndex;
    private final UpdateAttributeMapper[] updateAttributeMappers;
    private EventTable eventTable;
    private CompiledCondition compiledCondition;
    private boolean convertToStreamEvent;
    private StateEventPool stateEventPool;
    private StreamEventPool streamEventPool;
    private StreamEventConverter streamEventConvertor;

    public UpdateTableCallback(EventTable eventTable, CompiledCondition compiledCondition, AbstractDefinition updatingStreamDefinition,
                               int matchingStreamIndex, boolean convertToStreamEvent, StateEventPool stateEventPool,
                               StreamEventPool streamEventPool, StreamEventConverter streamEventConvertor) {
        this.eventTable = eventTable;
        this.compiledCondition = compiledCondition;
        this.matchingStreamIndex = matchingStreamIndex;
        this.convertToStreamEvent = convertToStreamEvent;
        this.stateEventPool = stateEventPool;
        this.streamEventPool = streamEventPool;
        this.streamEventConvertor = streamEventConvertor;
        this.updateAttributeMappers = MatcherParser.constructUpdateAttributeMapper(eventTable.getTableDefinition(),
                updatingStreamDefinition.getAttributeList(), matchingStreamIndex);
    }

    @Override
    public synchronized void send(ComplexEventChunk updatingEventChunk) {
        updatingEventChunk.reset();
        if (updatingEventChunk.hasNext()) {
            ComplexEventChunk<StateEvent> updatingStateEventChunk = constructMatchingStateEventChunk(updatingEventChunk,
                    convertToStreamEvent, stateEventPool, matchingStreamIndex, streamEventPool, streamEventConvertor);
            eventTable.update(updatingStateEventChunk, compiledCondition, updateAttributeMappers);
        }
    }

}
