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

import ballerinax/java;
import ballerina/sql;

# Represents a JDBC client endpoint.
#
public type Client client object {
    *sql:Client;
    private boolean clientActive = true;

    # Gets called when the JDBC client is instantiated.
    public function __init(public string url, public string? user = (), public string? password = (),
                           public string|Driver? driver = (),
                           public map<anydata>? options = (), public sql:ConnectionPoolOptions? connPool = ()) {
      ClientConfiguration clientConf = {
        url: url,
        user: user,
        password: password,
        driver: driver,
        options:options,
        connPool: connPool
      };
      createClient(self, clientConf, globalPoolConfigContainer.getGlobalConnectionPoolOptions());
    }

      # The call remote function implementation for SQL Client to invoke stored procedures/functions.
      #
      # + sqlQuery - The SQL query such as SELECT statements which returns the table rows.
      # + params - The parameters to be passed to query.
      # + rowType - The type description of the record that should be returns in the retuned stream.
      # + return - A `stream<record{}>` containing the records of the query results.
      public remote function query(@untainted string sqlQuery, sql:Value[]? params = (), typedesc<record {}>? rowType =())
                                  returns @tainted stream<record {}> {
        //TODO: handle invocation after client is closed.
        return nativeQuery(self, java:fromString(sqlQuery), params, rowType);
    }

    # Stops the JDBC client.
    #
    # + return - Possible error during closing the client
    public function close() returns error? {
        self.clientActive = false;
        return close(self);
    }
};

function nativeQuery(Client jdbcClient, @untainted handle sqlQuery,
    sql:Value[]? params, typedesc<record {}>? rowType) returns @tainted stream<record {}> = @java:Method {
        class: "org.ballerinalang.jdbc.methods.ExternActions"
    } external;

function createClient(Client jdbcClient, ClientConfiguration clientConf,
    sql:ConnectionPoolOptions globalConnPool) = @java:Method {
    class: "org.ballerinalang.jdbc.methods.ExternFunctions"
} external;

function close(Client jdbcClient) returns error? = @java:Method {
    class: "org.ballerinalang.jdbc.methods.ExternFunctions"
} external;
