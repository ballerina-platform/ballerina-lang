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
import ballerina/sql;

# Represents a JDBC client.
#
public type Client client object {
    *sql:Client;
    private boolean clientActive = true;

    # Initialize JDBC client.
    #
    # + url - The JDBC  URL of the database
    # + user - If the database is secured, the username of the database
    # + password - The password of provided username of the database
    # + options - The Database specific JDBC client properties
    # + connectionPool - The `sql:ConnectionPool` object to be used within the jdbc client.
    #                   If there is no connectionPool is provided, the global connection pool will be used and it will
    #                   be shared by other clients which has same properties
    public function init(public string url, public string? user = (), public string? password = (),
        public Options? options = (), public sql:ConnectionPool? connectionPool = ()) returns sql:Error? {
        ClientConfiguration clientConf = {
            url: url,
            user: user,
            password: password,
            options: options,
            connectionPool: connectionPool
        };
        return createClient(self, clientConf, sql:getGlobalConnectionPool());
    }

    # Queries the database with the query provided by the user, and returns the result as stream.
    #
    # + sqlQuery - The query which needs to be executed as `string` or `ParameterizedQuery` when the SQL query has
    #              params to be passed in
    # + rowType - The `typedesc` of the record that should be returned as a result. If this is not provided the default
    #             column names of the query result set be used for the record attributes
    # + return - Stream of records in the type of `rowType`
    public remote function query(@untainted string|sql:ParameterizedQuery sqlQuery, typedesc<record {}>? rowType = ())
    returns @tainted stream <record {}, sql:Error> {
        if (self.clientActive) {
            return nativeQuery(self, sqlQuery, rowType);
        } else {
            return sql:generateApplicationErrorStream("JDBC Client is already closed, hence "
                + "further operations are not allowed");
        }
    }

    # Executes the DDL or DML sql queries provided by the user, and returns summary of the execution.
    #
    # + sqlQuery - The DDL or DML query such as INSERT, DELETE, UPDATE, etc as `string` or `ParameterizedQuery`
    #              when the query has params to be passed in
    # + return - Summary of the sql update query as `ExecutionResult` or returns `Error`
    #           if any error occurred when executing the query
    public remote function execute(@untainted string|sql:ParameterizedQuery sqlQuery) returns sql:ExecutionResult|sql:Error {
        if (self.clientActive) {
            return nativeExecute(self, sqlQuery);
        } else {
            return sql:ApplicationError("JDBC Client is already closed, hence further operations are not allowed");
        }
    }

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
    public remote function batchExecute(@untainted sql:ParameterizedQuery[] sqlQueries) returns sql:ExecutionResult[]|sql:Error {
        if (sqlQueries.length() == 0) {
            return sql:ApplicationError(" Parameter 'sqlQueries' cannot be empty array");
        }
        if (self.clientActive) {
            return nativeBatchExecute(self, sqlQueries);
        } else {
            return sql:ApplicationError("JDBC Client is already closed, hence further operations are not allowed");
        }
    }

    # Close the JDBC client.
    #
    # + return - Possible error during closing the client
    public function close() returns sql:Error? {
        self.clientActive = false;
        return close(self);
    }
};

# Provides a set of configuration related to database.
# + datasourceName - The driver class name to be used to get the connection
# + properties - the properties of the database which should be applied when getting the connection
public type Options record {|
    string? datasourceName = ();
    map<anydata>? properties = ();
|};

# Provides a set of configurations for the JDBC Client to be passed internally within the module.
#
# + url - URL of the database to connect
# + user - Username for the database connection
# + password - Password for the database connection
# + options - A map of DB specific `Options`
# + connectionPool - Properties for the connection pool configuration. Refer `sql:ConnectionPool` for more details
type ClientConfiguration record {|
    string? url;
    string? user;
    string? password;
    Options? options;
    sql:ConnectionPool? connectionPool;
|};

function createClient(Client jdbcClient, ClientConfiguration clientConf,
    sql:ConnectionPool globalConnPool) returns sql:Error? = @java:Method {
    class: "org.ballerinalang.jdbc.NativeImpl"
} external;

function nativeQuery(Client sqlClient, string|sql:ParameterizedQuery sqlQuery, typedesc<record {}>? rowtype)
returns stream <record {}, sql:Error> = @java:Method {
    class: "org.ballerinalang.sql.utils.QueryUtils"
} external;

function nativeExecute(Client sqlClient, string|sql:ParameterizedQuery sqlQuery)
returns sql:ExecutionResult|sql:Error = @java:Method {
    class: "org.ballerinalang.sql.utils.ExecuteUtils"
} external;

function nativeBatchExecute(Client sqlClient, sql:ParameterizedQuery[] sqlQueries)
returns sql:ExecutionResult[]|sql:Error = @java:Method {
    class: "org.ballerinalang.sql.utils.ExecuteUtils"
} external;

function close(Client jdbcClient) returns sql:Error? = @java:Method {
    class: "org.ballerinalang.jdbc.NativeImpl"
} external;
