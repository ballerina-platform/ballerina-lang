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

package org.wso2.ballerina.core.nativeimpl.connectors.jms.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.nativeimpl.connectors.BallerinaConnectorManager;
import org.wso2.ballerina.core.nativeimpl.connectors.jms.utils.JMSConstants;
import org.wso2.ballerina.core.runtime.dispatching.ServiceDispatcher;
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
        Object serviceIdProperty = cMsg.getProperty(JMSConstants.JMS_SERVICE_ID);
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
        return JMSConstants.PROTOCOL_JMS;
    }

    @Override
    public void serviceRegistered(Service service) {
        for (Annotation annotation : service.getAnnotations()) {
            Map elementPairs = annotation.getElementPairs();
            if (!annotation.getName().equals(JMSConstants.ANNOTATION_NAME_SOURCE)) {
                continue;
            }
            if (annotation.getElementPairs().size() == 0) {
                continue;
            }
            if (!annotation.getValueOfElementPair(new SymbolName(JMSConstants.ANNOTATION_PROTOCOL))
                    .equals(JMSConstants.PROTOCOL_JMS)) {
                continue;
            }
            Set<Map.Entry<SymbolName, String>> annotationSet = elementPairs.entrySet();
            Map<String, String> annotationKeyValuePairs = new HashMap<String, String>();
            for (Map.Entry<SymbolName, String> entry : annotationSet) {
                annotationKeyValuePairs.put(entry.getKey().getName(), entry.getValue());
            }
            String serviceId = service.getSymbolName().toString();
            serviceMap.put(serviceId, service);
            annotationKeyValuePairs.putIfAbsent(JMSConstants.JMS_DESTINATION, serviceId);
            ServerConnector listener = BallerinaConnectorManager.getInstance()
                    .createServerConnector(JMSConstants.PROTOCOL_JMS, serviceId);
            try {
                listener.start(annotationKeyValuePairs);
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
                ServerConnector listener = BallerinaConnectorManager.getInstance().getServerConnector(serviceId);
                if (null != listener) {
                    listener.stop();
                }
            }
        } catch (ServerConnectorException e) {
            throw new BallerinaException("Error while stopping the jms server connector related with the service " +
                    serviceId, e);
        }
    }
}
