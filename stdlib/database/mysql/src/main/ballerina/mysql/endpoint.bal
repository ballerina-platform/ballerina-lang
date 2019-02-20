// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# The Client endpoint configuration for mysql databases.
#
# + host - The host name of the database to connect
# + port - The port of the database to connect
# + name - The name of the database to connect
# + username - Username for the database connection
# + password - Password for the database connection
# + poolOptions - Properties for the connection pool configuration. Refer `sql:PoolOptions` for more details
# + dbOptions - A map of DB specific properties
public type ClientEndpointConfig record {
    string host;
    int port = 3306;
    string name = "";
    string username = "";
    string password = "";
    sql:PoolOptions poolOptions = {};
    map<any> dbOptions = {};
    !...;
};

# Represents an MySQL client endpoint.
#
# + config - The configurations associated with the SQL endpoint
# + sqlClient - The base SQL Client
public type Client client object {
    *sql:AbstractSQLClient;
    private ClientEndpointConfig config;
    private sql:Client sqlClient;

    # Gets called when the MySQL client is instantiated.
    public function __init(ClientEndpointConfig c) {
        self.config = c;
        self.sqlClient = createClient(c);
    }

    # The call operation implementation for MySQL Client to invoke stored procedures/functions.
    #
    # + sqlQuery - The SQL stored procedure to execute
    # + recordType - Array of record types of the returned tables if there is any
    # + parameters - The parameters to be passed to the procedure/function call. The number of parameters is variable
    # + return - A `table[]` if there are tables returned by the call remote function and else nil,
    #            `error` will be returned if there is any error
    public remote function call(@sensitive string sqlQuery, typedesc[]? recordType, sql:Param... parameters)
                               returns @tainted table<record {}>[]|()|error {
        return self.sqlClient->call(sqlQuery, recordType, ...parameters);
    }

    # The select operation implementation for MySQL Client to select data from tables.
    #
    # + sqlQuery - SQL query to execute
    # + recordType - Type of the returned table
    # + loadToMemory - Indicates whether to load the retrieved data to memory or not
    # + parameters - The parameters to be passed to the select query. The number of parameters is variable
    # + return - A `table` returned by the sql query statement else `error` will be returned if there is any error
    public remote function select(@sensitive string sqlQuery, typedesc? recordType, boolean loadToMemory = false,
                                  sql:Param... parameters) returns @tainted table<record {}>|error {
        return self.sqlClient->select(sqlQuery, recordType, loadToMemory = loadToMemory, ...parameters);
    }


    # The update operation implementation for MySQL Client to update data and schema of the database.
    #
    # + sqlQuery - SQL statement to execute
    # + parameters - The parameters to be passed to the update query. The number of parameters is variable
    # + return - `int` number of rows updated by the statement and else `error` will be returned if there is any error
    public remote function update(@sensitive string sqlQuery, sql:Param... parameters) returns int|error {
        return self.sqlClient->update(sqlQuery, ...parameters);
    }

    # The batchUpdate operation implementation for MySQL Client to batch data insert.
    #
    # + sqlQuery - SQL statement to execute
    # + parameters - Variable number of parameter arrays each representing the set of parameters of belonging to each
    #                individual update
    # + return - An `int[]` - The elements in the array returned by the operation may be one of the following  or else
    #            an`error` will be returned if there is any error.
    #            A number greater than or equal to zero - indicates that the command was processed successfully
    #                                                     and is an update count giving the number of rows
    #            A value of -2 - Indicates that the command was processed successfully but that the number of rows affected
    #                            is unknown
    #            A value of -3 - Indicates that the command failed to execute successfully and occurs only if a driver
    #                            continues to process commands after a command fails
    public remote function batchUpdate(@sensitive string sqlQuery, sql:Param?[]... parameters) returns int[]|error {
        return self.sqlClient->batchUpdate(sqlQuery, ...parameters);
    }

    # The updateWithGeneratedKeys operation implementation for MySQL Client which returns the auto
    # generated keys during the update remote function.
    #
    # + sqlQuery - SQL statement to execute
    # + keyColumns - Names of auto generated columns for which the auto generated key values are returned
    # + parameters - The parameters to be passed to the update query. The number of parameters is variable
    # + return - A `Tuple` will be returned and would represent updated row count during the query exectuion,
    #            aray of auto generated key values during the query execution, in order.
    #            Else `error` will be returned if there is any error.
    public remote function updateWithGeneratedKeys(@sensitive string sqlQuery, string[]? keyColumns,
                                                   sql:Param... parameters) returns (int, string[])|error {
        return self.sqlClient->updateWithGeneratedKeys(sqlQuery,keyColumns, ...parameters);
    }

    # Stops the JDBC client.
    public function stop() {
        sql:close(self.sqlClient);
    }
};

extern function createClient(ClientEndpointConfig config) returns sql:Client;

