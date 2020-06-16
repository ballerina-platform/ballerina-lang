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

import ballerina/java.jdbc;
import ballerina/sql;

function testConnection1(string jdbcURL, string user, string password) returns error? {
    jdbc:Client testDB = check new (url = jdbcURL, user = user, password = password);
    return testDB.close();
}

function testConnection2(string jdbcURL, string user, string password) returns error? {
    jdbc:Client testDB = check new (jdbcURL, user, password);
    return testDB.close();
}

function testConnectionNoUserPassword(string jdbcURL) returns error? {
    jdbc:Client|sql:Error dbClient = new (jdbcURL);
    if (dbClient is sql:Error) {
        return dbClient;
    } else {
        return dbClient.close();
    }
}

function testConnectionWithValidDriver(string jdbcURL, string user, string password) returns error? {
    jdbc:Client|sql:Error dbClient = new (jdbcURL, user, password, {datasourceName: "org.h2.jdbcx.JdbcDataSource"});
    if (dbClient is sql:Error) {
        return dbClient;
    } else {
        return dbClient.close();
    }
}

function testConnectionWithInvalidDriver(string jdbcURL, string user, string password) returns error? {
    jdbc:Client|sql:Error dbClient = new (jdbcURL, user, password,
        {datasourceName: "org.h2.jdbcx.JdbcDataSourceInvalid"});
    if (dbClient is sql:Error) {
        return dbClient;
    } else {
        return dbClient.close();
    }
}

function testConnectionWithDatasourceOptions(string jdbcURL, string user, string password) returns error? {
    jdbc:Options options = {
        datasourceName: "org.h2.jdbcx.JdbcDataSource",
        properties: {"loginTimeout": 5000}
    };
    jdbc:Client|sql:Error dbClient = new (jdbcURL, user, password, options);
    if (dbClient is sql:Error) {
        return dbClient;
    } else {
        return dbClient.close();
    }
}

function testConnectionWithDatasourceInvalidProperty(string jdbcURL, string user, string password) returns error? {
    jdbc:Options options = {
        datasourceName: "org.h2.jdbcx.JdbcDataSource",
        properties: {"invalidProperty": 109}
    };
    jdbc:Client|sql:Error dbClient = new (jdbcURL, user, password, options);
    if (dbClient is sql:Error) {
        return dbClient;
    } else {
        return dbClient.close();
    }
}

function testWithConnectionPool(string jdbcURL, string user, string password) returns error|sql:ConnectionPool {
    sql:ConnectionPool connectionPool = {
        maxOpenConnections: 25
    };
    jdbc:Client dbClient = check new (url = jdbcURL, user = user,
        password = password, connectionPool = connectionPool);
    error? err = dbClient.close();
    if (err is error) {
        return err;
    } else {
        return connectionPool;
    }
}

function testWithSharedConnPool(string jdbcURL, string user, string password) returns error? {
    sql:ConnectionPool connectionPool = {
        maxOpenConnections: 25
    };
    jdbc:Client dbClient1 = check new (url = jdbcURL, user = user,
        password = password, connectionPool = connectionPool);
    jdbc:Client dbClient2 = check new (url = jdbcURL, user = user,
        password = password, connectionPool = connectionPool);
    jdbc:Client dbClient3 = check new (url = jdbcURL, user = user,
        password = password, connectionPool = connectionPool);

    check dbClient1.close();
    check dbClient2.close();
    check dbClient3.close();
}

function testWithAllParams(string jdbcURL, string user, string password) returns error? {
    jdbc:Options options = {
        datasourceName: "org.h2.jdbcx.JdbcDataSource",
        properties: {"loginTimeout": 5000}
    };
    sql:ConnectionPool connectionPool = {
        maxOpenConnections: 25
    };
    jdbc:Client dbClient = check new (jdbcURL, user, password, options, connectionPool);
    check dbClient.close();
}
