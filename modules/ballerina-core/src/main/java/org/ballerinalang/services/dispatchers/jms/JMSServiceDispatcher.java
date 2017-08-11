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
import org.ballerinalang.util.codegen.AnnAttachmentInfo;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.jms.utils.JMSConstants;

import java.util.HashMap;
import java.util.Map;

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

        AnnAttachmentInfo jmsConfig = service.getAnnotationAttachmentInfo(Constants.JMS_PACKAGE,
                Constants.ANNOTATION_JMS_CONFIG);

        if (jmsConfig == null) {
            return;
        }

        Map<String, String> configParams = JMSUtils.preProcessJmsConfig(jmsConfig);

        String serviceId = service.getName();
        serviceInfoMap.put(serviceId, service);
        configParams.putIfAbsent(JMSConstants.PARAM_DESTINATION_NAME, serviceId);
        ServerConnector serverConnector = BallerinaConnectorManager.getInstance()
                .createServerConnector(Constants.PROTOCOL_JMS, serviceId, configParams);
        try {
            serverConnector.start();
        } catch (ServerConnectorException e) {
            throw new BallerinaException("Error when starting to listen to the queue/topic while " + serviceId +
                    " deployment", e);
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
