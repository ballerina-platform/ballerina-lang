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
import ballerina/sql;
import ballerina/io;

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
                insertions: [4, binaryData, "very long text",binaryData, binaryData]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function insertIntoComplexTable2(string url, string user, string password) returns sql:ExecuteResult|sql:Error|error? {
    io:ReadableByteChannel blobChannel = check getBlobColumnChannel();
    io:ReadableCharacterChannel clobChannel = check getClobColumnChannel();
    io:ReadableByteChannel byteChannel = check getByteColumnChannel();

    sql:TypedValue blobType = {sqlType: sql:BLOB, value: blobChannel};
    sql:TypedValue clobType = {sqlType: sql:CLOB, value: clobChannel};
    sql:TypedValue binaryType = {sqlType: sql:BLOB, value: byteChannel};

    sql:ParameterizedString sqlQuery = {
                parts: ["INSERT INTO ComplexTypes (row_id, blob_type, clob_type, binary_type, var_binary_type) " +
                "VALUES (", ", ", " , CONVERT(" , ", CLOB), ", ", ", ")" ],
                insertions: [5, blobType, clobType, binaryType, binaryType]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function insertIntoComplexTable3(string url, string user, string password) returns sql:ExecuteResult|sql:Error|error? {
    sql:ParameterizedString sqlQuery = {
                parts: ["INSERT INTO ComplexTypes (row_id, blob_type, clob_type, binary_type, var_binary_type) " +
                "VALUES (", ", " , ", " , " ," , ", ", ")" ],
                insertions: [6, (), (), (), ()]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function deleteComplexTable(string url, string user, string password) returns sql:ExecuteResult|sql:Error? {
    record {}|error? value = queryMockClient(url, user, password, "Select * from ComplexTypes where row_id = 1");
    byte[] binaryData = <byte[]>getUntaintedData(value, "BLOB_TYPE");

    sql:ParameterizedString sqlQuery = {
                parts: ["DELETE FROM ComplexTypes where row_id = " , " AND blob_type= " , ""],
                insertions: [1, binaryData]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function deleteComplexTable2(string url, string user, string password) returns sql:ExecuteResult|sql:Error|error? {
    io:ReadableByteChannel blobChannel = check getBlobColumnChannel();
    io:ReadableCharacterChannel clobChannel = check getClobColumnChannel();
    io:ReadableByteChannel byteChannel = check getByteColumnChannel();

    sql:TypedValue blobType = {sqlType: sql:BLOB, value: blobChannel};
    sql:TypedValue clobType = {sqlType: sql:CLOB, value: clobChannel};
    sql:TypedValue binaryType = {sqlType: sql:BLOB, value: byteChannel};

    sql:ParameterizedString sqlQuery = {
       parts: ["DELETE FROM ComplexTypes where row_id = " , " AND blob_type= " , " AND clob_type=" ,
               "AND binary_type=", "AND var_binary_type="],
       insertions: [2, blobType, clobType, binaryType, binaryType]
    };
    return executeQueryMockClient(url, user, password, sqlQuery);
}

function deleteComplexTable3(string url, string user, string password) returns sql:ExecuteResult|sql:Error|error? {
    sql:ParameterizedString sqlQuery = {
       parts: ["DELETE FROM ComplexTypes where row_id = " , " AND blob_type= " , " AND clob_type=" ,
               "AND binary_type=", "AND var_binary_type="],
       insertions: [3, (), (), (), ()]
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
