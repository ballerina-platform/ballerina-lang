// Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/mysql;
import ballerina/sql;

//TODO:Remove this and pass with functions.
//After fixing this https://github.com/ballerina-platform/ballerina-lang/issues/21259
string host = "localhost";
string user = "test";
string password = "test123";
string database = "CONNECT_DB";
int port = 3305;

function testConnectionWithNoFields() returns error? {
    mysql:Client | sql:Error dbClient = new ();
    if (dbClient is sql:Error) {
        return dbClient;
    } else {
        return dbClient.close();
    }
}

function testWithURLParams(string host, string user, string password, string database, int port) returns error? {
    mysql:Client dbClient = check new (host, user, password, database, port);
    return dbClient.close();
}

function testWithoutHost() returns error? {
    mysql:Client dbClient = check new (user = user, password = password, database = database, port = port);
    return dbClient.close();
}

function testWithOptions() returns error? {
    mysql:Options options = {
        ssl: (),
        connectTimeoutInSeconds: 60
    };
    mysql:Client dbClient = check new (user = user, password = password, database = database,
    port = port, options = options);
    return dbClient.close();
}

function testWithConnectionPool() returns error | sql:ConnectionPool {
    sql:ConnectionPool connPool = {
        maxOpenConnections: 25
    };
    mysql:Client dbClient = check new (user = user, password = password, database = database,
    port = port, connPool = connPool);
    error? err = dbClient.close();
    if (err is error) {
        return err;
    } else {
        return connPool;
    }
}

function testWithConnectionParams() returns error? {
    sql:ConnectionPool connPool = {
        maxOpenConnections: 25
    };
    mysql:Options options = {
        ssl: (),
        connectTimeoutInSeconds: 60
    };
    mysql:Client dbClient = check new (host, user, password, database, port, options, connPool);
    return dbClient.close();
}