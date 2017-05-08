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

package org.ballerinalang.services.dispatchers.ws;

import org.ballerinalang.model.Service;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for WebSocket Client Services.
 */
public class WebSocketClientServicesRegistry {

    private static final WebSocketClientServicesRegistry clientServicesRegistry = new WebSocketClientServicesRegistry();

    // Map <serviceName, service>
    private final Map<String, Service> serviceMap = new ConcurrentHashMap<>();
    // Map <clientID, serviceName>
    private final Map<String, String> clientIDMap = new ConcurrentHashMap<>();

    private WebSocketClientServicesRegistry() {
    }

    public static WebSocketClientServicesRegistry getInstance() {
        return clientServicesRegistry;
    }
    /**
     * Find the service requested which is mapped to service name.
     *
     * @param serviceName name of the service.
     * @return service if exists else return null.
     */
    public Service getServiceByName(String serviceName) {
        return serviceMap.get(serviceName);
    }

    /**
     * Get the service requested by which is mapped to client ID.
     *
     * @param clientID ID of the client.
     * @return service if exists else return null.
     */
    public Service getServiceByClientID(String clientID) {
        String serviceName = clientIDMap.get(clientID);
        if (serviceName == null) {
            return null;
        }
        return serviceMap.get(serviceName);
    }

    /**
     * Maps the client ID to the service name.
     *
     * @param clientID DI of the client.
     * @param serviceName name of the service.
     */
    public void mapClientIdToServiceName(String clientID, String serviceName) throws ClientConnectorException {
        if (serviceMap.get(serviceName) == null) {
            throw new ClientConnectorException("Cannot find the client service: " + serviceName);
        }
        clientIDMap.put(clientID, serviceName);
    }

    /**
     * Add service to the registry.
     *
     * @param service service which need to be added to the registry.
     */
    public void addService(Service service) {
        serviceMap.put(service.getName(), service);
    }

    /**
     * Remove a service from the registry.
     *
     * @param serviceName name of the service which need to be removed.
     */
    public void removeService(String serviceName) {
        serviceMap.remove(serviceName);
    }

    /**
     * Check whether the service exists.
     *
     * @param serviceName name of the service to be checked.
     * @return true if the service exists.
     */
    public boolean serviceExists(String serviceName) {
        return serviceMap.containsKey(serviceName);
    }
}
