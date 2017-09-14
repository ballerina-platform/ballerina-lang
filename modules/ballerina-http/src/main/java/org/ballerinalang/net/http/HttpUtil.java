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
import org.ballerinalang.services.ErrorHandlerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.contract.ServerConnector;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class providing utility methods.
 */
public class HttpUtil {
    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);

    /**
     * Helper method to start pending http server connectors.
     *
     * @throws BallerinaConnectorException
     */
    public static void startPendingHttpConnectors() throws BallerinaConnectorException {
        try {
            // Starting up HTTP Server connectors
            PrintStream outStream = System.out;
            List<ServerConnector> startedHTTPConnectors = HttpConnectionManager.getInstance()
                    .startPendingHTTPConnectors();
            startedHTTPConnectors.forEach(serverConnector -> outStream.println("ballerina: started " +
                    "server connector " + serverConnector));
        } catch (ServerConnectorException e) {
            throw new BallerinaConnectorException(e);
        }
    }

    public static void handleResponse(HTTPCarbonMessage requestMsg, HTTPCarbonMessage responseMsg) {
        try {
            //TODO enable once new resource signature enabled
//                Session session = context.getCurrentSession();
//                if (session != null) {
//                    session.generateSessionHeader(responseMsg);
//                }
            //Process CORS if exists.
            if (requestMsg.getHeader("Origin") != null) {
                CorsHeaderGenerator.process(requestMsg, responseMsg, true);
            }
            requestMsg.respond(responseMsg);
        } catch (org.wso2.carbon.transport.http.netty.contract.ServerConnectorException e) {
            throw new BallerinaConnectorException("Error occurred during response", e);
        }
    }

    public static void handleFailure(HTTPCarbonMessage requestMessage, BallerinaConnectorException ex) {
        Object carbonStatusCode = requestMessage.getProperty(Constants.HTTP_STATUS_CODE);
        int statusCode = (carbonStatusCode == null) ? 500 : Integer.parseInt(carbonStatusCode.toString());
        String errorMsg = ex.getMessage();
        log.error(errorMsg);
        ErrorHandlerUtils.printError(ex);
        if (statusCode == 404) {
            HttpUtil.handleResponse(requestMessage, createErrorMessage(errorMsg, statusCode));
        } else {
            // TODO If you put just "", then we got a NPE. Need to find why
            HttpUtil.handleResponse(requestMessage, createErrorMessage("  ", statusCode));
        }
    }

    private static HTTPCarbonMessage createErrorMessage(String payload, int statusCode) {

        HTTPCarbonMessage response = new HTTPCarbonMessage();

        response.addMessageBody(ByteBuffer.wrap(payload.getBytes(Charset.defaultCharset())));
        response.setEndOfMsgAdded(true);
        byte[] errorMessageBytes = payload.getBytes(Charset.defaultCharset());

        // TODO: Set following according to the request
        Map<String, String> transportHeaders = new HashMap<>();
        transportHeaders.put(org.wso2.carbon.transport.http.netty.common.Constants.HTTP_CONNECTION,
                org.wso2.carbon.transport.http.netty.common.Constants.CONNECTION_KEEP_ALIVE);
        transportHeaders.put(org.wso2.carbon.transport.http.netty.common.Constants.HTTP_CONTENT_TYPE,
                org.wso2.carbon.transport.http.netty.common.Constants.TEXT_PLAIN);
        transportHeaders.put(org.wso2.carbon.transport.http.netty.common.Constants.HTTP_CONTENT_LENGTH,
                (String.valueOf(errorMessageBytes.length)));

        response.setHeaders(transportHeaders);

        response.setProperty(org.wso2.carbon.transport.http.netty.common.Constants.HTTP_STATUS_CODE, statusCode);
        response.setProperty(org.wso2.carbon.messaging.Constants.DIRECTION,
                org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE);
        return response;
    }
}
