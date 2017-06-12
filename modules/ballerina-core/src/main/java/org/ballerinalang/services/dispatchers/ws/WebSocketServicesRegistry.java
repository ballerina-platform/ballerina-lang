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

import org.ballerinalang.util.codegen.AnnotationAttachmentInfo;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Store all the WebSocket serviceEndpoints here.
 */
public class WebSocketServicesRegistry {

    private static final WebSocketServicesRegistry REGISTRY = new WebSocketServicesRegistry();

    // Map<interface, Map<uri, service>>
    private final Map<String, Map<String, ServiceInfo>> serviceEndpoints = new ConcurrentHashMap<>();
    // Map<clientServiceName, ClientService>
    private final Map<String, ServiceInfo> clientServices = new ConcurrentHashMap<>();

    private WebSocketServicesRegistry() {
    }

    public static WebSocketServicesRegistry getInstance() {
        return REGISTRY;
    }

    /**
     * Register the service. Check for WebSocket upgrade path and client service.
     *
     * @param service service to register.
     */
    public void registerService(ServiceInfo service) {
        String upgradePath = findFullWebSocketUpgradePath(service);
        String listenerInterface = getListenerInterface(service);
        boolean isClientService = isWebSocketClientService(service);
        if (upgradePath != null && !isClientService) {
            if (serviceEndpoints.containsKey(listenerInterface)) {
                serviceEndpoints.get(listenerInterface).put(upgradePath, service);
            } else {
                Map<String, ServiceInfo> servicesOnInterface = new ConcurrentHashMap<>();
                servicesOnInterface.put(upgradePath, service);
                serviceEndpoints.put(listenerInterface, servicesOnInterface);
            }
        } else if (isClientService) {
            if (service.getAnnotationAttachmentInfoList().size() > 1) {
                throw new BallerinaException(
                        "Cannot define any other service annotation with WebSocket Client service");
            } else {
                clientServices.put(service.getName(), service);
            }
        }
    }

    /**
     * Unregister service for Map.
     *
     * @param service service to unregister.
     */
    public void unregisterService(ServiceInfo service) {
        String upgradePath = findFullWebSocketUpgradePath(service);
        String listenerInterface = getListenerInterface(service);
        if (serviceEndpoints.containsKey(listenerInterface)) {
            serviceEndpoints.get(listenerInterface).remove(upgradePath);
        }
    }

    /**
     * Find the best matching service.
     *
     * @param listenerInterface Listener interface of the the service.
     * @param uri uri of the service.
     * @return the service which matches.
     */
    public ServiceInfo getServiceEndpoint(String listenerInterface, String uri) {
        return serviceEndpoints.get(listenerInterface).get(uri);
    }

    /**
     * Retrieve the client service by service name if exists.
     *
     * @param serviceName name of the service.
     * @return the service by service name if exists. Else return null.
     */
    public ServiceInfo getClientService(String serviceName) {
        return clientServices.get(serviceName);
    }

    /**
     * Check whether the given service name is a client service or not.
     *
     * @param serviceName name of the service.
     * @return true if the service given by service name is a client service. Else return false.
     */
    public boolean isClientService(String serviceName) {
        return clientServices.containsKey(serviceName);
    }

    /**
     * Check whether the given service is a WebSocket server endpoint.
     *
     * @param serviceName name of the service.
     * @return true if the service given by service name is a client service. Else return false.
     */
    public boolean isWebSocketServerEndpoint(String serviceName) {
        for (Map.Entry<String, Map<String, ServiceInfo>> endpoints: serviceEndpoints.entrySet()) {
            if (endpoints.getValue().containsKey(serviceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Refactor the given URI.
     *
     * @param uri URI to refactor.
     * @return refactored URI.
     */
    public String refactorUri(String uri) {
        if (uri.startsWith("\"")) {
            uri = uri.substring(1, uri.length() - 1);
        }

        if (!uri.startsWith("/")) {
            uri = "/".concat(uri);
        }

        if (uri.endsWith("/")) {
            uri = uri.substring(0, uri.length() - 1);
        }
        return uri;
    }

    private String findFullWebSocketUpgradePath(ServiceInfo service) {
        AnnotationAttachmentInfo websocketUpgradePathAnnotation = service.getAnnotationAttachmentInfo(
                Constants.WS_PACKAGE_PATH, Constants.ANNOTATION_NAME_WEBSOCKET_UPGRADE_PATH);
        AnnotationAttachmentInfo basePathAnnotation = service.getAnnotationAttachmentInfo(Constants.HTTP_PACKAGE_PATH,
                Constants.ANNOTATION_NAME_BASE_PATH);

        if (websocketUpgradePathAnnotation != null &&
                websocketUpgradePathAnnotation.getAnnotationAttributeValue(Constants.VALUE_ATTRIBUTE) != null) {
            String value = websocketUpgradePathAnnotation.getAnnotationAttributeValue(Constants.VALUE_ATTRIBUTE).
                    getStringValue();
            if (value != null && !value.trim().isEmpty()) {

                if (basePathAnnotation == null ||
                        basePathAnnotation.getAnnotationAttributeValue(Constants.VALUE_ATTRIBUTE) == null ||
                        basePathAnnotation.getAnnotationAttributeValue(Constants.VALUE_ATTRIBUTE).getStringValue()
                                .trim().isEmpty()) {
                    throw new BallerinaException("Cannot define @WebSocketPathUpgrade without @BasePath");
                }
                String basePath = refactorUri(basePathAnnotation.
                        getAnnotationAttributeValue(Constants.VALUE_ATTRIBUTE).getStringValue());
                String websocketUpgradePath = refactorUri(value);
                return refactorUri(basePath.concat(websocketUpgradePath));
            }
        }
        return null;
    }

    private boolean isWebSocketClientService(ServiceInfo service) {
        AnnotationAttachmentInfo annotation = service.
                getAnnotationAttachmentInfo(Constants.WS_PACKAGE_PATH,
                                            Constants.ANNOTATION_NAME_WEBSOCKET_CLIENT_SERVICE);
        if (annotation == null) {
            return false;
        }
        return true;
    }

    private String getListenerInterface(ServiceInfo service) {
        // TODO : Handle correct interface addition to default interface.
        String listenerInterface = org.ballerinalang.services.dispatchers.http.Constants.DEFAULT_INTERFACE;
        return listenerInterface;
    }

}
