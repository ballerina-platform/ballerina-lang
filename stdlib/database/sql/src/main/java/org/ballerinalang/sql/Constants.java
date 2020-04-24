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
package org.ballerinalang.sql;

import org.ballerinalang.jvm.types.BPackage;

import java.util.UUID;

/**
 * Constants for SQL client.
 *
 * @since 1.2.0
 */
public final class Constants {

    public static final String CONNECTOR_NAME = "ClientConnector";
    public static final BPackage SQL_PACKAGE_ID = new BPackage("ballerina", "sql");
    public static final String DATABASE_CLIENT = "Client";
    public static final String CONNECTOR_ID_KEY = UUID.randomUUID().toString();

    public static final String DATABASE_ERROR_DATA = "DatabaseErrorData";
    public static final String DATABASE_ERROR_CODE = "{ballerina/sql}DatabaseError";

    public static final String APPLICATION_ERROR_DATA = "ApplicationErrorData";
    public static final String APPLICATION_ERROR_CODE = "{ballerina/sql}ApplicationError";

    public static final String DATABASE_ERROR_MESSAGE = "Database Error Occurred";

    public static final String RESULT_ITERATOR_OBJECT = "ResultIterator";
    public static final String RESULT_SET_NATIVE_DATA_FIELD = "ResultSet";
    public static final String CONNECTION_NATIVE_DATA_FIELD = "Connection";
    public static final String STATEMENT_NATIVE_DATA_FIELD = "Statement";
    public static final String COLUMN_DEFINITIONS_DATA_FIELD = "ColumnDefinition";
    public static final String RECORD_TYPE_DATA_FIELD = "recordType";

    public static final String TIMEZONE_UTC = "UTC";

    public static final String EXCUTE_RESULT_RECORD = "ExecuteResult";
    public static final String AFFECTED_ROW_COUNT_FIELD = "affectedRowCount";
    public static final String LAST_INSERTED_ID_FIELD = "lastInsertId";

    public static final String READ_BYTE_CHANNEL_STRUCT = "ReadableByteChannel";
    public static final String READ_CHAR_CHANNEL_STRUCT = "ReadableCharacterChannel";

    /**
     * Constants related connection pool.
     */
    public static final class ConnectionPool {
        public static final String MAX_OPEN_CONNECTIONS = "maxOpenConnections";
        public static final String MAX_CONNECTION_LIFE_TIME_SECONDS = "maxConnectionLifeTimeInSeconds";
        public static final String MIN_IDLE_CONNECTIONS = "minIdleConnections";
    }

    /**
     * Constants related to database options.
     */
    public static final class Options {
        public static final String URL = "url";
    }

    /**
     * Constant related error fields.
     */
    public static final class ErrorRecordFields {
        public static final String MESSAGE = "message";
        public static final String ERROR_CODE = "errorCode";
        public static final String SQL_STATE = "sqlState";

    }

    /**
     * Constants related to parameterized string fields.
     */
    public static final class ParameterizedStingFields {
        public static final String PARTS = "parts";
        public static final String INSERTIONS = "insertions";
    }

    /**
     * Constants related to TypedValue fields.
     */
    public static final class TypedValueFields {
        public static final String SQL_TYPE = "sqlType";
        public static final String VALUE = "value";
    }

    /**
     * Constants related to SQL Types supported.
     */
    public static final class SqlTypes {
        public static final String VARCHAR = "VARCHAR";
        public static final String CHAR = "CHAR";
        public static final String LONGVARCHAR = "LONGVARCHAR";
        public static final String NCHAR = "NCHAR";
        public static final String LONGNVARCHAR = "LONGNVARCHAR";
        public static final String NVARCHAR = "NVARCHAR";
        public static final String BIT = "BIT";
        public static final String BOOLEAN = "BOOLEAN";
        public static final String TINYINT = "TINYINT";
        public static final String SMALLINT = "SMALLINT";
        public static final String INTEGER = "INTEGER";
        public static final String BIGINT = "BIGINT";
        public static final String NUMERIC = "NUMERIC";
        public static final String DECIMAL = "DECIMAL";
        public static final String REAL = "REAL";
        public static final String FLOAT = "FLOAT";
        public static final String DOUBLE = "DOUBLE";
        public static final String BINARY = "BINARY";
        public static final String BLOB = "BLOB";
        public static final String LONGVARBINARY = "LONGVARBINARY";
        public static final String VARBINARY = "VARBINARY";
        public static final String CLOB = "CLOB";
        public static final String NCLOB = "NCLOB";
        public static final String DATE = "DATE";
        public static final String TIME = "TIME";
        public static final String DATETIME = "DATETIME";
        public static final String TIMESTAMP = "TIMESTAMP";
        public static final String ARRAY = "ARRAY";
        public static final String STRUCT = "STRUCT";
    }

    /**
     * Constants for SQL Params.
     */
    public static final class SQLParamsFields {
        public static final String URL = "url";
        public static final String USER = "user";
        public static final String PASSWORD = "password";
        public static final String DATASOURCE_NAME = "datasourceName";
        public static final String OPTIONS = "options";
        public static final String CONNECTION_POOL = "connectionPool";
        public static final String CONNECTION_POOL_OPTIONS = "connectionPoolOptions";
    }
}
