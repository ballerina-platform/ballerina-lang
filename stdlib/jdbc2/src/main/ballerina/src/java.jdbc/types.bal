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

import ballerina/config;
import ballerinax/java;
import ballerinax/sql;

# Provides a set of configurations for the JDBC Client.
#
# + url - URL of the database to connect
# + dbOptions - A map of DB specific properties. These properties will have an effect only if the dataSourceClassName is
#               provided in poolOptions
# + poolOptions - Connection pool configurations to be used in the client
public type ClientConfiguration record {|
    *sql:ClientConfiguration;
    string url = "";
    PoolOptions poolOptions?;
    map<anydata> dbOptions = {};
|};

# Represents the properties which are used to configure DB connection pool.
# Default values of the fields can be set through the configuration API.
#
# + dataSourceClassName - Name of the DataSource class provided by the JDBC driver. This is used on following scenarios.
#                         1. In JDBC client when DB specific properties are required (which are given with dbOptions)
#                         2. In any data client in which XA transactions enabled by isXA property and need to provide a
#                         custom XA implementation.Default value is none and it can be changed through
#                         the configuration API with the key `b7a.jdbc.pool.dataSourceClassName`.
public type PoolOptions record {|
    *sql:PoolOptions;
    string dataSourceClassName = config:getAsString("b7a.jdbc.pool.dataSourceClassName", "");
|};

// This is a container object that holds the global pool config and initializes the internal map of connection pools
type GlobalPoolConfigContainer object {
    private PoolOptions poolConfig = {};

    function __init() {
        // poolConfig record is frozen so that it cannot be modified during runtime
        PoolOptions frozenConfig = self.poolConfig.cloneReadOnly();
        self.initGlobalPoolContainer(frozenConfig);
    }

    function initGlobalPoolContainer(PoolOptions poolConfig) {
        return initGlobalPoolContainer(self, poolConfig);
    }

    function getGlobalPoolConfig() returns PoolOptions {
        return self.poolConfig;
    }
};

function initGlobalPoolContainer(GlobalPoolConfigContainer poolConfigContainer, PoolOptions poolConfig) = @java:Method {
    class: "org.ballerinax.jdbc.methods.ExternFunctions"
} external;

// This is an instance of GlobalPoolConfigContainer object type. The __init functions of database clients pass
// poolConfig member of this instance to the external client creation logic in order to access the internal map
// of connection pools.
final GlobalPoolConfigContainer globalPoolConfigContainer = new;