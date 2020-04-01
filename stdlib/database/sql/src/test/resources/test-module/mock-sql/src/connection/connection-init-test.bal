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

import mockclient;
import ballerina/sql;

function testConnection1(string url, string user, string password) returns error? {
    mockclient:Client testDB = check new (url = url, user = user, password = password);
    return testDB.close();
}

function testConnection2(string url, string user, string password) returns error? {
    mockclient:Client testDB = check new (url, user, password);
    return testDB.close();
}

function testConnectionNoUserPassword(string url) returns error? {
    mockclient:Client|sql:Error dbClient = new (url);
    if (dbClient is sql:Error) {
        return dbClient;
    } else {
        return dbClient.close();
    }
}

function testConnectionWithValidDriver(string url, string user, string password) returns error? {
    mockclient:Client|sql:Error dbClient = new (url, user, password, "org.hsqldb.jdbc.JDBCDataSource");
    if (dbClient is sql:Error) {
        return dbClient;
    } else {
        return dbClient.close();
    }
}

function testConnectionWithInvalidDriver(string url, string user, string password) returns error? {
    mockclient:Client|sql:Error dbClient = new (url, user, password,
        "org.hsqldb.jdbc.JDBCDataSourceIvalid");
    if (dbClient is sql:Error) {
        return dbClient;
    } else {
        return dbClient.close();
    }
}

function testConnectionWithDatasourceOptions(string url, string user, string password) returns error? {
    mockclient:Client|sql:Error dbClient = new (url, user, password, "org.hsqldb.jdbc.JDBCDataSource",
        {"loginTimeout": 5000});
    if (dbClient is sql:Error) {
        return dbClient;
    } else {
        return dbClient.close();
    }
}

function testConnectionWithDatasourceInvalidProperty(string url, string user, string password) returns error? {
    mockclient:Client|sql:Error dbClient = new (url, user, password, "org.hsqldb.jdbc.JDBCDataSource",
        {"invalidProperty": 10});
    if (dbClient is sql:Error) {
        return dbClient;
    } else {
        return dbClient.close();
    }
}

function testWithConnectionPool(string url, string user, string password) returns error|sql:ConnectionPool {
    sql:ConnectionPool connectionPool = {
        maxOpenConnections: 25
    };
    mockclient:Client dbClient = check new (url = url, user = user,
        password = password, connectionPool = connectionPool);
    error? err = dbClient.close();
    if (err is error) {
        return err;
    } else {
        return connectionPool;
    }
}

function testWithSharedConnPool(string url, string user, string password) returns error? {
    sql:ConnectionPool connectionPool = {
        maxOpenConnections: 25
    };
    mockclient:Client dbClient1 = check new (url = url, user = user,
        password = password, connectionPool = connectionPool);
    mockclient:Client dbClient2 = check new (url = url, user = user,
        password = password, connectionPool = connectionPool);
    mockclient:Client dbClient3 = check new (url = url, user = user,
        password = password, connectionPool = connectionPool);

    check dbClient1.close();
    check dbClient2.close();
    check dbClient3.close();
}

function testWithAllParams(string url, string user, string password) returns error? {
    sql:ConnectionPool connectionPool = {
        maxOpenConnections: 25
    };
    mockclient:Client dbClient = check new (url, user, password, "org.hsqldb.jdbc.JDBCDataSource",
        {"loginTimeout": 5000}, connectionPool);
    check dbClient.close();
}
