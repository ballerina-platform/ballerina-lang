/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.stream.input.source;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverter;
import org.wso2.siddhi.core.event.stream.converter.ZeroStreamEventConverter;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.query.output.callback.OutputCallback;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.execution.io.map.AttributeMapping;

import java.util.List;
import java.util.Map;

/**
 * This mapper receives Event or Object[] as input and send the {@link ComplexEventChunk} to the {@link OutputCallback}.
 * No additional options are required.
 */
public class PassThroughInputMapper implements InputMapper {

    /**
     * SinkCallback to which the converted event must be sent.
     */
    private OutputCallback outputCallback;

    /**
     * StreamEventPool used to borrow a new event.
     */
    private StreamEventPool streamEventPool;

    /**
     * StreamEventConverter to convert {@link Event} to {@link StreamEvent}.
     */
    private StreamEventConverter streamEventConverter;

    /**
     * Initialize the mapper and the mapping configurations.
     *
     * @param outputStreamDefinition the output StreamDefinition
     * @param outputCallback         the SinkCallback to which the output has to be sent
     * @param metaStreamEvent        the MetaStreamEvent
     * @param options                additional mapping options
     * @param attributeMappingList   list of attributes mapping
     */
    @Override
    public void init(StreamDefinition outputStreamDefinition, OutputCallback outputCallback, MetaStreamEvent
            metaStreamEvent, Map<String, String> options, List<AttributeMapping> attributeMappingList) {

        this.outputCallback = outputCallback;
        this.streamEventConverter = new ZeroStreamEventConverter();
        this.streamEventPool = new StreamEventPool(metaStreamEvent, 5);
    }

    /**
     * Receive {@link Event} or Object[] from {@link InputTransport}, convert to {@link ComplexEventChunk} and send
     * to the
     * {@link OutputCallback}.
     *
     * @param eventObject the TEXT string
     */
    @Override
    public void onEvent(Object eventObject) {
        StreamEvent borrowedEvent = streamEventPool.borrowEvent();
        streamEventConverter.convertEvent(convertToEvent(eventObject), borrowedEvent);
        outputCallback.send(new ComplexEventChunk<StreamEvent>(borrowedEvent, borrowedEvent, true));
    }

    /**
     * Convert the given Object[] to {@link Event}. If the input is already an {@link Event}, just return it.
     *
     * @param eventObject TEXT string
     * @return the constructed Event object
     */
    private Event convertToEvent(Object eventObject) {
        Event event;
        if (eventObject == null) {
            throw new ExecutionPlanRuntimeException("Event object must be either Event or Object[] but found null");
        } else if (eventObject instanceof Event) {
            event = (Event) eventObject;
        } else if (eventObject instanceof Object[]) {
            Object[] data = (Object[]) eventObject;
            event = new Event(data.length);
            System.arraycopy(data, 0, event.getData(), 0, data.length);
        } else {
            throw new ExecutionPlanRuntimeException("Event object must be either Event or Object[] but found " +
                    eventObject.getClass().getCanonicalName());
        }

        return event;
    }
}
