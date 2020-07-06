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
import ballerina/java.jdbc;
import ballerina/sql;
import ballerina/time;

function querySingleIntParam(string url, string user, string password) returns @tainted record {}|error? {
    int rowId = 1;
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE row_id = ${rowId}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryDoubleIntParam(string url, string user, string password) returns @tainted record {}|error? {
    int rowId = 1;
    int intType = 1;
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE row_id = ${rowId} AND int_type =  ${intType}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryIntAndLongParam(string url, string user, string password) returns @tainted record {}|error? {
    int rowId = 1;
    int longType = 9223372036854774807;
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE row_id = ${rowId} AND long_type = ${longType}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryStringParam(string url, string user, string password) returns @tainted record {}|error? {
    string stringType = "Hello";
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE string_type = ${stringType}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryIntAndStringParam(string url, string user, string password) returns @tainted record {}|error? {
    string stringType = "Hello";
    int rowId =1;
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE string_type = ${stringType} AND row_id = ${rowId}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryDoubleParam(string url, string user, string password) returns @tainted record {}|error? {
    float doubleType = 2139095039.0;
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE double_type = ${doubleType}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryFloatParam(string url, string user, string password) returns @tainted record {}|error? {
    float floatType = 123.34;
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE float_type = ${floatType}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryDoubleAndFloatParam(string url, string user, string password) returns @tainted record {}|error? {
    float floatType = 123.34;
    float doubleType = 2139095039.0;
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE float_type = ${floatType}
                                                                    and double_type = ${doubleType}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryDecimalParam(string url, string user, string password) returns @tainted record {}|error? {
    decimal decimalValue = 23.45;
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE decimal_type = ${decimalValue}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryDecimalAnFloatParam(string url, string user, string password) returns @tainted record {}|error? {
    decimal decimalValue = 23.45;
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE decimal_type = ${decimalValue}
                                                                    and double_type = 2139095039.0`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryByteArrayParam(string url, string user, string password) returns @tainted record {}|error? {
    record {}|error? value = queryJdbcClient(url, user, password, "Select * from ComplexTypes where row_id = 1");
    byte[] binaryData = <byte[]>getUntaintedData(value, "BINARY_TYPE");
    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE binary_type = ${binaryData}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function getUntaintedData(record {}|error? value, string fieldName) returns @untainted anydata {
    if (value is record {}) {
        return value[fieldName];
    }
    return {};
}

function queryTypeVarcharStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:VarcharValue typeVal = new ("Hello");
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE string_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeCharStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:CharValue typeVal = new ("Hello");
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE string_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeNCharStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:NCharValue typeVal = new ("Hello");
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE string_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeNVarCharStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:NVarcharValue typeVal = new ("Hello");
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE string_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeVarCharIntegerParam(string url, string user, string password) returns @tainted record {}|error? {
    int intVal = 1;
    sql:NCharValue typeVal = new (intVal.toString());
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE string_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypBooleanBooleanParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:BooleanValue typeVal = new (true);
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE boolean_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypBitIntParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:BitValue typeVal = new (1);
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE boolean_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypBitStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:BitValue typeVal = new (true);
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE boolean_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypBitInvalidIntParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:BitValue typeVal = new (12);
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE boolean_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeIntIntParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:IntegerValue typeVal = new (2147483647);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE int_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeTinyIntIntParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:SmallIntValue typeVal = new (127);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE tinyint_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeSmallIntIntParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:SmallIntValue typeVal = new (32767);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE smallint_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeBigIntIntParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:BigIntValue typeVal = new (9223372036854774807);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE bigint_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeDoubleDoubleParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:DoubleValue typeVal = new (1234.567);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE float_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeDoubleIntParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:DoubleValue typeVal = new (1234);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE float_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeDoubleDecimalParam(string url, string user, string password) returns @tainted record {}|error? {
    decimal decimalVal = 1234.567;
    sql:DoubleValue typeVal = new (decimalVal);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE float_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeFloatDoubleParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:DoubleValue typeVal = new (1234.567);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE float_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeRealDoubleParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:RealValue typeVal = new (1234.567);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE real_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeNumericDoubleParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:NumericValue typeVal = new (1234.567);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE numeric_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeNumericIntParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:NumericValue typeVal = new (1234);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE numeric_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeNumericDecimalParam(string url, string user, string password) returns @tainted record {}|error? {
    decimal decimalVal = 1234.567;
    sql:NumericValue typeVal = new (decimalVal);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE numeric_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeDecimalDoubleParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:DecimalValue typeVal = new (1234.567);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE decimal_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeDecimalDecimalParam(string url, string user, string password) returns @tainted record {}|error? {
    decimal decimalVal = 1234.567;
    sql:DecimalValue typeVal = new (decimalVal);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE decimal_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeBinaryByteParam(string url, string user, string password) returns @tainted record {}|error? {
    record {}|error? value = queryJdbcClient(url, user, password, "Select * from ComplexTypes where row_id = 1");
    byte[] binaryData = <byte[]>getUntaintedData(value, "BINARY_TYPE");
    sql:BinaryValue typeVal = new (binaryData);
    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE binary_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeBinaryReadableByteChannelParam(string url, string user, string password)
returns @tainted record {}|error? {
    io:ReadableByteChannel byteChannel = check getByteColumnChannel();
    sql:BinaryValue typeVal = new (byteChannel);
    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE binary_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeVarBinaryReadableByteChannelParam(string url, string user, string password)
returns @tainted record {}|error? {
    io:ReadableByteChannel byteChannel = check getByteColumnChannel();
    sql:VarBinaryValue typeVal = new (byteChannel);
    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE var_binary_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeBlobByteParam(string url, string user, string password) returns @tainted record {}|error? {
    record {}|error? value = queryJdbcClient(url, user, password, "Select * from ComplexTypes where row_id = 1");
    byte[] binaryData = <byte[]>getUntaintedData(value, "BLOB_TYPE");
    sql:BinaryValue typeVal = new (binaryData);
    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE blob_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeBlobReadableByteChannelParam(string url, string user, string password)
returns @tainted record {}|error? {
    io:ReadableByteChannel byteChannel = check getBlobColumnChannel();
    sql:BlobValue typeVal = new (byteChannel);
    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE blob_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeClobStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:ClobValue typeVal = new ("very long text");
    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE clob_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeClobReadableCharChannelParam(string url, string user, string password)
returns @tainted record {}|error? {
    io:ReadableCharacterChannel clobChannel = check getClobColumnChannel();
    sql:ClobValue typeVal = new (clobChannel);
    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE clob_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTypeNClobReadableCharChannelParam(string url, string user, string password)
returns @tainted record {}|error? {
    io:ReadableCharacterChannel clobChannel = check getClobColumnChannel();
    sql:NClobValue typeVal = new (clobChannel);
    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE clob_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryDateStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:DateValue typeVal = new ("2017-02-03");
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE date_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryDateString2Param(string url, string user, string password) returns @tainted record {}|error? {
    sql:DateValue typeVal = new ("2017-2-3");
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE date_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryDateStringInvalidParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:DateValue typeVal = new ("2017/2/3");
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE date_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryDateLongParam(string url, string user, string password) returns @tainted record {}|error? {
    time:Time date = check time:parse("2017-02-03", "yyyy-MM-dd");
    sql:DateValue typeVal = new (date.time);
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE date_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryDateTimeRecordParam(string url, string user, string password) returns @tainted record {}|error? {
    time:Time date = check time:parse("2017-02-03", "yyyy-MM-dd");
    sql:DateValue typeVal = new (date);
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE date_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryDateTimeRecordWithTimeZoneParam(string url, string user, string password)
returns @tainted record {}|error? {
    time:Time date = check time:parse("2017-02-03T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    sql:DateValue typeVal = new (date);
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE date_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTimeStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TimeValue typeVal = new ("11:35:45");
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE time_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTimeStringInvalidParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TimeValue typeVal = new ("11-35-45");
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE time_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTimeLongParam(string url, string user, string password) returns @tainted record {}|error? {
    time:Time date = check time:parse("11:35:45", "HH:mm:ss");
    sql:TimeValue typeVal = new (date.time);
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE time_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTimeTimeRecordParam(string url, string user, string password) returns @tainted record {}|error? {
    time:Time date = check time:parse("11:35:45", "HH:mm:ss");
    sql:TimeValue typeVal = new (date);
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE time_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTimeTimeRecordWithTimeZoneParam(string url, string user, string password)
returns @tainted record {}|error? {
    time:Time date = check time:parse("2017-02-03T11:35:45", "yyyy-MM-dd'T'HH:mm:ss");
    sql:TimeValue typeVal = new (date);
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE time_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTimestampStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TimestampValue typeVal = new ("2017-02-03 11:53:00");
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE timestamp_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTimestampStringInvalidParam(string url, string user, string password)
returns @tainted record {}|error? {
    sql:TimestampValue typeVal = new ("2017/02/03 11:53:00");
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE timestamp_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTimestampLongParam(string url, string user, string password) returns @tainted record {}|error? {
    time:Time date = check time:parse("2017-02-03 11:53:00", "yyyy-MM-dd HH:mm:ss");
    sql:TimestampValue typeVal = new (date.time);
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE timestamp_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTimestampTimeRecordParam(string url, string user, string password) returns @tainted record {}|error? {
    time:Time date = check time:parse("2017-02-03 11:53:00", "yyyy-MM-dd HH:mm:ss");
    sql:TimestampValue typeVal = new (date);
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE timestamp_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTimestampTimeRecordWithTimeZoneParam(string url, string user, string password)
returns @tainted record {}|error? {
    time:Time date = check time:parse("2017-02-03 11:53:00", "yyyy-MM-dd HH:mm:ss");
    sql:TimestampValue typeVal = new (date);
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE timestamp_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryDateTimeTimeRecordWithTimeZoneParam(string url, string user, string password)
returns @tainted record {}|error? {
    time:Time date = check time:parse("2017-02-03 11:53:00", "yyyy-MM-dd HH:mm:ss");
    sql:TimestampValue typeVal = new (date);
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE datetime_type = ${typeVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryTimestampTimeRecordWithTimeZone2Param(string url, string user, string password)
returns @tainted record {}|error? {
    time:Time date = check time:parse("2008-08-08 20:08:08+0800", "yyyy-MM-dd HH:mm:ssZ");
    sql:TimestampValue typeVal = new (date);
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE timestamp_type2 = ${typeVal}`;
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
    sql:ArrayValue paraInt = new (dataint);
    sql:ArrayValue paraLong = new (datalong);
    sql:ArrayValue paraFloat = new (datafloat);
    sql:ArrayValue paraDecimal = new (datadecimal);
    sql:ArrayValue paraDouble = new (datadouble);
    sql:ArrayValue paraString = new (datastring);
    sql:ArrayValue paraBool = new (databoolean);

    sql:ParameterizedQuery sqlQuery =
    `SELECT * from ArrayTypes WHERE int_array = ${paraInt}
                                AND long_array = ${paraLong}
                                AND float_array = ${paraFloat}
                                AND double_array = ${paraDouble}
                                AND decimal_array = ${paraDecimal}
                                AND string_array = ${paraString}
                                AND boolean_array = ${paraBool}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryArrayBasicNullParams(string url, string user, string password) returns @tainted record {}|error? {
    sql:ParameterizedQuery sqlQuery =
        `SELECT * from ArrayTypes WHERE int_array is null AND long_array is null AND float_array
         is null AND double_array is null AND decimal_array is null AND string_array is null
         AND boolean_array is null`;

    return queryJdbcClient(url, user, password, sqlQuery);
}

type UUIDResult record {|
    int id;
    string data;
|};

function queryUUIDParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:ParameterizedQuery sqlQuery = `SELECT * from UUIDTable WHERE id = 1`;
    record {}? result = check queryJdbcClient(url, user, password, sqlQuery, resultType = UUIDResult);
    if (result is record {}) {
        UUIDResult uuid = <@untainted> <UUIDResult> result;
        sql:ParameterizedQuery sqlQuery2 = `SELECT * from UUIDTable WHERE data = ${uuid.data}`;
        return queryJdbcClient(url, user, password, sqlQuery2, resultType = UUIDResult);
    } else {
        return ();
    }
}

function queryEnumStringParam(string url, string user, string password) returns @tainted record {}|error? {
    string enumVal = "doctor";
    sql:ParameterizedQuery sqlQuery = `SELECT * from ENUMTable where enum_type= ${enumVal}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

type EnumResult record {|
    int id;
    string enum_type;
|};

function queryEnumStringParam2(string url, string user, string password) returns @tainted record {}|error? {
    string enumVal = "doctor";
    sql:ParameterizedQuery sqlQuery = `SELECT * from ENUMTable where enum_type= ${enumVal}`;
    return queryJdbcClient(url, user, password, sqlQuery, resultType = EnumResult);
}

function queryGeoParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:ParameterizedQuery sqlQuery = `SELECT * from GEOTable`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryGeoParam2(string url, string user, string password) returns @tainted record {}|error? {
    string geoParam = "POINT (7 52)";
    sql:ParameterizedQuery sqlQuery = `SELECT * from GEOTable where geom = ${geoParam}`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

function queryJsonParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:ParameterizedQuery sqlQuery = `SELECT * from JsonTable`;
    return queryJdbcClient(url, user, password, sqlQuery);
}

type JsonResult record {|
    int id;
    json json_type;
|};

function queryJsonParam2(string url, string user, string password) returns @tainted record {}|error? {
    sql:ParameterizedQuery sqlQuery = `SELECT * from JsonTable`;
    return queryJdbcClient(url, user, password, sqlQuery, resultType = JsonResult);
}

function queryJsonParam3(string url, string user, string password) returns @tainted record {}|error? {
    json jsonType = {"id": 100, "name": "Joe", "groups": [2, 5]};
    int id = 100;
    string name = "Joe";
    string arrayVal = "[2, 5]";
    sql:ParameterizedQuery sqlQuery =
            `SELECT * from JsonTable where json_type=JSON_OBJECT('id': ${id}, 'name':${name}, 'groups': ${arrayVal}FORMAT JSON)`;
    return queryJdbcClient(url, user, password, sqlQuery, resultType = JsonResult);
}

function queryIntervalParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:ParameterizedQuery sqlQuery = `SELECT * from IntervalTable`;
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

function queryJdbcClient(string url, string user, string password,@untainted string|sql:ParameterizedQuery sqlQuery,
 typedesc<record {}>? resultType = ())
returns @tainted record {}|error? {
    jdbc:Client dbClient = check new (url = url, user = user, password = password);
    stream<record {}, error> streamData = dbClient->query(sqlQuery, resultType);
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

