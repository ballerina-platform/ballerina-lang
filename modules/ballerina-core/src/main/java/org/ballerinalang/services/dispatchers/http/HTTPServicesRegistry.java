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
 *
 */

package org.ballerinalang.services.dispatchers.http;

import org.ballerinalang.model.Annotation;
import org.ballerinalang.model.Service;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This services registry holds all the services of HTTP + WebSocket.
 * This is a singleton class where all HTTP + WebSocket Dispatchers can access.
 *
 * @since 0.8
 */
public class HTTPServicesRegistry {

    private static final Logger logger = LoggerFactory.getLogger(HTTPServicesRegistry.class);

    // Outer Map key=interface, Inner Map key=basePath
    private final Map<String, Map<String, Service>> servicesMap = new ConcurrentHashMap<>();
    private static final HTTPServicesRegistry servicesRegistry = new HTTPServicesRegistry();

    private HTTPServicesRegistry() {
    }

    public static HTTPServicesRegistry getInstance() {
        return servicesRegistry;
    }

    /**
     * @param interfaceId interface id of the service.
     * @param basepath basepath of the service.
     * @return the {@link Service} is exists else null.
     */
    public Service getService(String interfaceId, String basepath) {
        return servicesMap.get(interfaceId).get(basepath);
    }

    /**
     * @param interfaceId interface id of the services.
     * @return the services map if exists else null.
     */
    public Map<String, Service> getServicesByInterface(String interfaceId) {
        return servicesMap.get(interfaceId);
    }

    /**
     * Register a service into the map.
     * @param service requested service to register.
     */
    public void registerService(Service service) {
        if (serviceExists(service)) {
            logger.debug("Service already exists.");
            return;
        }
        String listenerInterface = Constants.DEFAULT_INTERFACE;
        String basePath = service.getSymbolName().getName();
        for (Annotation annotation : service.getAnnotations()) {
            if (annotation.getName().equals(Constants.ANNOTATION_NAME_SOURCE)) {
                String sourceInterfaceVal = annotation
                        .getValueOfElementPair(new SymbolName(Constants.ANNOTATION_SOURCE_KEY_INTERFACE));
                if (sourceInterfaceVal != null) {   //TODO: Filter non-http protocols
                    listenerInterface = sourceInterfaceVal;
                }
            } else if (annotation.getName().equals(
                    Constants.PROTOCOL_HTTP + ":" + Constants.ANNOTATION_NAME_BASE_PATH)) {
                basePath = annotation.getValue();
            }
        }

        if (!basePath.startsWith(Constants.DEFAULT_BASE_PATH)) {
            basePath = Constants.DEFAULT_BASE_PATH.concat(basePath);
        }

        Map<String, Service> servicesOnInterface = servicesMap.get(listenerInterface);
        if (servicesOnInterface == null) {
            // Assumption : this is always sequential, no two simultaneous calls can get here
            servicesOnInterface = new HashMap<>();
            servicesMap.put(listenerInterface, servicesOnInterface);
            ServerConnector connector = BallerinaConnectorManager.getInstance().getServerConnector(listenerInterface);
            if (connector == null) {
                throw new BallerinaException(
                        "ServerConnector interface not registered for : " + listenerInterface);
            }
            // Delay the startup until all services are deployed
            BallerinaConnectorManager.getInstance().addStartupDelayedServerConnector(connector,
                                                                                     Collections.emptyMap());
        }
        if (servicesOnInterface.containsKey(basePath)) {
            throw new BallerinaException(
                    "service with base path :" + basePath + " already exists in listener : " + listenerInterface);
        }

        servicesOnInterface.put(basePath, service);

        logger.info("Service deployed : " +
                         (service.getSymbolName().getPkgPath() != null ?
                                 service.getSymbolName().getPkgPath() + ":" : "") +
                         service.getSymbolName().getName() +
                         " with context " +  basePath);
    }

    /**
     * Removing service from the service registry.
     * @param service requested service to be removed.
     */
    public void unregisterService(Service service) {
        String listenerInterface = Constants.DEFAULT_INTERFACE;
        // String basePath = Constants.DEFAULT_BASE_PATH;
        String basePath = service.getSymbolName().getName();

        for (Annotation annotation : service.getAnnotations()) {
            if (annotation.getName().equals(Constants.ANNOTATION_NAME_SOURCE)) {
                String sourceInterfaceVal = annotation
                        .getValueOfElementPair(new SymbolName(Constants.ANNOTATION_SOURCE_KEY_INTERFACE));
                if (sourceInterfaceVal != null) {   //TODO: Filter non-http protocols
                    listenerInterface = sourceInterfaceVal;
                }
            } else if (annotation.getName().equals(
                    Constants.PROTOCOL_HTTP + ":" + Constants.ANNOTATION_NAME_BASE_PATH)) {
                basePath = annotation.getValue();
            }
        }


        if (!basePath.startsWith(Constants.DEFAULT_BASE_PATH)) {
            basePath = Constants.DEFAULT_BASE_PATH.concat(basePath);
        }

        Map<String, Service> servicesOnInterface = servicesMap.get(listenerInterface);
        if (servicesOnInterface != null) {
            servicesOnInterface.remove(basePath);
            if (servicesOnInterface.isEmpty()) {
                servicesMap.remove(listenerInterface);
                ServerConnector connector =
                        BallerinaConnectorManager.getInstance().getServerConnector(listenerInterface);
                if (connector != null) {
                    try {
                        connector.stop();
                    } catch (ServerConnectorException e) {
                        throw new BallerinaException("Cannot stop the connector for the interface : " +
                                                             listenerInterface, e);
                    }
                }
            }
        }
    }

    /**
     * Indicate the service exists already.
     * @param service requested service to check.
     * @return true if service exists.
     */
    public boolean serviceExists(Service service) {
        String listenerInterface = Constants.DEFAULT_INTERFACE;
        String basePath = service.getSymbolName().getName();
        for (Annotation annotation : service.getAnnotations()) {
            if (annotation.getName().equals(Constants.ANNOTATION_NAME_SOURCE)) {
                String sourceInterfaceVal = annotation
                        .getValueOfElementPair(new SymbolName(Constants.ANNOTATION_SOURCE_KEY_INTERFACE));
                if (sourceInterfaceVal != null) {   //TODO: Filter non-http protocols
                    listenerInterface = sourceInterfaceVal;
                }
            } else if (annotation.getName().equals(
                    Constants.PROTOCOL_HTTP + ":" + Constants.ANNOTATION_NAME_BASE_PATH)) {
                basePath = annotation.getValue();
            }
        }
        return servicesMap.containsKey(listenerInterface) && servicesMap.get(listenerInterface).containsKey(basePath);
    }
}
