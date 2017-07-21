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
                .HTTP_PACKAGE_PATH, Constants.ANNOTATION_NAME_CONFIG);

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

        Map<String, String> propMap = buildServerConnectorProperties(service);

        //If port annotation is present in the service, then create a listener interface with schema and port
        if (propMap != null) {
            listenerInterface = buildInterfaceName(propMap);
        }

        //TODO check for parallel access
        Map<String, ServiceInfo> servicesOnInterface = servicesInfoMap.get(listenerInterface);
        if (servicesOnInterface == null) {
            // Assumption : this is always sequential, no two simultaneous calls can get here
            servicesOnInterface = new HashMap<>();
            servicesInfoMap.put(listenerInterface, servicesOnInterface);
            ServerConnector connector = BallerinaConnectorManager.getInstance().getServerConnector(listenerInterface);

            //If there is no listener and required listener properties are present, then create a new listener
            if (connector == null && propMap != null) {
                connector = BallerinaConnectorManager.getInstance()
                    .createServerConnector(Constants.PROTOCOL_HTTP, listenerInterface, propMap);
                listenerPropMap.put(listenerInterface, propMap);
            }

            if (connector == null) {
                throw new BallerinaException(
                        "ServerConnector interface not registered for : " + listenerInterface);
            }
            // Delay the startup until all services are deployed
            BallerinaConnectorManager.getInstance().addStartupDelayedServerConnector(connector);
        } else {
            //It comes to this means, same listener interface is already present. So in here, it checks
            //whether existing interface also has same parameters (schema, keystores etc). If not throw and error
            Map<String, String> existingMap = listenerPropMap.get(listenerInterface);
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

        logger.info("Service deployed : " + service.getName() + " with context " + basePath);
    }

    /**
     * Method to build property map given the service annotations.
     * This will first look for the port property and if present then it will get other properties,
     * and create the property map.
     *
     * @param service from which annotations are retrieved
     * @return  propMap with required properties
     */
    private Map<String, String> buildServerConnectorProperties(ServiceInfo service) {
        AnnotationAttachmentInfo configInfo = service.getAnnotationAttachmentInfo(Constants
                .HTTP_PACKAGE_PATH, Constants.ANNOTATION_NAME_CONFIG);
        if (configInfo == null) {
            return null;
        }
        AnnotationAttributeValue portAttrVal = configInfo.getAnnotationAttributeValue
                (Constants.ANNOTATION_ATTRIBUTE_PORT);
        if (portAttrVal == null || portAttrVal.getIntValue() < 0) {
            return null;
        }
        Map<String, String> propMap = new HashMap<>();
        propMap.put(Constants.ANNOTATION_ATTRIBUTE_PORT, Long.toString(portAttrVal.getIntValue()));

        AnnotationAttributeValue hostAttrVal = configInfo.getAnnotationAttributeValue
                (Constants.ANNOTATION_ATTRIBUTE_HOST);
        if (hostAttrVal != null && hostAttrVal.getStringValue() != null &&
                !hostAttrVal.getStringValue().trim().isEmpty()) {
            propMap.put(Constants.ANNOTATION_ATTRIBUTE_HOST, hostAttrVal.getStringValue());
        }

        AnnotationAttributeValue schemaAttrVal = configInfo.getAnnotationAttributeValue
                (Constants.ANNOTATION_ATTRIBUTE_SCHEME);
        if (schemaAttrVal != null && schemaAttrVal.getStringValue() != null &&
                !schemaAttrVal.getStringValue().trim().isEmpty()) {
            propMap.put(Constants.ANNOTATION_ATTRIBUTE_SCHEME, schemaAttrVal.getStringValue());
        }

        AnnotationAttributeValue keyStoreFileAttrVal = configInfo.getAnnotationAttributeValue
                (Constants.ANNOTATION_ATTRIBUTE_KEY_STORE_FILE);
        if (keyStoreFileAttrVal != null && keyStoreFileAttrVal.getStringValue() != null &&
                !keyStoreFileAttrVal.getStringValue().trim().isEmpty()) {
            propMap.put(Constants.ANNOTATION_ATTRIBUTE_KEY_STORE_FILE, keyStoreFileAttrVal.getStringValue());
            AnnotationAttributeValue keyStorePassAttrVal = configInfo.getAnnotationAttributeValue
                    (Constants.ANNOTATION_ATTRIBUTE_KEY_STORE_PASS);
            propMap.put(Constants.ANNOTATION_ATTRIBUTE_KEY_STORE_PASS, keyStorePassAttrVal.getStringValue());
            AnnotationAttributeValue certPassAttrVal = configInfo.getAnnotationAttributeValue
                    (Constants.ANNOTATION_ATTRIBUTE_CERT_PASS);
            propMap.put(Constants.ANNOTATION_ATTRIBUTE_CERT_PASS, certPassAttrVal.getStringValue());
        }

        return propMap;
    }

    /**
     * Build interface name using schema and port.
     *
     * @param propMap which has schema and port
     * @return interfaceName
     */
    private String buildInterfaceName(Map<String, String> propMap) {
        StringBuilder iName = new StringBuilder();
        iName.append(propMap.get(Constants.ANNOTATION_ATTRIBUTE_SCHEME) != null ?
                propMap.get(Constants.ANNOTATION_ATTRIBUTE_SCHEME) : Constants.PROTOCOL_HTTP);
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
