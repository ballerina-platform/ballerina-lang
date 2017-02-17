/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.services.dispatchers;

import java.util.HashMap;
import java.util.Map;

/**
 * The place where protocol specific dispatchers are stored.
 *
 * @since 0.8.0
 */
public class DispatcherRegistry {

    private Map<String, ServiceDispatcher> serviceDispatchers = new HashMap<String, ServiceDispatcher>();
    private Map<String, ResourceDispatcher> resourceDispatchers = new HashMap<String, ResourceDispatcher>();

    private static DispatcherRegistry instance = new DispatcherRegistry();

    private DispatcherRegistry() {
    }

    public static DispatcherRegistry getInstance() {
        return instance;
    }

    public ServiceDispatcher getServiceDispatcher(String protocol) {
        return serviceDispatchers.get(protocol);
    }

    public Map<String, ServiceDispatcher> getServiceDispatchers() {
        return serviceDispatchers;
    }

    public ResourceDispatcher getResourceDispatcher(String protocol) {
        return resourceDispatchers.get(protocol);
    }

    public void registerServiceDispatcher(ServiceDispatcher dispatcher) {
        serviceDispatchers.put(dispatcher.getProtocol(), dispatcher);
    }

    public void registerResourceDispatcher(ResourceDispatcher dispatcher) {
        resourceDispatchers.put(dispatcher.getProtocol(), dispatcher);
    }

    public void clearDispatchers() {
        serviceDispatchers.clear();
        resourceDispatchers.clear();
    }

    public void unregisterServiceDispatcher(String name) {
        serviceDispatchers.remove(name);
    }

    public void unregisterResourceDispatcher(String name) {
        resourceDispatchers.remove(name);
    }

}
