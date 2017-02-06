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
import org.wso2.ballerina.core.nativeimpl.connectors.jms.Constants;
import org.wso2.ballerina.core.runtime.dispatching.ServiceDispatcher;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.jms.jndi.exception.JMSServerConnectorException;
import org.wso2.carbon.transport.jms.jndi.listener.JMSServerConnector;
import org.wso2.carbon.transport.jms.jndi.utils.JMSConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Dispatcher which handles the JMS Service
 */
public class JMSServiceDispatcher implements ServiceDispatcher {
    private static final Logger log = LoggerFactory.getLogger(JMSServiceDispatcher.class);

    // Map <ServiceId, Service>
    private Map<String, Service> serviceMap = new HashMap<>();

    @Override
    public Service findService(CarbonMessage cMsg, CarbonCallback callback, Context balContext) {
        try {
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
        } catch (Throwable e) {
            throw new BallerinaException(e.getMessage(), balContext);
        }
    }

    @Override
    public String getProtocol() {
        return JMSConstants.PROTOCOL_JMS;
    }

    @Override
    public void serviceRegistered(Service service) {
        try {
            for (Annotation annotation : service.getAnnotations()) {
                Map elementPairs = annotation.getElementPairs();
                if (annotation.getName().equals(Constants.ANNOTATION_NAME_SOURCE)
                        && annotation.getElementPairs().size() > 0 && annotation
                        .getValueOfElementPair(new SymbolName(Constants.ANNOTATION_PROTOCOL))
                        .equals(JMSConstants.PROTOCOL_JMS)) {
                    Set<Map.Entry<SymbolName, String>> set = elementPairs.entrySet();
                    Map<String, String> keyValuePairs = new HashMap<String, String>();
                    for (Map.Entry<SymbolName, String> entry : set) {
                        keyValuePairs.put(entry.getKey().getName(), entry.getValue());
                    }
                    String serviceId = service.getSymbolName().toString();
                    serviceMap.put(serviceId, service);;
                    JMSServerConnector listener =  (JMSServerConnector) BallerinaConnectorManager.getInstance()
                            .createServerConnector(JMSConstants.PROTOCOL_JMS, serviceId);
                    listener.start(keyValuePairs);
                    listener.init();
                }
            }
        } catch (Throwable e) {
            throw new BallerinaException("Ballerina Error : " + e.getMessage());
        }
    }

    @Override
    public void serviceUnregistered(Service service) {
        try {
            String serviceId = service.getSymbolName().toString();
            if (serviceMap.get(serviceId) != null) {
                JMSServerConnector listener = (JMSServerConnector) BallerinaConnectorManager.getInstance()
                        .getServerConnector(serviceId);
                if (listener != null) {
                    listener.destroy();
                }
            }
        } catch (JMSServerConnectorException e) {
            throw new BallerinaException("Ballerina Error : " + e.getMessage());
        }
    }
}
