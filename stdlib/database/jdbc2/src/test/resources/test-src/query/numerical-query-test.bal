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
import ballerina/sql;


function testQuery(string jdbcURL, string user, string password) returns @tainted record {}|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    stream<record{}, sql:Error> streamData = dbClient->query("SELECT * FROM NumericTypes");
    record {}? returnData = ();
    error? e = streamData.forEach(function(record {} data) {
        returnData = data;
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

function testQueryNumericTypeRecord(string jdbcURL, string user, string password) returns @tainted NumericType|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    stream<record{}, error> streamResult = dbClient->query("SELECT * FROM NumericTypes", NumericType);
    stream<NumericType, sql:Error> streamData = <stream<NumericType, sql:Error>>streamResult;
    NumericType? returnData = ();
    error? e = streamData.forEach(function(NumericType data) {
        returnData = data;
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

function testQueryNumericInvalidColumnRecord(string jdbcURL, string user, string password) returns @tainted error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    stream<record{}, error> streamResult = dbClient->query("SELECT * FROM NumericTypes", NumericInvalidColumn);
    stream<NumericInvalidColumn, sql:Error> streamData = <stream<NumericInvalidColumn, sql:Error>>streamResult;
    record {|NumericInvalidColumn value;|}? data = check streamData.next();
    check streamData.close();
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

function testQueryNumericOptionalTypeRecord(string jdbcURL, string user, string password)
returns @tainted NumericOptionalType|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    stream<record{}, error> streamResult = dbClient->query("SELECT * FROM NumericTypes", NumericOptionalType);
    stream<NumericOptionalType, sql:Error> streamData = <stream<NumericOptionalType, sql:Error>>streamResult;
    record {|NumericOptionalType value;|}? data = check streamData.next();
    check streamData.close();
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

function testQueryNumericUnionTypeRecord(string jdbcURL, string user, string password)
returns @tainted NumericUnionType|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    stream<record{}, error> streamResult = dbClient->query("SELECT * FROM NumericTypes", NumericUnionType);
    stream<NumericUnionType, sql:Error> streamData = <stream<NumericUnionType, sql:Error>>streamResult;
    record {|NumericUnionType value;|}? data = check streamData.next();
    check streamData.close();
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

function testQueryNumericStringTypeRecord(string jdbcURL, string user, string password)
returns @tainted NumericStringType|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    stream<record{}, error> streamResult = dbClient->query("SELECT * FROM NumericTypes", NumericStringType);
    stream<NumericStringType, sql:Error> streamData = <stream<NumericStringType, sql:Error>>streamResult;
    record {|NumericStringType value;|}? data = check streamData.next();
    check streamData.close();
    NumericStringType? numericType = data?.value;
    check dbClient.close();
    return numericType;
}

public type CustomType int|decimal|float;

type NumericCustomType record {
    CustomType id;
    CustomType int_type;
    CustomType bigint_type;
    CustomType smallint_type;
    CustomType tinyint_type;
    CustomType bit_type;
    CustomType decimal_type;
    CustomType numeric_type;
    CustomType float_type;
    CustomType real_type;
};

function testQueryNumericCustomTypeRecord(string jdbcURL, string user, string password)
returns @tainted NumericCustomType|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    stream<record{}, error> streamResult = dbClient->query("SELECT * FROM NumericTypes", NumericCustomType);
    stream<NumericCustomType, sql:Error> streamData = <stream<NumericCustomType, sql:Error>>streamResult;
    record {|NumericCustomType value;|}? data = check streamData.next();
    check streamData.close();
    NumericCustomType? numericType = data?.value;
    check dbClient.close();
    return numericType;
}

function testQueryFromNullTable(string jdbcURL, string user, string password) returns @tainted record {}[]|error? {
    jdbc:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    stream<record{}, sql:Error> streamData = dbClient->query("SELECT * FROM NumericNullTypes");
    record {}[] returnData = [];
    int count = 0;
    error? e = streamData.forEach(function(record {} data) {
        returnData[count] = data;
        count += 1;
    });
    check dbClient.close();
    return returnData;
}
