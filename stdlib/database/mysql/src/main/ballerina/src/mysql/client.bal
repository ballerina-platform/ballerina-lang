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

import ballerina/crypto;
import ballerina/sql;
import ballerinax/java;

# Represents a MySQL database client.
public type Client client object {
    *sql:Client;
    private boolean clientActive = true;

    public function __init(public string host = "localhost",
    public string? user = (), public string? password = (), public string? database = (),
    public int port = 3306, public Options? options = (),
    public sql:ConnectionPool? connectionPool = ()) returns sql:Error? {
        ClientConfiguration clientConfig = {
            host: host,
            port: port,
            user: user,
            password: password,
            database: database,
            options: options,
            connectionPool: connectionPool
        };
        return createClient(self, clientConfig, sql:getGlobalConnectionPool());
    }

    # Executes the sql query provided by the user, and returns the result as stream.
    #
    # + sqlQuery - The query which needs to be executed
    # + rowType - The `typedesc` of the record that should be returned as a result. If this is not provided the default
    #             column names of the query result set be used for the record attributes
    # + return - Stream of records in the type of `rowType`
    public function query(@untainted string sqlQuery, typedesc<record {}>? rowType = ()) returns stream<record{}, sql:Error> {
        if(self.clientActive){
            return nativeQuery(self, java:fromString(sqlQuery) , rowType);
        } else {
           return sql:generateApplicationErrorStream("MySQL Client is already closed, hence further operations are not allowed");
        }
    }

    # Executes the DML sql queries provided by the user, and returns summary of the execution.
    #
    # + sqlQuery - The DML query such as INSERT, DELETE, UPDATE, etc
    # + return - Summary of the sql update query as `sql:ExecuteResult` or returns `sql:Error`
    #           if any error occured when executing the query
    public function execute(@untainted string sqlQuery) returns sql:ExecuteResult|sql:Error?{
        if(self.clientActive){
            return nativeExecute(self, java:fromString(sqlQuery));
        } else {
           return sql:ApplicationError(message = "JDBC Client is already closed, hence further operations are not allowed");
        }
    }


    # Close the SQL client.
    #
    # + return - Possible error during closing the client
    public function close() returns sql:Error? {
        self.clientActive = false;
        return close(self);
    }
};

# Provides a set of configurations for the mysql client to be passed internally within the module.
#
# + host - URL of the database to connect
# + port - Port of the database to connect
# + user - Username for the database connection
# + password - Password for the database connection
# + database - Name of the database
# + options - Mysql datasource `Options` to be configured
# + connectionPool - Properties for the connection pool configuration. Refer `sql:ConnectionPool` for more details
type ClientConfiguration record {|
    string host;
    int port;
    string? user;
    string? password;
    string? database;
    Options? options;
    sql:ConnectionPool? connectionPool;
|};

# MySQL database options.
#
# + ssl - SSL Configuration to be used
# + useXADatasource - Boolean value to enable XADatasource
# + connectTimeoutInSeconds - Timeout to be used when connecting to the mysql server
# + socketTimeoutInSeconds - Socket timeout during the read/write operations with mysql server,
#                            0 means no socket timeout
public type Options record {|
    SSLConfig? ssl = {};
    boolean useXADatasource = false;
    decimal connectTimeoutInSeconds = 30;
    decimal socketTimeoutInSeconds = 0;
|};

# Possible options for SSL Mode.
public type SSLMode "PREFERRED" | "REQUIRED" | "VERIFY_CERT" | "VERIFY_IDENTITY";

# SSL Configuration to be used when connecting to mysql server.
#
# + mode - `SSLMode` to be usedduring the connection
# + clientCertKeystore - Keystore configuration of the client certificates
# + trustCertKeystore - Keystore configurtion of the trust certificates
#
public type SSLConfig record {|
    SSLMode mode = "PREFERRED";
    crypto:KeyStore clientCertKeystore?;
    crypto:KeyStore trustCertKeystore?;
|};

function createClient(Client mysqlClient, ClientConfiguration clientConf,
sql:ConnectionPool globalConnPool) returns sql:Error? = @java:Method {
    class: "org.ballerinalang.mysql.NativeImpl"
} external;

function nativeQuery(Client sqlClient, @untainted handle sqlQuery, typedesc<record {}>? rowtype) returns stream<record{}, sql:Error> = @java:Method {
    class: "org.ballerinalang.sql.utils.QueryUtils"
} external;

function nativeExecute(Client sqlClient, @untainted handle sqlQuery) returns sql:ExecuteResult|sql:Error? = @java:Method {
    class: "org.ballerinalang.sql.utils.ExecuteUtils"
} external;

function close(Client mysqlClient) returns sql:Error? = @java:Method {
    class: "org.ballerinalang.mysql.NativeImpl"
} external;
