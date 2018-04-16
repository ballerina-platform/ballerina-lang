/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.logging.util;

/**
 * Constants used by the Ballerina logging module.
 *
 * @since 0.95.0
 */
public class Constants {

    public static final String LOG_LEVEL = ".loglevel";

    public static final String HTTP_TRACE_LOG = "tracelog.http";
    public static final String HTTP_ACCESS_LOG = "accesslog.http";
    public static final String BALLERINA_USER_LOG_LEVEL = "b7a.log.level";

    public static final String LOG_TO = "logto";
    public static final String LOG_TO_CONSOLE = "console";
    public static final String LOG_TO_SOCKET = "socket";
    public static final String LOG_PUBLISH_DEFAULT_HOST = "localhost";
    public static final int LOG_PUBLISH_DEFAULT_PORT = 5010;
}
