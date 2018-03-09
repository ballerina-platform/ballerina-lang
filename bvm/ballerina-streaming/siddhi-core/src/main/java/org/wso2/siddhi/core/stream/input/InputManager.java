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

package org.wso2.siddhi.core.stream.input;

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.DefinitionNotExistException;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Manager class to handle {@link org.wso2.siddhi.core.event.Event} insertion to Siddhi.
 */
public class InputManager {

    private final InputEntryValve inputEntryValve;
    private Map<String, InputHandler> inputHandlerMap = new LinkedHashMap<String, InputHandler>();
    private Map<String, StreamJunction> streamJunctionMap;
    private InputDistributor inputDistributor;

    public InputManager(SiddhiAppContext siddhiAppContext,
                        ConcurrentMap<String, AbstractDefinition> streamDefinitionMap,
                        ConcurrentMap<String, StreamJunction> streamJunctionMap) {
        this.streamJunctionMap = streamJunctionMap;
        this.inputDistributor = new InputDistributor();
        this.inputEntryValve = new InputEntryValve(siddhiAppContext, inputDistributor);
    }

    public InputHandler getInputHandler(String streamId) {
        InputHandler inputHandler = inputHandlerMap.get(streamId);
        if (inputHandler == null) {
            return constructInputHandler(streamId);
        } else {
            return inputHandler;
        }
    }

    public synchronized void disconnect() {
        for (InputHandler inputHandler : inputHandlerMap.values()) {
            inputHandler.disconnect();
        }
        inputHandlerMap.clear();
    }

    public InputHandler constructInputHandler(String streamId) {
        InputHandler inputHandler = new InputHandler(streamId, inputHandlerMap.size(), inputEntryValve);
        StreamJunction streamJunction = streamJunctionMap.get(streamId);
        if (streamJunction == null) {
            throw new DefinitionNotExistException("Stream with stream ID " + streamId + " has not been defined");
        }
        inputDistributor.addInputProcessor(streamJunctionMap.get(streamId).constructPublisher());
        inputHandlerMap.put(streamId, inputHandler);
        return inputHandler;
    }
}
