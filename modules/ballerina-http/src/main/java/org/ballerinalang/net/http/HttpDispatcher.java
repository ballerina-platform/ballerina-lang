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
package org.ballerinalang.net.http;

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.impl.ConnectorUtils;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.services.DefaultServerConnectorErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ServerConnectorErrorHandler;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPMessageUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * {@code HttpDispatcher} is responsible for dispatching incoming http requests to the correct resource.
 *
 * @since 0.94
 */
public class HttpDispatcher {

    private static final Logger breLog = LoggerFactory.getLogger(HttpDispatcher.class);

    public static void handleOutbound(CarbonMessage cMsg, CarbonCallback callback) {
        callback.done(cMsg);
    }

    public static void handleError(CarbonMessage cMsg, CarbonCallback callback, Throwable throwable) {
        String errorMsg = throwable.getMessage();

        // bre log should contain bre stack trace, not the ballerina stack trace
        breLog.error("error: " + errorMsg, throwable);
        Object protocol = cMsg.getProperty("PROTOCOL");
        Optional<ServerConnectorErrorHandler> optionalErrorHandler =
                BallerinaConnectorManager.getInstance().getServerConnectorErrorHandler((String) protocol);

        try {
            optionalErrorHandler
                    .orElseGet(DefaultServerConnectorErrorHandler::getInstance)
                    .handleError(new BallerinaConnectorException(errorMsg, throwable.getCause()), cMsg, callback);
        } catch (Exception e) {
            breLog.error("Cannot handle error using the error handler for: " + protocol, e);
        }

    }

    /**
     * This method finds the matching resource for the incoming request.
     *
     * @param httpCarbonMessage incoming message.
     * @return matching resource.
     */
    public static Resource findResource(HTTPCarbonMessage httpCarbonMessage) {
        Resource resource = null;
        String protocol = (String) httpCarbonMessage.getProperty(org.wso2.carbon.messaging.Constants.PROTOCOL);
        if (protocol == null) {
            throw new BallerinaConnectorException("protocol not defined in the incoming request");
        }

        // Find the Service Dispatcher
        HttpServerConnector serverConnector = (HttpServerConnector) Executor
                .getBallerinaServerConnector(Constants.PROTOCOL_PACKAGE_HTTP);
        if (serverConnector == null) {
            throw new BallerinaConnectorException("no service dispatcher available to handle protocol: " + protocol);
        }

        try {
            // Find the Service
            HttpService service = serverConnector.findService(httpCarbonMessage);
            if (service == null) {
                throw new BallerinaConnectorException("no Service found to handle the service request");
                // Finer details of the errors are thrown from the dispatcher itself, Ideally we shouldn't get here.
            }

            // Find the Resource
            resource = HTTPResourceDispatcher.findResource(service, httpCarbonMessage, getCallback(httpCarbonMessage));
        } catch (Throwable throwable) {
            handleError(httpCarbonMessage, getCallback(httpCarbonMessage), throwable);
        }
        return resource;
    }

    public static CarbonCallback getCallback(HTTPCarbonMessage httpCarbonMessage) {
        return (cMsg) -> {
            HTTPCarbonMessage carbonMessage = HTTPMessageUtil.convertCarbonMessage(cMsg);
            try {
                //Process CORS if exists.
                if (httpCarbonMessage.getHeader("Origin") != null) {
                    CorsHeaderGenerator.process(httpCarbonMessage, carbonMessage, true);
                }
                httpCarbonMessage.respond(carbonMessage);
            } catch (ServerConnectorException e) {
                throw new BallerinaConnectorException("Error occurred during response", e);
            }
        };
    }

    public static BValue[] getSignatureParameters(Resource resource, HTTPCarbonMessage httpCarbonMessage) {

        BStruct request = ConnectorUtils.createStruct(resource, Constants.PROTOCOL_PACKAGE_HTTP, Constants.REQUEST);
        BStruct response = ConnectorUtils.createStruct(resource, Constants.PROTOCOL_PACKAGE_HTTP, Constants.RESPONSE);
        request.addNativeData(Constants.HTTP_CARBON_MESSAGE, httpCarbonMessage);

        List<ParamDetail> paramDetails = resource.getParamDetails();
        Map<String, String> resourceArgumentValues =
                (Map<String, String>) httpCarbonMessage.getProperty(org.ballerinalang.runtime.Constants.RESOURCE_ARGS);

        BValue[] bValues = new BValue[paramDetails.size()];
        bValues[0] = request;
        bValues[1] = response;
        if (paramDetails.size() > 2) {
            int i = 2;
            for (ParamDetail parameter : paramDetails) {
                if (parameter.getVarType().getName().equals(Constants.TYPE_STRING)) {
                    bValues[i++] = new BString(resourceArgumentValues.get(parameter.getVarName()));
                }
            }
        }
        return bValues;
    }
}
