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

# Represents a JDBC client endpoint.
#
# + jdbcClient - The base JDBC Client
public type Client client object {

    private JdbcClient jdbcClient;
    private boolean clientActive = true;

    # Gets called when the JDBC client is instantiated.
    public function __init(ClientEndpointConfig c) {
        self.jdbcClient = createClient(c, getGlobalPoolConfigContainer().getGlobalPoolConfig());
    }

    # The call remote function implementation for JDBC Client to invoke stored procedures/functions.
    #
    # + sqlQuery - The SQL stored procedure to execute
    # + recordType - Array of record types of the returned tables if there is any
    # + parameters - The parameters to be passed to the procedure/function call. The number of parameters is variable
    # + return - A `table[]` if there are tables returned by the call remote function and else nil,
    #            `Error` will be returned if there is any error
    public remote function call(@untainted string sqlQuery, typedesc<record{}>[]? recordType, Param... parameters)
                                returns @tainted table<record {}>[]|()|Error {
        if (!self.clientActive) {
            return self.handleStoppedClientInvocation();
        }
        return self.jdbcClient->call(sqlQuery, recordType, ...parameters);
    }

    # The select remote function implementation for JDBC Client to select data from tables.
    #
    # + sqlQuery - SQL query to execute
    # + recordType - Type of the returned table
    # + parameters - The parameters to be passed to the select query. The number of parameters is variable
    # + return - A `table` returned by the sql query statement else `Error` will be returned if there
    # is any error
    public remote function select(@untainted string sqlQuery, typedesc<record{}>? recordType, Param... parameters)
                                  returns @tainted table<record {}>|Error {
        if (!self.clientActive) {
            return self.handleStoppedClientInvocation();
        }
        return self.jdbcClient->select(sqlQuery, recordType, ...parameters);
    }

    # The update remote function implementation for JDBC Client to update data and schema of the database.
    #
    # + sqlQuery - SQL statement to execute
    # + parameters - The parameters to be passed to the update query. The number of parameters is variable
    # + return - `UpdateResult` with the updated row count and key column values,
    #             else  `Error` will be returned if there is any error
    public remote function update(@untainted string sqlQuery, Param... parameters)
                                  returns UpdateResult|Error {
        if (!self.clientActive) {
            return self.handleStoppedClientInvocation();
        }
        return self.jdbcClient->update(sqlQuery, ...parameters);
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
        if (!self.clientActive) {
            return self.handleStoppedClientInvocationForBatchUpdate();
        }
        return self.jdbcClient->batchUpdate(sqlQuery, rollbackAllInFailure, ...parameters);
    }

    # Stops the JDBC client.
    # + return - Possible error during closing
    public function stop() returns error? {
        self.clientActive = false;
        return close(self.jdbcClient);
    }

    function handleStoppedClientInvocation() returns Error {
        ApplicationError e = error(message = "Client has been stopped");
        return e;
    }

    function handleStoppedClientInvocationForBatchUpdate() returns BatchUpdateResult {
        int[] rowCount = [];
        ApplicationError e = error(message = "Client has been stopped");
        BatchUpdateResult res = { updatedRowCount: rowCount, generatedKeys : {}, returnedError : e};
        return res;
    }
};

