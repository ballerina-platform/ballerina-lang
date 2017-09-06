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

    //TODO these constants are available in transport level as well, so find a better solution and remove this class
    public static final String DEFAULT_INTERFACE = "0.0.0.0:8080";

    public static final String PROTOCOL_HTTP = "http";
    public static final String HTTP_METHOD = "HTTP_METHOD";
    public static final String HTTP_STATUS_CODE = "HTTP_STATUS_CODE";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String ACCEPT_HEADER = "Accept";
    public static final String ALLOW = "Allow";
    public static final String PROTOCOL = "PROTOCOL";
    public static final String TO = "TO";

    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String HTTP_METHOD_PUT = "PUT";
    public static final String HTTP_METHOD_DELETE = "DELETE";
    public static final String HTTP_METHOD_HEAD = "HEAD";

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

    public static final String CONNECTOR_NAME = "ClientConnector";


    /**
     * Content type HTTP header.
     */
    public static final String CONTENT_TYPE = "Content-Type";
}
