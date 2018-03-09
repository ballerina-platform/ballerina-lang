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
package org.ballerinalang.siddhi.core.query.output.callback;

import org.ballerinalang.siddhi.core.debugger.SiddhiDebugger;
import org.ballerinalang.siddhi.core.event.ComplexEvent;
import org.ballerinalang.siddhi.core.event.ComplexEventChunk;
import org.ballerinalang.siddhi.core.event.stream.StreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEventPool;
import org.ballerinalang.siddhi.core.event.stream.converter.StreamEventConverter;
import org.ballerinalang.siddhi.core.table.Table;
import org.ballerinalang.siddhi.query.api.definition.StreamDefinition;

/**
 * Implementation of {@link OutputCallback} to receive processed Siddhi events from
 * Siddhi queries and insert data into a {@link Table}
 * based on received events and condition.
 */
public class InsertIntoTableCallback extends OutputCallback {
    private Table table;
    private StreamDefinition outputStreamDefinition;
    private boolean convertToStreamEvent;
    private StreamEventPool streamEventPool;
    private StreamEventConverter streamEventConverter;

    public InsertIntoTableCallback(Table table, StreamDefinition outputStreamDefinition,
                                   boolean convertToStreamEvent, StreamEventPool streamEventPool,
                                   StreamEventConverter streamEventConverter, String queryName) {
        super(queryName);
        this.table = table;
        this.outputStreamDefinition = outputStreamDefinition;
        this.convertToStreamEvent = convertToStreamEvent;
        this.streamEventPool = streamEventPool;
        this.streamEventConverter = streamEventConverter;
    }

    @Override
    public void send(ComplexEventChunk complexEventChunk, int noOfEvents) {
        if (getSiddhiDebugger() != null) {
            getSiddhiDebugger()
                    .checkBreakPoint(getQueryName(), SiddhiDebugger.QueryTerminal.OUT, complexEventChunk.getFirst());
        }
        if (convertToStreamEvent) {
            ComplexEventChunk<StreamEvent> streamEventChunk = new ComplexEventChunk<StreamEvent>(complexEventChunk
                    .isBatch());
            complexEventChunk.reset();
            while (complexEventChunk.hasNext()) {
                ComplexEvent complexEvent = complexEventChunk.next();
                StreamEvent borrowEvent = streamEventPool.borrowEvent();
                streamEventConverter.convertData(
                        complexEvent.getTimestamp(),
                        complexEvent.getOutputData(),
                        complexEvent.getType(),
                        borrowEvent);
                streamEventChunk.add(borrowEvent);
            }
            table.addEvents(streamEventChunk, noOfEvents);
        } else {
            table.addEvents((ComplexEventChunk<StreamEvent>) complexEventChunk, noOfEvents);
        }
    }

    public StreamDefinition getOutputStreamDefinition() {
        return outputStreamDefinition;
    }

}
