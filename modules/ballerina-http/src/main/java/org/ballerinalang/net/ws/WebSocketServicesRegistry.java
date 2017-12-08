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
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.websocket.WebSocketMessage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Store all the WebSocket serviceEndpointsTemplate here.
 */
public class WebSocketServicesRegistry {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServicesRegistry.class);

    // Map<interface, URITemplate<WebSocketServiceName, messageType>>
    private final Map<String, URITemplate<String, WebSocketMessage>>
            serviceEndpointsTemplate = new ConcurrentHashMap<>();
    // Map<clientServiceName, ClientService>
    private final Map<String, WebSocketService> clientServices = new ConcurrentHashMap<>();
    // Map<ServiceName, ServiceEndpoint>
    private final Map<String, WebSocketService> serviceEndpoints = new ConcurrentHashMap<>();

    private final Map<String, WebSocketService> slaveEndpoints = new HashMap<>();
    private final List<String> httpToWsUpgradableEndpoints = new LinkedList<>();

    public WebSocketServicesRegistry() {
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
                Annotation configAnnotation =
                        service.getAnnotation(Constants.PROTOCOL_PACKAGE_WS, Constants.ANNOTATION_CONFIGURATION);
                if (configAnnotation == null) {
                    slaveEndpoints.put(service.getName(), service);
                    return;
                }

                String basePath = findFullWebSocketUpgradePath(service);
                if (basePath == null) {
                    slaveEndpoints.put(service.getName(), service);
                    return;
                }

                Set<ListenerConfiguration> listenerConfigurationSet =
                        HttpUtil.getDefaultOrDynamicListenerConfig(configAnnotation);

                for (ListenerConfiguration listenerConfiguration : listenerConfigurationSet) {
                    String entryListenerInterface =
                            listenerConfiguration.getHost() + ":" + listenerConfiguration.getPort();
                    addServiceToURITemplate(entryListenerInterface, basePath, service.getName());
                    serviceEndpoints.put(service.getName(), service);
                    HttpConnectionManager.getInstance().createHttpServerConnector(listenerConfiguration);
                }

                logger.info("Service deployed : " + service.getName() + " with context " + basePath);
            }
        }
    }

    public void addServiceByName(String entryListenerInterface, String basePath, String serviceName) {
        addServiceToURITemplate(entryListenerInterface, basePath, serviceName);
        httpToWsUpgradableEndpoints.add(serviceName);
    }

    private void addServiceToURITemplate(String entryListenerInterface, String basePath, String serviceName) {
        URITemplate<String, WebSocketMessage> servicesOnInterface = serviceEndpointsTemplate
                .computeIfAbsent(entryListenerInterface, k -> {
                    try {
                        return new URITemplate<>(new Literal<>(new WsDataElement(), "/"));
                    } catch (URITemplateException e) {
                        throw new BallerinaConnectorException(e.getMessage());
                    }
                });

        try {
            servicesOnInterface.parse(basePath, serviceName, new WsDataElementFactory());
        } catch (URITemplateException e) {
            throw new BallerinaConnectorException(e.getMessage());
        }
    }

    public void completeDeployment() {
        httpToWsUpgradableEndpoints.forEach(serviceName -> {
            if (!serviceEndpoints.containsKey(serviceName)) {
                if (!slaveEndpoints.containsKey(serviceName)) {
                    throw new BallerinaConnectorException("Cannot find a WebSocket service for service name "
                                                                  + serviceName);
                }
                serviceEndpoints.put(serviceName, slaveEndpoints.remove(serviceName));
            }
        });

        if (slaveEndpoints.size() > 0) {
            String errorMsg = "Cannot register following services: \n";
            for (String serviceName : slaveEndpoints.keySet()) {
                WebSocketService service = slaveEndpoints.remove(serviceName);
                if (service.getAnnotation(Constants.PROTOCOL_PACKAGE_WS,
                                          Constants.ANNOTATION_CONFIGURATION) == null) {
                    String msg = "Cannot deploy WebSocket service without configuration annotation";
                    errorMsg = errorMsg + String.format("\t%s: %s\n", serviceName, msg);
                } else {
                    String msg = "Cannot deploy WebSocket service without associated path";
                    errorMsg = errorMsg + String.format("\t%s: %s\n", serviceName, msg);
                }
            }
            throw new BallerinaConnectorException(errorMsg);
        }
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
     * Find the best matching service.
     *
     * @param listenerInterface Listener interface of the the service.
     * @param uri uri of the service.
     * @return the service which matches.
     */
    public WebSocketService matchServiceEndpoint(String listenerInterface, String uri, Map<String, String> variables) {
        // Message can be null here since WsDataElement does not use inbound message to match services.
        return serviceEndpoints.get(serviceEndpointsTemplate.get(listenerInterface).matches(uri, variables, null));
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
        Annotation configAnnotation = service.getAnnotation(Constants.PROTOCOL_PACKAGE_WS,
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

    /**
     * Find the listener interface of a given service.
     *
     * @param service {@link WebSocketService} which the listener interface should be found.
     * @return the listener interface of the service.
     */
    private String getListenerInterface(WebSocketService service) {
        // TODO : Handle correct interface addition to default interface.
        String listenerInterface = Constants.DEFAULT_INTERFACE;
        return listenerInterface;
    }

}
