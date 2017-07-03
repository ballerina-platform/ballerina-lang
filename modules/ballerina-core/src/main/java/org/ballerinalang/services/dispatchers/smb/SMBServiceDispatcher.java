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

package org.ballerinalang.services.dispatchers.smb;

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
import java.util.Objects;

/**
 * Service dispatcher for File server connector.
 */
public class SMBServiceDispatcher implements ServiceDispatcher {

    Map<String, Service> servicesMap = new HashMap<>();

    Map<String, ServiceInfo> serviceInfoMap = new HashMap<>();

    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_SMB;
    }

    @Override public String getProtocolPackage() {
        return Constants.SMB_PACKAGE_NAME;
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
        AnnotationAttachmentInfo configInfo =
                service.getAnnotationAttachmentInfo(Constants.SMB_PACKAGE_NAME, Constants.ANNOTATION_CONFIG);
        AnnotationAttachmentInfo sortInfo =
                service.getAnnotationAttachmentInfo(Constants.SMB_PACKAGE_NAME, Constants.ANNOTATION_SORT);
        AnnotationAttachmentInfo postProcessInfo =
                service.getAnnotationAttachmentInfo(Constants.SMB_PACKAGE_NAME,
                                                    Constants.ANNOTATION_POST_PROCESS);
        AnnotationAttachmentInfo concurrencyInfo =
                service.getAnnotationAttachmentInfo(Constants.SMB_PACKAGE_NAME,
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
            elementsMap.values().removeIf(Objects::isNull);
            String dir = elementsMap.get(Constants.ANNOTATION_DIR_PATH);
            if (dir == null) {
                throw new BallerinaException("Cannot create file system server without dirPath");
            } else if (!dir.contains("smb:")) {
                throw new BallerinaException("ftp server connector should refer to a smb dirPath");
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

    private Map<String, String> getServerConnectorParamMap(AnnotationAttachmentInfo info) {
        Map<String, String> convertedMap = new HashMap<>();
        convertedMap.put(Constants.ANNOTATION_DIR_PATH,
                         info.getAnnotationAttributeValue(Constants.ANNOTATION_DIR_PATH).getStringValue());
        convertedMap.put(Constants.ANNOTATION_FILE_PATTERN,
                         info.getAnnotationAttributeValue(Constants.ANNOTATION_FILE_PATTERN).getStringValue());
        convertedMap.put(Constants.ANNOTATION_POLLING_INTERVAL,
                         info.getAnnotationAttributeValue(Constants.ANNOTATION_POLLING_INTERVAL).getStringValue());
        convertedMap.put(Constants.ANNOTATION_CRON_EXPRESSION,
                         info.getAnnotationAttributeValue(Constants.ANNOTATION_CRON_EXPRESSION).getStringValue());
        convertedMap.put(Constants.ANNOTATION_ACK_TIMEOUT,
                         info.getAnnotationAttributeValue(Constants.ANNOTATION_ACK_TIMEOUT).getStringValue());
        convertedMap.put(Constants.ANNOTATION_FILE_COUNT,
                         info.getAnnotationAttributeValue(Constants.ANNOTATION_FILE_COUNT).getStringValue());
        return convertedMap;
    }

    private void addSortProperties(Map<String, String> elements, AnnotationAttachmentInfo info) {
        elements.put(Constants.ANNOTATION_SORT_ATTRIBUTE,
                         info.getAnnotationAttributeValue(Constants.ANNOTATION_SORT_ATTRIBUTE).getStringValue());
        elements.put(Constants.ANNOTATION_SORT_ASCENDING,
                         info.getAnnotationAttributeValue(Constants.ANNOTATION_SORT_ASCENDING).getStringValue());
    }

    private void addPostProcessProperties(Map<String, String> elements, AnnotationAttachmentInfo info) {
        elements.put(Constants.ANNOTATION_ACTION_AFTER_PROCESS,
                         info.getAnnotationAttributeValue(Constants.ANNOTATION_ACTION_AFTER_PROCESS).getStringValue());
        elements.put(Constants.ANNOTATION_ACTION_AFTER_FAILURE,
                         info.getAnnotationAttributeValue(Constants.ANNOTATION_ACTION_AFTER_FAILURE).getStringValue());
        elements.put(Constants.ANNOTATION_MOVE_AFTER_PROCESS,
                         info.getAnnotationAttributeValue(Constants.ANNOTATION_MOVE_AFTER_PROCESS).getStringValue());
        elements.put(Constants.ANNOTATION_MOVE_AFTER_FAILURE,
                         info.getAnnotationAttributeValue(Constants.ANNOTATION_MOVE_AFTER_FAILURE).getStringValue());
        elements.put(Constants.ANNOTATION_MOVE_TIMESTAMP_FORMAT,
                         info.getAnnotationAttributeValue(Constants.ANNOTATION_MOVE_TIMESTAMP_FORMAT).getStringValue());
        elements.put(Constants.ANNOTATION_CREATE_DIR,
                         info.getAnnotationAttributeValue(Constants.ANNOTATION_CREATE_DIR).getStringValue());
    }

    private void addConcurrencyProperties(Map<String, String> elements, AnnotationAttachmentInfo info) {
        elements.put(Constants.ANNOTATION_PARALLEL,
                         info.getAnnotationAttributeValue(Constants.ANNOTATION_PARALLEL).getStringValue());
        elements.put(Constants.ANNOTATION_THREAD_POOL_SIZE,
                         info.getAnnotationAttributeValue(Constants.ANNOTATION_THREAD_POOL_SIZE).getStringValue());
    }
}
