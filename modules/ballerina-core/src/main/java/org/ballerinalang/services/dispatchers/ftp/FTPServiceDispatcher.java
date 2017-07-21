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

import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.services.dispatchers.ServiceDispatcher;
import org.ballerinalang.util.codegen.AnnAttachmentInfo;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;

import java.util.HashMap;
import java.util.Map;

/**
 * Service dispatcher for FTP Server Connector.
 */
public class FTPServiceDispatcher implements ServiceDispatcher {

    private Map<String, ServiceInfo> serviceInfoMap = new HashMap<>();

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
        AnnAttachmentInfo configInfo =
                service.getAnnotationAttachmentInfo(Constants.FTP_PACKAGE_NAME, Constants.ANNOTATION_CONFIG);

        if (configInfo != null) {
            Map<String, String> paramMap = getServerConnectorParamMap(configInfo);
            String dir = paramMap.get(Constants.ANNOTATION_DIR_URI);

            if (dir == null) {
                throw new BallerinaException("Cannot create file system server without dirPath");
            } else if (!(dir.startsWith("ftp:") || dir.startsWith("sftp:") || dir.startsWith("ftps:"))) {
                throw new BallerinaException("ftp server connector should refer to a ftp, sftp or ftps dirPath");
            }

            String serviceName = service.getName();
            ServerConnector fileServerConnector = BallerinaConnectorManager.getInstance().createServerConnector(
                    Constants.PROTOCOL_FILE_SYSTEM, serviceName, paramMap);
            fileServerConnector.setServerConnectorErrorHandler(
                    BallerinaConnectorManager.getInstance()
                                                .getServerConnectorErrorHandler(Constants.PROTOCOL_FTP)
                                                .get());
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
        Map<String, String> params = new HashMap<>();

        addAnnotationAttributeValue(info, Constants.ANNOTATION_DIR_URI, params);
        addAnnotationAttributeValue(info, Constants.ANNOTATION_FILE_PATTERN, params);
        addAnnotationAttributeValue(info, Constants.ANNOTATION_POLLING_INTERVAL, params);
        addAnnotationAttributeValue(info, Constants.ANNOTATION_CRON_EXPRESSION, params);
        addAnnotationAttributeValue(info, Constants.ANNOTATION_ACK_TIMEOUT, params);
        addAnnotationAttributeValue(info, Constants.ANNOTATION_FILE_COUNT, params);

        addAnnotationAttributeValue(info, Constants.ANNOTATION_SORT_ATTRIBUTE, params);
        addAnnotationAttributeValue(info, Constants.ANNOTATION_SORT_ASCENDING, params);

        addAnnotationAttributeValue(info, Constants.ANNOTATION_ACTION_AFTER_PROCESS, params);
        addAnnotationAttributeValue(info, Constants.ANNOTATION_ACTION_AFTER_FAILURE, params);
        addAnnotationAttributeValue(info, Constants.ANNOTATION_MOVE_AFTER_PROCESS, params);
        addAnnotationAttributeValue(info, Constants.ANNOTATION_MOVE_AFTER_FAILURE, params);
        addAnnotationAttributeValue(info, Constants.ANNOTATION_MOVE_TIMESTAMP_FORMAT, params);
        addAnnotationAttributeValue(info, Constants.ANNOTATION_CREATE_DIR, params);

        addAnnotationAttributeValue(info, Constants.ANNOTATION_PARALLEL, params);
        addAnnotationAttributeValue(info, Constants.ANNOTATION_THREAD_POOL_SIZE, params);

        addAnnotationAttributeValue(info, Constants.ANNOTATION_SFTP_IDENTITIES, params);
        addAnnotationAttributeValue(info, Constants.ANNOTATION_SFTP_IDENTITY_PASS_PHRASE, params);
        addAnnotationAttributeValue(info, Constants.ANNOTATION_SFTP_USER_DIR_IS_ROOT, params);

        return params;
    }

    private void addAnnotationAttributeValue(AnnAttachmentInfo info, String attribute,
                                             Map<String, String> params) {
        if (info.getAttributeValue(attribute) != null &&
                !info.getAttributeValue(attribute).getStringValue().trim().isEmpty()) {
            params.put(attribute, info.getAttributeValue(attribute).getStringValue());
        }
    }
}
