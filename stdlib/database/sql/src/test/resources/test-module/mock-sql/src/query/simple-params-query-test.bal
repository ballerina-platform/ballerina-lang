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

function querySingleIntParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE row_id = ", ""],
        insertions: [1]
    };
    return queryMockClient(url, user, password, sqlQuery);
}

function queryDoubleIntParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE row_id = ", " AND int_type = ", ""],
        insertions: [1, 1]
    };
    return queryMockClient(url, user, password, sqlQuery);
}

function queryIntAndLongParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE row_id = ", " AND long_type = ", ""],
        insertions: [1, 9223372036854774807]
    };
    return queryMockClient(url, user, password, sqlQuery);
}

function queryStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE string_type = ", ""],
        insertions: ["Hello"]
    };
    return queryMockClient(url, user, password, sqlQuery);
}

function queryIntAndStringParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE string_type = ", "AND row_id = ", ""],
        insertions: ["Hello", 1]
    };
    return queryMockClient(url, user, password, sqlQuery);
}

function queryDoubleParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE double_type = ", ""],
        insertions: [2139095039.0]
    };
    return queryMockClient(url, user, password, sqlQuery);
}

function queryFloatParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE float_type = ", ""],
        insertions: [123.34]
    };
    return queryMockClient(url, user, password, sqlQuery);
}

function queryDoubleAndFloatParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE float_type = ", "and double_type = ", ""],
        insertions: [123.34, 2139095039.0]
    };
    return queryMockClient(url, user, password, sqlQuery);
}


function queryDecimalParam(string url, string user, string password) returns @tainted record {}|error? {
    decimal decimalValue = 23.45;
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE decimal_type = ", ""],
        insertions: [decimalValue]
    };
    return queryMockClient(url, user, password, sqlQuery);
}

function queryDecimalAnFloatParam(string url, string user, string password) returns @tainted record {}|error? {
    decimal decimalValue = 23.45;
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from DataTable WHERE decimal_type = ", "and double_type = ", ""],
        insertions: [decimalValue, 2139095039.0]
    };
    return queryMockClient(url, user, password, sqlQuery);
}

function queryByteArrayParam(string url, string user, string password) returns @tainted record {}|error? {
    record{}|error? value = queryMockClient(url, user, password, "Select * from ComplexTypes where row_id = 1");
    byte[] binaryData = <byte[]> getUntaintedData(value, "BINARY_TYPE");

    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from ComplexTypes WHERE binary_type = ", ""],
        insertions: [binaryData]
    };
    return queryMockClient(url, user, password, sqlQuery);
}

function getUntaintedData(record {}|error? value, string fieldName) returns @untainted anydata{
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
}

function queryTypeFloatDoubleParam(string url, string user, string password) returns @tainted record {}|error? {
    sql:TypedValue typeVal = {
            sqlType: sql:TYPE_FLOAT,
            value: 1234.567
    };
    sql:ParameterizedString sqlQuery = {
        parts: ["SELECT * from NumericTypes WHERE float_type = ", ""],
        insertions: [typeVal]
    };
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
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
    return queryMockClient(url, user, password, sqlQuery);
}

function queryMockClient(string url, string user, string password, @untainted string|sql:ParameterizedString sqlQuery)
returns @tainted record {}|error? {
    mockclient:Client dbClient = check new (url = url, user = user, password = password);
    stream<record{}, error> streamData = dbClient->query(sqlQuery);
    record {|record {} value;|}? data = check streamData.next();
    check streamData.close();
    record {}? value = data?.value;
    check dbClient.close();
    return value;
}
