/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.io.socket;

/**
 * Constant variable for socket related operations.
 */
public class SocketConstants {

    public static final String PKCS_STORE_TYPE = "PKCS12";
    public static final String DEFAULT_SSL_PROTOCOL = "TLS";
    public static final String KEY_STORE_OPTION_FIELD = "keyStoreFile";
    public static final String KEY_STORE_PASS_OPTION_FIELD = "keyStorePassword";
    public static final String TRUST_STORE_OPTION_FIELD = "trustStoreFile";
    public static final String TRUST_STORE_PASS_OPTION_FIELD = "trustStorePassword";
    public static final String CERT_PASS_OPTION_FIELD = "certPassword";
    public static final String SSL_ENABLED_PROTOCOLS_OPTION_FIELD = "sslEnabledProtocols";
    public static final String CIPHERS_OPTION_FIELD = "ciphers";
    public static final String SSL_PROTOCOL_OPTION_FIELD = "sslProtocol";

    public static final String LOCAL_PORT_OPTION_FIELD = "localPort";
    public static final String LOCAL_ADDRESS_FIELD = "localAddress";
    public static final String REMOTE_PORT_FIELD = "remotePort";
    public static final String REMOTE_ADDRESS_FIELD = "remoteAddress";

    public static final String SERVER_SOCKET_KEY = "ServerSocket";
    public static final String SOCKET_KEY = "Socket";

    public static final String SOCKET_PACKAGE = "ballerina/io";
}
