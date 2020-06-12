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

function insertIntoDataTable(string url, string user, string password) returns sql:ExecutionResult[]|sql:Error? {
    var data = [
        {intVal:3, longVal:9223372036854774807, floatVal:123.34},
        {intVal:4, longVal:9223372036854774807, floatVal:123.34},
        {intVal:5, longVal:9223372036854774807, floatVal:123.34}
    ];
    sql:ParameterizedQuery[] sqlQueries =
        from var row in data
        select `INSERT INTO DataTable (int_type, long_type, float_type) VALUES (${row.intVal}, ${row.longVal}, ${row.floatVal})`;
    return batchExecuteQueryMockClient(url, user, password, sqlQueries);
}

function insertIntoDataTable2(string url, string user, string password) returns sql:ExecutionResult[]|sql:Error? {
    int intType = 6;
    sql:ParameterizedQuery sqlQuery = `INSERT INTO DataTable (int_type) VALUES(${intType})`;
    sql:ParameterizedQuery[] sqlQueries = [sqlQuery];
    return batchExecuteQueryMockClient(url, user, password, sqlQueries);
}

function insertIntoDataTableFailure(string url, string user, string password) returns sql:ExecutionResult[]|sql:Error? {
    var data = [
        {intVal:7, longVal:9223372036854774807, floatVal:123.34},
        {intVal:1, longVal:9223372036854774807, floatVal:123.34},
        {intVal:9, longVal:9223372036854774807, floatVal:123.34}
    ];
    sql:ParameterizedQuery[] sqlQueries =
        from var row in data
        select `INSERT INTO DataTable (int_type, long_type, float_type) VALUES (${row.intVal}, ${row.longVal}, ${row.floatVal})`;
    return batchExecuteQueryMockClient(url, user, password, sqlQueries);
}

function batchExecuteQueryMockClient(string jdbcURL, string user, string password, sql:ParameterizedQuery[] sqlQueries)
returns sql:ExecutionResult[]|sql:Error? {
    mockclient:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    sql:ExecutionResult[]? result = check dbClient->batchExecute(sqlQueries);
    check dbClient.close();
    return result;
}
