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
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.contract.ServerConnector;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import java.io.PrintStream;
import java.util.List;

/**
 * Utility class providing utility methods.
 */
public class HttpUtil {
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
}
