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

package org.ballerinalang.net.http;

import org.ballerinalang.connector.api.AnnAttrValue;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.net.uri.URITemplate;
import org.ballerinalang.net.uri.URITemplateException;
import org.ballerinalang.net.uri.parser.Literal;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketMessage;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Store all the WebSocket serviceEndpointsTemplate here.
 */
public class WebSocketServicesRegistry {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServicesRegistry.class);

    // Map<interface, URITemplate<WebSocketService, messageType>>
    private final Map<String, URITemplate<WebSocketService, WebSocketMessage>>
            serviceEndpointsTemplate = new ConcurrentHashMap<>();
    // Map<ServiceName, ServiceEndpoint>
    private Map<String, WebSocketService> masterServices = new ConcurrentHashMap<>();

    private Map<String, WebSocketService> slaveServices = new HashMap<>();
    private List<UpgradableServiceInfo> httpToWsUpgradableServices = new LinkedList<>();

    public WebSocketServicesRegistry() {
    }

    /**
     * Register the service. Check for WebSocket upgrade path and client service.
     *
     * @param service service to register.
     */
    public void registerService(WebSocketService service) {
        //TODO: check if client does not have enpoints annotation
        if (WebSocketServiceValidator.validateServiceEndpoint(service)) {
            Annotation configAnnotation =
                    HttpUtil.getServiceConfigAnnotation(service, HttpConstants.PROTOCOL_PACKAGE_HTTP);
            if (configAnnotation == null) {
                slaveServices.put(service.getName(), service);
                return;
            }

            String basePath = findFullWebSocketUpgradePath(service);
            if (basePath == null) {
                slaveServices.put(service.getName(), service);
            }
        }
    }


    public void addUpgradableServiceByName(String entryListenerInterface, String basePath, String serviceName) {
        httpToWsUpgradableServices.add(new UpgradableServiceInfo(entryListenerInterface, basePath, serviceName));
    }

    public void completeDeployment() {
        httpToWsUpgradableServices.forEach(upgradableServiceInfo -> {
            String serviceName = upgradableServiceInfo.getServiceName();
            if (!masterServices.containsKey(serviceName)) {
                if (!slaveServices.containsKey(serviceName)) {
                    throw new BallerinaConnectorException("Cannot find a WebSocket service for service name "
                                                                  + upgradableServiceInfo.getServiceName());
                }
                masterServices.put(serviceName, slaveServices.remove(serviceName));
            }
            addServiceToURITemplate(upgradableServiceInfo.getServiceInterface(),
                                    upgradableServiceInfo.getBasePath(), masterServices.get(serviceName));
        });

        if (slaveServices.size() > 0) {
            StringBuilder errorMsg = new StringBuilder("Cannot register following services: \n");
            for (String serviceName : slaveServices.keySet()) {
                WebSocketService service = slaveServices.remove(serviceName);
                if (HttpUtil.getServiceConfigAnnotation(service, HttpConstants.PROTOCOL_PACKAGE_HTTP) == null) {
                    String msg = "Cannot deploy WebSocket service without configuration annotation";
                    errorMsg.append(String.format("\t%s: %s\n", serviceName, msg));
                } else {
                    String msg = "Cannot deploy WebSocket service without associated path";
                    errorMsg.append(String.format("\t%s: %s\n", serviceName, msg));
                }
            }
            throw new BallerinaConnectorException(errorMsg.toString());
        }

        // After deployment is completed these data structures are not needed anymore.
        masterServices.clear();
        slaveServices.clear();
        httpToWsUpgradableServices.clear();
    }

    private void addServiceToURITemplate(String entryListenerInterface, String basePath, WebSocketService service) {
        URITemplate<WebSocketService, WebSocketMessage> servicesOnInterface = serviceEndpointsTemplate
                .computeIfAbsent(entryListenerInterface, k -> {
                    try {
                        return new URITemplate<>(new Literal<>(new WebSocketDataElement(), "/"));
                    } catch (URITemplateException e) {
                        throw new BallerinaConnectorException(e.getMessage());
                    }
                });

        try {
            servicesOnInterface.parse(basePath, service, new WebSocketDataElementFactory());
        } catch (URITemplateException | UnsupportedEncodingException e) {
            throw new BallerinaConnectorException(e.getMessage());
        }
    }

    /**
     * Find the best matching service.
     *
     * @param listenerInterface Listener interface of the the service.
     * @param uri               uri of the service.
     * @return the service which matches.
     */
    public WebSocketService matchServiceEndpoint(String listenerInterface, String uri, Map<String, String> variables) {
        // Message can be null here since WebSocketDataElement does not use inbound message to match services.
        return serviceEndpointsTemplate.get(listenerInterface).matches(uri, variables, null);
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
        Annotation configAnnotation = HttpUtil.getServiceConfigAnnotation(service,
                                                                          HttpConstants.PROTOCOL_PACKAGE_HTTP);
        String basePath = null;
        if (configAnnotation != null) {
            AnnAttrValue annotationAttributeBasePathValue = configAnnotation.getAnnAttrValue
                    (HttpConstants.ANN_CONFIG_ATTR_BASE_PATH);
            if (annotationAttributeBasePathValue != null && annotationAttributeBasePathValue.getStringValue() != null
                    && !annotationAttributeBasePathValue.getStringValue().trim().isEmpty()) {
                basePath = refactorUri(annotationAttributeBasePathValue.getStringValue());
            }
        }
        return basePath;
    }

    private class UpgradableServiceInfo {
        private final String basePath;
        private final String serviceInterface;
        private final String serviceName;

        private UpgradableServiceInfo(String basePath, String serviceInterface, String serviceName) {
            this.basePath = basePath;
            this.serviceInterface = serviceInterface;
            this.serviceName = serviceName;
        }

        private String getBasePath() {
            return basePath;
        }

        private String getServiceInterface() {
            return serviceInterface;
        }

        private String getServiceName() {
            return serviceName;
        }
    }

}
