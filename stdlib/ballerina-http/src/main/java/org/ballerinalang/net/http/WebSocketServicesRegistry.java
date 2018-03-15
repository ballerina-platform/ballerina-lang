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

import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.net.uri.URITemplate;
import org.ballerinalang.net.uri.URITemplateException;
import org.ballerinalang.net.uri.parser.Literal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Store all the WebSocket serviceEndpointsTemplate here.
 */
public class WebSocketServicesRegistry {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServicesRegistry.class);
    private final Map<String, WebSocketService> servicesInfoMap = new ConcurrentHashMap<>();

    // Map<interface, URITemplate<WebSocketService, messageType>>
    private final Map<String, URITemplate<WebSocketService, WebSocketMessage>>
            serviceEndpointsTemplate = new ConcurrentHashMap<>();
    // Map<ServiceName, ServiceEndpoint>
    private Map<String, WebSocketService> masterServices = new ConcurrentHashMap<>();

    private Map<String, WebSocketService> slaveServices = new HashMap<>();
    private List<UpgradableServiceInfo> httpToWsUpgradableServices = new LinkedList<>();
    private CopyOnWriteArrayList<String> sortedServiceURIs = new CopyOnWriteArrayList<>();

    public WebSocketServicesRegistry() {
    }

    public void registerService(WebSocketService service) {
        String basePath = findFullWebSocketUpgradePath(service);
        basePath = urlDecode(basePath);
        service.setBasePath(basePath);
        // TODO: Add websocket services to the service registry when service creation get available.
        servicesInfoMap.put(service.getBasePath(), service);
        logger.info("Service deployed : " + service.getName() + " with context " + basePath);
        postProcessService(service);
    }

    private void postProcessService(WebSocketService service) {
        WebSocketServiceValidator.validateServiceEndpoint(service);
        //basePath will get cached after registering service
        sortedServiceURIs.add(service.getBasePath());
        sortedServiceURIs.sort((basePath1, basePath2) -> basePath2.length() - basePath1.length());
    }

    public String findTheMostSpecificBasePath(String requestURIPath, Map<String, WebSocketService> services) {
        for (Object key : sortedServiceURIs) {
            if (!requestURIPath.toLowerCase().contains(key.toString().toLowerCase())) {
                continue;
            }
            if (requestURIPath.length() <= key.toString().length()) {
                return key.toString();
            }
            if (requestURIPath.charAt(key.toString().length()) == '/') {
                return key.toString();
            }
        }
        if (services.containsKey(HttpConstants.DEFAULT_BASE_PATH)) {
            return HttpConstants.DEFAULT_BASE_PATH;
        }
        return null;
    }

    /**
     * Get ServiceInfo map for given interfaceId.
     *
     * @return the serviceInfo map if exists else null.
     */
    public Map<String, WebSocketService> getServicesInfoByInterface() {
        return servicesInfoMap;
    }

    private String urlDecode(String basePath) {
        try {
            basePath = URLDecoder.decode(basePath, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new BallerinaConnectorException(e.getMessage());
        }
        return basePath;
    }

    /**
     * Find the Full path for WebSocket upgrade.
     *
     * @param service {@link WebSocketService} which the full path should be found.
     * @return the full path of the WebSocket upgrade.
     */
    private String findFullWebSocketUpgradePath(WebSocketService service) {
        // Find Base path for WebSocket
        Annotation configAnnotation = WebSocketUtil.getServiceConfigAnnotation(service,
                                                                               HttpConstants.PROTOCOL_PACKAGE_HTTP);
        String basePath = null;
        if (configAnnotation != null) {
            Struct annStruct = configAnnotation.getValue();
            String annotationValue = annStruct.getStringField(HttpConstants.ANN_CONFIG_ATTR_BASE_PATH);
            if (annotationValue != null && !annotationValue.trim().isEmpty()) {
                basePath = refactorUri(annotationValue);
            }
        }
        return basePath;
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


    public void addUpgradableServiceByName(WebSocketService service, String basePath) {
        basePath = urlDecode(basePath);
        service.setBasePath(basePath);
        servicesInfoMap.put(service.getBasePath(), service);
        logger.info("Service deployed : " + service.getName() + " with context " + basePath);
        postProcessService(service);
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
