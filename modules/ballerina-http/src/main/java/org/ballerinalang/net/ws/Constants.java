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

package org.ballerinalang.net.ws;

/**
 * Constants of WebSocket.
 */
public class Constants extends org.ballerinalang.net.http.Constants {

    // Common constants
    public static final String CONNECTOR_NAME = "ClientConnector";
    public static final String TO = "TO";

    public static final String WEBSOCKET_PACKAGE_NAME = "ballerina.net.ws";
    public static final String PROTOCOL_WEBSOCKET = "ws";

    public static final String CONNECTION = "Connection";
    public static final String UPGRADE = "Upgrade";
    public static final String WEBSOCKET_UPGRADE = "websocket";
    public static final String WEBSOCKET_SERVER_SESSION = "WEBSOCKET_SERVER_SESSION";
    public static final String WEBSOCKET_CLIENT_SESSION = "WEBSOCKET_CLIENT_SESSION";
    public static final String WEBSOCKET_CLIENT_SESSIONS_LIST = "WEBSOCKET_CLIENT_SESSIONS_LIST";
    public static final String IS_WEBSOCKET_SERVER = "IS_WEBSOCKET_SERVER";

    /**
     * WebSocket Annotation Constants.
     */
    public static final class WsAnnotation {

        /**
         * WebSocket service configuration annotation constants.
         */
        public static final class ServiceConfig {
            public static final String ANNOTATION_NAME = "configuration";

            /**
             * Attribute names of WebSocket Service Configuration annotation attribute constants.
             */
            public static final class Attribute {
                public static final String SUB_PROTOCOLS = "subProtocols";
                public static final String IDLE_TIMEOUT_IN_SECS = "idleTimeoutInSeconds";
                public static final String WSS_PORT = "wssPort";
            }
        }

        /**
         * WebSocket client service annotation constants.
         */
        public static final class ClientService {
            public static final String CLIENT_SERVICE = "clientService";
        }
    }

    /**
     * WebSocket Resource Names.
     */
    public static final class ResourceName {
        public static final String ON_HANDSHAKE = "onHandshake";
        public static final String ON_OPEN = "onOpen";
        public static final String ON_TEXT_MESSAGE = "onTextMessage";
        public static final String ON_BINARY_MESSAGE = "onBinaryMessage";
        public static final String ON_PING_MESSAGE = "onPingMessage";
        public static final String ON_CLOSE = "onClose";
        public static final String ON_IDLE_TIMEOUT = "onIdleTimeout";
        public static final String ON_ERROR = "onError";
    }

    /**
     * WebSocket Struct Names.
     */
    public static final class Struct {

        /**
         * Names of WebSocket Structs.
         */
        public static final class Name {
            public static final String HANDSHAKE_CONNECTION = "HandshakeConnection";
            public static final String CONNECTION = "Connection";
            public static final String TEXT_FRAME = "TextFrame";
            public static final String BINARY_FRAME = "BinaryFrame";
            public static final String PING_FRAME = "PingFrame";
            public static final String PONG_FRAME = "PongFrame";
            public static final String CLOSE_FRAME = "CloseFrame";
        }

        /**
         * Native data names relates to Structs.
         */
        public static final class NativeData {
            public static final String WEBSOCKET_MESSAGE = "NATIVE_DATA_WEBSOCKET_MESSAGE";
            public static final String WEBSOCKET_SESSION = "NATIVE_DATA_WEBSOCKET_SESSION";
            public static final String UPGRADE_HEADERS = "NATIVE_DATA_UPGRADE_HEADERS";
            public static final String PARENT_CONNECTION_ID = "NATIVE_DATA_PARENT_CONNECTION_ID";
        }
    }
}
