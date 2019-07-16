/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.socket;

/**
 * Constant variable for socket related operations.
 */
public class SocketConstants {
    private SocketConstants() {
    }

    public static final String SERVER_SOCKET_KEY = "ServerSocket";
    public static final String SOCKET_KEY = "Socket";
    public static final String SOCKET_PACKAGE = "ballerina/socket";
    public static final String RESOURCE_ON_CONNECT = "onConnect";
    public static final String RESOURCE_ON_READ_READY = "onReadReady";
    public static final String RESOURCE_ON_ERROR = "onError";
    public static final String CLIENT = "Client";
    public static final String UDP_CLIENT = "UdpClient";
    public static final String CONFIG_FIELD_INTERFACE = "interface";
    public static final String CONFIG_FIELD_HOST = "host";
    public static final String CONFIG_FIELD_PORT = "port";
    public static final String LISTENER_CONFIG = "config";
    public static final String CLIENT_CONFIG = "config";

    public static final String REMOTE_PORT = "remotePort";
    public static final String LOCAL_PORT = "localPort";
    public static final String REMOTE_ADDRESS = "remoteAddress";
    public static final String LOCAL_ADDRESS = "localAddress";
    public static final String ID = "id";

    public static final String CLIENT_SERVICE_CONFIG = "callbackService";
    public static final String SOCKET_SERVICE = "socketService";
    public static final String IS_CLIENT = "isClient";

    // If default length pass as the read length then the entire buffer read.
    public static final int DEFAULT_EXPECTED_READ_LENGTH = -100;

    // Default read timeout set as 5 min.
    public static final String READ_TIMEOUT = "readTimeoutInMilliseconds";

    /**
     * Specifies the error code for socket module.
     */
    public enum ErrorCode {

        GenericError("{ballerina/socket}GenericError"), ReadTimedOutError("{ballerina/socket}ReadTimedOut");

        private String errorCode;

        ErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String errorCode() {
            return errorCode;
        }
    }

}
