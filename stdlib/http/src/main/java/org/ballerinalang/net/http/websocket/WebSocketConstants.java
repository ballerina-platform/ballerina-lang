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

import io.ballerina.runtime.api.BStringUtils;
import io.ballerina.runtime.api.runtime.Module;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.util.BLangConstants;

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
    public static final BString ANNOTATION_ATTR_PATH = BStringUtils.fromString("path");
    public static final BString ANNOTATION_ATTR_SUB_PROTOCOLS = BStringUtils.fromString("subProtocols");
    public static final BString ANNOTATION_ATTR_IDLE_TIMEOUT = BStringUtils.fromString("idleTimeoutInSeconds");
    public static final BString ANNOTATION_ATTR_MAX_FRAME_SIZE = BStringUtils.fromString("maxFrameSize");

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

    public static final BString CLIENT_URL_CONFIG = BStringUtils.fromString("url");
    public static final BString CLIENT_SERVICE_CONFIG = BStringUtils.fromString("callbackService");
    public static final BString CLIENT_CUSTOM_HEADERS_CONFIG = BStringUtils.fromString("customHeaders");
    public static final BString CLIENT_READY_ON_CONNECT = BStringUtils.fromString("readyOnConnect");
    public static final BString WEBSOCKET_UPGRADE_SERVICE_CONFIG = BStringUtils.fromString("upgradeService");

    public static final BString RETRY_CONTEXT = BStringUtils.fromString("retryConfig");
    public static final String COUNT_DOWN_LATCH = "countDownLatch";
    public static final String CLIENT_LISTENER = "clientListener";
    public static final String CLIENT_CONNECTOR = "clientConnector";

    public static final BString CLIENT_ENDPOINT_CONFIG = BStringUtils.fromString("config");
    public static final BString TARGET_URLS = BStringUtils.fromString("targetUrls");
    public static final String FAILOVER_CONTEXT = "failoverContext";
    public static final String CONNECTOR_FACTORY = "connectorFactory";
    public static final String FAILOVER_WEBSOCKET_CLIENT = "WebSocketFailoverClient";
    public static final String FULL_FAILOVER_WEBSOCKET_CLIENT_NAME = BLangConstants.BALLERINA_PACKAGE_PREFIX +
            PACKAGE_HTTP + SEPARATOR + HTTP_MODULE_VERSION + SEPARATOR + FAILOVER_WEBSOCKET_CLIENT;

    public static final BString COMPRESSION_ENABLED_CONFIG = BStringUtils.fromString("webSocketCompressionEnabled");

    // WebSocketListener field names
    public static final BString LISTENER_ID_FIELD = BStringUtils.fromString("id");
    public static final BString LISTENER_NEGOTIATED_SUBPROTOCOLS_FIELD = BStringUtils.fromString(
            "negotiatedSubProtocol");
    public static final BString LISTENER_IS_SECURE_FIELD = BStringUtils.fromString("secure");
    public static final BString LISTENER_IS_OPEN_FIELD = BStringUtils.fromString("open");
    public static final BString LISTENER_CONNECTOR_FIELD = BStringUtils.fromString("conn");

    // WebSocketClient struct field names
    public static final BString CLIENT_RESPONSE_FIELD = BStringUtils.fromString("response");
    public static final BString CLIENT_CONNECTOR_FIELD = BStringUtils.fromString("conn");

    public static final String WEBSOCKET_ERROR_DETAILS = "Detail";

    // WebSocketConnector
    public static final BString CONNECTOR_IS_READY_FIELD = BStringUtils.fromString("isReady");

    public static final int STATUS_CODE_ABNORMAL_CLOSURE = 1006;
    public static final int STATUS_CODE_FOR_NO_STATUS_CODE_PRESENT = 1005;

    public static final int DEFAULT_MAX_FRAME_SIZE = 65536;
    public static final Module PROTOCOL_HTTP_PKG_ID = new Module(BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX,
                                                                 "http", HTTP_MODULE_VERSION);

    // Warning suppression
    public static final String UNCHECKED = "unchecked";
    public static final String THE_WEBSOCKET_CONNECTION_HAS_NOT_BEEN_MADE =
            "The WebSocket connection has not been made";

    // Strand meta data
    public static final StrandMetadata ON_OPEN_METADATA =
            new StrandMetadata(BALLERINA_ORG, PACKAGE_HTTP, HTTP_MODULE_VERSION, RESOURCE_NAME_ON_OPEN);
    public static final StrandMetadata ON_TEXT_METADATA =
            new StrandMetadata(BALLERINA_ORG, PACKAGE_HTTP, HTTP_MODULE_VERSION, RESOURCE_NAME_ON_TEXT);
    public static final StrandMetadata ON_BINARY_METADATA =
            new StrandMetadata(BALLERINA_ORG, PACKAGE_HTTP, HTTP_MODULE_VERSION, RESOURCE_NAME_ON_BINARY);
    public static final StrandMetadata ON_PING_METADATA =
            new StrandMetadata(BALLERINA_ORG, PACKAGE_HTTP, HTTP_MODULE_VERSION, RESOURCE_NAME_ON_PING);
    public static final StrandMetadata ON_PONG_METADATA =
            new StrandMetadata(BALLERINA_ORG, PACKAGE_HTTP, HTTP_MODULE_VERSION, RESOURCE_NAME_ON_PONG);
    public static final StrandMetadata ON_CLOSE_METADATA =
            new StrandMetadata(BALLERINA_ORG, PACKAGE_HTTP, HTTP_MODULE_VERSION, RESOURCE_NAME_ON_CLOSE);
    public static final StrandMetadata ON_ERROR_METADATA =
            new StrandMetadata(BALLERINA_ORG, PACKAGE_HTTP, HTTP_MODULE_VERSION, RESOURCE_NAME_ON_ERROR);
    public static final StrandMetadata ON_TIMEOUT_METADATA =
            new StrandMetadata(BALLERINA_ORG, PACKAGE_HTTP, HTTP_MODULE_VERSION, RESOURCE_NAME_ON_IDLE_TIMEOUT);

    public WebSocketConstants() {
    }

    /**
     * Specifies the error code for webSocket module.
     */
    public enum ErrorCode {

        WsConnectionClosureError("WsConnectionClosureError"),
        WsInvalidHandshakeError("WsInvalidHandshakeError"),
        WsPayloadTooBigError("WsPayloadTooBigError"),
        WsProtocolError("WsProtocolError"),
        WsConnectionError("WsConnectionError"),
        WsInvalidContinuationFrameError("WsInvalidContinuationFrameError"),
        WsGenericError("WsGenericError");

        private String errorCode;

        ErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String errorCode() {
            return errorCode;
        }
    }
}
