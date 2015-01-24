/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.wso2.siddhi.core.stream.input;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.DefinitionNotExistException;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class InputManager {

    private ExecutionPlanContext executionPlanContext;
    private Map<String, InputHandler> inputHandlerMap = new LinkedHashMap<String, InputHandler>();
    private Map<String, AbstractDefinition> streamDefinitionMap;
    private Map<String, StreamJunction> streamJunctionMap;
    private InputDistributor inputDistributor;
    private SingleStreamEntryValve singleStreamEntryValve;
    private SingleThreadEntryValve singleThreadEntryValve;

    public InputManager(ExecutionPlanContext executionPlanContext,
                        ConcurrentMap<String, AbstractDefinition> streamDefinitionMap,
                        ConcurrentMap<String, StreamJunction> streamJunctionMap) {
        this.executionPlanContext = executionPlanContext;
        this.streamDefinitionMap = streamDefinitionMap;
        this.streamJunctionMap = streamJunctionMap;
        if (!executionPlanContext.isPlayback() &&
                !executionPlanContext.isEnforceOrder() &&
                !executionPlanContext.isParallel()) {
            inputDistributor = new InputDistributor();
            singleThreadEntryValve = new SingleThreadEntryValve(executionPlanContext, inputDistributor);
            singleStreamEntryValve = new SingleStreamEntryValve(executionPlanContext, singleThreadEntryValve);
        }

    }

    public InputHandler getInputHandler(String streamId) {
        InputHandler inputHandler = inputHandlerMap.get(streamId);
        if (inputHandler == null) {
            return constructInputHandler(streamId);
        } else {
            return inputHandler;
        }
    }

    public synchronized void startProcessing() {
        if (singleStreamEntryValve != null) {
            singleStreamEntryValve.startProcessing();
        }
    }

    public synchronized void stopProcessing() {
        for (InputHandler inputHandler : inputHandlerMap.values()) {
            inputHandler.disconnect();
        }
        if (singleStreamEntryValve != null) {
            singleStreamEntryValve.stopProcessing();
        }
        inputHandlerMap.clear();

    }

    public InputHandler constructInputHandler(String streamId) {

        InputHandler inputHandler = null;
        if (singleStreamEntryValve != null) {
            inputHandler = new InputHandler(streamId, inputHandlerMap.size(), singleStreamEntryValve);
            StreamJunction streamJunction = streamJunctionMap.get(streamId);
            if (streamJunction == null) {
                throw new DefinitionNotExistException("Stream with stream ID " + streamId + " has not been defined");
            }
            inputDistributor.addInputProcessor(streamJunctionMap.get(streamId).constructPublisher());
        } else {
            //todo handle
        }
        inputHandlerMap.put(streamId, inputHandler);
        return inputHandler;
    }
}
