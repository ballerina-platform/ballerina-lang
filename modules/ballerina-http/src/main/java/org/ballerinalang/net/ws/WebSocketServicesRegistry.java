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

package org.ballerinalang.net.ws;

import org.ballerinalang.connector.api.AnnAttrValue;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.net.http.HttpConnectionManager;
import org.ballerinalang.net.http.HttpService;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Store all the WebSocket serviceEndpoints here.
 */
public class WebSocketServicesRegistry {

    private static final WebSocketServicesRegistry REGISTRY = new WebSocketServicesRegistry();

    // Map<interface, Map<uri, service>>
    private final Map<String, Map<String, WebSocketService>> serviceEndpoints = new ConcurrentHashMap<>();
    // Map<clientServiceName, ClientService>
    private final Map<String, ClientServiceInfo> clientServices = new ConcurrentHashMap<>();

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
    public void registerServiceEndpoint(WebSocketService service) {
        boolean isClientService = isWebSocketClientService(service);
        if (isClientService) {
            registerClientService(service);
        } else {

            String upgradePath = findFullWebSocketUpgradePath(service);
            // TODO: Add properties to propMap after adding config annotation to WebSocket.
            Set<ListenerConfiguration> listenerConfigurationSet =
                    HttpConnectionManager.getInstance().getDefaultListenerConfiugrationSet();

            for (ListenerConfiguration listenerConfiguration : listenerConfigurationSet) {
                String entryListenerInterface = listenerConfiguration.getHost() + ":" +
                        listenerConfiguration.getPort();
                Map<String, WebSocketService> servicesOnInterface = serviceEndpoints
                        .computeIfAbsent(entryListenerInterface, k -> new HashMap<>());
                servicesOnInterface.put(upgradePath, service);
                HttpConnectionManager.getInstance().createHttpServerConnector(listenerConfiguration);
            }
        }
    }

    /**
     * Register a service as a client service.
     *
     * @param clientService {@link HttpService} of the client service.
     */
    public void registerClientService(WebSocketService clientService) {
        boolean isClientService = isWebSocketClientService(clientService);
        if (isClientService) {

            if (clientServices.containsKey(clientService.getName())) {
                clientServices.get(clientService.getName()).setClientService(clientService);
            } else {
                ClientServiceInfo clientServiceInfo = new ClientServiceInfo(clientService);
                clientServices.put(clientService.getName(), clientServiceInfo);
            }
        } else {
            throw new BallerinaConnectorException("Cannot register as a client service");
        }
    }


    /**
     * Set the parent service for a given client service.
     *
     * @param clientServiceName name of the client service.
     * @param parentService parent service of the given client service.
     */
    public void setParentServiceToClientService(String clientServiceName, ServiceInfo parentService) {
        if (clientServices.containsKey(clientServiceName)) {
            clientServices.get(clientServiceName).setParentService(parentService);
        } else {
            ClientServiceInfo clientServiceInfo = new ClientServiceInfo();
            clientServiceInfo.setParentService(parentService);
            clientServices.put(clientServiceName, clientServiceInfo);
        }
    }

    /**
     * Unregister service for Map.
     *
     * @param service service to unregister.
     */
    public void unregisterService(WebSocketService service) {
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
    public WebSocketService getServiceEndpoint(String listenerInterface, String uri) {
        return serviceEndpoints.get(listenerInterface).get(uri);
    }

    /**
     * Retrieve the client service by service name if exists.
     *
     * @param serviceName name of the service.
     * @return the service by service name if exists. Else return null.
     */
    public WebSocketService getClientService(String serviceName) {
        if (clientServices.containsKey(serviceName)) {
            return clientServices.get(serviceName).getClientService();
        }

        return null;
    }

    public ServiceInfo getParentServiceOfClientService(ServiceInfo service) {
        String clientServiceName = service.getName();
        if (clientServices.containsKey(clientServiceName)) {
            return clientServices.get(clientServiceName).getParentService();
        }

        return null;
    }
    /**
     * Check whether the given service name is a client service or not.
     *
     * @param service {@link HttpService} of the service.
     * @return true if the service given by service name is a client service. Else return false.
     */
    public boolean isClientService(ServiceInfo service) {
        return clientServices.containsKey(service.getName());
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

    /**
     * Find the Full path for WebSocket upgrade.
     *
     * @param service {@link HttpService} which the full path should be found.
     * @return the full path of the WebSocket upgrade.
     */
    private String findFullWebSocketUpgradePath(WebSocketService service) {
        // Find Base path for WebSocket

        Annotation configAnnotation = service.getAnnotation(Constants.WEBSOCKET_PACKAGE_NAME, Constants.ANN_NAME_CONFIG);
        String serviceName = service.getName();
        String basePath;
        if (configAnnotation != null) {
            AnnAttrValue annotationAttributeBasePathValue = configAnnotation.getAnnAttrValue
                    (Constants.ANN_CONFIG_ATTR_BASE_PATH);
            if (annotationAttributeBasePathValue != null && annotationAttributeBasePathValue.getStringValue() != null &&
                    !annotationAttributeBasePathValue.getStringValue().trim().isEmpty()) {
                basePath = annotationAttributeBasePathValue.getStringValue();
            } else {
                throw new BallerinaConnectorException("Cannot define WebSocket endpoint without BasePath for service: "
                                                     + serviceName);
            }
        } else {
            throw new BallerinaConnectorException("Cannot define WebSocket endpoint without BasePath for service: "
                                                 + serviceName);
        }

        return refactorUri(basePath);
    }

    /**
     * Find out the given service is a WebSocket client service or not.
     *
     * @param service {@link HttpService} which should be identified.
     * @return true if the given service is a client service.
     */
    private boolean isWebSocketClientService(WebSocketService service) {
        Annotation annotation = service.getAnnotation(
                Constants.WEBSOCKET_PACKAGE_NAME, Constants.ANNOTATION_NAME_WEBSOCKET_CLIENT_SERVICE);
        return !(annotation == null);
    }

    /**
     * Find the listener interface of a given service.
     *
     * @param service {@link HttpService} which the listener interface should be found.
     * @return the listener interface of the service.
     */
    private String getListenerInterface(WebSocketService service) {
        // TODO : Handle correct interface addition to default interface.
        String listenerInterface = Constants.DEFAULT_INTERFACE;
        return listenerInterface;
    }

    /**
     * This class holds the necessary details of a WebSocket client service.
     */
    private static class ClientServiceInfo {
        private WebSocketService clientService;
        private ServiceInfo parentService;

        private ClientServiceInfo() {
        }

        private ClientServiceInfo(WebSocketService clientService) {
            this.clientService = clientService;
        }

        private WebSocketService getClientService() {
            return clientService;
        }

        private void setClientService(WebSocketService clientService) {
            this.clientService = clientService;
        }

        private void setParentService(ServiceInfo parentService) {
            this.parentService = parentService;
        }

        private ServiceInfo getParentService() {
            return parentService;
        }

    }

}
