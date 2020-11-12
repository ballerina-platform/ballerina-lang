/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.http.websocket;

import io.ballerina.runtime.api.BRuntime;
import io.ballerina.runtime.api.types.AttachedFunctionType;
import io.ballerina.runtime.api.values.BObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket service for service dispatching.
 */
public class WebSocketService {

    protected final BObject service;
    protected BRuntime runtime;
    private final Map<String, AttachedFunctionType> resourcesMap = new ConcurrentHashMap<>();

    public WebSocketService(BRuntime runtime) {
        this.runtime = runtime;
        service = null;
    }

    public WebSocketService(BObject service, BRuntime runtime) {
        this.runtime = runtime;
        this.service = service;
        populateResourcesMap(service);
    }

    private void populateResourcesMap(BObject service) {
        for (AttachedFunctionType resource : service.getType().getAttachedFunctions()) {
            resourcesMap.put(resource.getName(), resource);
        }
    }

    public AttachedFunctionType getResourceByName(String resourceName) {
        return resourcesMap.get(resourceName);
    }

    public BObject getBalService() {
        return service;
    }

    public BRuntime getRuntime() {
        return runtime;
    }
}
