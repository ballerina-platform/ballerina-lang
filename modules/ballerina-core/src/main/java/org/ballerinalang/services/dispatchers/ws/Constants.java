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

package org.ballerinalang.services.dispatchers.ws;

/**
 * Constants of WebSocket.
 */
public class Constants extends org.ballerinalang.services.dispatchers.http.Constants {

    // Common constants
    public static final String DEFAULT = "DEFAULT";
    public static final String CONNECTOR_NAME = "ClientConnector";
    public static final String TO = "TO";
    public static final String REMOTE_ADDRESS = "REMOTE_ADDRESS";

    // WebSocket constants
    public static final String WEBSOCKET_CLOSE_CODE = "WEBSOCKET_CLOSE_CODE";
    public static final String WEBSOCKET_CLOSE_REASON = "WEBSOCKET_CLOSE_REASON";

    public static final String WEBSOCKET_PACKAGE_PATH = "ballerina.net.ws";

    public static final String PROTOCOL_WEBSOCKET = "ws";
    public static final String ANNOTATION_NAME_WEBSOCKET_UPGRADE_PATH = "WebSocketUpgradePath";
    public static final String ANNOTATION_NAME_WEBSOCKET_CLIENT_SERVICE = "ClientService";
    public static final String ANNOTATION_NAME_ON_OPEN = "OnOpen";
    public static final String ANNOTATION_NAME_ON_TEXT_MESSAGE = "OnTextMessage";
    public static final String ANNOTATION_NAME_ON_BINARY_MESSAGE = "OnBinaryMessage";
    public static final String ANNOTATION_NAME_ON_PONG_MESSAGE = "OnPongMessage";
    public static final String ANNOTATION_NAME_ON_CLOSE = "OnClose";
    public static final String ANNOTATION_NAME_ON_ERROR = "OnError";
    public static final String IS_WEBSOCKET_SERVER = "IS_WEBSOCKET_SERVER";
    public static final String WEBSOCKET_CLIENT_ID = "WEBSOCKET_CLIENT_ID";

    public static final String CONNECTION = "Connection";
    public static final String UPGRADE = "Upgrade";
    public static final String WEBSOCKET_UPGRADE = "websocket";
    public static final String CHANNEL_ID = "CHANNEL_ID";
    public static final String WEBSOCKET_SERVER_SESSION = "WEBSOCKET_SERVER_SESSION";
    public static final String WEBSOCKET_CLIENT_SESSION = "WEBSOCKET_CLIENT_SESSION";
    public static final String WEBSOCKET_CLIENT_SESSIONS_LIST = "WEBSOCKET_CLIENT_SESSIONS_LIST";
    public static final String WEBSOCKET_MESSAGE = "WEBSOCKET_MESSAGE";
}
