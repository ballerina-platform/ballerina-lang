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
import mockclient;
import ballerina/io;
import ballerina/sql;
import ballerina/time;

function insertIntoDataTable(string url, string user, string password) returns sql:ExecuteResult|sql:Error? {
    sql:ParameterizedString sqlQuery = {
                parts: ["INSERT INTO DataTable (row_id, int_type, long_type, " +
                "float_type, double_type, boolean_type, string_type, decimal_type) " +
                "VALUES(", ", ", ", ", ", ", " ,", " , ", ", ", " ,", ")" ],
                insertions: [4, 1, 9223372036854774807, 123.34, 2139095039, true, "Hello", 23.45]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function insertIntoDataTable2(string url, string user, string password) returns sql:ExecuteResult|sql:Error? {
    sql:ParameterizedString sqlQuery = {
                parts: ["INSERT INTO DataTable (row_id) " +
                "VALUES(", ")"],
                insertions: [5]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function insertIntoDataTable3(string url, string user, string password) returns sql:ExecuteResult|sql:Error? {
    sql:ParameterizedString sqlQuery = {
                parts: ["INSERT INTO DataTable (row_id, int_type, long_type, " +
                "float_type, double_type, boolean_type, string_type, decimal_type) " +
                "VALUES(", ", ", ", ", ", ", " ,", " , ", ", ", " ,", ")" ],
                insertions: [6, 1, 9372036854774807, 124.34, 29095039, false, "1", 25.45]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function insertIntoDataTable4(string url, string user, string password) returns sql:ExecuteResult|sql:Error? {
    sql:TypedValue rowId = {sqlType: sql:INTEGER, value: 7};
    sql:TypedValue intType = {sqlType: sql:INTEGER, value: 2};
    sql:TypedValue longType = {sqlType: sql:BIGINT, value: 9372036854774807};
    sql:TypedValue floatType = {sqlType: sql:FLOAT, value:  124.34};
    sql:TypedValue doubleType = {sqlType: sql:DOUBLE, value:  29095039};
    sql:TypedValue boolType = {sqlType: sql:BOOLEAN, value:  "false"};
    sql:TypedValue stringType = {sqlType: sql:VARCHAR, value:  "stringvalue"};
    decimal decimalVal = 25.45;
    sql:TypedValue decimalType = {sqlType: sql:DECIMAL, value: decimalVal};

    sql:ParameterizedString sqlQuery = {
                parts: ["INSERT INTO DataTable (row_id, int_type, long_type, " +
                "float_type, double_type, boolean_type, string_type, decimal_type) " +
                "VALUES(", ", ", ", ", ", ", " ,", " , ", ", ", " ,", ")" ],
                insertions: [rowId, intType, longType, floatType, doubleType, boolType, stringType, decimalType]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function deleteDataTable1(string url, string user, string password) returns sql:ExecuteResult|sql:Error? {
    sql:ParameterizedString sqlQuery = {
                parts: ["DELETE FROM DataTable where row_id=" , "AND int_type=", " AND long_type=",
                "AND float_type=" , "AND double_type=", "AND boolean_type=", "AND string_type=", "AND decimal_type=", ""],
                insertions: [1, 1, 9223372036854774807, 123.34, 2139095039, true, "Hello", 23.45]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function deleteDataTable2(string url, string user, string password) returns sql:ExecuteResult|sql:Error? {
    sql:ParameterizedString sqlQuery = {
                parts: ["DELETE FROM DataTable where row_id=", ""],
                insertions: [2]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function deleteDataTable3(string url, string user, string password) returns sql:ExecuteResult|sql:Error? {
    sql:TypedValue rowId = {sqlType: sql:INTEGER, value: 3};
    sql:TypedValue intType = {sqlType: sql:INTEGER, value: 1};
    sql:TypedValue longType = {sqlType: sql:BIGINT, value: 9372036854774807};
    sql:TypedValue floatType = {sqlType: sql:FLOAT, value:  124.34};
    sql:TypedValue doubleType = {sqlType: sql:DOUBLE, value:  29095039};
    sql:TypedValue boolType = {sqlType: sql:BOOLEAN, value:  false};
    sql:TypedValue stringType = {sqlType: sql:VARCHAR, value:  "1"};
    decimal decimalVal = 25.45;
    sql:TypedValue decimalType = {sqlType: sql:DECIMAL, value: decimalVal};

    sql:ParameterizedString sqlQuery = {
        parts: ["DELETE FROM DataTable where row_id=" , "AND int_type=", " AND long_type=", " AND double_type=",
                " AND boolean_type=", " AND string_type=",  " AND decimal_type=", ""],
        insertions: [rowId, intType, longType, doubleType, boolType, stringType, decimalType]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function insertIntoComplexTable(string url, string user, string password) returns sql:ExecuteResult|sql:Error? {
    record {}|error? value = queryMockClient(url, user, password, "Select * from ComplexTypes where row_id = 1");
    byte[] binaryData = <byte[]>getUntaintedData(value, "BLOB_TYPE");
    
    sql:ParameterizedString sqlQuery = {
                parts: ["INSERT INTO ComplexTypes (row_id, blob_type, clob_type, binary_type, var_binary_type) " + 
                "VALUES (", ", ", " , CONVERT(" , ", CLOB), ", ", ", ")" ],
                insertions: [5, binaryData, "very long text",binaryData, binaryData]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function insertIntoComplexTable2(string url, string user, string password) returns sql:ExecuteResult|error? {
    io:ReadableByteChannel blobChannel = check getBlobColumnChannel();
    io:ReadableCharacterChannel clobChannel = check getClobColumnChannel();
    io:ReadableByteChannel byteChannel = check getByteColumnChannel();

    sql:TypedValue blobType = {sqlType: sql:BLOB, value: blobChannel};
    sql:TypedValue clobType = {sqlType: sql:CLOB, value: clobChannel};
    sql:TypedValue binaryType = {sqlType: sql:BLOB, value: byteChannel};

    sql:ParameterizedString sqlQuery = {
                parts: ["INSERT INTO ComplexTypes (row_id, blob_type, clob_type, binary_type, var_binary_type) " +
                "VALUES (", ", ", " , CONVERT(" , ", CLOB), ", ", ", ")" ],
                insertions: [6, blobType, clobType, binaryType, binaryType]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function insertIntoComplexTable3(string url, string user, string password) returns sql:ExecuteResult|sql:Error|error? {
    sql:ParameterizedString sqlQuery = {
                parts: ["INSERT INTO ComplexTypes (row_id, blob_type, clob_type, binary_type, var_binary_type) " +
                "VALUES (", ", " , ", " , " ," , ", ", ")" ],
                insertions: [7, (), (), (), ()]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function deleteComplexTable(string url, string user, string password) returns sql:ExecuteResult|sql:Error? {
    record {}|error? value = queryMockClient(url, user, password, "Select * from ComplexTypes where row_id = 1");
    byte[] binaryData = <byte[]>getUntaintedData(value, "BLOB_TYPE");

    sql:ParameterizedString sqlQuery = {
                parts: ["DELETE FROM ComplexTypes where row_id = " , " AND blob_type= " , ""],
                insertions: [2, binaryData]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function deleteComplexTable2(string url, string user, string password) returns sql:ExecuteResult|sql:Error|error? {
    sql:TypedValue blobType = {sqlType: sql:BLOB, value: ()};
    sql:TypedValue clobType = {sqlType: sql:CLOB, value: ()};
    sql:TypedValue binaryType = {sqlType: sql:BINARY, value: ()};
    sql:TypedValue varBinaryType = {sqlType: sql:VARBINARY, value: ()};

    sql:ParameterizedString sqlQuery = {
       parts: ["DELETE FROM ComplexTypes where row_id = " , " AND blob_type= " , " AND clob_type=", ""],
       insertions: [4, blobType, clobType]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function insertIntoNumericTable(string url, string user, string password) returns sql:ExecuteResult|sql:Error? {
    sql:TypedValue bitType = {sqlType: sql:BIT, value: 1};
    sql:ParameterizedString sqlQuery = {
        parts: ["INSERT INTO NumericTypes (id, int_type, bigint_type, smallint_type, tinyint_type, bit_type," +
                " decimal_type, numeric_type, float_type, real_type) " +
                "VALUES(", ", ", ", ", ", ", " ,", " , ", ", ", ", ", ", ", ",", ")"],
        insertions: [3, 2147483647, 9223372036854774807, 32767, 127, bitType, 1234.567, 1234.567, 1234.567, 1234.567]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function insertIntoNumericTable2(string url, string user, string password) returns sql:ExecuteResult|sql:Error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["INSERT INTO NumericTypes (id, int_type, bigint_type, smallint_type, tinyint_type, bit_type," +
                " decimal_type, numeric_type, float_type, real_type) " +
                "VALUES(", ", ", ", ", ", ", " ,", " , ", ", ", ", ", ", ", ",", ")"],
        insertions: [4, (), (), (), (), (), (), (), (), ()]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function insertIntoNumericTable3(string url, string user, string password) returns sql:ExecuteResult|sql:Error? {
    sql:TypedValue id = {sqlType: sql:INTEGER, value: 5};
    sql:TypedValue intType = {sqlType: sql:INTEGER, value: 2147483647};
    sql:TypedValue bigIntType = {sqlType: sql:BIGINT, value: 9223372036854774807};
    sql:TypedValue smallIntType = {sqlType: sql:SMALLINT, value: 32767};
    sql:TypedValue tinyIntType = {sqlType: sql:SMALLINT, value: 127};
    sql:TypedValue bitType = {sqlType: sql:BIT, value: 1};
    decimal decimalVal = 1234.567;
    sql:TypedValue decimalType = {sqlType: sql:DECIMAL, value: decimalVal};
    sql:TypedValue numbericType = {sqlType: sql:NUMERIC, value: 1234.567};
    sql:TypedValue floatType = {sqlType: sql:FLOAT, value: 1234.567};
    sql:TypedValue realType = {sqlType: sql:REAL, value: 1234.567};

    sql:ParameterizedString sqlQuery = {
        parts: ["INSERT INTO NumericTypes (id, int_type, bigint_type, smallint_type, tinyint_type, bit_type," +
                " decimal_type, numeric_type, float_type, real_type) " +
                "VALUES(", ", ", ", ", ", ", " ,", " , ", ", ", ", ", ", ", ",", ")"],
        insertions: [id, intType, bigIntType, smallIntType, tinyIntType, bitType, decimalType,
                    numbericType, floatType, realType]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function insertIntoDateTimeTable(string url, string user, string password) returns sql:ExecuteResult|sql:Error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["INSERT INTO DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type, time_type2," +
                " timestamp_type2) VALUES(", ", ", ", ", ", ", " ,", " , ", ",", ")"],
        insertions: [2,"2017-02-03", "11:35:45", "2017-02-03 11:53:00", "2017-02-03 11:53:00", "20:08:08-8:00",
                     "2008-08-08 20:08:08+8:00"]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function insertIntoDateTimeTable2(string url, string user, string password) returns sql:ExecuteResult|error? {
    sql:TypedValue dateVal = {sqlType: sql:DATE, value: "2017-02-03"};
    sql:TypedValue timeVal = {sqlType: sql:TIME, value: "11:35:45"};
    sql:TypedValue dateTimeVal = {sqlType: sql:DATETIME, value: "2017-02-03 11:53:00"};
    sql:TypedValue timestampVal = {sqlType: sql:TIMESTAMP, value: "2017-02-03 11:53:00"};
    sql:TypedValue time2Val = {sqlType: sql:TIME, value: check time:parse("20:08:08-0800", "HH:mm:ssZ")};
    sql:TypedValue timestamp2Val = {
        sqlType: sql:TIMESTAMP,
        value:  check time:parse("2008-08-08 20:08:08+0800", "yyyy-MM-dd HH:mm:ssZ")
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["INSERT INTO DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type, time_type2," +
                " timestamp_type2) VALUES(", ", ", ", ", ", ", " ,", " , ", ",", ")"],
        insertions: [3, dateVal, timeVal, dateTimeVal, timestampVal, time2Val, timestamp2Val]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function insertIntoDateTimeTable3(string url, string user, string password) returns sql:ExecuteResult|error? {
    sql:TypedValue dateVal = {sqlType: sql:DATE, value: ()};
    sql:TypedValue timeVal = {sqlType: sql:TIME, value: ()};
    sql:TypedValue dateTimeVal = {sqlType: sql:DATETIME, value: ()};
    sql:TypedValue timestampVal = {sqlType: sql:TIMESTAMP, value: ()};
    sql:TypedValue time2Val = {sqlType: sql:TIME, value: ()};
    sql:TypedValue timestamp2Val = {sqlType: sql:TIMESTAMP, value: ()};
    sql:ParameterizedString sqlQuery = {
        parts: ["INSERT INTO DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type, time_type2," +
                " timestamp_type2) VALUES(", ", ", ", ", ", ", " ,", " , ", ",", ")"],
        insertions: [4, dateVal, timeVal, dateTimeVal, timestampVal, time2Val, timestamp2Val]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function insertIntoDateTimeTable4(string url, string user, string password) returns sql:ExecuteResult|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["INSERT INTO DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type, time_type2," +
                " timestamp_type2) VALUES(", ", ", ", ", ", ", " ,", " , ", ",", ")"],
        insertions: [5, (), (), (), (), (), ()]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function insertIntoArrayTable(string url, string user, string password) returns sql:ExecuteResult|error? {
    int[] dataint = [1, 2, 3];
    int[] datalong = [100000000, 200000000, 300000000];
    float[] datafloat = [245.23, 5559.49, 8796.123];
    float[] datadouble = [245.23, 5559.49, 8796.123];
    decimal[] datadecimal = [245, 5559, 8796];
    string[] datastring = ["Hello", "Ballerina"];
    boolean[] databoolean = [true, false, true];

    record {}|error? value = queryMockClient(url, user, password, "Select * from ComplexTypes where row_id = 1");
    byte[][] dataBlob = [<byte[]>getUntaintedData(value, "BLOB_TYPE")];

    sql:TypedValue paraInt = {sqlType: sql:ARRAY, value: dataint};
    sql:TypedValue paraLong = {sqlType: sql:ARRAY, value: datalong};
    sql:TypedValue paraFloat = {sqlType: sql:ARRAY, value: datafloat};
    sql:TypedValue paraDecimal = {sqlType: sql:ARRAY, value: datadecimal};
    sql:TypedValue paraDouble = {sqlType: sql:ARRAY, value: datadouble};
    sql:TypedValue paraString = {sqlType: sql:ARRAY, value: datastring};
    sql:TypedValue paraBool = {sqlType: sql:ARRAY, value: databoolean};
    sql:TypedValue paraBlob = {sqlType: sql:ARRAY, value: dataBlob};

    sql:ParameterizedString sqlQuery = {
        parts: ["INSERT INTO ArrayTypes (row_id, int_array, long_array, float_array, double_array, decimal_array, " +
                "boolean_array, string_array, blob_array) VALUES(", ", ", ", ", ", ", " ," , " ," , " ," , ",",
                ", ", ")"],
        insertions: [5, paraInt, paraLong, paraFloat, paraDouble, paraDecimal, paraBool, paraString, paraBlob]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function insertIntoArrayTable2(string url, string user, string password) returns sql:ExecuteResult|error? {
    sql:TypedValue paraInt = {sqlType: sql:ARRAY, value: ()};
    sql:TypedValue paraLong = {sqlType: sql:ARRAY, value: ()};
    sql:TypedValue paraFloat = {sqlType: sql:ARRAY, value: ()};
    sql:TypedValue paraDecimal = {sqlType: sql:ARRAY, value: ()};
    sql:TypedValue paraDouble = {sqlType: sql:ARRAY, value: ()};
    sql:TypedValue paraString = {sqlType: sql:ARRAY, value: ()};
    sql:TypedValue paraBool = {sqlType: sql:ARRAY, value: ()};
    sql:TypedValue paraBlob = {sqlType: sql:ARRAY, value: ()};

    sql:ParameterizedString sqlQuery = {
        parts: ["INSERT INTO ArrayTypes (row_id, int_array, long_array, float_array, double_array, decimal_array, " +
                "boolean_array, string_array, blob_array) VALUES(", ", ", ", ", ", ", " ," , " ," , " ," , ",",
                ", ", ")"],
        insertions: [6, paraInt, paraLong, paraFloat, paraDouble, paraDecimal, paraBool, paraString, paraBlob]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function executeQueryMockClient(string jdbcURL, string user, string password, sql:ParameterizedString sqlQuery)
returns sql:ExecuteResult|sql:Error? {
    mockclient:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    sql:ExecuteResult? result = check dbClient->execute(sqlQuery);
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

function queryMockClient(string url, string user, string password,@untainted string|sql:ParameterizedString sqlQuery)
returns @tainted record {}|error? {
    mockclient:Client dbClient = check new (url = url, user = user, password = password);
    stream<record{}, error> streamData = dbClient->query(sqlQuery);
    record {|record {} value;|}? data = check streamData.next();
    check streamData.close();
    record {}? value = data?.value;
    check dbClient.close();
    return value;
}
