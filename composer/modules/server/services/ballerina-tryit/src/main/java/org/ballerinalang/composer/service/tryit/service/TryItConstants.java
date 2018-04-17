/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.composer.service.tryit.service;

/**
 * Constants for the try it service and implementation.
 */
public class TryItConstants {
    // Supported Protocols
    public static final String HTTP_PROTOCOL = "http";
    public static final String HTTPS_PROTOCOL = "https";
    public static final String JMS_PROTOCOL = "jms";
    public static final String WS_PROTOCOL = "ws";
    public static final String FILE_PROTOCOL = "file";
    public static final String BASE_URL = "baseUrl";
    
    // Constants for HTTP Client.
    public static final String HTTP_METHOD = "httpMethod";
    public static final String APPEND_URL = "appendUrl";
    public static final String REQUEST_BODY = "requestBody";
    public static final String REQUEST_HEADERS = "requestHeaders";
    public static final String REQUEST_URL = "requestUrl";
    public static final String CONTENT_TYPE = "contentType";
    public static final String RESPONSE_CODE = "responseCode";
    public static final String RESPONSE_BODY = "responseBody";
    public static final String RESPONSE_HEADERS = "responseHeaders";
    public static final String RETURNED_REQUEST_HEADERS = "returnedRequestHeaders";
    public static final String TIME_CONSUMED = "timeConsumed";
}
