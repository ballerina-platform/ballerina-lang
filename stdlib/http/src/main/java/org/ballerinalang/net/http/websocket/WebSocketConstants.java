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

package org.ballerinalang.net.http.websocket;


import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_PACKAGE_PREFIX;

/**
 * Constants of WebSocket.
 */
public class WebSocketConstants {

    public static final String BALLERINA_ORG = "ballerina";
    public static final String PACKAGE_HTTP = "http";
    public static final String FULL_PACKAGE_HTTP = BALLERINA_PACKAGE_PREFIX + PACKAGE_HTTP;
    public static final String SEPARATOR = ":";
    public static final String LISTENER = "Listener";
    public static final String WEBSOCKET_CONNECTOR = "WebSocketConnector";
    public static final String WEBSOCKET_CALLER = "WebSocketCaller";
    public static final String WEBSOCKET_CLIENT = "WebSocketClient";
    public static final String WEBSOCKET_SERVICE = "WebSocketService";
    public static final String WEBSOCKET_CLIENT_SERVICE = "WebSocketClientService";
    public static final String WSS_SCHEME = "wss";
    public static final String WEBSOCKET_CALLER_NAME = PACKAGE_HTTP + SEPARATOR + WEBSOCKET_CALLER;
    public static final String FULL_WEBSOCKET_CALLER_NAME = BALLERINA_PACKAGE_PREFIX + WEBSOCKET_CALLER_NAME;
    public static final String WEBSOCKET_CLIENT_NAME = PACKAGE_HTTP + SEPARATOR + WEBSOCKET_CLIENT;
    public static final String FULL_WEBSOCKET_CLIENT_NAME = BALLERINA_PACKAGE_PREFIX + WEBSOCKET_CLIENT_NAME;


    public static final String WEBSOCKET_ANNOTATION_CONFIGURATION = "WebSocketServiceConfig";
    public static final String ANNOTATION_ATTR_PATH = "path";
    public static final String ANNOTATION_ATTR_SUB_PROTOCOLS = "subProtocols";
    public static final String ANNOTATION_ATTR_IDLE_TIMEOUT = "idleTimeoutInSeconds";
    public static final String ANNOTATION_ATTR_MAX_FRAME_SIZE = "maxFrameSize";

    public static final String RESOURCE_NAME_ON_OPEN = "onOpen";
    public static final String RESOURCE_NAME_ON_TEXT = "onText";
    public static final String RESOURCE_NAME_ON_BINARY = "onBinary";
    public static final String RESOURCE_NAME_ON_PING = "onPing";
    public static final String RESOURCE_NAME_ON_PONG = "onPong";
    public static final String RESOURCE_NAME_ON_CLOSE = "onClose";
    public static final String RESOURCE_NAME_ON_IDLE_TIMEOUT = "onIdleTimeout";
    public static final String RESOURCE_NAME_ON_ERROR = "onError";
    public static final String RESOURCE_NAME_CLOSE = "close";
    public static final String RESOURCE_NAME_PING = "ping";
    public static final String RESOURCE_NAME_PONG = "pong";
    public static final String RESOURCE_NAME_PUSH_BINARY = "pushBinary";
    public static final String RESOURCE_NAME_PUSH_TEXT = "pushText";
    public static final String RESOURCE_NAME_READY = "ready";
    public static final String RESOURCE_NAME_UPGRADE = "upgrade";

    public static final String WEBSOCKET_HANDSHAKER = "WEBSOCKET_MESSAGE";

    public static final String NATIVE_DATA_WEBSOCKET_CONNECTION_INFO = "NATIVE_DATA_WEBSOCKET_CONNECTION_INFO";
    public static final String NATIVE_DATA_BASE_PATH = "BASE_PATH";

    public static final String CLIENT_URL_CONFIG = "url";
    public static final String CLIENT_SERVICE_CONFIG = "callbackService";
    public static final String CLIENT_CUSTOM_HEADERS_CONFIG = "customHeaders";
    public static final String CLIENT_READY_ON_CONNECT = "readyOnConnect";
    public static final String WEBSOCKET_UPGRADE_SERVICE_CONFIG = "upgradeService";

    public static final String COMPRESSION_ENABLED_CONFIG = "webSocketCompressionEnabled";

    // WebSocketListener field names
    public static final String LISTENER_ID_FIELD = "id";
    public static final String LISTENER_NEGOTIATED_SUBPROTOCOLS_FIELD = "negotiatedSubProtocol";
    public static final String LISTENER_IS_SECURE_FIELD = "secure";
    public static final String LISTENER_IS_OPEN_FIELD = "open";
    public static final String LISTENER_CONNECTOR_FIELD = "conn";

    // WebSocketClient struct field names
    public static final String CLIENT_RESPONSE_FIELD = "response";
    public static final String CLIENT_CONNECTOR_FIELD = "conn";

    public static final String WEBSOCKET_ERROR_DETAILS = "Detail";

    // WebSocketConnector
    public static final String CONNECTOR_IS_READY_FIELD = "isReady";

    public static final int STATUS_CODE_ABNORMAL_CLOSURE = 1006;
    public static final int STATUS_CODE_FOR_NO_STATUS_CODE_PRESENT = 1005;

    public static final int DEFAULT_MAX_FRAME_SIZE = 65536;

    // Warning suppression
    public static final String UNCHECKED = "unchecked";
    public static final String THE_WEBSOCKET_CONNECTION_HAS_NOT_BEEN_MADE =
            "The WebSocket connection has not been made";

    private WebSocketConstants() {
    }

    /**
     * Specifies the error code for webSocket module.
     */
    public enum ErrorCode {

        WsConnectionClosureError("{ballerina/http}WsConnectionClosureError"),
        WsInvalidHandshakeError("{ballerina/http}WsInvalidHandshakeError"),
        WsPayloadTooBigError("{ballerina/http}WsPayloadTooBigError"),
        WsProtocolError("{ballerina/http}WsProtocolError"),
        WsConnectionError("{ballerina/http}WsConnectionError"),
        WsInvalidContinuationFrameError("{ballerina/http}WsInvalidContinuationFrameError"),
        WsGenericError("{ballerina/http}WsGenericError");

        private String errorCode;

        ErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String errorCode() {
            return errorCode;
        }
    }
}
