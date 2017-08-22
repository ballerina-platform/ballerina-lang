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

package org.ballerinalang.services.dispatchers.http;

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
    public static final String HTTP_CONTENT_LENGTH = "Content-Length";
    public static final String USER_AGENT_HEADER = "User-Agent";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String ACCEPT_HEADER = "Accept";
    public static final String ALLOW = "Allow";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String PROTOCOL = "PROTOCOL";
    public static final String HOST = "HOST";
    public static final String PORT = "PORT";
    public static final String TO = "TO";
    public static final String HTTP_DEFAULT_HOST = "0.0.0.0";

    public static final String HTTP_PACKAGE_PATH = "ballerina.net.http";

    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String HTTP_METHOD_PUT = "PUT";
    public static final String HTTP_METHOD_PATCH = "PATCH";
    public static final String HTTP_METHOD_DELETE = "DELETE";
    public static final String HTTP_METHOD_OPTIONS = "OPTIONS";
    public static final String HTTP_METHOD_HEAD = "HEAD";

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
    public static final String ANN_CONFIG_ATTR_KEY_STORE_PASS = "keyStorePass";
    public static final String ANN_CONFIG_ATTR_CERT_PASS = "certPass";
    public static final String ANNOTATION_METHOD_GET = HTTP_METHOD_GET;
    public static final String ANNOTATION_METHOD_POST = HTTP_METHOD_POST;
    public static final String ANNOTATION_METHOD_PUT = HTTP_METHOD_PUT;
    public static final String ANNOTATION_METHOD_PATCH = HTTP_METHOD_PATCH;
    public static final String ANNOTATION_METHOD_DELETE = HTTP_METHOD_DELETE;
    public static final String ANNOTATION_METHOD_OPTIONS = HTTP_METHOD_OPTIONS;

    public static final String ANNOTATION_SOURCE_KEY_INTERFACE = "interface";
    public static final String VALUE_ATTRIBUTE = "value";

    public static final String COOKIE_HEADER = "Cookie";
    public static final String SESSION_ID = "BSESSIONID=";
    public static final String PATH = "Path=";
    public static final String RESPONSE_COOKIE_HEADER = "Set-Cookie";

    public static final String ORIGIN = "Origin";
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
}
