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

    // Common constants
    public static final String CONNECTOR_NAME = "WsClient";
    public static final String TO = "TO";

    public static final String WEBSOCKET_SERVICE_ENDPOINT_NAME = "ballerina.net.http:WebSocketService";
    public static final String PROTOCOL_WEBSOCKET = "ws";

    public static final String WEBSOCKET_ANNOTATION_CONFIGURATION = "webSocketServiceConfig";
    public static final String ANNOTATION_WEBSOCKET_CLIENT_SERVICE = "clientService";
    public static final String ANNOTATION_ATTR_SUB_PROTOCOLS = "subProtocols";
    public static final String ANNOTATION_ATTR_IDLE_TIMEOUT = "idleTimeoutInSeconds";
    public static final String ANN_CONFIG_ATTR_WSS_PORT = "wssPort";

    public static final String RESOURCE_NAME_ON_HANDSHAKE = "onHandshake";
    public static final String RESOURCE_NAME_ON_OPEN = "onOpen";
    public static final String RESOURCE_NAME_ON_TEXT_MESSAGE = "onTextMessage";
    public static final String RESOURCE_NAME_ON_BINARY_MESSAGE = "onBinaryMessage";
    public static final String RESOURCE_NAME_ON_PING = "onPing";
    public static final String RESOURCE_NAME_ON_PONG = "onPong";
    public static final String RESOURCE_NAME_ON_CLOSE = "onClose";
    public static final String RESOURCE_NAME_ON_IDLE_TIMEOUT = "onIdleTimeout";
    public static final String RESOURCE_NAME_ON_ERROR = "onError";
    public static final String IS_WEBSOCKET_SERVER = "IS_WEBSOCKET_SERVER";

    public static final String CONNECTION = "Connection";
    public static final String UPGRADE = "Upgrade";
    public static final String WEBSOCKET_UPGRADE = "websocket";
    public static final String WEBSOCKET_SERVER_SESSION = "WEBSOCKET_SERVER_SESSION";
    public static final String WEBSOCKET_CLIENT_SESSION = "WEBSOCKET_CLIENT_SESSION";
    public static final String WEBSOCKET_CLIENT_SESSIONS_LIST = "WEBSOCKET_CLIENT_SESSIONS_LIST";
    public static final String WEBSOCKET_MESSAGE = "WEBSOCKET_MESSAGE";

    public static final String STRUCT_WEBSOCKET_HANDSHAKE_CONNECTION = "HandshakeConnection";
    public static final String STRUCT_WEBSOCKET_TEXT_FRAME = "TextFrame";
    public static final String STRUCT_WEBSOCKET_BINARY_FRAME = "BinaryFrame";
    public static final String STRUCT_WEBSOCKET_PING_FRAME = "PingFrame";
    public static final String STRUCT_WEBSOCKET_PONG_FRAME = "PongFrame";
    public static final String STRUCT_WEBSOCKET_CLOSE_FRAME = "CloseFrame";
    public static final String STRUCT_WEBSOCKET_ERROR = "WsConnectorError";


    public static final String NATIVE_DATA_WEBSOCKET_SESSION = "NATIVE_DATA_WEBSOCKET_SESSION";
    public static final String NATIVE_DATA_UPGRADE_HEADERS = "NATIVE_DATA_UPGRADE_HEADERS";
    public static final String NATIVE_DATA_PING_TIME_VALIDATOR = "NATIVE_DATA_PING_TIME_VALIDATOR";

    public static final String NATIVE_DATA_QUERY_PARAMS = "NATIVE_DATA_QUERY_PARAMS";

    public static final String WEBSOCKET_CONNECTOR = "WebSocketConnector";
    public static final String CLIENT_URL_CONFIG = "url";
    public static final String CLIENT_SERVICE_CONFIG = "callbackService";
    public static final String CLIENT_SUBPROTOCOLS_CONFIG = "subProtocols";
    public static final String CLIENT_CUSTOMHEADERS_CONFIG = "customHeaders";
    public static final String CLIENT_IDLETIMOUT_CONFIG = "idleTimeoutInSeconds";
    public static final String CLIENT_CONNECTOR_CONFIGS = "clientEndpointConfigs";
    public static final String WEBSOCKET_UPGRADE_CONFIG = "webSocketUpgrade";
    public static final String WEBSOCKET_UPGRADE_SERVICE_CONFIG = "upgradeService";


}
