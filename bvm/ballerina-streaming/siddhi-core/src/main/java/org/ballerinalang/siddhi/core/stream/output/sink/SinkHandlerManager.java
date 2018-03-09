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

package org.ballerinalang.siddhi.core.stream.output.sink;

import java.util.HashMap;

/**
 * SinkHandlerManager is a factory interface that would create the appropriate {@link SinkHandler}
 * for each {@link Sink}.
 */
public abstract class SinkHandlerManager {

    /**
     * Saves a list of registered {@link SinkHandler} classes against the respective {@link Sink} elementId.
     */
    private HashMap<String, SinkHandler> registeredSinkHandlers = new HashMap<>();

    public abstract SinkHandler generateSinkHandler();

    public void registerSinkHandler(String elementId, SinkHandler sinkHandler) {
        this.registeredSinkHandlers.put(elementId, sinkHandler);
    }

    public void unregisterSinkHandler(String elementId) {
        this.registeredSinkHandlers.remove(elementId);
    }

    public HashMap<String, SinkHandler> getRegisteredSinkHandlers() {
        return registeredSinkHandlers;
    }

    public void clear() {
        registeredSinkHandlers.clear();
    }

}
