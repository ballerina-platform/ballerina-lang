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
    sql:ParameterizedString sqlQuery1 = {
            parts: ["INSERT INTO DataTable (int_type, long_type, float_type) VALUES(", ", ", ", ", ")" ],
            insertions: [3, 9223372036854774807, 123.34]
    };
    sql:ParameterizedString sqlQuery2 = {
            parts: ["INSERT INTO DataTable (int_type, long_type, float_type) VALUES(", ", ", ", ", ")" ],
            insertions: [4, 9223372036854774807, 123.34]
        };

    sql:ParameterizedString sqlQuery3 = {
            parts: ["INSERT INTO DataTable (int_type, long_type, float_type) VALUES(", ", ", ", ", ")" ],
            insertions: [5, 9223372036854774807, 123.34]
    };

    sql:ParameterizedString[] sqlQueries = [sqlQuery1, sqlQuery2, sqlQuery3];
    return batchExecuteQueryMockClient(url, user, password, sqlQueries);
}

function insertIntoDataTable2(string url, string user, string password) returns sql:ExecutionResult[]|sql:Error? {
    sql:ParameterizedString sqlQuery = {
                parts: ["INSERT INTO DataTable (int_type) VALUES(", ")"],
                insertions: [6]
    };
    sql:ParameterizedString[] sqlQueries = [sqlQuery];
    return batchExecuteQueryMockClient(url, user, password, sqlQueries);
}

function insertIntoDataTableFailure(string url, string user, string password) returns sql:ExecutionResult[]|sql:Error? {
    sql:ParameterizedString sqlQuery1 = {
            parts: ["INSERT INTO DataTable (int_type, long_type, float_type) VALUES(", ", ", ", ", ")" ],
            insertions: [7, 9372036854774807, 124.34]
    };
    sql:ParameterizedString sqlQuery2 = {
            parts: ["INSERT INTO DataTable (int_type, long_type, float_type) VALUES(", ", ", ", ", ")" ],
            insertions: [1, 9372036854774807, 124.34]
    };
    sql:ParameterizedString sqlQuery3 = {
            parts: ["INSERT INTO DataTable (int_type, long_type, float_type) VALUES(", ", ", ", ", ")" ],
            insertions: [9, 9372036854774807, 124.34]
    };
    sql:ParameterizedString[] sqlQueries = [sqlQuery1, sqlQuery2, sqlQuery3];
    return batchExecuteQueryMockClient(url, user, password, sqlQueries);
}

function batchExecuteQueryMockClient(string jdbcURL, string user, string password, sql:ParameterizedString[] sqlQueries)
returns sql:ExecutionResult[]|sql:Error? {
    mockclient:Client dbClient = check new (url = jdbcURL, user = user, password = password);
    sql:ExecutionResult[]? result = check dbClient->batchExecute(sqlQueries);
    check dbClient.close();
    return result;
}
