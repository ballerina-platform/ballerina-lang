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

import ballerinax/java;
import ballerina/sql;

# Represents a JDBC client.
#
public type Client client object {
    *sql:Client;
    private boolean clientActive = true;

    # Gets called when the JDBC client is instantiated.
    public function __init(public string url, public string? user = (), public string? password = (),
                           public Options? options = (), public sql:ConnectionPool? connPool = ()) returns sql:Error? {
      ClientConfiguration clientConf = {
        url: url,
        user: user,
        password: password,
        options:options,
        connPool: connPool
      };
      return createClient(self, clientConf, sql:getGlobalConnectionPool());
    }

    # Stops the JDBC client.
    #
    # + return - Possible error during closing the client
    public function close() returns error? {
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
# + options - A map of DB specific `Options`.
# + connPool - Properties for the connection pool configuration. Refer `sql:ConnectionPool` for more details
type ClientConfiguration record {|
    string? url;
    string? user;
    string? password;
    Options? options;
    sql:ConnectionPool? connPool;
|};


function createClient(Client jdbcClient, ClientConfiguration clientConf,
    sql:ConnectionPool globalConnPool) returns sql:Error? = @java:Method {
    class: "org.ballerinalang.jdbc.NativeImpl"
} external;


function close(Client jdbcClient) returns error? = @java:Method {
    class: "org.ballerinalang.jdbc.NativeImpl"
} external;
