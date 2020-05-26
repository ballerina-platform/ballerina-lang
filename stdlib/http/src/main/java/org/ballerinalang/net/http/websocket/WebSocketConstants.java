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

import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.util.BLangConstants;
import org.ballerinalang.jvm.values.api.BString;

import static org.ballerinalang.net.http.HttpConstants.HTTP_MODULE_VERSION;

/**
 * Constants of WebSocket.
 */
public class WebSocketConstants {

    public static final String BALLERINA_ORG = "ballerina";
    public static final String PACKAGE_HTTP = "http";
    public static final String FULL_PACKAGE_HTTP = BLangConstants.BALLERINA_PACKAGE_PREFIX + PACKAGE_HTTP;
    public static final String SEPARATOR = ":";
    public static final String LISTENER = "Listener";
    public static final String WEBSOCKET_CONNECTOR = "WebSocketConnector";
    public static final String WEBSOCKET_CALLER = "WebSocketCaller";
    public static final String WEBSOCKET_CLIENT = "WebSocketClient";
    public static final String WEBSOCKET_SERVICE = "WebSocketService";
    public static final String WEBSOCKET_CLIENT_SERVICE = "WebSocketClientService";
    public static final String WSS_SCHEME = "wss";
    public static final String WS_SCHEME = "ws";
    public static final String WEBSOCKET_CALLER_NAME = BLangConstants.BALLERINA_PACKAGE_PREFIX +
            PACKAGE_HTTP + SEPARATOR + HTTP_MODULE_VERSION + SEPARATOR + WEBSOCKET_CALLER;
    public static final String WEBSOCKET_CLIENT_NAME = PACKAGE_HTTP + SEPARATOR + WEBSOCKET_CLIENT;
    public static final String FULL_WEBSOCKET_CLIENT_NAME = BLangConstants.BALLERINA_PACKAGE_PREFIX +
            PACKAGE_HTTP + SEPARATOR + HTTP_MODULE_VERSION + SEPARATOR + WEBSOCKET_CLIENT;

    public static final String WEBSOCKET_ANNOTATION_CONFIGURATION = "WebSocketServiceConfig";
    public static final BString ANNOTATION_ATTR_PATH = StringUtils.fromString("path");
    public static final BString ANNOTATION_ATTR_SUB_PROTOCOLS = StringUtils.fromString("subProtocols");
    public static final BString ANNOTATION_ATTR_IDLE_TIMEOUT = StringUtils.fromString("idleTimeoutInSeconds");
    public static final BString ANNOTATION_ATTR_MAX_FRAME_SIZE = StringUtils.fromString("maxFrameSize");

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

    public static final BString CLIENT_URL_CONFIG = StringUtils.fromString("url");
    public static final BString CLIENT_SERVICE_CONFIG = StringUtils.fromString("callbackService");
    public static final BString CLIENT_CUSTOM_HEADERS_CONFIG = StringUtils.fromString("customHeaders");
    public static final BString CLIENT_READY_ON_CONNECT = StringUtils.fromString("readyOnConnect");
    public static final BString WEBSOCKET_UPGRADE_SERVICE_CONFIG = StringUtils.fromString("upgradeService");

    public static final BString RETRY_CONTEXT = StringUtils.fromString("retryConfig");
    public static final String COUNT_DOWN_LATCH = "countDownLatch";
    public static final String CLIENT_LISTENER = "clientListener";
    public static final String CLIENT_CONNECTOR = "clientConnector";

    public static final BString CLIENT_ENDPOINT_CONFIG = StringUtils.fromString("config");
    public static final BString TARGET_URLS = StringUtils.fromString("targetUrls");
    public static final String FAILOVER_CONTEXT = "failoverContext";
    public static final String CONNECTOR_FACTORY = "connectorFactory";
    public static final String FAILOVER_WEBSOCKET_CLIENT = "WebSocketFailoverClient";
    public static final String FULL_FAILOVER_WEBSOCKET_CLIENT_NAME = BLangConstants.BALLERINA_PACKAGE_PREFIX +
            PACKAGE_HTTP + SEPARATOR + HTTP_MODULE_VERSION + SEPARATOR + FAILOVER_WEBSOCKET_CLIENT;

    public static final BString COMPRESSION_ENABLED_CONFIG = StringUtils.fromString("webSocketCompressionEnabled");

    // WebSocketListener field names
    public static final BString LISTENER_ID_FIELD = StringUtils.fromString("id");
    public static final BString LISTENER_NEGOTIATED_SUBPROTOCOLS_FIELD = StringUtils.fromString(
            "negotiatedSubProtocol");
    public static final BString LISTENER_IS_SECURE_FIELD = StringUtils.fromString("secure");
    public static final BString LISTENER_IS_OPEN_FIELD = StringUtils.fromString("open");
    public static final BString LISTENER_CONNECTOR_FIELD = StringUtils.fromString("conn");

    // WebSocketClient struct field names
    public static final BString CLIENT_RESPONSE_FIELD = StringUtils.fromString("response");
    public static final BString CLIENT_CONNECTOR_FIELD = StringUtils.fromString("conn");

    public static final String WEBSOCKET_ERROR_DETAILS = "Detail";

    // WebSocketConnector
    public static final BString CONNECTOR_IS_READY_FIELD = StringUtils.fromString("isReady");

    public static final int STATUS_CODE_ABNORMAL_CLOSURE = 1006;
    public static final int STATUS_CODE_FOR_NO_STATUS_CODE_PRESENT = 1005;

    public static final int DEFAULT_MAX_FRAME_SIZE = 65536;
    public static final BPackage PROTOCOL_HTTP_PKG_ID = new BPackage(BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX,
            "http", HTTP_MODULE_VERSION);

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
