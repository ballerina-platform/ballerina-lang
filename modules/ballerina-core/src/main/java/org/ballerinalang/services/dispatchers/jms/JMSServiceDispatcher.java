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

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.Annotation;
import org.ballerinalang.model.Service;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.services.dispatchers.ServiceDispatcher;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Dispatcher which handles the JMS Service.
 */
public class JMSServiceDispatcher implements ServiceDispatcher {
    private static final Logger log = LoggerFactory.getLogger(JMSServiceDispatcher.class);

    // Map <ServiceId, Service>
    private Map<String, Service> serviceMap = new HashMap<>();

    @Override
    public Service findService(CarbonMessage cMsg, CarbonCallback callback, Context balContext) {
        Object serviceIdProperty = cMsg.getProperty(Constants.JMS_SERVICE_ID);
        String serviceId = (serviceIdProperty != null) ? serviceIdProperty.toString() : null;
        if (serviceId == null) {
            throw new BallerinaException("Service Id is not found in JMS Message", balContext);
        }
        Service service = serviceMap.get(serviceId);
        if (service == null) {
            throw new BallerinaException("No jms service is registered with the service id " + serviceId,
                    balContext);
        }
        return service;
    }

    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_JMS;
    }

    @Override
    public void serviceRegistered(Service service) {
        for (Annotation annotation : service.getAnnotations()) {
            Map elementPairs = annotation.getElementPairs();
            if (!Constants.ANNOTATION_NAME_SOURCE.equals(annotation.getName())) {
                continue;
            }
            if (annotation.getElementPairs().size() == 0) {
                continue;
            }
            if (!Constants.PROTOCOL_JMS.
                    equals(annotation.getValueOfElementPair(new SymbolName(Constants.ANNOTATION_PROTOCOL)))) {
                continue;
            }
            Set<Map.Entry<SymbolName, String>> annotationSet = elementPairs.entrySet();
            Map<String, String> annotationKeyValuePairs = new HashMap<String, String>();
            for (Map.Entry<SymbolName, String> entry : annotationSet) {
                annotationKeyValuePairs.put(entry.getKey().getName(), entry.getValue());
            }
            String serviceId = service.getSymbolName().toString();
            serviceMap.put(serviceId, service);
            annotationKeyValuePairs.putIfAbsent(Constants.JMS_DESTINATION, serviceId);
            ServerConnector serverConnector = BallerinaConnectorManager.getInstance()
                    .createServerConnector(Constants.PROTOCOL_JMS, serviceId);
            try {
                serverConnector.start(annotationKeyValuePairs);
                break;
            } catch (ServerConnectorException e) {
                throw new BallerinaException("Error when starting to listen to the queue/topic while " + serviceId +
                        " deployment", e);
            }
        }
    }

    @Override
    public void serviceUnregistered(Service service) {
        String serviceId = service.getSymbolName().toString();
        try {
            if (serviceMap.get(serviceId) != null) {
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
