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

import ballerina/mysql;
import ballerina/io;
import ballerina/sql;

string host = "localhost";
string user = "test";
string password = "test123";
string database = "TEST_SQL_PARAMS_EXECUTE";
int port = 3305;

function insertIntoDataTable() returns sql:ExecutionResult|sql:Error? {
    sql:ParameterizedString sqlQuery = {
                parts: ["INSERT INTO DataTable (row_id, int_type, long_type, " +
                "float_type, double_type, boolean_type, string_type, decimal_type) " +
                "VALUES(", ", ", ", ", ", ", " ,", " , ", ", ", " ,", ")" ],
                insertions: [4, 1, 9223372036854774807, 123.34, 2139095039, true, "Hello", 23.45]
    };
    return executeQueryMysqlClient(sqlQuery);
}

function insertIntoDataTable2() returns sql:ExecutionResult|sql:Error? {
    sql:ParameterizedString sqlQuery = {
                parts: ["INSERT INTO DataTable (row_id) " +
                "VALUES(", ")"],
                insertions: [5]
    };
    return executeQueryMysqlClient(sqlQuery);
}

function insertIntoDataTable3() returns sql:ExecutionResult|sql:Error? {
    sql:ParameterizedString sqlQuery = {
                parts: ["INSERT INTO DataTable (row_id, int_type, long_type, " +
                "float_type, double_type, boolean_type, string_type, decimal_type) " +
                "VALUES(", ", ", ", ", ", ", " ,", " , ", ", ", " ,", ")" ],
                insertions: [6, 1, 9372036854774807, 124.34, 29095039, false, "1", 25.45]
    };
    return executeQueryMysqlClient(sqlQuery);
}

function insertIntoDataTable4() returns sql:ExecutionResult|sql:Error? {
    sql:IntegerValue rowId = new (7);
    sql:IntegerValue intType = new (2);
    sql:BigIntValue longType = new (9372036854774807);
    sql:FloatValue floatType = new (124.34);
    sql:DoubleValue doubleType = new (29095039);
    sql:BooleanValue boolType = new (false);
    sql:VarcharValue stringType = new ("stringvalue");
    decimal decimalVal = 25.45;
    sql:DecimalValue decimalType = new (decimalVal);

    sql:ParameterizedString sqlQuery = {
                parts: ["INSERT INTO DataTable (row_id, int_type, long_type, " +
                "float_type, double_type, boolean_type, string_type, decimal_type) " +
                "VALUES(", ", ", ", ", ", ", " ,", " , ", ", ", " ,", ")" ],
                insertions: [rowId, intType, longType, floatType, doubleType, boolType, stringType, decimalType]
    };
    return executeQueryMysqlClient(sqlQuery);
}

function deleteDataTable1() returns sql:ExecutionResult|sql:Error? {
    sql:ParameterizedString sqlQuery = {
                parts: ["DELETE FROM DataTable where row_id=" , "AND int_type=", " AND long_type=",
                "AND double_type=", "AND boolean_type=", "AND string_type=", "AND decimal_type=",
                 ""],
                insertions: [1, 1, 9223372036854774807, 2139095039, true, "Hello", 23.45]
    };
    return executeQueryMysqlClient(sqlQuery);
}

function deleteDataTable2() returns sql:ExecutionResult|sql:Error? {
    sql:ParameterizedString sqlQuery = {
                parts: ["DELETE FROM DataTable where row_id=", ""],
                insertions: [2]
    };
    return executeQueryMysqlClient(sqlQuery);
}

function deleteDataTable3() returns sql:ExecutionResult|sql:Error? {
    sql:IntegerValue rowId = new (3);
    sql:IntegerValue intType = new (1);
    sql:BigIntValue longType = new (9372036854774807);
    sql:FloatValue floatType = new (124.34);
    sql:DoubleValue doubleType = new (29095039);
    sql:BooleanValue boolType = new (false);
    sql:VarcharValue stringType = new ("1");
    decimal decimalVal = 25.45;
    sql:DecimalValue decimalType = new (decimalVal);

    sql:ParameterizedString sqlQuery = {
        parts: ["DELETE FROM DataTable where row_id=" , "AND int_type=", " AND long_type=", " AND double_type=",
                " AND boolean_type=", " AND string_type=",  " AND decimal_type=", ""],
        insertions: [rowId, intType, longType, doubleType, boolType, stringType, decimalType]
    };
    return executeQueryMysqlClient(sqlQuery);
}

function insertIntoComplexTable() returns sql:ExecutionResult|sql:Error? {
    record {}|error? value = queryMysqlClient("Select * from ComplexTypes where row_id = 1");
    byte[] binaryData = <byte[]>getUntaintedData(value, "blob_type");
    
    sql:ParameterizedString sqlQuery = {
                parts: ["INSERT INTO ComplexTypes (row_id, blob_type, text_type, binary_type, var_binary_type) " + 
                "VALUES (", ", ", " , ", ", ", ", ", ")" ],
                insertions: [5, binaryData, "very long text",binaryData, binaryData]
    };
    return executeQueryMysqlClient(sqlQuery);
}

function insertIntoComplexTable2() returns sql:ExecutionResult|error? {
    io:ReadableByteChannel blobChannel = check getBlobColumnChannel();
    io:ReadableCharacterChannel clobChannel = check getClobColumnChannel();
    io:ReadableByteChannel byteChannel = check getByteColumnChannel();

    sql:BlobValue blobType = new (blobChannel);
    sql:TextValue textType = new (clobChannel);
    sql:BlobValue binaryType = new (byteChannel);

    sql:ParameterizedString sqlQuery = {
                parts: ["INSERT INTO ComplexTypes (row_id, blob_type, text_type, binary_type, var_binary_type) " +
                "VALUES (", ", ", " , ", ", ", ", ", ")" ],
                insertions: [6, blobType, textType, binaryType, binaryType]
    };
    return executeQueryMysqlClient(sqlQuery);
}

function insertIntoComplexTable3() returns sql:ExecutionResult|sql:Error|error? {
    sql:ParameterizedString sqlQuery = {
                parts: ["INSERT INTO ComplexTypes (row_id, blob_type, text_type, binary_type, var_binary_type) " +
                "VALUES (", ", " , ", " , " ," , ", ", ")" ],
                insertions: [7, (), (), (), ()]
    };
    return executeQueryMysqlClient(sqlQuery);
}

function deleteComplexTable() returns sql:ExecutionResult|sql:Error? {
    record {}|error? value = queryMysqlClient("Select * from ComplexTypes where row_id = 1");
    byte[] binaryData = <byte[]>getUntaintedData(value, "blob_type");

    sql:ParameterizedString sqlQuery = {
                parts: ["DELETE FROM ComplexTypes where row_id = " , " AND blob_type= " , ""],
                insertions: [2, binaryData]
    };
    return executeQueryMysqlClient(sqlQuery);
}

function deleteComplexTable2() returns sql:ExecutionResult|sql:Error|error? {
    sql:BlobValue blobType = new ();
    sql:TextValue textType = new ();
    sql:BinaryValue binaryType = new ();
    sql:VarBinaryValue varBinaryType = new ();

    sql:ParameterizedString sqlQuery = {
       parts: ["DELETE FROM ComplexTypes where row_id = " , " AND blob_type= " , " AND text_type=", ""],
       insertions: [3, blobType, textType]
    };
    return executeQueryMysqlClient(sqlQuery);
}

function insertIntoNumericTable() returns sql:ExecutionResult|sql:Error? {
    sql:BitValue bitType = new (1);
    sql:ParameterizedString sqlQuery = {
        parts: ["INSERT INTO NumericTypes (id, int_type, bigint_type, smallint_type, tinyint_type, bit_type," +
                " decimal_type, numeric_type, float_type, real_type) " +
                "VALUES(", ", ", ", ", ", ", " ,", " , ", ", ", ", ", ", ", ",", ")"],
        insertions: [3, 2147483647, 9223372036854774807, 32767, 127, bitType, 1234.567, 1234.567, 1234.567, 1234.567]
    };
    return executeQueryMysqlClient(sqlQuery);
}

function insertIntoNumericTable2() returns sql:ExecutionResult|sql:Error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["INSERT INTO NumericTypes (id, int_type, bigint_type, smallint_type, tinyint_type, bit_type," +
                " decimal_type, numeric_type, float_type, real_type) " +
                "VALUES(", ", ", ", ", ", ", " ,", " , ", ", ", ", ", ", ", ",", ")"],
        insertions: [4, (), (), (), (), (), (), (), (), ()]
    };
    return executeQueryMysqlClient(sqlQuery);
}

function insertIntoNumericTable3() returns sql:ExecutionResult|sql:Error? {
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

    sql:ParameterizedString sqlQuery = {
        parts: ["INSERT INTO NumericTypes (id, int_type, bigint_type, smallint_type, tinyint_type, bit_type," +
                " decimal_type, numeric_type, float_type, real_type) " +
                "VALUES(", ", ", ", ", ", ", " ,", " , ", ", ", ", ", ", ", ",", ")"],
        insertions: [id, intType, bigIntType, smallIntType, tinyIntType, bitType, decimalType,
                    numbericType, floatType, realType]
    };
    return executeQueryMysqlClient(sqlQuery);
}

function insertIntoDateTimeTable() returns sql:ExecutionResult|sql:Error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["INSERT INTO DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type)" +
                " VALUES(", ", ", ", ", ", ", " ,", ")"],
        insertions: [2,"2017-02-03", "11:35:45", "2017-02-03 11:53:00", "2017-02-03 11:53:00"]
    };
    return executeQueryMysqlClient(sqlQuery);
}

function insertIntoDateTimeTable2() returns sql:ExecutionResult|error? {
    sql:DateValue dateVal = new ("2017-02-03");
    sql:TimeValue timeVal = new ("11:35:45");
    sql:DateTimeValue dateTimeVal = new ("2017-02-03 11:53:00");
    sql:TimestampValue timestampVal = new ("2017-02-03 11:53:00");
    sql:ParameterizedString sqlQuery = {
        parts: ["INSERT INTO DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type)" +
                " VALUES(", ", ", ", ", ", ", " ,", ")"],
        insertions: [3, dateVal, timeVal, dateTimeVal, timestampVal]
    };
    return executeQueryMysqlClient(sqlQuery);
}

function insertIntoDateTimeTable3() returns sql:ExecutionResult|error? {
    sql:DateValue dateVal = new ();
    sql:TimeValue timeVal = new ();
    sql:DateTimeValue dateTimeVal = new ();
    sql:TimestampValue timestampVal = new ();
    sql:ParameterizedString sqlQuery = {
        parts: ["INSERT INTO DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type)" +
                "VALUES(", ", ", ", ", ", ", " ,", ")"],
        insertions: [4, dateVal, timeVal, dateTimeVal, timestampVal]
    };
    return executeQueryMysqlClient(sqlQuery);
}

function insertIntoDateTimeTable4() returns sql:ExecutionResult|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["INSERT INTO DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type)" +
                " VALUES(", ", ", ", ", ", ", " ,", ")"],
        insertions: [5, (), (), (), ()]
    };
    return executeQueryMysqlClient(sqlQuery);
}

function executeQueryMysqlClient(sql:ParameterizedString sqlQuery)
returns sql:ExecutionResult|sql:Error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
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
