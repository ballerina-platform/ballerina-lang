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
import ballerina/mysql;

string host = "localhost";
string user = "test";
string password = "test123";
string database = "TEST_SQL_PARAMS_QUERY";
int port = 3305;

function querySingleIntParam() returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE row_id = ", ""],
        insertions: [1]
    };
    return queryMysqlClient(sqlQuery);
}

function queryDoubleIntParam() returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE row_id = ", " AND int_type = ", ""],
        insertions: [1, 1]
    };
    return queryMysqlClient(sqlQuery);
}

function queryIntAndLongParam() returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE row_id = ", " AND long_type = ", ""],
        insertions: [1, 9223372036854774807]
    };
    return queryMysqlClient(sqlQuery);
}

function queryStringParam() returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE string_type = ", ""],
        insertions: ["Hello"]
    };
    return queryMysqlClient(sqlQuery);
}

function queryIntAndStringParam() returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE string_type = ", "AND row_id = ", ""],
        insertions: ["Hello", 1]
    };
    return queryMysqlClient(sqlQuery);
}

function queryDoubleParam() returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE double_type = ", ""],
        insertions: [2139095039.0]
    };
    return queryMysqlClient(sqlQuery);
}

function queryFloatParam() returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE FORMAT(`float_type`,2)  = ", ""],
        insertions: [123.34]
    };
    return queryMysqlClient(sqlQuery);
}

function queryDoubleAndFloatParam() returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE FORMAT(`float_type`,2) = ", "and double_type = ", ""],
        insertions: [123.34, 2139095039.0]
    };
    return queryMysqlClient(sqlQuery);
}

function queryDecimalParam() returns @tainted record {}|error? {
    decimal decimalValue = 23.45;
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE decimal_type = ", ""],
        insertions: [decimalValue]
    };
    return queryMysqlClient(sqlQuery);
}

function queryDecimalAnFloatParam() returns @tainted record {}|error? {
    decimal decimalValue = 23.45;
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE decimal_type = ", "and double_type = ", ""],
        insertions: [decimalValue, 2139095039.0]
    };
    return queryMysqlClient(sqlQuery);
}

function queryByteArrayParam() returns @tainted record {}|error? {
    record {}|error? value = queryMysqlClient("Select * from ComplexTypes where row_id = 1");
    byte[] binaryData = <byte[]>getUntaintedData(value, "binary_type");

    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from ComplexTypes WHERE binary_type = ", ""],
        insertions: [binaryData]
    };
    return queryMysqlClient(sqlQuery);
}

function getUntaintedData(record {}|error? value, string fieldName) returns @untainted anydata {
    if (value is record {}) {
        return value[fieldName];
    }
    return {};
}

function queryTypeVarcharStringParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_VARCHAR,
        value: "Hello"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE string_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeCharStringParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_CHAR,
        value: "Hello"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE string_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeLongNVarcharStringParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_LONGNVARCHAR,
        value: "Hello"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE string_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeLongVarcharStringParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_LONGVARCHAR,
        value: "Hello"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE string_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeNCharStringParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_NCHAR,
        value: "Hello"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE string_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeNVarCharStringParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_NVARCHAR,
        value: "Hello"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE string_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeVarCharIntegerParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_VARCHAR,
        value: 1
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE string_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypBooleanBooleanParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_BOOLEAN,
        value: true
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE boolean_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypBooleanIntParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_BOOLEAN,
        value: 1
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE boolean_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypBitIntParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_BIT,
        value: 1
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE boolean_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypBitStringParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_BIT,
        value: "true"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE boolean_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypBitInvalidIntParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_BIT,
        value: 12
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE boolean_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypBitDoubleParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_BIT,
        value: 1.0
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE boolean_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeIntIntParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_INTEGER,
        value: 2147483647
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE int_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeTinyIntIntParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_TINYINT,
        value: 127
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE tinyint_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeSmallIntIntParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_SMALLINT,
        value: 32767
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE smallint_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeBigIntIntParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_BIGINT,
        value: 9223372036854774807
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE bigint_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeDoubleDoubleParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_DOUBLE,
        value: 1234.567
    };
    sql:TypedValue typeVal2 = {
        sqlType: sql:TYPE_DOUBLE,
        value: 1234.57
    };
    sql:ParameterizedString sqlQuery = {
            parts: ["SELECT * from NumericTypes WHERE float_type between ", " AND ", ""],
            insertions: [typeVal, typeVal2]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeDoubleIntParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_DOUBLE,
        value: 1234
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE float_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeDoubleDecimalParam() returns @tainted record {}|error? {
    decimal decimalVal = 1234.567;
    decimal decimalVal2 = 1234.57;
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_DOUBLE,
        value: decimalVal
    };
    sql:TypedValue typeVal2 = {
            sqlType: sql:TYPE_DOUBLE,
            value: decimalVal2
    };

    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE float_type between ", " AND ", ""],
        insertions: [typeVal, typeVal2]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeFloatDoubleParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal1 = {
        sqlType: sql:TYPE_DOUBLE,
        value: 1234.567
    };

    sql:TypedValue typeVal2 = {
            sqlType: sql:TYPE_DOUBLE,
            value: 1234.57
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE float_type between ", " AND ", ""],
        insertions: [typeVal1, typeVal2]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeRealDoubleParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_REAL,
        value: 1234.567
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE real_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeNumericDoubleParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_NUMERIC,
        value: 1234.567
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE numeric_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeNumericIntParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_NUMERIC,
        value: 1234
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE numeric_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeNumericDecimalParam() returns @tainted record {}|error? {
    decimal decimalVal = 1234.567;
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_NUMERIC,
        value: decimalVal
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE numeric_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeDecimalDoubleParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_DECIMAL,
        value: 1234.567
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE decimal_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeDecimalDecimalParam() returns @tainted record {}|error? {
    decimal decimalVal = 1234.567;
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_DECIMAL,
        value: decimalVal
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE decimal_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeBinaryByteParam() returns @tainted record {}|error? {
    record {}|error? value = queryMysqlClient("Select * from ComplexTypes where row_id = 1");
    byte[] binaryData = <byte[]>getUntaintedData(value, "binary_type");
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_BINARY,
        value: binaryData
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from ComplexTypes WHERE binary_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeBinaryReadableByteChannelParam() returns @tainted record {}|error? {
    io:ReadableByteChannel byteChannel = check getByteColumnChannel();
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_BINARY,
        value: byteChannel
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from ComplexTypes WHERE binary_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeVarBinaryReadableByteChannelParam() returns @tainted record {}|error? {
    io:ReadableByteChannel byteChannel = check getByteColumnChannel();
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_VARBINARY,
        value: byteChannel
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from ComplexTypes WHERE var_binary_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeLongVarBinaryReadableByteChannelParam() returns @tainted record {}|error? {
    io:ReadableByteChannel byteChannel = check getByteColumnChannel();
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_LONGVARBINARY,
        value: byteChannel
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from ComplexTypes WHERE var_binary_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeBlobByteParam() returns @tainted record {}|error? {
    record {}|error? value = queryMysqlClient("Select * from ComplexTypes where row_id = 1");
    byte[] binaryData = <byte[]>getUntaintedData(value, "blob_type");
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_BLOB,
        value: binaryData
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from ComplexTypes WHERE blob_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeBlobReadableByteChannelParam() returns @tainted record {}|error? {
    io:ReadableByteChannel byteChannel = check getBlobColumnChannel();
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_BLOB,
        value: byteChannel
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from ComplexTypes WHERE blob_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeClobStringParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_CLOB,
        value: "very long text"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from ComplexTypes WHERE clob_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeClobReadableCharChannelParam() returns @tainted record {}|error? {
    io:ReadableCharacterChannel clobChannel = check getClobColumnChannel();
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_CLOB,
        value: clobChannel
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from ComplexTypes WHERE clob_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTypeNClobReadableCharChannelParam() returns @tainted record {}|error? {
    io:ReadableCharacterChannel clobChannel = check getClobColumnChannel();
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_NCLOB,
        value: clobChannel
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from ComplexTypes WHERE clob_type = ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryDateStringParam() returns @tainted record {}|error? {
    //Setting this as var char since the test database seems not working with date type.
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_VARCHAR,
        value: "2017-02-03"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE date_type= ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryDateString2Param() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_VARCHAR,
        value: "2017-2-3"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE date_type= ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTimeStringParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_VARCHAR,
        value: "11:35:45"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE time_type= ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTimeStringInvalidParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_TIME,
        value: "11-35-45"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE time_type= ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTimestampStringParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_VARCHAR,
        value: "2017-02-03 11:53:00"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE timestamp_type= ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
}

function queryTimestampStringInvalidParam() returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
        sqlType: sql:TYPE_TIMESTAMP,
        value: "2017/02/03 11:53:00"
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DateTimeTypes WHERE timestamp_type= ", ""],
        insertions: [typeVal]
    };
    return queryMysqlClient(sqlQuery);
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

function queryMysqlClient(@untainted string|sql:ParameterizedString sqlQuery)
returns @tainted record {}|error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
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

