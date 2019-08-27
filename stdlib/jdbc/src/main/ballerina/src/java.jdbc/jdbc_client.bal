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

# Represents the base JDBC Client
public type JdbcClient client object {

    # The call remote function implementation for JDBC client to invoke stored procedures/functions.
    #
    # + sqlQuery - The SQL stored procedure to execute
    # + recordType - Array of record types of the returned tables if there is any
    # + parameters - The parameters to be passed to the procedure/function call. The number of parameters is variable
    # + return - A `table[]` if there are tables returned by the call remote function and else nil,
    #            `Error` will be returned if there is any error
    public remote function call(@untainted string sqlQuery, typedesc<record{}>[]? recordType, Param... parameters)
        returns @tainted table<record {}>[]|()|Error {
        return nativeCall(self, sqlQuery, recordType, ...parameters);
    }

    # The select remote function implementation for JDBC Client to select data from tables.
    #
    # + sqlQuery - SQL query to execute
    # + recordType - Type of the returned table
    # + parameters - The parameters to be passed to the select query. The number of parameters is variable
    # + return - A `table` returned by the sql query statement else `Error` will be returned if there
    # is any error
    public remote function select(@untainted string sqlQuery, typedesc<record{}>? recordType,
                                  Param... parameters) returns @tainted table<record {}>|Error {
        return nativeSelect(self, sqlQuery, recordType, ...parameters);
    }

    # The update remote function implementation for JDBC Client to update data and schema of the database.
    #
    # + sqlQuery - SQL statement to execute
    # + parameters - The parameters to be passed to the update query. The number of parameters is variable
    # + return - A `UpdateResult` with the updated row count and key column values,
    #            else `Error` will be returned if there is any error
    public remote function update(@untainted string sqlQuery, Param... parameters) returns UpdateResult|Error {
        return nativeUpdate(self, sqlQuery, ...parameters);
    }

    # The batchUpdate remote function implementation for JDBC Client to batch data insert.
    #
    # + sqlQuery - SQL statement to execute
    # + parameters - Variable number of parameter arrays each representing the set of parameters of belonging to each
    #                individual update
    # + rollbackAllInFailure - If one of the commands in a batch update fails to execute properly, the JDBC driver
    #           may or may not continue to process the remaining commands in the batch.  But this property can be
    #           used to override this behavior.  If it is sets to true, if there is a failure in few commands and
    #           JDBC driver continued with the remaining commands, the successfully executed commands in the batch
    #           also will get rollback.
    # + return - A `BatchUpdateResult` with the updated row count and returned error. If all the commands in the batch
    #                has executed successfully, the error will be `nil`. If one or more commands has failed, the
    #               `returnedError` field will give the correspoing `JdbcClientError` along with the int[] which
    #                conains updated row count or the status returned from the each command in the batch.
    public remote function batchUpdate(@untainted string sqlQuery, boolean rollbackAllInFailure,
                                       Param?[]... parameters)
                                       returns BatchUpdateResult {
        return nativeBatchUpdate(self, sqlQuery, rollbackAllInFailure, ...parameters);
    }
};

function nativeSelect(JdbcClient sqlClient, @untainted string sqlQuery, typedesc<record{}>? recordType,
    Param... parameters) returns @tainted table<record {}>|Error = external;

function nativeCall(JdbcClient sqlClient, @untainted string sqlQuery, typedesc<record{}>[]? recordType, Param... parameters)
    returns @tainted table<record {}>[]|()|Error = external;

function nativeUpdate(JdbcClient sqlClient, @untainted string sqlQuery, Param... parameters)
    returns UpdateResult|Error = external;

function nativeBatchUpdate(JdbcClient sqlClient, @untainted string sqlQuery, boolean rollbackAllInFailure,
    Param?[]... parameters)
    returns BatchUpdateResult = external;

function createClient(ClientEndpointConfig config, PoolOptions globalPoolOptions) returns JdbcClient = external;

# An internal function used by clients to shutdown the connection pool.
#
# + jdbcClient - The Client object which represents the connection pool.
# + return - Possible error during closing
public function close(JdbcClient jdbcClient) returns error? = external;

