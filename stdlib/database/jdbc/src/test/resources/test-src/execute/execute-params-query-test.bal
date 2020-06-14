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
import ballerina/java.jdbc;
import ballerina/io;
import ballerina/sql;
import ballerina/time;

function insertIntoDataTable(string url, string user, string password) returns sql:ExecutionResult|sql:Error? {
    int rowId = 4;
    int intType = 1;
    int longType = 9223372036854774807;
    float floatType = 123.34;
    int doubleType = 2139095039;
    boolean boolType = true;
    string stringType = "Hello";
    decimal decimalType = 23.45;

    sql:ParameterizedQuery sqlQuery =
      `INSERT INTO DataTable (row_id, int_type, long_type, float_type, double_type, boolean_type, string_type, decimal_type)
        VALUES(${rowId}, ${intType}, ${longType}, ${floatType}, ${doubleType}, ${boolType}, ${stringType}, ${decimalType})`;
    return executeQueryJDBCClient(url, user, password, sqlQuery);
}

function insertIntoDataTable2(string url, string user, string password) returns sql:ExecutionResult|sql:Error? {
    int rowId = 5;
    sql:ParameterizedQuery sqlQuery = `INSERT INTO DataTable (row_id) VALUES(${rowId})`;
    return executeQueryJDBCClient(url, user, password, sqlQuery);
}

function insertIntoDataTable3(string url, string user, string password) returns sql:ExecutionResult|sql:Error? {
    int rowId = 6;
    int intType = 1;
    int longType = 9223372036854774807;
    float floatType = 123.34;
    int doubleType = 2139095039;
    boolean boolType = false;
    string stringType = "1";
    decimal decimalType = 23.45;

    sql:ParameterizedQuery sqlQuery =
      `INSERT INTO DataTable (row_id, int_type, long_type, float_type, double_type, boolean_type, string_type, decimal_type)
        VALUES(${rowId}, ${intType}, ${longType}, ${floatType}, ${doubleType}, ${boolType}, ${stringType}, ${decimalType})`;
    return executeQueryJDBCClient(url, user, password, sqlQuery);
}

function insertIntoDataTable4(string url, string user, string password) returns sql:ExecutionResult|sql:Error? {
    sql:IntegerValue rowId = new (7);
    sql:IntegerValue intType = new (2);
    sql:BigIntValue longType = new (9372036854774807);
    sql:FloatValue floatType = new (124.34);
    sql:DoubleValue doubleType = new (29095039);
    sql:BooleanValue boolType = new (false);
    sql:VarcharValue stringType = new ("stringvalue");
    decimal decimalVal = 25.45;
    sql:DecimalValue decimalType = new (decimalVal);

    sql:ParameterizedQuery sqlQuery =
      `INSERT INTO DataTable (row_id, int_type, long_type, float_type, double_type, boolean_type, string_type, decimal_type)
        VALUES(${rowId}, ${intType}, ${longType}, ${floatType}, ${doubleType}, ${boolType}, ${stringType}, ${decimalType})`;
    return executeQueryJDBCClient(url, user, password, sqlQuery);
}

function deleteDataTable1(string url, string user, string password) returns sql:ExecutionResult|sql:Error? {
    int rowId = 1;
    int intType = 1;
    int longType = 9223372036854774807;
    float floatType = 123.34;
    int doubleType = 2139095039;
    boolean boolType = true;
    string stringType = "Hello";
    decimal decimalType = 23.45;

    sql:ParameterizedQuery sqlQuery =
            `DELETE FROM DataTable where row_id=${rowId} AND int_type=${intType} AND long_type=${longType}
              AND float_type=${floatType} AND double_type=${doubleType} AND boolean_type=${boolType}
              AND string_type=${stringType} AND decimal_type=${decimalType}`;
    return executeQueryJDBCClient(url, user, password, sqlQuery);
}

function deleteDataTable2(string url, string user, string password) returns sql:ExecutionResult|sql:Error? {
    int rowId = 2;
    sql:ParameterizedQuery sqlQuery = `DELETE FROM DataTable where row_id = ${rowId}`;
    return executeQueryJDBCClient(url, user, password, sqlQuery);
}

function deleteDataTable3(string url, string user, string password) returns sql:ExecutionResult|sql:Error? {
    sql:IntegerValue rowId = new (3);
    sql:IntegerValue intType = new (1);
    sql:BigIntValue longType = new (9372036854774807);
    sql:FloatValue floatType = new (124.34);
    sql:DoubleValue doubleType = new (29095039);
    sql:BooleanValue boolType = new (false);
    sql:VarcharValue stringType = new ("1");
    decimal decimalVal = 25.45;
    sql:DecimalValue decimalType = new (decimalVal);

    sql:ParameterizedQuery sqlQuery =
            `DELETE FROM DataTable where row_id=${rowId} AND int_type=${intType} AND long_type=${longType}
              AND double_type=${doubleType} AND boolean_type=${boolType}
              AND string_type=${stringType} AND decimal_type=${decimalType}`;
    return executeQueryJDBCClient(url, user, password, sqlQuery);
}

function insertIntoComplexTable(string url, string user, string password) returns sql:ExecutionResult|sql:Error? {
    record {}|error? value = queryMockClient(url, user, password, "Select * from ComplexTypes where row_id = 1");
    byte[] binaryData = <byte[]>getUntaintedData(value, "BLOB_TYPE");
    int rowId = 5;
    string stringType = "very long text";
    sql:ParameterizedQuery sqlQuery =
        `INSERT INTO ComplexTypes (row_id, blob_type, clob_type, binary_type, var_binary_type) VALUES (
        ${rowId}, ${binaryData}, CONVERT(${stringType}, CLOB), ${binaryData}, ${binaryData})`;
    return executeQueryJDBCClient(url, user, password, sqlQuery);
}

function insertIntoComplexTable2(string url, string user, string password) returns sql:ExecutionResult|error? {
    io:ReadableByteChannel blobChannel = check getBlobColumnChannel();
    io:ReadableCharacterChannel clobChannel = check getClobColumnChannel();
    io:ReadableByteChannel byteChannel = check getByteColumnChannel();

    sql:BlobValue blobType = new (blobChannel);
    sql:ClobValue clobType = new (clobChannel);
    sql:BlobValue binaryType = new (byteChannel);
    int rowId = 6;

    sql:ParameterizedQuery sqlQuery =
        `INSERT INTO ComplexTypes (row_id, blob_type, clob_type, binary_type, var_binary_type) VALUES (
        ${rowId}, ${blobType}, CONVERT(${clobType}, CLOB), ${binaryType}, ${binaryType})`;
    return executeQueryJDBCClient(url, user, password, sqlQuery);
}

function insertIntoComplexTable3(string url, string user, string password) returns sql:ExecutionResult|sql:Error|error? {
    int rowId = 7;
    var nilType = ();
    sql:ParameterizedQuery sqlQuery =
            `INSERT INTO ComplexTypes (row_id, blob_type, clob_type, binary_type, var_binary_type) VALUES (
            ${rowId}, ${nilType}, CONVERT(${nilType}, CLOB), ${nilType}, ${nilType})`;
    return executeQueryJDBCClient(url, user, password, sqlQuery);
}

function deleteComplexTable(string url, string user, string password) returns sql:ExecutionResult|sql:Error? {
    record {}|error? value = queryMockClient(url, user, password, "Select * from ComplexTypes where row_id = 1");
    byte[] binaryData = <byte[]>getUntaintedData(value, "BLOB_TYPE");

    int rowId = 2;
    sql:ParameterizedQuery sqlQuery =
            `DELETE FROM ComplexTypes where row_id = ${rowId} AND blob_type= ${binaryData}`;
    return executeQueryJDBCClient(url, user, password, sqlQuery);
}

function deleteComplexTable2(string url, string user, string password) returns sql:ExecutionResult|sql:Error|error? {
    sql:BlobValue blobType = new ();
    sql:ClobValue clobType = new ();
    sql:BinaryValue binaryType = new ();
    sql:VarBinaryValue varBinaryType = new ();

    int rowId = 4;
    sql:ParameterizedQuery sqlQuery =
            `DELETE FROM ComplexTypes where row_id = ${rowId} AND blob_type= ${blobType} AND clob_type=${clobType}`;
    return executeQueryJDBCClient(url, user, password, sqlQuery);
}

function insertIntoNumericTable(string url, string user, string password) returns sql:ExecutionResult|sql:Error? {
    sql:BitValue bitType = new (1);
    int rowId = 3;
    int intType = 2147483647;
    int bigIntType = 9223372036854774807;
    int smallIntType = 32767;
    int tinyIntType = 127;
    decimal decimalType = 1234.567;

    sql:ParameterizedQuery sqlQuery =
        `INSERT INTO NumericTypes (id, int_type, bigint_type, smallint_type, tinyint_type, bit_type, decimal_type,
        numeric_type, float_type, real_type) VALUES(${rowId},${intType},${bigIntType},${smallIntType},${tinyIntType},
        ${bitType},${decimalType},${decimalType},${decimalType},${decimalType})`;
    return executeQueryJDBCClient(url, user, password, sqlQuery);
}

function insertIntoNumericTable2(string url, string user, string password) returns sql:ExecutionResult|sql:Error? {
    int rowId = 4;
    var nilType = ();
    sql:ParameterizedQuery sqlQuery =
            `INSERT INTO NumericTypes (id, int_type, bigint_type, smallint_type, tinyint_type, bit_type, decimal_type,
            numeric_type, float_type, real_type) VALUES(${rowId},${nilType},${nilType},${nilType},${nilType},
            ${nilType},${nilType},${nilType},${nilType},${nilType})`;
    return executeQueryJDBCClient(url, user, password, sqlQuery);
}

function insertIntoNumericTable3(string url, string user, string password) returns sql:ExecutionResult|sql:Error? {
    sql:IntegerValue id = new (5);
    sql:IntegerValue intType = new (2147483647);
    sql:BigIntValue bigIntType = new (9223372036854774807);
    sql:SmallIntValue smallIntType = new (32767);
    sql:SmallIntValue tinyIntType = new (127);
    sql:BitValue bitType = new (1);
    decimal decimalVal = 1234.567;
    sql:DecimalValue decimalType = new (decimalVal);
    sql:NumericValue numbericType = new (1234.567);
    sql:FloatValue floatType = new (1234.567);
    sql:RealValue realType = new (1234.567);

    sql:ParameterizedQuery sqlQuery =
        `INSERT INTO NumericTypes (id, int_type, bigint_type, smallint_type, tinyint_type, bit_type, decimal_type,
        numeric_type, float_type, real_type) VALUES(${id},${intType},${bigIntType},${smallIntType},${tinyIntType},
        ${bitType},${decimalType},${numbericType},${floatType},${realType})`;
    return executeQueryJDBCClient(url, user, password, sqlQuery);
}

function insertIntoDateTimeTable(string url, string user, string password) returns sql:ExecutionResult|sql:Error? {
    int rowId = 2;
    string dateType = "2017-02-03";
    string timeType = "11:35:45";
    string dateTimeType = "2017-02-03 11:53:00";
    string timeStampType = "2017-02-03 11:53:00";
    string timeType2 = "20:08:08-8:00";
    string timeStampType2 = "2008-08-08 20:08:08+8:00";

    sql:ParameterizedQuery sqlQuery =
        `INSERT INTO DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type, time_type2, timestamp_type2)
        VALUES(${rowId}, ${dateType}, ${timeType}, ${dateTimeType}, ${timeStampType}, ${timeType2}, ${timeStampType2})`;
    return executeQueryJDBCClient(url, user, password, sqlQuery);
}

function insertIntoDateTimeTable2(string url, string user, string password) returns sql:ExecutionResult|error? {
    sql:DateValue dateVal = new ("2017-02-03");
    sql:TimeValue timeVal = new ("11:35:45");
    sql:DateTimeValue dateTimeVal =  new ("2017-02-03 11:53:00");
    sql:TimestampValue timestampVal = new ("2017-02-03 11:53:00");
    sql:TimeValue time2Val = new (check time:parse("20:08:08-0800", "HH:mm:ssZ"));
    sql:TimestampValue timestamp2Val = new (check time:parse("2008-08-08 20:08:08+0800", "yyyy-MM-dd HH:mm:ssZ"));
    int rowId = 3;

    sql:ParameterizedQuery sqlQuery =
            `INSERT INTO DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type, time_type2, timestamp_type2)
            VALUES(${rowId}, ${dateVal}, ${timeVal}, ${dateTimeVal}, ${timestampVal}, ${time2Val}, ${timestamp2Val})`;
    return executeQueryJDBCClient(url, user, password, sqlQuery);
}

function insertIntoDateTimeTable3(string url, string user, string password) returns sql:ExecutionResult|error? {
    sql:DateValue dateVal = new ();
    sql:TimeValue timeVal = new ();
    sql:DateTimeValue dateTimeVal =  new ();
    sql:TimestampValue timestampVal = new ();
    sql:TimeValue time2Val = new ();
    sql:TimestampValue timestamp2Val = new ();
    int rowId = 4;

    sql:ParameterizedQuery sqlQuery =
                `INSERT INTO DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type, time_type2, timestamp_type2)
                VALUES(${rowId}, ${dateVal}, ${timeVal}, ${dateTimeVal}, ${timestampVal}, ${time2Val}, ${timestamp2Val})`;
    return executeQueryJDBCClient(url, user, password, sqlQuery);
}

function insertIntoDateTimeTable4(string url, string user, string password) returns sql:ExecutionResult|error? {
    int rowId = 5;
    var nilType = ();

    sql:ParameterizedQuery sqlQuery =
            `INSERT INTO DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type, time_type2, timestamp_type2)
            VALUES(${rowId}, ${nilType}, ${nilType}, ${nilType}, ${nilType}, ${nilType}, ${nilType})`;
    return executeQueryJDBCClient(url, user, password, sqlQuery);
}

function insertIntoArrayTable(string url, string user, string password) returns sql:ExecutionResult|error? {
    int[] dataint = [1, 2, 3];
    int[] datalong = [100000000, 200000000, 300000000];
    float[] datafloat = [245.23, 5559.49, 8796.123];
    float[] datadouble = [245.23, 5559.49, 8796.123];
    decimal[] datadecimal = [245, 5559, 8796];
    string[] datastring = ["Hello", "Ballerina"];
    boolean[] databoolean = [true, false, true];

    record {}|error? value = queryMockClient(url, user, password, "Select * from ComplexTypes where row_id = 1");
    byte[][] dataBlob = [<byte[]>getUntaintedData(value, "BLOB_TYPE")];

    sql:ArrayValue paraInt = new (dataint);
    sql:ArrayValue paraLong = new (datalong);
    sql:ArrayValue paraFloat = new (datafloat);
    sql:ArrayValue paraDecimal = new (datadecimal);
    sql:ArrayValue paraDouble = new (datadouble);
    sql:ArrayValue paraString = new (datastring);
    sql:ArrayValue paraBool = new (databoolean);
    sql:ArrayValue paraBlob = new (dataBlob);
    int rowId = 5;

    sql:ParameterizedQuery sqlQuery =
        `INSERT INTO ArrayTypes (row_id, int_array, long_array, float_array, double_array, decimal_array, boolean_array,
         string_array, blob_array) VALUES(${rowId}, ${paraInt}, ${paraLong}, ${paraFloat}, ${paraDouble}, ${paraDecimal},
         ${paraBool}, ${paraString}, ${paraBlob})`;
    return executeQueryJDBCClient(url, user, password, sqlQuery);
}

function insertIntoArrayTable2(string url, string user, string password) returns sql:ExecutionResult|error? {
    sql:ArrayValue paraInt = new ();
    sql:ArrayValue paraLong = new ();
    sql:ArrayValue paraFloat = new ();
    sql:ArrayValue paraDecimal = new ();
    sql:ArrayValue paraDouble = new ();
    sql:ArrayValue paraString = new ();
    sql:ArrayValue paraBool = new ();
    sql:ArrayValue paraBlob = new ();
    int rowId = 6;

    sql:ParameterizedQuery sqlQuery =
        `INSERT INTO ArrayTypes (row_id, int_array, long_array, float_array, double_array, decimal_array, boolean_array,
         string_array, blob_array) VALUES(${rowId}, ${paraInt}, ${paraLong}, ${paraFloat}, ${paraDouble}, ${paraDecimal},
         ${paraBool}, ${paraString}, ${paraBlob})`;
    return executeQueryJDBCClient(url, user, password, sqlQuery);
}

function executeQueryJDBCClient(string jdbcURL, string user, string password, sql:ParameterizedQuery sqlQuery)
returns sql:ExecutionResult|sql:Error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    sql:ExecutionResult? result = check dbClient->execute(sqlQuery);
    check dbClient.close();
    return result;
}

function getUntaintedData(record {}|error? value, string fieldName) returns @untainted anydata {
    if (value is record {}) {
        return value[fieldName];
    }
    return {};
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

function queryMockClient(string url, string user, string password,@untainted string|sql:ParameterizedQuery sqlQuery)
returns @tainted record {}|error? {
    jdbc:Client dbClient = check new (url = url, user = user, password = password);
    stream<record{}, error> streamData = dbClient->query(sqlQuery);
    record {|record {} value;|}? data = check streamData.next();
    check streamData.close();
    record {}? value = data?.value;
    check dbClient.close();
    return value;
}
