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

import ballerina/sql;
import ballerina/java;

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
    public function __init(public string url, public string? user = (), public string? password = (),
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
    # + sqlQuery - The query which needs to be executed as `string` or `ParameterizedString` when the SQL query has
    #              params to be passed in
    # + rowType - The `typedesc` of the record that should be returned as a result. If this is not provided the default
    #             column names of the query result set be used for the record attributes
    # + return - Stream of records in the type of `rowType`
    public remote function query(@untainted string|sql:ParameterizedString sqlQuery, typedesc<record {}>? rowType = ())
    returns @tainted stream<record{}, sql:Error> {
        if (self.clientActive) {
            sql:ParameterizedString sqlParamString;
            if (sqlQuery is string) {
                sqlParamString = {
                    parts : [sqlQuery],
                    insertions: []
                };
            } else {
                sqlParamString = sqlQuery;
            }
            return nativeQuery(self, sqlParamString, rowType);
        } else {
            return sql:generateApplicationErrorStream("JDBC Client is already closed, hence "
                + "further operations are not allowed");
        }
    }

    # Executes the DDL or DML sql queries provided by the user, and returns summary of the execution.
    #
    # + sqlQuery - The DDL or DML query such as INSERT, DELETE, UPDATE, etc as `string` or `ParameterizedString`
    #              when the query has params to be passed in
    # + return - Summary of the sql update query as `ExecuteResult` or returns `Error`
    #           if any error occured when executing the query
    public remote function execute(@untainted string|sql:ParameterizedString sqlQuery) returns sql:ExecuteResult|sql:Error? {
        if (self.clientActive) {
            sql:ParameterizedString sqlParamString;
            if (sqlQuery is string) {
                sqlParamString = {
                    parts: [sqlQuery],
                    insertions: []
                };
            } else {
                sqlParamString = sqlQuery;
            }
            return nativeExecute(self, sqlParamString);
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

function nativeQuery(Client sqlClient, sql:ParameterizedString sqlQuery, typedesc<record {}>? rowtype)
returns stream<record{}, sql:Error> = @java:Method {
    class: "org.ballerinalang.sql.utils.QueryUtils"
} external;

function nativeExecute(Client sqlClient, sql:ParameterizedString sqlQuery)
returns sql:ExecuteResult|sql:Error? = @java:Method {
    class: "org.ballerinalang.sql.utils.ExecuteUtils"
} external;

function close(Client jdbcClient) returns sql:Error? = @java:Method {
    class: "org.ballerinalang.jdbc.NativeImpl"
} external;
