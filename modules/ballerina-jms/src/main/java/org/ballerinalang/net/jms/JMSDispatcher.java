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

package org.ballerinalang.net.jms;

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.connector.impl.ServerConnectorRegistry;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.services.DefaultServerConnectorErrorHandler;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ServerConnectorErrorHandler;
import org.wso2.carbon.messaging.TextCarbonMessage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Dispatcher that handles the resources of a JMS Service.
 */
public class JMSDispatcher {
    private static final Logger log = LoggerFactory.getLogger(JMSDispatcher.class);

    public static Resource findResource(CarbonMessage cMsg, CarbonCallback carbonCallback) throws BallerinaException {

        Resource resource = null;
        String protocol = (String) cMsg.getProperty(org.wso2.carbon.messaging.Constants.PROTOCOL);
        if (protocol == null) {
            throw new BallerinaConnectorException("protocol not defined in the incoming request");
        }

        // Find the Service Dispatcher
        JMSServerConnector jmsServerConnector = (JMSServerConnector) ServerConnectorRegistry.getInstance()
                .getBallerinaServerConnector(Constants.PROTOCOL_PACKAGE_JMS);
        if (jmsServerConnector == null) {
            throw new BallerinaConnectorException("no service dispatcher available to handle protocol: " + protocol);
        }
        try {
            Service service = jmsServerConnector.findService(cMsg);

            Resource[] resources = service.getResources();
            if (resources.length == 0) {
                throw new BallerinaException("No resources found to handle the JMS message in " + service.getName());
            }
            if (resources.length > 1) {
                throw new BallerinaException("More than one resources found in JMS service " + service.getName()
                        + ".JMS Service should only have one resource");
            }
            resource = resources[0];
        } catch (Throwable throwable) {
            handleError(cMsg, carbonCallback, throwable);
        }
        return resource;
    }

    public static void handleError(CarbonMessage cMsg, CarbonCallback callback, Throwable throwable) {
        String errorMsg = throwable.getMessage();
        log.error("error: " + errorMsg, throwable);
        Object protocol = cMsg.getProperty("PROTOCOL");
        Optional<ServerConnectorErrorHandler> optionalErrorHandler =
                BallerinaConnectorManager.getInstance().getServerConnectorErrorHandler((String) protocol);

        try {
            optionalErrorHandler
                    .orElseGet(DefaultServerConnectorErrorHandler::getInstance)
                    .handleError(new BallerinaConnectorException(errorMsg, throwable.getCause()), cMsg, callback);
        } catch (Exception e) {
            log.error("Cannot handle error using the error handler for: " + protocol, e);
        }

    }

    public static BValue[] getSignatureParameters(Resource resource, CarbonMessage jmsCarbonMessage) {
        BStruct message = ConnectorUtils.createStruct(resource, Constants.PROTOCOL_PACKAGE_JMS, Constants.JMS_MESSAGE);
        message.addNativeData(Constants.TRANSPORT_MESSAGE, jmsCarbonMessage);

        List<ParamDetail> paramDetails = resource.getParamDetails();
        BValue[] bValues = new BValue[paramDetails.size()];
        bValues[0] = message;

        return bValues;
    }

}
