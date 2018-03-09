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

package org.ballerinalang.siddhi.core.stream.input.source;

import java.util.HashMap;

/**
 * SourceHandlerManager is a factory interface that would create the appropriate {@link SourceHandler}
 * for each {@link Source}.
 */
public abstract class SourceHandlerManager {

    /**
     * Saves a list of registered {@link SourceHandler} classes against the respective {@link Source} elementId.
     */
    private HashMap<String, SourceHandler> registeredSourceHandlers = new HashMap<>();

    public abstract SourceHandler generateSourceHandler();

    public void registerSourceHandler(String elementId, SourceHandler sourceHandler) {
        this.registeredSourceHandlers.put(elementId, sourceHandler);
    }

    public void unregisterSourceHandler(String elementId) {
        this.registeredSourceHandlers.remove(elementId);
    }

    public HashMap<String, SourceHandler> getRegsiteredSourceHandlers() {
        return this.registeredSourceHandlers;
    }

    public void clear() {
        registeredSourceHandlers.clear();
    }
}
