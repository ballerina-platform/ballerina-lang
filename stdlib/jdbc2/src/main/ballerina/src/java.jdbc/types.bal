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
    string|Driver? driver;
    map<anydata>? options;
    sql:ConnectionPool? connPool;
|};


# Provides a set of configurations to configure the Driver details.
#
# + name - Driver class name
# + isXA - Boolean value to enable distributed transactions.
public type Driver record {|
    string name;
    boolean? isXA;
|};

// This is a container object that holds the global pool config and initializes the internal map of connection pools
type GlobalConnectionPoolContainer object {
    private sql:ConnectionPool connectionPoolOpt = {};

    function __init() {
        // poolConfig record is frozen so that it cannot be modified during runtime
        sql:ConnectionPool frozenConfig = self.connectionPoolOpt.cloneReadOnly();
        initGlobalPoolContainer(self, frozenConfig);
    }

    function getGlobalConnectionPool() returns sql:ConnectionPool {
        return self.connectionPoolOpt;
    }
};

function initGlobalPoolContainer(GlobalConnectionPoolContainer poolConfigContainer,
                                 sql: ConnectionPool poolConfig) = @java:Method {
    class: "org.ballerinalang.jdbc.nativeImpl.Utils"
} external;

// This is an instance of GlobalPoolConfigContainer object type. The __init functions of database clients pass
// poolConfig member of this instance to the external client creation logic in order to access the internal map
// of connection pools.
final GlobalConnectionPoolContainer globalPoolConfigContainer = new;