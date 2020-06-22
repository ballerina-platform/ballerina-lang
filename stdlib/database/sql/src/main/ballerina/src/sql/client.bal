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

import ballerina/java;

# Represents a SQL client.
#
public type Client abstract client object {

    # Queries the database with the query provided by the user, and returns the result as stream.
    #
    # + sqlQuery - The query which needs to be executed as `string` or `ParameterizedQuery` when the SQL query has
    #              params to be passed in
    # + rowType - The `typedesc` of the record that should be returned as a result. If this is not provided the default
    #             column names of the query result set be used for the record attributes
    # + return - Stream of records in the type of `rowType`
    public remote function query(@untainted string|ParameterizedQuery sqlQuery, typedesc<record {}>? rowType = ())
    returns @tainted stream <record {}, Error>;

    # Executes the DDL or DML sql query provided by the user, and returns summary of the execution.
    #
    # + sqlQuery - The DDL or DML query such as INSERT, DELETE, UPDATE, etc as `string` or `ParameterizedQuery`
    #              when the query has params to be passed in
    # + return - Summary of the sql update query as `ExecutionResult` or an `Error`
    #           if any error occurred when executing the query
    public remote function execute(@untainted string|ParameterizedQuery sqlQuery) returns ExecutionResult|Error;

    # Executes a batch of parameterised DDL or DML sql query provided by the user,
    # and returns the summary of the execution.
    #
    # + sqlQueries - The DDL or DML query such as INSERT, DELETE, UPDATE, etc as `ParameterizedQuery` with an array
    #                of values passed in.
    # + return - Summary of the executed SQL queries as `ExecutionResult[]` which includes details such as
    #            `affectedRowCount` and `lastInsertId`. If one of the commands in the batch fails, this function
    #            will return `BatchExecuteError`, however the JDBC driver may or may not continue to process the
    #            remaining commands in the batch after a failure. The summary of the executed queries in case of error
    #            can be accessed as `(<sql:BatchExecuteError> result).detail()?.executionResults`.
    public remote function batchExecute(@untainted ParameterizedQuery[] sqlQueries) returns ExecutionResult[]|Error;

    # Close the SQL client.
    #
    # + return - Possible error during closing the client
    public function close() returns Error?;
};

function closedStreamInvocationError() returns Error {
   return ApplicationError("Stream is closed. Therefore, no operations are allowed further on the stream.");
}

public function generateApplicationErrorStream(string message) returns stream<record{}, Error> {
    ApplicationError applicationErr = ApplicationError(message);
    ResultIterator resultIterator = new (err = applicationErr);
    stream<record{}, Error> errorStream = new (resultIterator);
    return errorStream;
}

function nextResult(ResultIterator iterator) returns record {}|Error? = @java:Method {
    class: "org.ballerinalang.sql.utils.RecordItertorUtils"
} external;

function closeResult(ResultIterator iterator) returns Error? = @java:Method {
    class: "org.ballerinalang.sql.utils.RecordItertorUtils"
} external;
