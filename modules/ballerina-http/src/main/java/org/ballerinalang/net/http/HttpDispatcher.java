/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http;

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Dispatcher;
import org.ballerinalang.connector.api.Registry;
import org.ballerinalang.connector.api.Resource;
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

import java.util.Optional;

/**
 * {@code ServerConnectorMessageHandler} is responsible for bridging Ballerina Program and External Server Connector.
 *
 * @since 0.8.0
 */
public class HttpDispatcher implements Dispatcher {

    private static final Logger breLog = LoggerFactory.getLogger(HttpDispatcher.class);

    private HTTPCarbonMessage httpCarbonMessage;
    private HttpRegistry httpRegistry;

    public HttpDispatcher(HTTPCarbonMessage httpCarbonMessage) {
        this.httpCarbonMessage = httpCarbonMessage;
    }

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

    @Override
    public void setRegistry(Registry registry) {
        this.httpRegistry = (HttpRegistry) registry;
    }

    @Override
    public Resource findResource() {
        Resource resource = null;
        String protocol = (String) httpCarbonMessage.getProperty(org.wso2.carbon.messaging.Constants.PROTOCOL);
        if (protocol == null) {
            throw new BallerinaConnectorException("protocol not defined in the incoming request");
        }

        // Find the Service Dispatcher
        HttpServerConnector serverConnector = httpRegistry.getHttpServerConnector();
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
            resource = HTTPResourceDispatcher.findResource(service, httpCarbonMessage, getCallback());
        } catch (Throwable throwable) {
            handleError(httpCarbonMessage, getCallback(), throwable);
        }
        return resource;
    }

    @Override
    public BValue[] createParameters() {
        return new BValue[0];
    }

    @Override
    public CarbonCallback getCallback() {
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

    @Override
    public CarbonMessage getCarbonMsg() {
        return httpCarbonMessage;
    }
}
