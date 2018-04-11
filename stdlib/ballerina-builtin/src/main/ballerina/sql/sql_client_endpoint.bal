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
        ClientEndpointConfig config;
        SQLClient sqlClient;
    }

    @Description {value:"Gets called when the endpoint is being initialized during the package initialization."}
    public function init(ClientEndpointConfig config);

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

@Description {value:"ConnectionProperties structs represents the properties which are used to configure DB connection pool"}
@Field {value:"url: Platform independent DB access URL"}
@Field {value:"dataSourceClassName: Name of the DataSource class provided by the JDBC driver"}
@Field {value:"connectionTestQuery: Query that will be executed to validate that the connection to the database is still alive"}
@Field {value:"poolName: User-defined name for the connection pool and appears mainly in logging"}
@Field {value:"catalog: Catalog of connections created by this pool"}
@Field {value:"connectionInitSql:  SQL statement that will be executed after every new connection creation before adding it to the pool"}
@Field {value:"driverClassName: Fully qualified Java class name of the JDBC driver to be used"}
@Field {value:"transactionIsolation:  Transaction isolation level of connections returned from the pool. The supported values are TRANSACTION_READ_UNCOMMITTED, TRANSACTION_READ_COMMITTED, TRANSACTION_REPEATABLE_READ and TRANSACTION_SERIALIZABLE"}
@Field {value:"autoCommit: Auto-commit behavior of connections returned from the pool"}
@Field {value:"isolateInternalQueries: Determines whether HikariCP isolates internal pool queries, such as the connection alive test, in their own transaction"}
@Field {value:"allowPoolSuspension: Whether the pool can be suspended and resumed through JMX"}
@Field {value:"readOnly:  Whether Connections obtained from the pool are in read-only mode by default"}
@Field {value:"isXA:  Whether Connections are used for a distributed transaction"}
@Field {value:"maximumPoolSize: Maximum size that the pool is allowed to reach, including both idle and in-use connections"}
@Field {value:"connectionTimeout: Maximum number of milliseconds that a client will wait for a connection from the pool"}
@Field {value:"idleTimeout: Maximum amount of time that a connection is allowed to sit idle in the pool"}
@Field {value:"minimumIdle: Minimum number of idle connections that pool tries to maintain in the pool"}
@Field {value:"maxLifetime: Maximum lifetime of a connection in the pool"}
@Field {value:"validationTimeout:  Maximum amount of time that a connection will be tested for aliveness"}
@Field {value:"leakDetectionThreshold: Amount of time that a connection can be out of the pool before a message is logged indicating a possible connection leak"}
@Field {value:"datasourceProperties: Data source specific properties which are used along with the dataSourceClassName"}
public type ConnectionProperties {
    string url = "",
    string dataSourceClassName = "",
    string connectionTestQuery = "",
    string poolName = "",
    string catalog = "",
    string connectionInitSql = "",
    string driverClassName = "",
    string transactionIsolation = "",
    boolean autoCommit = true,
    boolean isolateInternalQueries = false,
    boolean allowPoolSuspension = false,
    boolean readOnly = false,
    boolean isXA = false,
    int maximumPoolSize = -1,
    int connectionTimeout = -1,
    int idleTimeout = -1,
    int minimumIdle = -1,
    int maxLifetime = -1,
    int validationTimeout = -1,
    int leakDetectionThreshold = -1,
    map datasourceProperties,
};

@Description {value:"The Client endpoint configuration for SQL databases."}
@Field {value:"database: SQL database type"}
@Field {value:"host: Host name of the database or file path for file based database"}
@Field {value:"port: Port of the database"}
@Field {value:"name: Name of the database to connect"}
@Field {value:"username: Username for the database connection"}
@Field {value:"password: Password for the database connection"}
@Field {value:"options: ConnectionProperties for the connection pool configuration"}
public type ClientEndpointConfig {
    DB database,
    string host = "",
    int port = 0,
    string name = "",
    string username = "",
    string password = "",
    ConnectionProperties options,
};

public native function createSQLClient(ClientEndpointConfig config) returns SQLClient;

public function Client::init(ClientEndpointConfig config) {
    self.sqlClient = createSQLClient(config);
}
