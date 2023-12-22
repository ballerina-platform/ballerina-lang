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
    static final String APPLICATION_JSON = "application/json";
    static final String ORGANIZATION = "organization";
    static final String VERSION = "version";
    static final String BALA_URL = "balaURL";
    static final String PLATFORM = "platform";
    static final String ANY_PLATFORM = "any";
    static final String PKG_NAME = "name";
    static final String IS_DEPRECATED = "isdeprecated";
    static final String DIGEST = "digest";
    static final String DEPRECATE_MESSAGE = "deprecatemessage";
    public static final String ENABLE_OUTPUT_STREAM = "enableOutputStream";
    static final String PRODUCTION_REPO = "central.ballerina.io";
    static final String STAGING_REPO = "staging-central.ballerina.io";
    static final String DEV_REPO = "dev-central.ballerina.io";
    public static final String BALLERINA_STAGE_CENTRAL = "BALLERINA_STAGE_CENTRAL";
    public static final String BALLERINA_DEV_CENTRAL = "BALLERINA_DEV_CENTRAL";
    public static final int BYTES_FOR_KB = 1024;
    public static final int PROGRESS_BAR_BYTE_THRESHOLD = 5;
    public static final int UPDATE_INTERVAL_MILLIS = 1000;
    public static final String SHA256 = "sha-256=";
    public static final String SHA256_ALGORITHM = "SHA-256";

}
