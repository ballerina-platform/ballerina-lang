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
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.uri.URITemplate;
import org.ballerinalang.net.uri.URITemplateException;
import org.ballerinalang.net.uri.parser.Literal;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Store all the WebSocket serviceEndpointsMap here.
 */
public class WebSocketServicesRegistry {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServicesRegistry.class);
    private static final WebSocketServicesRegistry REGISTRY = new WebSocketServicesRegistry();

    // Map <interface, URITemplate>
    private final Map<String, URITemplate<WebSocketService, WebSocketMessage>> serviceEndpointsMap
            = new ConcurrentHashMap<>();
    // Map<clientServiceName, ClientService>
    private final Map<String, WebSocketService> clientServices = new ConcurrentHashMap<>();
    // Map<ServiceName, List<BasePath>>
    private final Map<String, List<String>> serviceBoundedURIMap = new HashMap<>();

    // Map<ServiceEndpointName, ServiceEndpoint>
    private final Map<String, WebSocketService> serviceEndpoints = new ConcurrentHashMap<>();
    // Map<serviceInterface, Map<uri, serviceName>>
    private final Map<String, Map<String, String>> slaveEndpoints = new HashMap<>();

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
    public void registerService(WebSocketService service) {
        if (WebSocketServiceValidator.isWebSocketClientService(service)
                && WebSocketServiceValidator.validateClientService(service)) {
            registerClientService(service);
        } else {
            if (WebSocketServiceValidator.validateServiceEndpoint(service)) {
                serviceEndpoints.put(service.getName(), service);
            }
        }
    }

    public void registerServiceByName(String serviceInterface, String uri, String serviceName) {
        Map<String, String> servicesOnInterface = slaveEndpoints
                .computeIfAbsent(serviceInterface, k -> new HashMap<String, String>());
        servicesOnInterface.put(uri, serviceName);
    }

    public void deployServices() {
        // Deploying slave services.
        Set<WebSocketService> deployedSlaveServiceSet = deploySlaveServices();
        deployServiceEndpoints(deployedSlaveServiceSet);
    }

    private void deployServiceEndpoints(Set<WebSocketService> deployedSlaveServiceSet) {
        serviceEndpoints.entrySet().forEach(serviceEntry -> {
            WebSocketService service = serviceEntry.getValue();
            Annotation configAnnotation =
                    service.getAnnotation(Constants.WEBSOCKET_PACKAGE_NAME, Constants.ANNOTATION_CONFIGURATION);
            if (configAnnotation == null && !deployedSlaveServiceSet.contains(service)) {
                String errorMsg =
                        String.format("Service %s is without configuration annotation and also not referred " +
                                              "in HTTP service cannot be deployed.", service.getName());
                throw new BallerinaConnectorException(errorMsg);
            }
            String basePath = findFullWebSocketUpgradePath(service);
            if (basePath == null) {
                if (deployedSlaveServiceSet.contains(service)) {
                    return;
                }
                String errorMsg =
                        String.format("Service %s is without base path in configuration annotation and also " +
                                              "not referred in HTTP service cannot be deployed.",
                                      service.getName());
                throw new BallerinaConnectorException(errorMsg);
            }
            Set<ListenerConfiguration> listenerConfigurationSet =
                    HttpUtil.getDefaultOrDynamicListenerConfig(configAnnotation);
            listenerConfigurationSet.forEach(listenerConfiguration -> {
                String entryListenerInterface =
                        listenerConfiguration.getHost() + ":" + listenerConfiguration.getPort();
                try {
                    addUriTemplate(entryListenerInterface, basePath, service);
                } catch (URITemplateException e) {
                    throw new BallerinaConnectorException("Invalid URI template");
                }
                HttpConnectionManager.getInstance().createHttpServerConnector(listenerConfiguration);
            });
            List<String> uriList = serviceBoundedURIMap.computeIfAbsent(service.getName(), k -> new ArrayList<>());
            uriList.add(basePath);

            logger.info("Service deployed : " + service.getName() + " with context " + basePath);
        });
        deployedSlaveServiceSet.clear();
        serviceEndpoints.clear();
    }

    private Set<WebSocketService> deploySlaveServices() {
        Set<WebSocketService> deployedServiceSet = new LinkedHashSet<>();
        slaveEndpoints.entrySet().forEach(slaveEntry -> {
            String serviceInterface = slaveEntry.getKey();
            slaveEntry.getValue().entrySet().forEach(serviceEntry -> {
                String uri = refactorUri(serviceEntry.getKey());
                String serviceName = serviceEntry.getValue();
                if (!serviceEndpoints.containsKey(serviceName)) {
                    throw new BallerinaConnectorException("Could not find a WebSocket service for " +
                                                                  "the service name: " + serviceName);
                }
                WebSocketService service = serviceEndpoints.get(serviceName);
                try {
                    addUriTemplate(serviceInterface, uri, service);
                    List<String> uriList = serviceBoundedURIMap.
                            computeIfAbsent(service.getName(), k -> new ArrayList<>());
                    uriList.add(uri);
                } catch (Exception e) {
                    throw new BallerinaConnectorException("Invalid URI template.");
                }
                deployedServiceSet.add(service);
            });
        });
        slaveEndpoints.clear();
        return deployedServiceSet;
    }

    private void addUriTemplate(String serviceInterface, String uri, WebSocketService service)
            throws URITemplateException {
        URITemplate<WebSocketService, WebSocketMessage> uriTemplate;
        if (!serviceEndpointsMap.containsKey(serviceInterface)) {
            uriTemplate = new URITemplate<>(new Literal<>(new WsServiceElement(), "/"));
            serviceEndpointsMap.put(serviceInterface, uriTemplate);
        } else {
            uriTemplate = serviceEndpointsMap.get(serviceInterface);
        }

        uriTemplate.parse(uri, service, new WsServiceElementCreator());
    }

    /**
     * Register a service as a client service.
     *
     * @param clientService {@link WebSocketService} of the client service.
     */
    private void registerClientService(WebSocketService clientService) {
        if (clientServices.containsKey(clientService.getName())) {
            throw new BallerinaException("Already contains a client service with name " + clientService.getName());
        } else {
            clientServices.put(clientService.getName(), clientService);
        }
    }

    /**
     * Unregister service for Map.
     *
     * @param service service to unregister.
     */
    public void unregisterService(WebSocketService service) {
        // TODO : Implement service unregistration through URI template tree.
    }

    /**
     * Find the best matching service.
     *
     * @param listenerInterface Listener interface of the the service.
     * @param uri uri of the service.
     * @param variables Map to insert variables needed for dispatching.
     * @param message {@link WebSocketMessage} for checking if needed.
     * @return the service which matches.
     */
    public WebSocketService getServiceEndpoint(String listenerInterface, String uri,
                                               Map<String, String> variables, WebSocketMessage message) {
        return serviceEndpointsMap.get(listenerInterface).matches(uri, variables, message);
    }

    /**
     * Retrieve the client service by service name if exists.
     *
     * @param serviceName name of the service.
     * @return the service by service name if exists. Else return null.
     */
    public WebSocketService getClientService(String serviceName) {
        return clientServices.get(serviceName);
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
     * @param service {@link WebSocketService} which the full path should be found.
     * @return the full path of the WebSocket upgrade.
     */
    private String findFullWebSocketUpgradePath(WebSocketService service) {
        // Find Base path for WebSocket
        Annotation configAnnotation = service.getAnnotation(Constants.WEBSOCKET_PACKAGE_NAME,
                                                            Constants.ANN_NAME_CONFIG);
        String basePath = null;
        if (configAnnotation != null) {
            AnnAttrValue annotationAttributeBasePathValue = configAnnotation.getAnnAttrValue
                    (Constants.ANN_CONFIG_ATTR_BASE_PATH);
            if (annotationAttributeBasePathValue != null && annotationAttributeBasePathValue.getStringValue() != null
                    && !annotationAttributeBasePathValue.getStringValue().trim().isEmpty()) {
                basePath = refactorUri(annotationAttributeBasePathValue.getStringValue());
            }
        }
        return basePath;
    }

}
