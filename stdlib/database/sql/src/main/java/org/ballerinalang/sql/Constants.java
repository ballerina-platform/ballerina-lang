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

import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.values.api.BString;

import java.util.UUID;

/**
 * Constants for SQL client.
 *
 * @since 1.2.0
 */
public final class Constants {

    public static final String CONNECTOR_NAME = "ClientConnector";
    public static final BPackage SQL_PACKAGE_ID = new BPackage("ballerina", "sql", "0.5.0");
    public static final String DATABASE_CLIENT = "Client";
    public static final String CONNECTOR_ID_KEY = UUID.randomUUID().toString();

    public static final String DATABASE_ERROR_DETAILS = "DatabaseErrorDetail";
    public static final String DATABASE_ERROR = "DatabaseError";
    public static final String APPLICATION_ERROR = "ApplicationError";
    public static final String DATABASE_ERROR_MESSAGE = "Database Error Occurred";

    public static final String RESULT_ITERATOR_OBJECT = "ResultIterator";
    public static final String RESULT_SET_NATIVE_DATA_FIELD = "ResultSet";
    public static final String CONNECTION_NATIVE_DATA_FIELD = "Connection";
    public static final String STATEMENT_NATIVE_DATA_FIELD = "Statement";
    public static final String COLUMN_DEFINITIONS_DATA_FIELD = "ColumnDefinition";
    public static final String RECORD_TYPE_DATA_FIELD = "recordType";

    public static final BString TIMEZONE_UTC = StringUtils.fromString("UTC");

    public static final String EXCUTE_RESULT_RECORD = "ExecuteResult";
    public static final String AFFECTED_ROW_COUNT_FIELD = "affectedRowCount";
    public static final String LAST_INSERTED_ID_FIELD = "lastInsertId";

    public static final String READ_BYTE_CHANNEL_STRUCT = "ReadableByteChannel";
    public static final String READ_CHAR_CHANNEL_STRUCT = "ReadableCharacterChannel";

    /**
     * Constants related connection pool.
     */
    public static final class ConnectionPool {
        public static final BString MAX_OPEN_CONNECTIONS = StringUtils.fromString("maxOpenConnections");
        public static final BString MAX_CONNECTION_LIFE_TIME_SECONDS = StringUtils.fromString(
                "maxConnectionLifeTimeInSeconds");
        public static final BString MIN_IDLE_CONNECTIONS = StringUtils.fromString("minIdleConnections");
    }

    /**
     * Constants related to database options.
     */
    public static final class Options {
        public static final BString URL = StringUtils.fromString("url");
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
        public static final BString PARTS = StringUtils.fromString("parts");
        public static final BString INSERTIONS = StringUtils.fromString("insertions");
    }

    /**
     * Constants related to TypedValue fields.
     */
    public static final class TypedValueFields {
        public static final BString VALUE = StringUtils.fromString("value");
    }

    /**
     * Constants related to SQL Types supported.
     */
    public static final class SqlTypes {
        public static final String VARCHAR = "VarcharValue";
        public static final String CHAR = "CharValue";
        public static final String TEXT = "TextValue";
        public static final String CLOB = "ClobValue";
        public static final String NCHAR = "NCharValue";
        public static final String NVARCHAR = "NVarcharValue";
        public static final String NCLOB = "NClobValue";
        public static final String SMALLINT = "SmallIntValue";
        public static final String INTEGER = "IntegerValue";
        public static final String BIGINT = "BigIntValue";
        public static final String NUMERIC = "NumericValue";
        public static final String DECIMAL = "DecimalValue";
        public static final String REAL = "RealValue";
        public static final String FLOAT = "FloatValue";
        public static final String DOUBLE = "DoubleValue";
        public static final String BIT = "BitValue";
        public static final String BOOLEAN = "BooleanValue";
        public static final String BINARY = "BinaryValue";
        public static final String VARBINARY = "VarBinaryValue";
        public static final String BLOB = "BlobValue";
        public static final String DATE = "DateValue";
        public static final String TIME = "TimeValue";
        public static final String DATETIME = "DateTimeValue";
        public static final String TIMESTAMP = "TimestampValue";
        public static final String ARRAY = "ArrayValue";
        public static final String REF = "RefValue";
        public static final String ROW = "RowValue";
        public static final String STRUCT = "StructValue";

    }

    /**
     * Constants for SQL Params.
     */
    public static final class SQLParamsFields {
        public static final BString URL = StringUtils.fromString("url");
        public static final BString USER = StringUtils.fromString("user");
        public static final BString PASSWORD = StringUtils.fromString("password");
        public static final BString DATASOURCE_NAME = StringUtils.fromString("datasourceName");
        public static final BString OPTIONS = StringUtils.fromString("options");
        public static final BString CONNECTION_POOL = StringUtils.fromString("connectionPool");
        public static final BString CONNECTION_POOL_OPTIONS = StringUtils.fromString("connectionPoolOptions");
    }
}
