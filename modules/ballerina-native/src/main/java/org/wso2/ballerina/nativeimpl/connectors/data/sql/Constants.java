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

package org.wso2.ballerina.nativeimpl.connectors.data.sql;

/**
 * Constants for SQL Data Connectors.
 *
 * @since 0.8.0
 */
public final class Constants {

    /**
     * Constants for HikariCP pool configurations.
     */
    public static final class PoolProperties {
        public static final String DATA_SOURCE_CLASSNAME = "dataSourceClassName";
        public static final String JDBC_URL = "jdbcUrl";
        public static final String USER_NAME = "username";
        public static final String PASSWORD = "password";
        public static final String AUTO_COMMIT = "autoCommit";
        public static final String CONNECTION_TIMEOUT = "connectionTimeout";
        public static final String IDLE_TIMEOUT = "idleTimeout";
        public static final String MAX_LIFETIME = "maxLifetime";
        public static final String CONNECTION_TEST_QUERY = "connectionTestQuery";
        public static final String MINIMUM_IDLE = "minimumIdle";
        public static final String MAXIMUM_POOL_SIZE = "maximumPoolSize";
        public static final String POOOL_NAME = "poolName";
        public static final String ISOLATE_INTERNAL_QUERIES = "isolateInternalQueries";
        public static final String ALLOW_POOL_SUSPENSION = "allowPoolSuspension";
        public static final String READ_ONLY = "readOnly";
        public static final String REGISTER_MBEANS = "registerMbeans";
        public static final String CATALOG = "catalog";
        public static final String CONNECTION_INIT_SQL = "connectionInitSql";
        public static final String DRIVER_CLASSNAME = "driverClassName";
        public static final String TRANSACTION_ISOLATION = "transactionIsolation";
        public static final String VALIDATION_TIMEOUT = "validationTimeout";
        public static final String LEAK_DETECTION_THRESHOLD = "leakDetectionThreshold";
    }
}
