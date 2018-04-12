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

package org.ballerinalang.net.http;

/**
 * Constants of WebSocket.
 */
public class WebSocketConstants {

    public static final String WEBSOCKET_ENDPOINT_NAME = "ballerina.http:WebSocketListener";
    public static final String WEBSOCKET_CLIENT_ENDPOINT_NAME = "ballerina.http:WebSocketClient";
    public static final String WEBSOCKET_CONNECTOR = "WebSocketConnector";
    public static final String WEBSOCKET_ENDPOINT = "WebSocketListener";
    public static final String WEBSOCKET_CONNECTOR_ERROR = "WebSocketConnectorError";
    public static final String WEBSOCKET_SERVICE = "WebSocketService";
    public static final String WEBSOCKET_CLIENT_SERVICE = "WebSocketClientService";


    public static final String WEBSOCKET_ANNOTATION_CONFIGURATION = "WebSocketServiceConfig";
    public static final String ANNOTATION_ATTR_PATH = "path";
    public static final String ANNOTATION_ATTR_SUB_PROTOCOLS = "subProtocols";
    public static final String ANNOTATION_ATTR_IDLE_TIMEOUT = "idleTimeoutInSeconds";
    public static final String ANN_CONFIG_ATTR_WSS_PORT = "wssPort";

    public static final String RESOURCE_NAME_ON_OPEN = "onOpen";
    public static final String RESOURCE_NAME_ON_TEXT = "onText";
    public static final String RESOURCE_NAME_ON_BINARY = "onBinary";
    public static final String RESOURCE_NAME_ON_PING = "onPing";
    public static final String RESOURCE_NAME_ON_PONG = "onPong";
    public static final String RESOURCE_NAME_ON_CLOSE = "onClose";
    public static final String RESOURCE_NAME_ON_IDLE_TIMEOUT = "onIdleTimeout";

    public static final String WEBSOCKET_MESSAGE = "WEBSOCKET_MESSAGE";

    public static final String NATIVE_DATA_WEBSOCKET_SESSION = "NATIVE_DATA_WEBSOCKET_SESSION";
    public static final String NATIVE_DATA_UPGRADE_HEADERS = "NATIVE_DATA_UPGRADE_HEADERS";

    public static final String NATIVE_DATA_QUERY_PARAMS = "NATIVE_DATA_QUERY_PARAMS";

    public static final String CLIENT_URL_CONFIG = "url";
    public static final String CLIENT_SERVICE_CONFIG = "callbackService";
    public static final String CLIENT_SUBPROTOCOLS_CONFIG = "subProtocols";
    public static final String CLIENT_CUSTOMHEADERS_CONFIG = "customHeaders";
    public static final String CLIENT_IDLETIMOUT_CONFIG = "idleTimeoutInSeconds";
    public static final String CLIENT_CONNECTOR_CONFIGS = "clientEndpointConfigs";
    public static final String WEBSOCKET_UPGRADE_SERVICE_CONFIG = "upgradeService";
    public static final String WEBSOCKET_CONNECTION_MANAGER = "WEBSOCKET_CONNECTION_MANAGER";
}
