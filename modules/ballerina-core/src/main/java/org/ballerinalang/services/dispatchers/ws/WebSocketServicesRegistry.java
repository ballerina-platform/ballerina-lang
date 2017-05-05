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

import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.Service;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Store all the WebSocket serviceEndpoints here.
 */
public class WebSocketServicesRegistry {

    private static final WebSocketServicesRegistry REGISTRY = new WebSocketServicesRegistry();

    // Map<interface, Map<uri, service>>
    private final Map<String, Map<String, Service>> serviceEndpoints = new ConcurrentHashMap<>();

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
    public void registerService(Service service) {
        String upgradePath = findFullWebSocketUpgradePath(service);
        String listenerInterface = getListenerInterface(service);
        boolean isClientService = isWebSocketClientService(service);
        if (upgradePath != null && !isClientService) {
            if (serviceEndpoints.containsKey(listenerInterface)) {
                serviceEndpoints.get(listenerInterface).put(upgradePath, service);
            } else {
                Map<String, Service> servicesOnInterface = new ConcurrentHashMap<>();
                servicesOnInterface.put(upgradePath, service);
                serviceEndpoints.put(listenerInterface, servicesOnInterface);
            }
        } else if (isClientService) {
            if (service.getAnnotations().length > 1) {
                throw new BallerinaException(
                        "Cannot define any other service annotation with WebSocket Client service");
            } else {
                WebSocketClientsRegistry.getInstance().addService(service);
            }
        }
    }

    /**
     * Unregister service for Map.
     *
     * @param service service to unregister.
     */
    public void unregisterService(Service service) {
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
    public Service getServiceEndpoint(String listenerInterface, String uri) {
        return serviceEndpoints.get(listenerInterface).get(uri);
    }


    private String findFullWebSocketUpgradePath(Service service) {
        AnnotationAttachment websocketUpgradePathAnnotation = null;
        AnnotationAttachment basePathAnnotation = null;
        AnnotationAttachment[] annotations = service.getAnnotations();
        for (AnnotationAttachment annotation: annotations) {
            if (annotation.getPkgName().equals(org.ballerinalang.services.dispatchers.http.Constants.PROTOCOL_HTTP) &&
                    annotation.getName().equals(
                            org.ballerinalang.services.dispatchers.http.Constants.ANNOTATION_NAME_BASE_PATH)) {
                basePathAnnotation = annotation;
            } else if (annotation.getPkgName().equals(Constants.PROTOCOL_WEBSOCKET) &&
                    annotation.getName().equals(Constants.ANNOTATION_NAME_WEBSOCKET_UPGRADE_PATH)) {
                websocketUpgradePathAnnotation = annotation;
            }
        }
        if (websocketUpgradePathAnnotation != null && websocketUpgradePathAnnotation.getValue() != null &&
                !websocketUpgradePathAnnotation.getValue().trim().isEmpty()) {
            if (basePathAnnotation == null || basePathAnnotation.getValue() == null ||
                    basePathAnnotation.getValue().trim().isEmpty()) {
                throw new BallerinaException("Cannot define @WebSocketPathUpgrade without @BasePath");
            }

            String basePath = refactorUri(basePathAnnotation.getValue());
            String websocketUpgradePath = refactorUri(websocketUpgradePathAnnotation.getValue());

            return refactorUri(basePath.concat(websocketUpgradePath));
        }
        return null;
    }

    private boolean isWebSocketClientService(Service service) {
        AnnotationAttachment[] annotations = service.getAnnotations();
        for (AnnotationAttachment annotation: annotations) {
            if (annotation.getPkgName().equals(Constants.PROTOCOL_WEBSOCKET) &&
                    annotation.getName().equals(Constants.ANNOTATION_NAME_WEBSOCKET_CLIENT_SERVICE)) {
                return true;
            }
        }
        return false;
    }

    private String getListenerInterface(Service service) {
        String listenerInterface = org.ballerinalang.services.dispatchers.http.Constants.DEFAULT_INTERFACE;
        for (AnnotationAttachment annotation : service.getAnnotations()) {
            if (annotation.getName().equals(
                    org.ballerinalang.services.dispatchers.http.Constants.ANNOTATION_NAME_SOURCE)) {
                String sourceInterfaceVal = annotation.getAttribute(
                        org.ballerinalang.services.dispatchers.http.Constants.ANNOTATION_SOURCE_KEY_INTERFACE).
                        toString();
                if (sourceInterfaceVal != null) {
                    listenerInterface = sourceInterfaceVal;
                }
            }
        }
        return listenerInterface;
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

}
