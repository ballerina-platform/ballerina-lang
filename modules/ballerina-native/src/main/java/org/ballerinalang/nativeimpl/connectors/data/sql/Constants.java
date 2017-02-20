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
package org.ballerinalang.nativeimpl.connectors.data.sql;

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
        public static final String DATASOURCE = "dataSource.";
    }

    /**
     * Constants for SQL DataTypes.
     */
    public static final class SQLDataTypes {
        public static final String LONG = "LONG";
        public static final String STRING = "STRING";
        public static final String VARCHAR = "VARCHAR";
        public static final String NUMERIC = "NUMERIC";
        public static final String DECIMAL = "DECIMAL";
        public static final String BIT = "BIT";
        public static final String BOOLEAN = "BOOLEAN";
        public static final String TINYINT = "TINYINT";
        public static final String SMALLINT = "SMALLINT";
        public static final String INTEGER = "INTEGER";
        public static final String BIGINT = "BIGINT";
        public static final String REAL = "REAL";
        public static final String FLOAT = "FLOAT";
        public static final String DOUBLE = "DOUBLE";
        public static final String BINARY = "BINARY";
        public static final String BLOB = "BLOB";
        public static final String CLOB = "CLOB";
        public static final String DATE = "DATE";
        public static final String TIME = "TIME";
        public static final String TIMESTAMP = "TIMESTAMP";
        public static final String ARRAY = "ARRAY";
        public static final String STRUCT = "STRUCT";
        public static final String VARINT = "VARINT";
        public static final String UUID = "UUID";
        public static final String INETADDRESS = "INETADDRESS";
    }

    /**
     * Constants for SQL Query Parameter direction.
     */
    public static final class QueryParamDirection {
        public static final int IN = 0;
        public static final int OUT = 1;
        public static final int INOUT = 2;

    }
}
