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

package org.ballerinalang.services.dispatchers.fs;

import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.services.dispatchers.ServiceDispatcher;
import org.ballerinalang.util.codegen.AnnAttachmentInfo;
import org.ballerinalang.util.codegen.AnnAttributeValue;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service dispatcher for File System Server Connector.
 */
public class FileSystemServiceDispatcher implements ServiceDispatcher {

    private Map<String, ServiceInfo> serviceInfoMap = new HashMap<>();

    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_FILE_SYSTEM;
    }

    @Override
    public String getProtocolPackage() {
        return Constants.FILE_SYSTEM_PACKAGE_NAME;
    }

    @Override
    public ServiceInfo findService(CarbonMessage cMsg, CarbonCallback callback) {
        Object serviceNameProperty = cMsg.getProperty(Constants.TRANSPORT_PROPERTY_SERVICE_NAME);
        String serviceName = (serviceNameProperty != null) ? serviceNameProperty.toString() : null;
        if (serviceName == null) {
            throw new BallerinaException(
                    "Could not find a service to dispatch. " + Constants.TRANSPORT_PROPERTY_SERVICE_NAME +
                            " property not set.");
        }
        ServiceInfo service = serviceInfoMap.get(serviceName);
        if (service == null) {
            throw new BallerinaException("No service registered with the name: " + serviceName);
        }
        return service;
    }

    @Override
    public void serviceRegistered(ServiceInfo service) {
        AnnAttachmentInfo configInfo =
                service.getAnnotationAttachmentInfo(Constants.FILE_SYSTEM_PACKAGE_NAME, Constants.ANNOTATION_CONFIG);
        AnnAttachmentInfo sortInfo =
                service.getAnnotationAttachmentInfo(Constants.FILE_SYSTEM_PACKAGE_NAME, Constants.ANNOTATION_SORT);
        AnnAttachmentInfo postProcessInfo =
                service.getAnnotationAttachmentInfo(Constants.FILE_SYSTEM_PACKAGE_NAME,
                                                    Constants.ANNOTATION_POST_PROCESS);
        AnnAttachmentInfo concurrencyInfo =
                service.getAnnotationAttachmentInfo(Constants.FILE_SYSTEM_PACKAGE_NAME,
                                                    Constants.ANNOTATION_CONCURRENCY);
        if (configInfo != null) {
            Map<String, String> elementsMap = getServerConnectorParamMap(configInfo);
            if (sortInfo != null) {
                addSortProperties(elementsMap, sortInfo);
            }
            if (postProcessInfo != null) {
                addPostProcessProperties(elementsMap, postProcessInfo);
            }
            if (concurrencyInfo != null) {
                addConcurrencyProperties(elementsMap, concurrencyInfo);
            }
            String dir = elementsMap.get(Constants.ANNOTATION_DIR_URI);
            if (dir == null) {
                throw new BallerinaException("Cannot create file system server without dirURI");
            }
            String serviceName = service.getName();
            ServerConnector fileServerConnector = BallerinaConnectorManager.getInstance().createServerConnector(
                    Constants.PROTOCOL_FILE_SYSTEM, serviceName, elementsMap);
            try {
                fileServerConnector.start();
                serviceInfoMap.put(serviceName, service);
            } catch (ServerConnectorException e) {
                throw new BallerinaException("Could not start File System Server Connector for service: " +
                                             serviceName, e);
            }
        }
    }

    @Override
    public void serviceUnregistered(ServiceInfo service) {
        String serviceName = service.getName();
        if (serviceInfoMap.get(serviceName) != null) {
            serviceInfoMap.remove(serviceName);
            try {
                BallerinaConnectorManager.getInstance().getServerConnector(serviceName).stop();
            } catch (ServerConnectorException e) {
                throw new BallerinaException("Could not stop file server connector for " +
                                             "service: " + serviceName, e);
            }
        }
    }

    private Map<String, String> getServerConnectorParamMap(AnnAttachmentInfo info) {
        Map<String, String> convertedMap = new HashMap<>();
        List<String> annotations = new ArrayList<>();
        annotations.add(Constants.ANNOTATION_DIR_URI);
        annotations.add(Constants.ANNOTATION_FILE_PATTERN);
        annotations.add(Constants.ANNOTATION_POLLING_INTERVAL);
        annotations.add(Constants.ANNOTATION_CRON_EXPRESSION);
        annotations.add(Constants.ANNOTATION_ACK_TIMEOUT);
        annotations.add(Constants.ANNOTATION_FILE_COUNT);

        for (String data : annotations) {
            AnnAttributeValue value = info.getAttributeValue(data);
            if (value != null) {
                convertedMap.put(data, value.getStringValue());
            }
        }
        return convertedMap;
    }

    private void addSortProperties(Map<String, String> elements, AnnAttachmentInfo info) {
        List<String> annotations = new ArrayList<>();
        annotations.add(Constants.ANNOTATION_SORT_ATTRIBUTE);
        annotations.add(Constants.ANNOTATION_SORT_ASCENDING);

        for (String data : annotations) {
            AnnAttributeValue value = info.getAttributeValue(data);
            if (value != null) {
                elements.put(data, value.getStringValue());
            }
        }
    }

    private void addPostProcessProperties(Map<String, String> elements, AnnAttachmentInfo info) {
        List<String> annotations = new ArrayList<>();
        annotations.add(Constants.ANNOTATION_ACTION_AFTER_PROCESS);
        annotations.add(Constants.ANNOTATION_ACTION_AFTER_FAILURE);
        annotations.add(Constants.ANNOTATION_MOVE_AFTER_PROCESS);
        annotations.add(Constants.ANNOTATION_MOVE_AFTER_FAILURE);
        annotations.add(Constants.ANNOTATION_MOVE_TIMESTAMP_FORMAT);
        annotations.add(Constants.ANNOTATION_CREATE_DIR);

        for (String data : annotations) {
            AnnAttributeValue value = info.getAttributeValue(data);
            if (value != null) {
                elements.put(data, value.getStringValue());
            }
        }
    }

    private void addConcurrencyProperties(Map<String, String> elements, AnnAttachmentInfo info) {
        List<String> annotations = new ArrayList<>();
        annotations.add(Constants.ANNOTATION_PARALLEL);
        annotations.add(Constants.ANNOTATION_THREAD_POOL_SIZE);

        for (String data : annotations) {
            AnnAttributeValue value = info.getAttributeValue(data);
            if (value != null) {
                elements.put(data, value.getStringValue());
            }
        }
    }
}
