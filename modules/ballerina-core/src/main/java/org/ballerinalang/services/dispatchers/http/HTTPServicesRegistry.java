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

import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.Service;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.util.codegen.AnnotationAttachmentInfo;
import org.ballerinalang.util.codegen.AnnotationAttributeValue;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;

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
    @Deprecated
    private final Map<String, Map<String, Service>> servicesMap = new ConcurrentHashMap<>();
    private final Map<String, Map<String, ServiceInfo>> servicesInfoMap = new ConcurrentHashMap<>();

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
    @Deprecated
    public Service getService(String interfaceId, String basepath) {
        return servicesMap.get(interfaceId).get(basepath);
    }

    /**
     * @param interfaceId interface id of the services.
     * @return the services map if exists else null.
     */
    @Deprecated
    public Map<String, Service> getServicesByInterface(String interfaceId) {
        return servicesMap.get(interfaceId);
    }

    /**
     * Register a service into the map.
     * @param service requested service to register.
     */
    @Deprecated
    public void registerService(Service service) {
        if (serviceExists(service)) {
            logger.debug("Service already exists.");
            return;
        }
        String listenerInterface = Constants.DEFAULT_INTERFACE;
        String basePath = service.getSymbolName().getName();
        for (AnnotationAttachment annotation : service.getAnnotations()) {
            if (annotation.getName().equals(Constants.ANNOTATION_NAME_SOURCE)) {
                String sourceInterfaceVal = annotation
                        .getAttribute(Constants.ANNOTATION_SOURCE_KEY_INTERFACE).toString();
                if (sourceInterfaceVal != null) {   //TODO: Filter non-http protocols
                    listenerInterface = sourceInterfaceVal;
                }
            } else if (annotation.getPkgName().equals(Constants.PROTOCOL_HTTP) &&
                       annotation.getName().equals(Constants.ANNOTATION_NAME_BASE_PATH) &&
                       annotation.getValue() != null && !annotation.getValue().trim().isEmpty()) {
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
            BallerinaConnectorManager.getInstance().addStartupDelayedServerConnector(connector);
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
    @Deprecated
    public void unregisterService(Service service) {
        String listenerInterface = Constants.DEFAULT_INTERFACE;
        // String basePath = Constants.DEFAULT_BASE_PATH;
        String basePath = service.getSymbolName().getName();

        for (AnnotationAttachment annotation : service.getAnnotations()) {
            if (annotation.getName().equals(Constants.ANNOTATION_NAME_SOURCE)) {
                String sourceInterfaceVal = annotation
                        .getAttribute(Constants.ANNOTATION_SOURCE_KEY_INTERFACE).toString();
                if (sourceInterfaceVal != null) {   //TODO: Filter non-http protocols
                    listenerInterface = sourceInterfaceVal;
                }
            } else if (annotation.getPkgName().equals(Constants.PROTOCOL_HTTP) &&
                       annotation.getName().equals(Constants.ANNOTATION_NAME_BASE_PATH) &&
                       annotation.getValue() != null && !annotation.getValue().trim().isEmpty()) {
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
     *
     * TODO : This code seems redundant. Remove this.
     * @param service requested service to check.
     * @return true if service exists.
     */
    @Deprecated
    public boolean serviceExists(Service service) {
        String listenerInterface = Constants.DEFAULT_INTERFACE;
        String basePath = service.getSymbolName().getName();
        for (AnnotationAttachment annotation : service.getAnnotations()) {
            if (annotation.getName().equals(Constants.ANNOTATION_NAME_SOURCE)) {
                String sourceInterfaceVal = annotation
                        .getAttribute(Constants.ANNOTATION_SOURCE_KEY_INTERFACE).toString();
                if (sourceInterfaceVal != null) {   //TODO: Filter non-http protocols
                    listenerInterface = sourceInterfaceVal;
                }
            } else if (annotation.getPkgName().equals(Constants.PROTOCOL_HTTP) &&
                       annotation.getName().equals(Constants.ANNOTATION_NAME_BASE_PATH) &&
                       annotation.getValue() != null && !annotation.getValue().trim().isEmpty()) {
                basePath = annotation.getValue();
            }
        }
        return servicesMap.containsKey(listenerInterface) && servicesMap.get(listenerInterface).containsKey(basePath);
    }

    /**
     * Get ServiceInfo isntance for given interface and base path.
     *
     * @param interfaceId interface id of the service.
     * @param basepath    basepath of the service.
     * @return the {@link ServiceInfo} instance if exist else null
     */
    public ServiceInfo getServiceInfo(String interfaceId, String basepath) {
        return servicesInfoMap.get(interfaceId).get(basepath);
    }

    /**
     * Get ServiceInfo map for given interfaceId.
     *
     * @param interfaceId interfaceId interface id of the services.
     * @return the serviceInfo map if exists else null.
     */
    public Map<String, ServiceInfo> getServicesInfoByInterface(String interfaceId) {
        return servicesInfoMap.get(interfaceId);
    }

    /**
     * Register a service into the map.
     *
     * @param service requested serviceInfo to be registered.
     */
    public void registerService(ServiceInfo service) {
        String listenerInterface = Constants.DEFAULT_INTERFACE;
        String basePath = service.getName();
        AnnotationAttachmentInfo annotationInfo = service.getAnnotationAttachmentInfo(Constants
                .HTTP_PACKAGE_PATH, Constants.ANNOTATION_NAME_BASE_PATH);

        if (annotationInfo != null) {
            AnnotationAttributeValue annotationAttributeValue = annotationInfo.getAnnotationAttributeValue
                    (Constants.VALUE_ATTRIBUTE);
            if (annotationAttributeValue != null && annotationAttributeValue.getStringValue() != null &&
                    !annotationAttributeValue.getStringValue().trim().isEmpty()) {
                basePath = annotationAttributeValue.getStringValue();
            }
        }

        if (!basePath.startsWith(Constants.DEFAULT_BASE_PATH)) {
            basePath = Constants.DEFAULT_BASE_PATH.concat(basePath);
        }

        Map<String, ServiceInfo> servicesOnInterface = servicesInfoMap.get(listenerInterface);
        if (servicesOnInterface == null) {
            // Assumption : this is always sequential, no two simultaneous calls can get here
            servicesOnInterface = new HashMap<>();
            servicesInfoMap.put(listenerInterface, servicesOnInterface);
            ServerConnector connector = BallerinaConnectorManager.getInstance().getServerConnector(listenerInterface);
            if (connector == null) {
                throw new BallerinaException(
                        "ServerConnector interface not registered for : " + listenerInterface);
            }
            // Delay the startup until all services are deployed
            BallerinaConnectorManager.getInstance().addStartupDelayedServerConnector(connector);
        }
        if (servicesOnInterface.containsKey(basePath)) {
            throw new BallerinaException(
                    "service with base path :" + basePath + " already exists in listener : " + listenerInterface);
        }

        servicesOnInterface.put(basePath, service);

        logger.info("Service deployed : " + service.getName() + " with context " + basePath);
    }

    /**
     * Removing service from the service registry.
     * @param service requested service to be removed.
     */
    public void unregisterService(ServiceInfo service) {
        String listenerInterface = Constants.DEFAULT_INTERFACE;
        String basePath = service.getName();
        AnnotationAttachmentInfo annotationInfo = service.getAnnotationAttachmentInfo(Constants
                .HTTP_PACKAGE_PATH, Constants.BASE_PATH);

        if (annotationInfo != null) {
            AnnotationAttributeValue annotationAttributeValue = annotationInfo.getAnnotationAttributeValue
                    (Constants.VALUE_ATTRIBUTE);
            if (annotationAttributeValue != null && annotationAttributeValue.getStringValue() != null &&
                    !annotationAttributeValue.getStringValue().trim().isEmpty()) {
                basePath = annotationAttributeValue.getStringValue();
            }
        }

        if (!basePath.startsWith(Constants.DEFAULT_BASE_PATH)) {
            basePath = Constants.DEFAULT_BASE_PATH.concat(basePath);
        }

        Map<String, ServiceInfo> servicesOnInterface = servicesInfoMap.get(listenerInterface);
        if (servicesOnInterface != null) {
            servicesOnInterface.remove(basePath);
            if (servicesOnInterface.isEmpty()) {
                servicesInfoMap.remove(listenerInterface);
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
}
