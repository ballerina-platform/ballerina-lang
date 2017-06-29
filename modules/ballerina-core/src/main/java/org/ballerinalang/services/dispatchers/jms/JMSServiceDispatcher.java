/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.services.dispatchers.jms;

import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.services.dispatchers.ServiceDispatcher;
import org.ballerinalang.util.codegen.AnnotationAttachmentInfo;
import org.ballerinalang.util.codegen.AnnotationAttributeInfo;
import org.ballerinalang.util.codegen.AttributeInfo;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dispatcher which handles the JMS Service.
 */
public class JMSServiceDispatcher implements ServiceDispatcher {
    private static final Logger log = LoggerFactory.getLogger(JMSServiceDispatcher.class);

    // Map <ServiceId, Service>
    private Map<String, ServiceInfo> serviceInfoMap = new HashMap<>();

    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_JMS;
    }

    @Override
    public String getProtocolPackage() {
        return Constants.PROTOCOL_PACKAGE_JMS;
    }

    @Override
    public ServiceInfo findService(CarbonMessage cMsg, CarbonCallback callback) {
        Object serviceIdProperty = cMsg.getProperty(Constants.JMS_SERVICE_ID);
        String serviceId = (serviceIdProperty != null) ? serviceIdProperty.toString() : null;
        if (serviceId == null) {
            throw new BallerinaException("Service Id is not found in JMS Message");
        }
        ServiceInfo service = serviceInfoMap.get(serviceId);
        if (service == null) {
            throw new BallerinaException("No jms service is registered with the service id " + serviceId);
        }
        return service;
    }

    @Override
    public void serviceRegistered(ServiceInfo service) {

        List<AnnotationAttachmentInfo> connectionProperties = new ArrayList<>();

        AnnotationAttachmentInfo jmsSource = service.getAnnotationAttachmentInfo(Constants.JMS_PACKAGE,
                Constants.ANNOTATION_JMS_SOURCE);
        AnnotationAttributeInfo attributeInfo = (AnnotationAttributeInfo) service.getAttributeInfo(
                AttributeInfo.ANNOTATIONS_ATTRIBUTE);
        if (attributeInfo != null) {
            for (AnnotationAttachmentInfo annotationInfo : attributeInfo.getAnnotationAttachmentInfo()) {
                if (Constants.JMS_PACKAGE.equals(annotationInfo.getPkgPath()) &&
                        Constants.ANNOTATION_CONNECTION_PROPERTY.equals(annotationInfo.getName())) {
                    connectionProperties.add(annotationInfo);
                }
            }
        }

        if (jmsSource != null) {
            Map<String, String> annotationKeyValuePairs = connectionProperties.stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getAnnotationAttributeValue
                                    (Constants.CONNECTION_PROPERTY_KEY).getStringValue(),
                            entry -> entry.getAnnotationAttributeValue
                                    (Constants.CONNECTION_PROPERTY_VALUE).getStringValue()
                    ));

            annotationKeyValuePairs.put(Constants.CONNECTION_PROPERTY_FACTORY_INITIAL,
                    jmsSource.getAnnotationAttributeValue
                            (Constants.CONNECTION_PROPERTY_FACTORY_INITIAL).getStringValue());
            annotationKeyValuePairs.put(Constants.CONNECTION_PROPERTY_PROVIDE_URL,
                    jmsSource.getAnnotationAttributeValue
                            (Constants.CONNECTION_PROPERTY_PROVIDE_URL).getStringValue());

            String serviceId = service.getName();
            serviceInfoMap.put(serviceId, service);
            annotationKeyValuePairs.putIfAbsent(Constants.JMS_DESTINATION, serviceId);
            ServerConnector serverConnector = BallerinaConnectorManager.getInstance()
                    .createServerConnector(Constants.PROTOCOL_JMS, serviceId, annotationKeyValuePairs);
            try {
                serverConnector.start();
            } catch (ServerConnectorException e) {
                throw new BallerinaException("Error when starting to listen to the queue/topic while " + serviceId +
                        " deployment", e);
            }
        }
    }

    @Override
    public void serviceUnregistered(ServiceInfo service) {
        String serviceId = service.getName();
        try {
            if (serviceInfoMap.get(serviceId) != null) {
                ServerConnector serverConnector = BallerinaConnectorManager.getInstance().getServerConnector(serviceId);
                if (null != serverConnector) {
                    serverConnector.stop();
                }
            }
        } catch (ServerConnectorException e) {
            throw new BallerinaException("Error while stopping the jms server connector related with the service " +
                    serviceId, e);
        }
    }
}
