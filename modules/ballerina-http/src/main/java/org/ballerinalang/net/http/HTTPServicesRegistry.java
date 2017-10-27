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

package org.ballerinalang.net.http;

import org.ballerinalang.connector.api.AnnAttrValue;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
    private final Map<String, Map<String, HttpService>> servicesInfoMap = new ConcurrentHashMap<>();
    private static final HTTPServicesRegistry servicesRegistry = new HTTPServicesRegistry();

    private HTTPServicesRegistry() {
    }

    public static HTTPServicesRegistry getInstance() {
        return servicesRegistry;
    }

    /**
     * Get ServiceInfo isntance for given interface and base path.
     *
     * @param interfaceId interface id of the service.
     * @param basepath    basepath of the service.
     * @return the {@link HttpService} instance if exist else null
     */
    public HttpService getServiceInfo(String interfaceId, String basepath) {
        return servicesInfoMap.get(interfaceId).get(basepath);
    }

    /**
     * Get ServiceInfo map for given interfaceId.
     *
     * @param interfaceId interfaceId interface id of the services.
     * @return the serviceInfo map if exists else null.
     */
    public Map<String, HttpService> getServicesInfoByInterface(String interfaceId) {
        return servicesInfoMap.get(interfaceId);
    }

    /**
     * Register a service into the map.
     *
     * @param service requested serviceInfo to be registered.
     */
    public void registerService(HttpService service) {
        Annotation annotation = service.getBalService()
                .getAnnotation(Constants.HTTP_PACKAGE_PATH, Constants.ANN_NAME_CONFIG);

        String basePath = discoverBasePathFrom(service, annotation);
        service.setBasePath(basePath);
        Set<ListenerConfiguration> listenerConfigurationSet = HttpUtil.getDefaultOrDynamicListenerConfig(annotation);

        for (ListenerConfiguration listenerConfiguration : listenerConfigurationSet) {
            String entryListenerInterface = listenerConfiguration.getHost() + ":" + listenerConfiguration.getPort();
            Map<String, HttpService> servicesOnInterface = servicesInfoMap
                    .computeIfAbsent(entryListenerInterface, k -> new HashMap<>());

            HttpConnectionManager.getInstance().createHttpServerConnector(listenerConfiguration);
            // Assumption : this is always sequential, no two simultaneous calls can get here
            if (servicesOnInterface.containsKey(basePath)) {
                throw new BallerinaConnectorException(
                        "service with base path :" + basePath + " already exists in listener : "
                                + entryListenerInterface);
            }
            servicesOnInterface.put(basePath, service);
        }

        logger.info("Service deployed : " + service.getName() + " with context " + basePath);
    }

    /**
     * Removing service from the service registry.
     *
     * @param service requested service to be removed.
     */
    public void unregisterService(HttpService service) {
        Annotation annotation = service.getBalService()
                .getAnnotation(Constants.HTTP_PACKAGE_PATH, Constants.ANN_NAME_CONFIG);

        String basePath = discoverBasePathFrom(service, annotation);
        service.setBasePath(basePath);
        Set<ListenerConfiguration> listenerConfigurationSet = HttpUtil.getDefaultOrDynamicListenerConfig(annotation);

        for (ListenerConfiguration listenerConfiguration : listenerConfigurationSet) {
            String entryListenerInterface = listenerConfiguration.getHost() + ":" + listenerConfiguration.getPort();
            Map<String, HttpService> servicesOnInterface = servicesInfoMap.get(entryListenerInterface);
            if (servicesOnInterface == null) {
                continue;
            }
            servicesOnInterface.remove(basePath);
            if (servicesOnInterface.isEmpty()) {
                servicesInfoMap.remove(entryListenerInterface);
                HttpConnectionManager.getInstance().closeIfLast(entryListenerInterface);
            }
        }
    }

    private String discoverBasePathFrom(HttpService service, Annotation annotation) {
        String basePath = service.getName();
        if (annotation == null) {
            //service name cannot start with / hence concat
            return Constants.DEFAULT_BASE_PATH.concat(basePath);
        }
        AnnAttrValue annotationValue = annotation.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_BASE_PATH);
        if (annotationValue != null && annotationValue.getStringValue() != null
                && !annotationValue.getStringValue().trim().isEmpty()) {
            basePath = annotationValue.getStringValue();
        } else {
            basePath = Constants.DEFAULT_BASE_PATH;
        }
        return sanitizeBasePath(basePath);
    }

    private String sanitizeBasePath(String basePath) {
        if (!basePath.startsWith(Constants.DEFAULT_BASE_PATH)) {
            basePath = Constants.DEFAULT_BASE_PATH.concat(basePath);
        }
        if (basePath.endsWith(Constants.DEFAULT_BASE_PATH)) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
        return basePath;
    }
}
