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
import ballerina/mysql;
import ballerina/sql;

string host = "localhost";
string user = "test";
string password = "test123";
string database = "TEST_SQL_PARAMS_QUERY";
int port = 3305;

function querySingleIntParam() returns @tainted record {}|error? {
    int rowId = 1;
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE row_id = ${rowId}`;
    return queryMysqlClient(sqlQuery);
}

function queryDoubleIntParam() returns @tainted record {}|error? {
    int rowId = 1;
    int intType = 1;
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE row_id = ${rowId} AND int_type =  ${intType}`;
    return queryMysqlClient(sqlQuery);
}

function queryIntAndLongParam() returns @tainted record {}|error? {
    int rowId = 1;
    int longType = 9223372036854774807;
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE row_id = ${rowId} AND long_type = ${longType}`;
    return queryMysqlClient(sqlQuery);
}

function queryStringParam() returns @tainted record {}|error? {
    string stringType = "Hello";
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE string_type = ${stringType}`;
    return queryMysqlClient(sqlQuery);
}

function queryIntAndStringParam() returns @tainted record {}|error? {
    string stringType = "Hello";
    int rowId =1;
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE string_type = ${stringType} AND row_id = ${rowId}`;
    return queryMysqlClient(sqlQuery);
}

function queryDoubleParam() returns @tainted record {}|error? {
    float doubleType = 2139095039.0;
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE double_type = ${doubleType}`;
    return queryMysqlClient(sqlQuery);
}

function queryFloatParam() returns @tainted record {}|error? {
    float floatType = 123.34;
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE FORMAT(float_type,2) = ${floatType}`;
    return queryMysqlClient(sqlQuery);
}

function queryDoubleAndFloatParam() returns @tainted record {}|error? {
    float floatType = 123.34;
    float doubleType = 2139095039.0;
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE FORMAT(float_type,2) = ${floatType}
                                                                    and double_type = ${doubleType}`;
    return queryMysqlClient(sqlQuery);
}

function queryDecimalParam() returns @tainted record {}|error? {
    decimal decimalValue = 23.45;
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE decimal_type = ${decimalValue}`;
    return queryMysqlClient(sqlQuery);
}

function queryDecimalAnFloatParam() returns @tainted record {}|error? {
    decimal decimalValue = 23.45;
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE decimal_type = ${decimalValue}
                                                                    and double_type = 2139095039.0`;
    return queryMysqlClient(sqlQuery);
}

function queryByteArrayParam() returns @tainted record {}|error? {
    record {}|error? value = queryMysqlClient("Select * from ComplexTypes where row_id = 1");
    byte[] binaryData = <byte[]>getUntaintedData(value, "binary_type");

    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE binary_type = ${binaryData}`;
    return queryMysqlClient(sqlQuery);
}

function getUntaintedData(record {}|error? value, string fieldName) returns @untainted anydata {
    if (value is record {}) {
        return value[fieldName];
    }
    return {};
}

function queryTypeVarcharStringParam() returns @tainted record {}|error? {
    sql:VarcharValue typeVal = new ("Hello");
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE string_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeCharStringParam() returns @tainted record {}|error? {
    sql:CharValue typeVal = new ("Hello");
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE string_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeNCharStringParam() returns @tainted record {}|error? {
    sql:NCharValue typeVal = new ("Hello");
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE string_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeNVarCharStringParam() returns @tainted record {}|error? {
    sql:NVarcharValue typeVal = new ("Hello");
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE string_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeVarCharIntegerParam() returns @tainted record {}|error? {
    int intVal = 1;
    sql:VarcharValue typeVal = new (intVal.toString());
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE string_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypBooleanBooleanParam() returns @tainted record {}|error? {
    sql:BooleanValue typeVal = new (true);
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE boolean_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypBitIntParam() returns @tainted record {}|error? {
    sql:BitValue typeVal = new (1);
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE boolean_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypBitStringParam() returns @tainted record {}|error? {
    sql:BitValue typeVal = new (true);
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE boolean_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypBitInvalidIntParam() returns @tainted record {}|error? {
    sql:BitValue typeVal = new (12);
    sql:ParameterizedQuery sqlQuery = `SELECT * from DataTable WHERE boolean_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeIntIntParam() returns @tainted record {}|error? {
    sql:IntegerValue typeVal = new (2147483647);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE int_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeTinyIntIntParam() returns @tainted record {}|error? {
    sql:SmallIntValue typeVal = new (127);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE tinyint_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeSmallIntIntParam() returns @tainted record {}|error? {
    sql:SmallIntValue typeVal = new (32767);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE smallint_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeMediumIntIntParam() returns @tainted record {}|error? {
    sql:IntegerValue typeVal = new (8388607);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE mediumint_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeBigIntIntParam() returns @tainted record {}|error? {
    sql:BigIntValue typeVal = new (9223372036854774807);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE bigint_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeDoubleDoubleParam() returns @tainted record {}|error? {
    sql:DoubleValue typeVal = new (1234.567);
    sql:DoubleValue typeVal2 = new (1234.57);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE float_type between ${typeVal} AND ${typeVal2}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeDoubleIntParam() returns @tainted record {}|error? {
    sql:DoubleValue typeVal = new (1234);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE float_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeDoubleDecimalParam() returns @tainted record {}|error? {
    decimal decimalVal = 1234.567;
    decimal decimalVal2 = 1234.57;
    sql:DoubleValue typeVal = new (decimalVal);
    sql:DoubleValue typeVal2 = new (decimalVal2);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE float_type between ${typeVal} AND ${typeVal2}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeFloatDoubleParam() returns @tainted record {}|error? {
    sql:DoubleValue typeVal1 = new (1234.567);
    sql:DoubleValue typeVal2 = new (1234.57);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE float_type between ${typeVal1} AND ${typeVal2}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeRealDoubleParam() returns @tainted record {}|error? {
    sql:RealValue typeVal = new (1234.567);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE real_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeNumericDoubleParam() returns @tainted record {}|error? {
    sql:NumericValue typeVal = new (1234.567);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE numeric_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeNumericIntParam() returns @tainted record {}|error? {
    sql:NumericValue typeVal = new (1234);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE numeric_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeNumericDecimalParam() returns @tainted record {}|error? {
    decimal decimalVal = 1234.567;
    sql:NumericValue typeVal = new (decimalVal);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE numeric_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeDecimalDoubleParam() returns @tainted record {}|error? {
    sql:DecimalValue typeVal = new (1234.567);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE decimal_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeDecimalDecimalParam() returns @tainted record {}|error? {
    decimal decimalVal = 1234.567;
    sql:DecimalValue typeVal = new (decimalVal);
    sql:ParameterizedQuery sqlQuery = `SELECT * from NumericTypes WHERE decimal_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeBinaryByteParam() returns @tainted record {}|error? {
    record {}|error? value = queryMysqlClient("Select * from ComplexTypes where row_id = 1");
    byte[] binaryData = <byte[]>getUntaintedData(value, "binary_type");
    sql:BinaryValue typeVal = new (binaryData);
    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE binary_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeBinaryReadableByteChannelParam()
returns @tainted record {}|error? {
    io:ReadableByteChannel byteChannel = check getByteColumnChannel();
    sql:BinaryValue typeVal = new (byteChannel);
    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE binary_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeVarBinaryReadableByteChannelParam()
returns @tainted record {}|error? {
    io:ReadableByteChannel byteChannel = check getByteColumnChannel();
    sql:VarBinaryValue typeVal = new (byteChannel);
    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE var_binary_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeTinyBlobByteParam() returns @tainted record {}|error? {
    record {}|error? value = queryMysqlClient("Select * from ComplexTypes where row_id = 1");
    byte[] binaryData = <byte[]>getUntaintedData(value, "tinyblob_type");
    sql:BinaryValue typeVal = new (binaryData);
    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE tinyblob_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeBlobByteParam() returns @tainted record {}|error? {
    record {}|error? value = queryMysqlClient("Select * from ComplexTypes where row_id = 1");
    byte[] binaryData = <byte[]>getUntaintedData(value, "blob_type");
    sql:BlobValue typeVal = new (binaryData);
    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE blob_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeMediumBlobByteParam() returns @tainted record {}|error? {
    record {}|error? value = queryMysqlClient("Select * from ComplexTypes where row_id = 1");
    byte[] binaryData = <byte[]>getUntaintedData(value, "mediumblob_type");
    sql:BlobValue typeVal = new (binaryData);
    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE mediumblob_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeLongBlobByteParam() returns @tainted record {}|error? {
    record {}|error? value = queryMysqlClient("Select * from ComplexTypes where row_id = 1");
    byte[] binaryData = <byte[]>getUntaintedData(value, "longblob_type");
    sql:BlobValue typeVal = new (binaryData);
    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE longblob_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeBlobReadableByteChannelParam() returns @tainted record {}|error? {
    io:ReadableByteChannel byteChannel = check getBlobColumnChannel();
    sql:BlobValue typeVal = new (byteChannel);
    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE blob_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeTinyTextStringParam() returns @tainted record {}|error? {
    sql:TextValue typeVal = new ("very long text");
    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE tinytext_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeTextStringParam() returns @tainted record {}|error? {
    sql:TextValue typeVal = new ("very long text");
    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE text_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeMediumTextStringParam() returns @tainted record {}|error? {
    sql:TextValue typeVal = new ("very long text");
    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE mediumtext_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeLongTextStringParam() returns @tainted record {}|error? {
    sql:TextValue typeVal = new ("very long text");
    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE longtext_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeTextReadableCharChannelParam() returns @tainted record {}|error? {
    io:ReadableCharacterChannel clobChannel = check getTextColumnChannel();
    sql:ClobValue typeVal = new (clobChannel);
    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE text_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTypeNTextReadableCharChannelParam() returns @tainted record {}|error? {
    io:ReadableCharacterChannel clobChannel = check getTextColumnChannel();
    sql:NClobValue typeVal = new (clobChannel);
    sql:ParameterizedQuery sqlQuery = `SELECT * from ComplexTypes WHERE text_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryDateStringParam() returns @tainted record {}|error? {
    //Setting this as var char since the test database seems not working with date type.
    sql:VarcharValue typeVal = new ("2017-02-03");
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE date_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryDateString2Param() returns @tainted record {}|error? {
    sql:VarcharValue typeVal = new ("2017-2-3");
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE date_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTimeStringParam() returns @tainted record {}|error? {
    sql:VarcharValue typeVal = new ("11:35:45");
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE time_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTimeStringInvalidParam() returns @tainted record {}|error? {
    sql:TimeValue typeVal = new ("11-35-45");
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE time_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTimestampStringParam() returns @tainted record {}|error? {
    sql:VarcharValue typeVal = new ("2017-02-03 11:53:00");
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE timestamp_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryTimestampStringInvalidParam() returns @tainted record {}|error? {
    sql:TimestampValue typeVal = new ("2017/02/03 11:53:00");
    sql:ParameterizedQuery sqlQuery = `SELECT * from DateTimeTypes WHERE timestamp_type = ${typeVal}`;
    return queryMysqlClient(sqlQuery);
}

function queryEnumStringParam() returns @tainted record {}|error? {
    string enumVal = "doctor";
    sql:ParameterizedQuery sqlQuery = `SELECT * from ENUMTable where enum_type= ${enumVal}`;
    return queryMysqlClient(sqlQuery);
}

type EnumResult record {|
    int id;
    string enum_type;
|};

function queryEnumStringParam2() returns @tainted record {}|error? {
    string enumVal = "doctor";
    sql:ParameterizedQuery sqlQuery = `SELECT * from ENUMTable where enum_type= ${enumVal}`;
    return queryMysqlClient(sqlQuery, resultType = EnumResult);
}

function querySetStringParam() returns @tainted record {}|error? {
    string setType = "a,d";
    sql:ParameterizedQuery sqlQuery = `SELECT * from SetTable where set_type= ${setType}`;
    return queryMysqlClient(sqlQuery);
}

function queryGeoParam() returns @tainted record {}|error? {
    sql:ParameterizedQuery sqlQuery = `SELECT id, ST_AsText(geom) as geomText from GEOTable`;
    return queryMysqlClient(sqlQuery);
}

function queryGeoParam2() returns @tainted record {}|error? {
    string geoPoint = "POINT (7 52)";
    sql:ParameterizedQuery sqlQuery =
            `SELECT id, ST_AsText(geom) as geomText from GEOTable where geom = ST_GeomFromText(${geoPoint})`;
    return queryMysqlClient(sqlQuery);
}

function queryJsonParam() returns @tainted record {}|error? {
    sql:ParameterizedQuery sqlQuery = `SELECT * from JsonTable`;
    return queryMysqlClient(sqlQuery);
}

type JsonResult record {|
    int id;
    json json_type;
|};

function queryJsonParam2() returns @tainted record {}|error? {
    sql:ParameterizedQuery sqlQuery = `SELECT * from JsonTable`;
    return queryMysqlClient(sqlQuery, resultType = JsonResult);
}

function queryJsonParam3() returns @tainted record {}|error? {
    int id = 100;
    sql:ParameterizedQuery sqlQuery = `SELECT * from JsonTable where json_type->'$.id'=${id}`;
    return queryMysqlClient(sqlQuery, resultType = JsonResult);
}

function getByteColumnChannel() returns @untainted io:ReadableByteChannel|error {
    io:ReadableByteChannel byteChannel = check io:openReadableFile("./src/test/resources/files/byteValue.txt");
    return byteChannel;
}

function getBlobColumnChannel() returns @untainted io:ReadableByteChannel|error {
    io:ReadableByteChannel byteChannel = check io:openReadableFile("./src/test/resources/files/blobValue.txt");
    return byteChannel;
}

function getTextColumnChannel() returns @untainted io:ReadableCharacterChannel|error {
    io:ReadableByteChannel byteChannel = check io:openReadableFile("./src/test/resources/files/clobValue.txt");
    io:ReadableCharacterChannel sourceChannel = new (byteChannel, "UTF-8");
    return sourceChannel;
}

function queryMysqlClient(@untainted string|sql:ParameterizedQuery sqlQuery, typedesc<record {}>? resultType = ())
returns @tainted record {}|error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
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

