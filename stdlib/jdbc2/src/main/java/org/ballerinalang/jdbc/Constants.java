/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jdbc;

/**
 * Constants for JDBC client.
 *
 * @since 1.2.0
 */
public final class Constants {
    /**
     * Constants for Endpoint Configs.
     */
    public static final class ClientConfiguration {
        public static final String URL = "url";
        public static final String USER = "user";
        public static final String PASSWORD = "password";
        public static final String DATASOURCE_NAME = "datasourceName";
        public static final String CONNECTION_POOL_OPTIONS = "connPool";
        public static final String OPTIONS = "options";
        public static final String PROPERTIES = "properties";
    }
}
