// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
import ballerina/io;
import ballerina/sql;
import ballerina/java.jdbc;
import ballerina/time;

function querySingleIntParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE row_id = ", ""],
        insertions: [1]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryDoubleIntParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE row_id = ", " AND int_type = ", ""],
        insertions: [1, 1]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryIntAndLongParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE row_id = ", " AND long_type = ", ""],
        insertions: [1, 9223372036854774807]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE string_type = ", ""],
        insertions: ["Hello"]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryIntAndStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE string_type = ", "AND row_id = ", ""],
        insertions: ["Hello", 1]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryDoubleParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE double_type = ", ""],
        insertions: [2139095039.0]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryFloatParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE float_type = ", ""],
        insertions: [123.34]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryDoubleAndFloatParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE float_type = ", "and double_type = ", ""],
        insertions: [123.34, 2139095039.0]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}


function queryDecimalParam(string url, string user, string password) returns @tainted record {}|error? {
    decimal decimalValue = 23.45;
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE decimal_type = ", ""],
        insertions: [decimalValue]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryDecimalAnFloatParam(string url, string user, string password) returns @tainted record {}|error? {
    decimal decimalValue = 23.45;
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE decimal_type = ", "and double_type = ", ""],
        insertions: [decimalValue, 2139095039.0]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryByteArrayParam(string url, string user, string password) returns @tainted record {}|error? {
    record {}|error? value = queryJdbcClient(url, user, password, "Select * from ComplexTypes where row_id = 1");
    byte[] binaryData = <byte[]>getUntaintedData(value, "BINARY_TYPE");

    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from ComplexTypes WHERE binary_type = ", ""],
        insertions: [binaryData]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function getUntaintedData(record {}|error? value, string fieldName) returns @untainted anydata {
    if (value is record {}) {
        return value[fieldName];
    }
    return {};
}

function queryTypeVarcharStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_VARCHAR,
        value: "Hello"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE string_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeCharStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_CHAR,
        value: "Hello"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE string_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeLongNVarcharStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_LONGNVARCHAR,
        value: "Hello"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE string_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeLongVarcharStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_LONGVARCHAR,
        value: "Hello"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE string_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeNCharStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_NCHAR,
        value: "Hello"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE string_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeNVarCharStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_NVARCHAR,
        value: "Hello"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE string_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeVarCharIntegerParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_VARCHAR,
        value: 1
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE string_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypBooleanBooleanParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_BOOLEAN,
        value: true
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE boolean_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypBooleanIntParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_BOOLEAN,
        value: 1
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE boolean_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypBitIntParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_BIT,
        value: 1
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE boolean_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypBitStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_BIT,
        value: "true"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE boolean_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypBitInvalidIntParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_BIT,
        value: 12
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE boolean_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypBitDoubleParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_BIT,
        value: 1.0
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE boolean_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeIntIntParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_INTEGER,
        value: 2147483647
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE int_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeTinyIntIntParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_TINYINT,
        value: 127
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE tinyint_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeSmallIntIntParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_SMALLINT,
        value: 32767
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE smallint_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeBigIntIntParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_BIGINT,
        value: 9223372036854774807
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE bigint_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeDoubleDoubleParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_DOUBLE,
        value: 1234.567
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE float_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeDoubleIntParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_DOUBLE,
        value: 1234
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE float_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeDoubleDecimalParam(string url, string user, string password) returns @tainted record {}|error? {
    decimal decimalVal = 1234.567;
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_DOUBLE,
        value: decimalVal
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE float_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeFloatDoubleParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_DOUBLE,
        value: 1234.567
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE float_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeRealDoubleParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_REAL,
        value: 1234.567
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE real_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeNumericDoubleParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_NUMERIC,
        value: 1234.567
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE numeric_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeNumericIntParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_NUMERIC,
        value: 1234
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE numeric_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeNumericDecimalParam(string url, string user, string password) returns @tainted record {}|error? {
    decimal decimalVal = 1234.567;
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_NUMERIC,
        value: decimalVal
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE numeric_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeDecimalDoubleParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_DECIMAL,
        value: 1234.567
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE decimal_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeDecimalDecimalParam(string url, string user, string password) returns @tainted record {}|error? {
    decimal decimalVal = 1234.567;
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_DECIMAL,
        value: decimalVal
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE decimal_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeBinaryByteParam(string url, string user, string password) returns @tainted record {}|error? {
    record {}|error? value = queryJdbcClient(url, user, password, "Select * from ComplexTypes where row_id = 1");
    byte[] binaryData = <byte[]>getUntaintedData(value, "BINARY_TYPE");
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_BINARY,
        value: binaryData
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from ComplexTypes WHERE binary_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeBinaryReadableByteChannelParam(string url, string user, string password) returns @tainted record {}|error? {
    io:ReadableByteChannel byteChannel = check getByteColumnChannel();
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_BINARY,
        value: byteChannel
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from ComplexTypes WHERE binary_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeVarBinaryReadableByteChannelParam(string url, string user, string password) returns @tainted record {}|error? {
    io:ReadableByteChannel byteChannel = check getByteColumnChannel();
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_VARBINARY,
        value: byteChannel
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from ComplexTypes WHERE var_binary_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeLongVarBinaryReadableByteChannelParam(string url, string user, string password) returns @tainted record {}|error? {
    io:ReadableByteChannel byteChannel = check getByteColumnChannel();
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_LONGVARBINARY,
        value: byteChannel
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from ComplexTypes WHERE var_binary_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeBlobByteParam(string url, string user, string password) returns @tainted record {}|error? {
    record {}|error? value = queryJdbcClient(url, user, password, "Select * from ComplexTypes where row_id = 1");
    byte[] binaryData = <byte[]>getUntaintedData(value, "BLOB_TYPE");
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_BLOB,
        value: binaryData
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from ComplexTypes WHERE blob_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeBlobReadableByteChannelParam(string url, string user, string password) returns @tainted record {}|error? {
    io:ReadableByteChannel byteChannel = check getBlobColumnChannel();
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_BLOB,
        value: byteChannel
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from ComplexTypes WHERE blob_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeClobStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_CLOB,
        value: "very long text"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from ComplexTypes WHERE clob_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeClobReadableCharChannelParam(string url, string user, string password) returns @tainted record {}|error? {
    io:ReadableCharacterChannel clobChannel = check getClobColumnChannel();
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_CLOB,
        value: clobChannel
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from ComplexTypes WHERE clob_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeNClobReadableCharChannelParam(string url, string user, string password) returns @tainted record {}|error? {
    io:ReadableCharacterChannel clobChannel = check getClobColumnChannel();
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_NCLOB,
        value: clobChannel
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from ComplexTypes WHERE clob_type = ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryDateStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_DATE,
        value: "2017-02-03"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE date_type= ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryDateString2Param(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_DATE,
        value: "2017-2-3"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE date_type= ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryDateStringInvalidParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_DATE,
        value: "2017/2/3"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE date_type= ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryDateLongParam(string url, string user, string password) returns @tainted record {}|error? {
    time:Time date = check time:parse("2017-02-03", "yyyy-MM-dd");
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_DATE,
        value: date.time
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE date_type= ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryDateTimeRecordParam(string url, string user, string password) returns @tainted record {}|error? {
    time:Time date = check time:parse("2017-02-03", "yyyy-MM-dd");
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_DATE,
        value: date
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE date_type= ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryDateTimeRecordWithTimeZoneParam(string url, string user, string password) returns @tainted record {}|error? {
    time:Time date = check time:parse("2017-02-03T09:46:22.444-0500","yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_DATE,
        value: date
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE date_type= ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTimeStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_TIME,
        value: "11:35:45"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE time_type= ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTimeStringInvalidParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_TIME,
        value: "11-35-45"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE time_type= ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTimeLongParam(string url, string user, string password) returns @tainted record {}|error? {
    time:Time date = check time:parse("11:35:45", "HH:mm:ss");
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_TIME,
        value: date.time
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE time_type= ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTimeTimeRecordParam(string url, string user, string password) returns @tainted record {}|error? {
     time:Time date = check time:parse("11:35:45", "HH:mm:ss");
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_TIME,
        value: date
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE time_type= ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTimeTimeRecordWithTimeZoneParam(string url, string user, string password) returns @tainted record {}|error? {
    time:Time date = check time:parse("2017-02-03T11:35:45","yyyy-MM-dd'T'HH:mm:ss");
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_TIME,
        value: date
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE time_type= ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTimestampStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_TIMESTAMP,
        value: "2017-02-03 11:53:00"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE timestamp_type= ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTimestampStringInvalidParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_TIMESTAMP,
        value: "2017/02/03 11:53:00"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE timestamp_type= ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTimestampLongParam(string url, string user, string password) returns @tainted record {}|error? {
    time:Time date = check time:parse("2017-02-03 11:53:00", "yyyy-MM-dd HH:mm:ss");
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_TIMESTAMP,
        value: date.time
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE timestamp_type= ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTimestampTimeRecordParam(string url, string user, string password) returns @tainted record {}|error? {
     time:Time date = check time:parse("2017-02-03 11:53:00", "yyyy-MM-dd HH:mm:ss");
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_TIMESTAMP,
        value: date
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE timestamp_type= ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTimestampTimeRecordWithTimeZoneParam(string url, string user, string password) returns @tainted record {}|error? {
    time:Time date = check time:parse("2017-02-03 11:53:00", "yyyy-MM-dd HH:mm:ss");
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_TIMESTAMP,
        value: date
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE timestamp_type= ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryDateTimeTimeRecordWithTimeZoneParam(string url, string user, string password) returns @tainted record {}|error? {
    time:Time date = check time:parse("2017-02-03 11:53:00", "yyyy-MM-dd HH:mm:ss");
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_TIMESTAMP,
        value: date
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE datetime_type= ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTimestampTimeRecordWithTimeZone2Param(string url, string user, string password) returns @tainted record {}|error? {
    time:Time date = check time:parse("2008-08-08 20:08:08+0800", "yyyy-MM-dd HH:mm:ssZ");
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_TIMESTAMP,
        value: date
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE timestamp_type2= ", ""],
        insertions: [typeVal]
    };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryArrayBasicParams(string url, string user, string password) returns @tainted record {}|error? {
    int[] dataint = [1, 2, 3];
    int[] datalong = [100000000, 200000000, 300000000];
    float[] datafloat = [245.23, 5559.49, 8796.123];
    float[] datadouble = [245.23, 5559.49, 8796.123];
    decimal[] datadecimal = [245, 5559, 8796];
    string[] datastring = ["Hello", "Ballerina"];
    boolean[] databoolean = [true, false, true];
    sql:TypedValue paraInt = {sqlType: sql:TYPE_ARRAY, value: dataint};
    sql:TypedValue paraLong = {sqlType: sql:TYPE_ARRAY, value: datalong};
    sql:TypedValue paraFloat = {sqlType: sql:TYPE_ARRAY, value: datafloat};
    sql:TypedValue paraDecimal = {sqlType: sql:TYPE_ARRAY, value: datadecimal};
    sql:TypedValue paraDouble = {sqlType: sql:TYPE_ARRAY, value: datadouble};
    sql:TypedValue paraString = {sqlType: sql:TYPE_ARRAY, value: datastring};
    sql:TypedValue paraBool = {sqlType: sql:TYPE_ARRAY, value: databoolean};

    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from ArrayTypes WHERE int_array = ", "AND long_array = ",  "AND float_array = ", "AND double_array = ", "AND decimal_array = " , "AND string_array = ", "AND boolean_array = ", ""],
        insertions: [paraInt, paraLong, paraFloat, paraDouble, paraDecimal, paraString, paraBool]
    };

    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryArrayBasicNullParams(string url, string user, string password) returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
            parts: ["SELECT * from ArrayTypes WHERE int_array is null AND long_array is null AND float_array is null AND double_array is null AND decimal_array is null AND string_array is null AND boolean_array is null"],
            insertions: []
     };
    return queryJdbcClient(url, user, password, sqlQuery);
}

function getByteColumnChannel() returns @untainted io:ReadableByteChannel|error {
    io:ReadableByteChannel byteChannel = check io:openReadableFile("./src/test/resources/files/byteValue.txt");
    return byteChannel;
}

function getBlobColumnChannel() returns @untainted io:ReadableByteChannel|error {
    io:ReadableByteChannel byteChannel = check io:openReadableFile("./src/test/resources/files/blobValue.txt");
    return byteChannel;
}

function getClobColumnChannel() returns @untainted io:ReadableCharacterChannel|error {
    io:ReadableByteChannel byteChannel = check io:openReadableFile("./src/test/resources/files/clobValue.txt");
    io:ReadableCharacterChannel sourceChannel = new (byteChannel, "UTF-8");
    return sourceChannel;
}

function queryJdbcClient(string url, string user, string password,@untainted string|sql:ParameterizedString sqlQuery)
returns @tainted record {}|error? {
    jdbc:Client dbClient = check new (url = url, user = user, password = password);
    stream<record{}, error> streamData = dbClient->query(sqlQuery);
    record {|record {} value;|}? data = check streamData.next();
    check streamData.close();
    record {}? value = data?.value;
    check dbClient.close();
    return value;
}

function writeToFile(byte[] data) returns @tainted error? {
    io:WritableByteChannel byteChannel = check io:openWritableFile("./src/test/resources/files/blobValue.txt");
    int leng = check byteChannel.write(data, 0);
    return check byteChannel.close();
}

