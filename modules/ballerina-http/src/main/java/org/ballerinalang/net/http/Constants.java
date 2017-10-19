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
    public static final String HTTP_METHOD = "HTTP_METHOD";
    public static final String HTTP_STATUS_CODE = "HTTP_STATUS_CODE";
    public static final String HTTP_REASON_PHRASE = "HTTP_REASON_PHRASE";
    public static final String PROTOCOL = "PROTOCOL";
    public static final String PORT = "PORT";
    public static final String TO = "TO";
    public static final String HTTP_DEFAULT_HOST = "0.0.0.0";

    public static final String HTTP_PACKAGE_PATH = "ballerina.net.http";

    /* Annotations */
    public static final String ANN_NAME_RESOURCE_CONFIG = "resourceConfig";
    public static final String ANN_RESOURCE_ATTR_METHODS = "methods";
    public static final String ANN_RESOURCE_ATTR_PATH = "path";
    public static final String ANN_RESOURCE_ATTR_CONSUMES = "consumes";
    public static final String ANN_RESOURCE_ATTR_PRODUCES = "produces";
    public static final String ANN_NAME_CONFIG = "configuration";
    public static final String ANN_CONFIG_ATTR_HOST = "host";
    public static final String ANN_CONFIG_ATTR_PORT = "port";
    public static final String ANN_CONFIG_ATTR_HTTPS_PORT = "httpsPort";
    public static final String ANN_CONFIG_ATTR_BASE_PATH = "basePath";
    public static final String ANN_CONFIG_ATTR_SCHEME = "scheme";
    public static final String ANN_CONFIG_ATTR_KEY_STORE_FILE = "keyStoreFile";
    public static final String ANN_CONFIG_ATTR_KEY_STORE_PASS = "keyStorePassword";
    public static final String ANN_CONFIG_ATTR_TRUST_STORE_FILE = "trustStoreFile";
    public static final String ANN_CONFIG_ATTR_TRUST_STORE_PASS = "trustStorePassword";
    public static final String ANN_CONFIG_ATTR_CERT_PASS = "certPassword";
    public static final String ANN_CONFIG_ATTR_SSL_VERIFY_CLIENT = "sslVerifyClient";
    public static final String ANN_CONFIG_ATTR_SSL_ENABLED_PROTOCOLS = "sslEnabledProtocols";
    public static final String ANN_CONFIG_ATTR_CIPHERS = "ciphers";
    public static final String ANN_CONFIG_ATTR_SSL_PROTOCOL = "sslProtocol";
    public static final String ANNOTATION_METHOD_GET = HttpMethod.GET;
    public static final String ANNOTATION_METHOD_POST = HttpMethod.POST;
    public static final String ANNOTATION_METHOD_PUT = HttpMethod.PUT;
    public static final String ANNOTATION_METHOD_PATCH = HttpMethod.PATCH;
    public static final String ANNOTATION_METHOD_DELETE = HttpMethod.DELETE;
    public static final String ANNOTATION_METHOD_OPTIONS = HttpMethod.OPTIONS;

    public static final String ANNOTATION_SOURCE_KEY_INTERFACE = "interface";
    public static final String VALUE_ATTRIBUTE = "value";

    public static final String SESSION_ID = "BSESSIONID=";
    public static final String PATH = "Path=";

    public static final String AC_REQUEST_METHOD = "Access-Control-Request-Method";
    public static final String AC_REQUEST_HEADERS = "Access-Control-Request-Headers";
    public static final String AC_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String AC_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    public static final String AC_ALLOW_METHODS = "Access-Control-Allow-Methods";
    public static final String AC_MAX_AGE = "Access-Control-Max-Age";
    public static final String AC_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    public static final String AC_EXPOSE_HEADERS = "Access-Control-Expose-Headers";

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

    /* Annotations */
    public static final String ANNOTATION_NAME_SOURCE = "Source";
    public static final String ANNOTATION_NAME_BASE_PATH = "BasePath";
    public static final String ANNOTATION_NAME_PATH = "Path";
    public static final String HTTP_CLIENT_EXCEPTION_CATEGORY = "http-client";
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


    public static class HttpMethod {
        private HttpMethod() {
        }

        public static final String GET = "GET";
        public static final String POST = "POST";
        public static final String PUT = "PUT";
        public static final String PATCH = "PATCH";
        public static final String DELETE = "DELETE";
        public static final String OPTIONS = "OPTIONS";
        public static final String HEAD = "HEAD";
    }


    /**
     * Http Headers
     */
    public static class HttpHeader {
        private HttpHeader() {
        }

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
        public static class ContentType {
            private ContentType() {
            }

            public static final String CONTENT_TYPE = "Content-Type";
            public static final String APPLICATION_JSON = "application/json";
            public static final String APPLICATION_XML = "application/xml";
            public static final String TEXT_PLAIN = "text/plain";
            public static final String OCTET_STREAM = "application/octet-stream";
            public static final String APPLICATION_FORM = "application/x-www-form-urlencoded";
        }
    }

}
