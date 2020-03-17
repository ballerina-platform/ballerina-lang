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

import ballerina/config;
import ballerina/java;

# Represents the properties which are used to configure DB connection pool.
# Default values of the fields can be set through the configuration API.
#
# + maxOpenConnections - The maximum number of open connections that the pool is allowed to have, including
#                        both idle and in-use connections. Default value is 15 and it can be changed through the
#                        configuration API with the key `b7a.sql.pool.maxOpenConnections`.
# + maxConnectionLifeTimeInSeconds - The maximum lifetime of a connection in the pool. Default value is 1800 seconds
#                                  (30 minutes) and it can be changed through the configuration API with the
#                                  key `b7a.sql.pool.maxConnectionLifeTimeInSeconds`. A value of 0 indicates unlimited maximum
#                                  lifetime (infinite lifetime).
# + minIdleConnections - The minimum number of idle connections that pool tries to maintain in the pool. Default
#                        is the same as maxOpenConnections and it can be changed through the configuration
#                        API with the key `b7a.sql.pool.minIdleConnections`.
public type ConnectionPool record {|
    int maxOpenConnections = config:getAsInt("b7a.sql.pool.maxOpenConnections", 15);
    decimal maxConnectionLifeTimeInSeconds = <decimal>config:getAsFloat("b7a.sql.pool.maxConnectionLifeTimeInSeconds",
        1800.0);
    int minIdleConnections = config:getAsInt("b7a.sql.pool.minIdleConnections", 15);
|};

// This is a container object that holds the global pool config and initializes the internal map of connection pools
type GlobalConnectionPoolContainer object {
    private ConnectionPool connectionPool = {};

    function __init() {
        // poolConfig record is frozen so that it cannot be modified during runtime
        ConnectionPool frozenConfig = self.connectionPool.cloneReadOnly();
        initGlobalPoolContainer(frozenConfig);
    }

    public function getGlobalConnectionPool() returns ConnectionPool {
        return self.connectionPool;
    }
};

function initGlobalPoolContainer(ConnectionPool poolConfig) = @java:Method {
    class: "org.ballerinalang.sql.utils.ConnectionPoolUtils"
} external;

// This is an instance of GlobalPoolConfigContainer object type. The __init functions of database clients pass
// poolConfig member of this instance to the external client creation logic in order to access the internal map
// of connection pools.
final GlobalConnectionPoolContainer globalPoolContainer = new;

public function getGlobalConnectionPool() returns ConnectionPool {
    return globalPoolContainer.getGlobalConnectionPool();
}
