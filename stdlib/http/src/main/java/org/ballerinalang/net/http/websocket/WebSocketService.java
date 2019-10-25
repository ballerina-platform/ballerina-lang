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

import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.values.ObjectValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket service for service dispatching.
 */
public class WebSocketService {

    protected final ObjectValue service;
    protected Scheduler scheduler;
    private final Map<String, AttachedFunction> resourcesMap = new ConcurrentHashMap<>();

    public WebSocketService(Scheduler scheduler) {
        this.scheduler = scheduler;
        service = null;
    }

    public WebSocketService(ObjectValue service, Scheduler scheduler) {
        this.scheduler = scheduler;
        this.service = service;
        populateResourcesMap(service);
    }

    private void populateResourcesMap(ObjectValue service) {
        for (AttachedFunction resource : service.getType().getAttachedFunctions()) {
            resourcesMap.put(resource.getName(), resource);
        }
    }

    public AttachedFunction getResourceByName(String resourceName) {
        return resourcesMap.get(resourceName);
    }

    public ObjectValue getBalService() {
        return service;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }
}
