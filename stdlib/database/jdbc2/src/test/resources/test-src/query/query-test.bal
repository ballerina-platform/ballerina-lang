// Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/java.jdbc;

function testQuery(string jdbcURL, string user, string password) returns record{}|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    stream<record{}, error> streamData = dbClient.query("SELECT * FROM NumericTypes");
    record{}? returnData = ();
    error? e = streamData.forEach(function(record{} data){
        returnData =  data;
    });
    check dbClient.close();
    return returnData;
}

type NumericType record {
    int id;
    int int_type;
    int bigint_type;
    int smallint_type;
    int tinyint_type;
    boolean bit_type;
    decimal decimal_type;
    decimal numeric_type;
    float float_type;
    float real_type;
};

function testQueryNumericTypeRecord(string jdbcURL, string user, string password) returns NumericType|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    stream<NumericType, error> streamData = <stream<NumericType, error>> dbClient.query("SELECT * FROM NumericTypes", NumericType);
    NumericType? returnData = ();
    error? e =  streamData.forEach(function(NumericType data){
        returnData =  data;
    });
    check dbClient.close();
    return returnData;
}

type NumericInvalidColumn record {
    int num_id;
    int int_type;
    int bigint_type;
    int smallint_type;
    int tinyint_type;
    boolean bit_type;
    decimal decimal_type;
    decimal numeric_type;
    float float_type;
    float real_type;
};

function testQueryNumericInvalidColumnRecord(string jdbcURL, string user, string password) returns error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    stream<NumericInvalidColumn, error> streamData = <stream<NumericInvalidColumn, error>> dbClient.query("SELECT * FROM NumericTypes", NumericInvalidColumn);
    record{| NumericInvalidColumn value; |}? data =  check streamData.next();
    check dbClient.close();
    return ();
}

type NumericOptionalType record {
    int? id;
    int? int_type;
    int? bigint_type;
    int? smallint_type;
    int? tinyint_type;
    boolean? bit_type;
    decimal? decimal_type;
    decimal? numeric_type;
    float? float_type;
    float? real_type;
};

function testQueryNumericOptionalTypeRecord(string jdbcURL, string user, string password) returns NumericOptionalType|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    stream<NumericOptionalType, error> streamData = <stream<NumericOptionalType, error>> dbClient.query("SELECT * FROM NumericTypes", NumericOptionalType);
    record{| NumericOptionalType value; |}? data =  check streamData.next();
    NumericOptionalType? numericType = data?.value;
    check dbClient.close();
    return numericType;
}

type NumericUnionType record {
    int|string id;
    int|string int_type;
    int|string bigint_type;
    int|string smallint_type;
    int|string tinyint_type;
    int|string bit_type;
    int|decimal decimal_type;
    decimal|int numeric_type;
    decimal|float? float_type;
    decimal|float? real_type;
};

function testQueryNumericUnionTypeRecord(string jdbcURL, string user, string password) returns NumericUnionType|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    stream<NumericUnionType, error> streamData = <stream<NumericUnionType, error>> dbClient.query("SELECT * FROM NumericTypes", NumericUnionType);
    record{| NumericUnionType value; |}? data =  check streamData.next();
    NumericUnionType? numericType = data?.value;
    check dbClient.close();
    return numericType;
}

type NumericStringType record {
    string? id;
    string? int_type;
    string? bigint_type;
    string? smallint_type;
    string? tinyint_type;
    string? bit_type;
    string? decimal_type;
    string? numeric_type;
    string? float_type;
    string? real_type;
};

function testQueryNumericStringTypeRecord(string jdbcURL, string user, string password) returns NumericStringType|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    stream<NumericStringType, error> streamData = <stream<NumericStringType, error>> dbClient.query("SELECT * FROM NumericTypes", NumericStringType);
    record{| NumericStringType value; |}? data =  check streamData.next();
    NumericStringType? numericType = data?.value;
    check dbClient.close();
    return numericType;
}