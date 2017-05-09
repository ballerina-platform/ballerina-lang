/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.AttributeMapping;
import org.wso2.siddhi.core.stream.input.InputEventHandler;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.snapshot.Snapshotable;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convert custom input from {@link Source} to {@link org.wso2.siddhi.core.event.ComplexEventChunk}.
 */
public abstract class SourceMapper implements SourceEventListener, Snapshotable {

    private InputEventHandler inputHandler;
    private StreamDefinition streamDefinition;
    private String mapType;
    private String elementId;
    /**
     * {@link Event#id} of the last event which is processed by this mapper
     */
    private Long lastEventId = null;
    private static final Logger log = Logger.getLogger(SourceMapper.class);

    public void init(StreamDefinition streamDefinition, String mapType, OptionHolder mapOptionHolder,
                     List<AttributeMapping> attributeMappings, ConfigReader configReader, ExecutionPlanContext
                             executionPlanContext) {
        this.streamDefinition = streamDefinition;
        this.mapType = mapType;
        this.elementId = executionPlanContext.getElementIdGenerator().createNewId();
        init(streamDefinition, mapOptionHolder, attributeMappings, configReader);
    }

    public abstract void init(StreamDefinition streamDefinition, OptionHolder optionHolder, List<AttributeMapping> attributeMappingList, ConfigReader configReader);

    public void setInputHandler(InputEventHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    public InputHandler getInputHandler() {
        return inputHandler.getInputHandler();
    }

    public void onEvent(Object eventObject) {
        try {
            if (eventObject != null) {
                mapAndProcess(eventObject);
            }
        } catch (InterruptedException e) {
            log.error("Error while processing '" + eventObject + "', for the input Mapping '" + mapType +
                    "' for the stream '" + streamDefinition.getId() + "'");
        }
    }

    public void send(Event event) throws InterruptedException {
        lastEventId = inputHandler.sendEvent(event, getLastEventId());
    }

    public void send(Event[] events) throws InterruptedException {
        lastEventId = inputHandler.sendEvents(events, getLastEventId());
    }

    public void send(Object[] objects) throws InterruptedException {
        inputHandler.getInputHandler().send(objects);
    }

    public Long getLastEventId() {
        return lastEventId;
    }

    @Override
    public Map<String, Object> currentState() {
        Map<String, Object> state = new HashMap<>();
        synchronized (lastEventId) {
            state.put("LastEventId", lastEventId);
        }
        return state;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        lastEventId = (Long) state.get("LastEventId");
    }

    @Override
    public String getElementId() {
        return elementId;
    }

    public StreamDefinition getStreamDefinition() {
        return streamDefinition;
    }

    protected abstract void mapAndProcess(Object eventObject) throws InterruptedException;

}
