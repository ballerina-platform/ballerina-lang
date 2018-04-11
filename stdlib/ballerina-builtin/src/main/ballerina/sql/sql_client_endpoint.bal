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

package ballerina.sql;

///////////////////////////////
// SQL Client Endpoint
///////////////////////////////

@Description {value:"Represents an SQL client endpoint"}
@Field {value:"epName: The name of the endpoint"}
@Field {value:"config: The configurations associated with the endpoint"}
public type Client object {
    public {
        string epName;
        ClientEndpointConfiguration config;
        SQLClient sqlClient;
    }

    @Description {value:"Gets called when the endpoint is being initialized during the package initialization."}
    public function init(ClientEndpointConfiguration config);

    public function register(typedesc serviceType) {
    }

    public function start() {
    }

    @Description {value:"Returns the connector that client code uses"}
    @Return {value:"The connector that client code uses"}
    public function getClient() returns SQLClient {
        return self.sqlClient;
    }

    @Description {value:"Stops the registered service"}
    @Return {value:"Error occured during registration"}
    public function stop() {
    }
};

@Description {value:"PoolOptions structs represents the properties which are used to configure DB connection pool"}
@Field {value:"connectionInitSql:  SQL statement that will be executed after every new connection creation before adding it to the pool"}
@Field {value:"dataSourceClassName: Name of the DataSource class provided by the JDBC driver"}
@Field {value:"autoCommit: Auto-commit behavior of connections returned from the pool"}
@Field {value:"isXA:  Whether Connections are used for a distributed transaction"}
@Field {value:"maximumPoolSize: Maximum size that the pool is allowed to reach, including both idle and in-use connections"}
@Field {value:"connectionTimeout: Maximum number of milliseconds that a client will wait for a connection from the pool"}
@Field {value:"idleTimeout: Maximum amount of time that a connection is allowed to sit idle in the pool"}
@Field {value:"minimumIdle: Minimum number of idle connections that pool tries to maintain in the pool"}
@Field {value:"maxLifetime: Maximum lifetime of a connection in the pool"}
@Field {value:"validationTimeout:  Maximum amount of time that a connection will be tested for aliveness"}
@Field {value:"datasourceProperties: Data source specific properties which are used along with the dataSourceClassName"}
public type PoolOptions {
    string connectionInitSql = "",
    string dataSourceClassName = "",
    boolean autoCommit = true,
    boolean isXA = false,
    int maximumPoolSize = -1,
    int connectionTimeout = -1,
    int idleTimeout = -1,
    int minimumIdle = -1,
    int maxLifetime = -1,
    int validationTimeout = -1,
    map datasourceProperties,
};

@Description {value:"The Client endpoint configuration for SQL databases."}
@Field {value:"url: URL of the database to connect"}
@Field {value:"username: Username for the database connection"}
@Field {value:"password: Password for the database connection"}
@Field {value:"poolOptions: Properties for the connection pool configuration"}
public type ClientEndpointConfiguration {
    string url= "",
    string username = "",
    string password = "",
    PoolOptions poolOptions,
};

public native function createSQLClient(ClientEndpointConfiguration config) returns SQLClient;

public function Client::init(ClientEndpointConfiguration config) {
    self.sqlClient = createSQLClient(config);
}
