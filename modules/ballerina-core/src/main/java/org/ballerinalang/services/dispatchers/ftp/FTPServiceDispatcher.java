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

package org.ballerinalang.services.dispatchers.ftp;

import org.ballerinalang.model.Service;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.services.dispatchers.ServiceDispatcher;
import org.ballerinalang.util.codegen.AnnotationAttachmentInfo;
import org.ballerinalang.util.codegen.AnnotationAttributeValue;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;

import java.util.HashMap;
import java.util.Map;

/**
 * Service dispatcher for File server connector.
 */
public class FTPServiceDispatcher implements ServiceDispatcher {

    Map<String, Service> servicesMap = new HashMap<>();

    Map<String, ServiceInfo> serviceInfoMap = new HashMap<>();

    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_FTP;
    }

    @Override
    public String getProtocolPackage() {
        return Constants.FTP_PACKAGE_NAME;
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
                service.getAnnotationAttachmentInfo(Constants.FTP_PACKAGE_NAME, Constants.ANNOTATION_CONFIG);
        AnnotationAttachmentInfo sortInfo =
                service.getAnnotationAttachmentInfo(Constants.FTP_PACKAGE_NAME, Constants.ANNOTATION_SORT);
        AnnotationAttachmentInfo postProcessInfo =
                service.getAnnotationAttachmentInfo(Constants.FTP_PACKAGE_NAME,
                        Constants.ANNOTATION_POST_PROCESS);
        AnnotationAttachmentInfo concurrencyInfo =
                service.getAnnotationAttachmentInfo(Constants.FTP_PACKAGE_NAME,
                                                    Constants.ANNOTATION_CONCURRENCY);
        AnnotationAttachmentInfo sftpInfo =
                service.getAnnotationAttachmentInfo(Constants.FTP_PACKAGE_NAME,
                        Constants.ANNOTATION_SFTP_SETTINGS);

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
            if (sftpInfo != null) {
                addSftpProperties(elementsMap, sftpInfo);
            }
            String dir = elementsMap.get(Constants.ANNOTATION_DIR_URI);
            if (dir == null) {
                throw new BallerinaException("Cannot create file system server without dirPath");
            } else if (!(dir.startsWith("ftp:") || dir.startsWith("sftp:") || dir.startsWith("ftps:"))) {
                throw new BallerinaException("ftp server connector should refer to a ftp, sftp or ftps dirPath");
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
        if (validateAttribute(info.getAnnotationAttributeValue(Constants.ANNOTATION_DIR_URI))) {
            convertedMap.put(Constants.ANNOTATION_DIR_URI,
                    info.getAnnotationAttributeValue(Constants.ANNOTATION_DIR_URI).getStringValue());
        }
        if (validateAttribute(info.getAnnotationAttributeValue(Constants.ANNOTATION_FILE_PATTERN))) {
            convertedMap.put(Constants.ANNOTATION_FILE_PATTERN,
                    info.getAnnotationAttributeValue(Constants.ANNOTATION_FILE_PATTERN).getStringValue());
        }
        if (validateAttribute(info.getAnnotationAttributeValue(Constants.ANNOTATION_POLLING_INTERVAL))) {
            convertedMap.put(Constants.ANNOTATION_POLLING_INTERVAL,
                    info.getAnnotationAttributeValue(Constants.ANNOTATION_POLLING_INTERVAL).getStringValue());
        }
        if (validateAttribute(info.getAnnotationAttributeValue(Constants.ANNOTATION_CRON_EXPRESSION))) {
            convertedMap.put(Constants.ANNOTATION_CRON_EXPRESSION,
                    info.getAnnotationAttributeValue(Constants.ANNOTATION_CRON_EXPRESSION).getStringValue());
        }
        if (validateAttribute(info.getAnnotationAttributeValue(Constants.ANNOTATION_ACK_TIMEOUT))) {
            convertedMap.put(Constants.ANNOTATION_ACK_TIMEOUT,
                    info.getAnnotationAttributeValue(Constants.ANNOTATION_ACK_TIMEOUT).getStringValue());
        }
        if (validateAttribute(info.getAnnotationAttributeValue(Constants.ANNOTATION_FILE_COUNT))) {
            convertedMap.put(Constants.ANNOTATION_FILE_COUNT,
                    info.getAnnotationAttributeValue(Constants.ANNOTATION_FILE_COUNT).getStringValue());
        }
        return convertedMap;
    }

    private void addSortProperties(Map<String, String> elements, AnnotationAttachmentInfo info) {
        if (validateAttribute(info.getAnnotationAttributeValue(Constants.ANNOTATION_SORT_ATTRIBUTE))) {
            elements.put(Constants.ANNOTATION_SORT_ATTRIBUTE,
                    info.getAnnotationAttributeValue(Constants.ANNOTATION_SORT_ATTRIBUTE).getStringValue());
        }
        if (validateAttribute(info.getAnnotationAttributeValue(Constants.ANNOTATION_SORT_ASCENDING))) {
            elements.put(Constants.ANNOTATION_SORT_ASCENDING,
                    info.getAnnotationAttributeValue(Constants.ANNOTATION_SORT_ASCENDING).getStringValue());
        }
    }

    private void addPostProcessProperties(Map<String, String> elements, AnnotationAttachmentInfo info) {
        if (validateAttribute(info.getAnnotationAttributeValue(Constants.ANNOTATION_ACTION_AFTER_PROCESS))) {
            elements.put(Constants.ANNOTATION_ACTION_AFTER_PROCESS,
                    info.getAnnotationAttributeValue(Constants.ANNOTATION_ACTION_AFTER_PROCESS).getStringValue());
        }
        if (validateAttribute(info.getAnnotationAttributeValue(Constants.ANNOTATION_ACTION_AFTER_FAILURE))) {
            elements.put(Constants.ANNOTATION_ACTION_AFTER_FAILURE,
                    info.getAnnotationAttributeValue(Constants.ANNOTATION_ACTION_AFTER_FAILURE).getStringValue());
        }
        if (validateAttribute(info.getAnnotationAttributeValue(Constants.ANNOTATION_MOVE_AFTER_PROCESS))) {
            elements.put(Constants.ANNOTATION_MOVE_AFTER_PROCESS,
                    info.getAnnotationAttributeValue(Constants.ANNOTATION_MOVE_AFTER_PROCESS).getStringValue());
        }
        if (validateAttribute(info.getAnnotationAttributeValue(Constants.ANNOTATION_MOVE_AFTER_FAILURE))) {
            elements.put(Constants.ANNOTATION_MOVE_AFTER_FAILURE,
                    info.getAnnotationAttributeValue(Constants.ANNOTATION_MOVE_AFTER_FAILURE).getStringValue());
        }
        if (validateAttribute(info.getAnnotationAttributeValue(Constants.ANNOTATION_MOVE_TIMESTAMP_FORMAT))) {
            elements.put(Constants.ANNOTATION_MOVE_TIMESTAMP_FORMAT,
                    info.getAnnotationAttributeValue(Constants.ANNOTATION_MOVE_TIMESTAMP_FORMAT).getStringValue());
        }
        if (validateAttribute(info.getAnnotationAttributeValue(Constants.ANNOTATION_CREATE_DIR))) {
            elements.put(Constants.ANNOTATION_CREATE_DIR,
                    info.getAnnotationAttributeValue(Constants.ANNOTATION_CREATE_DIR).getStringValue());
        }
    }

    private void addConcurrencyProperties(Map<String, String> elements, AnnotationAttachmentInfo info) {
        if (validateAttribute(info.getAnnotationAttributeValue(Constants.ANNOTATION_PARALLEL))) {
            elements.put(Constants.ANNOTATION_PARALLEL,
                    info.getAnnotationAttributeValue(Constants.ANNOTATION_PARALLEL).getStringValue());
        }
        if (validateAttribute(info.getAnnotationAttributeValue(Constants.ANNOTATION_THREAD_POOL_SIZE))) {
            elements.put(Constants.ANNOTATION_THREAD_POOL_SIZE,
                    info.getAnnotationAttributeValue(Constants.ANNOTATION_THREAD_POOL_SIZE).getStringValue());
        }
    }

    private void addSftpProperties(Map<String, String> elements, AnnotationAttachmentInfo info) {
        if (validateAttribute(info.getAnnotationAttributeValue(Constants.ANNOTATION_SFTP_IDENTITIES))) {
            elements.put(Constants.ANNOTATION_SFTP_IDENTITIES,
                    info.getAnnotationAttributeValue(Constants.ANNOTATION_SFTP_IDENTITIES).getStringValue());
        }
        if (validateAttribute(info.getAnnotationAttributeValue(Constants.ANNOTATION_SFTP_IDENTITY_PASS_PHRASE))) {
            elements.put(Constants.ANNOTATION_SFTP_IDENTITY_PASS_PHRASE,
                    info.getAnnotationAttributeValue(Constants.ANNOTATION_SFTP_IDENTITY_PASS_PHRASE)
                            .getStringValue());
        }
        if (validateAttribute(info.getAnnotationAttributeValue(Constants.ANNOTATION_SFTP_USER_DIR_IS_ROOT))) {
            elements.put(Constants.ANNOTATION_SFTP_USER_DIR_IS_ROOT,
                    info.getAnnotationAttributeValue(Constants.ANNOTATION_SFTP_USER_DIR_IS_ROOT).getStringValue());
        }
    }

    private boolean validateAttribute(AnnotationAttributeValue annotationAttributeValue) {
        return annotationAttributeValue != null && annotationAttributeValue.getStringValue() != null &&
                !annotationAttributeValue.getStringValue().trim().isEmpty();
    }
}
