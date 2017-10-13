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

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

/**
 * SourceHandler is an optional implementable class that wraps {@link org.wso2.siddhi.core.stream.input.InputHandler}.
 * It will do optional processing to the events before sending the events to the input handler
 */
public abstract class SourceHandler implements InputEventHandler {

    private Source source;
    private String sourceElementId;
    private StreamDefinition streamDefinition;
    private SiddhiAppContext siddhiAppContext;
    private SourceMapper sourceMapper;
    private InputEventHandlerImpl inputEventHandlerImpl;

    public void init(Source source, String sourceElementId, StreamDefinition streamDefinition,
                     SiddhiAppContext siddhiAppContext, SourceMapper sourceMapper) {
        this.source = source;
        this.sourceElementId = sourceElementId;
        this.streamDefinition = streamDefinition;
        this.siddhiAppContext = siddhiAppContext;
        this.sourceMapper = sourceMapper;
    }

    public final void setInputEventHandlerImpl(InputEventHandlerImpl inputEventHandlerImpl) {
        this.inputEventHandlerImpl = inputEventHandlerImpl;
    }

    @Override
    public void sendEvents(Event[] events) throws InterruptedException {
        Event[] handledEvent = handle(events);
        if (handledEvent != null) {
            inputEventHandlerImpl.sendEvents(handledEvent);
        }
    }

    @Override
    public void sendEvent(Event event) throws InterruptedException {
        Event handledEvent = handle(event);
        if (handledEvent != null) {
            inputEventHandlerImpl.sendEvent(handledEvent);
        }
    }

    public abstract Event handle(Event event);

    public abstract Event[] handle(Event[] events);

    public String getSourceElementId() {
        return sourceElementId;
    }

    public Source getSource() {
        return source;
    }

    public StreamDefinition getStreamDefinition() {
        return streamDefinition;
    }

    public SiddhiAppContext getSiddhiAppContext() {
        return siddhiAppContext;
    }

    public SourceMapper getSourceMapper() {
        return sourceMapper;
    }
}
