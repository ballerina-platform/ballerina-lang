/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.services.dispatchers.file;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.AnnotationAttributeValue;
import org.ballerinalang.model.Service;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.services.dispatchers.ServiceDispatcher;
import org.ballerinalang.util.codegen.AnnotationAttachmentInfo;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service dispatcher for File server connector.
 */
public class FileServiceDispatcher implements ServiceDispatcher {

    Map<String, Service> servicesMap = new HashMap<>();

    Map<String, ServiceInfo> serviceInfoMap = new HashMap<>();

    @Override
    @Deprecated
    public Service findService(CarbonMessage cMsg, CarbonCallback callback, Context balContext) {
        Object serviceNameProperty = cMsg.getProperty(Constants.TRANSPORT_PROPERTY_SERVICE_NAME);
        String serviceName = (serviceNameProperty != null) ? serviceNameProperty.toString() : null;
        if (serviceName == null) {
            throw new BallerinaException("Service name is not found with the file input stream.", balContext);
        }
        Service service = servicesMap.get(serviceName);
        if (service == null) {
            throw new BallerinaException("No file service is registered with the service name " + serviceName,
                    balContext);
        }
        return service;
    }

    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_FILE;
    }

    @Override
    public void serviceRegistered(Service service) {
        for (AnnotationAttachment annotation : service.getAnnotations()) {
            if (annotation.getName().equals(Constants.ANNOTATION_FILE_SOURCE) &&
                annotation.getPkgName().equals(Constants.PROTOCOL_FILE)) {
                Map<String, String> elementsMap = annotation.getAttributeNameValuePairs().entrySet().stream().collect(
                        Collectors.toMap(Entry::getKey, entry -> entry.getValue().getLiteralValue().stringValue()));
                String serviceName = service.getSymbolName().getName();
                ServerConnector fileServerConnector = BallerinaConnectorManager.getInstance().createServerConnector(
                        Constants.PROTOCOL_FILE, serviceName, elementsMap);
                try {
                    fileServerConnector.start();
                    servicesMap.put(serviceName, service);
                } catch (ServerConnectorException e) {
                    throw new BallerinaException("Could not start File Server Connector for service: " +
                                                 serviceName, e);
                }
                return;
            }
        }
    }

    @Override
    public void serviceUnregistered(Service service) {
        String serviceName = service.getSymbolName().getName();
        if (servicesMap.get(serviceName) != null) {
            servicesMap.remove(serviceName);
            try {
                BallerinaConnectorManager.getInstance().getServerConnector(serviceName).stop();
            } catch (ServerConnectorException e) {
                throw new BallerinaException("Could not stop file server connector for " +
                        "service: " + serviceName, e);
            }
        }
    }

    @Override
    public ServiceInfo findService(CarbonMessage cMsg, CarbonCallback callback) {
        Object serviceNameProperty = cMsg.getProperty(Constants.TRANSPORT_PROPERTY_SERVICE_NAME);
        String serviceName = (serviceNameProperty != null) ? serviceNameProperty.toString() : null;
        if (serviceName == null) {
            throw new BallerinaException("Service name is not found with the file input stream.");
        }
        ServiceInfo service = serviceInfoMap.get(serviceName);
        if (service == null) {
            throw new BallerinaException("No file service is registered with the service name " + serviceName);
        }
        return service;
    }

    @Override
    public void serviceRegistered(ServiceInfo service) {
        AnnotationAttachmentInfo annotationInfo = service.getAnnotationAttachmentInfo(Constants.FILE_PACKAGE_NAME,
                Constants.ANNOTATION_FILE_SOURCE);

        if (annotationInfo != null) {
            Map<String, String> elementsMap = null; // TODO : Fix Annotation attachment in code generation.
            String serviceName = service.getName();
            ServerConnector fileServerConnector = BallerinaConnectorManager.getInstance().createServerConnector(
                    Constants.PROTOCOL_FILE, serviceName, elementsMap);
            try {
                fileServerConnector.start();
                serviceInfoMap.put(serviceName, service);
            } catch (ServerConnectorException e) {
                throw new BallerinaException("Could not start File Server Connector for service: " +
                        serviceName, e);
            }
        }
    }

    @Override
    public void serviceUnregistered(ServiceInfo service) {
        String serviceName = service.getName();
        if (servicesMap.get(serviceName) != null) {
            servicesMap.remove(serviceName);
            try {
                BallerinaConnectorManager.getInstance().getServerConnector(serviceName).stop();
            } catch (ServerConnectorException e) {
                throw new BallerinaException("Could not stop file server connector for " +
                        "service: " + serviceName, e);
            }
        }
    }

    private static Map<String, String> getServerConnectorParamMap(Map<String, AnnotationAttributeValue> map) {
        Map<String, String> convertedMap = new HashMap<>();
        Set<Entry<String, AnnotationAttributeValue>> entrySet = map.entrySet();
        for (Map.Entry<String, AnnotationAttributeValue> entry : entrySet) {
            convertedMap.put(entry.getKey(), entry.getValue().toString());
        }
        return convertedMap;
    }
}
