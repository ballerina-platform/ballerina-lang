/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.central.client;

/**
 * Defines constants related to the central client.
 *
 * @since 1.2.0
 */
public class CentralClientConstants {

    private CentralClientConstants() {
    }

    static final String BALLERINA_PLATFORM = "Ballerina-Platform";
    static final String IDENTITY = "identity";
    static final String RESOLVED_REQUESTED_URI = "RESOLVED_REQUESTED_URI";
    static final String SSL = "SSL";
    static final String AUTHORIZATION = "Authorization";
    static final String CONTENT_TYPE = "Content-Type";
    static final String ACCEPT_ENCODING = "Accept-Encoding";
    static final String USER_AGENT = "User-Agent";
    static final String LOCATION = "Location";
    static final String ACCEPT = "Accept";
    static final String CONTENT_DISPOSITION = "Content-Disposition";
    static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    static final String VERSION_REGEX = "(\\d+\\.)(\\d+\\.)(\\d+)";
}
