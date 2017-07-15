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
import org.ballerinalang.util.codegen.AnnotationAttachmentInfo;
import org.ballerinalang.util.codegen.AnnotationAttributeValue;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.RuntimeErrors;
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
    private final Map<String, Map<String, ServiceInfo>> servicesInfoMap = new ConcurrentHashMap<>();
    //Outer map key = interface, Inner map = listener property map
    private final Map<String, Map<String, String>> listenerPropMap = new ConcurrentHashMap<>();

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
        String listenerInterface = Constants.DEFAULT_INTERFACE;
        String basePath = service.getName();
        AnnotationAttachmentInfo annotationInfo = service.getAnnotationAttachmentInfo(Constants
                .HTTP_PACKAGE_PATH, Constants.ANNOTATION_NAME_CONFIGURATION);

        if (annotationInfo != null) {
            AnnotationAttributeValue annotationAttributeValue = annotationInfo.getAnnotationAttributeValue
                    (Constants.ANNOTATION_ATTRIBUTE_BASE_PATH);
            if (annotationAttributeValue != null && annotationAttributeValue.getStringValue() != null &&
                    !annotationAttributeValue.getStringValue().trim().isEmpty()) {
                basePath = annotationAttributeValue.getStringValue();
            }
        }

        if (!basePath.startsWith(Constants.DEFAULT_BASE_PATH)) {
            basePath = Constants.DEFAULT_BASE_PATH.concat(basePath);
        }
        //key - listenerId, value - listener config property map
        Map<String, Map<String, String>> listenerProp = buildListerProperties(annotationInfo);

        if (listenerProp == null || listenerProp.isEmpty()) {
            Map<String, ServiceInfo> servicesOnInterface = servicesInfoMap.get(listenerInterface);
            if (servicesOnInterface == null) {
                // Assumption : this is always sequential, no two simultaneous calls can get here
                servicesOnInterface = new HashMap<>();
                servicesInfoMap.put(listenerInterface, servicesOnInterface);
                ServerConnector connector = BallerinaConnectorManager.getInstance()
                        .getServerConnector(listenerInterface);

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
            return;
        }

        for (String listenerId : listenerProp.keySet()) {
            Map<String, ServiceInfo> servicesOnInterface = servicesInfoMap.get(listenerId);
            Map<String, String> propMap = listenerProp.get(listenerId);
            if (servicesOnInterface == null) {
                // Assumption : this is always sequential, no two simultaneous calls can get here
                servicesOnInterface = new HashMap<>();
                servicesInfoMap.put(listenerId, servicesOnInterface);
                ServerConnector connector = BallerinaConnectorManager.getInstance().getServerConnector(listenerId);

                if (connector == null) {
                    connector = BallerinaConnectorManager.getInstance()
                            .createServerConnector(Constants.PROTOCOL_HTTP, listenerId, propMap);
                    listenerPropMap.put(listenerId, propMap);
                }
                if (connector == null) {
                    throw new BallerinaException(
                            "ServerConnector interface not registered for : " + listenerId);
                }
                // Delay the startup until all services are deployed
                BallerinaConnectorManager.getInstance().addStartupDelayedServerConnector(connector);
            } else {
                Map<String, String> existingMap = listenerPropMap.get(listenerId);
                if (existingMap != null && propMap != null && !existingMap.equals(propMap)) {
                    throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.SERVER_CONNECTOR_ALREADY_EXIST,
                            propMap.get(Constants.ANNOTATION_ATTRIBUTE_PORT));
                }
            }
            if (servicesOnInterface.containsKey(basePath)) {
                throw new BallerinaException(
                        "service with base path :" + basePath + " already exists in listener : " + listenerInterface);
            }

            servicesOnInterface.put(basePath, service);
        }

        logger.info("Service deployed : " + service.getName() + " with context " + basePath);
    }

    /**
     * Method to build map of listener property maps given the service annotation attachment.
     * This will first look for the port property and if present then it will get other properties,
     * and create the property map.
     *
     * @param configInfo            In which listener configurations are specified.
     * @return listenerConfMap      With required properties
     */
    private Map<String, Map<String, String>> buildListerProperties(AnnotationAttachmentInfo configInfo) {
        if (configInfo == null) {
            return null;
        }
        //key - listenerId, value - listener config property map
        Map<String, Map<String, String>> listenerConfMap = new HashMap<>();

        AnnotationAttributeValue hostAttrVal = configInfo.getAnnotationAttributeValue
                (Constants.ANNOTATION_ATTRIBUTE_HOST);
        AnnotationAttributeValue portAttrVal = configInfo.getAnnotationAttributeValue
                (Constants.ANNOTATION_ATTRIBUTE_PORT);
        AnnotationAttributeValue httpsPortAttrVal = configInfo.getAnnotationAttributeValue
                (Constants.ANNOTATION_ATTRIBUTE_HTTPS_PORT);
        AnnotationAttributeValue keyStoreFileAttrVal = configInfo.getAnnotationAttributeValue
                (Constants.ANNOTATION_ATTRIBUTE_KEY_STORE_FILE);
        AnnotationAttributeValue keyStorePassAttrVal = configInfo.getAnnotationAttributeValue
                (Constants.ANNOTATION_ATTRIBUTE_KEY_STORE_PASS);
        AnnotationAttributeValue certPassAttrVal = configInfo.getAnnotationAttributeValue
                (Constants.ANNOTATION_ATTRIBUTE_CERT_PASS);

        if (portAttrVal != null && portAttrVal.getIntValue() > 0) {
            Map<String, String> httpPropMap = new HashMap<>();
            httpPropMap.put(Constants.ANNOTATION_ATTRIBUTE_PORT, Long.toString(portAttrVal.getIntValue()));
            httpPropMap.put(Constants.ANNOTATION_ATTRIBUTE_SCHEME, Constants.PROTOCOL_HTTP);
            if (hostAttrVal != null && hostAttrVal.getStringValue() != null) {
                httpPropMap.put(Constants.ANNOTATION_ATTRIBUTE_HOST, hostAttrVal.getStringValue());
            } else {
                httpPropMap.put(Constants.ANNOTATION_ATTRIBUTE_HOST, Constants.HTTP_DEFAULT_HOST);
            }
            listenerConfMap.put(buildInterfaceName(httpPropMap), httpPropMap);
        }

        if (httpsPortAttrVal != null && httpsPortAttrVal.getIntValue() > 0) {
            Map<String, String> httpsPropMap = new HashMap<>();
            httpsPropMap.put(Constants.ANNOTATION_ATTRIBUTE_PORT, Long.toString(httpsPortAttrVal.getIntValue()));
            httpsPropMap.put(Constants.ANNOTATION_ATTRIBUTE_SCHEME, Constants.PROTOCOL_HTTPS);
            if (hostAttrVal != null && hostAttrVal.getStringValue() != null) {
                httpsPropMap.put(Constants.ANNOTATION_ATTRIBUTE_HOST, hostAttrVal.getStringValue());
            } else {
                httpsPropMap.put(Constants.ANNOTATION_ATTRIBUTE_HOST, Constants.HTTP_DEFAULT_HOST);
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
            httpsPropMap.put(Constants.ANNOTATION_ATTRIBUTE_KEY_STORE_FILE, keyStoreFileAttrVal.getStringValue());
            httpsPropMap.put(Constants.ANNOTATION_ATTRIBUTE_KEY_STORE_PASS, keyStorePassAttrVal.getStringValue());
            httpsPropMap.put(Constants.ANNOTATION_ATTRIBUTE_CERT_PASS, certPassAttrVal.getStringValue());
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
        iName.append(propMap.get(Constants.ANNOTATION_ATTRIBUTE_SCHEME));
        iName.append("_");
        iName.append(propMap.get(Constants.ANNOTATION_ATTRIBUTE_HOST));
        iName.append("_");
        iName.append(propMap.get(Constants.ANNOTATION_ATTRIBUTE_PORT));
        return iName.toString();
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
