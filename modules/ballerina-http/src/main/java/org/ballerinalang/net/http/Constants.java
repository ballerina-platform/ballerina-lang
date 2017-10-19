/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http;

/**
 * Constants for HTTP.
 *
 * @since 0.8.0
 */
public class Constants {

    public static final String BASE_PATH = "BASE_PATH";
    public static final String SUB_PATH = "SUB_PATH";
    public static final String QUERY_STR = "QUERY_STR";
    public static final String RAW_QUERY_STR = "RAW_QUERY_STR";

    public static final String DEFAULT_INTERFACE = "0.0.0.0:8080";
    public static final String DEFAULT_BASE_PATH = "/";
    public static final String DEFAULT_SUB_PATH = "/*";

    public static final String PROTOCOL_HTTP = "http";
    public static final String PROTOCOL_PACKAGE_HTTP = "ballerina.net.http";
    public static final String PROTOCOL_HTTPS = "https";
    public static final String HTTP_STATUS_CODE = "HTTP_STATUS_CODE";
    public static final String HTTP_REASON_PHRASE = "HTTP_REASON_PHRASE";
    public static final String PROTOCOL = "PROTOCOL";
    public static final String PORT = "PORT";
    public static final String TO = "TO";
    public static final String HTTP_DEFAULT_HOST = "0.0.0.0";

    public static final String HTTP_PACKAGE_PATH = "ballerina.net.http";

    public static final String VALUE_ATTRIBUTE = "value";

    public static final String SESSION_ID = "BSESSIONID=";
    public static final String PATH = "Path=";

    public static final String ALLOW_ORIGIN = "allowOrigins";
    public static final String ALLOW_CREDENTIALS = "allowCredentials";
    public static final String ALLOW_METHODS = "allowMethods";
    public static final String MAX_AGE = "maxAge";
    public static final String ALLOW_HEADERS = "allowHeaders";
    public static final String EXPOSE_HEADERS = "exposeHeaders";
    public static final String PREFLIGHT_RESOURCES = "PREFLIGHT_RESOURCES";
    public static final String RESOURCES_CORS = "RESOURCES_CORS";


    public static final String CONNECTOR_NAME = "ClientConnector";

    public static final String REQUEST_URL = "REQUEST_URL";
    public static final String SRC_HANDLER = "SRC_HANDLER";

    public static final String REQUEST = "Request";
    public static final String RESPONSE = "Response";
    public static final String TYPE_STRING = "string";
    public static final String TRANSPORT_MESSAGE = "transport_message";
    public static final String HTTP_SESSION = "http_session";

    public static final String HTTP_TRANSPORT_CONF = "transports.netty.conf";
    public static final String CIPHERS = "ciphers";
    public static final String SSL_ENABLED_PROTOCOLS = "sslEnabledProtocols";
    public static final int OPTIONS_STRUCT_INDEX = 0;
    public static final int ENDPOINT_TIMEOUT_STRUCT_INDEX = 1;
    public static final int SSL_STRUCT_INDEX = 1;
    public static final int FOLLOW_REDIRECT_STRUCT_INDEX = 0;
    public static final int FOLLOW_REDIRECT_INDEX = 0;
    public static final int MAX_REDIRECT_COUNT = 0;
    public static final int TRUST_STORE_FILE_INDEX = 0;
    public static final int TRUST_STORE_PASSWORD_INDEX = 1;
    public static final int KEY_STORE_FILE_INDEX = 2;
    public static final int KEY_STORE_PASSWORD_INDEX = 3;
    public static final int SSL_ENABLED_PROTOCOLS_INDEX = 4;
    public static final int CIPHERS_INDEX = 5;
    public static final int SSL_PROTOCOL_INDEX = 6;

    /**
     * Http Annotation Constants.
     */
    public static final class HttpAnnotation {
        /**
         * Service Configuration annotation constants.
         */
        public static final class ServiceConfig {
            public static final String ANNOTATION_NAME = "configuration";

            /**
             * Service Configuration annotation attribute constants.
             */
            public static final class Attribute {
                public static final String HOST = "host";
                public static final String PORT = "port";
                public static final String HTTPS_PORT = "httpsPort";
                public static final String BASE_PATH = "basePath";
                public static final String SCHEME = "scheme";
                public static final String KEY_STORE_FILE = "keyStoreFile";
                public static final String KEY_STORE_PASS = "keyStorePassword";
                public static final String TRUST_STORE_FILE = "trustStoreFile";
                public static final String TRUST_STORE_PASS = "trustStorePassword";
                public static final String CERT_PASS = "certPassword";
                public static final String SSL_VERIFY_CLIENT = "sslVerifyClient";
                public static final String SSL_ENABLED_PROTOCOLS = "sslEnabledProtocols";
                public static final String CIPHERS = "ciphers";
                public static final String SSL_PROTOCOL = "sslProtocol";
            }
        }

        /**
         * Resource Configuration annotation constants.
         */
        public static final class ResourceConfig {
            public static final String ANNOTATION_NAME = "resourceConfig";

            /**
             * Service Configuration annotation attribute constants.
             */
            public static final class Attribute {
                public static final String METHODS = "methods";
                public static final String PATH = "path";
                public static final String CONSUMES = "consumes";
                public static final String PRODUCES = "produces";
            }

        }

    }

    /**
     * Http Methods.
     */
    public static final class HttpMethod {
        public static final String HTTP_METHOD = "HTTP_METHOD";
        public static final String GET = "GET";
        public static final String POST = "POST";
        public static final String PUT = "PUT";
        public static final String PATCH = "PATCH";
        public static final String DELETE = "DELETE";
        public static final String OPTIONS = "OPTIONS";
        public static final String HEAD = "HEAD";
    }


    /**
     * Http Headers.
     */
    public static final class HttpHeader {
        public static final String USER_AGENT = "User-Agent";
        public static final String HTTP_CONTENT_LENGTH = "Content-Length";
        public static final String ACCEPT = "Accept";
        public static final String COOKIE = "Cookie";
        public static final String RESPONSE_COOKIE = "Set-Cookie";
        public static final String ORIGIN = "Origin";
        public static final String ALLOW = "Allow";

        /**
         * Content type HTTP headers.
         */
        public static final class ContentType {
            public static final String CONTENT_TYPE = "Content-Type";
            public static final String APPLICATION_JSON = "application/json";
            public static final String APPLICATION_XML = "application/xml";
            public static final String TEXT_PLAIN = "text/plain";
            public static final String OCTET_STREAM = "application/octet-stream";
            public static final String APPLICATION_FORM = "application/x-www-form-urlencoded";
        }

        /**
         * Access control HTTP Headers.
         */
        public static final class AccessControl {
            public static final String REQUEST_METHOD = "Access-Control-Request-Method";
            public static final String REQUEST_HEADERS = "Access-Control-Request-Headers";
            public static final String ALLOW_ORIGIN = "Access-Control-Allow-Origin";
            public static final String ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
            public static final String ALLOW_METHODS = "Access-Control-Allow-Methods";
            public static final String MAX_AGE = "Access-Control-Max-Age";
            public static final String ALLOW_HEADERS = "Access-Control-Allow-Headers";
            public static final String EXPOSE_HEADERS = "Access-Control-Expose-Headers";
        }
    }



}
