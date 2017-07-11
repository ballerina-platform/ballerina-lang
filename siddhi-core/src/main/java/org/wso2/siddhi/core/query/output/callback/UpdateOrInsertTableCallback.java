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

package org.wso2.siddhi.core.query.output.callback;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.state.StateEventPool;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverter;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.collection.AddingStreamEventExtractor;
import org.wso2.siddhi.core.util.collection.UpdateAttributeMapper;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.core.util.parser.MatcherParser;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;

/**
 * Implementation of {@link OutputCallback} to receive processed Siddhi events from
 * Siddhi queries and insert/update data into a {@link Table}
 * based on received events and condition.
 */
public class UpdateOrInsertTableCallback extends OutputCallback {
    private final int matchingStreamIndex;
    private final UpdateAttributeMapper[] updateAttributeMappers;
    private final AddingStreamEventExtractor addingStreamEventExtractor;
    private Table table;
    private CompiledCondition compiledCondition;
    private boolean convertToStreamEvent;
    private StateEventPool stateEventPool;
    private StreamEventPool streamEventPool;
    private StreamEventConverter streamEventConvertor;

    public UpdateOrInsertTableCallback(Table table, CompiledCondition compiledCondition, AbstractDefinition
            updatingStreamDefinition,
                                       int matchingStreamIndex, boolean convertToStreamEvent, StateEventPool
                                               stateEventPool,
                                       StreamEventPool streamEventPool, StreamEventConverter streamEventConvertor) {
        this.matchingStreamIndex = matchingStreamIndex;
        this.table = table;
        this.compiledCondition = compiledCondition;
        this.convertToStreamEvent = convertToStreamEvent;
        this.stateEventPool = stateEventPool;
        this.streamEventPool = streamEventPool;
        this.streamEventConvertor = streamEventConvertor;
        this.updateAttributeMappers = MatcherParser.constructUpdateAttributeMapper(table.getTableDefinition(),
                updatingStreamDefinition.getAttributeList(), matchingStreamIndex);
        this.addingStreamEventExtractor = new AddingStreamEventExtractor(matchingStreamIndex);
    }

    @Override
    public void send(ComplexEventChunk updateOrAddEventChunk) {
        updateOrAddEventChunk.reset();
        if (updateOrAddEventChunk.hasNext()) {
            ComplexEventChunk<StateEvent> updateOrAddStateEventChunk = constructMatchingStateEventChunk
                    (updateOrAddEventChunk,
                    convertToStreamEvent, stateEventPool, matchingStreamIndex, streamEventPool, streamEventConvertor);
            constructMatchingStateEventChunk(updateOrAddEventChunk, convertToStreamEvent, stateEventPool,
                    matchingStreamIndex, streamEventPool, streamEventConvertor);
            table.updateOrAddEvents(updateOrAddStateEventChunk, compiledCondition, updateAttributeMappers,
                    addingStreamEventExtractor);
        }
    }

}
