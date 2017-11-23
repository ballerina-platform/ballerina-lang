/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net;

import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.net.http.HttpServerConnector;
import org.ballerinalang.net.ws.Constants;
import org.ballerinalang.net.ws.WebSocketServerConnector;

/**
 * Server Connector Utils To Get Server Connectors.
 */
public class ServerConnectorUtil {

    private final HttpServerConnector httpServerConnector;
    private final WebSocketServerConnector webSocketServerConnector;

    public ServerConnectorUtil() {
        httpServerConnector =
                (HttpServerConnector) ConnectorUtils.getBallerinaServerConnector(Constants.HTTP_PACKAGE_PATH);
        webSocketServerConnector =
                (WebSocketServerConnector) ConnectorUtils.getBallerinaServerConnector(Constants.WEBSOCKET_PACKAGE_NAME);
    }

    public HttpServerConnector getHttpServerConnector() {
        return httpServerConnector;
    }

    public WebSocketServerConnector getWebSocketServerConnector() {
        return webSocketServerConnector;
    }
}
