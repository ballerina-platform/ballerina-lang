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

import ballerina/crypto;
import ballerinax/sql;
import ballerinax/java.jdbc;

public type Client client object {
    *sql:Client;
    jdbc:Client jdbcClient = new ({
         url: "jdbc:mysql://localhost:3306/CONNECT_DB",
         username: "root",
         password: "Test@123"
    });
    Options? options = ();
    sql:PoolOptions? poolOptions = ();

    public function __init(public string host="localhost", public int port=3306,
        public string? username = (), public string? password= (), public string? database = (),
        public Options? options = (), public sql:PoolOptions? connectionPool = ()){
        ClientConfiguration clientConfig = {
            host: host,
            port :3306,
            username: "",
            password: "",
            database: ""
        };
        string jdbcMySQL = "jdbc:mysql://"+clientConfig.host+":"+clientConfig.port.toString()+"/"+clientConfig.database;
        map<anydata> jdbcOptions = getJdbcOptions(clientConfig.options);
        sql:PoolOptions? poolOps = clientConfig?.poolOptions;
        if(poolOps is sql:PoolOptions){
           self.jdbcClient = new ({
              url: jdbcMySQL,
              username: clientConfig.username,
              password: clientConfig.password,
              dbOptions: jdbcOptions,
              poolOptions: getJdbcPoolOptions(poolOps)
           });
        } else {
           self.jdbcClient = new ({
              url: jdbcMySQL,
              username: clientConfig.username,
              password: clientConfig.password,
              dbOptions: jdbcOptions
          });
       }
    }

    //public function __init(public string host="localhost", public int? port=3306, public string? username = (),
    //                       public string? password= (), public string? database = (), public Options? options = (),
    //                       public sql:PoolOptions? connectionPool = ()){
    //
    //      string jdbcMySQL = "jdbc:mysql://"+host+":"+port.toString();
    //          map<anydata> jdbcOptions = {};
    //          //self.jdbcClient = new ({
    //          //                    url: jdbcMySQL,
    //          //                    username: "root",
    //          //                    password: "Test@123"
    //          //                    });
    //
    //         // if(poolOps is sql:PoolOptions){
    //         //    self.jdbcClient = new ({
    //         //       url: jdbcMySQL,
    //         //       username: username,
    //         //       password: password,
    //         //       dbOptions: jdbcOptions,
    //         //       poolOptions: getJdbcPoolOptions(poolOps)
    //         //    });
    //         // } else {
    //         //    self.jdbcClient = new ({
    //         //       url: jdbcMySQL,
    //         //       username: clientConfig.username,
    //         //       password: clientConfig.password,
    //         //       dbOptions: jdbcOptions
    //         //   });
    //         //}
    // }

    # The call remote function implementation for JDBC Client to invoke stored procedures/functions.
    #
    # + sqlQuery - The SQL stored procedure to execute
    # + recordType - Array of record types of the returned tables if there is any
    # + parameters - The parameters to be passed to the procedure/function call
    # + return - A `table[]` if there are tables returned by the call remote function and else nil,
    #            `Error` will be returned if there is any error
    public remote function call(@untainted string sqlQuery, typedesc<record {}>[]? recordType, sql:Param... parameters)
                                    returns @tainted table<record {}>[]|()|sql:Error {
       return self.jdbcClient->call(sqlQuery, recordType, ...parameters);
    }

    # The select remote function implementation for JDBC Client to select data from tables.
    #
    # + sqlQuery - SQL query to execute
    # + recordType - Type of the returned table
    # + parameters - The parameters to be passed to the select query
    # + return - A `table` returned by the SQL query statement else `Error` will be returned if there is an error
    public remote function select(@untainted string sqlQuery, typedesc<record{}>? recordType, sql:Param... parameters)
                                  returns @tainted table<record {}>|sql:Error {
        return self.jdbcClient->select(sqlQuery, recordType, ...parameters);
    }

    # The update remote function implementation for SQL Client to insert/delete/modify data and schema of the database.
    #
    # + sqlQuery - SQL statement to execute
    # + parameters - The parameters to be passed to the update query
    # + return - `UpdateResult` with the updated row count and key column values,
    #             else `Error` will be returned if there is an error
    public remote function update(@untainted string sqlQuery, sql:Param... parameters)
                                  returns sql:UpdateResult|sql:Error {
         return self.jdbcClient->update(sqlQuery, ...parameters);
    }

    # The batchUpdate remote function implementation for SQL Client to execute batch operations.
    #
    # + sqlQuery - SQL statement to execute
    # + parameters - Variable number of parameter arrays each representing the set of parameters belonging to each
    #                update statement
    # + rollbackAllInFailure - If one of the commands in a batch update fails to execute properly, the SQL driver
    #           may or may not continue to process the remaining commands in the batch. This property can be
    #           used to override this behavior. When it is set to true, if there is a failure in a few commands and
    #           the SQL driver continues with the remaining commands, the successfully executed commands in the batch
    #           also will get rolled back.
    # + return - A `BatchUpdateResult` with the updated row count and returned error if any. If all the commands
    #            in the batch have executed successfully, the error will be `nil`. If one or more commands have failed,
    #            the `returnedError` field will give the corresponding `Error` along with the int[] which
    #            contains updated row count or the status returned from each command in the batch.
    public remote function batchUpdate(@untainted string sqlQuery, boolean rollbackAllInFailure,
                                       sql:Param?[]... parameters)
                                       returns sql:BatchUpdateResult{
          return self.jdbcClient->batchUpdate(sqlQuery, rollbackAllInFailure, ...parameters);
    }

    # Close the SQL client.
    #
    # + return - Possible error during closing the client
    public function close() returns error?{
        return self.jdbcClient.close();
    }

};

function getJdbcOptions(Options options) returns map<anydata>{
     map<anydata> jdbcOptions = {};
     return jdbcOptions;
}

function getJdbcPoolOptions(sql:PoolOptions? poolOptions) returns jdbc:PoolOptions {
     jdbc:PoolOptions jdbcPool = {};
    //todo: set all props from sqlPoolOptions to jdbc Pooloptions
     return jdbcPool;
}

public type ClientConfiguration record {|
    *sql:ClientConfiguration;
    string host;
    int port = 3306; //how to handle no port case?
    string database;
    sql:PoolOptions poolOptions?;
    Options options = {};
|};

public type Options record{|
    //https://blog.querypie.com/mysql-ssl-connection-using-jdbc/
    // https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-configuration-properties.html
    // To support CA and hostname, certs should be validated.
    // And this require the keys to be imported in the turst store via
    // java key tool. As this will be exposing the details of java, ignoring those properties.
    // If someone requires it, they can use JDBC connector.
    //*Specific to mysql 8.0.13 or later*
    //default is preferred
    SSLConfig? ssl = {};
    //Sets the collation used for client-server interaction on connection. In contrast to charset,
    // collation does not issue additional queries. If the specified collation is unavailable on the target server, the connection will fail.
    //A list of valid charsets for a server is retrievable with SHOW COLLATION.
    //The default collation (utf8mb4_general_ci) is supported from MySQL 5.5.
    //You should use an older collation (e.g. utf8_general_ci) for older MySQL.
    //default utf8mb4_general_ci
    string collation?;
    // Maximum allowed packet size to send to server. If not set, the value of system variable 'max_allowed_packet'
    // in server will be used to initialize this upon connecting.
    // This value will not take effect if set larger than the value of 'max_allowed_packet'.
    //default is 65535. Is this required?
    int maxAllowedPacket?;
    //If 0, no read/write timeout (socketTimeout in JDBC properties).
    //let's set default to 30
    int readWriteTimeoutInSeconds?;
|};

//Empty record will map to REQUIRED.
public type SSLMode "PREFERRED"| "REQUIRED" | "VERIFY_CERT" | "VERIFY_IDENTITY";

public type SSLConfig record {|
  SSLMode mode = "PREFERRED";
   //Setting clientKeystore or trustKeystore will enable VERIFY_CA
  crypto:KeyStore clientCertKeystore?;
  crypto:KeyStore trustCertKeystore?;
|};

