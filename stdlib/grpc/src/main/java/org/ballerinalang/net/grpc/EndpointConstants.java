/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.net.grpc;

/**
 * gRPC Endpoint Constants Class.
 *
 * @since 1.0.0
 */
public class EndpointConstants {
    //Client Endpoint

    public static final int SERVICE_ENDPOINT_INDEX = 0;
    public static final String CLIENT_END_POINT = "clientEndpoint";

    public static final String PROTOCOL_HTTPS = "https";
    public static final String PKCS_STORE_TYPE = "PKCS12";
    public static final String HTTP_DEFAULT_HOST = "localhost";
    public static final String ENDPOINT_CONFIG = "config";

    //Service Endpoint Config
    public static final String ENDPOINT_CONFIG_HOST = "host";
    public static final String ENDPOINT_CONFIG_PORT = "port";
    public static final String ENDPOINT_CONFIG_SSL = "ssl";

    //SslConfiguration indexes
    public static final String SSL_CONFIG_KEY_STORE_FILE = "keyStoreFile";
    public static final String SSL_CONFIG_KEY_STORE_PASSWORD = "keyStorePassword";
    public static final String SSL_CONFIG_STRUST_STORE_FILE = "trustStoreFile";
    public static final String SSL_CONFIG_STRUST_STORE_PASSWORD = "trustStorePassword";
    public static final String SSL_CONFIG_SSL_VERIFY_CLIENT = "sslVerifyClient";
    public static final String SSL_CONFIG_CERT_PASSWORD = "certPassword";
    public static final String SSL_CONFIG_SSL_PROTOCOL = "sslProtocol";
    public static final String SSL_CONFIG_CACHE_SIZE = "cacheSize";
    public static final String SSL_TLS_STORE_TYPE =   "tlsStoreType";
    public static final String SSL_CONFIG_CACHE_VALIDITY_PERIOD = "cacheValidityPeriod";
    public static final String SSL_CONFIG_VALIDATE_CERT_ENABLED = "validateCertEnabled";

}
