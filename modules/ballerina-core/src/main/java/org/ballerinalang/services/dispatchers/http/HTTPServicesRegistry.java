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

import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.util.codegen.AnnAttachmentInfo;
import org.ballerinalang.util.codegen.AnnAttributeValue;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.message.HTTPMessageUtil;

import java.util.HashMap;
import java.util.HashSet;
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
    private final Map<String, Map<String, ServiceInfo>> servicesInfoMap = new ConcurrentHashMap<>();
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
        AnnAttachmentInfo annotationInfo = service.getAnnotationAttachmentInfo(Constants
                .HTTP_PACKAGE_PATH, Constants.ANN_NAME_CONFIG);

        String basePath = discoverBasePathFrom(service, annotationInfo);
        Set<ListenerConfiguration> listenerConfigurationSet = getDefaultOrDynamicListenerConfig(annotationInfo);

        for (ListenerConfiguration listenerConfiguration : listenerConfigurationSet) {
            String entryListenerInterface = listenerConfiguration.getHost() + ":" + listenerConfiguration.getPort();
            Map<String, ServiceInfo> servicesOnInterface = servicesInfoMap
                    .computeIfAbsent(entryListenerInterface, k -> new HashMap<>());

            BallerinaConnectorManager.getInstance().createHttpServerConnector(listenerConfiguration);
            // Assumption : this is always sequential, no two simultaneous calls can get here
            if (servicesOnInterface.containsKey(basePath)) {
                throw new BallerinaException(
                        "service with base path :" + basePath + " already exists in listener : "
                                + entryListenerInterface);
            }
            servicesOnInterface.put(basePath, service);
        }

        logger.info("Service deployed : " + service.getName() + " with context " + basePath);
    }

    /**
     * Removing service from the service registry.
     * @param service requested service to be removed.
     */
    public void unregisterService(ServiceInfo service) {
        AnnAttachmentInfo annotationInfo = service.getAnnotationAttachmentInfo(Constants
                .HTTP_PACKAGE_PATH, Constants.ANN_NAME_CONFIG);

        String basePath = discoverBasePathFrom(service, annotationInfo);
        Set<ListenerConfiguration> listenerConfigurationSet = getDefaultOrDynamicListenerConfig(annotationInfo);

        for (ListenerConfiguration listenerConfiguration : listenerConfigurationSet) {
            String entryListenerInterface = listenerConfiguration.getHost() + ":" + listenerConfiguration.getPort();
            Map<String, ServiceInfo> servicesOnInterface = servicesInfoMap.get(entryListenerInterface);
            if (servicesOnInterface != null) {
                servicesOnInterface.remove(basePath);
                if (servicesOnInterface.isEmpty()) {
                    servicesInfoMap.remove(entryListenerInterface);
                    BallerinaConnectorManager.getInstance().closeIfLast(entryListenerInterface);
                }
            }
        }
    }

    private Set<ListenerConfiguration> getListenerConfigurationsFrom(Map<String, Map<String, String>> listenerProp) {
        Set<ListenerConfiguration> listenerConfigurationSet = new HashSet<>();
        for (Map.Entry<String, Map<String, String>> entry : listenerProp.entrySet()) {
            Map<String, String> propMap = entry.getValue();
            String entryListenerInterface = getListenerInterface(propMap);
            ListenerConfiguration listenerConfiguration = HTTPMessageUtil
                    .buildListenerConfig(entryListenerInterface, propMap);
            listenerConfigurationSet.add(listenerConfiguration);
        }
        return listenerConfigurationSet;
    }

    private String getListenerInterface(Map<String, String> parameters) {
        String host = parameters.get("host") != null ? parameters.get("host") : "0.0.0.0";
        int port = Integer.parseInt(parameters.get("port"));
        return host + ":" + port;
    }

    private String discoverBasePathFrom(ServiceInfo service, AnnAttachmentInfo annotationInfo) {
        String basePath = service.getName();
        if (annotationInfo != null) {
            AnnAttributeValue annAttributeValue = annotationInfo.getAttributeValue
                    (Constants.ANN_CONFIG_ATTR_BASE_PATH);
            if (annAttributeValue != null && annAttributeValue.getStringValue() != null) {
                if (annAttributeValue.getStringValue().trim().isEmpty()) {
                    basePath = Constants.DEFAULT_BASE_PATH;
                } else {
                    basePath = annAttributeValue.getStringValue();
                }
            }
        }
        if (!basePath.startsWith(Constants.DEFAULT_BASE_PATH)) {
            basePath = Constants.DEFAULT_BASE_PATH.concat(basePath);
        }
        return basePath;
    }

    private Set<ListenerConfiguration> getDefaultOrDynamicListenerConfig(AnnAttachmentInfo annotationInfo) {
        Map<String, Map<String, String>> listenerProp = buildListerProperties(annotationInfo);

        Set<ListenerConfiguration> listenerConfigurationSet;
        if (listenerProp == null || listenerProp.isEmpty()) {
            listenerConfigurationSet =
                    BallerinaConnectorManager.getInstance().getDefaultListenerConfiugrationSet();
        } else {
            listenerConfigurationSet = getListenerConfigurationsFrom(listenerProp);
        }
        return listenerConfigurationSet;
    }

    /**
     * Method to build map of listener property maps given the service annotation attachment.
     * This will first look for the port property and if present then it will get other properties,
     * and create the property map.
     *
     * @param configInfo            In which listener configurations are specified.
     * @return listenerConfMap      With required properties
     */
    private Map<String, Map<String, String>> buildListerProperties(AnnAttachmentInfo configInfo) {
        if (configInfo == null) {
            return null;
        }
        //key - listenerId, value - listener config property map
        Map<String, Map<String, String>> listenerConfMap = new HashMap<>();

        AnnAttributeValue hostAttrVal = configInfo.getAttributeValue
                (Constants.ANN_CONFIG_ATTR_HOST);
        AnnAttributeValue portAttrVal = configInfo.getAttributeValue
                (Constants.ANN_CONFIG_ATTR_PORT);
        AnnAttributeValue httpsPortAttrVal = configInfo.getAttributeValue
                (Constants.ANN_CONFIG_ATTR_HTTPS_PORT);
        AnnAttributeValue keyStoreFileAttrVal = configInfo.getAttributeValue
                (Constants.ANN_CONFIG_ATTR_KEY_STORE_FILE);
        AnnAttributeValue keyStorePassAttrVal = configInfo.getAttributeValue
                (Constants.ANN_CONFIG_ATTR_KEY_STORE_PASS);
        AnnAttributeValue certPassAttrVal = configInfo.getAttributeValue
                (Constants.ANN_CONFIG_ATTR_CERT_PASS);

        if (portAttrVal != null && portAttrVal.getIntValue() > 0) {
            Map<String, String> httpPropMap = new HashMap<>();
            httpPropMap.put(Constants.ANN_CONFIG_ATTR_PORT, Long.toString(portAttrVal.getIntValue()));
            httpPropMap.put(Constants.ANN_CONFIG_ATTR_SCHEME, Constants.PROTOCOL_HTTP);
            if (hostAttrVal != null && hostAttrVal.getStringValue() != null) {
                httpPropMap.put(Constants.ANN_CONFIG_ATTR_HOST, hostAttrVal.getStringValue());
            } else {
                httpPropMap.put(Constants.ANN_CONFIG_ATTR_HOST, Constants.HTTP_DEFAULT_HOST);
            }
            listenerConfMap.put(buildInterfaceName(httpPropMap), httpPropMap);
        }

        if (httpsPortAttrVal != null && httpsPortAttrVal.getIntValue() > 0) {
            Map<String, String> httpsPropMap = new HashMap<>();
            httpsPropMap.put(Constants.ANN_CONFIG_ATTR_PORT, Long.toString(httpsPortAttrVal.getIntValue()));
            httpsPropMap.put(Constants.ANN_CONFIG_ATTR_SCHEME, Constants.PROTOCOL_HTTPS);
            if (hostAttrVal != null && hostAttrVal.getStringValue() != null) {
                httpsPropMap.put(Constants.ANN_CONFIG_ATTR_HOST, hostAttrVal.getStringValue());
            } else {
                httpsPropMap.put(Constants.ANN_CONFIG_ATTR_HOST, Constants.HTTP_DEFAULT_HOST);
            }
            if (keyStoreFileAttrVal == null || keyStoreFileAttrVal.getStringValue() == null) {
                //TODO get from language pack, and add location
                throw new BallerinaException("Keystore location must be provided for protocol https");
            }
            if (keyStorePassAttrVal == null || keyStorePassAttrVal.getStringValue() == null) {
                //TODO get from language pack, and add location
                throw new BallerinaException("Keystore password value must be provided for protocol https");
            }
            if (certPassAttrVal == null || certPassAttrVal.getStringValue() == null) {
                //TODO get from language pack, and add location
                throw new BallerinaException("Certificate password value must be provided for protocol https");
            }
            httpsPropMap.put(Constants.ANN_CONFIG_ATTR_KEY_STORE_FILE, keyStoreFileAttrVal.getStringValue());
            httpsPropMap.put(Constants.ANN_CONFIG_ATTR_KEY_STORE_PASS, keyStorePassAttrVal.getStringValue());
            httpsPropMap.put(Constants.ANN_CONFIG_ATTR_CERT_PASS, certPassAttrVal.getStringValue());
            listenerConfMap.put(buildInterfaceName(httpsPropMap), httpsPropMap);
        }
        return listenerConfMap;
    }

    /**
     * Build interface name using schema and port.
     *
     * @param propMap which has schema and port
     * @return interfaceName
     */
    private String buildInterfaceName(Map<String, String> propMap) {
        StringBuilder iName = new StringBuilder();
        iName.append(propMap.get(Constants.ANN_CONFIG_ATTR_SCHEME));
        iName.append("_");
        iName.append(propMap.get(Constants.ANN_CONFIG_ATTR_HOST));
        iName.append("_");
        iName.append(propMap.get(Constants.ANN_CONFIG_ATTR_PORT));
        return iName.toString();
    }
}
