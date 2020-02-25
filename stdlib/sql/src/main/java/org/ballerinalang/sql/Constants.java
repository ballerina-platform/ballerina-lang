/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.sql;

import org.ballerinalang.jvm.types.BPackage;

import java.util.UUID;


/**
 * Constants for JDBC client.
 *
 * @since 0.8.0
 */
public final class Constants {

    /**
     * Constants related connection pool.
     */
    public static final class ConnectionPool {
        public static final String MAX_OPEN_CONNECTIONS = "maxOpenConnections";
        public static final String MAX_CONNECTION_LIFE_TIME_SECONDS = "maxConnectionLifeTimeSeconds";
        public static final String MIN_IDLE_CONNECTIONS = "minIdleConnections";
    }


    public static final String CONNECTOR_NAME = "ClientConnector";

    public static final BPackage SQL_PACKAGE_ID = new BPackage("ballerina", "sql");
    public static final String DATABASE_CLIENT = "Client";
    public static final String CONNECTOR_ID_KEY = UUID.randomUUID().toString();

    public static final String DATABASE_ERROR_DATA = "DatabaseErrorData";
    public static final String DATABASE_ERROR_CODE = "{ballerina/sql}DatabaseError";

    public static final String APPLICATION_ERROR_DATA = "ApplicationErrorData";
    public static final String APPLICATION_ERROR_CODE = "{ballerina/sql}ApplicationError";

    public static final String APPLICATION_ERROR_MESSAGE = "Application Error Occurred";
    public static final String DATABASE_ERROR_MESSAGE = "Database Error Occurred";

    /**
     * Constants related to database options.
     */
    public static final class Options {
        public static final String URL = "url";
    }

}
