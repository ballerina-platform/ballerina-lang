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
                           public string? driver = (),
                           public map<anydata>? options = (), public sql:ConnectionPool? connPool = ()) returns sql:Error?{
      ClientConfiguration clientConf = {
        url: url,
        user: user,
        password: password,
        driver: driver,
        options:options,
        connPool: connPool
      };
      createClient(self, clientConf, sql:getGlobalConnectionPool());
    }

      # The call remote function implementation for SQL Client to invoke stored procedures/functions.
      #
      # + sqlQuery - The SQL query such as SELECT statements which returns the table rows.
      # + rowType - The type description of the record that should be returns in the retuned stream.
      # + return - A `stream<record{}>` containing the records of the query results.
      public remote function query(@untainted string sqlQuery, typedesc<record {}>? rowType =())
                                  returns @tainted stream<record {}>|sql:Error {
        //TODO: handle invocation after client is closed.
        return nativeQuery(self, java:fromString(sqlQuery), rowType);
    }

    # Stops the JDBC client.
    #
    # + return - Possible error during closing the client
    public function close() returns error? {
        self.clientActive = false;
        return close(self);
    }
};

# Provides a set of configurations for the JDBC Client to be passed internally within the module.
#
# + url - URL of the database to connect
# + user - Username for the database connection
# + password - Password for the database connection
# + driver - The name of the drirver or optionally pass the `Driver` record to specify the isXA configuraiton.
# + options - A map of DB specific properties. These properties will have an effect only if the dataSourceClassName is
#               provided in poolOptions
# + connPool - Properties for the connection pool configuration. Refer `sql:ConnectionPool` for more details
type ClientConfiguration record {|
    string url;
    string? user;
    string? password;
    string? driver;
    map<anydata>? options;
    sql:ConnectionPool? connPool;
|};


function createClient(Client jdbcClient, ClientConfiguration clientConf,
    sql:ConnectionPool globalConnPool) = @java:Method {
    class: "org.ballerinalang.jdbc.NativeImpl"
} external;


function nativeQuery(Client jdbcClient, @untainted handle sqlQuery,
    typedesc<record {}>? rowType) returns @tainted stream<record {}> = @java:Method {
        class: "org.ballerinalang.jdbc.NativeImpl"
} external;


function close(Client jdbcClient) returns error? = @java:Method {
    class: "org.ballerinalang.jdbc.NativeImpl"
} external;
